package org.dice_group.k2.util;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for general RDF related utilities functions
 *
 */
public class RDFHelper {
	
	private static final Query COUNT_PROPERTIES_QUERY = QueryFactory.create("SELECT (COUNT(DISTINCT ?p) AS ?co) {?s ?p ?o}");
	private static final Query COUNT_SUBJECTS_QUERY = QueryFactory.create("SELECT (COUNT(DISTINCT ?s) AS ?co) {?s ?p ?o}");
	private static final Query COUNT_OBJECTS_QUERY = QueryFactory.create("SELECT (COUNT(DISTINCT ?o) AS ?co) {?s ?p ?o}");

	
	/**
	 * Formats the node as a string, depending on its nature
	 * @param node
	 * @return
	 */
	public static String formatNode(RDFNode node) {
		String nodeStr = "";
		if(node.isURIResource()) {
			nodeStr = new StringBuilder("<").append(node.toString()).append(">").toString();
		} else {
			nodeStr = JenaNodeFormatter.format(node);
		} 
		return nodeStr;
	}
	
	/**
	 * Creates a sparql query for a local model
	 * @param graph
	 * @param sparqlQuery
	 * @return
	 */
	public static QueryExecution queryModel(Model graph, String sparqlQuery) {
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, graph);
		return queryExecution;
	}
	
	/**
	 * Executes a select query
	 * @param queryExecution
	 * @return
	 */
	public static Set<QuerySolution> selectModel (QueryExecution queryExecution) {
		Set<QuerySolution> querySolutionList = new HashSet<QuerySolution>();
	    ResultSet resultSet = queryExecution.execSelect();
	    while(resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();	
		return querySolutionList;
	}
	
	public static Long getPropertyCount(Model model) {
		try(QueryExecution qexec = QueryExecutionFactory.create(COUNT_PROPERTIES_QUERY, model);){
			ResultSet res = qexec.execSelect();
			if(res.hasNext()) {
				RDFNode countNode = res.next().get(res.getResultVars().get(0));
				long count = countNode.asLiteral().getLong();
				return count;
				
			}
		}
		return null;
	}
	
	public static Long getSubjectCount(Model model) {
		try(QueryExecution qexec = QueryExecutionFactory.create(COUNT_SUBJECTS_QUERY, model);){
			ResultSet res = qexec.execSelect();
			if(res.hasNext()) {
				RDFNode countNode = res.next().get(res.getResultVars().get(0));
				long count = countNode.asLiteral().getLong();
				return count;
				
			}
		}
		return null;
	}
	
	public static Long getObjectCount(Model model) {
		try(QueryExecution qexec = QueryExecutionFactory.create(COUNT_OBJECTS_QUERY, model);){
			ResultSet res = qexec.execSelect();
			if(res.hasNext()) {
				RDFNode countNode = res.next().get(res.getResultVars().get(0));
				long count = countNode.asLiteral().getLong();
				return count;
				
			}
		}
		return null;
	}
}
