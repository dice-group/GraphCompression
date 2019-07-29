package org.dice_group.grp.grammar.digram;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

/**
 * 
 * The Digram of a graph
 * <br/>
 * v-e1->u-e2->o
 * 
 * with e1, e2 in E of a HyperGraph
 * 
 * @author minimal
 *
 */
public class Digram {

	
	private Resource edgeLabel2;
	private Resource edgeLabel1;
	private Set<Integer> external;

	private long occurences=0;
	
	/**
	 * 
	 * @param edgeLabel1
	 * @param edgeLabel2
	 * @param externalIndexes is subset of {0,1,2,3} with 0 -e1-> 1 and 2 -e2-> 3 
	 */
	public Digram(Resource edgeLabel1, Resource edgeLabel2, Set<Integer> externalIndexes) {
		this.setEdgeLabel1(edgeLabel1);
		this.setEdgeLabel2(edgeLabel2);
		this.external=externalIndexes;
	}

	public Resource getEdgeLabel2() {
		return edgeLabel2;
	}

	public void setEdgeLabel2(Resource edgeLabel2) {
		this.edgeLabel2 = edgeLabel2;
	}

	public Resource getEdgeLabel1() {
		return edgeLabel1;
	}

	public void setEdgeLabel1(Resource edgeLabel1) {
		this.edgeLabel1 = edgeLabel1;
	}
	
	public boolean isOccurence(DigramOccurence occ) {
		if(occ.getEdgeLabel1().equals(edgeLabel1) && occ.getEdgeLabel2().equals(edgeLabel2)){			
			return DigramHelper.getExternalIndexes(occ.getEdge1(), occ.getEdge2(), occ.getExternals()).equals(external);
		}
		else if(occ.getEdgeLabel2().equals(edgeLabel1) && occ.getEdgeLabel1().equals(edgeLabel2)) {
			return DigramHelper.getExternalIndexes(occ.getEdge2(), occ.getEdge1(), occ.getExternals()).equals(external);

		}
		return false;
	}
	
	

	public Set<Integer> getExternalIndexes() {
		return external;
	}

	public void setExternalIndexes(Set<Integer>  external) {
		this.external = external;
	}

	public long getNoOfOccurences() {
		return occurences;
	}

	public void setNoOfOccurences(long occurences) {
		this.occurences = occurences;
	}
	
}
