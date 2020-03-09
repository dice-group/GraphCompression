package org.dice_group.grp.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class PTriple extends Triple implements Comparable<PTriple> {


    public PTriple(Triple triple){
        this(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    public PTriple(Node s, Node p, Node o) {
        super(s, p, o);
    }


    @Override
    public int compareTo(PTriple other) {
        int pc =  getPredicate().getURI().compareTo(other.getPredicate().getURI());
        if(pc!=0){
            return pc;
        }
        int sc = getSubject().getURI().compareTo(other.getSubject().getURI());
        if (sc != 0) {
            return sc;
        }
        return getObject().toString().compareTo(other.getObject().toString());
    }
}
