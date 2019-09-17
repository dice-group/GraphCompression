package org.dice_group.grp.grammar.digram;


import java.util.Objects;
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
	 * Be aware that external Indexes be also like
	 * 
	 * 0 - e1 -> 1 ------|
	 * 		     ^ - e2 -|
	 * 
	 * in that case 1=2=3 if 1 is external, should be all set 
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
	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edgeLabel1 == null) ? 0 : edgeLabel1.hashCode());
		result = prime * result + ((edgeLabel2 == null) ? 0 : edgeLabel2.hashCode());
		result = prime * result + ((external == null) ? 0 : external.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Digram) {
			Digram other = (Digram) obj;
			checkForNull(this);
			checkForNull(other);
			if(!edgeLabel1.equals(other.edgeLabel1)|| !edgeLabel2.equals(other.edgeLabel2) || !external.equals(other.external))
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param digram
	 */
	private void checkForNull(Digram digram) {
		String errorMsg = "None of the attributes can be null!";
		Objects.requireNonNull(digram.edgeLabel1, errorMsg);
		Objects.requireNonNull(digram.edgeLabel2, errorMsg);
		Objects.requireNonNull(digram.external, errorMsg);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(edgeLabel1).append(" - ").append(edgeLabel2).append(" - ").append(external).append(" - ").append(occurences);
		return builder.toString();
	}
	
	
	
}
