package org.dice_group.grp.compression.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.Grammar;
import org.junit.Assert;
import org.junit.Test;

public class DecompressionTest {
	@Test
	public void decompressionTest() {
		Model originalModel = ModelFactory.createDefaultModel();
		originalModel.read("test.ttl");
		RDFCompressor com = new RDFCompressor();
		
		// since the compression changes the original model
		Model tobeCompressed = ModelFactory.createDefaultModel();
		tobeCompressed.add(originalModel);
		Grammar grammar = null;
		try {			
			grammar = com.createGrammar(tobeCompressed);
		} catch (NotAllowedInRDFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(grammar != null) {
			Model decompressed = com.decompressGrammar(grammar);
			Assert.assertTrue(decompressed.isIsomorphicWith(originalModel));
		}
	}
}
