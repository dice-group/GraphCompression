package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;

/**
 * Helper class for general RDF related utilities functions
 *
 */
public class RDFHelper {
	
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
	public static List<QuerySolution> selectModel (QueryExecution queryExecution) {
		List<QuerySolution> querySolutionList = new ArrayList<QuerySolution>();
	    ResultSet resultSet = queryExecution.execSelect();
	    while(resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();	
		return querySolutionList;
	}
}
