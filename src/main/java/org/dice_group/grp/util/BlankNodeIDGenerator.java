package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.List;

public class BlankNodeIDGenerator {

    private static int id=0;
    private static List<String> idList = new ArrayList<String>();

    public static int getNextID(){
        return id++;
    }

    public static void reset(){
        id=0;
        idList.clear();
    }

    public static int getID(String n) {
        int ret = idList.indexOf(n);
        if(ret<0){
            idList.add(n);
            return idList.size()-1;
        }
        return ret;
    }
}
