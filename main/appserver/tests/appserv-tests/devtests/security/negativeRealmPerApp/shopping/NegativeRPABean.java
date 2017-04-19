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
