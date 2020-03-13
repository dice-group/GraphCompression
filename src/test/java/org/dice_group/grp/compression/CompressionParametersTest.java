package org.dice_group.grp.compression;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.main.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CompressionParametersTest {

    private boolean c1=true;
    private boolean c2=true;
    private boolean c3=true;
    private boolean c4=true;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {true, true, true, true}, {true, false, true, true}, {true, true, false, true},
                {true, false, false, true}, {true, true, true, false}, {true, false, true, false},
                {true, true, false, false}, {true, false, false, false}
        });
    }

    public CompressionParametersTest(boolean c1, boolean c2, boolean c3, boolean c4){
        this.c1=c1;
        this.c2=c2;
        this.c3=c3;
        this.c4=c4;
    }

    @Test
    public void testPD() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
            //mainly weird digrams and combination of digrams
            Main.compress("src/test/resources/persondata_en.ttl", "src/test/resources/persondata_en.ttl.grp", c1, c2, c3);
            Main.decompress("src/test/resources/persondata_en.ttl.grp", "src/test/resources/persondata_en.ttl.grp.nt", c1,  c4, "N-TRIPLE");

            checkEqual("src/test/resources/persondata_en.ttl", "src/test/resources/persondata_en.ttl.grp.nt");

            new File("src/test/resources/persondata_en.ttl.grp").delete();
            new File("src/test/resources/persondata_en.ttl.grp.nt").delete();
    }

    @Test
    public void testGO() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
            // mainly literals and datatypes testing
            Main.compress("src/test/resources/geo_coordinates_en.ttl", "src/test/resources/geo_coordinates_en.ttl.grp", c1, c2, c3);
            Main.decompress("src/test/resources/geo_coordinates_en.ttl.grp", "src/test/resources/geo_coordinates_en.ttl.grp.ttl", c1, c4, "TURTLE");

            checkEqual("src/test/resources/geo_coordinates_en.ttl", "src/test/resources/geo_coordinates_en.ttl.grp.ttl");

            new File("src/test/resources/geo_coordinates_en.ttl.grp").delete();
            new File("src/test/resources/geo_coordinates_en.ttl.grp.ttl").delete();

    }

    @Test
    public void testBN() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
        //blank node test
        Main.compress("src/test/resources/bn.nt", "src/test/resources/bn.grp", c1, c2, c3);
        Main.decompress("src/test/resources/bn.grp", "src/test/resources/bn.grp.ttl", c1, c4, "TURTLE");

        checkEqual("src/test/resources/bn.nt", "src/test/resources/bn.grp.ttl");

        new File("src/test/resources/bn.grp").delete();
        new File("src/test/resources/bn.grp.ttl").delete();

    }

    public static void checkEqual(String expectedFile, String actualFile) throws FileNotFoundException {
        Model m1 = ModelFactory.createDefaultModel();
        m1.read(new FileReader(expectedFile), null, "TTL");


        Model m2 = ModelFactory.createDefaultModel();
        m2.read(new FileReader(actualFile), null, "TTL");
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
