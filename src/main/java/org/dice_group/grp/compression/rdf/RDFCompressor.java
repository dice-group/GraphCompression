package org.dice_group.grp.compression.rdf;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.xerces.util.URI;
import org.dice_group.grp.compression.GRPWriter;
import org.dice_group.grp.compression.impl.KD2TreeCompressor;
import org.dice_group.grp.exceptions.NotAllowedInRDFException;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.index.impl.IntBasedIndexer;
import org.dice_group.grp.util.BlankNodeIDGenerator;
import org.dice_group.grp.util.PTriple;
import org.dice_group.grp.util.Stats;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


public class RDFCompressor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RDFCompressor.class);
	private final boolean threaded;

	public RDFCompressor(){
		this(false);
	}

	public RDFCompressor(boolean threaded){
		this.threaded=threaded;
	}

	private TempDictionary dict;


	public File compressRDF(File rdfFile, String out)
			throws NotAllowedInRDFException, NotSupportedException, IOException, ExecutionException, InterruptedException {
		Stats.printMemStats();
		Stats.setCurrentFileName(rdfFile.getName());

		long s = Calendar.getInstance().getTimeInMillis();
		//Model graph = readFileToModel(rdfFile);
		HDTSpecification spec = new HDTSpecification();
		spec.set("tempDictionary.impl", DictionaryFactory.MOD_DICT_IMPL_HASH_PSFC);

		dict = DictionaryFactory.createTempDictionary(spec);

		readFile(rdfFile);
		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("reading took " + (e - s) + " ms");

		Stats.printMemStats();
		KD2TreeCompressor compressor;

		compressor = new KD2TreeCompressor(threaded, Long.valueOf(dict.getPredicates().getNumberOfElements()).intValue());
		s = Calendar.getInstance().getTimeInMillis();
		IntBasedIndexer indexer = new IntBasedIndexer(dict);

		indexer.indexTriples(rdfFile, compressor.getSerializer());
		e = Calendar.getInstance().getTimeInMillis();
		System.out.println("indexing took " + (e - s) + " ms");

		GRPWriter grpWriter = new GRPWriter(out);
		grpWriter.saveDict(indexer.getDict());
		long sSize = dict.getSubjects().getNumberOfElements();
		long oSize = dict.getObjects().getNumberOfElements();
		dict.clear();
		System.gc();
		Stats.printMemStats();
		grpWriter.save(compressor, indexer.getDict(),threaded,sSize, oSize);
		grpWriter.close();
		return new File(out);
	}

	public File compressRDFFast(File rdfFile, String out)
			throws NotAllowedInRDFException, NotSupportedException, IOException, ExecutionException, InterruptedException {
		Stats.printMemStats();
		Stats.setCurrentFileName(rdfFile.getName());

		long s = Calendar.getInstance().getTimeInMillis();
		//Model graph = readFileToModel(rdfFile);
		HDTSpecification spec = new HDTSpecification();
		spec.set("tempDictionary.impl", DictionaryFactory.MOD_DICT_IMPL_HASH_PSFC);

		dict = DictionaryFactory.createTempDictionary(spec);
		List<String> predicates = new ArrayList<String>();
		List<String> prefixMapping= new ArrayList<>();
		Map<String, Integer> prefix2 = new HashMap<String, Integer>();

		List<PTriple>  triples = readFileToTriples(rdfFile, predicates, prefixMapping, prefix2);
		prefix2.clear();
		long e = Calendar.getInstance().getTimeInMillis();
		System.out.println("reading took " + (e - s) + " ms");

		Stats.printMemStats();
		KD2TreeCompressor compressor;
		compressor = new KD2TreeCompressor(threaded, predicates.size());
		IntBasedIndexer indexer = new IntBasedIndexer(dict);
		s = Calendar.getInstance().getTimeInMillis();
		indexer.indexTriples(triples, predicates, compressor.getSerializer(), prefixMapping);
		prefixMapping.clear();
		e = Calendar.getInstance().getTimeInMillis();
		System.out.println("indexing took " + (e - s) + " ms");

		GRPWriter grpWriter = new GRPWriter(out);
		grpWriter.saveDict(indexer.getDict());
		long sSize = dict.getSubjects().getNumberOfElements();
		long oSize = dict.getObjects().getNumberOfElements();
		dict.clear();
		System.gc();
		Stats.printMemStats();
		grpWriter.save(compressor, indexer.getDict(),threaded,sSize, oSize);
		grpWriter.close();
		return new File(out);
	}


	private void readFile(File rdfFile){
		RDFNotation notation = RDFNotation.guess(rdfFile);
		RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);
		AtomicReference<Integer> count= new AtomicReference<>(0);
		//get namespaces -> prefix mapping  (trade off, time - mem)
		Map<Integer, Integer> bNodes = new HashMap<Integer, Integer>();

		try {
			parser.doParse(rdfFile.getAbsolutePath(), "", notation, (triple, l) -> {

				String subject = triple.getSubject().toString();
				if(subject.startsWith("_:")){
					if(bNodes.containsKey(subject.hashCode())){
						subject = "_:"+bNodes.get(subject.hashCode());
					}else {
						int id = BlankNodeIDGenerator.getNextID();
						bNodes.put(subject.hashCode(), id);
						subject = "_:" + id;
					}
				}
				String predicate = triple.getPredicate().toString();
				String object = triple.getObject().toString();
				if(object.startsWith("_:")){
					if(bNodes.containsKey(object.hashCode())){
						object = "_:"+bNodes.get(object.hashCode());
					}else {
						int id = BlankNodeIDGenerator.getNextID();
						bNodes.put(object.hashCode(), id);
						object = "_:" + id;
					}
				}

				indexNode(subject, TripleComponentRole.SUBJECT);
				indexNode(predicate, TripleComponentRole.PREDICATE);
				indexNode(object, TripleComponentRole.OBJECT);
				count.getAndSet(count.get() + 1);
				if(count.get() %10000000==0){
					System.out.print("\rLoaded "+count+" triples. ");
				}
			});
		} catch (ParserException e) {
			e.printStackTrace();
		}
		bNodes.clear();
		BlankNodeIDGenerator.reset();
	}

	private List<PTriple>  readFileToTriples(File rdfFile, List<String> predicates,List<String> prefixMapping, Map<String, Integer> prefix2) throws FileNotFoundException {
		Lang lang = RDFLanguages.filenameToLang(rdfFile.getName());

		RDFNotation notation = RDFNotation.guess(rdfFile);
		RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);

		List<PTriple> set = new ArrayList<PTriple>();
		//get namespaces -> prefix mapping  (trade off, time - mem)
		try {
			parser.doParse(rdfFile.getAbsolutePath(), "", notation, new RDFParserCallback.RDFCallback(){
				@Override
				public void processTriple(TripleString triple, long l) {

					String subject = triple.getSubject().toString();

					String predicate = triple.getPredicate().toString();
					String object = triple.getObject().toString();

					indexNode(subject, TripleComponentRole.SUBJECT);
					long pred = indexNode(predicate, TripleComponentRole.PREDICATE);
					indexNode(object, TripleComponentRole.OBJECT);

					subject = addNamespace(subject, prefixMapping, prefix2);
					predicate = addNamespace(predicate, prefixMapping, prefix2);
					object = addNamespace(object, prefixMapping, prefix2);

					set.add(new PTriple(subject, Long.valueOf(pred-1).intValue(), object));

					if(pred>predicates.size()){
						predicates.add(predicate);
					}
					if(set.size()%10000000==0){
						System.out.print("\rLoaded "+set.size()+" triples. ");
					}
				}

			});
		} catch (ParserException e) {
			e.printStackTrace();
		}
		System.out.println("");
		Stats.printMemStats();

		return set;

		//return set;
	}

	private String addNamespace(String node, List<String> prefixMapping, Map<String, Integer> prefix2){
		try {
			URI uri = new URI(node);
			String ns = uri.getScheme()+":"+uri.getAuthority();
			int id = prefixMapping.size()+1;
			if(prefix2.keySet().contains(ns)){
				id = prefix2.get(ns);
			}else {
				prefixMapping.add(ns);
				prefix2.put(ns, id);
				System.out.println("Added prefix: "+ns+", size: "+prefixMapping.size());
			}
			node = "$"+id+":"+node.substring(ns.length());
		} catch (Exception e) {
			return node;
		}

		/*int nsID = node.lastIndexOf('/');
		if(nsID>-1 && !node.startsWith("\"")){
			String ns =node.substring(0,nsID+1);
			int id = prefixMapping.size()+1;
			if(prefix2.keySet().contains(ns)){
				id = prefix2.get(ns);
			}else {
				prefixMapping.add(ns);
				prefix2.put(ns, id);
				System.out.println("Added prefix: "+ns+", size: "+prefixMapping.size());
			}
			node = "$"+id+":"+node.substring(nsID+1);
		}*/
		return node;
	}

	public long indexNode(String node, TripleComponentRole role){
		return dict.insert(node, role);
	}


}
