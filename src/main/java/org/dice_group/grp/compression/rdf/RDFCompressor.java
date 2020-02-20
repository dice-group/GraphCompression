package org.dice_group.grp.compression.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.*;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dice_group.grp.compression.GRPWriter;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramHelper;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Indexer;
import org.dice_group.grp.index.impl.IntBasedIndexer;
import org.dice_group.grp.index.impl.URIBasedIndexer;
//import org.jgrapht.graph.AbstractBaseGraph;
//import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.DirectedMultigraph;
//import org.jgrapht.graph.DirectedPseudograph;
import org.dice_group.grp.util.BoundedList;
import org.dice_group.grp.util.DigramOccurenceComparator;
import org.dice_group.grp.util.IndexedRDFNode;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFCompressor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RDFCompressor.class);
	private static final long THRESHOLD = 1;

	public DigramHelper dh3 = new DigramHelper();
	
	public File compressRDF(File rdfFile, String out, Boolean kd2Flag)
			throws NotAllowedInRDFException, NotSupportedException, IOException {


		long s = Calendar.getInstance().getTimeInMillis();
		Model graph = readFileToModel(rdfFile);
		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("reading took " + (e - s) + " ms");

		// Map<Long, String> dict = new HashMap<Long, String>();
		HDTSpecification spec = new HDTSpecification();
		spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);

		TempDictionary dict = DictionaryFactory.createTempDictionary(spec);

		Grammar grammar = createGrammar(graph, rdfFile);
		Map<Integer, Integer> map= new HashMap<Integer, Integer>();
		for (int edge : grammar.getStart().getEdges()){
			String uri = grammar.getProps().getBounded(edge).getRDFNode().asNode().getURI();
			if(uri.startsWith(GrammarHelper.NON_TERMINAL_PREFIX)) {
				Integer i = Integer.valueOf(uri.replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
				map.putIfAbsent(i, 0);
				map.put(i, map.get(i) + 1);
			}
		}

		IntBasedIndexer indexer = new IntBasedIndexer(dict);
		grammar = indexer.indexGrammar(grammar);
		GRPWriter.save(out, grammar, indexer.getDict(), kd2Flag);
		return new File(out);
	}

	public Grammar createGrammar(Model graph, File f) throws NotAllowedInRDFException, IOException {
		long origSize = graph.size();

		long s = Calendar.getInstance().getTimeInMillis();


		List<RDFNode> soIndex= new ArrayList<RDFNode>();
		BoundedList pIndex = new BoundedList();
		Grph g = this.rdfToGrph(graph, soIndex, pIndex, new InMemoryGrph());

		Grammar grammar = new Grammar(g);
		grammar.setProps(pIndex);
		grammar.setSOIndex(soIndex);
		GrammarHelper.setStartIndexForNT(pIndex.getHighestBound()+1);

		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("converting took " + (e - s) + " ms");

		Map<Digram, List<DigramOccurence>> digrams = dh3.getMappingVertex(g, pIndex);

		List<Digram> frequenceList = dh3.sortDigrambyFrequence(digrams);
		System.out.println("Found " + digrams.size() + " Digrams");

		dh3.removeOverlappingOcc(frequenceList, digrams);
		System.out.println("Found " + frequenceList.size() + " non overlapping digrams");

		int i = 0;
		for (Digram d : frequenceList) {
			System.out.println("Digram " + i++ + " has " + digrams.get(d).size() + " Occurences.");
		}
		//int head = g.getDirectedSimpleEdgeHead(2836);
		System.out.println("Prework done. Onto the algorithm...");
		Set<Digram> occured = new HashSet<Digram>();
		while (frequenceList.size() > 0) {
			Digram mfd = frequenceList.get(0);
			if (occured.contains(mfd)) {
				digrams.remove(mfd);
				frequenceList.remove(mfd);
				continue;
			}
			occured.add(mfd);
			if (mfd.getNoOfOccurences() <= THRESHOLD) {
				break;
			}
			if (mfd.getNoOfOccurences() > 100000) {
				System.out.println("WHAAT");
			}
			//TODO set NT first
			// we need to set the replaced in the same order
			Set<Integer> le = new HashSet<Integer>();
			List<DigramOccurence> replaced = new ArrayList<DigramOccurence>();
			int uriNT = replaceAllOccurences(digrams.get(mfd), g, le, pIndex, replaced);
			System.out.println("Graph size " + g.getEdges().size());

			if (replaced == null) {

				digrams.remove(mfd);
				frequenceList.remove(mfd);
				continue;
			}
			grammar.getReplaced().put(mfd, replaced);
			grammar.addRule(uriNT, mfd);

			digrams.remove(mfd);
			frequenceList.remove(mfd);

			digrams.putAll(dh3.findNewMappingsVertex(g, le, pIndex));
			//check which is better (faster, accuracy)
			//digrams = dh3.getMappingVertex(g, pIndex);

			long startSDF = Calendar.getInstance().getTimeInMillis();
			frequenceList = dh3.sortDigrambyFrequence(digrams);
			long endSDF = Calendar.getInstance().getTimeInMillis();
			System.out.println("Sorting took " + (endSDF - startSDF) + " ms");

			long startROO = Calendar.getInstance().getTimeInMillis();
			dh3.removeOverlappingOcc(frequenceList, digrams);
			long endROO = Calendar.getInstance().getTimeInMillis();
			System.out.println("Find & Remove Overlapping took " + (endROO - startROO) + " ms");

			System.out.println("Found " + frequenceList.size() + " non overlapping digrams");

			i = 0;
			int occCount = 0;
			for (Digram d : frequenceList) {
				i++;
				occCount += digrams.get(d).size();
			}
			Map<Integer, Integer> map= new HashMap<Integer, Integer>();
			for (int edge : grammar.getStart().getEdges()){
				String uri = grammar.getProps().getBounded(edge).getRDFNode().asNode().getURI();
				if(uri.startsWith(GrammarHelper.NON_TERMINAL_PREFIX)) {
					Integer ix = Integer.valueOf(uri.replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
					map.putIfAbsent(ix, 0);
					map.put(ix, map.get(ix) + 1);
				}
			}
			System.out.println(i + " Digrams with " + occCount + " Occurences.");
		}
		System.out.println("Start size " + grammar.getStart().getEdges().size() + " to original size " + origSize + " [ratio: "
				+ (grammar.getStart().getEdges().size() * 1.0 / origSize) + "]");
		System.out.println("No Of Rules " + grammar.getRules().size());
		e = Calendar.getInstance().getTimeInMillis();
		System.out.println("Grammar compression took " + (e - s) + " ms");
		System.out.println("Grammar done. Onto indexing & serialization...");

		return grammar;
	}


	/**
	 * Replaces all Digram Occurences in graph with uriNT and returns the new graph
	 * Be Aware that the original graph will be changed.
	 *
	 *
	 * We will encounter a little problem here, as we exchange a lower_bound occ, so we need to get the original id, and not the lower bound,
	 *  we could handle this by saving the orig edge ID in the occ as well. Then this is a piece of cake
	 *  Done
	 * 
	 * @param collection
	 * @param g
	 * @return
	 * @throws NotAllowedInRDFException
	 */
	protected Integer replaceAllOccurences(List<DigramOccurence> collection,
			Grph  g, Set<Integer> checkEdges, BoundedList pIndex, List<DigramOccurence> replaced) {
		//use BoundedList for NonTerminals too
		IndexedRDFNode node = new IndexedRDFNode();
		boolean first = true;
		Integer firstNT=-1;
		// sort collection first so we can decompress in the same order
		Collections.sort(collection, new DigramOccurenceComparator());
		String uri =GrammarHelper.getNextNonTerminal();

		for(DigramOccurence occ : collection){
			if(occ==null){
				continue;
			}
			if (occ.getExternals().size() >2) {
				continue;
			}
			Integer nt = GrammarHelper.getNextNonTerminalInt();
			checkEdges.add(nt);
			if(first){
				firstNT=nt;
				node.setLowerBound(nt);
				node.setRDFNode(ResourceFactory.createResource(uri));
				pIndex.add(node);
				first=false;
			}
			node.setUpperBound(nt);
			replaced.add(occ);
				try {
					g.removeEdge(occ.getOrigE1());
					g.removeEdge(occ.getOrigE2());
					for (Integer v : occ.getInternals()) {
						//g.removeVertex(v);
					}
				}catch(NullPointerException e){
					e.printStackTrace();
					//System.out.println(occ);
					System.out.println(occ.getOrigE1());
					System.out.println();
				}
				catch(IllegalArgumentException e){
					e.printStackTrace();
					//System.out.println(occ);
					//System.out.println(occ.getOrigE1());
					//System.out.println();
				}

			List<Integer> ext = occ.getExternals();
			if (occ.getExternals().size() == 1) {
				Integer s = occ.getExternals().get(0);
				g.addSimpleEdge(s, nt ,s, true);
			}
			if (occ.getExternals().size() == 2) {
				Integer s = occ.getExternals().get(0);
				Integer o = occ.getExternals().get(1);
				g.addSimpleEdge(s, nt ,o, true);
			}
		}

		return firstNT;

	}
	/*
        /**
         *
         * @param uriNT
         * @param occurrence
         * @return
         *
	private Statement getReplacingStatement(String uriNT, DigramOccurence occurrence) {
		Statement stmt = null;
		Property p = ResourceFactory.createProperty(uriNT);
		// add uriNT between the external nodes
		// 1 external node
		// is it an edge to itself then?
		if (occurrence.getExternals().size() == 1) {
			Resource ext = occurrence.getExternals().get(0).asResource();
			stmt = ResourceFactory.createStatement(ext, p, ext);
		}
		// 2 external nodes
		// get first and second => add edge with uriNT
		if (occurrence.getExternals().size() == 2) {
			stmt = ResourceFactory.createStatement(occurrence.getExternals().get(0).asResource(), p,
					occurrence.getExternals().get(1));
		}
		return stmt;
	}
*/
	private Model readFileToModel(File rdfFile) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());
		return ModelFactory.createDefaultModel().read(new FileReader(rdfFile), null, lang.getLabel());
	}

	/**
	 * Converts a Jena Model into Grph graph using a map which converts a hash to an RDF Node.
	 * Thus converting each rdf node into a hash, while we can still access the underlying rdf node.
	 * This is sooo dumb. Well...
	 *
	 * @param m Model to convert from
	 * @param soIndex index to save mapping to
	 * @param g Grph to convert to
	 * @return converter Grph Object
	 */
	private Grph rdfToGrph(Model m, List<RDFNode> soIndex, BoundedList propertyIndex, Grph g){
		//sort after properties
		List<Statement> stmts = m.listStatements().toList();
		Collections.sort(stmts, new Comparator<Statement>() {
			@Override
			public int compare(Statement s1, Statement s2) {
				return s1.getPredicate().toString().compareTo(s2.getPredicate().toString());
			}
		});
		int p = 0;
		RDFNode oldP = null;
		Map<Integer, Integer> nodeID = new HashMap<Integer, Integer>();
		for(Statement stmt : stmts){

			int s = getNodeIndex(stmt.getSubject(), soIndex, nodeID);
			IndexedRDFNode iNode = new IndexedRDFNode();
			iNode.setRDFNode(stmt.getPredicate());
			//basically just check if the last RDFNode was the same
			//int i = propertyIndex.indexOf(iNode);
			if(oldP!=null && oldP.toString().equals(stmt.getPredicate().toString())){
				// if exists, just set upper bound
				iNode = propertyIndex.get(propertyIndex.size()-1);
				iNode.setUpperBound(iNode.getUpperBound()+1);
			}
			else{
				iNode.setLowerBound(p);
				iNode.setUpperBound(p);
				propertyIndex.add(iNode);
				oldP=stmt.getPredicate();
			}

			//int p = soIndex.size();
			//We have to add a unique index for every predicate -> thus having |E| amount of
			//soIndex.add(stmt.getPredicate());
			int o = getNodeIndex(stmt.getObject(), soIndex, nodeID);

			//soIndex.add(stmt.getSubject());
			//soIndex.add(stmt.getObject());
			g.addSimpleEdge(s, p++, o,
					true);

		}
		return g;
	}

	private int getNodeIndex(RDFNode node, List<RDFNode> index, Map<Integer, Integer> nodeID) {
		// int n = index.indexOf(node);
		int hash = JenaNodeFormatter.format(node.asNode()).hashCode();
		if(nodeID.containsKey(hash)){
			return nodeID.get(hash);
		}

		index.add(node);
		nodeID.put(hash, index.size()-1);
		return index.size()-1;
	}


}
