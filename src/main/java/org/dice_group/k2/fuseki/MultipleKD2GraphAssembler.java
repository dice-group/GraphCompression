package org.dice_group.k2.fuseki;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.dice_group.k2.decompression.GRPReader;
import org.dice_group.k2.serialization.impl.KD2PPDeserializer;
import org.dice_group.k2.serialization.impl.KD2TreeDeserializer;
import org.dice_group.k2.util.LabledMatrix;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class MultipleKD2GraphAssembler   extends AssemblerBase implements Assembler {



    private static boolean initialized;

    public static void init() {
        if(initialized) {
            return;
        }

        initialized = true;

        Assembler.general.implementWith(ResourceFactory.createProperty("https://dice-research.org/fuseki/MultipleKD2Graph"), new MultipleKD2GraphAssembler());
    }


    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        String folder = GraphUtils.getStringValue(root, ResourceFactory.createProperty("https://dice-research.org/fuseki/folder"));
        File dir = new File(folder);
        MultipleKD2Graph mgraph = new MultipleKD2Graph();
        for (File file : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(s.endsWith(".dict")){
                    return false;
                }
                return true;
            }
        })) {
            HDTSpecification spec = new HDTSpecification();
            spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

            PSFCFourSectionDictionary dict = new PSFCFourSectionDictionary(spec);
            try {

                byte[] kd2ser = GRPReader.load(file.getAbsolutePath(), dict);


                KD2PPDeserializer desr = new KD2PPDeserializer();
                List<LabledMatrix> matrices = desr.deserialize(kd2ser);
                System.out.println("Created " + matrices.size() + " matrices");
                KD2Graph graph = new KD2Graph(matrices, dict);
                mgraph.addGraph(graph);
            } catch (Exception e) {
                System.err.println("Reading KD2 file " + file + " caused following error");
                e.printStackTrace();
                throw new AssemblerException(root, "Reading KD2 file " + file + " caused following error :\n " + e.toString());
            }
        }
        return ModelFactory.createModelForGraph(mgraph);
    }


    static {
        init();
    }

}
