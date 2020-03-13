package org.dice_group.grp.fuseki;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.dice_group.grp.decompression.GRPReader;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.serialization.impl.KD2TreeDeserializer;
import org.dice_group.grp.util.LabledMatrix;
import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdtjena.HDTJenaConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class KD2GraphAssembler  extends AssemblerBase implements Assembler {



    private static boolean initialized;

    public static void init() {
        if(initialized) {
            return;
        }

        initialized = true;

        Assembler.general.implementWith(ResourceFactory.createProperty("https://dice-research.org/fuseki/KD2Graph"), new KD2GraphAssembler());
    }


    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        String file = GraphUtils.getStringValue(root, ResourceFactory.createProperty("https://dice-research.org/fuseki/fileName")) ;

        HDTSpecification spec = new HDTSpecification();
        spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

        PSFCFourSectionDictionary dict = new PSFCFourSectionDictionary(spec);
        try {

            byte[] kd2ser = GRPReader.load(file, dict);
            //read head
            ByteBuffer bb = ByteBuffer.wrap(kd2ser);
            byte[] startBytes = new byte[4];
            bb.get(startBytes);
            int startSize = ByteBuffer.wrap(startBytes).getInt();
            //2. X bytes = start Graph
            byte[] start = new byte[startSize];
            bb = bb.slice();
            bb.get(start);

            KD2TreeDeserializer desr = new KD2TreeDeserializer();
            List<LabledMatrix> matrices = desr.deserialize(start);
            System.out.println("Created "+matrices.size()+" matrices");
            KD2Graph graph = new KD2Graph(matrices, dict);
            return ModelFactory.createModelForGraph(graph);
        } catch (Exception e) {
            System.err.println("Reading KD2 file "+file+" caused following error");
            e.printStackTrace();
            throw new AssemblerException(root, "Reading KD2 file "+file+" caused following error :\n "+e.toString());
        }
    }

    static {
        init();
    }
}
