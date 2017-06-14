
package test;

import javax.ejb.*;
import java.rmi.*;

/**
 * @author mvatkina
 */

public interface A1Home extends javax.ejb.EJBHome {

    public A1 findByPrimaryKey(java.lang.String pk)  
            throws FinderException, RemoteException;
    
    public java.util.Collection findByShortName(java.lang.String shortName)  
            throws FinderException, RemoteException;
    
}
