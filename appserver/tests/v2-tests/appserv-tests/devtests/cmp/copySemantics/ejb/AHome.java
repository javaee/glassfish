
package test;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface AHome extends javax.ejb.EJBHome {
    
    public  A create(Integer i, java.lang.String name, java.util.Date d, byte[] b) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
