package org.dice_group.grp.serialization.impl;

import org.dice_group.grp.serialization.GraphDeserializer;
import org.dice_group.grp.util.KD2Tree;
import org.dice_group.grp.util.LabledMatrix;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class KD2TreeDeserializer {

    public List<LabledMatrix>  deserialize(byte[] serialized) throws IOException {
        List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();

        //        baos.write(ser.length); +4
        //        baos.write(labelId);
        //        baos.write(ser);
        //ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        int offset = 0;
        long avgM=0;
        long avgKd=0;
        System.out.println("starting deserializing kd2trees and build matrices");
        int count=0;
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

            //bais.read(ser, offset, size);
            long s = Calendar.getInstance().getTimeInMillis();
            KD2Tree tree = KD2Tree.deserialize(labelId, ser);
            long e = Calendar.getInstance().getTimeInMillis();
            avgKd+=e-s;
            s = Calendar.getInstance().getTimeInMillis();
            LabledMatrix matrix = tree.createMatrix();
            e = Calendar.getInstance().getTimeInMillis();
            avgM+=e-s;
            matrices.add(matrix);
            offset+=size+1;
//            offset++;
            i+=size+1;
            count++;
            if(count%10 ==0) {
                System.out.println("Created "+count+" kd trees/matrices");
                System.out.println("Matrices took avg "+avgM*1.0/count+"ms");
                System.out.println("Trees took avg "+avgKd*1.0/count+"ms");
            }
        }
        System.out.println("Finished creating "+count+" kd trees/matrices");
        return matrices;
    }
}
