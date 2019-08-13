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
		// finds all occurrences
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		
		//finds and maps all the non overlapping occurrences
		Map<Digram, Set<DigramOccurence>> map = DigramHelper.findNonOverOccurrences(occurrences);
		Set<Digram> digrams = map.keySet();
		List<Digram> sortedDigrams = DigramHelper.sortDigrambyFrequence(digrams);
		
		
		System.out.println("Map");
		map.forEach((k,v)->{
			System.out.println("Digram\n"+k);
			System.out.println("Occurrences:\n"+v);
		});
		
		System.out.println("All Occurrences:");
		occurrences.forEach((occurrence)->{
			System.out.println(occurrence.toString());
		});
		
		System.out.println("Sorted Digrams");
		sortedDigrams.forEach((digram)->{
			System.out.println(digram.toString());
		});
	}

}
