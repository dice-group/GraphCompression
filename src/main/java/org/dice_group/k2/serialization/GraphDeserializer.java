package org.dice_group.k2.serialization;

import java.util.List;

public interface GraphDeserializer {

	public List<Integer>[] deserialize(byte[] serialized);
	
}
