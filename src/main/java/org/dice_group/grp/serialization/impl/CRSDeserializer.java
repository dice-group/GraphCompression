package org.dice_group.grp.serialization.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.dice_group.grp.serialization.GraphDeserializer;


public class CRSDeserializer implements GraphDeserializer{

	public static List<Integer>  deserializeIntegerList(byte[] serList) {
		List<Integer> ret = new LinkedList<Integer>();
		for(int i=0; i<serList.length;i+=Integer.BYTES) {
			ret.add(ByteBuffer.wrap(serList).getInt(i));
		}
		return ret;
	}	
	

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer>[] deserialize(byte[] serialized) {
		Integer cvSize = ByteBuffer.wrap(serialized).getInt(0);
		List<Integer> colInd = deserializeIntegerList(Arrays.copyOfRange(serialized, Integer.BYTES, cvSize+Integer.BYTES));
		int offset = 2*cvSize+Integer.BYTES;
		List<Integer> val = deserializeIntegerList(Arrays.copyOfRange(serialized, cvSize+Integer.BYTES, offset));
		List<Integer> rowPtr = deserializeIntegerList(Arrays.copyOfRange(serialized, offset, serialized.length));
		
		return new List[] {colInd, val, rowPtr};
	}
	
	
}
