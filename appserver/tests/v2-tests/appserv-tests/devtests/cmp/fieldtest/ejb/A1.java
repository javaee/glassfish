
package fieldtest;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A1 extends javax.ejb.EJBObject {
 
    public String getName() throws java.rmi.RemoteException;

    public java.util.Date getMyDate() throws java.rmi.RemoteException;

    public java.sql.Date getSqlDate() throws java.rmi.RemoteException;
    
    public byte[] getBlb() throws java.rmi.RemoteException;
    
    public java.util.ArrayList getList() throws java.rmi.RemoteException;
    
    public void update() throws java.rmi.RemoteException;
    
}

