package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import javax.ejb.EJBObject;

public interface AccountRemote extends javax.ejb.EJBObject
{
	public AccountDataObject getDAO() throws java.rmi.RemoteException;
}
