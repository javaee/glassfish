
package fieldtest;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2Home extends javax.ejb.EJBHome {
    
    public  A2 findByPrimaryKey(A2PK pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A2 create(java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
