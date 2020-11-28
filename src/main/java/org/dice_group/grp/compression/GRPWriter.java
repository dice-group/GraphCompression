package org.dice_group.grp.compression;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

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
	private final String output;
	//private final TarArchiveOutputStream taos;

	private final FileOutputStream faos;
	private final FileOutputStream faosDict;



	public GRPWriter(String output) throws FileNotFoundException {
		this.output=output;
		FileOutputStream fos = new FileOutputStream(output);
		//GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(fos);
		//taos = new TarArchiveOutputStream(fos);
		faos = fos;
		faosDict  = new FileOutputStream(output+".dict");
	}

	public void saveDict(DictionaryPrivate dictionaryPrivate){
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);
			TarArchiveEntry dictEntry = new TarArchiveEntry(DICTIONARY_ENTRY_NAME);
			dictEntry.setSize(dictionaryPrivate.size());
			//taos.putArchiveEntry(dictEntry);

			dictionaryPrivate.save(faosDict, ci, new ProgressOut());
			//taos.write(baos.toByteArray());
			//taos.closeArchiveEntry();
			faosDict.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}


	public void save(GrammarCompressor compressor, DictionaryPrivate dictionaryPrivate, Boolean threaded, long sSize, long oSize) throws NotSupportedException, IOException, ExecutionException, InterruptedException {


		long start = Calendar.getInstance().getTimeInMillis();

		byte[] serializedGrammar = compressor.compress(sSize, oSize);
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Serialization took "+(end-start)+" ms");
		
		
		try{
			//TarArchiveEntry grammarEntry = new TarArchiveEntry(GRAMMAR_ENTRY_NAME);
			long grammarSize=0;
			long dictSize=0;
			grammarSize+=serializedGrammar.length;
			//grammarEntry.setSize(grammarSize);
			//taos.putArchiveEntry(grammarEntry);
			//taos.write(serializedGrammar);
			//write an empty long
			faos.write(serializedGrammar);
			//taos.closeArchiveEntry();
/**
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);
			dictionaryPrivate.save(baos, ci, new ProgressOut());
			TarArchiveEntry dictEntry = new TarArchiveEntry(DICTIONARY_ENTRY_NAME);
			dictEntry.setSize(baos.size());
			taos.putArchiveEntry(dictEntry);
			taos.write(baos.toByteArray());
			taos.closeArchiveEntry();
			//taos.close();
*/

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		faos.close();
	}
	
	
}
