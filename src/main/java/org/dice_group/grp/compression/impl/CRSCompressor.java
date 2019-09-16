package org.dice_group.grp.compression.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.CRSSerializer;
import org.dice_group.grp.util.GraphUtils;
import org.dice_group.grp.util.RDFHelper;

public class CRSCompressor implements GrammarCompressor {

	public static final byte TERMINAL_SPLIT = '.';
	public static final byte SINGLE_DOUBLE_SPLIT = '\t';

	
	private CRSSerializer serializer = new CRSSerializer();

	@Override
	public byte[] compress(Grammar grammar) throws NotSupportedException, IOException {
		byte[] start = compress(grammar.getStart());
		byte[] rules = serializeRules(grammar);
		byte[] serialized = new byte[start.length+1+rules.length];
		byte[] startSize = ByteBuffer.allocate(Integer.BYTES).putInt(start.length).array();
		System.arraycopy(startSize, 0, serialized, 0,Integer.BYTES);
		System.arraycopy(start, 0, serialized, Integer.BYTES,start.length);
		System.arraycopy(rules, 0, serialized, Integer.BYTES+start.length, rules.length);
		return serialized;
	}

	/*
	 * 
	 * 
	 */
	@Override
	public byte[] serializeRules(Grammar grammar) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Map<String, Digram> rules = grammar.getRules();
		List<Digram> digrams = new LinkedList<Digram>();
		//add digrams in order of the Non Terminals, thus it is reversable without saving NT
		for(Integer i=0;i<rules.size();i++) {
			digrams.add(rules.get(GrammarHelper.NON_TERMINAL_PREFIX+i.toString()));
		}
		for(Digram digram : digrams) {
			byte[] serRule = serializeDigram(digram);
			baos.write(serRule);
		}
		return baos.toByteArray();
	}
	
	/**
	 * @return
	 * @throws IOException 
	 */
	private byte[] serializeDigram(Digram m) throws IOException {
		// e1e2{flag|ext1|ext2}[o1{int1(, int2)}(,o2{int1, int2},..)]
		//e1 is complement => we know when next digram starts
		Integer e1 = -1*Integer.valueOf(m.getEdgeLabel1().toString().replace(":p", ""));
		Integer e2 = Integer.valueOf(m.getEdgeLabel2().toString().replace(":p", ""));
		
		//set ext bytes (0, 1, 2, 3) if only 1 ext ext1=ext2
		List<Integer> ext= new LinkedList<Integer>(m.getExternalIndexes());
		Collections.sort(ext);
		Integer exti1 = 0;
		Integer exti2 = 0;
		exti1 = ext.get(0);
		if(ext.size()>1) {
			exti2 = ext.get(1);
		}
		else {
			exti2=exti1;
		}
		exti2 = exti2 << 2;
		//create combined external
		byte external = Integer.valueOf(exti2+exti1).byteValue();
		//TODO getInternals
		List<Long[]> internals = new LinkedList<Long[]>();
		byte sizeFlag= getSizeFlag(internals);
		byte internalsFlag = Integer.valueOf((2+internals.get(0).length) << 4).byteValue(); 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for(Long[] occInternals : internals) {
			for(Long internal : occInternals) {
				if(sizeFlag==0) {
					baos.write(internal.byteValue());
				}
				else if(sizeFlag==1) {
					baos.write(internal.shortValue());
				}
				else if(sizeFlag==2) {
					baos.write(internal.intValue());
				}
				else {
					baos.write(ByteBuffer.allocate(Long.BYTES).putLong(internal).array());
				}
			}
		}
		/* set flags
		 * 1XYYX_1X_2 with 
		 * X = 1 if two internals, 0 if one internal, 
		 * YY as internals size 0 = byte, 1 = short, 2= int, 3= long
		 * X_i as external i
		 */
		byte flags = Integer.valueOf(internalsFlag + (sizeFlag << 6) + external).byteValue();
		
		byte[] internalsBytes = baos.toByteArray();
		ByteBuffer ret = ByteBuffer.allocate(9+internalsBytes.length);
		ret.putInt(e1);
		ret.putInt(e2);
		ret.put(flags);
		ret.put(internalsBytes);
		return ret.array();
		
	}
	
	private byte getSizeFlag(List<Long[]> internals) {
		Long max = 0L;
		for(Long[] internal : internals) {
			for(Long internalEl : internal) {
				max = Math.max(max, internalEl);
			}			
		}
		if(max<=Byte.MAX_VALUE) {
			return 0;
		}
		if(max<=Short.MAX_VALUE) {
			return 1;
		}
		if(max<=Integer.MAX_VALUE) {
			return 2;
		}
		//otherwise long
		return 3;
	}

	@Override
	public byte[] compress(Model graph) throws NotSupportedException {
		Long noOfProperties = RDFHelper.getPropertyCount(graph);
		byte[] ret = null;
		if(noOfProperties == null) {
			return null;
		}
		if(noOfProperties<=Byte.MAX_VALUE) {
			List<List<Integer[]>> matrix = GraphUtils.createIntegerRCMatrix(graph);
			ret = createCRS(matrix, Byte.class);
		}
		if(noOfProperties<=Short.MAX_VALUE) {
			List<List<Integer[]>> matrix = GraphUtils.createIntegerRCMatrix(graph);
			ret = createCRS(matrix, Short.class);
		}
		if(noOfProperties<=Integer.MAX_VALUE) {
			List<List<Integer[]>> matrix = GraphUtils.createIntegerRCMatrix(graph);
			ret = createCRS(matrix, Integer.class);
		}
		else {
			throw new NotSupportedException("Currently RDF this big is not supported and limited to 2^31-1 triples");
		}
		return ret;
	}

	public  <T extends Number>  byte[] createCRS(List<List<T[]>> matrix, Class<? extends Number> noFormat) {
		List<T> val = new LinkedList<T>();
		List<Integer> colRow = new LinkedList<Integer>();
		List<Integer> rowPtr = new LinkedList<Integer>();
		rowPtr.add(0);
		for(int i=0;i<matrix.size();i++) {
			List<T[]> currentRow = matrix.get(i);
			int rowPtrCount = 0;
			for(int j=0;j<currentRow.size();j++) {
				T[] cell = currentRow.get(j);
				if(cell[1].longValue()>0) {
					val.add(cell[1]);
					colRow.add(cell[0].intValue());
					rowPtrCount++;
				}
			}
			rowPtr.add(rowPtrCount);
		}
		byte[] serializedCRS = serializer.serialize(val, colRow, rowPtr);
		return serializedCRS;
	}



	



}
