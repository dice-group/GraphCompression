package org.dice_group.grp.util;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class RDFHelperTest {


	
	@Test
	public void countTest() throws FileNotFoundException {
		Model m = ModelFactory.createDefaultModel();
		m.read(new FileReader("src/test/resources/repOcc.nt"), null, "NT");
		
		assertEquals(6, RDFHelper.getPropertyCount(m).longValue());
		assertEquals(8, RDFHelper.getSubjectCount(m).longValue());
		assertEquals(8, RDFHelper.getObjectCount(m).longValue());

	}
}
