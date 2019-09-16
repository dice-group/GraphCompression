package org.dice_group.grp.compression;

import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.dice_group.grp.compression.impl.CRSCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.listener.ProgressOut;
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

	public static void save(String output,Grammar grammar, DictionaryPrivate dictionaryPrivate) throws  NotSupportedException, IOException {
		CRSCompressor compressor = new CRSCompressor();
		byte[] serializedGrammar = compressor.compress(grammar);
		
		try(FileOutputStream fos = new FileOutputStream(output);
				GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(fos);
				TarArchiveOutputStream taos = new TarArchiveOutputStream(gzip);){
			TarArchiveEntry grammarEntry = new TarArchiveEntry(GRAMMAR_ENTRY_NAME);
			taos.putArchiveEntry(grammarEntry);
			taos.write(serializedGrammar);
			taos.closeArchiveEntry();
			TarArchiveEntry dictEntry = new TarArchiveEntry(DICTIONARY_ENTRY_NAME);
			taos.putArchiveEntry(dictEntry);
			dictionaryPrivate.save(taos, new ControlInformation(), new ProgressOut());
			taos.closeArchiveEntry();
			taos.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	
	
}
