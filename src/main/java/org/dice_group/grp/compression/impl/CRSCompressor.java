package org.dice_group.grp.compression.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
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
	 * TODO 
	 * can be compressed better 
	 * 
	 * 
	 */
	@Override
	public byte[] serializeRules(Grammar grammar) throws IOException {
		//1. sort rules using number of externals
		List<String> singleExternals = new LinkedList<String>();
		List<String> doubleExternals = new LinkedList<String>();
		Map<String, Digram> rules = grammar.getRules();
		for(String nt : rules.keySet()) {
			if(rules.get(nt).getExternalIndexes().size() == 1 ) {
				singleExternals.add(nt);
			}
			else {
				doubleExternals.add(nt);
			}
		}
		//2. serialize rule after rule 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(String nt : singleExternals) {
			
			byte[] serRule = serializeRule(nt, rules.get(nt));
			baos.write(serRule);
		}
		//add sdSplit
		baos.write(SINGLE_DOUBLE_SPLIT);
		for(String nt : doubleExternals) {
			byte[] serRule = serializeRule(nt, rules.get(nt));
			baos.write(serRule);
		}
		
		return baos.toByteArray();
	}
	
	/**
	 * @return
	 */
	private byte[] serializeRule(String terminal, Digram m) {
		// sizeterminal.ext1(ext2)e1e2[o1{int1(, int2)}(,o2{int1, int2},..)]
		Integer e1 = Integer.valueOf(m.getEdgeLabel1().toString().replace(":p", ""));
		Integer e2 = Integer.valueOf(m.getEdgeLabel2().toString().replace(":p", ""));
		byte[] terminalBytes = terminal.getBytes();
		byte[] be1 = ByteBuffer.allocate(Integer.BYTES).putInt(e1).array();
		byte[] be2 = ByteBuffer.allocate(Integer.BYTES).putInt(e2).array();
		//TODO external Indexes 1 BYTE FOR both is sufficient
		
		Set<Integer> ext= m.getExternalIndexes();
		ByteBuffer bf = ByteBuffer.allocate(ext.size()*Integer.BYTES);
		for(Integer extInt: ext) {
			bf.putInt(extInt);
		}
		byte[] extBytes = bf.array();
		//TODO getInternals
		List<Integer[]> internals = new LinkedList<Integer[]>();
		ByteBuffer intByteBuffer = ByteBuffer.allocate(Integer.BYTES*internals.get(0).length);
		int bufferIndex=0;
		for(Integer[] occInternals : internals) {
			for(Integer internal : occInternals) {
				intByteBuffer.putInt(bufferIndex,internal);
				bufferIndex+=Integer.BYTES;
			}
		}
		byte[] internalsBytes = intByteBuffer.array();
		byte[] ret = new byte[Integer.BYTES+1+terminalBytes.length+2*Integer.BYTES+internalsBytes.length];
		System.arraycopy(ret.length-Integer.BYTES, 0, ret, 0,Integer.BYTES);
		System.arraycopy(terminalBytes, 0, ret, Integer.BYTES,terminalBytes.length);
		System.arraycopy(TERMINAL_SPLIT, 0, ret, Integer.BYTES+terminalBytes.length, 1);
		System.arraycopy(be1, 0, ret, Integer.BYTES+terminalBytes.length+1, Integer.BYTES);
		System.arraycopy(be2, 0, ret, terminalBytes.length+1+2*Integer.BYTES, Integer.BYTES);
		System.arraycopy(internalsBytes, 0, ret, terminalBytes.length+1+3*Integer.BYTES, internalsBytes.length);

		return ret;
		
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
