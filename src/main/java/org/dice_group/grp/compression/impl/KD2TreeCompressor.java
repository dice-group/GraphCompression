package org.dice_group.grp.compression.impl;

import grph.Grph;
import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.serialization.impl.KD2TreeSerializer;
import org.dice_group.grp.util.BoundedList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KD2TreeCompressor implements GrammarCompressor {

    DigramSerializerImpl digramSerializer;

    @Override
    public byte[][] compress(Grammar grammar) throws NotSupportedException, IOException {
        digramSerializer = new DigramSerializerImpl(grammar);
        byte[] start = compress(grammar.getStart(), grammar.getProps());
        byte[] rules = serializeRules(grammar);
        //byte[] serialized = new byte[start.length+1+rules.length];
        byte[] startSize = ByteBuffer.allocate(Integer.BYTES).putInt(start.length).array();

        System.out.println("Start graph size: "+start.length+" bytes");
        System.out.println("Rules size: "+rules.length+" bytes");
        return new byte[][] {startSize, start, rules};
    }

    @Override
    public byte[] compress(Grph g, BoundedList pIndex) throws NotSupportedException, IOException {
        KD2TreeSerializer serializer = new KD2TreeSerializer();
        return serializer.serialize(g, pIndex);
    }

    @Override
    public byte[] serializeRules(Grammar grammar) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<Integer, Digram> rules = grammar.getRules();
        List<Digram> digrams = new ArrayList<Digram>();
        //add digrams in order of the Non Terminals, thus it is reversable without saving NT
        for(Integer i : rules.keySet()) {
            digrams.add(rules.get(i));
        }
        for(Digram digram : digrams) {
            byte[] serRule = digramSerializer.serialize(digram);
            baos.write(serRule);
        }
        return baos.toByteArray();
    }
}
