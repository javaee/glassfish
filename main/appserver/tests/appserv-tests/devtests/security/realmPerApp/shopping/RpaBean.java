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
    private String principal = "harpreet";
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
        checkCallerPrincipal();
        items.add(item);
        itemPrice.add(new Integer(price));
        totalItems++;
        totalPrice += price;
        System.out.println(" Shopping Cart: Shopper "+ shopper +" has bought "
            + item +" for price ="+ price +" .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
    }
    
    public void deleteItem(java.lang.String item) throws EJBException, 
        RemoteException{
        checkCallerPrincipal();
        int index = items.indexOf(item);
        items.remove(item);
        Integer price = (Integer) itemPrice.get(index);
        System.out.println("Shopping Cart: Removing item "+ item +" @price "+ 
            price.intValue());
        totalPrice -= price.shortValue();
        itemPrice.remove(index);                    
        System.out.println(" Shopping Cart: Shopper "+ shopper +"  .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
    }
    
    public double getTotalCost() throws EJBException{
        checkCallerPrincipal();
        return totalPrice;
    }
    
    public String[] getItems() throws EJBException{
        checkCallerPrincipal();

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
    
    private void checkCallerPrincipal() throws EJBException {
        System.out.println("Caller Princial = " + sc.getCallerPrincipal() +
                " comparing against " + principal);

        if (!sc.getCallerPrincipal().getName().equals(principal)) {
            throw new EJBException("Wrong Principal. Principal should be = "
            + principal);
        }

    }
}
