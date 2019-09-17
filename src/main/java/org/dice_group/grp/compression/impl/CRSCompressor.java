package org.dice_group.grp.compression.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.DigramSerializer;
import org.dice_group.grp.serialization.GraphSerializer;
import org.dice_group.grp.serialization.impl.CRSSerializer;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.util.GraphUtils;
import org.dice_group.grp.util.RDFHelper;

public class CRSCompressor implements GrammarCompressor {

	public static final byte TERMINAL_SPLIT = '.';
	public static final byte SINGLE_DOUBLE_SPLIT = '\t';

	
	private GraphSerializer serializer = new CRSSerializer();

	private DigramSerializer digramSerializer;
	
	@Override
	public byte[] compress(Grammar grammar) throws NotSupportedException, IOException {
		digramSerializer = new DigramSerializerImpl(grammar);
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
			byte[] serRule = digramSerializer.serialize(digram);
			baos.write(serRule);
		}
		return baos.toByteArray();
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
