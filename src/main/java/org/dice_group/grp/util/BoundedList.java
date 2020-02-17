package org.dice_group.grp.util;

import java.util.ArrayList;

public class BoundedList extends ArrayList<IndexedRDFNode> {

    public IndexedRDFNode getBounded(int inBound){
        for(IndexedRDFNode node : this){
            if(node.getLowerBound()<=inBound && node.getUpperBound()>=inBound){
                return node;
            }
        }
        return null;
    }

    public int getHighestBound() {
        int highest = -1;
        for(IndexedRDFNode node : this){
            highest = Math.max(node.getUpperBound(), highest);
        }
        return highest;
    }

}
