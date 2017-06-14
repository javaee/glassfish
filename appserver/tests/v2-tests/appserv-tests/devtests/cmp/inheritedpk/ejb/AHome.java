
package pkvalidation;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface AHome extends javax.ejb.EJBHome {
    
    public  A findByPrimaryKey(APK pk)  throws java.rmi.RemoteException, javax.ejb.FinderException;
    
    public  A create(long i, java.lang.String name, double s) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
