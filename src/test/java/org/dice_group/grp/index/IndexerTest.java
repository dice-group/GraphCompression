package org.dice_group.grp.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.index.impl.URIBasedIndexer;
import org.junit.Test;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.options.HDTSpecification;

public class IndexerTest {

	private Model createGraph() {
		Model graph = ModelFactory.createDefaultModel();
		
		graph.add(ResourceFactory.createResource("http://exa.com/s/S1"), ResourceFactory.createProperty("http://exa.com/p/P1"), 
				ResourceFactory.createResource("http://exa.com/o/O1"));
		graph.add(ResourceFactory.createResource("http://exa.com/o/O1"), ResourceFactory.createProperty("http://exa.com/p/P2"), 
				ResourceFactory.createPlainLiteral("test"));
		graph.add(ResourceFactory.createResource("http://exa.com/s/S1"), ResourceFactory.createProperty("http://exa.com/p/P2"), 
				ResourceFactory.createPlainLiteral("test"));
		graph.add(ResourceFactory.createResource("http://exa.com/o/O1"), ResourceFactory.createProperty("http://exa.com/p/P1"), 
				ResourceFactory.createPlainLiteral("abc"));
		return graph;
	}
	
	@Test
	public void indexedGraphTest() {
		Model graph = createGraph();
		Model graphCopy = ModelFactory.createDefaultModel();
		//copy the graph for assertion later 
		graphCopy.add(graph);
		
		System.out.println("Original Graph: ");
		System.out.println(graph);
		
		//Create HDT Dict
		TempDictionary dict = DictionaryFactory.createTempDictionary(new HDTSpecification());

		//Create indexer and index graph
		Indexer index = new URIBasedIndexer(dict);
		Model indexed = index.indexGraph(graph);

		System.out.println("#########################\nIndexed Graph:");
		System.out.println(indexed);

		Set<String> nodes = getNodesOfGraph(indexed);
		Map<String, Node> nodeToOrig = expandNodes(nodes, index);
		//expand triples in indexed
		indexed = expandGraph(nodeToOrig, indexed);
		System.out.println("#################\nExpanded:\n");
		System.out.println(indexed);
		System.out.println(graphCopy);

		//check if indexed graph without the original graph is empty
		assertEquals(0, indexed.difference(graphCopy).size());
		//check if they are isomorphic 
		assertTrue(indexed.isIsomorphicWith(graphCopy));
		// both tests true -> same Size and same Statements -> same graph
	}

	private Model expandGraph(Map<String, Node> nodeToOrig, Model indexed) {
		for(Statement stmt : indexed.listStatements().toList()) {
			Triple t = new Triple(nodeToOrig.get(stmt.getSubject().getURI()),
					nodeToOrig.get(stmt.getPredicate().getURI()),
					nodeToOrig.get(stmt.getObject().toString()));
				
					
			indexed.getGraph().add(t);
			
			indexed.remove(stmt);
		}
		return indexed;
	}

	private Map<String, Node> expandNodes(Set<String> nodes, Indexer index) {
		Map<String, Node> nodeToOrig = new HashMap<String, Node>();
		for(String s : nodes) {
			char pre = s.charAt(1);
			TripleComponentRole role=null;
			if(pre=='s') {
				role = TripleComponentRole.SUBJECT;
			}
			if(pre=='p') {
				role = TripleComponentRole.PREDICATE;
			}
			if(pre=='o') {
				role = TripleComponentRole.OBJECT;
			}
			Node n = index.getNodeFromID(s, role);
			nodeToOrig.put(s, n);
			System.out.println("Node "+s+" ---> "+n);
		}
		return nodeToOrig;
	}

	private Set<String> getNodesOfGraph(Model indexed) {

		Set<String> nodes = new HashSet<String>();
		for(Statement stmt : indexed.listStatements().toSet()){
			nodes.add(stmt.getSubject().toString());
			nodes.add(stmt.getObject().toString());
			nodes.add(stmt.getPredicate().toString());
		}
		return nodes;
	}
	
}
