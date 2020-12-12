package org.dice_group.k2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LabledMatrix {



    private Integer labelId=0;
    //TODO safe time by precalculating size?
    private Collection<Point> matrix = new ArrayList<Point>();

    public int max=-1;

    public LabledMatrix(){
    }

    public LabledMatrix(Integer labelId){
        this.labelId = labelId;
    }

    public LabledMatrix(Integer labelId, int listSize){
        this.labelId = labelId;
        matrix = new ArrayList<Point>(listSize);
    }

    public void setMatrixList(List<Point> list){
        this.matrix=list;
    }


    public Integer getLabelId() {
        return labelId;
    }
    public void set(Integer row, Integer col){
        matrix.add(new Point(row, col));
        if(row>max){
            max=row;
        }
        if(col>max){
            max=col;
        }
    }

    public int get(Integer row, Integer col){
        if(matrix.contains(new Point(row, col))){
            return 1;
        }
        return 0;
    }


    public Collection<Point> getPoints(){
        return matrix;
    }

    public Integer getSize() {
        return matrix.size();
    }

    public long getMaxVal() {
        return max;
    }

    public double getH(){
        return Math.ceil(log(max+1, 2));
    }

    private double log(int x, int base) {
        return  (Math.log(x) / Math.log(base));
    }
}
