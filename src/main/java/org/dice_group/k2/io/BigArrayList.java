package org.dice_group.k2.io;

import java.util.*;

public class BigArrayList<E> {

    private static final int MAX_ARRAY_SIZE = 2147483639;
    private long size;


    public List<List<E>> lists = new ArrayList<List<E>>();


    public BigArrayList(Long initspace){
        long size=MAX_ARRAY_SIZE;
        for(;size<initspace;size+=MAX_ARRAY_SIZE){
            lists.add(new ArrayList<E>(MAX_ARRAY_SIZE));
        }
        lists.add(new ArrayList<E>(initspace.intValue()));
    }

    public BigArrayList(){
        lists.add(new ArrayList<E>());
    }

    public long size() {
        return MAX_ARRAY_SIZE*(lists.size()-1)+lists.get(lists.size()-1).size();
    }


    public void set(long i, E object){
        //locate block
        Double block = Math.floor(i*1.0/MAX_ARRAY_SIZE);
        //calculate index in block
        int indexInBlock = Long.valueOf(i-block.intValue()).intValue();
        //add object at correct location
        lists.get(block.intValue()).set(indexInBlock, object);
        if(size<i){
            size=i;
        }
    }

    public void add(E object) {
        //check if array is full
        if(lists.get(lists.size()-1).size()==MAX_ARRAY_SIZE){
            lists.add(new ArrayList<E>());
        }

        if(size<MAX_ARRAY_SIZE){
            lists.get(0).add(object);
            size++;
        }
        else{
            Double block = Math.floor(size*1.0/MAX_ARRAY_SIZE);
            //calculate index in block
            //add object at correct location
            lists.get(block.intValue()).add(object);
        }

    }

    public E get(long i){
        Double block = Math.floor(i*1.0/MAX_ARRAY_SIZE);
        //calculate index in block
        int indexInBlock = Long.valueOf(i-block.intValue()).intValue();
        return lists.get(block.intValue()).get(indexInBlock);
    }

    public void clear(){
        for(List<E> arr : lists){
            arr.clear();
        }
        lists.clear();
    }
}
