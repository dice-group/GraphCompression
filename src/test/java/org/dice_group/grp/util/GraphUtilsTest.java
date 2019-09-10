package org.dice_group.grp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

public class GraphUtilsTest {

	
	@Test
	public void getRDFIndexTest() {
		RDFNode node = ResourceFactory.createResource(":s12");
		assertEquals(12, GraphUtils.getRDFIndex(node).intValue());
		node = ResourceFactory.createResource(":o1");
		assertEquals(1, GraphUtils.getRDFIndex(node).intValue());
		node = ResourceFactory.createResource(":p412234");
		assertEquals(412234, GraphUtils.getRDFIndex(node).intValue());
		node = ResourceFactory.createResource(":s"+Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, GraphUtils.getRDFIndex(node).intValue());
		
		node = ResourceFactory.createResource(":q1");
		assertNull(GraphUtils.getRDFIndex(node));
	}
	
	/**
	 * Row 0 | 
Row 1 | 7 0 | 
Row 2 | 8 1 | 
Row 3 | 3 1 | 9 1 | 
Row 4 | 10 2 | 
Row 5 | 6 2 | 
Row 6 | 11 0 | 
Row 7 | 
Row 8 | 
Row 9 | 
Row 10 | 
Row 11 | 5 3 | 4 3 | 1 3 | 
	 * @throws FileNotFoundException
	 */
	@Test
	public void createIntegerRCMatrix() throws FileNotFoundException {
		Model m = ModelFactory.createDefaultModel();
		m.read(new FileReader("src/test/resources/matrix.nt"), null, "NT");
		List<List<Integer[]>> ircMatrix = GraphUtils.createIntegerRCMatrix(m);
		int i=0;
		for(List<Integer[]> row : ircMatrix) {
			System.out.print("Row "+i+++" | ");
			for(Integer[] cell : row) {
				System.out.print(cell[0]+" "+cell[1]+" | ");
			}
			System.out.println();
		}
		//check empty lists
		assertEquals(0, ircMatrix.get(0).size());
		assertEquals(0, ircMatrix.get(7).size());
		assertEquals(0, ircMatrix.get(8).size());
		assertEquals(0, ircMatrix.get(9).size());
		assertEquals(0, ircMatrix.get(10).size());
		//check single value lists
		assertEquals(1, ircMatrix.get(1).size());
		Integer[] val = ircMatrix.get(1).get(0);
		assertEquals(7, val[0].intValue());
		assertEquals(0, val[1].intValue());
		assertEquals(1, ircMatrix.get(2).size());
		val = ircMatrix.get(2).get(0);
		assertEquals(8, val[0].intValue());
		assertEquals(1, val[1].intValue());
		assertEquals(1, ircMatrix.get(4).size());
		val = ircMatrix.get(4).get(0);
		assertEquals(10, val[0].intValue());
		assertEquals(2, val[1].intValue());
		assertEquals(1, ircMatrix.get(5).size());
		val = ircMatrix.get(5).get(0);
		assertEquals(6, val[0].intValue());
		assertEquals(2, val[1].intValue());
		assertEquals(1, ircMatrix.get(6).size());
		val = ircMatrix.get(6).get(0);
		assertEquals(11, val[0].intValue());
		assertEquals(0, val[1].intValue());
		
		assertEquals(2, ircMatrix.get(3).size());
		val = ircMatrix.get(3).get(0);
		assertEquals(3, val[0].intValue());
		assertEquals(1, val[1].intValue());
		val = ircMatrix.get(3).get(1);
		assertEquals(9, val[0].intValue());
		assertEquals(1, val[1].intValue());
		
		assertEquals(3, ircMatrix.get(11).size());
		val = ircMatrix.get(11).get(0);
		assertEquals(5, val[0].intValue());
		assertEquals(3, val[1].intValue());
		val = ircMatrix.get(11).get(1);
		assertEquals(4, val[0].intValue());
		assertEquals(3, val[1].intValue());
		val = ircMatrix.get(11).get(2);
		assertEquals(1, val[0].intValue());
		assertEquals(3, val[1].intValue());
	}
}
