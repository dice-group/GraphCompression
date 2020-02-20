package org.dice_group.grp.serialization.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.dice_group.grp.serialization.GraphDeserializer;


public class CRSDeserializer implements GraphDeserializer{

	public static List<Integer>  deserializeIntegerList(byte[] serList, int cvSize) {
		List<Integer> ret = new LinkedList<Integer>();

		for(int i=1; i<serList.length;i+=cvSize) {
			ret.add(ByteBuffer.wrap(serList).getInt(i));
		}
		return ret;
	}

	public static List<Integer>  deserializeShortList(byte[] serList, int cvSize) {
		List<Integer> ret = new LinkedList<Integer>();

		for(int i=1; i<serList.length;i+=cvSize) {
			ret.add(Short.valueOf(ByteBuffer.wrap(serList).getShort(i)).intValue());
		}
		return ret;
	}

	public static List<Integer>  deserializeLongList(byte[] serList, int cvSize) {
		List<Integer> ret = new LinkedList<Integer>();

		for(int i=1; i<serList.length;i+=cvSize) {
			ret.add(Long.valueOf(ByteBuffer.wrap(serList).getLong(i)).intValue());
		}
		return ret;
	}

	public static List<Integer>  deserializeByteList(byte[] serList, int cvSize) {
		List<Integer> ret = new LinkedList<Integer>();

		for(int i=1; i<serList.length;i+=cvSize) {
			ret.add(Byte.valueOf(ByteBuffer.wrap(serList).get(i)).intValue());
		}
		return ret;
	}




	/**
	 * returns Lists {colInd, val, rowPtr}
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer>[] deserialize(byte[] serialized) {
		Integer cvSize = ByteBuffer.wrap(serialized).getInt(0);
		List<Integer> colInd=deserializeIntegerList(Arrays.copyOfRange(serialized, Integer.BYTES, cvSize + Integer.BYTES), Integer.BYTES);

		int offset = cvSize+Integer.BYTES+1;
		byte size = Arrays.copyOfRange(serialized, cvSize+Integer.BYTES, cvSize+Integer.BYTES+1)[0];
		List<Integer> val =null;
		if(size==0) {
			offset += (cvSize - 1) / 4;
			val = deserializeByteList(Arrays.copyOfRange(serialized, cvSize + Integer.BYTES, offset), Byte.BYTES);
		}
		else if(size==1) {
			offset += (cvSize - 1) / 2;
			val = deserializeShortList(Arrays.copyOfRange(serialized, cvSize + Integer.BYTES, offset), Short.BYTES);
		}
		else if(size==2) {
			offset += (cvSize - 1);
			val = deserializeIntegerList(Arrays.copyOfRange(serialized, cvSize + Integer.BYTES, offset), Integer.BYTES);
		}
		else if(size==3) {
			offset += (cvSize - 1) * 2;
			val = deserializeLongList(Arrays.copyOfRange(serialized, cvSize + Integer.BYTES, offset), Long.BYTES);
		}
		List<Integer> rowPtr = deserializeIntegerList(Arrays.copyOfRange(serialized, offset, serialized.length), Integer.BYTES);
		
		return new List[] {colInd, val, rowPtr};
	}
	
	
}
