/*
 * @author Marina Vatkina
 */

package com.acme;

import javax.persistence.*;

@Entity
public class FooEntity {
    
    @Id 
    @GeneratedValue
    private int id;
    private String name;

    public FooEntity(String name) {
        setName(name);
    }
    
    public FooEntity() {
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
