package com.sun.s1asdev.security.wss.defprovider.servlet.taxcal;

import java.rmi.RemoteException;
import java.rmi.Remote;


public interface FedTaxIF extends Remote{

	public double getFedTax(double income, double deductions) throws RemoteException;

}
