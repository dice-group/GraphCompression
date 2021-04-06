package org.dice_group.k2.serialization.impl;

import org.dice_group.k2.util.LabledMatrix;
import org.dice_group.k2.util.TreeNode;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadedKD2PPSerializer extends ThreadedKD2TreeSerializer{
    public ThreadedKD2PPSerializer(int threads, int predicates) {
        super(threads, predicates);
    }

    @Override
    public void writeToBaos(ByteArrayOutputStream baos, TreeNode root, Double h, Integer label){
        //go along the Tree and write it
        //write label
        for(byte i=0;i<=(4-1)*8;i+=8){
            byte w= Integer.valueOf(((label >> i) & 255)).byteValue();
            baos.write(w);
        }
        baos.write(h.byteValue());
        AtomicReference<Byte> last= new AtomicReference<Byte>();
        last.set((byte)0);
        boolean shift = merge(root, baos, true, last);
        if(!shift){
            baos.write(last.get().byteValue());
        }
        //baos.write(0);
    }

    private boolean merge(TreeNode root, ByteArrayOutputStream baos, boolean shift, AtomicReference<Byte> last) {
        if(root ==null || root.isLeaf()){
            return shift;
        }
        byte b = root.getRawValue(false);
        if(shift) {
            Integer tmp = b << 4;
            last.set(tmp.byteValue());
            shift=false;
        }
        else{
            Integer write = last.get() | b;
            baos.write( write.byteValue());
            last.set((byte)0);
            shift=true;
        }
        TreeNode c0 = root.getChild(0);
        TreeNode c1 = root.getChild(1);
        TreeNode c2 = root.getChild(2);
        TreeNode c3 = root.getChild(3);
        shift = merge(c0, baos, shift, last);
        shift = merge(c1, baos,  shift, last);
        shift = merge(c2, baos,  shift, last);
        shift = merge(c3, baos,  shift, last);
        root.clear();

        return shift;
    }


}
