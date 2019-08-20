package org.dice_group.grp.grammar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.grammar.digram.Digram;

public class Grammar {

	private Map<String, Model> rules = new HashMap<String, Model>();

	public Grammar(Model start) {
		this.rules.put("S", start);
	}
	
	
	public Grammar(Map<String, Model> rules) {
		this.rules = rules;
	}
	
	public Map<String, Model> getRules() {
		return rules;
	}

	public void setRules(Map<String, Model> rules) {
		this.rules = rules;
	}
	
	public Model getGraph(String nonTerminal) {
		return this.rules.get(nonTerminal);
	}
	
	public Model getStart() {
		return this.rules.get("S");
	}

	public void addRule(String lhs,  Digram rhs) {
		//this.rules.put(lhs, rhs);
	}
	
	public void addRule(String lhs,  Model rhs) {
		this.rules.put(lhs, rhs);
	}
	
	public Set<String> getNonTerminals(){
		return rules.keySet();
	}
	
}
