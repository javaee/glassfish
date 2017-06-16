/*
 * RpaHome.java
 *
 * Created on May 15, 2003, 5:21 PM
 */

package com.sun.devtest.admin.synchronization.api.security.shopping;
import javax.ejb.EJBHome;
/**
 *
 * @author  Harpreet Singh
 */
public interface RpaHome extends EJBHome{
    
    public RpaRemote create(java.lang.String shopperName) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
