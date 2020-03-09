package org.dice_group.grp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TreeNode {

    private Byte[] value = new Byte[]{0,0,0,0};
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
            value[i]=1;
            this.children[i] = child;
        }else{
            child=null;
        }
        return this.children[i];
    }


    public TreeNode getChild(int i){
        return this.children[i];
    }

    public Byte[] getValue() {
        return value;
    }

    public void setValue(Byte[] value) {
        this.value = value;
    }

    public List<List<Byte>> createPaths(){
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        paths.add(new ArrayList<Byte>());
        return createPathsRec(paths);
    }

    private List<List<Byte>> createPathsRec(List<List<Byte>> cpaths){
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        List<List<Byte>> ret = new ArrayList<List<Byte>>();
        for(Integer i=0;i<4;i++){
            if(value[i]==1 && children[i]!=null) {
                for (List<Byte> cpath : cpaths) {
                    List<Byte> p = new ArrayList<Byte>(cpath);
                    p.add(i.byteValue());
                    paths.add(p);
                }
                //we have to add the return somewhere
                ret.addAll(children[i].createPathsRec(paths));
            }

        }
        return ret;
    }


    //TODO check if the problem occurs here
    public Collection<? extends List<Byte>> createUpwardsPath() {
        List<List<Byte>> paths = new ArrayList<List<Byte>>();
        for(Integer i=0; i<4;i++) {
            if(value[i]==1) {

                List<Byte> path = new ArrayList<Byte>();
                path.add(0, i.byteValue());
                TreeNode cParent = this;
                do{
                    path.add(0, cParent.getParent().getIndex(cParent));
                    cParent=cParent.getParent();
                }while(cParent.getParent()!=null);
                paths.add(path);
            }

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
}
