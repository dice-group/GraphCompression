package org.dice_group.grp.grammar.digram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
		        RDFNode n2 = solution.get("n2");
		        RDFNode n3 = solution.get("n3");
		        Property e1 = ResourceFactory.createProperty(solution.get("e1").toString());
		        Property e2 = ResourceFactory.createProperty(solution.get("e2").toString());
		 
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
				
				if(stmt1!= null && stmt2 != null && !stmt1.equals(stmt2)) {
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
					// and a maximum of 2 external nodes 
			        if(!externals.isEmpty() && externals.size()<3) {
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
		RDFNode n1 = nodes.get(0);
		RDFNode n2 = nodes.get(1);
		RDFNode n3 = nodes.get(2);
		
		boolean isN1External = isNodeExternal(getNodeName(n1), 1, graph);
		boolean isN2External = isNodeExternal(getNodeName(n2), 2, graph);
		boolean isN3External = isNodeExternal(getNodeName(n3), 1, graph);
		
		if(isN1External)
			externals.add(n1);
		
		if(isN2External)
			externals.add(n2);
		
		if(isN3External)
			externals.add(n3);
		
		return externals;
	}
	
	/**
	 * Returns true if the triple count for a given node is positive
	 * @param nodeString
	 * @param diff
	 * @param graph
	 * @return
	 */
	private static boolean isNodeExternal(String nodeString, int diff, Model graph) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT (COUNT(*)-").append(diff).append(" AS ?total) WHERE { { ")
		.append(nodeString).append(" ?p ?n . } UNION { ?s ?p1 ").append(nodeString).append(" . } }");	
		
		QueryExecution queryExecution = queryModel(graph, query.toString());
		List<QuerySolution> results = selectModel(queryExecution);
		for(QuerySolution solution: results) {
			long count = solution.getLiteral("total").getLong();
			if(count > 0) {
				return true;
			}
		}
		return false;
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
		} else if(node.isLiteral()) {
			nodeStr = new StringBuilder("\"").append(node.toString()).append("\"").toString();
		} else {
			return node.toString();
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
