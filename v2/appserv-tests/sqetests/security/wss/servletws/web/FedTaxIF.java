package  com.sun.appserv.sqe.security.wss.servletws.taxcal;

import java.rmi.RemoteException;
import java.rmi.Remote;


public interface FedTaxIF extends Remote{

	public double getFedTax(double income, double deductions) throws RemoteException;

}
