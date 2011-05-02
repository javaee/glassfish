
package pkvalidation;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface BHome extends javax.ejb.EJBHome {
    
    public  B findByPrimaryKey(java.sql.Date pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  B create(java.sql.Date i, java.lang.String name) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
