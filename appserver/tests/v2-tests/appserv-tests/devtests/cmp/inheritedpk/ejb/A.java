
package pkvalidation;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A extends javax.ejb.EJBObject {
 
    public String getLastname() throws java.rmi.RemoteException;
    
}

