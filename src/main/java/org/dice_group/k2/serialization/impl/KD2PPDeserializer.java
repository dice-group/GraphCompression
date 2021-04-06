package org.dice_group.k2.serialization.impl;

import org.dice_group.k2.util.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KD2PPDeserializer extends KD2TreeDeserializer{

    public int offset=8;

    @Override
    public List<LabledMatrix> deserialize(byte[] serialized) throws IOException {
        List<LabledMatrix> matrices = new ArrayList<LabledMatrix>();
        ByteBuffer byteBuffer = ByteBuffer.wrap(serialized);
        while(byteBuffer.hasRemaining()){
            byte[] id = new byte[]{byteBuffer.get(),byteBuffer.get(),byteBuffer.get(),byteBuffer.get()};
            int labelId = readLabelId(id);
            LabledMatrix matrix = new LabledMatrix(labelId);
            byte hSize=byteBuffer.get();
            byte k = hSize;
            int j=0;
            Path p = new Path(hSize);
            boolean matrixEnd=false;
            do {
                if (p.hasLast()) {
                    p.addLast(j);
                    j++;
                }
                //byte[] b = new byte[1];
                for (int i = j; i < k; i += 2) {

                    //file.read((char *) b, sizeof(u_char));
                    byte b = byteBuffer.get();
                    p.add(i, b);

                }
                for(Point point : p.calculatePoints()){
                    matrix.set(point.getRow(), point.getCol());
                }
                j = hSize-1;
                for (;j>0;j--) {
                    p.pop(j - 1);
                    if (!p.isEmpty(j - 1)) {
                        break;
                    }
                }

                if(j<=0){
                    matrixEnd=true;
                }
            } while (!matrixEnd);
            matrices.add(matrix);
        }
        return matrices;
    }

    @Override
    protected LabledMatrix getMatrix(int labelId, byte[] ser) throws IOException {
        LabledMatrix matrix = new LabledMatrix(labelId);
        ByteBuffer byteBuffer  = ByteBuffer.wrap(ser);

        byte hSize=byteBuffer.get();
        byte k = hSize;
        int j=0;
        Path p = new Path(hSize);
        boolean matrixEnd=false;
        do {
            if (p.hasLast()) {
                p.addLast(j);
                j++;
            }
            //byte[] b = new byte[1];
            for (int i = j; i < k; i += 2) {

                //file.read((char *) b, sizeof(u_char));
                byte b = byteBuffer.get();
                p.add(i, b);

            }
            for(Point point : p.calculatePoints()){
                matrix.set(point.getRow(), point.getCol());
            }
            j = hSize-1;
            for (;j>0;j--) {
                p.pop(j - 1);
                if (!p.isEmpty(j - 1)) {
                    break;
                }
            }

            if(j==0){
                matrixEnd=true;
            }
        } while (!matrixEnd);
        return matrix;
    }

    @Override
    public int readLabelId(byte[] id){
        byte[] pLabel = new byte[4];
        int label=0;
        for(byte byteShift=0;byteShift<4;byteShift++){
            int add = id[byteShift] << byteShift*8;
            label += add;
        }
        return label;
    }
}
