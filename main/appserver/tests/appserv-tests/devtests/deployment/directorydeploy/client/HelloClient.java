/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
