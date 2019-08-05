package org.dice_group.grp.grammar.digram;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class DigramTest {

	@Test
	public void test() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("test.ttl");
		
		
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		Set<Digram> digrams = DigramHelper.getDigrams(occurrences);
		Map<Digram, Set<DigramOccurence>> map = DigramHelper.mapDigrams(occurrences);
		
		for(DigramOccurence occur: occurrences) {
			System.out.println(occur.toString());
		}

			
	}

}
