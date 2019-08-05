package org.dice_group.grp.grammar.digram;

import java.util.ArrayList;
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
import org.apache.jena.rdf.model.StmtIterator;

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
	
	// TODO while searching for the digrams, we already search for the occurrences ??
	public static Set<Digram> findDigrams(Model graph) {		
		Set<Digram> digrams = new HashSet<Digram>();
		
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
		        	stmt2  = ResourceFactory.createStatement(n2.asResource(), e2, n3);
		        }
		        
				if(CASES[i].equals(CASE_2)) {
					stmt1 = ResourceFactory.createStatement(n1.asResource(), e1, n2);
		        	stmt2  = ResourceFactory.createStatement(n3.asResource(), e2, n2);
				}
				
				if(CASES[i].equals(CASE_3)) {
					stmt1 = ResourceFactory.createStatement(n2.asResource(), e1, n1);
		        	stmt2  = ResourceFactory.createStatement(n2.asResource(), e2, n3);
				}
		        
				if(!stmt1.equals(stmt2)) {
					Set<RDFNode> externals = findExternals(stmt1, stmt2, graph);
			        Digram digram = new Digram(e1, e2, getExternalIndexes(stmt1, stmt2, externals));
			        digrams.add(digram);
				}
		    }
		}
		return digrams;
	}
	
	/**
	 * Checks which of the nodes of both statements, are connected to the overall graph
	 * @param stmt1
	 * @param stmt2
	 * @param graph
	 * @return
	 */
	public static Set<RDFNode> findExternals(Statement stmt1, Statement stmt2, Model graph){
		Set<RDFNode> externals = new HashSet<RDFNode>();
		
		String sparql = "select ?n1 ?e1 ?n2 ?e2 ?n3 where {\n" + 
				"VALUES (?n1) { ( \"" + stmt1.getSubject() + "\" ) "
								+ "( \""+ stmt1.getObject() +"\" ) "
								+ "( \""+ stmt2.getSubject() +"\" ) "
								+ "( \""+ stmt2.getObject()+"\" )}\n" + 
				"{?n1 ?e1 ?n2 .}\n" + 
				"UNION\n" + 
				"{?n3 ?e2 ?n1 .}\n" + 
				"}";
		
		List<QuerySolution> results = queryModel(graph, sparql);
		
		for(QuerySolution solution: results){			
			Statement s1 = null;
			RDFNode curNode = solution.get("n1");
			if(curNode.isResource()) {
				curNode = solution.getResource("n1");
				
				Property e1 = ResourceFactory.createProperty(solution.get("e1").toString());
				s1 = ResourceFactory.createStatement(curNode.asResource(), e1, solution.get("n3"));
			}
			
			Property e2 = ResourceFactory.createProperty(solution.get("e2").toString());
			Statement s2 = ResourceFactory.createStatement(solution.getResource("n3"), e2, curNode);
			
			if( !stmt1.equals(s1) && !stmt1.equals(s2) &&
					!stmt2.equals(s1) && !stmt2.equals(s2)
					
					//(s1 != null && !stmt1.equals(s1) && !stmt1.equals(s2)) && //||
					//(!stmt2.equals(s1) && !stmt2.equals(s2))
					) {
				externals.add(curNode);
			}
			
		}		
		return externals;
	}
	
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
	
	public static Set<DigramOccurence> findDigrams(Model graph, Digram dig) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
// ******* restarting it, to be deleted ********
	
//	/**
//	 * finds all existing digrams in the model by searching for sets of 2 statements 
//	 * that share at least one common node
//	 * @param graph
//	 * @return
//	 */
//	public static Set<Digram> findDigrams(Model graph) {
//		StmtIterator stmtIterator = graph.listStatements();
//		while(stmtIterator.hasNext()) {
//			Statement curStmt = stmtIterator.next();
//			// for method ambiguity problems
//			Property nullProp = null;
//			Resource nullRes = null;
//			
//			//find statements with common nodes
//			Resource curSubject = curStmt.getSubject();
//			StmtIterator possibleDigramsIter = graph.listStatements(nullRes, nullProp, curSubject);
//			StmtIterator finalIter = null;
//			if(possibleDigramsIter != null) {
//				
//			}
//			
//			RDFNode curObject = curStmt.getObject();
//			if(curObject.isResource()) {
//				StmtIterator anotherPossDigramsIter = graph.listStatements(curObject.asResource(), nullProp, nullRes);
//				if(possibleDigramsIter != null) {
//					finalIter = (StmtIterator) Iterators.concat(possibleDigramsIter, anotherPossDigramsIter);
//				}
//			}
//			
//			createDigrams(curStmt, curSubject, finalIter);
//		}		
//		return null;
//	}
//	
//	private static void createDigrams(Statement statement, Resource commonNode, StmtIterator iterator) {
//		Property edgeLabel1 = null;
//		Property edgeLabel2 = null;
//		boolean isSubject = false;
//		
//		Set<RDFNode> externals = new HashSet<RDFNode>();
//		
//		if(statement.getSubject().equals(commonNode)) {
//			isSubject = true;
//		}
//		
//		while(iterator.hasNext()) {
//			Set<Integer> ext = new HashSet<Integer>();
//			Statement curStmt = iterator.next(); 
//			// 
//			if(isSubject) {
//				edgeLabel1 = statement.getPredicate();
//				edgeLabel2 = curStmt.getPredicate();
//				externals.add(statement.getObject());
//				externals.add(curStmt.getSubject());
//			} else {
//				edgeLabel2 = statement.getPredicate();
//				edgeLabel1 = curStmt.getPredicate();
//				externals.add(curStmt.getObject());
//				externals.add(statement.getSubject());
//			}
//			
//			
//			Digram digram = new Digram(edgeLabel1, edgeLabel2, getExternalIndexes(statement, curStmt, externals));
//		}
//	}
//	
//	/**
//	 * Searches for sets of 2 triples that share one common node
//	 * @param graph
//	 * @param digrams
//	 * @return
//	 */
//	public static Set<DigramOccurence> findOccurrences (Model graph, Set<Digram> digrams){
//		StmtIterator stmtIter = graph.listStatements();
//		Set<RDFNode> visitedNodes = new HashSet<RDFNode>();
//		Set<DigramOccurence> occurrences = new HashSet<DigramOccurence>();
//		while(stmtIter.hasNext()) {
//			Statement curStmt = stmtIter.next();
//			Resource subject = curStmt.getSubject();
//			RDFNode object = curStmt.getObject();
//			if(!visitedNodes.contains(subject)) {
//				occurrences.addAll(findOthers(graph, subject, curStmt));
//				visitedNodes.add(subject);
//			}
//				
//			if(!visitedNodes.contains(object)) {
//				occurrences.addAll(findOthers(graph, object, curStmt));
//				visitedNodes.add(object);
//			}
//			
//		}
//		return occurrences;	
//		
//	}
	
}
