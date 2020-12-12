package org.dice_group.k2.index.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InternalIndexer implements Iterator<Integer> {

	private List<Integer> orderedNodes = new ArrayList<Integer>();
	private int currentIndex=0;
	
	@Override
	public boolean hasNext() {
		return orderedNodes.size()>currentIndex;
	}

	@Override
	public Integer next() {
		return orderedNodes.get(currentIndex++);
	}
	
	public void addNode(Integer nextNode) {
		orderedNodes.add(nextNode);
	}
	
	public void load(InputStream is) throws IOException {
		Integer next;
		orderedNodes.clear();
		StringBuilder node = new StringBuilder();
		while((next = is.read())>-1) {
			char b = (char)next.byteValue();
			if(b == '\n') {
				//orderedNodes.add(node.toString().replace("\\\\", "\\").replace("\\n", "\n"));
			}
			else {
				node.append(b);
			}
		}
		
	}
	
	
	public void save(OutputStream os) throws IOException {
		for(Integer n : orderedNodes) {
			
			for(Byte b : ByteBuffer.allocate(Integer.BYTES).putInt(n).array()) {
				
				os.write(b);
			}
			//os.write('\n');
		
		}
	}
	
}
