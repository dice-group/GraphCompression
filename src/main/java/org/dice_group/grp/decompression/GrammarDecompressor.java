package org.dice_group.grp.decompression;

import java.io.IOException;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.rdfhdt.hdtjena.NodeDictionary;

public interface GrammarDecompressor {

	
	public Grammar decompress(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException;

	
	public Map<String, Digram> decompressRules(byte[] arr, Grammar g) throws IOException;

	public Model decompressStart(byte[] arr, NodeDictionary dict) throws NotSupportedException, IOException;
}
