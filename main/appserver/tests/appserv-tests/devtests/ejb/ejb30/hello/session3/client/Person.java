package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.persistence.*;

@Entity
@Table(name="EJB30_HELLO_SESSION3_PERSON")
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
 
