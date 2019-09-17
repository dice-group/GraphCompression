package org.dice_group.grp.serialization;

import java.io.IOException;

import org.dice_group.grp.grammar.digram.Digram;

public interface DigramSerializer {

	byte[] serialize(Digram m) throws IOException;

}
