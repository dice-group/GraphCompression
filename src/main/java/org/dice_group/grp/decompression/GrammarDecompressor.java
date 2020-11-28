package org.dice_group.grp.decompression;

import org.apache.jena.graph.Graph;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface GrammarDecompressor {


	public Graph decompressStart(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException, ExecutionException, InterruptedException;

	public int getStartID();
}
