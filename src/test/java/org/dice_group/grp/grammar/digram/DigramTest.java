package org.dice_group.grp.grammar.digram;

import java.util.List;
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
		Map<Digram, Set<DigramOccurence>> map = DigramHelper.mapDigrams(occurrences);
		Set<Digram> digrams = DigramHelper.getDigrams(map);
		List<Digram> sortedDigrams = DigramHelper.sortDigrambyFrequence(digrams);
		
		sortedDigrams.forEach((digram)->{
			System.out.println(digram.toString());
		});
		
		for(DigramOccurence occur: occurrences) {
			System.out.println(occur.toString());
		}

			
	}

}
