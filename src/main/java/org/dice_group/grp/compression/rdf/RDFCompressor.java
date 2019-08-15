package org.dice_group.grp.compression.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dice_group.grp.compression.DictionaryCompression;
import org.dice_group.grp.compression.Finalizer;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;

public class RDFCompressor {

	private GrammarHelper grammarHelper = new GrammarHelper();
	
	public File compressRDF(File rdfFile) throws FileNotFoundException{
		Model graph = readFileToModel(rdfFile);
		Map<Long, String> dict = new HashMap<Long, String>();
		Grammar grammar = createGrammar(graph, dict);
		//TODO prune grammar
		//TODO dict -> dict++
		DictionaryCompression.compress(dict);
		//TODO compress grammar and dict into bin file
		Finalizer.save(grammar, dict);
		//DONE
		return null;
	}
	
	private Grammar createGrammar(Model graph, Map<Long, String> dict) {
		Grammar grammar = new Grammar(graph);
		//List<Digram> frequenceList = new ArrayList<Digram>();
		//Map<Digram, Set<DigramOccurence>> digrams = DigramHelper.findDigramsOcc(graph, frequenceList);
		Set<DigramOccurence> occurrences = DigramHelper.findDigramOccurrences(graph);
		Map<Digram, Set<DigramOccurence>> digrams = DigramHelper.findNonOverOccurrences(occurrences);
		List<Digram> frequenceList = DigramHelper.sortDigrambyFrequence(digrams.keySet());
		while(frequenceList.size()>0) {
			Digram mfd = frequenceList.get(0);
			if(mfd.getNoOfOccurences()<=1) {
				break;
			}
			String uriNT = grammarHelper.getNextNonTerminal();
			graph = replaceAllOccurences(uriNT, digrams.get(mfd), graph, dict);
			grammar.addRule(uriNT, graph);
			updateOccurences(digrams, frequenceList);
		}
		return grammar;
	}

	/**
	 * The method will update the Occurence Lists and frequenceLists accordingly 
	 * 
	 * @param digrams
	 * @param frequency
	 */
	private void updateOccurences(Map<Digram, Set<DigramOccurence>> digrams, List<Digram> frequency) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Replaces all Digram Occurences in graph with uriNT and returns the new graph
	 * Be Aware that the original graph will be changed.
	 * 
	 * Also this will update the Dictionary accordingly
	 * 
	 * @param uriNT
	 * @param set
	 * @param graph
	 * @param dict 
	 * @return
	 */
	private Model replaceAllOccurences(String uriNT, Set<DigramOccurence> set, Model graph, Map<Long, String> dict) {
		// TODO Auto-generated method stub
		return null;
	}

	private Model readFileToModel(File rdfFile) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());
		return ModelFactory.createDefaultModel().read(new FileReader(rdfFile), null, lang.toString());
	}
	
}
