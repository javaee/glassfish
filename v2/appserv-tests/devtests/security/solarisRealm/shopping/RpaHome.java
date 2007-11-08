/*
 * RpaHome.java
 *
 * Created on May 15, 2003, 5:21 PM
 */

package shopping;
import javax.ejb.EJBHome;
import shopping.RpaRemote;
/**
 *
 * @author  Harpreet Singh
 */
public interface RpaHome extends EJBHome{
    
    public RpaRemote create(java.lang.String shopperName) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
