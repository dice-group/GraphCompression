package org.dice_group.grp.grammar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;

public class Grammar {


	private Model start;
	private Map<String, Digram> rules = new HashMap<String, Digram>();
	private Map<Digram, List<DigramOccurence>> replaced = new HashMap<Digram, List<DigramOccurence>>();

	public Grammar(Model start) {
		this.start = start;
	}
	
	public void setStart(Model start) {
		this.start =start;
	}
	
	
	public Grammar(Map<String, Digram> rules) {
		this.rules = rules;
	}
	
	public Map<String, Digram> getRules() {
		return rules;
	}

	public void setRules(Map<String, Digram> rules) {
		this.rules = rules;
	}
	
	public Digram getGraph(String nonTerminal) {
		return this.rules.get(nonTerminal);
	}
	
	public Model getStart() {
		return this.start;
	}

	public void addRule(String lhs,  Digram rhs) {
		this.rules.put(lhs, rhs);
	}
	
	
	public Set<String> getNonTerminals(){
		return rules.keySet();
	}

	public Map<Digram, List<DigramOccurence>> getReplaced() {
		return replaced;
	}

	public void setReplaced(Map<Digram, List<DigramOccurence>> replaced) {
		this.replaced = replaced;
	}

	

	
}
