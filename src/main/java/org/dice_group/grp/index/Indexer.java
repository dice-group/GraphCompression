package org.dice_group.grp.index;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.grammar.Grammar;
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
	public Model indexGraph(Model graph);

	public Node getNodeFromID(int id, TripleComponentRole role);

	Node getNodeFromID(String s, TripleComponentRole role);

	public Grammar indexGrammar(Grammar grammar);

}
