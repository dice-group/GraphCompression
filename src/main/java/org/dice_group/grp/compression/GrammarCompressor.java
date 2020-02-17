package org.dice_group.grp.compression;

import java.io.IOException;

import grph.Grph;
import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.util.BoundedList;

public interface GrammarCompressor {

	
	public byte[][] compress(Grammar grammar) throws NotSupportedException, IOException;

	public byte[] compress(Grph graph, BoundedList pIndex) throws NotSupportedException, IOException;

	public byte[] serializeRules(Grammar grammar) throws IOException;
}


