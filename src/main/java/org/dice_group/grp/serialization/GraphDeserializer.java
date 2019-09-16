package org.dice_group.grp.serialization;

import java.util.List;

public interface GraphDeserializer {

	public List<Integer>[] deserialize(byte[] serialized);
	
}
