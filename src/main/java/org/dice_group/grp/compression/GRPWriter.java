package org.dice_group.grp.compression;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.dice_group.grp.compression.impl.CRSCompressor;
import org.dice_group.grp.compression.impl.KD2TreeCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;

/**
 * Compresses the resulting Grammar and Dictionary into 
 * the final compression form in which it will be saved
 * 
 * 
 * @author minimal
 *
 */
public class GRPWriter {

	public static final String DICTIONARY_ENTRY_NAME = "dict";
	public static final String GRAMMAR_ENTRY_NAME = "grammar";

	public static void save(String output,Grammar grammar, DictionaryPrivate dictionaryPrivate, Boolean kd2Flag) throws  NotSupportedException, IOException {
		GrammarCompressor compressor;
		if(kd2Flag){
			compressor = new KD2TreeCompressor();
		}else {
			compressor = new CRSCompressor();
		}
		long start = Calendar.getInstance().getTimeInMillis();

		byte[][] serializedGrammar = compressor.compress(grammar);
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Serialization took "+(end-start)+" ms");
		
		
		try(FileOutputStream fos = new FileOutputStream(output);
				GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(fos);
				TarArchiveOutputStream taos = new TarArchiveOutputStream(gzip);){
			TarArchiveEntry grammarEntry = new TarArchiveEntry(GRAMMAR_ENTRY_NAME);
			long grammarSize=0;
			long dictSize=0;
			for(byte[] ser : serializedGrammar) {
				grammarSize+=ser.length;
			}
			grammarEntry.setSize(grammarSize);
			taos.putArchiveEntry(grammarEntry);
			for(byte[] ser : serializedGrammar) {
				taos.write(ser);
			}
			taos.closeArchiveEntry();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);
			dictionaryPrivate.save(baos, ci, new ProgressOut());
			TarArchiveEntry dictEntry = new TarArchiveEntry(DICTIONARY_ENTRY_NAME);
			dictEntry.setSize(baos.size());
			taos.putArchiveEntry(dictEntry);
			taos.write(baos.toByteArray());
			taos.closeArchiveEntry();
			taos.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	
}
