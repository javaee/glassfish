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


public class NegativeRPAClient {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        NegativeRPAClient client = new NegativeRPAClient(args);
        client.doTest();
    }
    
    public NegativeRPAClient(String[] args) {
        //super(args);
    }
    
    public String doTest() {
        
	NegativeRPARemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        NegativeRPAHome home=null;
        String testId = "Sec::NegativeTest-Realm per app";
    	try{
            stat.addDescription("Security::NegativeTest - Realm per app");
	    ic = new InitialContext();
            // create EJB using factory from container 
            java.lang.Object objref = ic.lookup("negativeRPALoginBean");
		
	    System.err.println("Looked up home!!");
		
            home = (NegativeRPAHome)PortableRemoteObject.narrow(
					   objref, NegativeRPAHome.class);
	    System.err.println("Narrowed home!!");
				
            hr = home.create("LizHurley");
            System.out.println("Got the EJB!!");
            System.out.println ("Calling authorized method - addItem");
            hr.addItem("lipstick", 30);
            System.out.println("NegativeRPA:StatefulLoginBean Test Failed");
            res = "FAIL";
        }catch (Exception ex) {
            // should get a login exception
            //ex.printStackTrace();
            if(ex instanceof java.rmi.AccessException){
                System.out.println(" Got java.rmi.AccessException !! ");
                System.out.println("NegativeRPA:StatefulLoginBean Test Passed: Exception expected");            
                res="PASS";
                stat.addStatus(testId, stat.PASS);

            } else {
                System.out.println("NegativeRPA:StatefulLoginBean Test Failed");
                res = "FAIL";  
                stat.addStatus(testId, stat.FAIL);
            }
        } finally {
            stat.printSummary();
        }

        return res;
        
    }

    
    public final static String helloStr = "Hello NegativeRPA!!!";
}

