package org.dice_group.k2.util;

public class LabledByteArray {

    private int labelID;

    private byte[] bytes;

    public LabledByteArray(Integer labelID, byte[] bytes){
        this.labelID=labelID;
        this.bytes=bytes;
    }

    public int getLabelID() {
        return labelID;
    }

    public void setLabelID(int labelID) {
        this.labelID = labelID;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


}
