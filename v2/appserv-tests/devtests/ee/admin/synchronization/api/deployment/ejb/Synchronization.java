package com.sun.s1asdev.admin.ee.synchronization.api.deployment;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.io.IOException;
import com.sun.enterprise.ee.synchronization.SynchronizationException;


public interface Synchronization
    extends EJBObject
{

	public boolean getFile(String instanceName, String sourceFile, 
		String destLoc) throws RemoteException;

	public boolean get(String instanceName, String name, String type,
		String destLoc) throws RemoteException;

	public boolean putFile(String instanceName, String sourceFile, 	
		String destDir) throws RemoteException;


}
