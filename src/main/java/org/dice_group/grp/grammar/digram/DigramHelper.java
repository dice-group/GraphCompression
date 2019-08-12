package org.dice_group.grp.grammar.digram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

public class DigramHelper {
	
	private static final String CASE_1 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where {\n" + 
			"?n1 ?e1 ?n2 .\n" + 
			"\n" + 
			"?n2 ?e2 ?n3 .\n" + 
			"\n" + 
			"} ";
	private static final String CASE_2 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where {\n" + 
			"?n1 ?e1 ?n2 .\n" + 
			"\n" + 
			"?n3 ?e2 ?n2 .\n" + 
			"\n" + 
			"} ";
	private static final String CASE_3 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where {\n" + 
			"?n2 ?e1 ?n1 .\n" + 
			"\n" + 
			"?n2 ?e2 ?n3 .\n" + 
			"\n" + 
			"} ";
	
	private static final String [] CASES = {
		CASE_1, 
		CASE_2, 
		CASE_3	
	};
	

	protected static Set<Integer> getExternalIndexes(Statement e1, Statement e2, Set<RDFNode> externals) {
		Set<Integer> externalIndex = new HashSet<Integer>();
		for(RDFNode node : externals) {
			if(e1.getSubject().equals(node)) {
				externalIndex.add(0);
			}
			if(e1.getObject().equals(node)) {
				externalIndex.add(1);
			}
			if(e2.getSubject().equals(node)) {
				externalIndex.add(2);
			}
			if(e2.getObject().equals(node)) {
				externalIndex.add(3);
			}
		}
		return externalIndex;
	}

	/**
	 * Find all non overlapping Digrams and their Occurences in the graph 
	 * The structure might be stupid, as it is super RAM intensive. 
	 * Maybe we should use Digram->Binary File, to receive the next most frequent Digram
	 * or split it into get Digrams and getOccurenceForDigram
	 * 
	 * 
	 * frequence should be updated and sorted
	 * 
	 * @param graph
	 * @return
	 */
	public static Map<Digram, Set<DigramOccurence>> findDigramsOcc(Model graph, List<Digram> frequence) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static Set<DigramOccurence> findDigrams(Model graph, Digram dig) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Searches the model for non overlapping occurrences, based on three main patterns:
	 * Case 1:
	 * n1 e1 n2*
	 * n2* e2 n3
	 * 
	 * Case 2:
	 * n1 e1 n2*
	 * n3 e2 n2*
	 * 
	 * Case 3:
	 * n2* e1 n1
	 * n2* e2 n3
	 * 
	 * 
	 * @param graph
	 * @return
	 */
	public static Set<DigramOccurence> findDigramOccurrences(Model graph) {		
		// only disjoint occurrences are added
		Set<DigramOccurence> occurrences = new HashSet<DigramOccurence>();
		
		for(int i = 0; i< CASES.length; i++) {
			QueryExecution queryExec = queryModel(graph, CASES[i]);
			List<QuerySolution>  results = selectModel(queryExec);
			for(QuerySolution solution: results){
		        
		        RDFNode n1 = solution.get("n1");
		        Property e1 = ResourceFactory.createProperty(solution.get("e1").toString());
		        RDFNode n2 = solution.get("n2");
		       
		        Property e2 = ResourceFactory.createProperty(solution.get("e2").toString());
		 
		        RDFNode n3 = solution.get("n3");
		        
		        Statement stmt1 = null;
		        Statement stmt2 = null;
		        switch (CASES[i]) {
		        	case CASE_1:
		        		stmt1 = ResourceFactory.createStatement(n1.asResource(), e1, n2);
			        	stmt2 = ResourceFactory.createStatement(n2.asResource(), e2, n3);
			        	break;
					
					case CASE_2:
						stmt1 = ResourceFactory.createStatement(n1.asResource(), e1, n2);
			        	stmt2 = ResourceFactory.createStatement(n3.asResource(), e2, n2);
					break;
					
					case CASE_3:
						stmt1 = ResourceFactory.createStatement(n2.asResource(), e1, n1);
			        	stmt2 = ResourceFactory.createStatement(n2.asResource(), e2, n3);
					break;

				default:
					break;
				}
				
				if(!stmt1.equals(stmt2)) {
					List<RDFNode> nodes = new ArrayList<RDFNode>();
					nodes.add(n1);
					nodes.add(n2);
					nodes.add(n3);
					nodes.add(e1);
					nodes.add(e2);
					
					Set<RDFNode> externals = findExternals(nodes, stmt1, stmt2, graph, CASES[i]);
//					
//			        Digram digram = new Digram(e1, e2, getExternalIndexes(stmt1, stmt2, externals));
//			        digrams.add(digram);
//			        
			        // it's only an occurrence if it has at least one external node
					// maximum of 2 external nodes && externals.size() < 2
			        if(!externals.isEmpty() ) {
			        	DigramOccurence occurrence = new DigramOccurence(stmt1, stmt2, externals);
			        	if(occurrences.isEmpty()) {
			        		occurrences.add(occurrence);
			        	}
			        	if(occurrence.isNonOverlapping(occurrences)) {
		        			occurrences.add(occurrence);
		        			break;
		        		}
			        }
				}
		    }
		}
		return occurrences;
	}
	
	/**
	 * Checks which of the nodes of both statements, are connected to the overall graph
	 * @param stmt1
	 * @param stmt2
	 * @param graph
	 * @param digramCase 
	 * @return the set of external nodes
	 */
	public static Set<RDFNode> findExternals(List<RDFNode> nodes, Statement stmt1, Statement stmt2, Model graph, String digramCase){
		Set<RDFNode> externals = new HashSet<RDFNode>();
		String n1 = getNodeName(nodes.get(0));
		String n2 = getNodeName(nodes.get(1));
		String n3 = getNodeName(nodes.get(2));
		String e1 = getNodeName(nodes.get(3));
		String e2 = getNodeName(nodes.get(4));
		
		StringBuilder n1Query = new StringBuilder();
		StringBuilder n2Query = new StringBuilder();
		StringBuilder n3Query = new StringBuilder();
		
		switch (digramCase) {
			// n1 - e1 - n2 - e2 - n3
			case CASE_1:
				
				// n1 
				n1Query.append("ASK WHERE { { ").append(n1).append(" ?p ?n . "); 
				n1Query.append("MINUS { ").append(n1).append(" ?q ?m .");
				n1Query.append(" FILTER (?n = ?m && ").append(n2).append(" = ?m && ?q = ?p && ?q = ").append(e1).append(") ");
				n1Query.append("} } UNION { ?s ?p1 ").append(n1).append(" . } }");
				
				//n2
				n2Query.append("ASK WHERE { { { "); 
				n2Query.append(n2).append(" ?p ?n .");
				n2Query.append(" MINUS { ").append(n2).append(" ?q ?m .");
				n2Query.append(" FILTER (?n = ?m && ").append(n3).append(" = ?m && ?q = ?p && ?q = ").append(e2).append(") } }");
				n2Query.append(" } UNION { ?s ?p1 ").append(n2).append(" .");
				n2Query.append(" MINUS { ?a ?b ").append(n2).append(" . FILTER (?s = ?a && ");
				n2Query.append(n1).append(" = ?a && ?b = ?p1 && ?b = ").append(e1).append(") } } }");
				
				//n3
				n3Query.append("ASK WHERE { { ").append(n3).append(" ?p ?n . } UNION { ?s ?p1 ").append(n3).append(" . MINUS { ?a ?b ");
				n3Query.append(n3).append(" . FILTER (?s = ?a && ");
				n3Query.append(n2).append(" = ?a && ?b = ?p1 && ?b = ").append(e2).append(") } } }");
				
				break;
			// n1 - e1 - n2 - n3 - e2 -n2
			case CASE_2:
				
				// n1 
				n1Query.append("ASK WHERE { {").append(n1).append(" ?p ?n . "); 
				n1Query.append("MINUS { ").append(n1).append(" ?q ?m .");
				n1Query.append("FILTER (?n = ?m && ").append(n2).append(" = ?m && ?q = ?p && ?q = ").append(e1).append(")");
				n1Query.append("}} UNION { ?s ?p1 ").append(n1).append(" .}}");
				
				// n2
				n2Query.append("ASK WHERE { {");
				n2Query.append(n2).append(" ?p ?n . }").append(" UNION { ?s ?p1 ").append(n2).append(" . MINUS { ?a ?b ").append(n2);
				n2Query.append(" . FILTER (?s = ?a && ").append(n1).append(" =?a && ?b=?p1 && ?b=").append(e1).append(")");
				n2Query.append(" FILTER (?s = ?a && ").append(n3).append(" =?a && ?b=?p1 && ?b=").append(e2).append(") } } }");
				
				// n3
				n3Query.append("ASK WHERE { {").append(n3).append(" ?p ?n . "); 
				n3Query.append("MINUS { ").append(n3).append(" ?q ?m . FILTER (?n = ?m && ");
				n3Query.append(n2).append(" =?m && ?q=?p && ?q=").append(e2).append(") }} UNION { ?s ?p1 ");
				n3Query.append(n3).append(" .}}");		
				break;
			// n2 - e1 - n1 - n2 - e2 - n3
			case CASE_3:
				
				// n1 
				n1Query.append("ASK WHERE { { ").append(n1).append(" ?p ?n . } UNION { ?s ?p1 ");
				n1Query.append(n1).append(" .MINUS { ?a ?b ").append(n1).append(" . FILTER (?s = ?a && ").append(n2).append(" =?a && ?b=?p1 && ?b=");
				n1Query.append(e1).append(") } } }");
				
				// n2
				n2Query.append("ASK WHERE { {").append(n2).append(" ?p ?n . MINUS {")
					.append(n2).append(" ?q ?m . FILTER (?n = ?m && ").append(n1).append(" =?m && ?b=?p && ?q=").append(e1)
					.append(") FILTER (?n = ?m && ").append(n3).append(" =?m && ?b=?p && ?q=").append(e2).append(") } } UNION { ?s ?p1 ")
					.append(n2).append(".} }");
				
				// n3
				n3Query.append("ASK WHERE { { ") 
						.append(n3).append(" ?p ?n . } UNION {  ?s ?p1 ").append(n3).append(" . MINUS { ?a ?b ")
						.append(n3).append(" . FILTER (?s = ?a && ").append(n2).append(" =?a && ?b=?p1 && ?b=").append(e2).append(") } } }");		
				break;

			default:
				break;
		}
		
		Map<String, RDFNode> map = new HashMap<String, RDFNode>();
		map.put(n1Query.toString(), nodes.get(0));
		map.put(n2Query.toString(), nodes.get(1));
		map.put(n3Query.toString(), nodes.get(2));
		
		for(String curQuery: map.keySet()) {
			QueryExecution queryExecution = queryModel(graph, curQuery);
			
			boolean isExternal = askModel(queryExecution);
			if(isExternal) {
				externals.add(map.get(curQuery));
			}
		}
		
		return externals;
	}
	
	/**
	 * Returns a string encapsulated with < > in case of an URI resource and " " in case of a literal
	 * @param node
	 * @return
	 */
	private static String getNodeName(RDFNode node) {
		String nodeStr = "";
		if(node.isURIResource()) {
			nodeStr = new StringBuilder("<").append(node.toString()).append(">").toString();
		} else {
			nodeStr = new StringBuilder("\"").append(node.toString()).append("\"").toString();
		}
		return nodeStr;
	}
	
	/**
	 * executes a sparql query for a given model
	 * @param graph
	 * @param sparqlQuery
	 * @return
	 */
	public static QueryExecution queryModel(Model graph, String sparqlQuery) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, graph);
		return queryExecution;
	}
	
	public static List<QuerySolution> selectModel (QueryExecution queryExecution) {
		List<QuerySolution> querySolutionList = new ArrayList<QuerySolution>();
	    ResultSet resultSet = queryExecution.execSelect();
	    while(resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();	
		return querySolutionList;
	}
	
	public static boolean askModel(QueryExecution queryExecution) {
		return queryExecution.execAsk();
	}
	
	/**
	 * Updates the occurrences count for each digram
	 * @param diOccurMap
	 */
	private static void updateDigramCount(Map<Digram, Set<DigramOccurence>> diOccurMap) {
		diOccurMap.forEach((digram,occurrences)->{
			digram.setNoOfOccurences(occurrences.size());
		});
	}
	
	/**
	 * 
	 * @param occurrences
	 * @return
	 */
	public static Map<Digram, Set<DigramOccurence>> mapDigrams(Set<DigramOccurence> occurrences) {
		Map<Digram, Set<DigramOccurence>> digramMap = new HashMap<Digram, Set<DigramOccurence>>();
		for(DigramOccurence occurrence : occurrences) {
			Digram curDigram = occurrence.getDigram();
			if(digramMap.containsKey(curDigram)) {
				digramMap.get(curDigram).add(occurrence);
			} else {
				Set<DigramOccurence> newOccur = new HashSet<DigramOccurence>();
				newOccur.add(occurrence);
				digramMap.put(curDigram, newOccur);
			}
		}
		updateDigramCount(digramMap);
		return digramMap;
	}
	
	/**
	 * 
	 * @param occurrences
	 * @return
	 */
	public static Set<Digram> getDigrams (Map<Digram, Set<DigramOccurence>> map){
		return map.keySet();
	}


	/**
	 * Sort the digrams by most to least frequent
	 * @param digrams
	 * @return
	 */
	public static List<Digram> sortDigrambyFrequence(Set<Digram> digrams) {
		List<Digram> sortedDigrams = new ArrayList<Digram>();
		sortedDigrams.addAll(digrams);
		sortedDigrams.sort(new Comparator<Digram>() {
			@Override
			public int compare(Digram dig0, Digram dig1) {
				if(dig0.getNoOfOccurences() > dig1.getNoOfOccurences()) {
					return -1;
				}
				if(dig0.getNoOfOccurences() < dig1.getNoOfOccurences()) {
					return 1;
				}
				return 0;
			}
		});
		return sortedDigrams;
	}
	
}
