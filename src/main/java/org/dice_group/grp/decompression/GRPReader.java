package org.dice_group.grp.decompression;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.dice_group.grp.compression.GRPWriter;
import org.dice_group.grp.decompression.impl.CRSDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdtjena.NodeDictionary;

public class GRPReader {
	

	public static Grammar load(String input, DictionaryPrivate dict, Map<Digram, List<Integer[]>> internalMap) throws  NotSupportedException, IOException {
		CRSDecompressor decompressor = new CRSDecompressor();
				
		try(FileInputStream fos = new FileInputStream(input);
				GzipCompressorInputStream gzip = new GzipCompressorInputStream(fos);
				TarArchiveInputStream tais = new TarArchiveInputStream(gzip);){
			TarArchiveEntry entry = tais.getCurrentEntry();
			byte[] grammarArr = new byte[0];
			byte[] dictArr = new byte[0];
			if(entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			if(entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				dictArr = tais.readAllBytes();
			}
			entry = tais.getNextTarEntry();
			if(entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			if(entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				dictArr = tais.readAllBytes();
			}
			dict.load(new ByteArrayInputStream(dictArr), new ControlInformation(), new ProgressOut());
			return decompressor.decompress(grammarArr, new NodeDictionary(dict), internalMap);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
