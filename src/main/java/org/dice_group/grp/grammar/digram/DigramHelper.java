package org.dice_group.grp.grammar.digram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.util.RDFHelper;

public class DigramHelper {
	private static final String CASE_1 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where { ?n1 ?e1 ?n2 . ?n2 ?e2 ?n3 . } ";
	private static final String CASE_2 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where { ?n1 ?e1 ?n2 . ?n3 ?e2 ?n2 . } ";
	private static final String CASE_3 = "select ?n1 ?n2 ?n3 ?e1 ?e2 where { ?n2 ?e1 ?n1 . ?n2 ?e2 ?n3 . } ";
	
	private static final String [] CASES = {
		CASE_1, 
		CASE_2, 
		CASE_3	
	};

	protected static Set<Integer> getExternalIndexes(Statement e1, Statement e2, List<RDFNode> externals) {
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
	 * Searches the model for ALL occurrences, based on three main patterns:
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
		
		Set<DigramOccurence> occurrences = new HashSet<DigramOccurence>();
		
		for(int i = 0; i< CASES.length; i++) {
			QueryExecution queryExec = RDFHelper.queryModel(graph, CASES[i]);
			List<QuerySolution>  results = RDFHelper.selectModel(queryExec);
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
					
					List<RDFNode> externals = findExternals(nodes, stmt1, stmt2, graph, CASES[i]);

			        // it's only an occurrence if it has at least one external node and a maximum of 2 external nodes 
			        if(!externals.isEmpty() && externals.size()<3) {
			        	DigramOccurence occurrence = new DigramOccurence(stmt1, stmt2, externals);
			        	occurrences.add(occurrence);			        	
			        }
				}
		    }
		}
		return occurrences;
	}
	
	/**
	 * Non overlapping occurrences, priority given to the most frequent digrams
	 * @param occurrences
	 * @return
	 */
	public static Map<Digram, Set<DigramOccurence>> findNonOverOccurrences(Set <DigramOccurence> occurrences){
		Map<Digram, Set<DigramOccurence>> nonOverlMap = new HashMap<Digram, Set<DigramOccurence>>();
		Map<Digram, Set<DigramOccurence>> map = DigramHelper.mapDigrams(occurrences);
		List<Digram> sortedDigrams = DigramHelper.sortDigrambyFrequence(map.keySet());
		
		Set<RDFNode> visitedNodes = new HashSet<RDFNode>();
		
		for(Digram curDigram: sortedDigrams) {
			Set<DigramOccurence> digramOccurences = map.getOrDefault(curDigram, new HashSet<DigramOccurence>());
			for(DigramOccurence curOccurrence: digramOccurences) {
				Set <RDFNode> nodes = curOccurrence.getNodes();

				// adds the occurrence if there's no nodes in common
				if(Collections.disjoint(visitedNodes, nodes)) {
					if(nonOverlMap.get(curDigram) != null)
						nonOverlMap.get(curDigram).add(curOccurrence);
					else {
						Set<DigramOccurence> tempOccur = new HashSet<DigramOccurence>();
						tempOccur.add(curOccurrence);
						nonOverlMap.put(curDigram, tempOccur);
					}
				}
				
				// if empty, add the first occurrence anyways
				if(nonOverlMap.isEmpty()) {
					Set<DigramOccurence> tempOccur = new HashSet<DigramOccurence>();
					tempOccur.add(curOccurrence);
					nonOverlMap.put(curDigram, tempOccur);
				}
				visitedNodes.addAll(nodes);
			}
		}
		updateDigramCount(nonOverlMap);
		return nonOverlMap;
	}
	
	/**
	 * Checks which of the nodes of both statements, are connected to the overall graph
	 * @param stmt1
	 * @param stmt2
	 * @param graph
	 * @param digramCase 
	 * @return the set of external nodes
	 */
	public static List<RDFNode> findExternals(List<RDFNode> nodes, Statement stmt1, Statement stmt2, Model graph, String digramCase){
		List<RDFNode> externals = new LinkedList<RDFNode>();
		for(RDFNode node: nodes) {
			int diff = 1;
			if(node.equals(nodes.get(1))) {
				diff = 2;
			} 
			boolean isNodeExternal = isNodeExternal(RDFHelper.formatNode(node), diff, graph);
			
			if(isNodeExternal)
				externals.add(node);
		}
		
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
		
		QueryExecution queryExecution = RDFHelper.queryModel(graph, query.toString());
		List<QuerySolution> results = RDFHelper.selectModel(queryExecution);
		for(QuerySolution solution: results) {
			long count = solution.getLiteral("total").getLong();
			if(count > 0) {
				return true;
			}
		}
		return false;
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
	 * Maps all occurrences to its corresponding Digrams
	 * @param occurrences
	 * @return
	 */
	public static Map<Digram, Set<DigramOccurence>> mapDigrams(Set<DigramOccurence> occurrences) {
		Map<Digram, Set<DigramOccurence>> digramMap = new HashMap<Digram, Set<DigramOccurence>>();
		for(DigramOccurence occurrence : occurrences) {
			Digram curDigram = new Digram(occurrence.getEdgeLabel1(), occurrence.getEdgeLabel2(), occurrence.getExternalIndexes());
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
