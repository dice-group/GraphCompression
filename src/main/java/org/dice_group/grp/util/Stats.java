package org.dice_group.grp.util;

import grph.Grph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class Stats {
    private static final String FILE = "stats.log";
    private static String fileName="";

    public static double createSPS(Grph g, double x) {
        int sumAll = 0;
        int sumTop=0;
        List<Integer> orderV = new ArrayList<Integer>();
        for(Integer v : g.getVertices()){
            orderV.add(v);
            sumAll += g.getEdgeDegree(v);
        }
        Collections.sort(orderV, new Comparator<Integer>() {
            @Override
            public int compare(Integer v1, Integer v2) {
                Integer deg1= g.getEdgeDegree(v1);
                Integer deg2= g.getEdgeDegree(v2);
                return deg2.compareTo(deg1);
            }
        });
        for(int i=0;i<x;i++){
            sumTop += g.getEdgeDegree(orderV.get(i));
        }
        return sumTop*1.0/sumAll;
    }

    public static double createElr(BoundedList pIndex) {
        return pIndex.size()*1.0/ pIndex.getHighestBound();
    }

    public static void printStats(Grph g, BoundedList pIndex) {
        double elr = Stats.createElr(pIndex);
        double sps = Stats.createSPS(g, 0.001*g.getVertices().size());
        String stats = "Dataset{ ELR : "+elr+", SPS: "+sps+", #Resources: "+g.getVertices().size()+" , #Triples: "+pIndex.getHighestBound()+",  R/T: "+g.getVertices().size()*1.0/pIndex.getHighestBound()+"}";
        System.out.println(stats);
        try(FileWriter fw = new FileWriter(FILE,true); BufferedWriter bw = new BufferedWriter(fw)){
            bw.write(fileName+" : "+stats);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void printMemStats(){
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        Instant now = Instant.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );
        String output = formatter.format( now );

        StringBuilder sb = new StringBuilder(output).append(": ");
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("actual used memory: " + format.format((allocatedMemory-freeMemory) / 1024) + ", ");
        sb.append("free memory: " + format.format(freeMemory / 1024) + ", ");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + ", ");
        sb.append("max memory: " + format.format(maxMemory / 1024) + ", ");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        System.out.println(sb.toString());
    }

    public static void setCurrentFileName(String name) {
        fileName=name;
    }
}
