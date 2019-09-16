package org.dice_group.grp.serializer;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.dice_group.grp.serialization.impl.CRSDeserializer;
import org.junit.Test;

public class CRSDeserializerTest {

	
	@Test
	public void deserializationTest() {
		//create expected
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
		
		List<Integer>[] crsDes = null;
		CRSDeserializer deserializer = new CRSDeserializer();
		try(FileInputStream fis = new FileInputStream("src/test/resources/graph.crs")){
			byte[] crs = fis.readAllBytes();
			crsDes = deserializer.deserialize(crs);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(colIndex, crsDes[0]);
		assertEquals(val, crsDes[1]);
		assertEquals(rowPtr, crsDes[2]);
		
	}
	
}
