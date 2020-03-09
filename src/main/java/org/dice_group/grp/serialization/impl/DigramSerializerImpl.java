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
import org.dice_group.grp.index.impl.InternalIndexer;
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
		// TODO for some reaseon getReplaced wont work here 
		List<Long[]> internals = getInternalIndexes(grammar.getReplaced().get(m));

		byte sizeFlag = getSizeFlag(internals);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for(Long[] occInternals : internals) {
			for(Long internal : occInternals) {
				if(sizeFlag==0) {
					baos.write(internal.byteValue());
				}
				else if(sizeFlag==1) {
					ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
					bb.putShort(internal.shortValue());
					baos.write(bb.array());
				}
				else if(sizeFlag==2) {
					ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
					bb.putInt(internal.intValue());
					baos.write(bb.array());
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
		for(byte b : internalsBytes) {
			ret.put(b);
		}
		return ret.array();
		
	}
	
	private List<Long[]> getInternalIndexes(List<DigramOccurence> list) {
		List<Long[]> ret = new LinkedList<Long[]>();
		// sort occ list alphabetically after external nodes!!!
		if(list==null) {
			return null;
		}
		Collections.sort(list, new Comparator<DigramOccurence>() {

			@Override
			public int compare(DigramOccurence arg0, DigramOccurence arg1) {
				//TODO sort externals: s1 s2 o1 o2
				for(int x=0;x<arg0.getExternals().size();x++){
					int r = arg0.getExternals().get(x).compareTo(arg1.getExternals().get(x));
					if(r!=0){
						return r;
					}
				}
				return 0;
				/*
				StringBuilder b1 = new StringBuilder();
				for(int n : arg0.getExternals()) {
					b1.append(n+" ");
				}
				StringBuilder b2 = new StringBuilder();
				for(int n : arg1.getExternals()) {
					b1.append(n+" ");
				}
	
				return b1.toString().compareTo(b2.toString());
				*/
			}
			
		});
		for(DigramOccurence occ : list) {
			List<Integer> internals = occ.getInternals();
			
			Long[] indexInternals = new Long[internals.size()];
			for(int i=0;i<internals.size();i++) {
				Integer n = internals.get(i);
				try {
					indexInternals[i] = Long.valueOf(n);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			ret.add(indexInternals);
		}
		return ret;
	}

	private byte getSizeFlag(List<Long[]> internals) {
		Long max = 0L;
		if(internals == null) {
			return 0;
		}
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
