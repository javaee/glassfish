/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1peqe.ejb.cmp.preselect.ejb;

import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

public abstract class CustomerBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields
    public abstract String getId();
    public abstract void setId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);
  
    // Access methods for relationship fields
    public abstract Collection getItems();
    public abstract void setItems(Collection items);

    // Select methods
    public abstract Collection ejbSelectItemsLessThan100(CustomerLocal customer)
        throws FinderException;

    // Business methods
    public int getItemsForLess() throws FinderException, RemoteException {
         int count = 0;
         CustomerLocal customer = 
             (CustomerLocal)context.getEJBLocalObject();
	 try {
	   InitialContext initial = new InitialContext();
	   Object objref = initial.lookup("java:comp/env/ejb/SimpleItem");
	   ItemLocalHome itemHome = 
	     (ItemLocalHome)PortableRemoteObject.narrow(objref, 
                                                        ItemLocalHome.class);
	   ItemLocal item = itemHome.create("Dark Chocolate", "1", 20.00);
	   getItems().add(item);
	   	   
	   item = itemHome.create("Milk Chocolate", "2", 30.00);
	   getItems().add(item);

	   item = itemHome.create("White Chocolate", "3", 40.00);
	   getItems().add(item);

	   System.out.println("calling ejbSelectItemsLessThan100...");
	   Collection items = ejbSelectItemsLessThan100(customer);	   

	   if (items != null) {
	       
	       for (Iterator iterator = items.iterator(); iterator.hasNext();) {
		   System.out.println("Item.price < 100 : " +
				      ((ItemLocal)iterator.next()).getId());
		   count++;
	       }
	   }
	   return count;

	 } catch (Exception e) {
	     throw new EJBException(e.getMessage());
	 }
    }

    // EntityBean  methods
    public String ejbCreate (String id, String name) throws CreateException {

        System.out.print("CustomerBean ejbCreate");
        setId(id);
        setName(name);
	return null;
    }
         
    public void ejbPostCreate (String id, String name) throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }
    
    public void unsetEntityContext() {
        context = null;
    }
    
    public void ejbRemove() {
        System.out.print("CustomerBean ejbRemove");
    }
    
    public void ejbLoad() {
        System.out.print("CustomerBean ejbLoad");
    }
    
    public void ejbStore() {
        System.out.print("CustomerBean ejbStore");


    }
    
    public void ejbPassivate() { }
    
    public void ejbActivate() { }
}
