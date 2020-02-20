package org.dice_group.grp.compression.impl;

import grph.Grph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.dice_group.grp.compression.AbstractGrammarCompressor;
import org.dice_group.grp.compression.GrammarCompressor;
import org.dice_group.grp.exceptions.NotSupportedException;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.Statement;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.serialization.impl.DigramSerializerImpl;
import org.dice_group.grp.serialization.impl.KD2TreeSerializer;
import org.dice_group.grp.util.BoundedList;
import org.dice_group.grp.util.IndexedRDFNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class KD2TreeCompressor extends AbstractGrammarCompressor {




    @Override
    public byte[] compress(List<Statement> stmts, Grph g, BoundedList pIndex) throws NotSupportedException, IOException {
        KD2TreeSerializer serializer = new KD2TreeSerializer();
        return serializer.serialize(stmts, g, pIndex);
    }


}
