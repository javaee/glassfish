
package pkvalidation;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface CHome extends javax.ejb.EJBHome {
    
    public  C findByPrimaryKey(CPK pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  C create(long i, java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
