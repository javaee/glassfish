/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package standaloneclient;

import java.util.*;
import javax.ejb.EJBHome;
import statelesshello.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;


public class HelloClient {

    public static void main(String[] args) {

	boolean testPositive = (Boolean.valueOf(args[0])).booleanValue();
	if(testPositive)
	    System.out.println("Test expects successful result");
	else
	    System.out.println("Test expected to fail");
        try {

            Context ic = new InitialContext();

            // create EJB using factory from container
            java.lang.Object objref = ic.lookup("MyStatelesshello");

            System.out.println("Looked up home!!");

            StatelesshelloHome home =
                (StatelesshelloHome) PortableRemoteObject.narrow(
                    objref,
                    StatelesshelloHome.class);
            System.out.println("Narrowed home!!");

            Statelesshello hr = home.create();
            System.out.println("Got the EJB!!");

            // invoke method on the EJB
            System.out.println(hr.sayStatelesshello());
            System.out.println(
                "Client's sayStatelesshello() method succeeded\n");
            try {

                System.out.println(
                    "Client now getting a User Defined Exception");
                System.out.println(hr.getUserDefinedException());

            } catch (StatelesshelloException he) {
                System.out.println("Success!  Caught StatelesshelloException");
                System.out.println(
                    "Client's getUserDefinedException() method succeeded\n");

            } catch (java.rmi.ServerException se) {
                if (se.detail instanceof StatelesshelloException) {
                    System.out.println(
                        "Success!  Caught StatelesshelloException");
                    System.out.println(
                        "Client's getUserDefinedException() method succeeded\n");

                } else {
                    System.out.println("Failure!  Caught unasked for Exception");
		    System.exit(-1);
                }
            }
            System.out.println(
                "Client is now trying to remove the session bean\n");
            hr.remove();
        } catch (NamingException ne) {
	    if(testPositive) {
		System.out.println("Caught exception while initializing context : " +
				   ne.getMessage() + " \n");
		System.exit(-1);
	    } else {
		System.out.println("Recd exception as expected");
	    }
        } catch (Exception re) {
	    if(testPositive) {
		re.printStackTrace();
		System.out.println( "Session beans could not be removed by the client.\n");
		System.exit(-1);
	    } else {
		System.out.println("Recd exception as expected");
	    }
	}
        System.out.println(
            "Session bean was successfully removed by the client.\n");
    }
}
