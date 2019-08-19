package org.dice_group.grp.index.impl;

import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.grammar.Grammar;
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
	public static final String OBJECT_PREFIX = ":o";
	public static final String PROPERTY_PREFIX = ":p";
	
	private TempDictionary tmpDict;
	private NodeDictionary nodeDict; 

	public URIBasedIndexer(TempDictionary tmpDict) {
		this.tmpDict = tmpDict;
		
		
	}

	
	@Override
	public Model indexGraph(Model graph) {
		Model indexedGraph = ModelFactory.createDefaultModel();
		List<Statement> stmts = graph.listStatements().toList();
		
		for(Statement stmt : stmts) {
			tmpDict.insert(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.OBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);
		}
		DictionaryPrivate dict = DictionaryFactory.createDictionary(new HDTSpecification());
		ProgressListener listener = new ProgressOut();
		tmpDict.reorganize();
		dict.load(tmpDict, listener);
		for(Statement stmt : stmts) {
			//TODO does not work like that
			String s = SUBJECT_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			String o = OBJECT_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.OBJECT);
			String p = PROPERTY_PREFIX+dict.stringToId(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);
			indexedGraph.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),
					ResourceFactory.createResource(o));
			graph.remove(stmt);
		}
		this.nodeDict = new NodeDictionary(dict);
		return indexedGraph;
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
		for(String key : grammar.getRules().keySet()) {
			Model graph = grammar.getRules().get(key);
			graph = this.indexGraph(graph);
			//overwrite old graph with indexed graph
			grammar.getRules().put(key, graph);
		}
		return grammar;
	}

}
