package org.dice_group.k2.decompression;

import org.dice_group.k2.exceptions.NotSupportedException;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.ControlInfo;
import org.rdfhdt.hdt.options.ControlInformation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class GRPReader {

	public static byte[] load(String input, DictionaryPrivate dict)
			throws NotSupportedException, IOException {
		try (FileInputStream fos = new FileInputStream(input);FileInputStream fosDict = new FileInputStream(input+".dict");){
			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);
			BufferedInputStream bis=null;
			try {
				bis = new BufferedInputStream(fosDict);
				ci.load(bis);
				dict.load(bis, ci, new ProgressOut());
			}catch (Exception e){
				e.printStackTrace();
			}

			try {
				bis = new BufferedInputStream(fos);
				return bis.readAllBytes();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return null;


		/*
		try (FileInputStream fos = new FileInputStream(input);
				//GzipCompressorInputStream gzip = new GzipCompressorInputStream(fos);
				TarArchiveInputStream tais = new TarArchiveInputStream(fos);
				) {
			ArchiveEntry entry = tais.getNextEntry();

			byte[] grammarArr = new byte[0];

			ControlInformation ci = new ControlInformation();
			ci.setType(ControlInfo.Type.DICTIONARY);

			if (entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			BufferedInputStream bis=null;
			if (entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				try {
					bis = new BufferedInputStream(tais);
					ci.load(bis);
					dict.load(bis, ci, new ProgressOut());
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			entry = tais.getNextTarEntry();
			if (entry.getName().equals(GRPWriter.GRAMMAR_ENTRY_NAME)) {
				grammarArr = tais.readAllBytes();
			}
			if (entry.getName().equals(GRPWriter.DICTIONARY_ENTRY_NAME)) {
				try {
					bis = new BufferedInputStream(tais);
					ci.load(bis);
					dict.load(bis, ci, new ProgressOut());
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			if(bis!=null){
				bis.close();
			}
			return grammarArr;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

		 */
	}
}
