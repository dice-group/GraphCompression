package org.dice_group.k2.compression.impl;

import org.dice_group.k2.compression.AbstractGrammarCompressor;
import org.dice_group.k2.exceptions.NotSupportedException;
import org.dice_group.k2.serialization.impl.ThreadedKD2PPSerializer;
import org.dice_group.k2.serialization.impl.ThreadedKD2TreeSerializer;

import java.io.FileOutputStream;
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
            serializer = new ThreadedKD2PPSerializer(cores, predicates);
        }
        else{
            serializer = new ThreadedKD2PPSerializer(1, predicates);
        }
    }


    public ThreadedKD2TreeSerializer getSerializer(){
        return this.serializer;
    }

    @Override
    public void compress(long sSize, long oSize) throws NotSupportedException, IOException, ExecutionException, InterruptedException {
            serializer.serialize(sSize, oSize);
    }

    @Override
    public void setOutStream(FileOutputStream faos) {
        serializer.setOutStream(faos);
    }
}
