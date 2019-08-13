package org.dice_group.grp.grammar.digram;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

public class DigramOccurence extends Digram {

	private Statement e1;
	private Statement e2;
	private Set<RDFNode> external;

	public DigramOccurence(Statement e1, Statement e2, Set<RDFNode> external) {
		super(e1.getPredicate(), e2.getPredicate(), DigramHelper.getExternalIndexes(e1, e2, external));
		this.setEdge1(e1);
		this.setEdge2(e2);
		this.setExternals(external);
	}



	public Statement getEdge1() {
		return e1;
	}

	public void setEdge1(Statement e1) {
		this.e1 = e1;
	}

	public Statement getEdge2() {
		return e2;
	}

	public void setEdge2(Statement e2) {
		this.e2 = e2;
	}

	public Set<RDFNode> getExternals() {
		return external;
	}

	public void setExternals(Set<RDFNode> external) {
		this.external = external;
	}

	
	public Set<RDFNode> getNodes(){
		Set<RDFNode> nodes = new HashSet<RDFNode>();
		nodes.add(getEdge1().getSubject());
		nodes.add(getEdge2().getObject());
		nodes.add(getEdge1().getSubject());
		nodes.add(getEdge2().getObject());
		return nodes;
	}
	
	/**
	 * Returns true when there are no nodes in common 
	 * are same
	 * @param occurrences
	 * @return
	 */
	public boolean isNonOverlapping(Set<DigramOccurence> occurrences) {
		Set<RDFNode> allNodes = new HashSet<RDFNode>();
		addNodes(allNodes, e1);
		addNodes(allNodes, e2);
		
		Set<RDFNode> setNodes = new HashSet<RDFNode>();
		occurrences.forEach((curOccur)->{
			addNodes(setNodes, curOccur.getEdge1());
			addNodes(setNodes, curOccur.getEdge1());
		});

		return Collections.disjoint(allNodes, setNodes);
	}
	
	private void addNodes(Set<RDFNode> internalNodes, Statement s1) {
		internalNodes.add(s1.getSubject());
		internalNodes.add(s1.getObject());
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DigramOccurence) {
			DigramOccurence otherOcc = (DigramOccurence) obj;
			//this checks if the statements are the same
			boolean eq =  (otherOcc.getEdge1().equals(e1) &&  otherOcc.getEdge2().equals(e2)) 
					|| (otherOcc.getEdge2().equals(e1) && otherOcc.getEdge1().equals(e2)) ;
			//this additionally checks fit the external nodes are the same
			return eq && isOccurence(otherOcc);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (e1.getSubject().equals(e2.getObject())) {
			builder.append(e1.getSubject()).append("-").append(e1.getPredicate()).append("->").append(e1.getObject())
					.append("-").append(e2.getPredicate()).append("->").append(e2.getSubject());
		} else if (e2.getSubject().equals(e1.getObject())) {
			builder.append(e2.getSubject()).append("-").append(e2.getPredicate()).append("->").append(e2.getObject())
					.append("-").append(e1.getPredicate()).append("->").append(e1.getSubject());
		} else {
			builder.append(e1.getSubject()).append("-").append(e1.getPredicate()).append("->").append(e1.getObject());
			builder.append(e2.getSubject()).append("-").append(e2.getPredicate()).append("->").append(e2.getObject());
		}
		return builder.toString();
	}
}
