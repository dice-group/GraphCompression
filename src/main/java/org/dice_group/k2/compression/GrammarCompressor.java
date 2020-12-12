package org.dice_group.k2.compression;

import org.dice_group.k2.exceptions.NotSupportedException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface GrammarCompressor {

	
	public void compress(long sSize, long oSzie) throws NotSupportedException, IOException, ExecutionException, InterruptedException;

	void setOutStream(FileOutputStream faos);
}


