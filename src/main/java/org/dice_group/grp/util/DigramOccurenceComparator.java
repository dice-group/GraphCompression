package org.dice_group.grp.util;

import org.dice_group.grp.grammar.digram.DigramOccurence;

import java.util.Comparator;

public class DigramOccurenceComparator implements Comparator<DigramOccurence> {

    @Override
    public int compare(DigramOccurence d1, DigramOccurence d2) {
        StringBuilder s1 = new StringBuilder(d1.getEdge1().getSubject());
        s1.append(d1.getEdge1().getObject());
        s1.append(d1.getEdge2().getSubject());
        s1.append(d1.getEdge2().getObject());
        StringBuilder s2 = new StringBuilder(d2.getEdge1().getSubject());
        s2.append(d2.getEdge1().getObject());
        s2.append(d2.getEdge2().getSubject());
        s2.append(d2.getEdge2().getObject());
        return s1.compareTo(s2);
    }
}
