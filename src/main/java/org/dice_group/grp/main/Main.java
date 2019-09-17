package org.dice_group.grp.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
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
	
	public static void main(String[] args) throws NotAllowedInRDFException, NotSupportedException, IOException {
		if(args.length!=3) {
			System.out.println("grp [-c|-d] in out");
			System.out.println("\t-c\tcompress RDF File");
			System.out.println("\t-d\tdecompress GRP File");
			return;
		}
		if(args[0].equals("-c")){
			compress(args[1], args[2]);
		}
		else if(args[0].equals("-d")) {
			decompress(args[1], args[2]);
		}
		else {
			System.out.println("grp [-c|-d] in out");
			System.out.println("\t-c\tcompress RDF File");
			System.out.println("\t-d\tdecompress GRP File");
			return;
		}
	}
	
	public static void compress(String fileName, String output) throws NotAllowedInRDFException, NotSupportedException, IOException {
		LOGGER.info("Compressing file {} ",fileName);
		RDFCompressor c = new RDFCompressor();
		File grpFile = c.compressRDF(new File(fileName), output);
		LOGGER.info("Compressed file {} succesfully to {}", fileName, grpFile.getName());
		//TODO add more output of stats (File Size etc)
	}
	
	public static void decompress(String input, String output) throws IOException {
		RDFCompressor c = new RDFCompressor();
		Model m = c.decompress(input);
		FileWriter f = new FileWriter(output);
		m.write(f, "TURTLE");
	}
	
	public void query() {
		
	}
	
}
