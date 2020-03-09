package org.dice_group.grp.compression.impl;

import grph.Grph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.dice_group.grp.compression.AbstractGrammarCompressor;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.serialization.impl.KD2TreeSerializer;
import org.dice_group.grp.serialization.impl.ThreadedKD2TreeSerializer;
import org.dice_group.grp.util.BoundedList;
import org.dice_group.grp.util.IndexedRDFNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class KD2TreeCompressor extends AbstractGrammarCompressor {

    private boolean threaded;

    public KD2TreeCompressor(){
        this(false);
    }

    public KD2TreeCompressor(boolean threaded){
        this.threaded = threaded;
    }


    @Override
    public byte[] compress(List<Statement> stmts, int vSize) throws NotSupportedException, IOException, ExecutionException, InterruptedException {
        if(threaded) {
            ThreadedKD2TreeSerializer serializer = new ThreadedKD2TreeSerializer();
            int cores = Runtime.getRuntime().availableProcessors();
            return serializer.serialize(stmts, vSize, cores);
        }else{
            KD2TreeSerializer serializer = new KD2TreeSerializer();
            return serializer.serialize(stmts, vSize);
        }
    }


}
