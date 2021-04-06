package org.dice_group.k2.util;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private final byte h;
    private byte last;
    private List<List<Byte>> paths = new ArrayList<List<Byte>>();

    public Path(byte hSize) {
        this.h=hSize;
        for(int i=0;i<h;i++){
            paths.add(new ArrayList<Byte>());
        }
    }

    public boolean hasLast() {
        return last!='\00';
    }

    public void addLast(int i) {
        byte x=3;
        for (byte j = 8; j >= 1; j /= 2) {
            if((last & j)!=0){
               paths.get(i).add(x);
            }
            x--;
        }
        last='\00';
    }

    public void add(int i, byte b) {

        //dissasmble newpath
        byte x=3;
        for(int j=128;j>=16;j/=2){
            if((b & j)!=0){
                paths.get(i).add(x);
            }
            x--;
        }
        //we were already at the last point
        if(i+1==h) {
            //shift;
            byte newLast = Integer.valueOf(b & 15).byteValue();
            last=newLast;
        }else {
            x=3;
            for (byte j = 8; j >= 1; j /= 2) {
                if((b & j)!=0){
                    paths.get(i+1).add(x);
                }
                x--;
            }
        }

    }

    public List<Point> calculatePoints() {
        List<Point> ret = new ArrayList<Point>();
        while(!isEmpty(h-1)) {
            ret.add(calculatePoint());
            pop(h - 1);
        }
        return ret;
    }

    private Point calculatePoint() {
        long row=0, col=0;
        byte x=0;
        for(int i=h;i>0;i--){
            byte p =paths.get(i-1).get(paths.get(i-1).size()-1);
            if(p==1){col+=Math.pow(2, x);}
            if(p==2){row+=Math.pow(2, x);}
            if(p==3){row+=Math.pow(2, x);col+=Math.pow(2, x);}
            x++;
        }
        Integer r = Long.valueOf(row).intValue();
        Integer c = Long.valueOf(col).intValue();
        return new Point(r, c);
    }

    public void pop(int i) {
        paths.get(i).remove(paths.get(i).size()-1);
    }

    public boolean isEmpty(int i) {
        return paths.get(i).isEmpty();
    }
}
