package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabledMatrix {



    private Integer labelId=0;
    private Set<Point> matrix = new HashSet<Point>();


    public LabledMatrix(){
    }

    public LabledMatrix(Integer labelId){
        this.labelId = labelId;
    }


    public Integer getLabelId() {
        return labelId;
    }
    public void set(int row, int col){
        matrix.add(new Point(row, col));
    }

    public int get(int row, int col){
        if(matrix.contains(new Point(row, col))){
            return 1;
        }
        return 0;
    }


    public Set<Point> getPoints(){
        return matrix;
    }

    public Integer getSize() {
        return matrix.size();
    }
}
