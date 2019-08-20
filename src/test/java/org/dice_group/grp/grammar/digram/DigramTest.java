package org.dice_group.grp.grammar.digram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.ldap.Rdn;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class DigramTest {
	private RDFNode KENTUCKY_LITERAL = ResourceFactory.createPlainLiteral("HardinCounty Kentucky");
	private Property BIRTH_PLACE = ResourceFactory.createProperty("http://dbpedia.org/ontology/birthPlace");
	private Resource LINCOLN = ResourceFactory.createResource("http://dbpedia.org/resource/Abraham_Lincoln");
	private Resource KENTUCKY = ResourceFactory.createResource("http://dbpedia.org/resource/Kentucky");
	private Resource SCHEMA_PLACE = ResourceFactory.createResource("http://schema.org/Place");
	private Resource ONT_PLACE = ResourceFactory.createResource("http://dbpedia.org/ontology/Place");
	private Resource ALABAMA = ResourceFactory.createResource("http://dbpedia.org/resource/Alabama");
	private Resource OBAMA = ResourceFactory.createResource("http://dbpedia.org/resource/Barack_Obama");
	private Resource HAWAII = ResourceFactory.createResource("http://dbpedia.org/resource/Hawaii");
	private Resource CHALCIS = ResourceFactory.createResource("http://dbpedia.org/resource/Chalcis");

	@Test
	public void test() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("test.ttl");
		
		// finds all occurrences
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		
		// possible occurrences, manually observed
		Set<DigramOccurence> possibleOccurrences = buildPossibleSet();
		
		Assert.assertTrue(possibleOccurrences.containsAll(occurrences));

	}
	
	@Test
	public void test2() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("test.ttl");
		
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		Map<Digram, Set<DigramOccurence>> map = DigramHelper.findNonOverOccurrences(occurrences);
		
		Assert.assertTrue(isNonRepeating(map.values()));

	}
	
	/**
	 * returns true if there are no repeating nodes in the different digram occurrences
	 * @param allOccurs
	 * @return
	 */
	private boolean isNonRepeating(Collection<Set<DigramOccurence>> allOccurs) {
		List<RDFNode> nodes = new ArrayList<RDFNode>();
		for(Set<DigramOccurence> curSet: allOccurs) {
			for(DigramOccurence curOccur: curSet) {
				Set<RDFNode> tempNodes = new HashSet<RDFNode>();
				tempNodes.add(curOccur.getEdge1().getSubject());
				tempNodes.add(curOccur.getEdge1().getObject());
				tempNodes.add(curOccur.getEdge2().getSubject());
				tempNodes.add(curOccur.getEdge2().getObject());
				nodes.addAll(tempNodes);
			}
		}
		Set<RDFNode> nodeSet = new HashSet<RDFNode>(nodes);
		return nodes.size() == nodeSet.size();
	}
	
	/**
	 * 
	 * @return
	 */
	private Set<DigramOccurence> buildPossibleSet() {
		Set<DigramOccurence> possibleOccurrences = new HashSet<>();
	
		List<RDFNode> fExternal = new LinkedList<RDFNode>();
		fExternal.add(KENTUCKY);
		addOccurrence(possibleOccurrences, 
				ResourceFactory.createStatement(KENTUCKY, RDF.type, SCHEMA_PLACE), 
				ResourceFactory.createStatement(ALABAMA, RDF.type, SCHEMA_PLACE), fExternal);
		
		List<RDFNode> sExternal = new LinkedList<RDFNode>();
		sExternal.add(KENTUCKY);
		addOccurrence(possibleOccurrences, 
				ResourceFactory.createStatement(LINCOLN, BIRTH_PLACE, KENTUCKY_LITERAL), 
				ResourceFactory.createStatement(LINCOLN, BIRTH_PLACE, KENTUCKY), sExternal);
		
		List<RDFNode> tExternal = new LinkedList<RDFNode>();
		tExternal.add(ONT_PLACE);
		addOccurrence(possibleOccurrences, 
				ResourceFactory.createStatement(OBAMA, BIRTH_PLACE, HAWAII), 
				ResourceFactory.createStatement(HAWAII, RDF.type, ONT_PLACE), tExternal);
		
		List<RDFNode> foExternal = new LinkedList<RDFNode>();
		foExternal.add(HAWAII);
		addOccurrence(possibleOccurrences, 
				ResourceFactory.createStatement(HAWAII, RDF.type, ONT_PLACE), 
				ResourceFactory.createStatement(CHALCIS, RDF.type, ONT_PLACE), foExternal);
		
		List<RDFNode> fiExternal = new LinkedList<RDFNode>();
		fiExternal.add(LINCOLN);
		fiExternal.add(SCHEMA_PLACE);
		addOccurrence(possibleOccurrences, 
				ResourceFactory.createStatement(LINCOLN, BIRTH_PLACE, KENTUCKY), 
				ResourceFactory.createStatement(KENTUCKY, RDF.type, SCHEMA_PLACE), fiExternal);
		
		return possibleOccurrences;
	}
	
	/**
	 * 
	 * @param possibleOccurrences
	 * @param stmt1
	 * @param stmt2
	 * @param externals
	 */
	private void addOccurrence(Set<DigramOccurence> possibleOccurrences, Statement stmt1, Statement stmt2, List<RDFNode>externals) {
		DigramOccurence newOccurence = new DigramOccurence(stmt1, stmt2, externals);
		DigramOccurence newOccurence2 = new DigramOccurence(stmt2, stmt1, externals);
		possibleOccurrences.add(newOccurence);
		possibleOccurrences.add(newOccurence2);
	}
}
