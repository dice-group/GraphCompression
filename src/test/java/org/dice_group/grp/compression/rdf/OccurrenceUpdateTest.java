package org.dice_group.grp.compression.rdf;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.junit.Assert;
import org.junit.Test;

public class OccurrenceUpdateTest{

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

			// are the newly recognized occurrences legit?	
			digrams.forEach((k,v)->{
				System.out.println(k.toString()+" "+v.toString());
			});
			
		}
	}
}
