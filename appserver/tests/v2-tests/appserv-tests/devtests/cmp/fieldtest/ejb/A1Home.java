
package fieldtest;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A1Home extends javax.ejb.EJBHome {
    
    public  A1 findByPrimaryKey(A1PK pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A1 create(java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
