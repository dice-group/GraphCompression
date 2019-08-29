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
import org.apache.jena.rdf.model.Resource;
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

	public static Set<Integer> getExternalIndexes(Statement e1, Statement e2, List<RDFNode> externals) {
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
	 * 
	 * @param allOccurrences
	 * @param graph
	 */
	public static void updateExternals(Set<DigramOccurence> allOccurrences, Model graph) {
		// update the externals ?
		allOccurrences.forEach(occurrence->{
			List<RDFNode> nodes = occurrence.getNodes();
			Set<RDFNode> externals = new HashSet<RDFNode>(DigramHelper.findExternals(nodes, null, graph));
			List<RDFNode> externalList = new LinkedList<RDFNode>(externals);
			occurrence.setExternals(externalList);
			occurrence.setExternalIndexes(DigramHelper.getExternalIndexes(occurrence.getEdge1(), 
					occurrence.getEdge2(), 
					externalList));
		});
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
	 * Finds the digram occurrences revolving around a specific statement in a given model
	 * @param graph
	 * @param newStmt
	 * @return
	 */
	public static Set<DigramOccurence> findStmtBasedDigrams(Model graph, Statement newStmt) {
		Set<DigramOccurence> occurrences = new HashSet<DigramOccurence>();
		Resource n1 = newStmt.getSubject();
		Property e1 = newStmt.getPredicate();
		RDFNode n2 = newStmt.getObject();
		
		StringBuilder spec_case_1 = new StringBuilder("select * where { ");
		spec_case_1.append(RDFHelper.formatNode(n1)).append(" ")
				.append(RDFHelper.formatNode(e1)).append(" ")
				.append(RDFHelper.formatNode(n2)).append(". ")
				.append(RDFHelper.formatNode(n2))
				.append(" ?e2 ?n3 . } ");
		
		StringBuilder spec_case_2 = new StringBuilder("select * where {");
		spec_case_2.append(RDFHelper.formatNode(n1)).append(" ")
		.append(RDFHelper.formatNode(e1)).append(" ")
		.append(RDFHelper.formatNode(n2)).append(" ")
		.append(". ?n3 ?e2 ").append(RDFHelper.formatNode(n2)).append(". } ");
		
		StringBuilder spec_case_3 = new StringBuilder("select * where {");
		spec_case_3.append(RDFHelper.formatNode(n2)).append(" ")
		.append(RDFHelper.formatNode(e1)).append(" ")
		.append(RDFHelper.formatNode(n1)).append(" ")
		.append(". ").append(RDFHelper.formatNode(n2)).append(" ?e2 ?n3 . } ");
		
		String [] spec_cases = {
				spec_case_1.toString(),
				spec_case_2.toString(),
				spec_case_3.toString()
		};
		
		for (String curCase: spec_cases) {
			QueryExecution queryExecution = RDFHelper.queryModel(graph, curCase);
			List<QuerySolution> results = RDFHelper.selectModel(queryExecution);
			for(QuerySolution solution: results) {
				Statement secStmt = null;
				RDFNode n3 = solution.get("n3");
				Property e2 = ResourceFactory.createProperty(solution.get("e2").toString());
				if(curCase.equals(spec_case_1.toString()) && n2.isResource()){
					secStmt = ResourceFactory.createStatement(n2.asResource(), e2, n3);
				} else if(curCase.equals(spec_case_2.toString())){
					secStmt = ResourceFactory.createStatement(n3.asResource(), e2, n2);
				} else if(curCase.equals(spec_case_3.toString())){
					secStmt = ResourceFactory.createStatement(n1, e2, n3);
				}
				
				if(!newStmt.equals(secStmt)) {
					List<RDFNode> nodes = new ArrayList<RDFNode>();
					nodes.add(n1);
					nodes.add(n2);
					nodes.add(n2);
					nodes.add(n3);
					Set<RDFNode> externals = new HashSet<RDFNode>(findExternals(nodes, null, graph));
					
					DigramOccurence occurrence = new DigramOccurence(newStmt, secStmt, new LinkedList<RDFNode>(externals));
					if(!externals.isEmpty() && externals.size()<3) {
						 occurrences.add(occurrence);
					}
				}
			}
		}
		return occurrences;
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
					nodes.add(n2);
					nodes.add(n3);
//					nodes.add(e1);
//					nodes.add(e2);
					
					Set<RDFNode> externals = new HashSet<RDFNode>(findExternals(nodes, n2, graph));

			        // it's only an occurrence if it has at least one external node and a maximum of 2 external nodes 
			        if(!externals.isEmpty() && externals.size()<3) {
			        	DigramOccurence occurrence = new DigramOccurence(stmt1, stmt2, new LinkedList<RDFNode>(externals));
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
				List <RDFNode> nodes = curOccurrence.getNodes();

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
	 * @return the set of external nodes
	 */
	public static List<RDFNode> findExternals(List<RDFNode> nodes, RDFNode commonNode, Model graph){
		List<RDFNode> externals = new LinkedList<RDFNode>();
		// by default n2 is the common node
		if(commonNode == null)
			commonNode = nodes.get(1);
		
		for(RDFNode node: new HashSet<RDFNode>(nodes)) {
			int diff = 1;
			if(node.equals(commonNode)) {
				diff++;
			} 
			if (nodes.get(0).equals(nodes.get(1))) {
				diff++;
			}
			if (nodes.get(2).equals(nodes.get(3))) {
				diff++;
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
	public static void updateDigramCount(Map<Digram, Set<DigramOccurence>> diOccurMap) {
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
	
	/**
	 * Adds the new entries to the original map
	 * @param originalMap
	 * @param newEntries
	 */
	public static void mergeMaps (Map<Digram, Set<DigramOccurence>> originalMap, Map<Digram, Set<DigramOccurence>> newEntries){
		newEntries.forEach((digram, occurrences)->{
			if(originalMap.containsKey(digram)) {
				originalMap.get(digram).addAll(occurrences);
			} else {
				originalMap.put(digram, occurrences);
			}
		});
	}

}
