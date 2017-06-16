/*
 * Employee.java
 *
 * Created on February 23, 2005, 8:22 PM
 */

package com.acme;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;
/**
 *
 * @author ss141213
 */
@Entity(access=FIELD)
public class Customer { 
    @Id(generate=AUTO) 
    protected Long id; 
    @Version protected int version; 
    @ManyToOne protected Address address;
    @Basic protected String description; 
    @OneToMany(targetEntity="com.acme.Order", mappedBy="customer") 
    protected Collection orders = new Vector(); 
    @ManyToMany(mappedBy="customers") 
    protected Set<DeliveryService> serviceOptions = new HashSet(); 
}
