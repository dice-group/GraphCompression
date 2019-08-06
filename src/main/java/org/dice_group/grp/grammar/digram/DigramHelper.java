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
			List<QuerySolution>  results = queryModel(graph, CASES[i]);
			for(QuerySolution solution: results){
		        
		        RDFNode n1 = solution.get("n1");
		        Property e1 = ResourceFactory.createProperty(solution.get("e1").toString());
		        RDFNode n2 = solution.get("n2");
		       
		        Property e2 = ResourceFactory.createProperty(solution.get("e2").toString());
		 
		        RDFNode n3 = solution.get("n3");
		        
		        Statement stmt1 = null;
		        Statement stmt2 = null;
		        if(CASES[i].equals(CASE_1)) {
		        	stmt1 = ResourceFactory.createStatement(n1.asResource(), e1, n2);
		        	stmt2 = ResourceFactory.createStatement(n2.asResource(), e2, n3);
		        }
		        
				if(CASES[i].equals(CASE_2)) {
					stmt1 = ResourceFactory.createStatement(n1.asResource(), e1, n2);
		        	stmt2 = ResourceFactory.createStatement(n3.asResource(), e2, n2);
				}
				
				if(CASES[i].equals(CASE_3)) {
					stmt1 = ResourceFactory.createStatement(n2.asResource(), e1, n1);
		        	stmt2 = ResourceFactory.createStatement(n2.asResource(), e2, n3);
				}
				
				if(!stmt1.equals(stmt2)) {
					Set<RDFNode> externals = findExternals(stmt1, stmt2, graph);
//					
//			        Digram digram = new Digram(e1, e2, getExternalIndexes(stmt1, stmt2, externals));
//			        digrams.add(digram);
//			        
			        //it's only an occurrence if it has at least one external node
			        if(!externals.isEmpty()) {
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
	 * @return the set of external nodes
	 */
	public static Set<RDFNode> findExternals(Statement stmt1, Statement stmt2, Model graph){
		Set<RDFNode> externals = new HashSet<RDFNode>();
		
		
		StringBuilder stb = new StringBuilder("select ?n1 ?e1 ?n2 ?e2 ?n3 where { ");
		stb.append("VALUES (?n1) { ( ");
		stb.append("<" + stmt1.getSubject() + "> ) ");
		if(stmt1.getObject().isResource()) {
			stb.append("(<" + stmt1.getObject() + "> ) ");
		} else {
			stb.append("(\"" + stmt1.getObject() + "\" ) ");
		}
		stb.append("( <"+ stmt2.getSubject() +"> ) ");
		if(stmt2.getObject().isResource()) {
			stb.append("( <"+ stmt2.getObject()+"> )} ");
		} else {
			stb.append("( \""+ stmt2.getObject()+"\" )} ");
		}
		stb.append("{?n1 ?e1 ?n2 .} UNION {?n3 ?e2 ?n1 .}}");
		
				
		List<QuerySolution> results = queryModel(graph, stb.toString());
		
		for(QuerySolution solution: results){			
			Statement s1 = null;
			RDFNode curNode = solution.get("n1");
			if(curNode.isResource()) {
				curNode = curNode.asResource();
				
				RDFNode n2t = solution.get("n2");
				RDFNode el1 = solution.get("e1");
				if(n2t!=null && el1!=null) {
					Property e1 = ResourceFactory.createProperty(el1.toString());
					s1 = ResourceFactory.createStatement(curNode.asResource(), e1, n2t);
				}
				
			}
			RDFNode el2 = solution.get("e2");
			RDFNode tn3 = solution.get("n3");
			Statement s2 = null;
			if(el2 != null && tn3!= null) {
				Property e2 = ResourceFactory.createProperty(el2.toString());
				s2 = ResourceFactory.createStatement(tn3.asResource(), e2, curNode);
			}
				
			
			if(stmt1.equals(s1) || stmt2.equals(s1) ||stmt1.equals(s2) || stmt2.equals(s2)){
				continue;
			} 
			
			externals.add(curNode);
			
		}		
		return externals;
	}
	
	/**
	 * 
	 * @param graph
	 * @param sparqlQuery
	 * @return
	 */
	public static List<QuerySolution> queryModel(Model graph, String sparqlQuery) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, graph);
		List<QuerySolution> querySolutionList = new ArrayList<QuerySolution>();
	    ResultSet resultSet = queryExecution.execSelect();
	    while(resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();	
		return querySolutionList;
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
	
	
	public static Set<DigramOccurence> findDigrams(Model graph, Digram dig) {
		// TODO Auto-generated method stub
		return null;
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
