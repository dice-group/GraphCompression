package org.dice_group.grp.index.impl;

import grph.Grph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Indexer;
import org.dice_group.grp.util.BoundedList;
import org.dice_group.grp.util.IndexedRDFNode;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCTempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntBasedIndexer {

    private TempDictionary tmpDict;
    private NodeDictionary nodeDict;
    private DictionaryPrivate dict;

    public IntBasedIndexer(TempDictionary tmpDict) {
        this.tmpDict = tmpDict;

    }

    public DictionaryPrivate getDict() {
        return dict;
    }

    /*
     *
     */
    public Grph indexGraph(Grph graph,BoundedList pIndex, List<RDFNode> soIndex) {
        for(IndexedRDFNode node : pIndex){
            node.setHdtIndex(Long.valueOf(dict.stringToId(JenaNodeFormatter.format(node.getRDFNode()), TripleComponentRole.PREDICATE)).intValue());
        }
        for (int edge : graph.getEdges()) {
            int s = graph.getDirectedSimpleEdgeTail(edge);
            int o = graph.getDirectedSimpleEdgeHead(edge);
            Long s1 = dict.stringToId(JenaNodeFormatter.format(soIndex.get(s)), TripleComponentRole.SUBJECT);
            Long o1;
            if(soIndex.get(o).isLiteral()){
                o1 = dict.stringToId(JenaNodeFormatter.format(soIndex.get(o)).replaceAll("[^a-zA-Z0-9\\s@]", " ").trim(), TripleComponentRole.SUBJECT);
            }
            else {
                o1 = dict.stringToId(JenaNodeFormatter.format(soIndex.get(o)), TripleComponentRole.SUBJECT);
            }
            //Long o1 = dict.stringToId(JenaNodeFormatter.format(soIndex.get(o)), TripleComponentRole.SUBJECT);
            Long p = dict.stringToId(JenaNodeFormatter.format(pIndex.getBounded(edge).getRDFNode()), TripleComponentRole.PREDICATE);
            pIndex.getBounded(edge).setHdtIndex(p.intValue());
            graph.removeEdge(edge);
            if(s1==-1 || o1 ==-1 || p==-1){
                RDFNode n1 = soIndex.get(o);
                RDFNode n2 = soIndex.get(s);
                String hmm =JenaNodeFormatter.format(soIndex.get(o)).replaceAll("[^a-zA-Z0-9\\s@]", " ").trim();
                System.out.println("GNARF");
            }
            graph.addSimpleEdge(s1.intValue(), p.intValue(), o1.intValue(), true);
        }
        this.nodeDict = new NodeDictionary(dict);
        return graph;
    }

    private void tmpIndexGraph(Grph graph, BoundedList pIndex, List<RDFNode> soIndex) {
        for(IndexedRDFNode node : pIndex){
            tmpDict.insert(JenaNodeFormatter.format(node.getRDFNode()), TripleComponentRole.PREDICATE);
        }
        for (int edge : graph.getEdges()) {

            int s = graph.getDirectedSimpleEdgeTail(edge);
            int o = graph.getDirectedSimpleEdgeHead(edge);

            tmpDict.insert(JenaNodeFormatter.format(soIndex.get(s)), TripleComponentRole.SUBJECT);
            if(soIndex.get(o).isLiteral()){
                tmpDict.insert(JenaNodeFormatter.format(soIndex.get(o)).replaceAll("[^a-zA-Z0-9\\s@]", " ").trim(), TripleComponentRole.SUBJECT);
            }
            else {
                tmpDict.insert(JenaNodeFormatter.format(soIndex.get(o)), TripleComponentRole.SUBJECT);
            }
            tmpDict.insert(JenaNodeFormatter.format(pIndex.getBounded(edge).getRDFNode()), TripleComponentRole.PREDICATE);

        }
    }


    public Node getNodeFromID(int id, TripleComponentRole role) {
        return this.nodeDict.getNode(id, role);

    }

    public Grammar indexGrammar(Grammar grammar) {
        // 1. tmpIndex everything

        tmpIndexGraph(grammar.getStart(), grammar.getProps(), grammar.getSOIndex());
        for (Integer key : grammar.getRules().keySet()) {
            Digram d = grammar.getRules().get(key);
            tmpIndexDigrams(d, grammar.getReplaced().get(d), grammar.getProps(), grammar.getSOIndex());
        }
        // 2. reorganize
        HDTSpecification spec = new HDTSpecification();
        spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);
        dict = DictionaryFactory.createDictionary(spec);
        ProgressListener listener = new ProgressOut();
        tmpDict.reorganize();
        dict.load(new PSFCTempDictionary(tmpDict), listener);
        // 3. replace
        indexGraph(grammar.getStart(), grammar.getProps(), grammar.getSOIndex());
        Map<Digram, List<DigramOccurence>> realMap = new HashMap<Digram, List<DigramOccurence>>();
        for (Integer key : grammar.getRules().keySet()) {
            Digram digram = grammar.getRules().get(key);

            List<DigramOccurence> tmp = grammar.getReplaced().get(digram);
            digram = this.indexDigram(digram, grammar.getProps());
            tmp = indexDigramOcc(digram, tmp, grammar.getSOIndex());
            realMap.put(digram, tmp);
            // TODO index occ too
            // overwrite old graph with indexed graph
            grammar.getRules().put(key, digram);
        }
        grammar.setReplaced(realMap);
        return grammar;
    }

    private List<DigramOccurence> indexDigramOcc(Digram digram, List<DigramOccurence> tmp, List<RDFNode> soIndex) {
        List<DigramOccurence> ret = new ArrayList<DigramOccurence>();
        for (DigramOccurence occ : tmp) {
            List<Integer> ext = new ArrayList<Integer>();
            List<Integer> internals = new ArrayList<Integer>();
            for (Integer extNode : occ.getExternals()) {
                //TODO it is not nec. a subject :/ Literals can be externals
                Long s = dict.stringToId(JenaNodeFormatter.format(soIndex.get(extNode)), TripleComponentRole.SUBJECT);
                ext.add(s.intValue());
            }
            for (Integer intNode : occ.getInternals()) {
                Long s = dict.stringToId(JenaNodeFormatter.format(soIndex.get(intNode)), TripleComponentRole.SUBJECT);
                internals.add(s.intValue());
            }
            ret.add(digram.createOccurence(ext, internals));
        }
        return ret;
    }

    private void tmpIndexDigrams(Digram digram, List<DigramOccurence> occs, BoundedList pIndex, List<RDFNode> soIndex) {
        tmpDict.insert(JenaNodeFormatter.format(pIndex.getBounded(digram.getEdgeLabel1()).getRDFNode()), TripleComponentRole.PREDICATE);
        tmpDict.insert(JenaNodeFormatter.format(pIndex.getBounded(digram.getEdgeLabel2()).getRDFNode()), TripleComponentRole.PREDICATE);

        // internals (use OBJECT)
        for (DigramOccurence occ : occs) {
            for (Integer n : occ.getInternals()) {
                tmpDict.insert(JenaNodeFormatter.format(soIndex.get(n)), TripleComponentRole.SUBJECT);
            }
        }
    }

    private Digram indexDigram(Digram digram, BoundedList pIndex) {
        Long el1 = dict.stringToId(JenaNodeFormatter.format(pIndex.getBounded(digram.getEdgeLabel1()).getRDFNode()), TripleComponentRole.PREDICATE);
        Long el2 = dict.stringToId(JenaNodeFormatter.format(pIndex.getBounded(digram.getEdgeLabel2()).getRDFNode()), TripleComponentRole.PREDICATE);
        pIndex.getBounded(digram.getEdgeLabel1()).setHdtIndex(el1.intValue());
        pIndex.getBounded(digram.getEdgeLabel1()).setHdtIndex(el2.intValue());
        digram.setEdgeLabel1(el1.intValue());
        digram.setEdgeLabel2(el2.intValue());
        return digram;
    }

}
