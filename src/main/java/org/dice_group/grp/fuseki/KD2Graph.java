package org.dice_group.grp.fuseki;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.dice_group.grp.util.LabledMatrix;
import org.dice_group.grp.util.Point;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KD2Graph extends GraphBase {

    private final Map<Integer, LabledMatrix> predicateMap = new HashMap<Integer, LabledMatrix>();
    private final NodeDictionary dict;

    public KD2Graph(List<LabledMatrix> matrices, PSFCFourSectionDictionary dict) {
        for(LabledMatrix m : matrices){
            predicateMap.put(m.getLabelId(), m);
        }
        this.dict=new NodeDictionary(dict);
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        //cases:    ?s :p ?o -> all points
        //          ?s :p :o -> get All points with col
        //          :s :p ?o -> get All points with row
        //          :s :p :o -> get specific point (only dict)
        //          ?s ?p ?o -> get all points for all matrices
        //          ?s ?p :o -> get all points for all matrices with col
        //          :s ?p ?o -> get all points for all matrices with row
        //          :s ?p :o -> get specific point for all matrices
        TripleID ids = getTriplePatID(triplePattern);

        KD2JenaIterator it = new KD2JenaIterator(dict);
        //Node does not exist in Dict
        if(ids.getPredicate()==-1|| ids.getSubject()==-1 || ids.getObject()==-1){
            return it;
        }

        //p is variable
        if(ids.getPredicate()==0){
            for(Integer pID : predicateMap.keySet()){
                getAllPointsForMatrix(it, pID+1, Long.valueOf(ids.getSubject()).intValue(), Long.valueOf(ids.getObject()).intValue());
            }
        }
        else{
            getAllPointsForMatrix(it, Long.valueOf(ids.getPredicate()).intValue(), Long.valueOf(ids.getSubject()).intValue(), Long.valueOf(ids.getObject()).intValue());
        }
        return it;
    }

    public KD2JenaIterator getAllPointsForMatrix(KD2JenaIterator it, Integer propertyID, int row, int col){
        if(row!=0 && col!=0){
            if(predicateMap.get(propertyID-1).get(row-1, col-1)==1){
                it.add(new TripleID(row, propertyID, col));
                return it;
            }
        }
        else {
            for (Point p : predicateMap.get(propertyID-1).getPoints()) {
                if ((col == 0l || col == p.getCol()+1) && (row == 0l || row == p.getRow()+1)) {
                    it.add(new TripleID(p.getRow()+1, propertyID, p.getCol()+1));
                }
            }
        }
        return it;
    }

    public TripleID getTriplePatID(Triple jenaTriple) {
        long subject=0, predicate=0, object=0;

        if(jenaTriple.getMatchSubject()!=null) {
            subject = dict.getIntID(jenaTriple.getMatchSubject(), TripleComponentRole.SUBJECT);
        }

        if(jenaTriple.getMatchPredicate()!=null) {
            predicate = dict.getIntID(jenaTriple.getMatchPredicate(), TripleComponentRole.PREDICATE);
        }

        if(jenaTriple.getMatchObject()!=null) {
            object = dict.getIntID(jenaTriple.getMatchObject(), TripleComponentRole.OBJECT);
        }
        return new TripleID(subject, predicate, object);
    }
}
