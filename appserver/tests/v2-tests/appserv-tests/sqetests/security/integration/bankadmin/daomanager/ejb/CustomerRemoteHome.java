package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import javax.ejb.EJBHome;
import javax.ejb.FinderException;
import java.util.Collection;
import java.rmi.RemoteException;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public interface CustomerRemoteHome extends javax.ejb.EJBHome
{
    public CustomerRemote createCustomer (String id,String name)
    throws javax.ejb.CreateException,RemoteException;
    public CustomerRemote findByPrimaryKey(String id)
    throws javax.ejb.FinderException,RemoteException;
}
