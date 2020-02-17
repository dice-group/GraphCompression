package org.dice_group.grp.decompression.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.CRSDeserializer;
import org.dice_group.grp.util.GraphUtils;
import org.rdfhdt.hdtjena.NodeDictionary;

public class CRSDecompressor {
/*
	private CRSDeserializer deserializer = new CRSDeserializer();
	
	public int[] oneInternalStructs = new int[] {2,3,5,6,7,8,10,13,14,16,19,20,22,25,26,28,32,33};
	
	public int[] externalIndexOne = new int[] {0,1,2,4,5,7,8,9,13,14,15,19,22,23,25,28,29,31,33};
	public int[] externalIndexTwo = new int[] {0,1,2,4,5,7,10,11,13,16,17,19,20,21,25,26,27,31,32};
	public int[] externalIndexThree = new int[] {0,1,2,4,6,7,9,14,16,18,19,20,21,26,28,30,31,32};
	public int[] externalIndexFour = new int[] {0,1,3,4,5,8,10,12,13,14,15,20,22,24,25,26,27,31,32};
	
	@Override
	public Grammar decompress(byte[] arr, NodeDictionary dict, Map<Digram, List<Integer[]>> internalMap) throws NotSupportedException, IOException {
		//1. 4 bytes = length of start := X
		ByteBuffer bb = ByteBuffer.wrap(arr);
		byte[] startBytes = new byte[4];
		bb.get(startBytes);
		int startSize = ByteBuffer.wrap(startBytes).getInt();
		//2. X bytes = start Graph 
		byte[] start = new byte[startSize];
		bb = bb.slice();
		bb.get(start);
		Grph startGraph = decompressStart(start, dict);
		//3. decompress rules
		Grammar g = new Grammar(startGraph);
		bb = bb.slice();
		byte[] rules = new byte[arr.length-(Integer.BYTES+startSize)];
		bb.get(rules);
		Map<String, Digram> map = decompressRules(rules, internalMap);
		
		g.setRules(map);
		return g;
	}
	
	

	@Override
	public Map<String, Digram> decompressRules(byte[] arr, Map<Digram, List<Integer[]>> internalMap) throws IOException {
		//read the shit
		Map<String, Digram> ret = new HashMap<String, Digram>();
		int j=0;
		ByteBuffer bbuffer = ByteBuffer.wrap(arr);
		do {
			
			//1. read e1 INT * -1
			Integer e1=bbuffer.getInt()*-1;
			//2. read e2 INT
			Integer e2=bbuffer.getInt();
			// 3.1. read flags 
			// 3.2. read externals 
			byte flags = bbuffer.get();
			// 1XYY EXT1 EXT2
			// YY STRUCT
			byte structure = Integer.valueOf((flags & 63)).byteValue();
			byte size = Integer.valueOf(-1*(flags & -64) >> 6).byteValue();
			
			byte internalFlag=1;
			for(int i : oneInternalStructs) {
				if(i == structure) {
					internalFlag=0;
					break;
				}
			}
			Set<Integer> externals = getExternals(structure);
			
			
			//ext1 and ext2 -> 
			
			//4. read arr until leading bit is 1 (that is start of next digram)
			boolean nextDigram = false;
			List<Integer[]> internals = new LinkedList<Integer[]>();
			while(!nextDigram && bbuffer.hasRemaining()) {
				byte next = bbuffer.get();
				bbuffer.position(bbuffer.position()-1);
				
				if(next <0) {
					nextDigram = true;
					break;
				}
				internals.add(getNextInternals(bbuffer, size, internalFlag));
			}
			
			Digram d = new Digram(ResourceFactory.createResource(":"+e1), ResourceFactory.createResource(":"+e2), externals);
			d.setStructure(structure);
			internalMap.put(d, internals);
			ret.put(GrammarHelper.NON_TERMINAL_PREFIX+j++, d);
		}while(bbuffer.hasRemaining());
		return ret;
	}



	private Set<Integer> getExternals(byte struct) {
		Set<Integer> ret = new HashSet<Integer>();
		if(ArrayUtils.contains(externalIndexOne, struct))
			ret.add(0);
		if(ArrayUtils.contains(externalIndexTwo, struct))
			ret.add(1);
		if(ArrayUtils.contains(externalIndexThree, struct))
			ret.add(2);
		if(ArrayUtils.contains(externalIndexFour, struct))
			ret.add(3);
		return ret;
	}



	private byte getInternalFlag(byte struct) {
		if(struct == 0 || struct == 1 || struct == 4 || struct == 31) {
			return 0;
		}
		else if(ArrayUtils.contains(oneInternalStructs, struct)) {
			return 1;
		}
		return 2;
	}



	private Integer[] getNextInternals(ByteBuffer bbuffer, byte classFlag, byte internalFlag) {
		Integer[] ret = new Integer[internalFlag+1];
		for(int i=0;i<internalFlag+1;i++) {
			if(classFlag==0) {
				//byte
				ret[i] = Integer.valueOf(bbuffer.get());
			}
			if(classFlag==1) {
				//short
				ret[i] = Integer.valueOf(bbuffer.getShort());
			}
			if(classFlag==2) {
				//int
				ret[i] = bbuffer.getInt();
			}
			if(classFlag==3) {
				//long
				//TODO use long instead of integer
				ret[i] = Long.valueOf(bbuffer.getLong()).intValue();
			}
		}
		return ret;
	}



	@Override
	public Model decompressStart(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException {
		List<Integer>[] deserCRS =  deserializer.deserialize(arr);
		
		// reverse CRS
		List<List<Integer[]>> rcMatrix = new LinkedList<List<Integer[]>>();
		int oldPtr=0;
		for(int i=1;i<deserCRS[2].size();i++) {
			List<Integer[]> row = new LinkedList<Integer[]>();
			//rowPtr now tells the amount of colIndex in the row
			Integer rowPtr = deserCRS[2].get(i);
			for(int k=oldPtr;k<rowPtr;k++) {
				//colInd
				Integer col = deserCRS[0].get(k);
				row.add(new Integer[] {col, deserCRS[1].get(k)});
			}
			rcMatrix.add(row);
			oldPtr = rowPtr;
		}
		
		// reverse Matrice
		Model indexedGraph = GraphUtils.createModelFromRCMatrice(rcMatrix, dict);
		
		return indexedGraph;
	}
*/
}
