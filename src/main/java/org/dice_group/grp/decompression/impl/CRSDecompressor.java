package org.dice_group.grp.decompression.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.serialization.impl.CRSDeserializer;
import org.dice_group.grp.util.GraphUtils;
import org.rdfhdt.hdtjena.NodeDictionary;

public class CRSDecompressor implements GrammarDecompressor {

	private CRSDeserializer deserializer = new CRSDeserializer();
	
	@Override
	public Grammar decompress(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException {
		//1. 4 bytes = length of start := X
		int startSize = ByteBuffer.wrap(arr, 0, Integer.BYTES).getInt(0);
		//2. X bytes = start Graph 
		byte[] start = ByteBuffer.wrap(arr, Integer.BYTES, Integer.BYTES+startSize).array();
		Model startGraph = decompressStart(start, dict);
		//3. decompress rules
		Grammar g = new Grammar(startGraph);
		Map<String, Digram> map = decompressRules(ByteBuffer.wrap(arr, Integer.BYTES+startSize, arr.length).array(), g);
		
		g.setRules(map);
		return g;
	}
	
	

	@Override
	public Map<String, Digram> decompressRules(byte[] arr, Grammar g) throws IOException {
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
			byte classFlag = Integer.valueOf((flags ^ 48) >> 6).byteValue();
			byte internalFlag = Integer.valueOf((flags ^ 64) >> 4).byteValue();
			byte ext1 = Integer.valueOf((flags ^ 12) >> 2).byteValue();
			byte ext2 = Integer.valueOf(flags ^ 3).byteValue();
			Set<Integer> externals = new HashSet<Integer>();
			//ext1 and ext2 -> 
			externals.add(ext1+1);
			if(ext1 != ext2) {
				externals.add(ext2+1);
			}
			//4. read arr until leading bit is 1 (that is start of next digram)
			boolean nextDigram = false;
			List<Integer[]> internals = new LinkedList<Integer[]>();
			while(!nextDigram) {
				byte next = bbuffer.get();
				bbuffer.position(bbuffer.position()-1);
				if((next ^ 128 )>0) {
					nextDigram = true;
					break;
				}
				internals.add(getNextInternals(bbuffer, classFlag, internalFlag));
			}
			//deindex e1 and e2
			Digram d = new Digram(ResourceFactory.createResource(":"+e1), ResourceFactory.createResource(":"+e2), externals);
			g.getReplaced().put(d, getDigramOccurences(d, internals));
			ret.put(GrammarHelper.NON_TERMINAL_PREFIX+j, d);
		}while(bbuffer.hasRemaining());
		return ret;
	}



	/**
	 * TODO Only provides placeholder occurences (statements do not have nodes yet!)
	 * @param d
	 * @param internals
	 * @return
	 */
	private List<DigramOccurence> getDigramOccurences(Digram d, List<Integer[]> internals) {
		return null;
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

}
