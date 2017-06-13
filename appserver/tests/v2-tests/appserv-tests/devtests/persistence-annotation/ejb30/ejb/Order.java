/*
 * Order.java
 *
 * Created on February 23, 2005, 8:35 PM
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
public class Order {
    private Long id;
    private int version;
    private int itemId;
    private int quantity;
    private Customer customer;
    @Id(generate=AUTO)
    public Long getId() {
        return id;
    } 
    public void setId(Long id) { 
        this.id = id; 
    } 
    @Version 
    protected int getVersion() { 
        return version; 
    } 
    protected void setVersion(int version) { 
        this.version = version; 
    } 
    @Basic 
    public int getItemId() { 
        return itemId; 
    } 
    public void setItemId(int itemId) { 
        this.itemId = itemId; 
    } 
    @Basic public int getQuantity() { 
        return quantity; 
    } 
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    } 
    @ManyToOne public Customer getCustomer() { 
        return customer; 
    } 
    public void setCustomer(Customer cust) { 
        this.customer = cust; 
    } 
}
