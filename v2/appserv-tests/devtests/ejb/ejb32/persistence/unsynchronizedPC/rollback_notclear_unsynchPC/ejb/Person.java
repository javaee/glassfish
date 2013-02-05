package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.ejb;

import javax.persistence.*;

@Entity
@Table(name = "EJB32_PERSISTENCE_CONTEXT_PERSON")
public class Person implements java.io.Serializable {

    @Id
    private String name;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person: {" + "(name = " + name + ")}";
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
