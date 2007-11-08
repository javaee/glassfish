package com.sun.s1asdev.ejb.ejb30.persistence.extendedem;

import javax.ejb.*;
import javax.persistence.*;

@Entity
@Table(name="EJB30_PERSISTENCE_EXTENDEDEM_PERSON")
public class Person implements java.io.Serializable {

    @Id String name;

    public Person(){
    }

    public Person(String name){
        this.name = name;
    }

    @Override public String toString(){
        return "Person: {"+"(name = "+name+")}";
    }

}

