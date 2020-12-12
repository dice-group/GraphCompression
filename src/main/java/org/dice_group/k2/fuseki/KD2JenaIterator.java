package org.dice_group.k2.fuseki;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.NiceIterator;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.util.ArrayList;
import java.util.List;

public class KD2JenaIterator extends NiceIterator<Triple> {

    private NodeDictionary nodeDictionary;
    private List<TripleID> triples = new ArrayList<TripleID>();
    private int current=0;

    public KD2JenaIterator(NodeDictionary nD){
        this.nodeDictionary =nD;
    }

    @Override
    public boolean hasNext() {
        return current<triples.size();
    }

    public void add(TripleID t){

        triples.add(t);
    }


    @Override
    public Triple next() {

        TripleID triple = triples.get(current++);
        Triple t = new Triple(
                nodeDictionary.getNode(triple.getSubject(), TripleComponentRole.SUBJECT),
                nodeDictionary.getNode(triple.getPredicate(), TripleComponentRole.PREDICATE),
                nodeDictionary.getNode(triple.getObject(), TripleComponentRole.OBJECT)
        );

        return t;
    }

}
