package org.dice_group.grp.serialization.impl;

import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedKD2TreeSerializer {


    public byte[] serialize(List<Statement> stmts, int vSize, int threads) throws IOException, ExecutionException, InterruptedException {


        Map<Integer, LabledMatrix> matrices = new HashMap<Integer, LabledMatrix>();
        //create matrices
        Map<Integer, List<Integer>> threadedMatrices = new HashMap<Integer, List<Integer>>();
        for(int i=0;i<threads;i++){
            threadedMatrices.put(i, new ArrayList<Integer>());
        }
        int count=0;
        for(Statement stmt : stmts){

            int edgeIndex = stmt.getPredicate();
            if (!matrices.containsKey(edgeIndex)) {
                matrices.put(edgeIndex, new LabledMatrix(edgeIndex));
                threadedMatrices.get(count++).add(edgeIndex);
                if(count>=threads){
                    count =0;
                }
            }
            matrices.get(edgeIndex).set(stmt.getSubject(), stmt.getObject());

        }

        ExecutorService service = Executors.newFixedThreadPool(threads);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int x=0;
        Double h = Math.ceil(log(vSize, 2));
        Double size = Math.pow(2, h);
        List<Future<List<KD2Tree>>> futures = new ArrayList<Future<List<KD2Tree>>>();
        for(Integer key : threadedMatrices.keySet()) {
            Future<List<KD2Tree>> fut = (Future<List<KD2Tree>>) service.submit(() -> {
                return threadedCreation(threadedMatrices.get(key), matrices, size, h);
            } );
            futures.add(fut);
        }
        service.shutdown();
        for(Future<List<KD2Tree>> fut : futures) {
            List<KD2Tree> o =fut.get();
            for (KD2Tree tree : o) {
                baos.write(tree.serialize());
            }
        }

        return baos.toByteArray();
    }

    private List<KD2Tree> threadedCreation(List<Integer> use, Map<Integer, LabledMatrix> matrices, double size, double h){
        int x=0;
        List<KD2Tree> trees = new ArrayList<KD2Tree>();
        String threadName = Thread.currentThread().getName();
        for(Integer key : use){
            LabledMatrix matrix = matrices.get(key);
            trees.add(createTree(matrix, size, h));
            x++;
            if(x%10 ==0){
                System.out.println(threadName + " Created "+x+" kd2 trees of "+use.size());
            }
        };


        return trees;
    }

    private KD2Tree createTree(LabledMatrix matrix, Double size, Double h){
        KD2Tree tree = new KD2Tree(matrix.getLabelId());
        TreeNode root = new TreeNode();
        int c=0;
        for(Point p : matrix.getPoints()){
            //get path
            int c1=0 ;
            int r1=0;
            int c2=size.intValue();
            int r2=size.intValue();
            TreeNode pnode = root;
            c++;
            if(c%1000000==0){
                System.out.print(c+"\t");
                Stats.printMemStats();
            }

            for(int i=0;i<h;i++){
                Byte node = getNode(p, c1, r1, c2, r2);
                TreeNode cnode = new TreeNode();

                cnode = pnode.setChildIfAbsent(node, cnode);
                pnode = cnode;
                //path.add(node);
                if(node==0){
                    r2 = (r2 - r1) / 2 + r1;
                    c2 = (c2 - c1) / 2 + c1;
                }
                else if(node==1){
                    ///y=row, x =col
                    r2 = (r2 - r1) / 2 + r1;
                    c1 = (c2 - c1) / 2 + c1;
                }
                else if(node==2){
                    r1 = (r2 - r1) / 2 + r1;
                    c2 = (c2 - c1) / 2 + c1;
                }
                else if(node==3){
                    r1 = (r2 - r1) / 2 + r1;
                    c1 = (c2 - c1) / 2 + c1;
                }

            }
            //tree.addPath(path);
            //return tree;
        }

        Map<Integer, List<Byte>> hMap = new HashMap<Integer, List<Byte>>();
        merge(root, hMap, 0, h);
        for(int i=0; i<hMap.size();i++){
            for(Byte b : hMap.get(i)) {
                tree.addNodeNeighbor(b);
            }
        }
        return tree;
    }

    private void merge(TreeNode root, Map<Integer, List<Byte>> hMap, int h, double max) {
        if(root==null || h>=max){
            return;
        }
        hMap.putIfAbsent(h, new ArrayList<Byte>());
        hMap.get(h).add(root.getValue()[0]);
        hMap.get(h).add(root.getValue()[1]);
        hMap.get(h).add(root.getValue()[2]);
        hMap.get(h).add(root.getValue()[3]);
        TreeNode c0 = root.getChild(0);
        TreeNode c1 = root.getChild(1);
        TreeNode c2 = root.getChild(2);
        TreeNode c3 = root.getChild(3);
        root =null;
        merge(c0, hMap, h+1, max);
        merge(c1, hMap, h+1, max);
        merge(c2, hMap, h+1, max);
        merge(c3, hMap, h+1, max);

    }

    public Byte getNode(Point p, int c1, int r1, int c2, int r2){
        int rCenter = (r2 - r1) / 2 + r1;
        int cCenter = (c2 - c1) / 2 + c1;
        //TODO >= and > might be a problem at the last point.
        if(p.getCol()<cCenter && p.getRow() <rCenter){
            return 0;
        }
        if(p.getCol()>=cCenter && p.getRow() <rCenter){
            return 1;
        }
        if(p.getCol()<cCenter && p.getRow() >= rCenter){
            return 2;
        }
        else{
            return 3;
        }
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


    }

    private double log(int x, int base)
    {
        return  (Math.log(x) / Math.log(base));
    }
}
