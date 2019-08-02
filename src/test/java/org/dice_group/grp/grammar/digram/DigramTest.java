package org.dice_group.grp.grammar.digram;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class DigramTest {

	@Test
	public void test() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("test.ttl");
		
		Set<Digram> digrams = DigramHelper.findDigrams(graph);
		
		for(Digram digram: digrams) {
			System.out.println(digram.toString());
		}

			
	}

}
