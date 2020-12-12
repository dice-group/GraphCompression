package org.dice_group.k2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

//TODO work directly on ser
public class KD2Tree {

   private List<Byte> tree = new ArrayList<Byte>();

    private List<List<Byte>> paths = new ArrayList<List<Byte>>();



    private Integer labelId;

   public KD2Tree(Integer labelId){
       this.labelId=labelId;
   }


   public void addPath(List<Byte> path){
       paths.add(path);
   }


    public void merge(){
       List<Byte[]> tree = new ArrayList<Byte[]>();
       List<Integer> order = new ArrayList<Integer>();
       List<Integer> nextOrder = new ArrayList<Integer>();

       Map<Integer, List<List<Byte>>> map = new HashMap<Integer, List<List<Byte>>>();

       List<List<Byte>> rest = paths;
       int k=0;
       Byte[] node = mergeInternal(rest, map, order, k);
       k+=4;
       tree.add(node);
       int h=0;
       do {
           //check if map contains, 0, 1, 2, 3
           rest.clear();
           for (Integer i : order) {
               if (map.containsKey(i)) {
                   rest = map.get(i);
                   node = mergeInternal(rest, map,nextOrder,  k);
                   k+=4;
                   tree.add(node);
               }
               map.remove(i);
           }
           order.clear();
           order = nextOrder;
           nextOrder =new ArrayList<Integer>();
            h++;
       }while(!rest.isEmpty());
       for(Byte[] b : tree){
           addNodeNeighbor(b);
       }
   }

   private Byte[] mergeInternal(List<List<Byte>> rest, Map<Integer, List<List<Byte>>> map, List<Integer> order, int k){
       Byte[] node = new Byte[]{0,0,0,0};
       List<List<Byte>> remove = new ArrayList<List<Byte>>();
       for(List<Byte> b : rest){
           int nodeVal = b.remove(0);
           if(node[nodeVal]!=1){
               //set k

               order.add(k+nodeVal);
           }
           node[nodeVal]=1;
           if(b.isEmpty()) {
               remove.add(b);
           }
           else {
               map.putIfAbsent(k+nodeVal, new ArrayList<List<Byte>>());
               map.get(k+nodeVal).add(b);
           }
       }
       rest.removeAll(remove);
       Collections.sort(order);
       return node;
   }

    /*
    0101 1000 1100 0100 1000 0001 (3 bytes 88, 196, 129)

                   ____  * ____
                 /      | |     \
              /       /    \      \
           /        /        \       \
          0         1        0         1 ____
                  / |\ \             /  |   \ \
                /   | | |           /   |    | |
               1    0 0 0          1    1    0 0
             /||\                /||\  /||\
             0100                0100  0001

     */

    public void addNodeNeighbor(Byte k){
        tree.add(k);
    }

    public void prune(){
        for(int i=tree.size()-1;i>=0;i-=4){
            Byte a = tree.get(i);
            Byte b = tree.get(i-1);
            Byte c = tree.get(i-2);
            Byte d = tree.get(i-3);
            if(a==0 && b==0 && c==0 && d==0){
                tree.remove(i);
                tree.remove(i-1);
                tree.remove(i-2);
                tree.remove(i-3);
            }else{
                break;
            }
        }
    }

    public void addNodeNeighbor(Byte... k){
        for (Byte i : k) {
            tree.add(i);
        }
    }

    public Integer getLabelId() {
        return labelId;
    }

    public void setLabelId(Integer labelId) {
        this.labelId = labelId;
    }

    public List<List<Byte>> createPaths() {
        Set<TreeNode> leaves = new HashSet<TreeNode>();
        List<TreeNode> order = new ArrayList<TreeNode>();
        TreeNode root = new TreeNode();
        root.setValue(new Byte[]{tree.get(0),tree.get(1),tree.get(2), tree.get(3)});
        int count=0;
        for(Byte b : root.getValue()){
            if(b==1){
                count++;
            }
        }
        int orderIndex=0;
        order.add(root);
        leaves.add(root);
        for(int i=4; i<tree.size();i+=4){
            TreeNode node = new TreeNode();
            node.setValue(new Byte[]{tree.get(i),tree.get(i+1),tree.get(i+2), tree.get(i+3)});
            //check next node to add to
            order.add(node);
            TreeNode parent = order.get(orderIndex);
            leaves.remove(parent);
            int k =0;
            for(k=0;k<4;k++){
                if(parent.getValue()[k]==1 && parent.getChild(k)==null){
                    break;
                }
            }
            leaves.add(node);
            node.setParent(parent);
            parent.setChild(k, node);
            count--;
            if(count==0){
                orderIndex++;
                //count=0;
                for(Byte b : order.get(orderIndex).getValue()){
                    if(b==1){
                        count++;
                    }
                }
            }
        }
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        for(TreeNode n : leaves){
            paths.addAll(n.createUpwardsPath());
        }
        return paths;
    }


    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //add label ID, add size, add bytes
        int k=7;
        byte b = 0;
        for(Byte val : tree){
            // adds the next bit value to b and decrement k, so next bit will be set the position after
            b += (val << k--);
            if(k==-1){
                //write old byte to stream
                baos.write(b);
                //set next byte
                b = 0;
                k=7;
            }
        }
        //write last byte if not empty
        if(b!=0){
            if(k==-1){
                k=7;
            }
            baos.write(b);
        }
        byte[] ser = baos.toByteArray();
        baos = new ByteArrayOutputStream();

        //baos.writeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(ser.length).array());
        baos.write(ByteBuffer.allocate(Integer.BYTES).putInt(labelId.intValue()).array());
        baos.write(ser);
        baos.write(0);
        return baos.toByteArray();
    }

    //TODO optimize!
    public LabledMatrix createMatrix(){
        LabledMatrix matrix = new LabledMatrix(labelId);

        List<List<Byte>> queue = this.createPaths();
            for(List<Byte> arr : queue){
                int row=0;
                int col=0;
                int h=arr.size()-1;
                for(Byte o : arr){
                    if(o==1){col+=Math.pow(2, h);}
                    if(o==2){row+=Math.pow(2, h);}
                    if(o==3){row+=Math.pow(2, h);col+=Math.pow(2, h);}
                    h--;
                }
                matrix.set(row, col);
            }
        //}
        return matrix;
    }


    public static KD2Tree deserialize(int labelId, byte[] arr) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        //byte[] id = bais.readNBytes(4);
        //int labelId = ByteBuffer.wrap(id).getInt();
        KD2Tree tree = new KD2Tree(labelId);
        //arr[4:] to List<Integer>
        //int k=7;
        for(int i=0; i<arr.length;i++){
            for(int k=7;k>-1;k--) {
                byte cmp = (Double.valueOf(Math.pow(2, k)).byteValue());
                //int c = cmp & arr[i];
                tree.addNodeNeighbor((cmp & arr[i]) == cmp ? (byte) 1 : 0);
                //if(k<0){k=7;}
            }

        }
        tree.prune();
        return tree;
    }


    public void clear() {
        tree.clear();
    }
}
