package org.dice_group.grp.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.*;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdtjena.NodeDictionary;

import java.util.*;

public class GraphUtils {

	private static String[] replaceStrings = new String[] {":s", ":n", ":p", ":o"};



	public static Node parseLiteral(String l){
		String literalStr = l.replace("\\\"", "\"").replace("\\-", "-").replace("\\_", "_");
		if(literalStr.startsWith("\"") && literalStr.endsWith("\"")){
			literalStr = literalStr.substring(1, literalStr.length()-1);
		}
		if(literalStr.matches(".*@[a-zA-Z0-9\\s]+]")){
			String val = literalStr.substring(0, literalStr.lastIndexOf("@"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String lang = literalStr.substring(literalStr.indexOf("@")+1);
			return NodeFactory.createLiteral(val, lang);
		}
		if(literalStr.matches(".*\\^\\^<.*>")){
			String val = literalStr.substring(0,literalStr.lastIndexOf("^^"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String uri = literalStr.substring(literalStr.lastIndexOf("^^")+3, literalStr.length()-1);
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(uri);
			return NodeFactory.createLiteral(val, dtype);

		}


		/*
		Node literal = JenaNodeCreator.createLiteral(literalStr);
		if(literal.getLiteralLanguage()!=null && !literal.getLiteralLanguage().isEmpty()){
			return ResourceFactory.createLangLiteral(literal.getLiteralValue().toString(), literal.getLiteralLanguage());
		}
		else if(literal.getLiteralDatatype()!=null){

			return ResourceFactory.createTypedLiteral(literal.getLiteralValue());
		}

		 */
		return NodeFactory.createLiteral(literalStr);
	}

	public static RDFNode parseHDTLiteral(Node l){
		String literalStr = l.toString().replace("\\\"", "\"").replace("\\-", "-").replace("\\_", "_");
		if(literalStr.startsWith("\"") && literalStr.endsWith("\"")){
			literalStr = literalStr.substring(1, literalStr.length()-1);
		}
		if(literalStr.matches(".*@[a-zA-Z0-9\\s]+]")){
			String val = literalStr.substring(0, literalStr.lastIndexOf("@"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String lang = literalStr.substring(literalStr.indexOf("@")+1);
			return ResourceFactory.createLangLiteral(val, lang);
		}
		if(literalStr.matches(".*\\^\\^<.*>")){
			String val = literalStr.substring(0,literalStr.lastIndexOf("^^"));
			if(val.startsWith("\"") && val.endsWith("\"")){
				val = val.substring(1, val.length()-1);
			}
			String uri = literalStr.substring(literalStr.lastIndexOf("^^")+3, literalStr.length()-1);
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(uri);
			return ResourceFactory.createTypedLiteral(val, dtype);

		}


		/*
		Node literal = JenaNodeCreator.createLiteral(literalStr);
		if(literal.getLiteralLanguage()!=null && !literal.getLiteralLanguage().isEmpty()){
			return ResourceFactory.createLangLiteral(literal.getLiteralValue().toString(), literal.getLiteralLanguage());
		}
		else if(literal.getLiteralDatatype()!=null){

			return ResourceFactory.createTypedLiteral(literal.getLiteralValue());
		}

		 */
		return ResourceFactory.createPlainLiteral(literalStr);
	}
	

	public static Node getObject(int o, NodeDictionary dict){
		try {
			return dict.getNode(o, TripleComponentRole.OBJECT);
		}catch(Exception e){
			return dict.getNode(o, TripleComponentRole.OBJECT);
		}
	}
	
	public static Integer getRDFIndex(RDFNode node) {
		try {
			String nodeStr = node.toString();
			for(String replace : replaceStrings ) {
				nodeStr = nodeStr.replace(replace, "");
			}
			return Integer.valueOf(nodeStr);
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
}
