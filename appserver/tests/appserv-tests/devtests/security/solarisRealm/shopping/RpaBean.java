/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * ShoppingEJB.java
 *
 * Created on May 15, 2003, 5:16 PM
 */

package shopping;

import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import java.util.Vector;
import java.lang.String;
import java.util.Iterator;
import javax.ejb.EJBException;
import java.rmi.RemoteException;
/**
 *
 * @author  Harpreet
 * @version
 */

public class RpaBean implements SessionBean {
    
    private String shopper = "anonymous";
    private int totalPrice = 0;
    
    private int totalItems = 0;
    
    private Vector items;
    
    private Vector itemPrice;
    
    private SessionContext sc = null;
    
    /** Creates a new instance of ShoppingEJB */
    public void ejbCreate(String shopperName) {
        shopper = shopperName;
        items = new Vector();
        itemPrice = new Vector();
    }
    
    public void addItem(java.lang.String item, int price) throws EJBException,
        RemoteException{
        items.add(item);
        itemPrice.add(new Integer(price));
        totalItems++;
        totalPrice += price;
        System.out.println(" Shopping Cart: Shopper "+ shopper +" has bought "
            + item +" for price ="+ price +" .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
        
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());
    }
    
    public void deleteItem(java.lang.String item) throws EJBException, 
        RemoteException{
        int index = items.indexOf(item);
        items.remove(item);
        Integer price = (Integer) itemPrice.get(index);
        System.out.println("Shopping Cart: Removing item "+ item +" @price "+ 
            price.intValue());
        totalPrice -= price.shortValue();
        itemPrice.remove(index);                    
        System.out.println(" Shopping Cart: Shopper "+ shopper +"  .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());
    }
    
    public double getTotalCost() throws EJBException{
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());

        return totalPrice;
    }
    
    public String[] getItems() throws EJBException{
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());

        Iterator it = items.iterator();
        int sz = items.size();
        String[] itemNames = new String[sz];
        for(int i=0; it.hasNext();){
            itemNames[i++] = new String( (String)it.next());
        }
        return itemNames;
    }
    
    public void ejbActivate() {
        System.out.println("In Rpa ejbActivate");
    }
    
    
    public void ejbPassivate() {
        System.out.println("In Rpa ejbPassivate");
    }
    
    
    public void ejbRemove()  {
        System.out.println("In Rpa ejbRemove");
    }
    
    
    public void setSessionContext(javax.ejb.SessionContext sessionContext) {
        sc = sessionContext;
    }
    
}
