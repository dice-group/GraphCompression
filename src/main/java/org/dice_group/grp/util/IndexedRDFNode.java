package org.dice_group.grp.util;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.tdb.index.Index;

public class IndexedRDFNode {

    private int lowerBound=0;
    private int upperBound=0;
    private RDFNode node;


    private Integer hdtIndex;

    public void setLowerBound(int lowerBound){
        this.lowerBound = lowerBound;
    }
    public void setUpperBound(int upperBound){
        this.upperBound = upperBound;
    }
    public void setRDFNode(RDFNode node){
        this.node=node;
    }

    public int getLowerBound(){
        return this.lowerBound;
    }

    public int getUpperBound(){
        return this.upperBound;
    }

    public RDFNode getRDFNode(){
        return node;
    }

    public Integer getHdtIndex() {
        return hdtIndex;
    }

    public void setHdtIndex(Integer hdtIndex) {
        this.hdtIndex = hdtIndex;
    }
    @Override
    public int hashCode(){
        return node.toString().hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o != null && o instanceof IndexedRDFNode){
            return node.toString().equals(((IndexedRDFNode)o).getRDFNode().toString());
        }
        return false;
    }
}
