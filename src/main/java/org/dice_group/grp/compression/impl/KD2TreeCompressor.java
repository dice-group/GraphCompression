package org.dice_group.grp.compression.impl;

import org.dice_group.grp.compression.AbstractGrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.serialization.impl.ThreadedKD2TreeSerializer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class KD2TreeCompressor extends AbstractGrammarCompressor {

    private ThreadedKD2TreeSerializer serializer;
    private boolean threaded;

    public KD2TreeCompressor(int predicates){
        this(false, predicates);
    }


    public KD2TreeCompressor(boolean threaded, int predicates){
        this.threaded = threaded;
        if(threaded) {
            int cores = Runtime.getRuntime().availableProcessors();
            serializer = new ThreadedKD2TreeSerializer(cores, predicates);
        }
        else{
            serializer = new ThreadedKD2TreeSerializer(1, predicates);
        }
    }


    public ThreadedKD2TreeSerializer getSerializer(){
        return this.serializer;
    }

    @Override
    public byte[] compress(long sSize, long oSize) throws NotSupportedException, IOException, ExecutionException, InterruptedException {
            return serializer.serialize(sSize, oSize);
    }
}
