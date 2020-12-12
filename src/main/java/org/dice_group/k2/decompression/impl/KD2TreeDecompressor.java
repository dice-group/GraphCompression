package org.dice_group.k2.decompression.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.dice_group.k2.decompression.GrammarDecompressor;
import org.dice_group.k2.exceptions.NotSupportedException;
import org.dice_group.k2.serialization.impl.KD2TreeDeserializer;
import org.dice_group.k2.serialization.impl.ThreadedKD2TreeDeserializer;
import org.dice_group.k2.util.LabledMatrix;
import org.dice_group.k2.util.Point;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.util.List;
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


    public Graph decompressStart(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException, ExecutionException, InterruptedException {
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
            Node property = dict.getNode(matrix.getLabelId()+1, TripleComponentRole.PREDICATE);
            Integer ntID = null;


            for(Point p : matrix.getPoints()){
                Node subject = dict.getNode(p.getRow()+1, TripleComponentRole.SUBJECT);
                Node object = dict.getNode(p.getCol()+1, TripleComponentRole.OBJECT);
                g.add(new Triple(subject, property, object));

            }
        }
        return g;
    }
}
