package org.dice_group.grp.compression.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.junit.Test;

public class RDFCompressorTest {

	
	@Test
	public void replaceAllOccurencesTest() throws FileNotFoundException, NotAllowedInRDFException {
		//Create Graph with Digrams
		Model replaced = ModelFactory.createDefaultModel();
		Model expected = ModelFactory.createDefaultModel();
		
		
		replaced.read(new FileInputStream("src/test/resources/replace.nt"), null, "NT");
		expected.read(new FileInputStream("src/test/resources/repOcc.nt"), null, "NT");
		System.out.println(replaced);
		System.out.println("##############");
		// create all DigramOccurences
		Set<DigramOccurence> occSet = new HashSet<DigramOccurence>();
		/* <urn:s1> <urn:p1> <urn:o1> .
		 * <urn:s1> <urn:p2> <urn:o2> .
		 * <urn:s2> <urn:p1> <urn:o3> .
		 * <urn:s2> <urn:p2> <urn:o4> .
		 * 
		 */
		Resource s1 = ResourceFactory.createResource("urn://s1.d");
		Resource s2 = ResourceFactory.createResource("urn://s2.d");
		Resource o1 = ResourceFactory.createResource("urn://o1.d");
		Resource o2 = ResourceFactory.createResource("urn://o2.d");
		Resource o3 = ResourceFactory.createResource("urn://o3.d");
		Resource o4 = ResourceFactory.createResource("urn://o4.d");
		Property p1 = ResourceFactory.createProperty("urn://p1.d");
		Property p2 = ResourceFactory.createProperty("urn://p2.d");
		
		Statement d1 = ResourceFactory.createStatement(s1, p1, o1);
		Statement d2 = ResourceFactory.createStatement(s1, p2, o2);
		Statement d3 = ResourceFactory.createStatement(s2, p1, o3);
		Statement d4 = ResourceFactory.createStatement(s2, p2, o4);
		
		List<RDFNode> external1 = new LinkedList<RDFNode>();
		external1.add(s1);
		external1.add(o1);
		//external1.add(o2);
		
		List<RDFNode> external2 = new LinkedList<RDFNode>();
		external2.add(s2);
		external2.add(o3);
		//external2.add(o4);
		
		DigramOccurence occ1 = new DigramOccurence(d1, d2, external1);
		DigramOccurence occ2 = new DigramOccurence(d3, d4, external2);
		occSet.add(occ1);
		occSet.add(occ2);
		
		//replace all occ
		RDFCompressor c = new RDFCompressor();
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		System.out.println(replaced);
		System.out.println();
		/*check if the resulting graph is correct
		*[urn://o3.d, urn://p4.d, urn://o4.d] 
		*[urn://s2.d, urn://p3.d, urn://o3.d] 
		*[urn://s1.d, urn://p6.d, urn://s1.d] 
		*[urn://s1.d, urn://p5.d, urn://s1.d] 
		*[urn://s1.d, urn://p3.d, urn://o1.d] 
		*[urn://s1.d, urn://p7.d, urn://o1.d] 
		*[urn://s1.d, :n0, urn://o1.d] 
		*[urn://o1.d, urn://p8.d, urn://o1.d] 
		*[urn://o1.d, urn://p4.d, urn://o2.d]>
		**
		**/
		assertTrue(expected.isIsomorphicWith(replaced));
		for(Statement stmt : replaced.listStatements().toList()) {
			assertTrue(expected.contains(stmt));
		}
				
	}
}
