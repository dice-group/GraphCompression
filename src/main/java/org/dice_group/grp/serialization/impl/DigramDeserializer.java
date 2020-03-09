package org.dice_group.grp.serialization.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class DigramDeserializer {


    public int[] oneInternalStructs = new int[] {2,3,5,6,7,8,10,13,14,16,19,20,22,25,26,28,32,33,34,35};

    public int[] externalIndexOne = new int[] {0,1,2,4,5,7,8,9,13,14,15,19,22,23,25,28,29,31,33,34,35,36};
    public int[] externalIndexTwo = new int[] {0,1,2,4,5,7,10,11,13,16,17,19,20,21,25,26,27,31,32,34,36};
    public int[] externalIndexThree = new int[] {0,1,2,4,6,7,9,14,16,18,19,20,21,26,28,30,31,32,35,36};
    public int[] externalIndexFour = new int[] {0,1,3,4,5,8,10,12,13,14,15,20,22,24,25,26,27,31,32,35,36};

    //make Digram -> List<Integer[]> mapping and Digram list!
    public List<Statement> decompressRules(byte[] arr, NodeDictionary dict, int startID, List<Statement> nonTerminalEdges) throws IOException {
        if(arr.length==0){
            return new ArrayList<Statement>();
        }
        // create ntMap and queue
        Map<Integer, List<Statement>> ntMap = new HashMap<Integer, List<Statement>>();
        List<Integer> queue;
        for(Statement s : nonTerminalEdges){
            Integer e = Integer.valueOf(dict.getNode(s.getPredicate(), TripleComponentRole.PREDICATE).getURI().replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
            ntMap.putIfAbsent(e, new ArrayList<Statement>());
            ntMap.get(e).add(s);
        }
        queue = new ArrayList(ntMap.keySet());
        List<Statement> stmts = new ArrayList<Statement>();
        int j=0;
        int k=0;
        ByteBuffer bbuffer = ByteBuffer.wrap(arr);
        List<Digram> keys = new ArrayList<Digram>();
        Map<Digram, List<Integer[]>> map = new HashMap<Digram, List<Integer[]>>();
        do {

            //1. read e1 INT * -1
            Integer e1 = bbuffer.getInt() * -1;
            //2. read e2 INT
            Integer e2 = bbuffer.getInt();
            // 3.1. read flags
            // 3.2. read externals
            byte flags = bbuffer.get();
            // 1XYY EXT1 EXT2
            // YY STRUCT
            byte structure = Integer.valueOf((flags & 63)).byteValue();
            byte size = Integer.valueOf((flags & -64) >> 6).byteValue();
            if (size < 0) {
                size *= -1;
            }

            byte internalFlag = 1;
            for (int i : oneInternalStructs) {
                if (i == structure) {
                    internalFlag = 0;
                    break;
                }
            }
            Set<Integer> externals = getExternals(structure);


            //ext1 and ext2 ->

            //4. read arr until leading bit is 1 (that is start of next digram)
            boolean nextDigram = false;
            List<Integer[]> internals = new LinkedList<Integer[]>();
            while (!nextDigram && bbuffer.hasRemaining()) {
                byte next = bbuffer.get();
                bbuffer.position(bbuffer.position() - 1);

                if (next < 0) {
                    nextDigram = true;
                    break;
                }
                internals.add(getNextInternals(bbuffer, size, internalFlag));
            }

            //here we just create the stamtents according to the structure flag

            Digram d = new Digram(e1, e2, externals);
            d.setStructure(structure);
            keys.add(d);
            map.put(d, internals);




        }while(bbuffer.hasRemaining());
        //TODO for each encountance add to ntMap and queue
        Collections.sort(queue, Collections.reverseOrder());
        for(int y=0; y<queue.size();y++) {

            Integer key = queue.get(y);
            if(key==28){
                System.out.println();
            }

            Digram d = keys.get(key);
            String ntIndex1 = dict.getNode(d.getEdgeLabel1(), TripleComponentRole.PREDICATE).getURI();
            String ntIndex2 = dict.getNode(d.getEdgeLabel2(), TripleComponentRole.PREDICATE).getURI();
            Integer index1 = null;
            Integer index2 = null;
            //check if we have  nt edges
            if (ntIndex1.startsWith("http://n.")) {
                index1 = Integer.valueOf(ntIndex1.replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                ntMap.putIfAbsent(index1, new ArrayList<Statement>());
                if(!queue.contains(index1)) {
                    queue.add(y + 1, index1);
                    Collections.sort(queue, Collections.reverseOrder());
                }
            }
            if (ntIndex2.startsWith("http://n.")) {
                index2 = Integer.valueOf(ntIndex2.replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                ntMap.putIfAbsent(index2, new ArrayList<Statement>());
                if(!queue.contains(index2)) {
                    queue.add(y + 1, index2);
                    Collections.sort(queue, Collections.reverseOrder());
                }
            }
            List<Integer[]> internals = map.get(d);
            int x=0;

            Collections.sort(ntMap.get(key), new Comparator<Statement>() {
                @Override
                public int compare(org.dice_group.grp.grammar.Statement s1, org.dice_group.grp.grammar.Statement s2) {
                    Integer nt1= Integer.valueOf(dict.getNode(s1.getPredicate(), TripleComponentRole.PREDICATE).getURI().replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                    Integer nt2= Integer.valueOf(dict.getNode(s2.getPredicate(), TripleComponentRole.PREDICATE).getURI().replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
                    int pCT =nt1.compareTo(nt2);
                    if(pCT!=0){
                        return pCT;
                    }
                    int sCT = s1.getSubject().compareTo(s2.getSubject());
                    if(sCT!=0){
                        return sCT;
                    }
                    int oCT = s1.getObject().compareTo(s2.getObject());
                    return oCT;
                }
            });

            for(Statement ntEdge : ntMap.get(key)){

            //}
            //for (Integer[] internal : internals) {
                // k states the order


               // Statement ntEdge = null;
                //try {
                //    ntEdge = ntMap.get(key).get(x++);
                //} catch (IndexOutOfBoundsException e) {
                //    e.printStackTrace();
                //}

                List<Integer> ext = new ArrayList<Integer>();

                ext.add(ntEdge.getSubject());

                if (ntEdge.getSubject() != ntEdge.getObject()) {
                    ext.add(ntEdge.getObject());
                }
                DigramOccurence occ = null;
                List<Integer> ints = new ArrayList<Integer>();
                if(!internals.isEmpty()) {
                    Integer[] internal = internals.get(x++);
                    for (Integer i : internal) {
                        ints.add(i);
                    }
                }
                try {

                    occ = d.createOccurence(ext, ints);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //if edge is nt edge. add statement to edge, otherwise add to edges we can add
                if (index1 !=null) {
                    Statement st = new Statement(occ.getEdge1().getSubject(), d.getEdgeLabel1(), occ.getEdge1().getObject());
                    ntMap.get(index1).add(st);

                }else{
                    stmts.add(occ.getEdge1());
                }
                if (index2 !=null) {
                    Statement st = new Statement(occ.getEdge2().getSubject(), d.getEdgeLabel2(), occ.getEdge2().getObject());
                    ntMap.get(index2).add(st);
                }else{
                    stmts.add(occ.getEdge2());
                }
            }

        }

        return stmts;
    }


    private Set<Integer> getExternals(byte struct) {
        Set<Integer> ret = new HashSet<Integer>();
        if(ArrayUtils.contains(externalIndexOne, struct))
            ret.add(0);
        if(ArrayUtils.contains(externalIndexTwo, struct))
            ret.add(1);
        if(ArrayUtils.contains(externalIndexThree, struct))
            ret.add(2);
        if(ArrayUtils.contains(externalIndexFour, struct))
            ret.add(3);
        return ret;
    }



    private byte getInternalFlag(byte struct) {
        if(struct == 0 || struct == 1 || struct == 4 || struct == 31) {
            return 0;
        }
        else if(ArrayUtils.contains(oneInternalStructs, struct)) {
            return 1;
        }
        return 2;
    }



    private Integer[] getNextInternals(ByteBuffer bbuffer, byte classFlag, byte internalFlag) {
        Integer[] ret = new Integer[internalFlag+1];
        for(int i=0;i<internalFlag+1;i++) {
            if(classFlag==0) {
                //byte
                ret[i] = Integer.valueOf(bbuffer.get());
            }
            if(classFlag==1) {
                //short
                ret[i] = Integer.valueOf(bbuffer.getShort());
            }
            if(classFlag==2) {
                //int
                ret[i] = bbuffer.getInt();
            }
            if(classFlag==3) {
                //long
                //TODO use long instead of integer
                ret[i] = Long.valueOf(bbuffer.getLong()).intValue();
            }
        }
        return ret;
    }

}
