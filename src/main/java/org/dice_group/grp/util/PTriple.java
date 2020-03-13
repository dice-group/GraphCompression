package org.dice_group.grp.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class PTriple  implements Comparable<PTriple> {


    private final String subject;



    private final String predicate;
    private final String object;

    public PTriple(String s, String p, String o) {
        this.predicate = p;
        this.subject = s;
        this.object=o;
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }


    @Override
    public int compareTo(PTriple other) {
        int pc =  getPredicate().compareTo(other.getPredicate());
        if(pc!=0){
            return pc;
        }
        int sc = getSubject().compareTo(other.getSubject());
        if (sc != 0) {
            return sc;
        }
        return getObject().toString().compareTo(other.getObject().toString());
    }
}
