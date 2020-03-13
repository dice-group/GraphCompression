package org.dice_group.grp.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
	
	public static void main(String[] args) throws NotAllowedInRDFException, NotSupportedException, IOException, ExecutionException, InterruptedException {
		if(args.length<4) {
			printHelp();
			return;
		}
		if(args[0].equals("-c")){
			if(args[1].toLowerCase().equals("-kd2")){
				if(args[2].toLowerCase().equals("-digrams")){
					compress(args[3], args[4], true, false, false);
				}
				else{
					compress(args[2], args[3], true, true, false);

				}
			}
			else if(args[1].toLowerCase().equals("-tkd2")){
				if(args[2].toLowerCase().equals("-digrams")){
					compress(args[3], args[4], true, false, true);
				}
				else{
					compress(args[2], args[3], true, true, true);

				}
			}
			else{
				printHelp();
				return;
			}
		}
		else if(args[0].equals("-d")) {
			List<String> cliargs = new ArrayList<String>();
			String outFormat="N-TRIPLE";
			if(cliargs.contains("-out")){
				outFormat = cliargs.get(cliargs.indexOf("-out")+1).toUpperCase();
			}
			for(int i=1;i<args.length;i++){
				cliargs.add(args[i].toLowerCase());
			}
			if(cliargs.contains("-crs")){
				decompress(args[2], args[3], false, outFormat);
			}
			else if(cliargs.contains("-kd2")){
				decompress(args[2], args[3], true, outFormat);
			}
			else if(cliargs.contains("-tkd2")){
				decompress(args[2], args[3], true, true, outFormat);
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
		System.out.println("grp [-c|-d] options [-kd2|-tkd2] [-digrams] in out");
		System.out.println("\t-c\tcompress RDF File");
		System.out.println("\t-d\tdecompress GRP File");
		System.out.println();
		System.out.println("\t-kd2\t(de)serialize using KD2 TREE");
		System.out.println("\t-tkd2\t(de)serialize using Threaded KD2 Tree");

		System.out.println();
		System.out.println("\tCompress only");
		System.out.println("\t-digrams\tuse gRePair algorithm");

		System.out.println();
		System.out.println("\tDecompress Options");
		System.out.println("\t-out: N-TRIPLE, TURTLE, RDF/XML - will save output in the format specified");


		return;
	}

	public static void compress(String fileName, String output, Boolean kd2Serializer, Boolean kdFlag, Boolean threaded) throws NotAllowedInRDFException, NotSupportedException, IOException, ExecutionException, InterruptedException {
		LOGGER.info("Compressing file {} ",fileName);
		LOGGER.info("Start time {}", Calendar.getInstance().getTime());
		System.out.println("start compression of "+fileName+" with size "+new File(fileName).length()+" bytes");
		long start = Calendar.getInstance().getTimeInMillis();
		RDFCompressor c = new RDFCompressor(threaded);
		File grpFile = c.compressRDF(new File(fileName), output, kd2Serializer, kdFlag);
		LOGGER.info("Compressed file {} succesfully to {}", fileName, grpFile.getName());
		long end = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Start time {}", Calendar.getInstance().getTime());
		LOGGER.info("Compression of {} with size {} bytes to {} with size {} bytes took {} ms",fileName, new File(fileName).length() , grpFile.getName(), grpFile.length() ,end-start);
		System.out.println("Compression of "+fileName+" with size "+new File(fileName).length()+" bytes to "+grpFile.getName()+" with size "+grpFile.length()+" bytes took "+(end-start)+" ms");
		System.out.println("Ratio is "+(1.0*grpFile.length()/new File(fileName).length()));

	}

	public static void decompress(String input, String output, boolean kd2decompressor, String outFormat) throws IOException, NotSupportedException, ExecutionException, InterruptedException {
		decompress(input, output, kd2decompressor, false, outFormat);
	}


	public static void decompress(String input, String output, boolean kd2decompressor, boolean threaded, String outFormat) throws IOException, NotSupportedException, ExecutionException, InterruptedException {
		RDFDecompressor c = new RDFDecompressor(threaded);
		System.out.println("start decompression of "+input+" with size "+new File(input).length()+" bytes");
		long start = Calendar.getInstance().getTimeInMillis();
		Model m = c.decompress(input, kd2decompressor);
		System.out.println("Decompression done, will save dataset to file now.");
		FileWriter f = new FileWriter(output);
		m.write(f, outFormat);
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Decompression of "+input+" with size "+new File(input).length()+" bytes to "+output+" with size "+new File(output).length()+" bytes took "+(end-start)+" ms");

	}

	
}
