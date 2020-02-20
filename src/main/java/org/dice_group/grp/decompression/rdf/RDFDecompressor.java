package org.dice_group.grp.decompression.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.dice_group.grp.decompression.GRPReader;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.decompression.impl.CRSDecompressor;
import org.dice_group.grp.decompression.impl.KD2TreeDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.impl.URIBasedSearcher;
import org.dice_group.grp.serialization.impl.DigramDeserializer;
import org.dice_group.grp.util.GraphUtils;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class RDFDecompressor {

    public Model decompress(String file, boolean kd2Decompressor) throws IOException, NotSupportedException {
        GrammarDecompressor dcmpr;
        if(kd2Decompressor){
             dcmpr = new KD2TreeDecompressor();
        }
        else{
            dcmpr = new CRSDecompressor();
        }

        HDTSpecification spec = new HDTSpecification();
		spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

        PSFCFourSectionDictionary dict = new PSFCFourSectionDictionary(spec);
        byte[] grammar = GRPReader.load(file, dict);
        return decompressFull(grammar, new NodeDictionary(dict), dcmpr);
    }

    public Model decompressFull(byte[] arr, NodeDictionary dict, GrammarDecompressor dcmpr) throws IOException, NotSupportedException {
        //startSize, start, rules
        //1. 4 bytes = length of start := X
        ByteBuffer bb = ByteBuffer.wrap(arr);
        byte[] startBytes = new byte[4];
        bb.get(startBytes);
        int startSize = ByteBuffer.wrap(startBytes).getInt();
        //2. X bytes = start Graph
        byte[] start = new byte[startSize];
        bb = bb.slice();
        bb.get(start);
        //rather a mapping Map<Integer, List<Statement>>
        List<org.dice_group.grp.grammar.Statement> nonTerminalEdges = new ArrayList<org.dice_group.grp.grammar.Statement>();
        Model startGraph = dcmpr.decompressStart(start, dict, nonTerminalEdges);


        Collections.sort(nonTerminalEdges, new Comparator<org.dice_group.grp.grammar.Statement>() {
            @Override
            public int compare(org.dice_group.grp.grammar.Statement s1, org.dice_group.grp.grammar.Statement s2) {
                Integer nt1= Integer.valueOf(dict.getNode(s1.getPredicate(), TripleComponentRole.PREDICATE).getURI().replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                Integer nt2= Integer.valueOf(dict.getNode(s2.getPredicate(), TripleComponentRole.PREDICATE).getURI().replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                int pCT =nt1.compareTo(nt2);
                if(pCT!=0){
                    return pCT;
                }
                int sCT = s1.getSubject().compareTo(s2.getSubject());
                if(sCT!=0){
                    return sCT;
                }
                int oCT = s1.getObject().compareTo(s2.getObject());
                return oCT;
            }
        });
        //3. decompress rules
        bb = bb.slice();
        byte[] rules = new byte[arr.length-(Integer.BYTES+startSize)];
        bb.get(rules);
        decompressRules(startGraph, rules, dict, nonTerminalEdges, dcmpr.getStartID());

        return startGraph;
    }


    public void decompressRules(Model m, byte[] arr, NodeDictionary dict, List<org.dice_group.grp.grammar.Statement> nonTerminalEdges, int startID) throws IOException {
        DigramDeserializer dSer = new DigramDeserializer();

        List<Statement> mapping = dSer.decompressRules(arr, dict, startID, nonTerminalEdges);
        for(int i=0; i<mapping.size();i++){
            Statement occ = mapping.get(i);
            addStatement(occ, m, dict);
        }

    }

    private void addStatement(Statement edge, Model m, NodeDictionary dict){
        Node property = dict.getNode(edge.getPredicate(), TripleComponentRole.PREDICATE);
        Node subject = dict.getNode(edge.getSubject(), TripleComponentRole.SUBJECT);
        Node object = dict.getNode(edge.getObject(), TripleComponentRole.SUBJECT);

        RDFNode o;
        if(object.getURI().startsWith("\\\"") || object.getURI().startsWith("\"")) {
            //TODO parse Node, what literal it is !!!
            o = GraphUtils.parseHDTLiteral(object);
        }
        else{
            o = ResourceFactory.createProperty(JenaNodeFormatter.format(object));
        }
        m.add(ResourceFactory.createResource(JenaNodeFormatter.format(subject)),
                ResourceFactory.createProperty(JenaNodeFormatter.format(property)),
                o);
    }

}
