package org.dice_group.k2.compression;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.k2.exceptions.NotAllowedInRDFException;
import org.dice_group.k2.exceptions.NotSupportedException;
import org.dice_group.k2.main.Main;
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
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CompressionParametersTest {

    private boolean c1;



    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {true}, {false}
        });
    }

    public CompressionParametersTest(boolean c1){
        this.c1=c1;

    }

    @Test
    public void testPD() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
            //mainly weird digrams and combination of digrams
            Main.compress("src/test/resources/persondata_en.ttl", "src/test/resources/persondata_en.ttl.grp", c1, false);
            Main.decompress("src/test/resources/persondata_en.ttl.grp", "src/test/resources/persondata_en.ttl.grp.nt", c1, "N-TRIPLE");

            checkEqual("src/test/resources/persondata_en.ttl", "src/test/resources/persondata_en.ttl.grp.nt");

            new File("src/test/resources/persondata_en.ttl.grp").delete();
            new File("src/test/resources/persondata_en.ttl.grp.nt").delete();
    }

    @Test
    public void testGO() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
            // mainly literals and datatypes testing
            Main.compress("src/test/resources/geo_coordinates_en.ttl", "src/test/resources/geo_coordinates_en.ttl.grp", c1, false);
            Main.decompress("src/test/resources/geo_coordinates_en.ttl.grp", "src/test/resources/geo_coordinates_en.ttl.grp.ttl", c1, "TURTLE");

            checkEqual("src/test/resources/geo_coordinates_en.ttl", "src/test/resources/geo_coordinates_en.ttl.grp.ttl");

            new File("src/test/resources/geo_coordinates_en.ttl.grp").delete();
            new File("src/test/resources/geo_coordinates_en.ttl.grp.ttl").delete();

    }

    @Test
    public void testBN() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
        //blank node test
        Main.compress("src/test/resources/bn.nt", "src/test/resources/bn.grp", c1, false);
        Main.decompress("src/test/resources/bn.grp", "src/test/resources/bn.grp.ttl", c1, "TURTLE");

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
                if(st1.getSubject().isAnon()){
                    sC= -1;
                    if(st2.getSubject().isAnon()){
                        sC=0;
                    }
                }
                if(sC!=0){return sC;}
                int pC = st1.getPredicate().toString().compareTo(st2.getPredicate().toString());
                if(pC!=0){return pC;}
                if(st1.getObject().isAnon()){
                    int oC = -1;
                    if(st2.getSubject().isAnon()){
                        oC=0;
                    }
                    return oC;
                }
                return st1.getObject().toString().compareTo(st2.getObject().toString());
            }
        });

        List<Statement> actual = m2.listStatements().toList();
        Collections.sort(actual, new Comparator<Statement>() {
            @Override
            public int compare(Statement st1, Statement st2) {
                int sC = st1.getSubject().toString().compareTo(st2.getSubject().toString());
                if(st1.getSubject().isAnon()){
                    sC= -1;
                    if(st2.getSubject().isAnon()){
                        sC=0;
                    }
                }
                if(sC!=0){return sC;}
                int pC = st1.getPredicate().toString().compareTo(st2.getPredicate().toString());
                if(pC!=0){return pC;}
                if(st1.getObject().isAnon()){
                    int oC = -1;
                    if(st2.getSubject().isAnon()){
                        oC=0;
                    }
                    return oC;
                }
                return st1.getObject().toString().compareTo(st2.getObject().toString());
            }
        });
        for(int i=0;i<expected.size();i++){
            Statement stmtExp = expected.get(i);
            Statement stmtAct = actual.get(i);
            if(stmtExp.getSubject().isAnon()){
                assertTrue(stmtAct.getSubject().isAnon());
            }
            else{
                assertEquals(stmtExp.getSubject(), stmtAct.getSubject());
            }
            if(stmtExp.getObject().isAnon()){
                assertTrue(stmtAct.getObject().isAnon());
            }
            else{
                assertEquals(stmtExp.getObject(), stmtAct.getObject());
            }
            assertEquals(stmtExp.getPredicate(), stmtAct.getPredicate());
        }
    }

}
