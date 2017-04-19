/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package helloservice;

import javax.rmi.*;
import java.rmi.*;
import javax.naming.*;
import javax.ejb.*;
import statelesshello.*;

public class SayHelloImpl implements SayHello {

	public String message ="Message from servlet is : Bonjour ";

	public String sayHello(String s) throws RemoteException {
	    try {

		Context ic = new InitialContext();

		// create EJB using factory from container
		java.lang.Object objref = ic.lookup("java:comp/env/ejb/MyStatelesshello");
		System.out.println("Looked up home!!");

		StatelesshelloHome home =
		    (StatelesshelloHome) PortableRemoteObject.narrow(
				objref, StatelesshelloHome.class);
		System.out.println("Narrowed home!!");

		Statelesshello hr = home.create();
		System.out.println("Got the EJB!!");

		// invoke method on the EJB
		message = hr.sayStatelesshello() + ";" + message + s;
	    } catch (Exception e) {
		System.out.println("Servlet sayHelloImpl recd exception : " +
				   e.getMessage());
		e.printStackTrace();
	    }
	    return message;
	} 
}
