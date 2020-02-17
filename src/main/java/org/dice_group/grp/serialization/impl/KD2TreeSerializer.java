package org.dice_group.grp.serialization.impl;

import grph.Grph;
import org.dice_group.grp.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class KD2TreeSerializer{


    public byte[] serialize(Grph g, BoundedList propertyIndex) throws IOException {
        Map<Integer, LabledMatrix> matrices = new HashMap<Integer, LabledMatrix>();
        //create matrices
        Set<Integer> vertices = new HashSet<Integer>();
        for(int edge : g.getEdges()){
            vertices.add(g.getDirectedSimpleEdgeHead(edge));
            vertices.add(g.getDirectedSimpleEdgeTail(edge));
        }
        for(IndexedRDFNode node : propertyIndex){
            for(int edge=node.getLowerBound(); edge< node.getUpperBound();edge++) {
                //use propertyIndex and use first label for all in the boundedlist, also we need a HDT Index for them
                if(node.getHdtIndex() == null){
                    continue;
                }
                int edgeIndex = node.getHdtIndex();
                if (!matrices.containsKey(edgeIndex)) {
                    matrices.put(edgeIndex, new LabledMatrix());
                }
                matrices.get(edgeIndex).set(g.getDirectedSimpleEdgeTail(edge), g.getDirectedSimpleEdgeHead(edge));
            }
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int x=0;
        Double size = Math.pow(2, Math.ceil(log(g.getVertices().size(), 2)));
        for(Integer key : matrices.keySet()){
            LabledMatrix matrix = matrices.get(key);
            List<Integer[]> queue = new ArrayList<Integer[]>();
            KD2Tree tree = new KD2Tree(matrix.getLabelId());

            queue.add(new Integer[]{0, size.intValue(), 0, size.intValue()});
            //check if dividable
            while(!queue.isEmpty()) {
                Integer[] queueItem = queue.remove(0);

                int rowDiv = (queueItem[1] - queueItem[0]) / 2 + queueItem[0];
                int colDiv = (queueItem[3] - queueItem[2]) / 2 + queueItem[2];
                // queueItem[3] has to be done the same way, so we have xDiv and yDiv instead one divIndex
                //except if b-a = 1 if b-a=1 and d-c=1 -> we have a leaf, otherwise: set rowDiv (resp. colDiv) to a (resp. c)
                if ((queueItem[1] - queueItem[0]) == 0 && (queueItem[3] - queueItem[2]) == 0) {
                    //leaf we do not have to do anything, as the leaf is already saved, also leafs ==1 are always at the same h level.
                    continue;
                }
                if ((queueItem[1] - queueItem[0]) == 1) {
                    rowDiv = queueItem[0];
                } else if ((queueItem[1] - queueItem[0]) % 2 != 0) {
                    //rowDiv = queueItem[1] - 1;
                }
                if ((queueItem[3] - queueItem[2]) == 1) {
                    colDiv = queueItem[2];
                } else if ((queueItem[3] - queueItem[2]) % 2 != 0) {
                   // colDiv = queueItem[3] - 1;
                }
                Byte[] vals = containsOne(matrix, rowDiv, colDiv, queueItem[0], queueItem[1], queueItem[2], queueItem[3]);
                //row column
                //add nodes to tree, the tree will automatically  save  it correctly.
                tree.addNodeNeighbor(vals);
                if (vals[0] > 0) {
                    queue.add(new Integer[]{queueItem[0], rowDiv , queueItem[2], colDiv });
                }
                if (vals[1] > 0) {
                    queue.add(new Integer[]{queueItem[0], rowDiv, colDiv + 1, queueItem[3]});
                }
                if (vals[2] > 0) {
                    queue.add(new Integer[]{rowDiv +1, queueItem[1], queueItem[2], colDiv });
                }
                if (vals[3] > 0) {
                    queue.add(new Integer[]{rowDiv + 1, queueItem[1], colDiv + 1, queueItem[3]});
                }
            }
            baos.write(tree.serialize());
            x++;
            if(x%10 ==0)
                System.out.println("Created "+x+" kd2 trees of "+matrices.size());
        }


        return baos.toByteArray();
    }

    /**
     * returns 4 bytes representing the submatrix values.
     * if a submatrix contains a 1, the representing byte will be 1, 0 otherwise
     *
     * @param matrix
     * @return
     */
    private Byte[] containsOne(LabledMatrix matrix, int rowDiv, int colDiv, int starti, int endi, int startj, int endj) {
        Byte[] ret = new Byte[]{0,0,0,0};

        for(Point p : matrix.getPoints()){
            if(p.getRow()>=starti && p.getRow()<=rowDiv &&
                p.getCol()>=startj && p.getCol()<=colDiv){
                ret[0] =1;
            }
            if(p.getRow()>=starti && p.getRow()<=rowDiv &&
                    p.getCol()>colDiv && p.getCol()<endj){
                ret[1] =1;
            }
            if(p.getRow()>rowDiv && p.getRow()<endi &&
                    p.getCol()>=startj && p.getCol()<=colDiv){
                ret[2] =1;
            }
            if(p.getRow()>rowDiv && p.getRow()<endi &&
                    p.getCol()>colDiv && p.getCol()<endj){
                ret[3] =1;
            }

            if(ret[0]==1 && ret[1] ==1 && ret[2]==1 && ret[3]==1){
                return ret;
            }
        }

        return ret;
/*
        for(int i=starti;i<=rowDiv  && ret[0]!=1; i++) {
            for (int j = startj; j <= colDiv; j++) {
                if (matrix.get(i, j) > 0) {
                    ret[0] = 1;
                    break;
                }
            }
        }
        for(int i=starti;i<=rowDiv  && ret[1]!=1; i++) {
            for(int j=colDiv+1;j<endj; j++) {
                if (matrix.get(i,j) > 0){
                    ret[1]=1;
                    break;
                }
            }
        }
        for(int i=rowDiv+1;i<=endi && ret[2]!=1; i++) {
            for (int j = startj; j <= colDiv; j++) {
                if (matrix.get(i, j) > 0) {
                    ret[2] = 1;
                    break;
                }
            }
        }
        for(int i=rowDiv+1;i<=endi  && ret[3]!=1; i++) {
            for(int j=colDiv+1;j<endj; j++) {
                if (matrix.get(i,j) > 0){
                    ret[3]=1;
                    break;
                }
            }
        }
        return ret;
        */

    }

    private double log(int x, int base)
    {
        return  (Math.log(x) / Math.log(base));
    }
}
