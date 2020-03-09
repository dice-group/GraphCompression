package org.dice_group.grp.grammar;

import java.util.*;

import grph.Grph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.util.BoundedList;

public class Grammar {


	private Grph start;


	private BoundedList props;
	private Map<Integer, Digram> rules = new HashMap<Integer, Digram>();
	private Map<Digram, List<DigramOccurence>> replaced = new HashMap<Digram, List<DigramOccurence>>();
	private List<Node> soIndex;


	private List<Statement> stmts =new ArrayList<Statement>();
	private int vSize=0;

	public Grammar(Grph start) {
		this.start = start;
		vSize=start.getVertices().size();
	}
	
	public void setStart(Grph start) {
		this.start =start;
	}
	
	
	public Grammar(Map<Integer, Digram> rules) {
		this.rules = rules;
	}
	
	public Map<Integer, Digram> getRules() {
		return rules;
	}

	public void setRules(Map<Integer, Digram> rules) {
		this.rules = rules;
	}
	
	public Digram getGraph(String nonTerminal) {
		return this.rules.get(nonTerminal);
	}
	
	public Grph getStart() {
		return this.start;
	}

	public void addRule(Integer lhs,  Digram rhs) {
		this.rules.put(lhs, rhs);
	}
	
	
	public Set<Integer> getNonTerminals(){
		return rules.keySet();
	}

	public Map<Digram, List<DigramOccurence>> getReplaced() {
		return replaced;
	}

	public void setReplaced(Map<Digram, List<DigramOccurence>> replaced) {
		this.replaced = replaced;
	}


	public BoundedList getProps() {
		return props;
	}

	public void setProps(BoundedList props) {
		this.props = props;
	}


    public void setSOIndex(List<Node> soIndex) {
		this.soIndex = soIndex;
    }

	public List<Node> getSOIndex() {
		return this.soIndex;
	}

	public List<Statement> getStmts() {
		return stmts;
	}

	public void setStmts(List<Statement> stmts) {
		this.stmts = stmts;
	}


    public int getVSize() {
		return this.vSize;
    }
}


