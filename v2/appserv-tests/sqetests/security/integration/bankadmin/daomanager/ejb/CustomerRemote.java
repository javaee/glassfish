package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public interface CustomerRemote extends javax.ejb.EJBObject
{
  public String getCustomerID() throws java.rmi.RemoteException;
  public String getCustomerName() throws java.rmi.RemoteException;
  public void setCustomerName(String name) throws java.rmi.RemoteException;
  public void addAccount(AccountDataObject ado) throws java.rmi.RemoteException;
  public boolean TestCallerInRole() throws java.rmi.RemoteException;

}
