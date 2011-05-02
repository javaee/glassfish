
package create;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2UnPKHome extends javax.ejb.EJBHome {
    
    public  java.util.Collection findAll()  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A2 create(java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
    public  A2 create(int i) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
