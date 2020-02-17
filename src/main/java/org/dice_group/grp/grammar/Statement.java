package org.dice_group.grp.grammar;

public class Statement {

    private  Integer object;
    private  Integer subject;
    private  Integer predicate;

    public Statement(Integer subject, Integer predicate, Integer object){
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public Integer getObject() {
        return object;
    }

    public void setObject(Integer object) {
        this.object = object;
    }

    public Integer getSubject() {
        return subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getPredicate() {
        return predicate;
    }

    public void setPredicate(Integer predicate) {
        this.predicate = predicate;
    }




}
