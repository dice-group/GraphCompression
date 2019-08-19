package org.dice_group.grp.compression.rdf;

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
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.junit.Test;

public class RDFCompressorTest {

	
	private Resource[] getSubjects() {
		Resource[] ret = new Resource[15];
		for(int i=1;i<=15;i++) {
			ret[i-1] = ResourceFactory.createResource("urn://s"+i+".d");
		}
		return ret;
	}
	
	private Resource[] getObjects() {
		Resource[] ret = new Resource[4];
		for(int i=1;i<=4;i++) {
			ret[i-1] = ResourceFactory.createResource("urn://o"+i+".d");
		}
		return ret;
	}
	
	private Property[] getPredicates() {
		Property[] ret = new Property[11];
		for(int i=1;i<=11;i++) {
			ret[i-1] = ResourceFactory.createProperty("urn://p"+i+".d");
		}
		return ret;
	}
	
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

		Resource[] subjects = getSubjects();
		Resource[] objects = getObjects();
		Property[] props = getPredicates();

		List<DigramOccurence> occ = generateDigramOcc(subjects, objects, props);
		
		occSet.add(occ.get(0));
		occSet.add(occ.get(1));
		
		//replace all occ
		RDFCompressor c = new RDFCompressor();
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		occSet.clear();
		occSet.add(occ.get(2));
		
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		occSet.clear();
		occSet.add(occ.get(3));
		
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		occSet.clear();
		occSet.add(occ.get(4));
		
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		occSet.clear();
		occSet.add(occ.get(5));
		
		replaced = c.replaceAllOccurences(GrammarHelper.getNextNonTerminal(), occSet, replaced);
		

		System.out.println(replaced);
		System.out.println();

		//check if result is correct
		assertTrue(expected.isIsomorphicWith(replaced));
		for(Statement stmt : replaced.listStatements().toList()) {
			assertTrue(expected.contains(stmt));
		}
				
	}

	private List<DigramOccurence> generateDigramOcc(Resource[] s, Resource[] o, Property[] p) {
		List<DigramOccurence> ret = new LinkedList<DigramOccurence>();
		Statement d1 = ResourceFactory.createStatement(s[0], p[0], o[0]);
		Statement d2 = ResourceFactory.createStatement(s[0], p[1], o[1]);
		Statement d3 = ResourceFactory.createStatement(s[1], p[0], o[2]);
		Statement d4 = ResourceFactory.createStatement(s[1], p[1], o[3]);
		
		Statement d5 = ResourceFactory.createStatement(s[3], p[3], s[4]);
		Statement d6 = ResourceFactory.createStatement(s[4], p[4], s[5]);
		
		Statement d7 = ResourceFactory.createStatement(s[6], p[5], s[7]);
		Statement d8 = ResourceFactory.createStatement(s[7], p[6], s[8]);
		
		Statement d9 = ResourceFactory.createStatement(s[9], p[7], s[10]);
		Statement d10 = ResourceFactory.createStatement(s[10], p[8], s[11]);
		
		Statement d11 = ResourceFactory.createStatement(s[12], p[9], s[13]);
		Statement d12 = ResourceFactory.createStatement(s[12], p[10], s[14]);
		
		List<RDFNode> external1 = new LinkedList<RDFNode>();
		external1.add(s[0]);
		external1.add(o[0]);
		//external1.add(o2);
		
		List<RDFNode> external2 = new LinkedList<RDFNode>();
		external2.add(s[1]);
		external2.add(o[2]);
		//external2.add(o4);
		
		List<RDFNode> external3 = new LinkedList<RDFNode>();
		external3.add(s[3]);
		
		List<RDFNode> external4 = new LinkedList<RDFNode>();
		external4.add(s[6]);
		external4.add(s[8]);
		
		List<RDFNode> external5 = new LinkedList<RDFNode>();
		external5.add(s[11]);
		
		List<RDFNode> external6 = new LinkedList<RDFNode>();
		external6.add(s[14]);
		
		ret.add(new DigramOccurence(d1, d2, external1));
		ret.add(new DigramOccurence(d3, d4, external2));
		ret.add(new DigramOccurence(d5, d6, external3));
		ret.add(new DigramOccurence(d7, d8, external4));
		ret.add(new DigramOccurence(d9, d10, external5));
		ret.add(new DigramOccurence(d11, d12, external6));

		return ret;
	}
	
}
