package samples.ejb.subclassing.ejb;

import java.rmi.RemoteException;

public interface Customer extends javax.ejb.EJBObject
{
  public String getLastName() throws RemoteException;

  public String getFirstName() throws RemoteException;

  public String getAddress1() throws RemoteException;

  public String getAddress2() throws RemoteException;

  public String getCity() throws RemoteException;

  public String getState() throws RemoteException;

  public String getZipCode() throws RemoteException;

  public String getSSN() throws RemoteException;

  public long getSavingsBalance() throws RemoteException;

  public long getCheckingBalance() throws RemoteException;

  public void doCredit(long amount, String accountType) throws RemoteException;

  public void doDebit(long amount, String accountType) throws RemoteException;

}

  
