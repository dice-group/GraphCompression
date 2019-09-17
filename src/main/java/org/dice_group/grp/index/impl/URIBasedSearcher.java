package org.dice_group.grp.index.impl;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Searcher;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

public class URIBasedSearcher implements Searcher{

	private DictionaryPrivate dict;
	private NodeDictionary nodeDict;

	public URIBasedSearcher(DictionaryPrivate dict) {
		this.dict = dict;
		this.nodeDict = new NodeDictionary(dict);
	}
	
	@Override
	public Model deindexGraph(Model indexed) {
		Model graph = ModelFactory.createDefaultModel();
		List<Statement> stmts = indexed.listStatements().toList();

		for(Statement stmt : stmts) {
			String s = getNodeFromID(stmt.getSubject().toString(), TripleComponentRole.SUBJECT).toString();
			String p = stmt.getPredicate().toString();
			if(!p.startsWith(GrammarHelper.NON_TERMINAL_PREFIX))
				p = getNodeFromID(p, TripleComponentRole.PREDICATE).toString();
			String o = getNodeFromID(stmt.getObject().toString(), TripleComponentRole.SUBJECT).toString();
			graph.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),
					ResourceFactory.createResource(o));
			indexed.remove(stmt);
		}
		return graph;
	}
	
	@Override
	public Grammar deindexGrammar(Grammar grammar) {
		grammar.setStart(deindexGraph(grammar.getStart()));
		for(String key : grammar.getRules().keySet()) {
			grammar.getRules().put(key, deindexRule(grammar.getRules().get(key), grammar));
		}
		return grammar;
	}

	private Digram deindexRule(Digram digram, Grammar g) {
		digram.setEdgeLabel1(ResourceFactory.createProperty(getNodeFromID(digram.getEdgeLabel1().toString().replace(":", ""), TripleComponentRole.PREDICATE).toString()));
		digram.setEdgeLabel2(ResourceFactory.createProperty(getNodeFromID(digram.getEdgeLabel2().toString().replace(":", ""), TripleComponentRole.PREDICATE).toString()));
		// occurences
		for(DigramOccurence occ : g.getReplaced().get(digram)) {
			//TODO replace internals
			//TODO replace externals 
			//TODO replace the nodes in the Statements which are not placeholder :placeholder
		}
		return digram;
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
	public void setDict(DictionaryPrivate dict) {
		this.dict = dict;
		this.nodeDict = new NodeDictionary(dict);
	}

}
