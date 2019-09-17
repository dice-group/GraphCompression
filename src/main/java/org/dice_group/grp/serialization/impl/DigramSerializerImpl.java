package org.dice_group.grp.serialization.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.serialization.DigramSerializer;

public class DigramSerializerImpl implements DigramSerializer {

	private Grammar grammar;

	public DigramSerializerImpl(Grammar g) {
		this.grammar = g;
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	@Override
	public byte[] serialize(Digram m) throws IOException {
		// e1e2{flag|ext1|ext2}[o1{int1(, int2)}(,o2{int1, int2},..)]
		//e1 is complement => we know when next digram starts
		Integer e1 = -1*Integer.valueOf(m.getEdgeLabel1().toString().replace(":p", ""));
		Integer e2 = Integer.valueOf(m.getEdgeLabel2().toString().replace(":p", ""));
		
		// save struct byte 
		byte struct = m.getStructure();
		// getInternals
		List<Long[]> internals = getInternalIndexes(grammar.getReplaced().get(m));
		
		byte sizeFlag= getSizeFlag(internals);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for(Long[] occInternals : internals) {
			for(Long internal : occInternals) {
				if(sizeFlag==0) {
					baos.write(internal.byteValue());
				}
				else if(sizeFlag==1) {
					baos.write(internal.shortValue());
				}
				else if(sizeFlag==2) {
					baos.write(internal.intValue());
				}
				else {
					baos.write(ByteBuffer.allocate(Long.BYTES).putLong(internal).array());
				}
			}
		}
		/* set flags
		 * 1XYYX_1X_2 with 
		 * X = 1 if two internals, 0 if one internal, 
		 * YY as internals size 0 = byte, 1 = short, 2= int, 3= long
		 * X_i as external i
		 */
		byte flags = Integer.valueOf((sizeFlag << 6) + struct).byteValue();
		
		byte[] internalsBytes = baos.toByteArray();
		ByteBuffer ret = ByteBuffer.allocate(9+internalsBytes.length);
		ret.putInt(e1);
		ret.putInt(e2);
		ret.put(flags);
		ret.put(internalsBytes);
		return ret.array();
		
	}
	
	private List<Long[]> getInternalIndexes(List<DigramOccurence> list) {
		List<Long[]> ret = new LinkedList<Long[]>();
		// sort occ list alphabetically after external nodes!!!
		Collections.sort(list, new Comparator<DigramOccurence>() {

			@Override
			public int compare(DigramOccurence arg0, DigramOccurence arg1) {
				StringBuilder b1 = new StringBuilder();
				for(RDFNode n : arg0.getExternals()) {
					b1.append(n.toString());
				}
				StringBuilder b2 = new StringBuilder();
				for(RDFNode n : arg1.getExternals()) {
					b1.append(n.toString());
				}
	
				return b1.toString().compareTo(b2.toString());
			}
			
		});
		for(DigramOccurence occ : list) {
			List<RDFNode> internals = occ.getInternals();
			Long[] indexInternals = new Long[internals.size()];
			for(int i=0;i<internals.size();i++) {
				RDFNode n = internals.get(i);
				indexInternals[i] = Long.valueOf(n.toString().replace(":s", "").replace(":p", ""));
			}
			ret.add(indexInternals);
		}
		return ret;
	}

	private byte getSizeFlag(List<Long[]> internals) {
		Long max = 0L;
		for(Long[] internal : internals) {
			for(Long internalEl : internal) {
				max = Math.max(max, internalEl);
			}			
		}
		if(max<=Byte.MAX_VALUE) {
			return 0;
		}
		if(max<=Short.MAX_VALUE) {
			return 1;
		}
		if(max<=Integer.MAX_VALUE) {
			return 2;
		}
		//otherwise long
		return 3;
	}
	
}
