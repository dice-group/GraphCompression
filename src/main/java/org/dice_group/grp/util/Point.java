package org.dice_group.grp.util;

public class Point {


    private int row;
    private int col;

    public Point(int row, int col){
        this.row=row;
        this.col=col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
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
