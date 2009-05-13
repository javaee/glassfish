package com.sun.s1asdev.ejb.ejb30.persistence.context;

import javax.persistence.*;

@Entity
@NamedQuery(name="findPersonByName",
            query="SELECT OBJECT(p) FROM Person p WHERE p.name LIKE :pName")
@SqlResultSetMapping(name="PersonSqlMapping",
                     entities=@EntityResult(entityClass=Person.class))
@Table(name="EJB30_PERSISTENCE_CONTEXT_PERSON")

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

    public String getName() {
        return name;
    }
}
 
