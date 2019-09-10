package org.dice_group.grp.compression;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;

public interface GrammarCompressor {

	
	public byte[] compress(Grammar grammar) throws NotSupportedException, IOException;

	public byte[] compress(Model graph) throws NotSupportedException;

	public byte[] serializeRules(Grammar grammar) throws IOException;
}


