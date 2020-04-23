package org.dice_group.grp.decompression.impl;

import grph.Grph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.serialization.DigramSerializer;
import org.dice_group.grp.serialization.impl.DigramDeserializer;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.serialization.impl.KD2TreeDeserializer;
import org.dice_group.grp.serialization.impl.ThreadedKD2TreeDeserializer;
import org.dice_group.grp.util.GraphUtils;
import org.dice_group.grp.util.LabledMatrix;
import org.dice_group.grp.util.Point;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeCreator;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class KD2TreeDecompressor implements GrammarDecompressor {


    private final Boolean threaded;

    public KD2TreeDecompressor(){
        this(false);
    }
    
    public KD2TreeDecompressor(Boolean threaded){
        this.threaded = threaded;
    }

    private int startID =-1;
    public int getStartID() {
        return startID;
    }

    public void setStartID(int startID) {
        this.startID = startID;
    }


    public Graph decompressStart(byte[] arr, NodeDictionary dict, List<Statement> nonTerminalEdges) throws NotSupportedException, IOException, ExecutionException, InterruptedException {
        Graph g = GraphFactory.createDefaultGraph();
        List<LabledMatrix> matrices;
        if(threaded){
            int cores = Runtime.getRuntime().availableProcessors();
            ThreadedKD2TreeDeserializer deser = new ThreadedKD2TreeDeserializer();
            matrices = deser.deserialize(arr, cores);
        }else {
            KD2TreeDeserializer deser = new KD2TreeDeserializer();
            matrices = deser.deserialize(arr);
        }
        for(LabledMatrix matrix : matrices){
            boolean isNT = false;
            Node property = dict.getNode(matrix.getLabelId(), TripleComponentRole.PREDICATE);
            Integer ntID = null;
            if(property.getURI().startsWith(GrammarHelper.NON_TERMINAL_PREFIX)){
                ntID  = matrix.getLabelId();
            }

            for(Point p : matrix.getPoints()){
                Node subject = dict.getNode(p.getRow(), TripleComponentRole.OBJECT);
                Node object = dict.getNode(p.getCol(), TripleComponentRole.OBJECT);

                if(ntID !=null){
                    nonTerminalEdges.add(new Statement(p.getRow(),ntID, p.getCol()));
                }
                else {
                    g.add(new Triple(subject, property, object));
                    //m.add(s,ResourceFactory.createProperty(property.getURI()),o);
                }
            }
        }
        return g;
    }
}
