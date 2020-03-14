package org.dice_group.grp.fuseki;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.TripleID;

import java.util.ArrayList;
import java.util.List;

public class ExtKD2JenaIterator extends NiceIterator<Triple> {

    private List<ExtendedIterator<Triple>> its = new ArrayList<ExtendedIterator<Triple>>();

    private int currentIt=0;

    public void addIterator(ExtendedIterator<Triple> it ){
        its.add(it);
    }

    @Override
    public boolean hasNext(){
        if(its.size()>0 && its.size()>currentIt){
            if(its.get(currentIt).hasNext()){
                return true;
            }
            currentIt++;
            return hasNext();
        }
        return false;
    }


    @Override
    public Triple next() {

        return its.get(currentIt).next();

    }


}
