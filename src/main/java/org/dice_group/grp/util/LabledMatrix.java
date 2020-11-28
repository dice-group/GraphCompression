package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.Collection;

public class LabledMatrix {



    private Integer labelId=0;
    private Collection<Point> matrix = new ArrayList<>();

    public int max=-1;

    public LabledMatrix(){
    }

    public LabledMatrix(Integer labelId){
        this.labelId = labelId;
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
