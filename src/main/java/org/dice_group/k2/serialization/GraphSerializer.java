package org.dice_group.k2.serialization;

import java.util.List;

public interface GraphSerializer{


	public <T extends Number> byte[] serialize(List<T> val, List<Integer> colRow, List<Integer> rowPtr);

}
