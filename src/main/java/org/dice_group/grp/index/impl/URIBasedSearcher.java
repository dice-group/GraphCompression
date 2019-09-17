package org.dice_group.grp.index.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Searcher;
import org.dice_group.grp.util.COMMON;
import org.dice_group.grp.util.GraphUtils;
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
			// replace internals
			List<RDFNode> internals = new LinkedList<RDFNode>();
 			for(RDFNode n : occ.getInternals()) {
				internals.add(ResourceFactory.createResource(getNodeFromID(GraphUtils.getRDFIndex(n), TripleComponentRole.OBJECT).toString()));
			}
 			occ.getInternals().clear();
 			occ.getInternals().addAll(internals);
			// replace externals
 			List<RDFNode> externals = new LinkedList<RDFNode>();
 			for(RDFNode n : occ.getExternals()) {
 				externals.add(ResourceFactory.createResource(getNodeFromID(GraphUtils.getRDFIndex(n), TripleComponentRole.SUBJECT).toString()));
			}
 			occ.setExternals(externals);
			// replace the nodes in the Statements which are not placeholder :placeholder
 			int noOfExternals1=countExternals(occ.getEdge1());
 			int noOfExternals2=countExternals(occ.getEdge2());
 			occ.setEdge1(deindexStatement(occ.getEdge1(), (Property)occ.getEdgeLabel1(), getExternalsForEdge(externals, 0, noOfExternals1)));
 			occ.setEdge1(deindexStatement(occ.getEdge2(), (Property)occ.getEdgeLabel2(), getExternalsForEdge(externals, noOfExternals1, noOfExternals2)));

		}
		return digram;
	}

	private int countExternals(Statement edge) {
		int count=0;
		count+=edge.getSubject().toString().startsWith(COMMON.OCCURENCE_PLACEHOLDER_URI)?1:0;
		count+=edge.getObject().toString().startsWith(COMMON.OCCURENCE_PLACEHOLDER_URI)?1:0;
		return count;
	}

	private List<RDFNode> getExternalsForEdge(List<RDFNode> externals, int offset, int size) {
		//decompress externalIndexes already done
		List<RDFNode> ret = new LinkedList<RDFNode>();
		for(int i = offset; i<size; i++) {
			ret.add(externals.get(i));
		}
		return ret;
	}

	private Statement deindexStatement(Statement stmt, Property edge, List<RDFNode> externals) {
		Resource subject = stmt.getSubject();
		int i=0;
		//1. check if :placeholder then external, otherwise internal
		if(!subject.toString().startsWith(COMMON.OCCURENCE_PLACEHOLDER_URI)) {
			//internal
			subject = ResourceFactory.createResource(getNodeFromID(GraphUtils.getRDFIndex(subject), TripleComponentRole.OBJECT).toString());
		}
		else {
			//external
			subject = ResourceFactory.createResource(externals.get(i++).toString());
		}
		Property predicate = edge;
		RDFNode object = stmt.getObject();
		if(!object.toString().startsWith(COMMON.OCCURENCE_PLACEHOLDER_URI)) {
			//internal
			object = ResourceFactory.createResource(getNodeFromID(GraphUtils.getRDFIndex(subject), TripleComponentRole.OBJECT).toString());
		}
		else {
			//external
			object = ResourceFactory.createResource(externals.get(i++).toString());
		}
		return new StatementImpl(subject, predicate, object);
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

	public Map<Digram, List<List<RDFNode>>> deindexInternalMap(Map<Digram, List<Integer[]>> internalMap) {
		Map<Digram, List<List<RDFNode>>> realMap = new HashMap<Digram, List<List<RDFNode>>>();
		for(Digram key : internalMap.keySet()) {
			List<List<RDFNode>> realInternals = new LinkedList<List<RDFNode>>();
			for(Integer[] internals : internalMap.get(key)) {
				List<RDFNode> occInternals = new LinkedList<RDFNode>();
				for(Integer indexed : internals) {
					occInternals.add(ResourceFactory.createResource(nodeDict.getNode(indexed, TripleComponentRole.OBJECT).toString()));
				}
				realInternals.add(occInternals);
			}
			realMap.put(key, realInternals);
		}
		return realMap;
	}

}
