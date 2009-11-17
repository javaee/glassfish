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

public class ShoppingCartClient {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        ShoppingCartClient client = new ShoppingCartClient(args);
        client.doTest();
    }
    
    public ShoppingCartClient(String[] args) {
        //super(args);
    }
    
    public String doTest() {
        
	ShoppingCartRemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        ShoppingCartHome home=null;
        String testId = "Sec::Stateful Login Bean";
    	try {
            stat.addDescription("Security::Statefule Login Bean");
	    ic = new InitialContext();
            // create EJB using factory from container 
            java.lang.Object objref = ic.lookup("statefulLoginBean");
		
	    System.err.println("Looked up home!!");
		
	    home = (ShoppingCartHome)PortableRemoteObject.narrow(
					   objref, ShoppingCartHome.class);
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

            boolean canSaveQuote;
            try {
                hr.saveAsQuote();
                canSaveQuote = true;
            } catch(Exception ex) {
                canSaveQuote = false;
            }

            if (canSaveQuote) {
                stat.addStatus(testId, stat.FAIL);
                System.out.println("RealmPerApp:RpaLoginBean Test Failed");
            } else {
                stat.addStatus(testId, stat.PASS);
                System.out.println("RealmPerApp:RpaLoginBean Test Passed");
            }
        } catch(Exception re){
            re.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
            System.out.println("Shopping Cart:StatefulLoginBean Test Failed");
            System.exit(-1);
	} finally {
            stat.printSummary();
        }
        System.out.println("ShoppingCart:StatefulLoginBean Test Passed");
        return res;
        
    }

    
    public final static String helloStr = "Hello ShoppingCart!!!";
}

