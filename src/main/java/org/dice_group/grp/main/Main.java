package org.dice_group.grp.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.compression.rdf.RDFCompressor;
import org.dice_group.grp.decompression.rdf.RDFDecompressor;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.serialization.GraphSerializer;
import org.dice_group.grp.serialization.impl.CRSSerializer;
import org.dice_group.grp.serialization.impl.KD2TreeSerializer;
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
		if(args.length!=4) {
			printHelp();
			return;
		}
		if(args[0].equals("-c")){
			if(args[1].toLowerCase().equals("-crs")){
				compress(args[2], args[3], false);
			}
			else if(args[1].toLowerCase().equals("-kd2")){
				compress(args[2], args[3], true);
			}
			else{
				printHelp();
				return;
			}
		}
		else if(args[0].equals("-d")) {
			if(args[1].toLowerCase().equals("-crs")){
				decompress(args[2], args[3], false);
			}
			else if(args[1].toLowerCase().equals("-kd2")){
				decompress(args[2], args[3], true);
			}
			else{
				printHelp();
				return;
			}
		}
		else {
			printHelp();
			return;
		}
	}

	public static void printHelp(){
		System.out.println("grp [-c|-d] [-crs|-kd2] in out");
		System.out.println("\t-c\tcompress RDF File");
		System.out.println("\t-d\tdecompress GRP File");
		System.out.println();
		System.out.println("\t-crs\tserialize using CRS");
		System.out.println("\t-kd2\tserialize using KD2 TREE");
		return;
	}

	public static void compress(String fileName, String output, Boolean kd2Serializer) throws NotAllowedInRDFException, NotSupportedException, IOException {
		LOGGER.info("Compressing file {} ",fileName);
		LOGGER.info("Start time {}", Calendar.getInstance().getTime());
		System.out.println("start compression of "+fileName+" with size "+new File(fileName).length()+" bytes");
		long start = Calendar.getInstance().getTimeInMillis();
		RDFCompressor c = new RDFCompressor();
		File grpFile = c.compressRDF(new File(fileName), output, kd2Serializer);
		LOGGER.info("Compressed file {} succesfully to {}", fileName, grpFile.getName());
		//TODO add more output of stats (File Size etc)
		long end = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Start time {}", Calendar.getInstance().getTime());
		LOGGER.info("Compression of {} with size {} bytes to {} with size {} bytes took {} ms",fileName, new File(fileName).length() , grpFile.getName(), grpFile.length() ,end-start);
		System.out.println("Compression of "+fileName+" with size "+new File(fileName).length()+" bytes to "+grpFile.getName()+" with size "+grpFile.length()+" bytes took "+(end-start)+" ms");
		System.out.println("Ratio is "+(1.0*grpFile.length()/new File(fileName).length()));

	}
	
	public static void decompress(String input, String output, boolean kd2decompressor) throws IOException, NotSupportedException {
		RDFDecompressor c = new RDFDecompressor();
		System.out.println("start decompression of "+input+" with size "+new File(input).length()+" bytes");
		long start = Calendar.getInstance().getTimeInMillis();
		Model m = c.decompress(input, kd2decompressor);
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Decompression of "+input+" with size "+new File(input).length()+" bytes to "+output+" with size "+output.length()+" bytes took "+(end-start)+" ms");
		FileWriter f = new FileWriter(output);
		m.write(f, "TURTLE");
	}
	
	public void query() {
		
	}
	
}
