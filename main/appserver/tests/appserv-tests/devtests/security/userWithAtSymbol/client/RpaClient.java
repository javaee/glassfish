/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package shopping;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.enterprise.security.LoginContext;
//import com.sun.enterprise.security.auth.login.common.LoginException;
import java.rmi.RemoteException;
import java.security.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class RpaClient {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        RpaClient client = new RpaClient(args);
        client.doTest();
    }
    
    public RpaClient(String[] args) {
        //super(args);
    }
    
    public String doTest() {
        
	RpaRemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        RpaHome home=null;
        String testId = "Sec::Username with @";
    	try{
            stat.addDescription("Security::Username with @");
	    ic = new InitialContext();
            // create EJB using factory from container 
            java.lang.Object objref = ic.lookup("rpaLoginBean");
		
	    System.err.println("Looked up home!!");
		
	    home = (RpaHome)PortableRemoteObject.narrow(
					   objref, RpaHome.class);
	    System.err.println("Narrowed home!!");
				
            hr = home.create("LizHurley");
            System.out.println("Got the EJB!!");

            // invoke 3 overloaded methods on the EJB
            System.out.println ("Calling authorized method - addItem");
            hr.addItem("lipstick", 30);
            hr.addItem("mascara", 40);
            hr.addItem("lipstick2", 50);
            hr.addItem("sandals",  200);
            System.out.println(hr.getTotalCost());
            hr.deleteItem("lipstick2");
            java.lang.String[] shoppingList = hr.getItems();
            System.out.println("Shopping list for LizHurley");
            for (int i=0; i<shoppingList.length; i++){
                System.out.println(shoppingList[i]);
            }
            System.out.println("Total Cost for Ms Hurley = "+
            hr.getTotalCost());
            stat.addStatus(testId, stat.PASS);
            System.out.println("Username with @:RpaLoginBean Test Passed");
        } catch(Exception re){
            re.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
            System.out.println("Username with @:RpaLoginBean Test Failed");
            System.exit(-1);
	} finally {
            stat.printSummary();
        }
        return res;
        
    }
}

