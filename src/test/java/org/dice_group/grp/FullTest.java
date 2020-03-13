package org.dice_group.grp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class FullTest {

    @Test
    public void test() throws FileNotFoundException {
        Model m1 = ModelFactory.createDefaultModel();
        m1.read(new FileReader("/home/minimal/work/datasets/swdfu8.nt"), null, "TTL");


        Model m2 = ModelFactory.createDefaultModel();
        m2.read(new FileReader("/home/minimal/swdf_grp2.ttl"), null, "TTL");
        //assertTrue(m1.isIsomorphicWith(m2));
        List<Statement> expected = m1.listStatements().toList();
        Collections.sort(expected, new Comparator<Statement>() {
            @Override
            public int compare(Statement st1, Statement st2) {
                int sC = st1.getSubject().toString().compareTo(st2.getSubject().toString());
                if(sC!=0){return sC;}
                int pC = st1.getPredicate().toString().compareTo(st2.getPredicate().toString());
                if(pC!=0){return pC;}
                return st1.getObject().toString().compareTo(st2.getObject().toString());
            }
        });

        List<Statement> actual = m1.listStatements().toList();
        Collections.sort(actual, new Comparator<Statement>() {
            @Override
            public int compare(Statement st1, Statement st2) {
                int sC = st1.getSubject().toString().compareTo(st2.getSubject().toString());
                if(sC!=0){return sC;}
                int pC = st1.getPredicate().toString().compareTo(st2.getPredicate().toString());
                if(pC!=0){return pC;}
                return st1.getObject().toString().compareTo(st2.getObject().toString());
            }
        });
        for(int i=0;i<expected.size();i++){
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
