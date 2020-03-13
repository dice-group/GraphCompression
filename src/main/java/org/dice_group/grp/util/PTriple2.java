package org.dice_group.grp.util;

public class PTriple2 implements Comparable<PTriple2> {


    private final Integer subject;



    private final String predicate;
    private final Integer object;

    public PTriple2(Integer s, String p, Integer o) {
        this.predicate = p;
        this.subject = s;
        this.object=o;
    }

    public Integer getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public Integer getObject() {
        return object;
    }


    @Override
    public int compareTo(PTriple2 other) {
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
