package org.dice_group.grp.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dice_group.grp.compression.rdf.RDFCompressor;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Graph Compression Algorithm - gRePair
 *
 * Main execution method 
 * 
 * @author minimal
 *
 */
public class Main {

	private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		
	}
	
	public void compress(String fileName) throws NotAllowedInRDFException, NotSupportedException, IOException {
		LOGGER.info("Compressing file {} ",fileName);
		RDFCompressor c = new RDFCompressor();
		File grpFile = c.compressRDF(new File(fileName));
		LOGGER.info("Compressed file {} succesfully to {}", fileName, grpFile.getName());
		//TODO add more output of stats (File Size etc)
	}
	
	public void decompress() {
		
	}
	
	public void query() {
		
	}
	
}
