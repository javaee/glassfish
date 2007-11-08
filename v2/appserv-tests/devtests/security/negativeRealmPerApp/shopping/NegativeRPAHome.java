/*
 * NegativeRPAHome.java
 *
 */

package shopping;
import javax.ejb.EJBHome;
import shopping.NegativeRPARemote;
/**
 *
 * @author  Harpreet Singh
 * @version
 */
public interface NegativeRPAHome extends EJBHome{
    
    public NegativeRPARemote create(java.lang.String shopperName) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
