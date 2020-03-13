package org.dice_group.grp.grammar.digram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import grph.Grph;
import org.apache.jena.base.Sys;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;

import org.apache.jena.reasoner.rulesys.builtins.Bound;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.util.BoundedList;
import org.miv.graphstream.graph.DepthFirstIterator;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;

public class DigramHelper<T>{
	


	public  Map<Digram, Collection<DigramOccurence>> getMappingVertex(Grph g, BoundedList pIndex){

		//List<DigramOccurence> occ = new ArrayList<DigramOccurence>();
		Map<Digram, Collection<DigramOccurence>> map = new HashMap<Digram, Collection<DigramOccurence>>();
		long start = Calendar.getInstance().getTimeInMillis();
		int x=0;
		int vsize= g.getVertices().size();
		Iterator<Integer> it = g.getVertices().iterator();
		Map<Digram, Set<Integer>> alreadyInMap = new HashMap<Digram, Set<Integer>>();
		while (it.hasNext()) {
			Integer v = it.next();

			mapVertex(v, g, map, pIndex, alreadyInMap);
			long endON = Calendar.getInstance().getTimeInMillis();
			x++;
			if(x%10000==0)
				System.out.println(x+"th Node/"+vsize+" Nodes done [ avg "+(endON-start*1.0)/x+"ms, total: "+(endON-start)+"ms ]");

		}
		List<Digram> digrams = new ArrayList<Digram>();
		
		long end = Calendar.getInstance().getTimeInMillis();
		//System.out.println("\bFound " + occ.size() + " no of occurences");
		System.out.println("Found "+map.size()+" no of digrams");
		System.out.println("Done in " + (end - start) + " ms");
		System.out.println("sorting and removing...");
		
		return map;
	}

	/**
	 * Check if this works and is faster than the vertex based approach. It is not
	 * @param g
	 * @param pIndex
	 * @return
	 */
	public  Map<Digram, List<DigramOccurence>> getMappingEdge(Grph g, BoundedList pIndex){
		Map<Digram, List<DigramOccurence>> map = new HashMap<Digram, List<DigramOccurence>>();
		//ArrayList or HashSet? check which is faster, probably hashset
		Collection<Integer> done = new HashSet<Integer>();
		int count=0;
		for(int edge1 : g.getEdges()){
			getDigramsEdge(g, edge1, map, done, pIndex);
			//done.add(edge1);
			count++;
			if(count%10000==0){
				System.out.println(count+"/"+g.getEdges().size()+" Edges done");
			}
		}
		return map;
	}

	private void getDigramsEdge(Grph g, int edge1, Map<Digram, List<DigramOccurence>> map, Collection<Integer> done, BoundedList pIndex){
		for(int edge2 : g.getEdgesAdjacentToEdge(edge1)){
			if(done.contains(edge2)){
				// save finished edges, and remove them from the adjacents as we already done them
				continue;
			}
			done.add(edge1);
			//create Digram, at Occurence
			List<Integer> exter = new ArrayList<Integer>();
			List<Integer> inter = new ArrayList<Integer>();
			int s1 = g.getDirectedSimpleEdgeHead(edge1);
			int o1 = g.getDirectedSimpleEdgeTail(edge1);
			int s2 = g.getDirectedSimpleEdgeHead(edge2);
			int o2 = g.getDirectedSimpleEdgeTail(edge2);
			Set<Integer> externals = new HashSet<Integer>();
			if(g.getEdgeDegree(s1)>1){
				externals.add(0);
				exter.add(s1);
			}
			else{inter.add(s1);}
			if(g.getEdgeDegree(o1)>1){
				externals.add(1);
				exter.add(o1);
			}
			else{inter.add(o1);}
			if(g.getEdgeDegree(s2)>1){
				externals.add(2);
				exter.add(s2);
			}
			else{inter.add(s2);}
			if(g.getEdgeDegree(o2)>1){
				externals.add(3);
				exter.add(o2);
			}
			else{inter.add(o2);}
			Digram d = new Digram(pIndex.getBounded(edge1).getLowerBound(),
					pIndex.getBounded(edge2).getLowerBound(),
					externals);
			DigramOccurence occ =getOccurence2(g,edge1, edge2, exter.toArray(new Integer[0]),  pIndex);
			d.setStructure(occ.getStructure());
			//DigramOccurence occ = d.createOccurence(exter, inter);
			//occ.setOrigE1(edge1);
			//occ.setOrigE2(edge2);
			if(map.containsKey(d)){
				map.get(d).add(occ);
			}
			else{
				List<DigramOccurence> occs = new ArrayList<DigramOccurence>();
				occs.add(occ);
				map.put(d, occs);
			}
		}
	}

/*
	private  void mapOutVertex(Model m, Integer notE, Integer v,Integer v_1,Integer v_2,  Boolean isExt, Boolean otherExt, Grph g, Map<Digram, Collection<DigramOccurence>> map) {
		Set<Integer> edges = g.getOutEdges(v);
		Set<Integer> outExt = new HashSet<Integer>();
		Set<Integer> outInt = new HashSet<Integer>();
		for (Integer e : edges) {
			if(e.equals(notE)) {
				continue;
			}
			Integer v2 = g.getDirectedSimpleEdgeTail(e);
			if (g.getEdgeDegree(v2) > 1) {
				// external
				if(otherExt)
					continue;
				outExt.add(v2);
			} else {
				
				for (Integer d : outExt) {
					// v->d, v->v2

					Integer[] ext = new Integer[] { d };
					if(isExt)
						ext = new Integer[] { v, d };
						
					
					DigramOccurence occurence = getOccurence(g, v_1, v_2, v, d, ext);
					checkMap(occurence, map);
					//occ.add(occurence);
				}
				// internal
				outInt.add(v2);
			}
			if(!isExt) {
				for (Integer d : outInt) {
					// v->d, v->v2
					DigramOccurence occurence = getOccurence(g, v_1, v_2, v, d, new Integer[] { v });
					checkMap(occurence, map);
					//occ.add(occurence);
				}
			}
		}
	}
	
	private  void mapInVertex(Model m, Integer notE, Integer v,Integer v_1,Integer v_2, Boolean isExt, Boolean otherExt, Grph g, Map<Digram, Collection<DigramOccurence>> map) {
		Set<Integer> edges = g.getOutEdges(v);
		Set<Integer> inExt = new HashSet<Integer>();
		Set<Integer> inInt = new HashSet<Integer>();
		for (Integer e : edges) {
			if(e.equals(notE)) {
				continue;
			}
			Integer v2 = g.getDirectedSimpleEdgeHead(e);
			if (g.getEdgeDegree(v2) > 1) {
				// external
				if(otherExt)
					continue;
				inExt.add(v2);
				
			} else {
				// internal
				inInt.add(v2);
				// DigramOccurence occurence = getOccurence(g, d, v, v2, v, new String[] {d,v});
				// occ.add(occurence);
				for (Integer d : inExt) {
					// d->v v2->v
					Integer[] ext = new Integer[] { d };
					if(isExt)
						ext = new Integer[] { d, v };
					DigramOccurence occurence = getOccurence(g, d, v, v_1, v_2, ext);
					checkMap(occurence, map);
					//occ.add(occurence);
				}
			}
			if(!isExt) {
				for (Integer d : inInt) {
					// d->v v2->v
					DigramOccurence occurence = getOccurence(g, d, v, v_1, v_2, new Integer[] { v });
					checkMap(occurence, map);
					//occ.add(occurence);
				}
			}
		}
	}

	/**
	 * check if edge solution is still correct
	 * @param v
	 * @param g
	 * @param map
	 */
	private  void mapVertex(Integer v, Grph g, Map<Digram, Collection<DigramOccurence>> map, BoundedList pIndex, Map<Digram, Set<Integer>> alreadyInMap) {
		//mapVertex use edges instead of vertexes
		Set<Integer> edges = g.getInEdges(v);;
		List<Integer> inExt = new ArrayList<Integer>();
		List<Integer> outExt = new ArrayList<Integer>();
		List<Integer> inInt = new ArrayList<Integer>();
		List<Integer> outInt = new ArrayList<Integer>();

		for(Integer e : edges){
			Integer v2 = g.getDirectedSimpleEdgeTail(e);;

			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {
				// external
				inExt.add(e);
			} else {
				// internal
				inInt.add(e);
			}
		}
		edges = g.getOutEdges(v);
		for(Integer e : edges){
			Integer v2 = g.getDirectedSimpleEdgeHead(e);;

			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 2 ) || (v.equals(v2) && g.getEdgeDegree(v2)>3)) {
				// external
				outExt.add(e);
			} else {
				// internal
				outInt.add(e);
			}
		}


		if (g.getEdgeDegree(v)>2){
			//ext +int && int + int
			for(Integer e1 : inExt){
				int ex = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex ,  v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex, v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e2, e1, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : inInt){
				for(Integer e2 : inInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outInt){
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
		}
		else{
			//int + ext and ext + ext
			for(Integer e1 : inExt){
				int ex = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex ,  v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex, v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e2, e1, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2)){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : inExt){
				int ex1 = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inExt){
					if(e1.equals(e2)){continue;}
					int ex2 = g.getDirectedSimpleEdgeTail(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outExt){
					if(e1.equals(e2)){continue;}
					int ex2 = g.getDirectedSimpleEdgeHead(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex1 = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : outExt){
					if(e1.equals(e2)){continue;}
					int ex2 = g.getDirectedSimpleEdgeHead(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
		}

		/*
		for (Integer e : edges) {
			Integer v2 = g.getDirectedSimpleEdgeTail(e);;
			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {
				// external
				inExt.add(e);
			} else {
				// internal
				inInt.add(e);
				// DigramOccurence occurence = getOccurence(g, d, v, v2, v, new String[] {d,v});
				// occ.add(occurence);
				for (Integer d : inExt) {
					if(e==d){continue;}
					// d->v v2->v
					//f.e. getOccurence(g, e, {d,v})
					//DigramOccurence occurence = getOccurence(g, d, v, v2, v, new Integer[] { d, v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { g.getDirectedSimpleEdgeTail(d), v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
			}
			for (Integer d : inInt) {
				if(e==d){continue;}
				// d->v v2->v
				//DigramOccurence occurence = getOccurence(g, d, v, v2, v, new Integer[] { v });
				DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);

				checkMap(occurence, map, alreadyInMap);
				//occ.add(occurence);
			}
		}
		edges = g.getOutEdges(v);
		for (Integer e : edges) {
			Integer v2 = g.getDirectedSimpleEdgeHead(e);
			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {

				// external
				for (Integer d : inInt) {
					if(e==d){continue;}
					// d->v v->v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { v, v2 });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] {v, v2 }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				outExt.add(e);
			} else {
				for (Integer d : inInt) {
					if(e==d){continue;}
					// d -> v, v-> v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				for (Integer d : inExt) {
					if(e==d){continue;}
					// d -> v, v-> v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { d, v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { g.getDirectedSimpleEdgeTail(d), v }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				for (Integer d : outExt) {
					if(e==d){continue;}
					// v->d, v->v2
					//DigramOccurence occurence = getOccurence(g, v, d, v, v2, new Integer[] { v, d });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v, g.getDirectedSimpleEdgeTail(d) }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				// internal
				outInt.add(e);
			}
			for (Integer d : outInt) {
				if(e==d){continue;}
				// v->d, v->v2
				//DigramOccurence occurence = getOccurence(g, v, d, v, v2, new Integer[] { v });
				DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);

				checkMap(occurence, map, alreadyInMap);
				//occ.add(occurence);
			}
		}
		*/
	}

	private DigramOccurence getOccurence2(Grph g, Integer d, Integer e, Integer[] rdfNodes, BoundedList pIndex) {
		List<Integer> externals = new ArrayList<Integer>();
		if(d.equals(e)){
			System.out.println("Haeeeeh???");
			System.out.println("Haeeeeh???");
		}
		//instead of model list statments use grph
		Statement stmt1 = new Statement(g.getDirectedSimpleEdgeTail(d), pIndex.getBounded(d).getLowerBound(), g.getDirectedSimpleEdgeHead(d));
		Statement stmt2 = new Statement(g.getDirectedSimpleEdgeTail(e), pIndex.getBounded(e).getLowerBound(), g.getDirectedSimpleEdgeHead(e));

		//Statement stmt1 = m.listStatements(v0, ResourceFactory.createProperty(g.getEdge(v0, v1).getLabel()), v1).next();
		//Statement stmt2 = m.listStatements(v2, ResourceFactory.createProperty(g.getEdge(v2, v3).getLabel()), v3).next();
		for(Integer v : rdfNodes) {
			externals.add(v);
		}
		DigramOccurence occ = new DigramOccurence(stmt1, stmt2, externals);
		occ.setOrigE1(d);
		occ.setOrigE2(e);

		return occ;
	}

	public static Set<Integer> getExternalIndexes(Statement e1, Statement e2, List<Integer> externals) {
		Set<Integer> externalIndex = new HashSet<Integer>();
		for(Integer node : externals) {
			if(e1.getSubject().equals(node)) {
				externalIndex.add(0);
			}
			if(e1.getObject().equals(node)) {
				externalIndex.add(1);
			}
			if(e2.getSubject().equals(node)) {
				externalIndex.add(2);
			}
			if(e2.getObject().equals(node)) {
				externalIndex.add(3);
			}
		}
		return externalIndex;
	}

//	private  RDFNode parseNode(Object o2) {
//		RDFNode v2;
//		if(o2 instanceof String) {
//			String s2 = o2.toString();
//
//			if(s2.contains("@")) {
//				v2 = ResourceFactory.createLangLiteral(s2.substring(s2.lastIndexOf("@")), s2.substring(s2.lastIndexOf("@")+1));
//			}
//			else if (s2.contains("^^")){
//				LiteralLabel ll = LiteralLabelFactory.create(s2);
//				v2 = ResourceFactory.createTypedLiteral(ll.getValue().toString(), ll.getDatatype());
//			}
//			else {
//				v2 = ResourceFactory.createPlainLiteral(s2);
//			}
//		}
//		else {
//			v2 = (RDFNode) o2;
//		}
//		return v2;
//	}
/*
	private  DigramOccurence getOccurence(Grph g, Integer v0, Integer v1, Integer v2, Integer v3,
			Integer[] rdfNodes) {
		List<Integer> externals = new ArrayList<Integer>();
		//instead of model list statments use grph
		Statement stmt1 = new Statement(v0, g.getSomeEdgeConnecting(v0, v1),v1);
		Statement stmt2 = new Statement(v2, g.getSomeEdgeConnecting(v2, v3),v3);

		//Statement stmt1 = m.listStatements(v0, ResourceFactory.createProperty(g.getEdge(v0, v1).getLabel()), v1).next();
		//Statement stmt2 = m.listStatements(v2, ResourceFactory.createProperty(g.getEdge(v2, v3).getLabel()), v3).next();
		for(Integer e : rdfNodes) {
			externals.add(e);
		}
		return new DigramOccurence(stmt1, stmt2, externals);

	}
*/
	private  void checkMap(DigramOccurence occur, Map<Digram, Collection<DigramOccurence>> map, Map<Digram, Set<Integer>> alreadyInMap) {
		Digram d = new Digram(occur.getEdgeLabel1(), occur.getEdgeLabel2(), occur.getExternalIndexes());
		d.setStructure(occur.generateStructure());
		alreadyInMap.putIfAbsent(d, new HashSet<Integer>());
		if(alreadyInMap.get(d).contains(occur.getOrigE1()) || alreadyInMap.get(d).contains(occur.getOrigE2())  ){
			return;
		}
		map.putIfAbsent(d, new ArrayList<DigramOccurence>());
		map.get(d).add(occur);
		alreadyInMap.get(d).add(occur.getOrigE1());
		alreadyInMap.get(d).add(occur.getOrigE2());
	}
	
//	public static Map<Digram, Set<DigramOccurence>> findMapping(Model graph, String replaceEdge){
//		
//	}
	
	
	public  List<Digram> sortDigrambyFrequence(Map<Digram, Collection<DigramOccurence>> map){

		List<Digram> sortedDigrams = new ArrayList<Digram>();
		List<Digram> removeOnes = new ArrayList<Digram>();
		for(Digram d : map.keySet()) {
			d.setNoOfOccurences(map.get(d).size());
			if(d.getNoOfOccurences()==1) {
				removeOnes.add(d);
			}else {
				sortedDigrams.add(d);
			}
		}
		for(Digram remove : removeOnes) {
			map.remove(remove);
		}

		
		sortedDigrams.sort(new Comparator<Digram>() {
			@Override
			public int compare(Digram dig0, Digram dig1) {
				if(dig0.getNoOfOccurences() > dig1.getNoOfOccurences()) {
					return -1;
				}
				if(dig0.getNoOfOccurences() < dig1.getNoOfOccurences()) {
					return 1;
				}
				return 0;
			}
		});
		return sortedDigrams;
	}

	// also duplicates from the occurences. This is good, but we also need to purge the occurence list beforehand
	public  void removeOverlappingOcc(List<Digram> freq, Map<Digram, Collection<DigramOccurence>> map) {
		//TODO this is sadly TSP, we just use a greedy algorithm here. should be the best option though.
		Set<Integer> occuredEdges = new HashSet<Integer>();
		Set<Digram> remove = new HashSet<Digram>();
		for(Digram d : freq) {
			if(occuredEdges.contains(d.getEdgeLabel1()) || occuredEdges.contains(d.getEdgeLabel2())) {
				map.remove(d);
				remove.add(d);
			}
			else {
				occuredEdges.add(d.getEdgeLabel1());
				occuredEdges.add(d.getEdgeLabel2());
			}
		}
		freq.removeAll(remove);
	}


	/*
	public  DefaultDirectedGraph<RDFNode, LabledEdge> NTFile2Graph(File f) throws IOException {
		DefaultDirectedGraph<RDFNode, LabledEdge> g = new DefaultDirectedGraph<>(LabledEdge.class);
		BufferedReader reader = new BufferedReader(new FileReader(f));
		long index = 0;
		String line;
		Model m = ModelFactory.createDefaultModel();
		while ((line=reader.readLine())!=null) {
			if(line.isEmpty()) {
				continue;
			}
			m.read(new StringReader(line), null, "NTRIPLE");
			
			Statement stmt = m.listStatements().next();
			LabledEdge e = new LabledEdge(stmt.getPredicate().toString());
			g.addVertex(stmt.getSubject());
			g.addVertex(stmt.getObject());
			g.addEdge(stmt.getSubject(), stmt.getObject(), e);
			m.remove(stmt);
		}
		return g;
	}

	public DirectedPseudograph<String, LabledEdge> model2Graph(Model m) {
		DirectedPseudograph<String, LabledEdge> g = new DirectedPseudograph<String, LabledEdge>(LabledEdge.class);
		//StmtIterator stmtIt = m.listStatements();
		Set<Statement> stmts = m.listStatements().toSet();
		m = m.removeAll();
		
		long index = 0;
		for (Statement stmt : stmts) {
//			Statement stmt = stmtIt.next();
			m.add(stmt);
			LabledEdge e = new LabledEdge(stmt.getPredicate().toString());
			boolean check1 = g.addVertex(stmt.getSubject().toString());
			if(stmt.getObject().isLiteral()) {
				g.addVertex(JenaNodeFormatter.format(stmt.getObject()));
				g.addEdge(stmt.getSubject().toString(),JenaNodeFormatter.format(stmt.getObject()), e);
			}else {
				g.addVertex(stmt.getObject().toString());
				boolean check3 = g.addEdge(stmt.getSubject().toString(), stmt.getObject().toString(), e);
				assertTrue(check3);
			}
		}
		assertEquals(m.size(), g.edgeSet().size());

		return g;
	}
*/

	public  Map<Digram, List<DigramOccurence>> findNewMappingsEdge(Grph g, Set<Integer> le, BoundedList pIndex) {
		Map<Digram, List<DigramOccurence>> map = new HashMap<Digram, List<DigramOccurence>>();
		List<Integer> done =new ArrayList<Integer>();
		for(Integer edge : le){
			getDigramsEdge(g, edge, map, done, pIndex);
		}
		return map;
	}




	public  Map<Digram, Collection<DigramOccurence>> findNewMappingsVertex(Grph g, Set<Integer> le, BoundedList pIndex) {
		Map<Digram, Collection<DigramOccurence>> map = new HashMap<Digram, Collection<DigramOccurence>>();
		Map<Digram, Set<Integer>> alreadyInMap = new HashMap<Digram, Set<Integer>>();
		Map<Integer, Set<Integer>> visited = new HashMap<Integer, Set<Integer>>();
		//Iterator<T> iterator = new DepthFirstIterator<>(g, g.vertexSet().iterator().next());
		long start = Calendar.getInstance().getTimeInMillis();
//		Set<RDFNode> occured = new HashSet<RDFNode>();
		for(Integer e : le) {


			Integer v1 =g.getDirectedSimpleEdgeTail(e);
			Integer v2 =g.getDirectedSimpleEdgeHead(e);
			visited.putIfAbsent(v1, new HashSet<Integer>());
			visited.get(v1).add(e);
			visited.putIfAbsent(v2, new HashSet<Integer>());
			visited.get(v2).add(e);
			/*
			if(!visited.contains(v1)) {
				mapVertex(v1, g, map, pIndex, alreadyInMap);
				visited.add(v1);
			}
			if(!visited.contains(v2)) {
				mapVertex(v2, g, map, pIndex, alreadyInMap);
				visited.add(v2);
			}
			*/
		}
		int x=0;
		for (Integer v : visited.keySet()) {

			mapVertexEdge(v, visited.get(v), g, map, pIndex, alreadyInMap);
			x++;

			long endON = Calendar.getInstance().getTimeInMillis();

			if(x%10000==0)
				System.out.println(x+"th Node/"+visited.size()+" Nodes done [ avg "+(endON-start*1.0)/x+"ms, total: "+(endON-start)+"ms ]");

		}
		long end = Calendar.getInstance().getTimeInMillis();
		//System.out.println("\bFound " + occ.size() + " no of occurences");
		System.out.println("Found "+map.size()+" no of digrams");
		System.out.println("Done in " + (end - start) + " ms");
		System.out.println("sorting and removing...");
		return map;
	}


	private  void mapVertexEdge(Integer v, Set<Integer> mustEdge, Grph g, Map<Digram, Collection<DigramOccurence>> map, BoundedList pIndex, Map<Digram, Set<Integer>> alreadyInMap) {
		//mapVertex use edges instead of vertexes
		Set<Integer> edges = g.getInEdges(v);;
		List<Integer> inExt = new ArrayList<Integer>();
		List<Integer> outExt = new ArrayList<Integer>();
		List<Integer> inInt = new ArrayList<Integer>();
		List<Integer> outInt = new ArrayList<Integer>();

		for(Integer e : edges){
			Integer v2 = g.getDirectedSimpleEdgeTail(e);;

			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {
				// external
				inExt.add(e);
			} else {
				// internal
				inInt.add(e);
			}
		}
		edges = g.getOutEdges(v);
		for(Integer e : edges){
			Integer v2 = g.getDirectedSimpleEdgeHead(e);;

			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 2 ) || (v.equals(v2) && g.getEdgeDegree(v2)>3)) {
				// external
				outExt.add(e);
			} else {
				// internal
				outInt.add(e);
			}
		}


		if (g.getEdgeDegree(v)>2){
			//ext +int && int + int
			for(Integer e1 : inExt){
				int ex = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex ,  v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex, v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e2, e1, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : inInt){
				for(Integer e2 : inInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2) ||(!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outInt){
				for(Integer e2 : outInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
		}
		else{
			//int + ext and ext + ext
			for(Integer e1 : inExt){
				int ex = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex ,  v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex, v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : inInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e2, e1, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outInt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { v, ex }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : inExt){
				int ex1 = g.getDirectedSimpleEdgeTail(e1);
				for(Integer e2 : inExt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					int ex2 = g.getDirectedSimpleEdgeTail(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
				for(Integer e2 : outExt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					int ex2 = g.getDirectedSimpleEdgeHead(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
			for(Integer e1 : outExt){
				int ex1 = g.getDirectedSimpleEdgeHead(e1);
				for(Integer e2 : outExt){
					if(e1.equals(e2) || (!mustEdge.contains(e1) && !mustEdge.contains(e2) )){continue;}
					int ex2 = g.getDirectedSimpleEdgeHead(e2);
					DigramOccurence occurence = getOccurence2(g, e1, e2, new Integer[] { ex1, ex2 }, pIndex);
					checkMap(occurence, map, alreadyInMap);
				}
			}
		}

		/*
		for (Integer e : edges) {
			Integer v2 = g.getDirectedSimpleEdgeTail(e);;
			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {
				// external
				inExt.add(e);
			} else {
				// internal
				inInt.add(e);
				// DigramOccurence occurence = getOccurence(g, d, v, v2, v, new String[] {d,v});
				// occ.add(occurence);
				for (Integer d : inExt) {
					if(e==d){continue;}
					// d->v v2->v
					//f.e. getOccurence(g, e, {d,v})
					//DigramOccurence occurence = getOccurence(g, d, v, v2, v, new Integer[] { d, v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { g.getDirectedSimpleEdgeTail(d), v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
			}
			for (Integer d : inInt) {
				if(e==d){continue;}
				// d->v v2->v
				//DigramOccurence occurence = getOccurence(g, d, v, v2, v, new Integer[] { v });
				DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);

				checkMap(occurence, map, alreadyInMap);
				//occ.add(occurence);
			}
		}
		edges = g.getOutEdges(v);
		for (Integer e : edges) {
			Integer v2 = g.getDirectedSimpleEdgeHead(e);
			if ((!v.equals(v2) && g.getEdgeDegree(v2) > 1 ) || (v.equals(v2) && g.getEdgeDegree(v2)>2)) {

				// external
				for (Integer d : inInt) {
					if(e==d){continue;}
					// d->v v->v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { v, v2 });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] {v, v2 }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				outExt.add(e);
			} else {
				for (Integer d : inInt) {
					if(e==d){continue;}
					// d -> v, v-> v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);
					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				for (Integer d : inExt) {
					if(e==d){continue;}
					// d -> v, v-> v2
					//DigramOccurence occurence = getOccurence(g, d, v, v, v2, new Integer[] { d, v });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { g.getDirectedSimpleEdgeTail(d), v }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				for (Integer d : outExt) {
					if(e==d){continue;}
					// v->d, v->v2
					//DigramOccurence occurence = getOccurence(g, v, d, v, v2, new Integer[] { v, d });
					DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v, g.getDirectedSimpleEdgeTail(d) }, pIndex);

					checkMap(occurence, map, alreadyInMap);
					//occ.add(occurence);
				}
				// internal
				outInt.add(e);
			}
			for (Integer d : outInt) {
				if(e==d){continue;}
				// v->d, v->v2
				//DigramOccurence occurence = getOccurence(g, v, d, v, v2, new Integer[] { v });
				DigramOccurence occurence = getOccurence2(g, d, e, new Integer[] { v }, pIndex);

				checkMap(occurence, map, alreadyInMap);
				//occ.add(occurence);
			}
		}
		*/
	}


}



