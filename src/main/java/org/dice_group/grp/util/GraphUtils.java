package org.dice_group.grp.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

public class GraphUtils {

	private static String[] replaceStrings = new String[] {":s", ":n", ":p", ":o"};

	/**
	 * 
	 * 
	 * 
	 * creates a matrix with outer List as rows and inner List as columns
	 * S\O 1 2 3 4
	 *     ___________
	 * 1   | 0 1 0 0 |
	 * 2   | 2 4 1 5 |
	 * 3   | 1 0 0 0 |
	 * 4   | 3 0 1 0 | 
	 *	   -----------
	 * 
	 * will be saved RAM friendly as:
	 * 
	 * LIST: ( (1, 1) ), 
	 *       ( (0, 2), (1, 4), (2, 1), (3, 5) ), 
	 *       ( (0, 1) ),
	 *       ( (0, 3), (2, 1) )
	 *       
	 * thus optimizing it for sparse matrices
	 * 
	 * 
	 * @param <T>
	 * @param m
	 * @return
	 */
	public static List<List<Integer[]>> createIntegerRCMatrix(Model m){
		List<List<Integer[]>> ret = new LinkedList<List<Integer[]>>();
		Map<Integer, List<Integer[]>> map = new HashMap<Integer, List<Integer[]>>();

		
		long size=0;
		// create mapping with this
		StmtIterator stmtI = m.listStatements();
		while(stmtI.hasNext()) {
			Statement stmt = stmtI.next();
			Integer s = getRDFIndex(stmt.getSubject());
			Integer p = getRDFIndex(stmt.getPredicate());
			Integer o = getRDFIndex(stmt.getObject());
			size = Math.max(o, Math.max(size, s));
			if(map.containsKey(s)) {
				map.get(s).add(new Integer[] {o,p});
			}
			else {
				List<Integer[]> col = new LinkedList<Integer[]>();
				col.add(new Integer[] {o,p});
				map.put(s, col);
			}
		}
		for(Integer i=0;i<=size;i++) {
			if(map.containsKey(i)) {
				ret.add(map.get(i));
			}
			else {
				ret.add(new LinkedList<Integer[]>());
			}
		}
		return ret;
	}
	
	
	/**
	 * 
	 * @param rcMatrix
	 * @return
	 */
	public static Model createModelFromRCMatrice(List<List<Integer[]>> rcMatrix, NodeDictionary dict){
		Model m = ModelFactory.createDefaultModel();
		
		int rowPtr =0;
		for(List<Integer[]> row : rcMatrix) {
			for(Integer[] col : row) {
				Resource s = ResourceFactory.createResource(":s"+rowPtr);
				// check if nonTerminal
				Property p = ResourceFactory.createProperty(":p"+col[0]);
				if(dict !=null){
					Node n = dict.getNode(col[0], TripleComponentRole.PREDICATE);
					if(n.toString().matches(":n[0-9]+")) {
						// nterminal
						p = ResourceFactory.createProperty(n.toString());
					}
				}
				Resource o = ResourceFactory.createResource(":o"+col[1]);
				m.add(s, p, o);
			}
			rowPtr++;
		}
		
		return m;
	}
	
	public static Integer getRDFIndex(RDFNode node) {
		try {
			String nodeStr = node.toString();
			for(String replace : replaceStrings ) {
				nodeStr = nodeStr.replace(replace, "");
			}
			return Integer.valueOf(nodeStr);
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
}
