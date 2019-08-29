package org.dice_group.grp.compression.rdf;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.junit.Assert;
import org.junit.Test;

public class OccurrenceUpdateTest{
	private RDFNode KENTUCKY_LITERAL = ResourceFactory.createPlainLiteral("HardinCounty Kentucky");
	private Property BIRTH_PLACE = ResourceFactory.createProperty("http://dbpedia.org/ontology/birthPlace");
	private Resource LINCOLN = ResourceFactory.createResource("http://dbpedia.org/resource/Abraham_Lincoln");
	private Resource HARDIN_KENTUCKY = ResourceFactory.createResource("http://dbpedia.org/resource/Hardin_County,_Kentucky");
	private Resource SCHEMA_PLACE = ResourceFactory.createResource("http://schema.org/Place");
	private Resource ONT_PLACE = ResourceFactory.createResource("http://dbpedia.org/ontology/Place");
	private Resource ALABAMA = ResourceFactory.createResource("http://dbpedia.org/resource/Alabama");
	private Resource THING = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Thing");
	private Resource CHALCIS = ResourceFactory.createResource("http://dbpedia.org/resource/Chalcis");

	@Test
	public void occurrenceUpdateTest() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("test.ttl");
		
		RDFCompressor c = new RDFCompressor();
		
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		Map<Digram, Set<DigramOccurence>> digrams = DigramHelper.findNonOverOccurrences(occurrences);
		List<Digram> frequenceList = DigramHelper.sortDigrambyFrequence(digrams.keySet());
		while(frequenceList.size()>0) {
			Digram mfd = frequenceList.get(0);
			// just for testing purposes
			if(mfd.getNoOfOccurences()<=0) {
				break;
			}
			String uriNT = GrammarHelper.getNextNonTerminal();
			try {
				graph = c.replaceAllOccurences(uriNT, digrams.get(mfd), graph);
			} catch (NotAllowedInRDFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			c.updateOccurences(digrams, frequenceList, graph, uriNT);
			
			// does it contain anything that was supposed to be removed?		
			Assert.assertFalse(frequenceList.contains(mfd));
			Assert.assertFalse(digrams.containsKey(mfd));
		}
		Set<Statement> modelStmts = graph.listStatements().toSet();
		
		Assert.assertTrue(modelStmts.containsAll(getExpected()));
		
	}
	
	public Set<Statement> getExpected() {
		Set<Statement> expected = new HashSet<Statement>();
		expected.add(ResourceFactory.createStatement(LINCOLN, BIRTH_PLACE, KENTUCKY_LITERAL));
		expected.add(ResourceFactory.createStatement(LINCOLN, ResourceFactory.createProperty(":n1"), LINCOLN));
		expected.add(ResourceFactory.createStatement(ONT_PLACE, ResourceFactory.createProperty(":n0"), ONT_PLACE));
		expected.add(ResourceFactory.createStatement(ALABAMA, RDF.type, SCHEMA_PLACE));
		expected.add(ResourceFactory.createStatement(CHALCIS, RDF.type, ONT_PLACE));
		expected.add(ResourceFactory.createStatement(HARDIN_KENTUCKY, RDF.type, THING));
		return expected;
	}
}
