/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 *
 */

package com.sun.s1peqe.ejb.cmp.preselect.ejb;

import java.util.*;
import javax.ejb.*;
import javax.naming.*;

public abstract class ItemBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields
    public abstract String getId();
    public abstract void setId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);

    public abstract double getPrice();
    public abstract void setPrice(double price);
  

    // Business methods
    public void modifyPrice(double newPrice) {
        setPrice(newPrice);
    }


    // EntityBean  methods
    public String ejbCreate (String id, String name, double price) throws CreateException {

        System.out.print("ItemBean ejbCreate");
        setId(id);
        setName(name);
	setPrice(price);
	return null;
    }
         
    public void ejbPostCreate (String id, String name, double price) throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }
    
    public void unsetEntityContext() {
        context = null;
    }
    
    public void ejbRemove() {
        System.out.print("ItemBean ejbRemove");
    }
    
    public void ejbLoad() {
        System.out.print("ItemBean ejbLoad");
    }
    
    public void ejbStore() {
        System.out.print("ItemBean ejbStore");
	ItemLocal item = (ItemLocal)context.getEJBLocalObject();
	
	System.out.println("Item price less than $100 : " + 
			   item.getId());
	System.out.println("Modifying its price to $200...");
	item.modifyPrice(200.00);
    }
    
    public void ejbPassivate() { }
    
    public void ejbActivate() { }
}
