package org.dice_group.grp.grammar;

import java.util.Set;
import java.util.SortedSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * 
 * A basic non terminal Rule of the GRP algorithm
 * 
 * @author minimal
 *
 */
public class Rule {

	private String lhs;
	private Model rhs;
	private SortedSet<RDFNode> external;
	
	/**
	 * create a Rule with a given nonTerminal called left hand side of the rule
	 * and the corresponding graph called right hand side of the rule
	 * 
	 * @param nonTerminal 
	 * @param graph
	 * @param exeternalNodes
	 */
	public Rule(String nonTerminal, Model graph, SortedSet<RDFNode> externalNodes) {
		this.setLhs(nonTerminal);
		this.setRhs(graph);
		this.external = externalNodes;
	}

	public String getLhs() {
		return lhs;
	}

	public void setLhs(String lhs) {
		this.lhs = lhs;
	}

	public Model getRhs() {
		return rhs;
	}

	public void setRhs(Model rhs) {
		this.rhs = rhs;
	}
	
	public String getNonTerminal() {
		return lhs;
	}

	public void setNonTerminal(String lhs) {
		this.lhs = lhs;
	}

	public Model getGraph() {
		return rhs;
	}

	public void setGraph(Model rhs) {
		this.rhs = rhs;
	}
	
	public boolean updateRule(Rule other) {
		//TODO check if other.getRhs is in rhs 
		//TODO if yes replace with other.getLhs 
		return false;
	}

	public Set<RDFNode> getExternal() {
		return external;
	}

	public void setExternal(SortedSet<RDFNode> external) {
		this.external = external;
	}
	
	
}
