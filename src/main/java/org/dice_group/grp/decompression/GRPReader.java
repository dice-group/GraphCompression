package org.dice_group.grp.decompression;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.dice_group.grp.compression.GRPWriter;
import org.dice_group.grp.decompression.impl.CRSDecompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.digram.Digram;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.util.io.CountInputStream;
import org.rdfhdt.hdtjena.NodeDictionary;

public class GRPReader {

	public static Grammar load(String input, DictionaryPrivate dict, Map<Digram, List<Integer[]>> internalMap)
			throws NotSupportedException, IOException {
		CRSDecompressor decompressor = new CRSDecompressor();

		try (FileInputStream fos = new FileInputStream(input);
				GzipCompressorInputStream gzip = new GzipCompressorInputStream(fos);
				TarArchiveInputStream tais = new TarArchiveInputStream(gzip);) {
			ArchiveEntry entry = tais.getNextEntry();

			byte[] grammarArr = new byte[0];

			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);

			if (entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			if (entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				try (BufferedInputStream bis = new BufferedInputStream(tais)) {
					ci.load(bis);
					dict.load(bis, ci, new ProgressOut());
				}
			}
			entry = tais.getNextTarEntry();
			if (entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			if (entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				try (BufferedInputStream bis = new BufferedInputStream(tais)) {
					ci.load(bis);
					dict.load(bis, ci, new ProgressOut());
				}
			}
			return null;
			//return decompressor.decompress(grammarArr, new NodeDictionary(dict), internalMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
