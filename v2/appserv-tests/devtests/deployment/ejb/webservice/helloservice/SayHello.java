/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package helloservice;
 
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SayHello  extends Remote {
	public String sayHello(String name) throws RemoteException;
}
  	
