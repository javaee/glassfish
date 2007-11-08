/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package helloservice;

import java.rmi.RemoteException;

public class SayHelloImpl implements SayHello {

	public String message ="Bonjour ";

	public String sayHello(String s) throws RemoteException {
		return message + s;
	}
} 