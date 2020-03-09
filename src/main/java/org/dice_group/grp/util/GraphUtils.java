package org.dice_group.grp.util;

import java.util.*;

import grph.Grph;
import org.apache.jena.base.Sys;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.dice_group.grp.grammar.Statement;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeCreator;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
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
	 * @return
	 */
	public static Map<Integer, List<Integer[]>> createIntegerRCMatrix(List<Statement> g){
		List<List<Integer[]>> ret = new LinkedList<List<Integer[]>>();
		Map<Integer, List<Integer[]>> map = new HashMap<Integer, List<Integer[]>>();
		Set<Integer> props = new HashSet<Integer>();
		//TODO first val is row Count
		long size=0;
		// create mapping with this
		Collections.sort(g, new Comparator<Statement>() {
			@Override
			public int compare(Statement s1, Statement s2) {
				int sc = s1.getSubject().compareTo(s2.getSubject());
				if(sc!=0){
					return sc;
				}
				int oc = s1.getObject().compareTo(s2.getObject());
				if(oc!=0){
					return oc;
				}
				return  s1.getPredicate().compareTo(s2.getPredicate());
			}
		});
		for(Statement stmt : g){
			//Statement stmt = stmtI.next();
			Integer s = stmt.getSubject();
			Integer p = stmt.getPredicate();
			props.add(p);
			Integer o = stmt.getObject();
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
		return map;
	}


	public static List<List<Short[]>> createShortRCMatrix(List<Statement> g){
		List<List<Short[]>> ret = new LinkedList<List<Short[]>>();
		Map<Short, List<Short[]>> map = new HashMap<Short, List<Short[]>>();


		long size=0;
		// create mapping with this
		for(Statement stmt : g){
			//Statement stmt = stmtI.next();
			Short s = stmt.getSubject().shortValue();
			Short p = stmt.getPredicate().shortValue();
			Short o = stmt.getObject().shortValue();
			size = Math.max(o, Math.max(size, s));
			if(map.containsKey(s)) {
				map.get(s).add(new Short[] {o,p});
			}
			else {
				List<Short[]> col = new LinkedList<Short[]>();
				col.add(new Short[] {o,p});
				map.put(s, col);
			}
		}
		for(Integer i=0;i<=size;i++) {
			if(map.containsKey(i)) {
				ret.add(map.get(i));
			}
			else {
				ret.add(new LinkedList<Short[]>());
			}
		}
		return ret;
	}


	public static Node parseLiteral(String l){
		String literalStr = l.replace("\\\"", "\"").replace("\\-", "-").replace("\\_", "_");
		if(literalStr.startsWith("\"") && literalStr.endsWith("\"")){
			literalStr = literalStr.substring(1, literalStr.length()-1);
		}
		if(literalStr.matches(".*@[a-zA-Z0-9\\s]+]")){
			String val = literalStr.substring(0, literalStr.lastIndexOf("@"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String lang = literalStr.substring(literalStr.indexOf("@")+1);
			return NodeFactory.createLiteral(val, lang);
		}
		if(literalStr.matches(".*\\^\\^<.*>")){
			String val = literalStr.substring(0,literalStr.lastIndexOf("^^"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String uri = literalStr.substring(literalStr.lastIndexOf("^^")+3, literalStr.length()-1);
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(uri);
			return NodeFactory.createLiteral(val, dtype);

		}


		/*
		Node literal = JenaNodeCreator.createLiteral(literalStr);
		if(literal.getLiteralLanguage()!=null && !literal.getLiteralLanguage().isEmpty()){
			return ResourceFactory.createLangLiteral(literal.getLiteralValue().toString(), literal.getLiteralLanguage());
		}
		else if(literal.getLiteralDatatype()!=null){

			return ResourceFactory.createTypedLiteral(literal.getLiteralValue());
		}

		 */
		return NodeFactory.createLiteral(literalStr);
	}

	public static RDFNode parseHDTLiteral(Node l){
		String literalStr = l.toString().replace("\\\"", "\"").replace("\\-", "-").replace("\\_", "_");
		if(literalStr.startsWith("\"") && literalStr.endsWith("\"")){
			literalStr = literalStr.substring(1, literalStr.length()-1);
		}
		if(literalStr.matches(".*@[a-zA-Z0-9\\s]+]")){
			String val = literalStr.substring(0, literalStr.lastIndexOf("@"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String lang = literalStr.substring(literalStr.indexOf("@")+1);
			return ResourceFactory.createLangLiteral(val, lang);
		}
		if(literalStr.matches(".*\\^\\^<.*>")){
			String val = literalStr.substring(0,literalStr.lastIndexOf("^^"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String uri = literalStr.substring(literalStr.lastIndexOf("^^")+3, literalStr.length()-1);
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(uri);
			return ResourceFactory.createTypedLiteral(val, dtype);

		}


		/*
		Node literal = JenaNodeCreator.createLiteral(literalStr);
		if(literal.getLiteralLanguage()!=null && !literal.getLiteralLanguage().isEmpty()){
			return ResourceFactory.createLangLiteral(literal.getLiteralValue().toString(), literal.getLiteralLanguage());
		}
		else if(literal.getLiteralDatatype()!=null){

			return ResourceFactory.createTypedLiteral(literal.getLiteralValue());
		}

		 */
		return ResourceFactory.createPlainLiteral(literalStr);
	}
	
	/**
	 * TODO deindex otf
	 * @param rcMatrix
	 * @param nonTerminalEdges
	 * @return
	 */
	public static Model createModelFromRCMatrice(List<List<Integer[]>> rcMatrix, NodeDictionary dict, List<Statement> nonTerminalEdges){
		Model m = ModelFactory.createDefaultModel();
		Set<Integer> props = new HashSet<Integer>();

		int rowPtr =0;
		for(List<Integer[]> row : rcMatrix) {
			for(Integer[] col : row) {



				// check if nonTerminal
				Property p = ResourceFactory.createProperty(dict.getNode(col[1], TripleComponentRole.PREDICATE).getURI());
				props.add(col[1]);


				if(p.getURI().startsWith("http://n.")){
						//add statement to nonTerminalEdges
						Statement stmt = new Statement(rowPtr, col[1], col[0]);
						nonTerminalEdges.add(stmt);
				}
				else {
					Resource s = ResourceFactory.createResource(dict.getNode(rowPtr, TripleComponentRole.OBJECT).getURI());
					try {
						Node n = getObject(col[0], dict);
						RDFNode o;
						if (n.getURI().startsWith("\\\"")) {
							o = GraphUtils.parseHDTLiteral(n);
						} else {
							o = ResourceFactory.createResource(dict.getNode(col[0], TripleComponentRole.OBJECT).getURI());
						}
						m.add(s, p, o);
					}catch(NullPointerException e){
						e.printStackTrace();
						System.out.println();
					}
				}
			}
			rowPtr++;
		}
		System.out.println(props);
		return m;
	}

	public static Node getObject(int o, NodeDictionary dict){
		try {
			return dict.getNode(o, TripleComponentRole.OBJECT);
		}catch(Exception e){
			return dict.getNode(o, TripleComponentRole.OBJECT);
		}
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
