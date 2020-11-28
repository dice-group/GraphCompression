package org.dice_group.grp.index;

import grph.Grph;
import org.apache.jena.graph.Node;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.enums.TripleComponentRole;

/**
 * Interface for the indexing to use
 *  
 * @author minimal
 *
 */
public interface Indexer {

	/**
	 * index the Graph and return an indexed version of the Graph. 
	 * 
	 * graph will be empty afterwards
	 * 
	 * @param graph
	 * @return
	 */
	public Grph indexGraph(Grph graph);

	public Node getNodeFromID(int id, TripleComponentRole role);

	public Node getNodeFromID(String s, TripleComponentRole role);

	public DictionaryPrivate getDict();
	
}
