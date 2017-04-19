package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb;

import javax.persistence.*;

@Entity
@Table(name = "EJB32_PERSISTENCE_CONTEXT_PERSON")
public class Person implements java.io.Serializable {

    @Id
    private int id;
    
    private String name;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }
    
    public Person(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Person: {" + "(name = " + name + ")}";
    }
    
    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
