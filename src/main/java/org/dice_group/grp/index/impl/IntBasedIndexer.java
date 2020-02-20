package org.dice_group.grp.index.impl;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.apache.jena.base.Sys;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.Statement;
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

import java.util.*;

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
    public List<Statement> indexGraph(Grph graph,BoundedList pIndex, List<RDFNode> soIndex) {
        List<Statement> stmts = new ArrayList<Statement>();
        List<String> nts  = new ArrayList<String>();
        for(IndexedRDFNode node : pIndex){
            node.setHdtIndex(Long.valueOf(dict.stringToId(NodeDictionary.nodeToStr(node.getRDFNode().asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.PREDICATE)).intValue());
        }
        //Grph graphC = new InMemoryGrph();
        for (int edge : graph.getEdges()) {
            int s = graph.getDirectedSimpleEdgeTail(edge);
            int o = graph.getDirectedSimpleEdgeHead(edge);

            Long s1 = dict.stringToId(NodeDictionary.nodeToStr(soIndex.get(s).asNode()), TripleComponentRole.SUBJECT);
            Long o1;
            if(soIndex.get(o).isLiteral()){
                o1 = dict.stringToId(NodeDictionary.nodeToStr(soIndex.get(o).asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
            }
            else {
                o1 = dict.stringToId(NodeDictionary.nodeToStr(soIndex.get(o).asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
            }
            if(s1 == -1 || o1 == -1){
                System.out.println("FUUUCK");
            }
            //Long o1 = dict.stringToId(JenaNodeFormatter.format(soIndex.get(o)), TripleComponentRole.SUBJECT);
            String pr = NodeDictionary.nodeToStr((pIndex.getBounded(edge).getRDFNode().asNode())).replace("\"", "\\\"").trim();
            if(pr.startsWith("http://n.")){
                nts.add(pr);
            }
            Long p = dict.stringToId(NodeDictionary.nodeToStr((pIndex.getBounded(edge).getRDFNode().asNode())), TripleComponentRole.PREDICATE);
            pIndex.getBounded(edge).setHdtIndex(p.intValue());
            graph.removeEdge(edge);

            //FUUUUUUUUUUCK, we cannot just use p.
            stmts.add(new Statement(s1.intValue(), p.intValue(), o1.intValue()));
        }
        this.nodeDict = new NodeDictionary(dict);
        return stmts;
    }

    private void tmpIndexGraph(Grph graph, BoundedList pIndex, List<RDFNode> soIndex) {
        for(IndexedRDFNode node : pIndex){

            tmpDict.insert(NodeDictionary.nodeToStr(node.getRDFNode().asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.PREDICATE);
        }
        for (int edge : graph.getEdges()) {

            int s = graph.getDirectedSimpleEdgeTail(edge);
            int o = graph.getDirectedSimpleEdgeHead(edge);

            tmpDict.insert(NodeDictionary.nodeToStr(soIndex.get(s).asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
            if(soIndex.get(o).isLiteral()){
                tmpDict.insert(NodeDictionary.nodeToStr(soIndex.get(o).asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
            }
            else {
                tmpDict.insert(NodeDictionary.nodeToStr(soIndex.get(o).asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
            }
            tmpDict.insert(NodeDictionary.nodeToStr(pIndex.getBounded(edge).getRDFNode().asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.PREDICATE);

        }
    }


    public Node getNodeFromID(int id, TripleComponentRole role) {
        return this.nodeDict.getNode(id, role);

    }

    public Grammar indexGrammar(Grammar grammar) {
        // 1. tmpIndex everything

        tmpIndexGraph(grammar.getStart(), grammar.getProps(), grammar.getSOIndex());
        List<Integer> keys = new ArrayList<Integer>();
        for (Integer key : grammar.getRules().keySet()) {
            keys.add(key);
        }
        Collections.sort(keys);
        for (Integer key : keys) {
            Digram d = grammar.getRules().get(key);
            tmpIndexDigrams(d, grammar.getReplaced().get(d), grammar.getProps(), grammar.getSOIndex());
        }
        int size=0;
        for(Digram dod : grammar.getReplaced().keySet()){
            size+=grammar.getReplaced().get(dod).size();
        }

        // 2. reorganize
        HDTSpecification spec = new HDTSpecification();
        spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);
        dict = DictionaryFactory.createDictionary(spec);
        ProgressListener listener = new ProgressOut();
        tmpDict.reorganize();
        dict.load(new PSFCTempDictionary(tmpDict), listener);
        // 3. replace
        List<Statement> stmts = indexGraph(grammar.getStart(), grammar.getProps(), grammar.getSOIndex());
        grammar.setStmts(stmts);
        Map<Digram, List<DigramOccurence>> realMap = new HashMap<Digram, List<DigramOccurence>>();
        for (Integer key : grammar.getRules().keySet()) {
            Digram digram = grammar.getRules().get(key);

            List<DigramOccurence> tmp = grammar.getReplaced().get(digram);
            digram = this.indexDigram(digram, grammar.getProps());
            tmp = indexDigramOcc(digram, tmp, grammar.getSOIndex());
            realMap.put(digram, tmp);
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
                //Long s = dict.stringToId(JenaNodeFormatter.format(soIndex.get(extNode)), TripleComponentRole.SUBJECT);
                //ext.add(s.intValue());
                Long o1;
                RDFNode index = soIndex.get(extNode);
                if(index.isLiteral()){
                    o1 = dict.stringToId(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                else {
                    o1 = dict.stringToId(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                ext.add(o1.intValue());
            }
            for (Integer intNode : occ.getInternals()) {
                //Long s = dict.stringToId(JenaNodeFormatter.format(soIndex.get(intNode)), TripleComponentRole.SUBJECT);
                //internals.add(s.intValue());

                Long o1;
                RDFNode index = soIndex.get(intNode);
                if(index.isLiteral()){
                    o1 = dict.stringToId(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                else {
                    o1 = dict.stringToId(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                internals.add(o1.intValue());
            }
            ret.add(digram.createOccurence(ext, internals));
        }
        return ret;
    }

    private void tmpIndexDigrams(Digram digram, List<DigramOccurence> occs, BoundedList pIndex, List<RDFNode> soIndex) {
        tmpDict.insert(NodeDictionary.nodeToStr(pIndex.getBounded(digram.getEdgeLabel1()).getRDFNode().asNode()), TripleComponentRole.PREDICATE);
        tmpDict.insert(NodeDictionary.nodeToStr(pIndex.getBounded(digram.getEdgeLabel2()).getRDFNode().asNode()), TripleComponentRole.PREDICATE);

        // internals (use OBJECT)
        for (DigramOccurence occ : occs) {
            for (Integer n : occ.getInternals()) {
                RDFNode index = soIndex.get(n);
                if(index.isLiteral()){

                    tmpDict.insert(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                else {
                    tmpDict.insert(NodeDictionary.nodeToStr(index.asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.SUBJECT);
                }
                //tmpDict.insert(JenaNodeFormatter.format(soIndex.get(n)), TripleComponentRole.SUBJECT);
            }
        }
    }

    private Digram indexDigram(Digram digram, BoundedList pIndex) {
        Long el1 = dict.stringToId(NodeDictionary.nodeToStr(pIndex.getBounded(digram.getEdgeLabel1()).getRDFNode().asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.PREDICATE);
        Long el2 = dict.stringToId(NodeDictionary.nodeToStr(pIndex.getBounded(digram.getEdgeLabel2()).getRDFNode().asNode()).replace("\"", "\\\"").trim(), TripleComponentRole.PREDICATE);
        pIndex.getBounded(digram.getEdgeLabel1()).setHdtIndex(el1.intValue());
        pIndex.getBounded(digram.getEdgeLabel1()).setHdtIndex(el2.intValue());
        digram.setEdgeLabel1(el1.intValue());
        digram.setEdgeLabel2(el2.intValue());
        return digram;
    }

}
