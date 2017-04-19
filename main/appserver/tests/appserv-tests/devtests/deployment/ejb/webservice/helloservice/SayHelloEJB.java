/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package helloservice;

import javax.ejb.*;
import java.rmi.RemoteException;

public class SayHelloEJB implements SessionBean {

	private SessionContext sc;

	public SayHelloEJB() {}

	public void ejbCreate() throws CreateException {}

	public String sayHello(String s) throws RemoteException {
		return "Hello EJB returns your Hello : " + s;
	}

	public void setSessionContext(SessionContext sc) {
				this.sc = sc;
	}

	public void ejbRemove() throws RemoteException {}
	public void ejbActivate() {}
	public void ejbPassivate() {}
} 
