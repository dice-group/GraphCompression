package org.dice_group.grp.serialization.impl;

import org.dice_group.grp.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedKD2TreeSerializer {

    private final int threads;

    private List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();
    private List<List<Integer>> threadedMatrices = new ArrayList<List<Integer>>();
    private int count=0;

    public ThreadedKD2TreeSerializer(int threads, int predicates){
        this.threads=threads;
        for(int i=0;i<threads;i++){
            threadedMatrices.add(new ArrayList<Integer>());
        }
        for(int i=0;i<predicates;i++){
            matrices.add(new LabledMatrix(i));
            threadedMatrices.get(count++).add(i);
            if(count>=threads){
                count =0;
            }
        }
    }

    public void addTriple(int subject, int predicate, int object){
        matrices.get(predicate).set(subject, object);
    }



    public byte[] serialize(long sSize, long oSize) throws IOException, ExecutionException, InterruptedException {



        ExecutorService service = Executors.newFixedThreadPool(threads);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int x=0;
        Long vSize =Math.max(sSize, oSize);
        Double h = Math.ceil(log(vSize.intValue(), 2));
        Double size = Math.pow(2, h);
        System.out.println("H: "+h+" vSize: "+vSize+" sSize: "+sSize+" oSize: "+oSize);
        List<Future<List<byte[]>>> futures = new ArrayList<Future<List<byte[]>>>();
        for(List<Integer> matrice : threadedMatrices) {
            Future<List<byte[]>> fut = (Future<List<byte[]>>) service.submit(() -> {
                return threadedCreation(matrice, matrices, size, h);
            } );
            futures.add(fut);
        }
        service.shutdown();
        for(Future<List<byte[]>> fut : futures) {
            List<byte[]> o =fut.get();
            for (byte[] tree : o) {
                baos.write(tree);
            }

        }

        return baos.toByteArray();
    }

    private List<byte[]> threadedCreation(List<Integer> use, List<LabledMatrix> matrices, double size, double h) throws IOException {
        int x=0;
        //TODO
        List<byte[]> trees = new ArrayList<byte[]>();
        String threadName = Thread.currentThread().getName();
        for(Integer key : use){
            LabledMatrix matrix = matrices.get(key);
            byte[] tree =createTree(matrix, size, h);
            trees.add(tree);
            //tree.clear();
            x++;
            if(x%10 ==0){
                System.out.println(threadName + " Created "+x+" kd2 trees of "+use.size());
            }
        };


        return trees;
    }

    private byte[] createTree(LabledMatrix matrix, Double actSize, Double actH){
        TreeNode root = new TreeNode();
        int c=0;
        int mSize= matrix.getPoints().size();

        //Double h = actH;
        //Double size = actSize;
        Double h = matrix.getH();
        Double size = Math.pow(2, h);
        for(Point p : matrix.getPoints()){
            //get path
            int c1=0 ;
            int r1=0;
            int c2=size.intValue();
            int r2=size.intValue();
            TreeNode pnode = root;
            c++;
            if(c%1000000==0){
                System.out.print(c+"/"+mSize+"\t");
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
        matrix.getPoints().clear();

        List<List<Byte>> hMap = new ArrayList<List<Byte>>();
        for(int i=0;i<h;i++){
            hMap.add(new ArrayList<Byte>());
        }
        merge(root, hMap, 0, h);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(byte b : ByteBuffer.allocate(Integer.BYTES).putInt(matrix.getLabelId()).array()) {
            baos.write(b);
        }

        boolean shift=true;
        byte last=0;
        for(int i=0; i<hMap.size();i++){
            for(Byte b : hMap.get(i)) {
                //tree.addNodeNeighbor(b);
                if(shift) {
                    last = Integer.valueOf(b << 4).byteValue();
                    shift=false;
                }
                else{
                    baos.write( Integer.valueOf(last | b).byteValue());
                    shift=true;
                }
            }
            hMap.get(i).clear();
        }
        if(!shift){
            baos.write(last);
        }
        baos.write(0);
        hMap.clear();
        return baos.toByteArray();
    }

    private void merge(TreeNode root, List<List<Byte>> hMap, int h, double max) {
        if(root==null || h>=max){
            return;
        }
        if(h>=hMap.size()){
            System.err.println("h: "+h+" hMap.size: "+hMap.size());
        }

        List<Byte> arr = hMap.get(h);
        arr.add(root.getRawValue(true));
        //arr.add(root.getValue()[0]);
        //arr.add(root.getValue()[1]);
        //arr.add(root.getValue()[2]);
        //arr.add(root.getValue()[3]);
        TreeNode c0 = root.getChild(0);
        TreeNode c1 = root.getChild(1);
        TreeNode c2 = root.getChild(2);
        TreeNode c3 = root.getChild(3);
        root.clear();
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
