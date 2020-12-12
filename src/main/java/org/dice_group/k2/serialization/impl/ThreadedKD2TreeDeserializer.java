package org.dice_group.k2.serialization.impl;

import org.dice_group.k2.util.KD2Tree;
import org.dice_group.k2.util.LabledByteArray;
import org.dice_group.k2.util.LabledMatrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedKD2TreeDeserializer {
    //private Long avgM=0l;
    //private Long avgKd=0l;

    public List<LabledMatrix> deserialize(byte[] serialized, int threads) throws IOException, ExecutionException, InterruptedException {
        List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();
        long s=System.currentTimeMillis();

        //        baos.write(ser.length); +4
        //        baos.write(labelId);
        //        baos.write(ser);
        //ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        int offset = 0;

        System.out.println("starting deserializing kd2trees and build matrices using "+threads+" threads");
        int count=0;
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Map<Integer, List<LabledByteArray>> map = new HashMap<Integer, List<LabledByteArray>>();
        for(int i=0;i<threads;i++){
            map.put(i, new ArrayList<LabledByteArray>());
        }
        Integer nextThread=0;
        for(int i=0;i<serialized.length;) {
            byte[] id = Arrays.copyOfRange(serialized, offset, offset+4);
            int labelId = ByteBuffer.wrap(id).getInt();
            i+=4;
            offset+=4;
            int size=0;
            //List<Byte> ser = new ArrayList<Byte>();
            for(int j=offset;j<serialized.length;j++) {
                if(serialized[j]==0){
                    //next tree;
                    //System.out.println(serialized[j-1]);
                    break;
                }
                //i++;
                size++;
                //offset++;
            }

            // int size = ByteBuffer.wrap(Arrays.copyOfRange(serialized, offset-4, offset)).getInt() + 4;
            byte[] ser = Arrays.copyOfRange(serialized, offset, offset+size);
            //System.out.println(ser[ser.length-1]);


            offset+=size+1;
//            offset++;
            i+=size+1;


            map.get(nextThread++).add(new LabledByteArray(labelId, ser));
            if(nextThread>=threads){
                nextThread=0;
            }

            //bais.read(ser, offset, size);

        }
        List<Future<List<LabledMatrix>>> futures = new ArrayList<Future<List<LabledMatrix>>>();
        for(Integer key : map.keySet()) {
            if(map.get(key).isEmpty()){
                continue;
            }
            Future<List<LabledMatrix>> fut = (Future<List<LabledMatrix>>) service.submit(() -> {
                return threadedCreation(map.get(key));
            } );
            futures.add(fut);
        }
        service.shutdown();
        for(Future<List<LabledMatrix>> fut : futures) {
            matrices.addAll(fut.get());
        }
        long e=System.currentTimeMillis();
        System.out.println("Finished creating "+matrices.size()+" kd trees/matrices. Took "+(e-s)+"ms");
        return matrices;
    }


    private List<LabledMatrix> threadedCreation(List<LabledByteArray> use) throws IOException {
        int count=0;
        List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();
        String threadName = Thread.currentThread().getName();
        Long avgM=Long.valueOf(0);
        Long avgKd=Long.valueOf(0);
        for(LabledByteArray treeB : use){
            long s = Calendar.getInstance().getTimeInMillis();
            KD2Tree tree = KD2Tree.deserialize(treeB.getLabelID(), treeB.getBytes());
            long e = Calendar.getInstance().getTimeInMillis();
            avgKd+=e-s;
            s = Calendar.getInstance().getTimeInMillis();
            LabledMatrix matrix = tree.createMatrix();
            e = Calendar.getInstance().getTimeInMillis();
            avgM+=e-s;
            matrices.add(matrix);
            count++;
            if(count%10 ==0) {
                System.out.println(threadName+" Created "+count+"/"+use.size()+" matrices");
                System.out.println(threadName+" Matrices took avg "+avgM*1.0/count+"ms");
                System.out.println(threadName+" Trees took avg "+avgKd*1.0/count+"ms");
            }
        };

        System.out.println(threadName+" Created "+count+" matrices");

        return matrices;
    }


}
