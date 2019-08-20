package org.dice_group.grp.index.impl;

import java.util.Comparator;

public class  ToStringComparator implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		return arg0.toString().compareTo(arg1.toString());
	}




}
