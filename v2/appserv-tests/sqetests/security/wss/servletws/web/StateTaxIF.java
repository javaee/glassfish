package  com.sun.appserv.sqe.security.wss.servletws.taxcal;

import java.rmi.RemoteException;
import java.rmi.Remote;


public interface StateTaxIF extends Remote{

	public double getStateTax(double income, double deductions) throws RemoteException;

}
