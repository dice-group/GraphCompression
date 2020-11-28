package org.dice_group.grp.util;

public class Point {


    private Integer row;
    private Integer col;

    public Point(Integer row, Integer col){
        this.row=row;
        this.col=col;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    @Override
    public boolean equals(Object o){
        if(o!=null && o instanceof Point){
            if(((Point) o).getCol()==col && ((Point) o).getRow()==row){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        return (row+" "+col).hashCode();
    }

    @Override
    public String toString(){
        return "Row: "+this.getRow() +" , Col: "+this.getCol();
    }

}
