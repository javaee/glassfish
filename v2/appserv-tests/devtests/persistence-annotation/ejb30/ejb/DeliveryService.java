/*
 * DeliveryService.java
 *
 * Created on February 23, 2005, 8:38 PM
 */

package com.acme;
import java.util.Collection;
import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;

/**
 *
 * @author ss141213
 */
@Entity 
public class DeliveryService { 
    private String serviceName; 
    private int priceCategory; 
    private Collection customers; 
    @Id public String getServiceName() { 
        return serviceName; 
    } 
    public void setServiceName(String serviceName) { 
        this.serviceName = serviceName; 
    } 
    @Basic public int getPriceCategory() { 
        return priceCategory; 
    } 
    public void setPriceCategory(int priceCategory) { 
        this.priceCategory = priceCategory; 
    } 
    @ManyToMany(targetEntity="com.acme.Customer") 
    @AssociationTable(table=@Table(schema="TEST1"))
    public Collection getCustomers() { 
        return customers; 
    } 
    public void setCustomers(Collection customers) { 
        this.customers = customers; 
    } 
}
