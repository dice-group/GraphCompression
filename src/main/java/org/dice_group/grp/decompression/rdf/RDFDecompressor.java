package org.dice_group.grp.decompression.rdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_group.grp.decompression.GRPReader;
import org.dice_group.grp.decompression.GrammarDecompressor;
import org.dice_group.grp.decompression.impl.KD2TreeDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;

import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RDFDecompressor {

    private boolean threaded=false;

    public RDFDecompressor(boolean threaded){
        this.threaded = threaded;
    }

    public Model decompress(String file) throws IOException, NotSupportedException, ExecutionException, InterruptedException {
        GrammarDecompressor dcmpr;
        dcmpr = new KD2TreeDecompressor(threaded);

        HDTSpecification spec = new HDTSpecification();
		spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

        PSFCFourSectionDictionary dict = new PSFCFourSectionDictionary(spec);
        byte[] grammar = GRPReader.load(file, dict);
        return decompressFull(grammar, new NodeDictionary(dict), dcmpr);
    }

    public Model decompressFull(byte[] arr, NodeDictionary dict, GrammarDecompressor dcmpr) throws IOException, NotSupportedException, ExecutionException, InterruptedException {
        //startSize, start, rules
        //1. 4 bytes = length of start := X
        //rather a mapping Map<Integer, List<Statement>>
        Graph startGraph = dcmpr.decompressStart(arr, dict);


        return ModelFactory.createModelForGraph(startGraph);
    }




}
