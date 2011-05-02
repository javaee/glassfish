
package create;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A1UnPKHome extends javax.ejb.EJBHome {
    
    public  java.util.Collection findAll()  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A1 create(java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
    public  A1 create(int i) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
