/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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


 
import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.*;

public class HelloClient
{
  static Hello helloImpl;

  public static void main(String args[])
    {
      try{
	  Properties p1 = new Properties();
	  //p1.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
	  //p1.put( "org.omg.CORBA.ORBInitialPort", "2510" );
	  p1.put("org.omg.CORBA.ORBClass", "com.inprise.vbroker.orb.ORB");
	  p1.put("org.omg.CORBA.ORBSingletonClass", "com.inprise.vbroker.orb.ORBSingleton");
	  org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( new String[]{"",""}, p1 );
	  
	  // get the root naming context
	  org.omg.CORBA.Object objRef = 
	    orb.resolve_initial_references("NameService");
	  // Use NamingContextExt instead of NamingContext. This is 
	  // part of the Interoperable naming Service.  
	  NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	  
	  // resolve the Object Reference in Naming
	  String name = "Hello";
	  helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
	  
	  System.out.println("Obtained a handle on server object: " + helloImpl);
	  System.out.println(helloImpl.sayHello());
	  helloImpl.shutdown();
	  
	  /*	  Properties p2 = new Properties();
	  p2.put( javax.naming.Context.INITIAL_CONTEXT_FACTORY, 
		  "com.sun.jndi.cosnaming.CNCtxFactory" );
	  p2.put("java.naming.corba.orb", orb);
	  
	  InitialContext ic = new InitialContext( p2);
	  
	  System.out.println("Done");
	  System.out.println("About to do lookup...");
	  
	  java.lang.Object o = ic.lookup("Hello" );
	  System.out.println("Completed lookup!!");
	  
	  HelloApp.Hello hello = (HelloApp.Hello) PortableRemoteObject.narrow( o, HelloApp.Hello.class );
	  
	  System.out.println(hello.sayHello());
	  System.out.println("Done");
	  
	  hello.shutdown();*/
	  

	} catch (Exception e) {
          System.out.println("ERROR : " + e) ;
	  e.printStackTrace(System.out);
	  }
    }

}
