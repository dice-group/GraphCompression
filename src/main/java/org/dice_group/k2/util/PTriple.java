package org.dice_group.k2.util;

public class PTriple  implements Comparable<PTriple> {


    private  String subject;
    private  Integer predicate;
    private  String object;


    public PTriple(String s, Integer p, String o) {
        this.predicate = p;
        this.subject = s;
        this.object=o;
    }

    public String getSubject() {
        return subject;
    }

    public Integer getPredicate() {
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

    public void clear() {
        this.object=null;
        this.subject=null;
        this.predicate=null;
    }
}
