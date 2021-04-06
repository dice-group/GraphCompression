package org.dice_group.k2.serialization.impl;

import org.dice_group.k2.util.KD2Tree;
import org.dice_group.k2.util.LabledMatrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class KD2TreeDeserializer {

    public int addOffset=4;

    public int readLabelId(byte[] id){
        int labelId = ByteBuffer.wrap(id).getInt();
        return labelId;
    }

    public List<LabledMatrix>  deserialize(byte[] serialized) throws IOException {
        List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();

        //        baos.write(ser.length); +4
        //        baos.write(labelId);
        //        baos.write(ser);
        //ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        int offset = 0;
        System.out.println("starting deserializing kd2trees and build matrices");
        int count=0;
        for(int i=0;i<serialized.length;) {
            byte[] id = Arrays.copyOfRange(serialized, offset, offset+addOffset);
            int labelId=readLabelId(id);
            i+=addOffset;
            offset+=addOffset;
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
            LabledMatrix matrix = getMatrix(labelId, ser);

            matrices.add(matrix);
            offset+=size+1;
//            offset++;
            i+=size+1;
            count++;
            if(count%10 ==0) {
                System.out.println("Created "+count+" kd trees/matrices");
            }
        }
        System.out.println("Finished creating "+count+" kd trees/matrices");
        return matrices;
    }

    protected LabledMatrix getMatrix(int labelId, byte[] ser) throws IOException {
        KD2Tree tree = deserialize(labelId, ser);
        return tree.createMatrix();
    }

    protected KD2Tree deserialize(int labelId, byte[] ser) throws IOException {
        return KD2Tree.deserialize(labelId, ser);
    }
}
