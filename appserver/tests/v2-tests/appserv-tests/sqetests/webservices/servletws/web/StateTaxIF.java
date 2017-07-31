package com.sun.s1peqe.webservices.servlet.taxcal;

import java.rmi.RemoteException;
import java.rmi.Remote;


public interface StateTaxIF extends Remote{

	public double getStateTax(double income, double deductions) throws RemoteException;

}
