package org.dice_group.grp.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * This class streams a given graph, counts the number of triples, forwards
 * resources in one file and classes into a second file.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SimpleGraphAnalyzer implements StreamRDF {

    private static final Lang INPUT_LANG = Lang.NT;
    private static final String RESOURCES_FILE = "resources.txt";
    private static final String CLASSES_FILE = "classes.txt";
    
    private static final String TYPE_URI = RDF.uri + "type";
    private static final String SUBCLASS_URI = RDFS.uri + "subClassOf";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        SimpleGraphAnalyzer analyzer = new SimpleGraphAnalyzer();
        try(InputStream in = new BufferedInputStream(new FileInputStream(new File(args[0])))) {
            RDFDataMgr.parse(analyzer, in, "", INPUT_LANG);
        }
        System.out.print("Triples:    ");
        System.out.println(analyzer.getCount());
        System.out.print("Properties: ");
        System.out.println(analyzer.getPropertiesCount());
    }

    private PrintStream rOut;
    private PrintStream cOut;
    private long count = 0;
    private Set<String> properties = new HashSet<String>();

    @Override
    public void start() {
        try {
            rOut = new PrintStream(new File(RESOURCES_FILE));
            cOut = new PrintStream(new File(CLASSES_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void triple(Triple triple) {
        Node subject = triple.getSubject();
        printResource(subject);
        Node object = triple.getSubject();
        printResource(object);
        // Check the property
        String predicate = triple.getPredicate().getURI();
        if(!properties.contains(predicate)) {
            printResource(triple.getPredicate());
            properties.add(predicate);
        }
        // Check for classes
        switch(predicate) {
        case TYPE_URI: {
            printClass(object);
            if (object.isURI()) {
                String objectUri = object.getURI();
                if(objectUri.equals(RDFS.Class.getURI()) || objectUri.equals(OWL.Class.getURI())) {
                    printClass(subject);
                }
            }
            break;
        }
        case SUBCLASS_URI : {
            printClass(subject);
            printClass(object);
            break;
        }
        default:{
            // nothing to do
            break;
        }
        }
        ++count;
        if((count % 1000000) == 0) {
            System.out.print(count);
            System.out.println(" triples");
        }
    }

    private void printResource(Node node) {
        if (node.isURI()) {
            rOut.println(node.getURI());
        }
    }

    private void printClass(Node node) {
        if (node.isURI()) {
            cOut.println(node.getURI());
        }
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
    }

    @Override
    public void base(String base) {
    }

    @Override
    public void prefix(String prefix, String iri) {
    }

    @Override
    public void finish() {
        IOUtils.closeQuietly(cOut);
        IOUtils.closeQuietly(rOut);
    }

    public long getCount() {
        return count;
    }

    public int getPropertiesCount() {
        return properties.size();
    }
}
