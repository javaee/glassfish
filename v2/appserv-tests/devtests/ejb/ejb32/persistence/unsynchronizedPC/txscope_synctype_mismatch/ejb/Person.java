package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.persistence.*;

@Entity
@Table(name = "EJB32_PERSISTENCE_CONTEXT_PERSON")
public class Person implements java.io.Serializable {

    @Id
    String name;

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
}
