package org.dice_group.grp.compression.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;


import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.tdb.index.Index;
import org.dice_group.grp.compression.AbstractGrammarCompressor;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.index.impl.InternalIndexer;
import org.dice_group.grp.serialization.DigramSerializer;
import org.dice_group.grp.serialization.GraphSerializer;
import org.dice_group.grp.serialization.impl.CRSSerializer;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.util.BoundedList;
import org.dice_group.grp.util.GraphUtils;
import org.dice_group.grp.util.IndexedRDFNode;
import org.dice_group.grp.util.RDFHelper;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

public class CRSCompressor extends AbstractGrammarCompressor {

	public static final byte TERMINAL_SPLIT = '.';
	public static final byte SINGLE_DOUBLE_SPLIT = '\t';

	
	private GraphSerializer serializer = new CRSSerializer();



	

	@Override
	public byte[] compress(List<Statement> g, int vSize) throws NotSupportedException {
		//Long noOfProperties = RDFHelper.getPropertyCount(graph);
		Set<Integer> props = new HashSet<Integer>();
		for(Statement s : g){
			props.add(s.getPredicate());
		}
		Integer noOfProperties=props.size();
		props.clear();

		byte[] ret = null;
		if(noOfProperties == null) {
			return null;
		}
		if(noOfProperties<=Byte.MAX_VALUE) {
			Map<Integer, List<Integer[]>>  matrix = GraphUtils.createIntegerRCMatrix(g);
			ret = createCRS(matrix, Integer.class);
		}
		else if(noOfProperties<=Short.MAX_VALUE) {
			Map<Integer, List<Integer[]>> matrix = GraphUtils.createIntegerRCMatrix(g);
			ret = createCRS(matrix, Short.class);
		}
		else if(noOfProperties<=Integer.MAX_VALUE) {
			Map<Integer, List<Integer[]>> matrix = GraphUtils.createIntegerRCMatrix(g);
			ret = createCRS(matrix, Integer.class);
		}
		else {
			throw new NotSupportedException("Currently RDF this big is not supported and limited to 2^31-1 triples");
		}
		return ret;
	}

	public  <T extends Number>  byte[] createCRS(Map<Integer, List<Integer[]>> matrix, Class<? extends Number> noFormat) {
		List<T> val = new LinkedList<T>();
		List<Integer> colRow = new LinkedList<Integer>();
		List<Integer> rowPtr = new LinkedList<Integer>();
		rowPtr.add(0);
		List<Integer> keys = Lists.newArrayList(matrix.keySet());
		Set<Integer> props = new HashSet<Integer>();
		Collections.sort(keys);

		int rowPtrCount = 0;
		for(int i=0;i<keys.get(keys.size()-1);i++) {

			if(matrix.containsKey(i)) {
				List<Integer[]> currentRow = matrix.get(i);
				for (int j = 0; j < currentRow.size(); j++) {
					Integer[] cell = currentRow.get(j);
					if (cell[1].longValue() > 0) {
						if (noFormat == Short.class) {
							val.add((T) Short.valueOf(cell[1].shortValue()));
							props.add(cell[1]);
						} else if (noFormat == Integer.class) {
							val.add((T) Integer.valueOf(cell[1].intValue()));
						}
						if(cell[0]==-1){
							System.out.println();
						}
						colRow.add(cell[0]);
						rowPtrCount++;
					}
				}
			}
			else{
			}
			rowPtr.add(rowPtrCount);
		}
		byte[] serializedCRS = serializer.serialize(val, colRow, rowPtr);
		return serializedCRS;
	}



	



}
