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
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.CRSDeserializer;
import org.dice_group.grp.util.GraphUtils;
import org.rdfhdt.hdtjena.NodeDictionary;

public class CRSDecompressor implements GrammarDecompressor {

	private CRSDeserializer deserializer = new CRSDeserializer();



	private int startID =-1;
	public int getStartID() {
		return startID;
	}

	public void setStartID(int startID) {
		this.startID = startID;
	}



	@Override
	public Graph decompressStart(byte[] arr, NodeDictionary dict, List<Statement> nonTerminalEdges) throws NotSupportedException, IOException {
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
				if(col == -1){
					System.out.println();
				}
				row.add(new Integer[] {col, deserCRS[1].get(k)});
			}
			rcMatrix.add(row);
			oldPtr = rowPtr;
		}
		
		// reverse Matrice
		Model indexedGraph = GraphUtils.createModelFromRCMatrice(rcMatrix, dict, nonTerminalEdges);
		return indexedGraph.getGraph();
	}

}
