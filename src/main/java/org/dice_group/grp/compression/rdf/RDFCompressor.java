package org.dice_group.grp.compression.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.io.IOException;
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
import org.dice_group.grp.decompression.GRPReader;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Indexer;
import org.dice_group.grp.index.impl.URIBasedIndexer;
import org.dice_group.grp.index.impl.URIBasedSearcher;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFCompressor {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(RDFCompressor.class);

		
	public File compressRDF(File rdfFile) throws NotAllowedInRDFException, NotSupportedException, IOException{
		Model graph = readFileToModel(rdfFile);
		//Map<Long, String> dict = new HashMap<Long, String>();
		TempDictionary dict = DictionaryFactory.createTempDictionary(new HDTSpecification());
		
		Grammar grammar = createGrammar(graph);
		
		Indexer indexer = new URIBasedIndexer(dict);
		grammar = indexer.indexGrammar(grammar);
		GRPWriter.save("CHANGE TO USER SPECIFIED NAME", grammar, indexer.getDict());
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
			//we need to set the replaced in the same order
			grammar.getReplaced().put(mfd, replaceAllOccurences(uriNT, digrams.get(mfd), graph));
			grammar.addRule(uriNT, mfd);
			updateOccurences(digrams, frequenceList, graph, uriNT);
		}		
		return grammar;
	}
	
	public Model decompress(String file) {
		DictionaryPrivate dict = DictionaryFactory.createDictionary(new HDTSpecification());
		try {
			Grammar g = GRPReader.load(file, dict);
			URIBasedSearcher searcher = new URIBasedSearcher(dict);
			searcher.deindexGrammar(g);
			return decompressGrammar(g);
		} catch (NotSupportedException | IOException e) {
			e.printStackTrace();
		}
		return null;
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
			replaceStmts(uriNT, digram, graph, grammar.getReplaced().get(digram));
		});
		return graph;
	}
	
	/**
	 * substitutes the compressed statement with the original statements
	 */
	private void replaceStmts(String uriNT, Digram digram, Model graph, List<DigramOccurence> occurences) {
		List<DigramOccurence> digOccurs = new ArrayList<DigramOccurence>(occurences);
		List<Statement> stmts = graph.listStatements(null, ResourceFactory.createProperty(uriNT),(RDFNode)null).toList();
		// sort stmts after first and second node alphabetically
		Collections.sort(stmts, new Comparator<Statement>() {

			@Override
			public int compare(Statement arg0, Statement arg1) {
				String e1 = arg0.getSubject().toString()+arg0.getObject().toString();
				String e2 = arg1.getSubject().toString()+arg1.getObject().toString();
				return e1.compareTo(e2);
			}
			
		});
		for(int i=0;i<stmts.size();i++) {
			graph.remove(stmts.get(i));
			//TODO replace placeholder in OCC with actual external nodes
			graph.add(digOccurs.get(i).getEdge1());
			graph.add(digOccurs.get(i).getEdge2());
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
	protected List<DigramOccurence> replaceAllOccurences(String uriNT, Set<DigramOccurence> set, Model graph) throws NotAllowedInRDFException {
		Property p = ResourceFactory.createProperty(uriNT);
		List<DigramOccurence> replaced = new LinkedList<DigramOccurence>();
		for(DigramOccurence docc : set) {
			replaced.add(docc);
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
		return replaced;
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
