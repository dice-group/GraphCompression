package org.dice_group.grp.fuseki;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.main.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ExtIteratorTest {


    private final String query;
    private final List<String[]> expectedRows = new ArrayList<String[]>();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"SELECT * {?s <http://example.com/d> ?o}",
                        new String[][]{{"http://example.com/a", "_:"}, {"_:", "http://example.com/a"},
                                {"http://example.com/rt", "_:"}, {"_:", "http://example.com/ac"}}},
                {"SELECT * {?s <http://example.com/b> ?o}",
                        new String[][]{{"http://example.com/rt", "http://example.com/c"}, {"_:", "_:"},
                                {"http://example.com/a", "http://example.com/c"}, {"_:", "_:"}}},
                {"SELECT * {<http://example.com/a> <http://example.com/b> ?o}",
                        new String[][]{{"http://example.com/c"}}},
                {"SELECT * {?s <http://example.com/b> <http://example.com/c>}",
                        new String[][]{{"http://example.com/rt"}, {"http://example.com/a"}}},
                {"SELECT * {?s ?p <http://example.com/c>}",
                        new String[][]{{"http://example.com/rt", "http://example.com/b"}, {"http://example.com/a", "http://example.com/b"}}}
        });
    }

    public ExtIteratorTest(String query, String[][] expectedRows){
       this.query = query;
       for(String[] row : expectedRows){
           this.expectedRows.add(row);
       }

    }

    @Test
    public void testExtendedIterator() throws InterruptedException, NotAllowedInRDFException, NotSupportedException, ExecutionException, IOException {
        File dir = new File("src/test/resources/fuseki/");
        dir.mkdir();
        ExtKD2JenaIterator it = new ExtKD2JenaIterator();
        Main.compress("src/test/resources/matrix.nt", "src/test/resources/fuseki/matrix.kd2", true, false);
        Main.compress("src/test/resources/bn.nt", "src/test/resources/fuseki/bn.kd2", true, false);
        MultipleKD2GraphAssembler assembler = new MultipleKD2GraphAssembler();
        Model meta = ModelFactory.createDefaultModel();
        RDFDataMgr.read(meta, new FileInputStream("src/test/resources/multifilesexa.ttl"), Lang.TURTLE);

        RDFDataMgr.write(new FileOutputStream(new File("hae.nt")), meta, Lang.NTRIPLES);

        Model m = (Model) assembler.open(null, meta.getResource("file://"+new File("").getAbsolutePath()+"/#graph1"), null);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);

        ResultSet res = qexec.execSelect();

        while (res.hasNext()){
            QuerySolution qsol = res.next();
            boolean contains = contains(qsol, expectedRows);
            assertTrue(contains);
        }
        assertTrue(expectedRows.size()==0);
        new File("src/test/resources/fuseki/matrix.kd2").delete();
        new File("src/test/resources/fuseki/bn.kd2").delete();
        expectedRows.clear();
    }

    private boolean contains(QuerySolution qsol, List<String[]> expectedRows) {
        List<RDFNode> actual = new ArrayList<RDFNode>();
        if(qsol.contains("s")){
            actual.add(qsol.get("s"));
        }
        if(qsol.contains("p")){
            actual.add(qsol.get("p"));
        }
        if(qsol.contains("o")){
            actual.add(qsol.get("o"));
        }
        for(int j=0;j<expectedRows.size();j++){
            String[] row = expectedRows.get(j);
            boolean contains=true;
            if(actual.size()!=row.length){
                return false;
            }
            for(int i=0;i<actual.size();i++){
                if(!(actual.get(i).isAnon() && row[i].startsWith("_:"))){
                    contains &= actual.get(i).toString().equals(row[i]);
                }
            }
            if(contains){
                expectedRows.remove(row);
                return true;
            }
        }
        return false;
    }

}
