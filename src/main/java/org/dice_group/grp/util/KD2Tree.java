package org.dice_group.grp.util;

import org.apache.jena.ext.com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KD2Tree {

   private List<Byte> tree = new ArrayList<Byte>();


    private Integer labelId;

   public KD2Tree(Integer labelId){
       this.labelId=labelId;
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
            baos.write(b);
        }
        byte[] ser = baos.toByteArray();
        baos = new ByteArrayOutputStream();
        baos.write(ser.length);
        baos.write(labelId);
        baos.write(ser);
        return baos.toByteArray();
    }

    public LabledMatrix createMatrix(){
        LabledMatrix matrix = new LabledMatrix(labelId);
        List< List<Integer>> queue = new ArrayList<List<Integer>>();
        for(int i=0;i<this.tree.size();){
            //always get 4
            if(queue.isEmpty()) {
                if (tree.get(i++) == 1) {queue.add(Lists.newArrayList(1));}
                if (tree.get(i++) == 1) {queue.add(Lists.newArrayList(2));}
                if (tree.get(i++) == 1) {queue.add(Lists.newArrayList(3));}
                if (tree.get(i++) == 1) {queue.add(Lists.newArrayList(4));}
            }
            else{
                List<Integer> c = queue.remove(0);
                if (tree.get(i++) == 1) {
                    List<Integer> arr = new ArrayList<Integer>(c);
                    arr.add(1);
                    queue.add(arr);
                }
                if (tree.get(i++) == 1) {
                    List<Integer> arr = new ArrayList<Integer>(c);
                    arr.add(2);
                    queue.add(arr);
                }
                if (tree.get(i++) == 1) {
                    List<Integer> arr = new ArrayList<Integer>(c);
                    arr.add(3);
                    queue.add(arr);
                }
                if (tree.get(i++) == 1) {
                    List<Integer> arr = new ArrayList<Integer>(c);
                    arr.add(4);
                    queue.add(arr);
                }
            }
            for(List<Integer> arr : queue){
                int row=0;
                int col=0;
                int h=arr.size()-1;
                for(Integer o : arr){
                    if(o==2){col+=Math.pow(2, h);}
                    if(o==3){row+=Math.pow(2, h);}
                    if(o==4){row+=Math.pow(2, h);col+=Math.pow(2, h);}
                    h--;
                }
                matrix.set(row, col);
            }
        }
        return matrix;
    }


    public static KD2Tree deserialize(byte[] arr) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        byte[] id = bais.readNBytes(4);
        int labelId = ByteBuffer.wrap(id).getInt();
        KD2Tree tree = new KD2Tree(labelId);
        //arr[4:] to List<Integer>
        int k=7;
        for(int i=4; i<arr.length;){
            tree.addNodeNeighbor((Double.valueOf(Math.pow(2, k)).byteValue()&arr[i++])==Math.pow(2, k--)?(byte)1:0);
            if(k<0){k=7;}
        }
        return tree;
    }



}
