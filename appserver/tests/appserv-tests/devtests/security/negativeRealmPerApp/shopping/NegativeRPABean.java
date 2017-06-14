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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 *
 * @author  Harpreet Singh
 */

public class NegativeRPABean implements SessionBean {
    
    private String shopper = "anonymous";
    private String principal = "j2ee";
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
        // this method should be uncallable.
       throw new EJBException("Method should be uncallable ");
    }
    
    public void deleteItem(java.lang.String item) throws EJBException, 
        RemoteException{
        // this method should be uncallable.
       throw new EJBException("Method should be uncallable ");

    }
    
    public double getTotalCost() throws EJBException{
        // this method should be uncallable.
       throw new EJBException("Method should be uncallable ");
    }
    
    public String[] getItems() throws EJBException{
       // this method should be uncallable.
       throw new EJBException("Method should be uncallable ");
    }
    
    public void ejbActivate() {
        System.out.println("In ShoppingCart ejbActivate");
    }
    
    
    public void ejbPassivate() {
        System.out.println("In ShoppingCart ejbPassivate");
    }
    
    
    public void ejbRemove()  {
        System.out.println("In ShoppingCart ejbRemove");
    }
    
    
    public void setSessionContext(SessionContext sessionContext) {
        sc = sessionContext;
    }
    
}
