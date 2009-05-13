package com.sun.s1asdev.ejb.ejb30.persistence.eem_injection;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;

@Entity
@Table(name = "EJB30_PERSISTENCE_EEM_INJECTION_PERSON")
public class Person implements java.io.Serializable {

    @Id
    String name;

    String data;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
        this.data = "data: " + name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override 
    public String toString() {
        return "Person: (name=" + name + "; data= " + data + ")";
    }
}
