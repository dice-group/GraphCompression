package org.dice_group.grp.index.impl;

import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.index.Indexer;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;

/**
 * Uses uris for indexing such that JENA can still use them
 * 
 * @author minimal
 *
 */
public class URIBasedIndexer implements Indexer {

	public static final String SUBJECT_PREFIX = ":s";
	//s and o have to be same
	public static final String OBJECT_PREFIX = ":s";
	public static final String PROPERTY_PREFIX = ":p";
	
	private TempDictionary tmpDict;
	private NodeDictionary nodeDict; 
	private DictionaryPrivate dict;

	public URIBasedIndexer(TempDictionary tmpDict) {
		this.tmpDict = tmpDict;
		
		
	}

	/*
	 * 
	 */
	@Override
	public Model indexGraph(Model graph) {
		Model indexedGraph = ModelFactory.createDefaultModel();
		List<Statement> stmts = graph.listStatements().toList();

		for(Statement stmt : stmts) {
			String s = SUBJECT_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			String o = OBJECT_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.SUBJECT);
			String p = PROPERTY_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);
			indexedGraph.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),
					ResourceFactory.createResource(o));
			graph.remove(stmt);
		}
		this.nodeDict = new NodeDictionary(dict);
		return indexedGraph;
	}
	
	private void tmpIndexGraph(Model graph) {
		List<Statement> stmts = graph.listStatements().toList();
		
		for(Statement stmt : stmts) {
			tmpDict.insert(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.SUBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);
		}
	}
	
	@Override
	public Node getNodeFromID(int id, TripleComponentRole role ) {
		return this.nodeDict.getNode(id, role);
		
	}
	
	@Override
	public Node getNodeFromID(String s, TripleComponentRole role ) {

		int hdtID = Integer.valueOf(s.substring(2));
		return getNodeFromID(hdtID, role);
	}


	@Override
	public Grammar indexGrammar(Grammar grammar) {
		//1. tmpIndex everything
		tmpIndexGraph(grammar.getStart());
		for(String key : grammar.getRules().keySet()) {
			tmpIndexDigrams(grammar.getRules().get(key));
		}
		//2. reorganize
		dict = DictionaryFactory.createDictionary(new HDTSpecification());
		ProgressListener listener = new ProgressOut();
		tmpDict.reorganize();
		dict.load(tmpDict, listener);
		//3. replace
		grammar.setStart(indexGraph(grammar.getStart()));
		for(String key : grammar.getRules().keySet()) {
			Digram digram = grammar.getRules().get(key);
			digram = this.indexDigram(digram);
			//overwrite old graph with indexed graph
			grammar.getRules().put(key, digram);
		}
		return grammar;
	}


	private void tmpIndexDigrams(Digram digram) {
		tmpDict.insert(JenaNodeFormatter.format(digram.getEdgeLabel1()), TripleComponentRole.PREDICATE);
		tmpDict.insert(JenaNodeFormatter.format(digram.getEdgeLabel2()), TripleComponentRole.PREDICATE);
		//TODO whatever we need to do with the internal Nodes. <<< FUUUUCK
	}

	private Digram indexDigram(Digram digram) {
		String el1 = PROPERTY_PREFIX+dict.stringToId(JenaNodeFormatter.format(digram.getEdgeLabel1()), TripleComponentRole.PREDICATE);
		String el2 = PROPERTY_PREFIX+dict.stringToId(JenaNodeFormatter.format(digram.getEdgeLabel2()), TripleComponentRole.PREDICATE);
		digram.setEdgeLabel1(ResourceFactory.createResource(el1));
		digram.setEdgeLabel2(ResourceFactory.createResource(el2));
		return digram;
	}

}
