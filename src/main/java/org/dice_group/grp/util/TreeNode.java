package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TreeNode {

    private byte value = 0;
    private TreeNode[] children = new TreeNode[]{null,null,null,null};
    private TreeNode parent;

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }


    public TreeNode[] getChildren() {
        return children;
    }

    public void setChildren(TreeNode[] children) {
        this.children = children;
    }

    public void setChild(int i, TreeNode child){
        this.children[i] = child;
    }

    public TreeNode setChildIfAbsent(int i, TreeNode child){
        if(this.children[i]==null) {
            value+=Math.pow(2, i);
            this.children[i] = child;
        }else{
            child=null;
        }
        return this.children[i];
    }


    public TreeNode getChild(int i){
        return this.children[i];
    }

    public Byte getRawValue(boolean reverse){
        if(!reverse)
            return value;
        Byte ret=Integer.valueOf((value&8)/8).byteValue();
        ret = Integer.valueOf(ret | (value&4)/2).byteValue() ;
        ret = Integer.valueOf(ret | (value&2)*2).byteValue() ;
        return Integer.valueOf(ret | (value&1)*8).byteValue() ;
    }

    public Byte[] getValue() {
        byte b0 = Integer.valueOf(value&1).byteValue();
        byte b1 = Integer.valueOf((value&2)/2).byteValue();
        byte b2 = Integer.valueOf((value&4)/4).byteValue();
        byte b3 = Integer.valueOf((value&8)/8).byteValue();
        Byte[] ret = new Byte[]{b0,b1, b2, b3};
        return ret;
    }

    public void setValue(Byte[] value) {
        int j=1;
        for(int i=0; i<value.length;i++){
            this.value+=value[i]*j;
            j*=2;
        }
    }

    public List<List<Byte>> createPaths(){
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        paths.add(new ArrayList<Byte>());
        return createPathsRec(paths);
    }

    private List<List<Byte>> createPathsRec(List<List<Byte>> cpaths){
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        List<List<Byte>> ret = new ArrayList<List<Byte>>();
        byte j=1;
        for(Integer i=0;i<4;i++){
            if((value&j)>=1 && children[i]!=null) {
                for (List<Byte> cpath : cpaths) {
                    List<Byte> p = new ArrayList<Byte>(cpath);
                    p.add(i.byteValue());
                    paths.add(p);
                }

                //we have to add the return somewhere
                ret.addAll(children[i].createPathsRec(paths));
            }
            j*=2;
        }
        return ret;
    }


    //TODO check if the problem occurs here
    public Collection<? extends List<Byte>> createUpwardsPath() {
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        byte j=1;
        for(Integer i=0; i<4;i++) {
            if((value&j)>=1) {

                List<Byte> path = new ArrayList<Byte>();
                path.add(0, i.byteValue());
                TreeNode cParent = this;

                while(cParent.getParent()!=null){
                    path.add(0, cParent.getParent().getIndex(cParent));
                    cParent=cParent.getParent();
                }
                paths.add(path);
            }
            j*=2;
        }
        return paths;
    }

    private Byte getIndex(TreeNode child) {
        for(Byte i=0;i<4;i++){
            if(child.equals(children[i])){
                return i;
            }
        }
        return null;

    }

    public void clear() {
        this.value=0;
        this.children=null;
        this.parent=null;
    }
}
