package org.dice_group.grp.decompression.rdf;

import org.apache.jena.rdf.model.*;
import org.dice_group.grp.decompression.GRPReader;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.index.impl.URIBasedSearcher;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.IOException;
import java.util.*;

public class RDFDecompressor {

    public Model decompress(String file) {
        HDTSpecification spec = new HDTSpecification();
//		spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

        PSFCFourSectionDictionary dict = new PSFCFourSectionDictionary(spec);
/*
        try {
            Map<Digram, List<Integer[]>> internalMap = new HashMap<Digram, List<Integer[]>>();
            Grammar g = GRPReader.load(file, dict, internalMap);
            URIBasedSearcher searcher = new URIBasedSearcher(dict);
            searcher.deindexGrammar(g);
            Map<Digram, List<List<RDFNode>>> realMap = searcher.deindexInternalMap(internalMap);
            return decompressGrammar(g, realMap);
        } catch (NotSupportedException | IOException e) {
            e.printStackTrace();
        }
        */

        return null;
    }

    /**
     * iterates through the grammar's rules and decompresses the statements
     * pertinent to each digram
     *
     * @param grammar
     * @param realMap
     * @return
     */
    public Model decompressGrammar(Grammar grammar, Map<Digram, List<List<RDFNode>>> realMap) {
       /* Model graph = ModelFactory.createDefaultModel();
        graph.add(grammar.getStart());

        Map<String, Digram> rules = grammar.getRules();
        rules.forEach((uriNT, digram) -> {
            replaceStmts(uriNT, digram, graph, realMap.get(digram));
        });*/
        return null;
    }

    /**
     * substitutes the compressed statement with the original statements
     *
     */
    private void replaceStmts(String uriNT, Digram digram, Model graph, List<List<RDFNode>> internals) {
        List<Statement> stmts = graph.listStatements(null, ResourceFactory.createProperty(uriNT), (RDFNode) null)
                .toList();
        // sort stmts after first and second node alphabetically
        Collections.sort(stmts, new Comparator<Statement>() {

            @Override
            public int compare(Statement arg0, Statement arg1) {
                String e1 = arg0.getSubject().toString() + arg0.getObject().toString();
                String e2 = arg1.getSubject().toString() + arg1.getObject().toString();
                return e1.compareTo(e2);
            }

        });
        for (int i = 0; i < stmts.size(); i++) {
            List<RDFNode> externals = new LinkedList<RDFNode>();
            externals.add(stmts.get(i).getSubject());
            externals.add(stmts.get(i).getObject());
            //DigramOccurence occ = digram.createOccurence(externals, internals.get(i));
            //graph.remove(stmts.get(i));
            //graph.add(occ.getEdge1());
            //graph.add(occ.getEdge2());
        }
    }

}
