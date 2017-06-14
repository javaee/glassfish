/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package jaxr;


import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import jaxr.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;

public class JaxrClient {

    String company = "Sun";
    String url = null;
    String ctxFactory = null;
    String jndiName = null;
    public static void main (String[] args) {
        JaxrClient client = new JaxrClient(args);
        client.doTest();
    }
    
    public JaxrClient (String[] args) {
             if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
                jndiName = args[2];
            }
    }

    public String doTest() {
        
        String res = "fail";
        
    	try {          
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                InitialContext context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                System.out.println("*****"+jndiName);
                java.lang.Object obj = context.lookup(jndiName);
                // create EJB using factory from container 
                //java.lang.Object objref = ic.lookup("MyJaxr");

                System.out.println("Looked up home!!");

                JaxrHome  home = (JaxrHome)PortableRemoteObject.narrow(
			                     obj, JaxrHome.class);
                System.out.println("Narrowed home!!");

                JaxrRemote hr = home.create();
                System.out.println("Got the EJB!!");

                // invoke method on the EJB
                System.out.println (" Looking up company information for "+company); 
                System.out.println(hr.getCompanyInformation(company));
                hr.remove();
	} catch(NamingException ne){
            System.out.println("Caught exception while initializing context.\n");
            ne.printStackTrace();
	    System.out.println (" Test Failed !"); 
            return res;
	} catch(Exception re) {
            re.printStackTrace();
	    System.out.println (" Test Failed !"); 
            return res;
	} 
        res = "pass";
	System.out.println (" Test Passed !"); 
        return res;
        
    }
    
}

