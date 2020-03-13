package org.dice_group.grp.compression.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.lang.CollectorStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
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
import org.dice_group.grp.util.*;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCFourSectionDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdtjena.NodeDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFCompressor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RDFCompressor.class);
	private static final long THRESHOLD = 2;
	private static final long MAX = Integer.MAX_VALUE;
	private final boolean threaded;
	private boolean onlyKD = true;

	public RDFCompressor(){
		this(false);
	}

	public RDFCompressor(boolean threaded){
		this.threaded=threaded;
	}

	public DigramHelper dh3 = new DigramHelper();
	private TempDictionary dict;

	public File compressRDF(File rdfFile, String out, Boolean kd2Flag, Boolean onlyKD)
			throws NotAllowedInRDFException, NotSupportedException, IOException, ExecutionException, InterruptedException {
		this.onlyKD=onlyKD;
		Stats.printMemStats();
		Stats.setCurrentFileName(rdfFile.getName());

		long s = Calendar.getInstance().getTimeInMillis();
		//Model graph = readFileToModel(rdfFile);
		List<String> soIndex= new ArrayList<String>();
		HDTSpecification spec = new HDTSpecification();
		spec.set("tempDictionary.impl", DictionaryFactory.MOD_DICT_IMPL_HASH_PSFC);

		dict = DictionaryFactory.createTempDictionary(spec);

		SortedSet<PTriple> triples = readFileToTriples(rdfFile, soIndex);
		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("reading took " + (e - s) + " ms");

		// Map<Long, String> dict = new HashMap<Long, String>();


		Grammar grammar = createGrammar(triples, rdfFile, soIndex);
		Map<Integer, Integer> map= new HashMap<Integer, Integer>();
		/*
		for (int edge : grammar.getStart().getEdges()){
			String uri = grammar.getProps().getBounded(edge).getRDFNode().asNode().getURI();
			if(uri.startsWith(GrammarHelper.NON_TERMINAL_PREFIX)) {
				Integer i = Integer.valueOf(uri.replace(GrammarHelper.NON_TERMINAL_PREFIX, ""));
				map.putIfAbsent(i, 0);
				map.put(i, map.get(i) + 1);
			}
		}
		*/
		Stats.printMemStats();
		IntBasedIndexer indexer = new IntBasedIndexer(dict);
		grammar = indexer.indexGrammar(grammar);
		Stats.printMemStats();
		System.gc();
		GRPWriter.save(out, grammar, indexer.getDict(), kd2Flag, threaded);
		GrammarHelper.reset();
		return new File(out);
	}

	public Grammar createGrammar(SortedSet<PTriple> graph, File f, List<String> soIndex) throws NotAllowedInRDFException, IOException {
		long origSize = graph.size();

		long s = Calendar.getInstance().getTimeInMillis();


		BoundedList pIndex = new BoundedList();
		Grph g = this.rdfToGrph(graph, soIndex, pIndex, new InMemoryGrph());

		Grammar grammar = new Grammar(g);
		grammar.setProps(pIndex);
		grammar.setSOIndex(soIndex);
		GrammarHelper.setStartIndexForNT(pIndex.getHighestBound()+1);

		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("converting took " + (e - s) + " ms");

		if(onlyKD){
			return grammar;
		}

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
			if (mfd.getNoOfOccurences() > MAX) {
				continue;
			}
			Set<Integer> le = new HashSet<Integer>();
			List<DigramOccurence> replaced = new ArrayList<DigramOccurence>();
			if(mfd.getStructure()==4){
				System.out.println();
			}
			Integer uriNT = replaceAllOccurences(digrams.get(mfd), g, le, pIndex, replaced);

			System.out.println("Graph size " + g.getEdges().size());

			if (replaced == null || uriNT ==null) {

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
				String uri = grammar.getProps().getBounded(edge).getRDFNode();
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
		grammar.setVSize(grammar.getVSize()+grammar.getRules().size());
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
		if(collection.isEmpty()){
			return null;
		}
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
				node.setRDFNode(uri);
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
			//if(uri.equals("http://n.1")){
				if(occ.getExternals().contains(3770)){
					System.out.println();
				}
			//}
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

	private SortedSet<PTriple>  readFileToTriples(File rdfFile, List<String> soIndex) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());
		//List<Triple> triples = new ArrayList();
		RDFNotation notation = RDFNotation.guess(rdfFile);
		RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);

		//TODO can we get a boundedlist
		SortedSet<PTriple> set = new TreeSet<PTriple>();

		//Map<Integer, List<PTriple2>> map = new HashMap<Integer, List<PTriple2>>();
		//we know the keySet implicitly 1 ... map.size()
		try {
			parser.doParse(rdfFile.getAbsolutePath(), "", notation, new RDFParserCallback.RDFCallback() {
				@Override
				public void processTriple(TripleString triple, long pos) {

					set.add(new PTriple(triple.getSubject().toString(), triple.getPredicate().toString(), triple.getObject().toString()));
				}
			});
		} catch (ParserException e) {
			e.printStackTrace();
		}
		/*
		StreamRDF stream = new StreamRDF() {

			@Override
			public void start() {
				set.clear();
			}

			@Override
			public void triple(Triple triple) {

				set.add(new PTriple(triple));
			}

			@Override
			public void quad(Quad quad) {}

			@Override
			public void base(String base) {}

			@Override
			public void prefix(String prefix, String iri) {

			}

			@Override
			public void finish() {
			}
		};
		RDFDataMgr.parse(stream, new FileInputStream(rdfFile), lang);
		*/
		return set;
		//return set;
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
	private Grph rdfToGrph(SortedSet<PTriple> m, List<String> soIndex, BoundedList propertyIndex, Grph g){
		//sort after properties
		/*List<Statement> stmts = m.listStatements().toList();
		m.remove(stmts);
		Collections.sort(stmts, new Comparator<Statement>() {
			@Override
			public int compare(Statement s1, Statement s2) {
				return s1.getPredicate().toString().compareTo(s2.getPredicate().toString());
			}
		});
		*/


		int p = 0;
		String oldP = null;
		Map<Integer, Integer> nodeID = new HashMap<Integer, Integer>();

		//for i in (1...map.size()) map get at i -> for each PTriple2() add

		for(PTriple t : m){

			int s = getNodeIndex(t.getSubject(), soIndex, nodeID);
			IndexedRDFNode iNode = new IndexedRDFNode();
			iNode.setRDFNode(t.getPredicate());
			//basically just check if the last RDFNode was the same
			//int i = propertyIndex.indexOf(iNode);
			if(oldP!=null && oldP.equals(t.getPredicate())){
				// if exists, just set upper bound
				iNode = propertyIndex.get(propertyIndex.size()-1);
				iNode.setUpperBound(iNode.getUpperBound()+1);
			}
			else{
				iNode.setLowerBound(p);
				iNode.setUpperBound(p);
				propertyIndex.add(iNode);
				oldP=t.getPredicate();
			}

			//int p = soIndex.size();
			//We have to add a unique index for every predicate -> thus having |E| amount of
			//soIndex.add(stmt.getPredicate());
			int o = getNodeIndex(t.getObject(), soIndex, nodeID);

			//soIndex.add(stmt.getSubject());
			//soIndex.add(stmt.getObject());
			g.addSimpleEdge(s, p++, o,
					true);
			if(p% 1000000 ==0){
				System.out.println("Converted "+p+" edges");
			}
		}


		//Stats.printStats(g, propertyIndex);
		BlankNodeIDGenerator.reset();
		m.clear();
		return g;
	}

	private int getNodeIndex(String node, List<String> index, Map<Integer, Integer> nodeID) {
		//maybe as tmpIndex directly
		// int n = index.indexOf(node);
		int ret =  addObject(node, dict).intValue();
		if(ret>index.size()){
			if(node.startsWith("_:")) {
				index.add("_:"+BlankNodeIDGenerator.getID(node));
			}
			else {
				index.add(node);
			}
		}
		else{
			System.out.print("");
		}
		return ret-1;

		//nodeID.put(hash, index.size()-1);
		//return index.size()-1;
	}

	private Long addObject(String n, TempDictionary dict){
		Long o1;
		if(n.startsWith("\"") || n.startsWith("\'")){
			//o1 = dict.insert(escape(n), TripleComponentRole.OBJECT);
			o1 = dict.insert(n, TripleComponentRole.OBJECT);

		}
		else if(n.startsWith("_:")){
			//blank node
			o1 = dict.insert("_:"+BlankNodeIDGenerator.getID(n), TripleComponentRole.OBJECT);
		}
		else {
			o1 = dict.insert(n, TripleComponentRole.OBJECT);
		}
		if(o1==-1){
			System.out.println();
		}
		return o1;
	}
	private String escape(String literal){

		return literal.replace("\"", "\\\"").replace("_", "\\_").replace("-", "\\-").trim();
	}

}
