package org.dice_group.grp.fuseki;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rdfhdt.hdt.triples.TripleID;

import java.util.ArrayList;
import java.util.List;

public class MultipleKD2Graph extends GraphBase {

    private List<KD2Graph> graphs = new ArrayList<KD2Graph>();

    public void addGraph(KD2Graph graph){
        this.graphs.add(graph);
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        ExtKD2JenaIterator ret = new ExtKD2JenaIterator();
        for(KD2Graph graph : graphs){
            ret.addIterator(graph.graphBaseFind(triplePattern));
        }
        return ret;
    }
}
