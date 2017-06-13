/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package j2eeguide.product;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface Product extends EJBObject {
 
   public void setPrice(double price) throws RemoteException;

   public double getPrice() throws RemoteException;  

   public String getDescription() throws RemoteException;
}
