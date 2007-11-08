
package com.sun.s1asdev.jdbc.cmpsimple.ejb;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface BlobTestHome extends javax.ejb.EJBHome {
    /*    
    public  BlobTest create(Integer i, java.lang.String name, byte[] b) throws java.rmi.RemoteException, javax.ejb.CreateException;
    
    public  BlobTest findByPrimaryKey (Integer key) throws java.rmi.RemoteException, javax.ejb.FinderException;
    */
    public  BlobTest create(Integer i, java.lang.String name) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
    public  BlobTest findByPrimaryKey (Integer key) 
        throws java.rmi.RemoteException, javax.ejb.FinderException;
}
