package org.dice_group.grp.serializer;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.dice_group.grp.serialization.impl.CRSSerializer;
import org.junit.Test;

public class CRSSerializerTest {

	@Test
	public void testSerializing() {

		// create CRS
		/*
		 * 0 1 2 _______ 0 |2 0 0 1 |0 3 1 2 |1 0 1
		 * 
		 */
		List<Integer> val = new LinkedList<Integer>();
		val.add(2);
		val.add(3);
		val.add(1);
		val.add(1);
		val.add(1);
		List<Integer> colIndex = new LinkedList<Integer>();
		colIndex.add(0);
		colIndex.add(1);
		colIndex.add(2);
		colIndex.add(0);
		colIndex.add(2);
		List<Integer> rowPtr = new LinkedList<Integer>();
		rowPtr.add(0);
		rowPtr.add(1);
		rowPtr.add(3);
		rowPtr.add(5);

		// serialize crs
		byte[] crs = new CRSSerializer().serialize(val, colIndex, rowPtr);
		
		int cvSize = ByteBuffer.wrap(crs).getInt(0);
		// 5 * 4 bytes
		assertEquals(20, cvSize);

		List<Integer> desCol = getBytesAsList(Arrays.copyOfRange(crs, Integer.BYTES, cvSize + Integer.BYTES));
		int offset = 2 * cvSize + Integer.BYTES;
		List<Integer> desVal = getBytesAsList(Arrays.copyOfRange(crs, cvSize + Integer.BYTES, offset));
		List<Integer> desRow = getBytesAsList(Arrays.copyOfRange(crs, offset, crs.length));

		assertEquals(val, desVal);
		assertEquals(colIndex, desCol);
		assertEquals(rowPtr, desRow);
		// assuming that 1 triple = INT INT INT\n we need 3*4 bytes+3 bytes for the
		// spaces and newline chars
		System.out.println(
				"5 indexed Triples wiht approx size of 5 * (3 * 4 + 3) = 75 bytes vs " + crs.length + " bytes ");

	}

	private List<Integer> getBytesAsList(byte[] serL) {
		List<Integer> ret = new LinkedList<Integer>();
		for (int i = 0; i < serL.length; i += Integer.BYTES) {
			ret.add(ByteBuffer.wrap(serL).getInt(i));
		}
		return ret;
	}
}
