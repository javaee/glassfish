package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Collection;
import javax.ejb.CreateException;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import java.util.Date;
import javax.ejb.FinderException;

public interface AccountRemoteHome extends javax.ejb.EJBHome
{
	public AccountRemote createAccount (AccountDataObject dao)
	throws javax.ejb.CreateException,RemoteException;
        public AccountRemote findByPrimaryKey(String id) throws javax.ejb.FinderException,RemoteException;

}
