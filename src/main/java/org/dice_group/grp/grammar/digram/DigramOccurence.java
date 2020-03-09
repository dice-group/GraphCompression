package org.dice_group.grp.grammar.digram;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.dice_group.grp.grammar.Statement;

public class DigramOccurence extends Digram {

	private Statement e1;
	private Statement e2;
	private int origE1;


	private int origE2;
	private List<Integer> external;

	public DigramOccurence(Statement e1, Statement e2, List<Integer> external) {
		super(e1.getPredicate(), e2.getPredicate(), DigramHelper.getExternalIndexes(e1, e2, external));
		this.setEdge1(e1);
		this.setEdge2(e2);
		this.setExternals(external);
		this.structure = generateStructure();
	}

	// spagetthi code par excellence, but no idea how to do it better 3h away from vacation
	public byte generateStructure() {
		byte struct = 0;
		if (e1.getSubject().equals(e1.getObject()) && e2.getSubject().equals(e1.getSubject())
				&& e2.getObject().equals(e1.getSubject())) {
			// -n- nothing to do
		} else if (e1.getSubject().equals(e1.getObject())) {
			if (e1.getSubject().equals(e2.getSubject())) {
				// n-n --> o
				if (external.contains(e1.getSubject())) {
					struct = external.contains(e2.getObject()) ? (byte) 1 : 2;
				} else {
					struct = 3;
				}
			} else {
				// n-n <-- o
				if (external.contains(e1.getSubject())) {
					struct = external.contains(e2.getObject()) ? (byte) 4 : 5;
				} else {
					struct = 6;
				}
			}
		} else if (e1.getSubject().equals(e2.getSubject())) {
			if (e2.getSubject().equals(e2.getObject())) {
				// n --> o --> n-n
				if (external.contains(e1.getSubject())) {
					struct = external.contains(e1.getObject()) ? (byte) 1 : 2;
				} else {
					struct = 6;
				}
			} else {
				if (external.contains(e1.getSubject())) {
					if (external.contains(e1.getObject())) {
						if(e1.getObject().equals(e2.getObject())){
							struct=36;
						}else {
							struct = 7;
						}
					} else if (external.contains(e2.getObject())) {
						struct = 8;
					} else {
						if(e1.getObject().equals(e2.getObject())){
							struct = 34;
						}else {
							struct = 9;
						}
					}
				} else {
					if (external.contains(e1.getObject())) {
						if(e1.getObject().equals(e2.getObject())){
							struct= 35;
						}
						else {
							struct = external.contains(e2.getObject()) ? (byte) 10 : 11;
						}
					} else {
						struct = 12;
					}
				}
			}
		} else if (e1.getSubject().equals(e2.getObject())) {
			if (external.contains(e1.getSubject())) {
				if (external.contains(e1.getObject())) {
					struct = 13;
				} else if (external.contains(e2.getSubject())) {
					struct = 14;
				} else {
					struct = 15;
				}
			} else {
				if (external.contains(e1.getObject())) {
					struct = external.contains(e2.getSubject()) ? (byte) 16 : 17;
				} else {
					struct = 18;
				}
			}
		} else if (e1.getObject().equals(e2.getSubject())) {
			if (e2.getSubject().equals(e2.getObject())) {
				if(external.contains(e1.getSubject())) {
					struct = external.contains(e2.getSubject())?(byte)31:33;
				}
				else {
					struct = 33;
				}
			} else {
				if (external.contains(e1.getObject())) {
					if (external.contains(e1.getSubject())) {
						struct = 19;
					} else if (external.contains(e2.getObject())) {
						struct = 20;
					} else {
						struct = 21;
					}
				} else {
					if (external.contains(e1.getSubject())) {
						struct = external.contains(e2.getObject()) ? (byte) 22 : 23;
					} else {
						struct = 24;
					}
				}
			}
		} else if (e1.getObject().equals(e2.getObject())) {
			if (external.contains(e1.getObject())) {
				if (external.contains(e1.getSubject())) {
					struct = 25;
				} else if (external.contains(e2.getSubject())) {
					struct = 26;
				} else {
					struct = 27;
				}
			} else {
				if (external.contains(e1.getSubject())) {
					struct = external.contains(e2.getSubject()) ? (byte) 28 : 29;
				} else {
					struct = 30;
				}
			}
		}

		this.setStructure(struct);
		return struct;
	}

	public List<Integer> getInternals() {
		Set<Integer> ret = new HashSet<Integer>();
		if (!external.contains(e1.getSubject())) {
			ret.add(e1.getSubject());
		}
		if (!external.contains(e1.getObject())) {
			ret.add(e1.getObject());
		}
//		if (!external.contains(e1.getPredicate())) {
//			ret.add(e1.getPredicate());
//		}
		if (!external.contains(e2.getSubject())) {
			ret.add(e2.getSubject());
		}
		if (!external.contains(e2.getObject())) {
			ret.add(e2.getObject());
		}
//		if (!external.contains(e2.getPredicate())) {
//			ret.add(e2.getPredicate());
//		}
		return new ArrayList<Integer>(ret);
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

	public List<Integer> getExternals() {
		return external;
	}

	public void setExternals(List<Integer> external) {
		this.external = external;
	}

	public List<Integer> getNodes() {
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.add(getEdge1().getSubject());
		nodes.add(getEdge1().getObject());
		nodes.add(getEdge2().getSubject());
		nodes.add(getEdge2().getObject());
		return nodes;
	}

	/**
	 * Returns true when there are no nodes in common are same
	 * 
	 * @param occurrences
	 * @return
	 */
	public boolean isNonOverlapping(Set<DigramOccurence> occurrences) {
		Set<Integer> allNodes = new HashSet<Integer>();
		addNodes(allNodes, e1);
		addNodes(allNodes, e2);

		Set<Integer> setNodes = new HashSet<Integer>();
		occurrences.forEach((curOccur) -> {
			addNodes(setNodes, curOccur.getEdge1());
			addNodes(setNodes, curOccur.getEdge1());
		});

		return Collections.disjoint(allNodes, setNodes);
	}

	private void addNodes(Set<Integer> internalNodes, Statement s1) {
		internalNodes.add(s1.getSubject());
		internalNodes.add(s1.getObject());
	}

	public int getOrigE1() {
		return origE1;
	}

	public void setOrigE1(int origE1) {
		this.origE1 = origE1;
	}

	public int getOrigE2() {
		return origE2;
	}

	public void setOrigE2(int origE2) {
		this.origE2 = origE2;
	}



	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DigramOccurence) {
			DigramOccurence otherOcc = (DigramOccurence) obj;
			// this checks if the statements are the same
			boolean eq = (otherOcc.getEdge1().equals(e1) && otherOcc.getEdge2().equals(e2))
					|| (otherOcc.getEdge2().equals(e1) && otherOcc.getEdge1().equals(e2));
			// this additionally checks fit the external nodes are the same
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
