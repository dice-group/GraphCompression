package org.dice_group.grp.decompression.impl;

import grph.Grph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.out.NodeFormatter;
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

public class KD2TreeDecompressor implements GrammarDecompressor {



    private int startID =-1;
    public int getStartID() {
        return startID;
    }

    public void setStartID(int startID) {
        this.startID = startID;
    }



    public Model decompressStart(byte[] arr, NodeDictionary dict, List<Statement> nonTerminalEdges) throws NotSupportedException, IOException {
        Model m = ModelFactory.createDefaultModel();
        KD2TreeDeserializer deser = new KD2TreeDeserializer();
        List<LabledMatrix> matrices = deser.deserialize(arr);
        for(LabledMatrix matrix : matrices){
            boolean isNT = false;
            Node property = dict.getNode(matrix.getLabelId(), TripleComponentRole.PREDICATE);
            Integer ntID = null;
            if(property.getURI().startsWith(GrammarHelper.NON_TERMINAL_PREFIX)){
                ntID  = matrix.getLabelId();
            }

            for(Point p : matrix.getPoints()){
                Node subject = dict.getNode(p.getRow(), TripleComponentRole.SUBJECT);
                Node object = dict.getNode(p.getCol(), TripleComponentRole.SUBJECT);
                RDFNode o;
                if(object.getURI().startsWith("\\\"")) {
                    o = GraphUtils.parseHDTLiteral(object);
                }
                else{
                    o = ResourceFactory.createResource(object.getURI());
                }
                if(ntID !=null){
                    nonTerminalEdges.add(new Statement(p.getRow(),ntID, p.getCol()));
                }
                else {
                    m.add(ResourceFactory.createResource(subject.getURI()),
                            ResourceFactory.createProperty(property.getURI()),
                            o);
                }
            }
        }
        return m;
    }
}
