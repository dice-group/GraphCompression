package org.dice_group.grp.compression;

import org.apache.jena.rdf.model.RDFNode;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.util.IndexedRDFNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public abstract  class AbstractGrammarCompressor implements GrammarCompressor {

    protected DigramSerializerImpl digramSerializer;



    @Override
    public byte[][] compress(Grammar grammar) throws NotSupportedException, IOException {
        digramSerializer = new DigramSerializerImpl(grammar);
        Collections.sort(grammar.getStmts(), new Comparator<Statement>() {
            @Override
            public int compare(Statement s1, Statement s2) {
                int sc = s1.getSubject().compareTo(s2.getSubject());
                if(sc!=0){
                    return sc;
                }
                int oc = s1.getObject().compareTo(s2.getObject());
                if(oc!=0){
                    return oc;
                }
                return  s1.getPredicate().compareTo(s2.getPredicate());
            }
        });
        byte[] start = compress(grammar.getStmts(), grammar.getStart(), grammar.getProps());
        byte[] rules = serializeRules(grammar);
        byte[] serialized = new byte[start.length+1+rules.length];
        byte[] startSize = ByteBuffer.allocate(Integer.BYTES).putInt(start.length).array();
//		System.arraycopy(startSize, 0, serialized, 0,Integer.BYTES);
//		System.arraycopy(start, 0, serialized, Integer.BYTES,start.length);
//		System.arraycopy(rules, 0, serialized, Integer.BYTES+start.length, rules.length);;
        System.out.println("Start graph size: "+start.length+" bytes");
        System.out.println("Rules size: "+rules.length+" bytes");
        return new byte[][] {startSize, start, rules};
    }


    /*
     *
     *
     */
    @Override
    public byte[] serializeRules(Grammar grammar) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<Integer, Digram> rules = grammar.getRules();
        List<Integer> ruleOrdered = new ArrayList<Integer>();
        Map<Integer, Integer> r2o = new HashMap<Integer, Integer>();
        List<Digram> digrams = new ArrayList<Digram>();
        //add digrams in order of the Non Terminals, thus it is reversable without saving NT
        //TODO
        for(Integer i : rules.keySet()) {
            IndexedRDFNode iNode = grammar.getProps().getBounded(i);
            RDFNode node = grammar.getProps().getBounded(i).getRDFNode();
            String uri =node.asResource().getURI();
            String intStr =  uri.replace(GrammarHelper.NON_TERMINAL_PREFIX, "");
            try {

                Integer nt = Integer.valueOf(intStr);
                ruleOrdered.add(nt);
                r2o.put(nt, i);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println();
            }
        }
        Collections.sort(ruleOrdered);
        for(Integer i : ruleOrdered){
            digrams.add(rules.get(r2o.get(i)));
        }

        for(Digram digram : digrams) {
            byte[] serRule = digramSerializer.serialize(digram);
            baos.write(serRule);
        }
        return baos.toByteArray();
    }


}
