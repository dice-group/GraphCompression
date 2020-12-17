package org.dice_group.grp.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

public class KendallsCorrelationCalculation {

    private static final Set<String> COMPRESSORS = new HashSet<>(Arrays.asList("HDT", "HDT++", "k²", "OFR", "RDFRePair", "OrigGRP"));
    //private static final String INPUT_FILE = "/home/micha/Downloads/stats_allother.tsv";
    //private static final String INPUT_FILE = "/home/micha/Downloads/stats_k2.tsv";
    private static final String INPUT_FILE = "/home/micha/Downloads/stats_rdf_grp.tsv";
    //private static final String INPUT_FILE = "/home/micha/Downloads/stats_grp.tsv";

    public static void main(String[] args) throws IOException {
        Map<String, double[]> arrays = new HashMap<>();

        String content = FileUtils.readFileToString(new File(INPUT_FILE), StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        String[] cells;
        double[] values;
        for (int i = 1; i < lines.length; ++i) {
            cells = lines[i].split("\t");
            if (cells.length > 0) {
                values = new double[cells.length - 1];
                for (int j = 0; j < values.length; ++j) {
                    values[j] = Double.parseDouble(cells[j + 1]);
                }
                arrays.put(cells[0], values);
            }
        }
        BitSet foundDatasets = new BitSet();
        double[] values2;
        KendallsCorrelation correl = new KendallsCorrelation();
        for (String comp : arrays.keySet()) {
            if (COMPRESSORS.contains(comp)) {
                values = arrays.get(comp);
                foundDatasets.clear();
                // check for empty cells
                for (int i = 0; i < values.length; ++i) {
                    if(values[i] == 0) {
                        foundDatasets.set(i);
                    }
                }
                if(foundDatasets.cardinality() > 0) {
                    values = reduce(values, foundDatasets);
                }
                System.out.printf("%20s %d values%n", comp, values.length);
                for (String prop : arrays.keySet()) {
                    if (!COMPRESSORS.contains(prop)) {
                        values2 = arrays.get(prop);
                        if(foundDatasets.cardinality() > 0) {
                            values2 = reduce(values2, foundDatasets);
                        }
                        System.out.printf("%20s %20s %f%n", comp, prop, correl.correlation(values, values2));
                    }
                }
            }
        }
    }

    private static double[] reduce(double[] values, BitSet foundDatasets) {
        int pos = 0;
        double[] reduced = new double[values.length - foundDatasets.cardinality()];
        for (int i = 0; i < values.length; ++i) {
            if(!foundDatasets.get(i)) {
                reduced[pos] = values[i];
                ++pos;
            }
        }
        return reduced;
    }
}
