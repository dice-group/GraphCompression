package org.dice_group.grp.compression.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dice_group.grp.compression.GRPWriter;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Indexer;
import org.dice_group.grp.index.impl.URIBasedIndexer;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.options.HDTSpecification;

public class RDFCompressor {

		
	public File compressRDF(File rdfFile) throws FileNotFoundException, NotAllowedInRDFException{
		Model graph = readFileToModel(rdfFile);
		//Map<Long, String> dict = new HashMap<Long, String>();
		TempDictionary dict = DictionaryFactory.createTempDictionary(new HDTSpecification());
		
		Grammar grammar = createGrammar(graph);
		//TODO prune grammar
		
		//TODO compress grammar and dict into bin file
		Indexer indexer = new URIBasedIndexer(dict);
		grammar = indexer.indexGrammar(grammar);
		GRPWriter.save(grammar, dict);
		//DONE
		return null;
	}
	

	private Grammar createGrammar(Model graph) throws NotAllowedInRDFException {
		Grammar grammar = new Grammar(graph);
		List<Digram> frequenceList = new ArrayList<Digram>();
		Map<Digram, Set<DigramOccurence>> digrams = DigramHelper.findDigramsOcc(graph, frequenceList);
		while(frequenceList.size()>0) {
			Digram mfd = frequenceList.get(0);
			if(mfd.getNoOfOccurences()<=1) {
				break;
			}
			String uriNT = GrammarHelper.getNextNonTerminal();
			graph = replaceAllOccurences(uriNT, digrams.get(mfd), graph);
			grammar.addRule(uriNT, mfd);
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
		// TODO @Felix
		
	}

	/**
	 * Replaces all Digram Occurences in graph with uriNT and returns the new graph
	 * Be Aware that the original graph will be changed.
	 * 
	 * 
	 * @param uriNT
	 * @param set
	 * @param graph 
	 * @return
	 * @throws NotAllowedInRDFException 
	 */
	protected Model replaceAllOccurences(String uriNT, Set<DigramOccurence> set, Model graph) throws NotAllowedInRDFException {
		Property p = ResourceFactory.createProperty(uriNT);
		for(DigramOccurence docc : set) {
			graph.remove(docc.getEdge1());
			graph.remove(docc.getEdge2());
			//add uriNT between the external nodes
			// 1 external node 
			// is it an edge to itself then? 
			if(docc.getExternals().size()==1) {
				Resource ext = docc.getExternals().get(0).asResource();
				graph.add(ext, p, ext);
			}
			// 2 external nodes
			// get first and second => add edge with uriNT
			if(docc.getExternals().size()==2) {				
				graph.add(docc.getExternals().get(0).asResource(), p, docc.getExternals().get(1));
			}
			// 3 external nodes. 
			// not possible in RDF if i am correct without adding a node, which makes it only bigger
			if(docc.getExternals().size()>2) {
				throw new NotAllowedInRDFException("Digrams cannot have more than 2 externals in RDF");
			}
			//docc.getExternals();
			//graph.add(graph);
		}
		return graph;
	}

	private Model readFileToModel(File rdfFile) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());
		return ModelFactory.createDefaultModel().read(new FileReader(rdfFile), null, lang.toString());
	}
	
}
