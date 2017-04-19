/*
 * ShoppingCart.java
 *
 * Created on May 15, 2003, 5:09 PM
 */

package shopping;
import javax.ejb.EJBObject;
/**
 * Shopping Cart Stateful Session Bean. Just tests -Dj2eelogin.name 
 *  -Dj2eelogin.password system properties.
 * @author  hsingh
 */
public interface ShoppingCartRemote extends EJBObject {
            
    public void addItem(java.lang.String item, int price) throws java.rmi.RemoteException;
    
    public void deleteItem(java.lang.String item) throws java.rmi.RemoteException;
       
    public double getTotalCost() throws java.rmi.RemoteException;
    
    public java.lang.String[] getItems() throws java.rmi.RemoteException;
    public void saveAsQuote() throws java.rmi.RemoteException;
}
