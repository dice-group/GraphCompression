package org.dice_group.grp.compression;

import org.dice_group.grp.exceptions.NotSupportedException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface GrammarCompressor {

	
	public byte[] compress(long sSize, long oSzie) throws NotSupportedException, IOException, ExecutionException, InterruptedException;

}


