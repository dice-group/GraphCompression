package org.dice_group.k2;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tester {


    @Test
    public void diff() throws FileNotFoundException {
        String m1file = "/home/minimal/work/datasets/real_case_datasets/scholarydata_dump.nt";
        String m2file = "swdf_kd2_cpp.nt";

        Model m1 = ModelFactory.createDefaultModel();
        m1.read(new FileReader(m1file), null, "N-TRIPLE");
        Model m2 = ModelFactory.createDefaultModel();
        m2.read(new FileReader(m2file), null, "N-TRIPLE");
        System.out.println("Read m1 with size "+m1.size()+" and m2 with size "+m2.size());
        assertTrue(m1.isIsomorphicWith(m2));
        m1.remove(m2.listStatements());
        assertEquals(0, m1.size());
    }
}
