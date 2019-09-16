package org.dice_group.grp.decompression;

import java.io.IOException;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;

public interface GrammarDecompressor {

	
	public Grammar decompress(byte[] arr) throws NotSupportedException, IOException;

	public Model decompressStart(byte[] arr) throws NotSupportedException, IOException;

	
	public Map<String, Digram> decompressRules(byte[] arr) throws IOException;
}
