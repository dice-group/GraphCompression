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

	@Test
	public void indexedGraphTest() {
		Model graph = ModelFactory.createDefaultModel();
		
		graph.add(ResourceFactory.createResource("http://exa.com/s/S1"), ResourceFactory.createProperty("http://exa.com/p/P1"), 
				ResourceFactory.createResource("http://exa.com/o/O1"));
		graph.add(ResourceFactory.createResource("http://exa.com/o/O1"), ResourceFactory.createProperty("http://exa.com/p/P2"), 
				ResourceFactory.createPlainLiteral("test"));
		graph.add(ResourceFactory.createResource("http://exa.com/s/S1"), ResourceFactory.createProperty("http://exa.com/p/P2"), 
				ResourceFactory.createPlainLiteral("test"));
		graph.add(ResourceFactory.createResource("http://exa.com/o/O1"), ResourceFactory.createProperty("http://exa.com/p/P1"), 
				ResourceFactory.createPlainLiteral("abc"));
		Model graphCopy = ModelFactory.createDefaultModel();
		graphCopy.add(graph);
		System.out.println("Original Graph: ");
		System.out.println(graph);
		TempDictionary dict = DictionaryFactory.createTempDictionary(new HDTSpecification());

		Indexer index = new URIBasedIndexer(dict);
		Model indexed = index.indexGraph(graph);

		System.out.println("#########################\nIndexed Graph:");
		System.out.println(indexed);
		/*
		 * S1 - P1 -> O1
		 * O1 - P2 -> test
		 * S1 - P2 -> test
		 * O1 - P1 -> abc
		 * 
		 * index:
		 * 
		 * :s1, :p2, :o2
		 * :s1, :p1, :o1
		 * :s2, :p1, :o1
		 * :s2, :p2, :o3
		 * 
		 */
		//TODO check if this checkouts somehow (SPOILER it does)
		Set<String> nodes = new HashSet<String>();
		for(Statement stmt : indexed.listStatements().toSet()){
			nodes.add(stmt.getSubject().toString());
			nodes.add(stmt.getObject().toString());
			nodes.add(stmt.getPredicate().toString());
		}
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
		//expand triples in indexed
		for(Statement stmt : indexed.listStatements().toList()) {
			Triple t = new Triple(nodeToOrig.get(stmt.getSubject().getURI()),
					nodeToOrig.get(stmt.getPredicate().getURI()),
					nodeToOrig.get(stmt.getObject().toString()));
				
					
			indexed.getGraph().add(t);
			
			indexed.remove(stmt);
		}
		System.out.println("#################\nExpanded:\n");
		System.out.println(indexed);
		System.out.println(graphCopy);

		assertEquals(0, indexed.difference(graphCopy).size());
		assertTrue(indexed.isIsomorphicWith(graphCopy));
	}
	
}
