package org.dice_group.grp.compression;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import grph.Grph;
import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.util.BoundedList;

public interface GrammarCompressor {

	
	public byte[][] compress(Grammar grammar) throws NotSupportedException, IOException, ExecutionException, InterruptedException;

	public byte[] compress(List<Statement> graph, int vSize) throws NotSupportedException, IOException, ExecutionException, InterruptedException;

	public byte[] serializeRules(Grammar grammar) throws IOException;
}


