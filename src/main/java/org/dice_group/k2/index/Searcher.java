package org.dice_group.k2.index;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.enums.TripleComponentRole;

public interface Searcher {

	public Model deindexGraph(Model graph);

	public Node getNodeFromID(int id, TripleComponentRole role);

	public Node getNodeFromID(String s, TripleComponentRole role);

	public void setDict(DictionaryPrivate dict);
	
}
