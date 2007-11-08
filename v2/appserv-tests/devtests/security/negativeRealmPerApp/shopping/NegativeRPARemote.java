/*
 * NegativeRPARemote.java
 *
 * Created on May 15, 2003, 5:09 PM
 */

package shopping;
import javax.ejb.EJBObject;
/**
 * Negative RealmPerAPP Stateful Session Bean. 
 * Tries to log into a non existent realm
 * All methods should be uncallableN
 * @author  Harpreet Singh
 */
public interface NegativeRPARemote extends EJBObject {
            
    public void addItem(java.lang.String item, int price) throws java.rmi.RemoteException;
    
    public void deleteItem(java.lang.String item) throws java.rmi.RemoteException;
       
    public double getTotalCost() throws java.rmi.RemoteException;
    
    public java.lang.String[] getItems() throws java.rmi.RemoteException;
}
