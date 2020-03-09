package org.dice_group.grp.grammar.digram;


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.dice_group.grp.grammar.Statement;

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
	private Integer edgeLabel2;
	private Integer edgeLabel1;
	private Set<Integer> external;
	protected Byte structure;

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
	public Digram(Integer edgeLabel1, Integer edgeLabel2, Set<Integer> externalIndexes) {
		this.setEdgeLabel1(edgeLabel1);
		this.setEdgeLabel2(edgeLabel2);
		this.external=externalIndexes;
	}



	public byte getStructure() {
		return structure;
	}
	
	public void setStructure(byte struct) {
		this.structure=struct;
	}

	// this is bs, but no idea how to do it better 3h away from vacation
	public DigramOccurence createOccurence(List<Integer> external, List<Integer> internals) {
		// create occ from digram struct
		if(structure==0) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(0)), new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(0)), external);
			return createOccInternal(external, external.get(0), external.get(0), external.get(0), external.get(0));
		}
		if(structure==1) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(0)), new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(1)), external);
			return createOccInternal(external, external.get(0), external.get(0), external.get(0), external.get(1));
		}
		if(structure==2) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(0)), new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,internals.get(0)), external);
			return createOccInternal(external, external.get(0), external.get(0), external.get(0), internals.get(0));
		}
		if(structure==3) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(internals.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,internals.get(0)), new StatementImpl(ResourceFactory.createResource(internals.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(0)), external);
			return createOccInternal(external, internals.get(0), internals.get(0), internals.get(0), external.get(0));
		}
		if(structure==4) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(0)), new StatementImpl(ResourceFactory.createResource(external.get(1).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(0)), external);
			return createOccInternal(external, external.get(0), external.get(0), external.get(1), external.get(0));
		}
		if(structure==5) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(0)), new StatementImpl(ResourceFactory.createResource(internals.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(0)), external);
			return createOccInternal(external, external.get(0), external.get(0), internals.get(0), external.get(0));
		}
		if(structure==6) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(internals.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,internals.get(0)), new StatementImpl(ResourceFactory.createResource(internals.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,external.get(0)), external);
			return createOccInternal(external, internals.get(0), internals.get(0), external.get(0), internals.get(0));
		}
		if(structure==7) {
			//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,external.get(1)), new StatementImpl(ResourceFactory.createResource(external.get(0).toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,internals.get(0)), external);
			return createOccInternal(external, external.get(0), external.get(1), external.get(0), internals.get(0));
		}
		if(structure==8) {
			return createOccInternal(external, external.get(0), internals.get(0), external.get(0), external.get(1));
		}
		if(structure==9) {
			return createOccInternal(external, external.get(0), internals.get(0), external.get(0), internals.get(1));
		}
		if(structure==10) {
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(0), external.get(1));
		}
		if(structure==11) {
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(0), internals.get(1));
		}
		if(structure==12) {
			return createOccInternal(external, internals.get(0), internals.get(1), internals.get(0), external.get(0));
		}
		if(structure==13) {
			return createOccInternal(external, external.get(0), external.get(1), internals.get(0), external.get(0));
		}
		if(structure==14) {
			return createOccInternal(external, external.get(0), internals.get(0), external.get(1), external.get(0));
		}
		if(structure==15) {
			return createOccInternal(external, external.get(0), internals.get(0), internals.get(1), external.get(0));
		}
		if(structure==16) {
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(0), external.get(1));
		}
		if(structure==17) {
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(0), internals.get(1));
		}
		if(structure==18) {
			return createOccInternal(external, internals.get(0), internals.get(1), internals.get(0), external.get(0));
		}
		if(structure==18) {
			return createOccInternal(external, external.get(1), external.get(0), external.get(0), internals.get(0));
		}
		if(structure==19) {
			return createOccInternal(external, external.get(0), external.get(1), external.get(1), internals.get(0));
		}
		if(structure==20) {
			return createOccInternal(external, internals.get(0), external.get(0), external.get(0), external.get(1));
		}
		if(structure==21) {
			return createOccInternal(external, internals.get(0), external.get(0), external.get(0), internals.get(1));
		}
		if(structure==22) {
			return createOccInternal(external, external.get(0), internals.get(0), internals.get(0), external.get(1));
		}
		if(structure==23) {
			return createOccInternal(external, external.get(0), internals.get(0), internals.get(0), internals.get(1));
		}
		if(structure==24) {
			return createOccInternal(external, internals.get(0), internals.get(1), internals.get(1), external.get(0));
		}
		if(structure==25) {
			return createOccInternal(external, external.get(0), external.get(1), internals.get(0), external.get(1));
		}
		if(structure==26) {
			return createOccInternal(external, internals.get(0), external.get(0), external.get(1), external.get(0));
		}
		if(structure==27) {
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(1), external.get(0));
		}
		
		if(structure==28) {
			return createOccInternal(external, external.get(0), internals.get(0), external.get(1), internals.get(0));
		}
		if(structure==29) {
			return createOccInternal(external, external.get(0), internals.get(0), internals.get(1), internals.get(0));
		}
		if(structure==30) {
			return createOccInternal(external, internals.get(0), internals.get(1), external.get(0), internals.get(1));
		}
		if(structure==31) {
			return createOccInternal(external, external.get(0), external.get(1), external.get(1), external.get(1));
		}
		if(structure==32) {
			return createOccInternal(external, internals.get(0), external.get(0), external.get(0), external.get(0));
		}
		if(structure==33) {
			return createOccInternal(external, external.get(0), internals.get(0), internals.get(0), internals.get(0));
		}
		if(structure==34){
			return createOccInternal(external, external.get(0), internals.get(0), external.get(0), internals.get(0));
		}
		if(structure==35){
			return createOccInternal(external, internals.get(0), external.get(0), internals.get(0), external.get(0));
		}
		if(structure==36){
			return createOccInternal(external, external.get(0), external.get(0), external.get(1), external.get(1));
		}
		
		return null;
	}
	
	
	private DigramOccurence createOccInternal(List<Integer> external, Integer n1, Integer n2, Integer n3, Integer n4) {
		return new DigramOccurence(new Statement(n1, edgeLabel1, n2), new Statement(n3, edgeLabel2, n4), external);
		//return new DigramOccurence(new StatementImpl(ResourceFactory.createResource(n1.toString()), ResourceFactory.createProperty(edgeLabel1.toString()) ,n2), new StatementImpl(ResourceFactory.createResource(n3.toString()), ResourceFactory.createProperty(edgeLabel2.toString()) ,n4), external);
	}

	public Integer getEdgeLabel2() {
		return edgeLabel2;
	}

	public void setEdgeLabel2(Integer edgeLabel2) {
		this.edgeLabel2 = edgeLabel2;
	}

	public Integer getEdgeLabel1() {
		return edgeLabel1;
	}

	public void setEdgeLabel1(Integer edgeLabel1) {
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
		result = prime * result + ((structure == null) ? 0 : structure.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Digram) {
			Digram other = (Digram) obj;
			checkForNull(this);
			checkForNull(other);
			if(!edgeLabel1.equals(other.edgeLabel1)|| !edgeLabel2.equals(other.edgeLabel2) || !external.equals(other.external) || !structure.equals(other.structure))
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
