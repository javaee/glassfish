/*
 * Order.java
 *
 * Created on February 23, 2005, 8:31 PM
 */

package com.acme;

import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;

/**
 *
 * @author ss141213
 */
@Entity 
public class Address { 
    private Long id; 
    private int version; 
    private String street; 
    @Id(generate=AUTO) public Long getId() { 
        return id; 
    } 
    public void setId(Long id) { 
        this.id = id; 
    } 
    @Version protected int getVersion() { 
        return version; 
    } 
    protected void setVersion(int version) { 
        this.version = version; 
    } 
    @Basic public String getStreet() { 
        return street; 
    } 
    public void setStreet(String street) { 
        this.street = street; 
    } 
}
