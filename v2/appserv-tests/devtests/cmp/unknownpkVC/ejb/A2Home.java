
package unknownpk;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2Home extends javax.ejb.EJBHome {
    
    public  A2 findByPrimaryKey(Object pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A2 create(java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
