/*
 * ShoppingCartHome.java
 *
 * Created on May 15, 2003, 5:21 PM
 */

package shopping;
import javax.ejb.EJBHome;
import shopping.ShoppingCartRemote;
/**
 *
 * @author  hsingh
 */
public interface ShoppingCartHome extends EJBHome{
    
    public ShoppingCartRemote create(java.lang.String shopperName) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
