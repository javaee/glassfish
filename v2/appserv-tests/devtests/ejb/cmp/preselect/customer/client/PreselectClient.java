/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.ejb.cmp.preselect.client;

import java.util.Iterator;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.ejb.cmp.preselect.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class PreselectClient {
    
    private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        try {
	    System.out.println("Starting preselect....");
	    System.out.println("Customer Bob has added 3 items to his shopping cart :");
	    System.out.println(" The items are : 1> Dark Chocolate $40, 2> Milk Chocolate $30, 3> White Chocolate $20");
            status.addDescription("Testing cmp preselect app.");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCustomer");

            CustomerHome home = 
	      (CustomerHome)PortableRemoteObject.narrow(objref, 
                                                        CustomerHome.class);
            CustomerRemote myCustomer = home.create("1","Bob");

	    System.out.println("Let us select all items that Bob bought whose price is below $100");
	    int items = myCustomer.getItemsForLess();

	    if (items == 0) {
	        System.out.println("No items available below $100 for this customer. ejbStore() was called before ejbSelect() and it modified the price of each item to $200");
		status.addStatus("cmp preselect:getSomeInfo", status.PASS);
	    } else {
	        System.out.println("Found " + items + " items with price less than 100. ejbStore was not called before ejbSelect. Hence the price of the items was not modified to $200");
	        status.addStatus("cmp preselect:getSomeInfo", status.FAIL);
	    }
 
            status.printSummary("preselectAppID");
            System.exit(0);
        } catch (Throwable ex) {
	    status.addStatus("cmp preselect:getSomeInfo", status.FAIL);
            System.err.println("Caught an exception in main: " + ex.toString());
            ex.printStackTrace();
        }
    }

}
