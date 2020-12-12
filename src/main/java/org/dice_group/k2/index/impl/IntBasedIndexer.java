package org.dice_group.k2.index.impl;

import org.dice_group.k2.serialization.impl.ThreadedKD2TreeSerializer;
import org.dice_group.k2.util.BlankNodeIDGenerator;
import org.dice_group.k2.util.PTriple;
import org.dice_group.k2.util.Stats;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCTempDictionary;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IntBasedIndexer {

    private TempDictionary tmpDict;
    private NodeDictionary nodeDict;
    private DictionaryPrivate dict;

    public IntBasedIndexer(TempDictionary tmpDict) {
        this.tmpDict = tmpDict;

    }

    public DictionaryPrivate getDict() {
        return dict;
    }


    public void load(){
        Stats.printMemStats();
        HDTSpecification spec = new HDTSpecification();
        //FIXME if no shared big dict fails
        spec.set("dictionary.type", "dictionaryFourBig");
        dict = DictionaryFactory.createDictionary(spec);
        ProgressListener listener = new ProgressOut();

        //NOT A SOLUTION.
        if(tmpDict.getShared().size()==0){
            tmpDict.getShared().add("a");
            spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);
        }
        tmpDict.reorganize();
        dict.load(new PSFCTempDictionary(tmpDict), listener);
        Stats.printMemStats();
    }

    public int[] indexTriples(File rdfFile, ThreadedKD2TreeSerializer serializer) {

        load();
        RDFNotation notation = RDFNotation.guess(rdfFile);
        RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);
        AtomicReference<Integer> count= new AtomicReference<>(0);
        AtomicInteger bnodeCount= new AtomicInteger(0);

        Map<String, Integer> bNodes = new HashMap<String, Integer>();

        //TODO test and remove
        //initSpace(rdfFile, serializer);
        //Stats.printMemStats();
        int[] sizeList = new int[Long.valueOf(tmpDict.getPredicates().getNumberOfElements()).intValue()];

        long start = Calendar.getInstance().getTimeInMillis();
        //get namespaces -> prefix mapping  (trade off, time - mem)
        try {
            parser.doParse(rdfFile.getAbsolutePath(), "", notation, (triple, l) -> {

                String subject = triple.getSubject().toString();
                String predicate = triple.getPredicate().toString();
                String object = triple.getObject().toString();

                if(subject.startsWith("_:")){
                    if(bNodes.containsKey(subject)){
                        subject = "_:"+bNodes.get(subject);
                    }else {
                        int id = BlankNodeIDGenerator.getNextID();
                        bNodes.put(subject, id);
                        subject = "_:" + id;
                    }
                }
                if(object.startsWith("_:")){
                    if(bNodes.containsKey(object)){
                        object = "_:"+bNodes.get(object);
                    }else {
                        int id = BlankNodeIDGenerator.getNextID();
                        bNodes.put(object, id);
                        object = "_:" + id;
                    }
                }

                int subjectID = Long.valueOf(tmpDict.stringToId(subject, TripleComponentRole.SUBJECT)).intValue()-1;
                int predicateID = Long.valueOf(tmpDict.stringToId(predicate, TripleComponentRole.PREDICATE)).intValue()-1;
                int objectID = Long.valueOf(tmpDict.stringToId(object, TripleComponentRole.OBJECT)).intValue()-1;

                serializer.addTriple(subjectID, predicateID, objectID);
                sizeList[predicateID] = sizeList[predicateID]+1;

                count.getAndSet(count.get() + 1);
                if(count.get() %100000==0){
                    long end = Calendar.getInstance().getTimeInMillis();
                    System.out.print("\rIndexed "+count+" triples. ["+((end-start)*1.0/count.get())+"ms/triple avg] ");
                }
            });
        } catch (ParserException e) {
            e.printStackTrace();
        }
        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Indexed "+count+" triples ["+((end-start)*1.0/count.get())+"ms/triple avg]");
        bNodes.clear();
        return sizeList;
    }

    //ensuring that the arrays in indexing don't have to be grown each time, which takes most of the time.
    private void initSpace(File rdfFile, ThreadedKD2TreeSerializer serializer) {
        RDFNotation notation = RDFNotation.guess(rdfFile);
        RDFParserCallback parser = RDFParserFactory.getParserCallback(notation);
        AtomicReference<Integer> count = new AtomicReference<Integer>();
        int[] sizeList = new int[Long.valueOf(tmpDict.getPredicates().getNumberOfElements()).intValue()];
        try {
            parser.doParse(rdfFile.getAbsolutePath(), "", notation, (triple, l) -> {
                String predicate = triple.getPredicate().toString();
                int predicateID = Long.valueOf(tmpDict.stringToId(predicate, TripleComponentRole.PREDICATE)).intValue()-1;

                sizeList[predicateID] = sizeList[predicateID]+1;
                count.getAndSet(count.get() + 1);
                if(count.get() %100000==0){
                    System.out.print("\rInit space for "+count.get()+" triple");
                }
            });
        } catch (ParserException e) {
            e.printStackTrace();
        }
        serializer.initSpace(sizeList);
    }

    public void indexTriples(Collection<PTriple> triples, List<String> predicates, ThreadedKD2TreeSerializer serializer, List<String> prefixMapping) {
        HDTSpecification spec = new HDTSpecification();
        spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);
        dict = DictionaryFactory.createDictionary(spec);
        ProgressListener listener = new ProgressOut();
        tmpDict.reorganize();
        dict.load(new PSFCTempDictionary(tmpDict), listener);

        for(PTriple triple : triples){
            //string prefix replacement
            String subject=replacePrefix(triple.getSubject(), prefixMapping);
            String predicate=replacePrefix(predicates.get(triple.getPredicate()), prefixMapping);
            String object=replacePrefix(triple.getObject(), prefixMapping);

            int subjectID = Long.valueOf(dict.stringToId(subject, TripleComponentRole.SUBJECT)).intValue()-1;
            int predicateID = Long.valueOf(dict.stringToId(predicate, TripleComponentRole.PREDICATE)).intValue()-1;
            int objectID = Long.valueOf(dict.stringToId(object, TripleComponentRole.OBJECT)).intValue()-1;


            //int subjectID = Long.valueOf(tmpDict.getSubjects().locate(encode(subject))).intValue()-1;
            //int predicateID = Long.valueOf(tmpDict.getPredicates().locate(encode(predicate))).intValue()-1;
            //int objectID = Long.valueOf(tmpDict.getObjects().locate(encode(object))).intValue()-1;

            serializer.addTriple(subjectID, predicateID, objectID);
            triple.clear();
            predicates.remove(triple.getPredicate());
        }
        predicates.clear();
        triples.clear();

    }

    private String replacePrefix(String node, List<String> prefixMapping) {
       if(node.startsWith("$")){
            int prefixID = Integer.valueOf(node.substring(1,node.indexOf(":")));
            String prefix = prefixMapping.get(prefixID-1);
            return node.replace("$"+prefixID+":", prefix);
        }
        return node;
    }


}
