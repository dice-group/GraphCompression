package org.dice_group.grp.compression.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFCompressor {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(RDFCompressor.class);

		
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
	

	public Grammar createGrammar(Model graph) throws NotAllowedInRDFException {
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
			String uriNT = GrammarHelper.getNextNonTerminal();
			graph = replaceAllOccurences(uriNT, digrams.get(mfd), graph);
			grammar.addRule(uriNT, mfd);
			grammar.getReplaced().put(mfd, digrams.get(mfd));
			updateOccurences(digrams, frequenceList, graph, uriNT);
		}		
		return grammar;
	}
	
	/**
	 * iterates through the grammar's rules and decompresses the statements pertinent to each digram
	 * @param grammar
	 * @param replaced
	 * @return
	 */
	public Model decompressGrammar(Grammar grammar) {
		Model graph = ModelFactory.createDefaultModel();
		graph.add(grammar.getStart());
		
		Map<String, Digram> rules = grammar.getRules();
		rules.forEach((uriNT, digram)->{
			replaceStmts(uriNT, digram, graph, grammar.getReplaced());
		});
		return graph;
	}
	
	/**
	 * substitutes the compressed statement with the original statements
	 */
	private void replaceStmts(String uriNT, Digram digram, Model graph, Map<Digram, Set<DigramOccurence>> replaced) {
		List<DigramOccurence> digOccurs = new ArrayList<DigramOccurence>(replaced.get(digram));
		for(DigramOccurence curOccur: digOccurs) {
			Statement curStmt = getReplacingStatement(uriNT, curOccur);
			if(curStmt!=null && graph.contains(curStmt)) {
				graph.remove(curStmt);
				graph.add(curOccur.getEdge1());
				graph.add(curOccur.getEdge2());
			}
		}
	}

	/**
	 * 1) removes the most frequent digram, along with its occurrences, from the map
	 * 2) finds the new digrams revolving aroung the newly added statements
	 * 3) updates the map and sorts the frequency list
	 * 
	 * @param digrams map of digrams to its corresponding non-overlapping occurrences
	 * @param frequency sorted digrams by frequency
	 * @param graph
	 * @param uriNT
	 */
	protected void updateOccurences(Map<Digram, Set<DigramOccurence>> digrams, List<Digram> frequency, Model graph, String uriNT) {
		// remove mfd and the replaced occurrences
		Digram mfd = frequency.get(0);
		frequency.remove(0);
		digrams.remove(mfd);
			
		RDFNode nullNode = null;
		//the new statements will have uriNT as predicate
		StmtIterator iterator = graph.listStatements(null, ResourceFactory.createProperty(uriNT), nullNode);
		Set<DigramOccurence> occurrences = new HashSet<DigramOccurence>();
		while(iterator.hasNext()) {
			Statement curStmt = iterator.next();
			occurrences.addAll(DigramHelper.findStmtBasedDigrams(graph, curStmt));
		}	
		
		digrams.forEach((digram,occrs)->{
			DigramHelper.updateExternals(occrs, graph);
		});
		
		Map<Digram, Set<DigramOccurence>> newEntries = DigramHelper.findNonOverOccurrences(occurrences);
		DigramHelper.mergeMaps(digrams, newEntries);
		DigramHelper.updateDigramCount(digrams);
		frequency = DigramHelper.sortDigrambyFrequence(digrams.keySet());
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
			Statement stmt = getReplacingStatement(uriNT, docc);
			if(stmt!=null)
				graph.add(stmt);
		
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
	
	/**
	 * 
	 * @param uriNT
	 * @param occurrence
	 * @return
	 */
	private Statement getReplacingStatement(String uriNT, DigramOccurence occurrence) {
		Statement stmt = null;
		Property p = ResourceFactory.createProperty(uriNT);
		//add uriNT between the external nodes
		// 1 external node 
		// is it an edge to itself then? 
		if(occurrence.getExternals().size()==1) {
			Resource ext = occurrence.getExternals().get(0).asResource();
			stmt = ResourceFactory.createStatement(ext, p, ext);
		}
		// 2 external nodes
		// get first and second => add edge with uriNT
		if(occurrence.getExternals().size()==2) {				
			stmt = ResourceFactory.createStatement(occurrence.getExternals().get(0).asResource(), p, occurrence.getExternals().get(1));
		}
		return stmt;
	}

	private Model readFileToModel(File rdfFile) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());
		return ModelFactory.createDefaultModel().read(new FileReader(rdfFile), null, lang.toString());
	}
	
}
