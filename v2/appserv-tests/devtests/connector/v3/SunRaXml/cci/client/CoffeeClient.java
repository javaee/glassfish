/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.connector.cci;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class CoffeeClient {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String testId = "J2EE Connectors 1.5 : Standalone adapter tests";
        try {

            if (args.length == 1) {
                testId = args[0];
            }

            System.err.println(testId + " : CoffeeClient started in main...");
            stat.addDescription("J2EE Connectors 1.5 : Standalone CCI adapter Tests");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCoffee");

            CoffeeRemoteHome home =
                    (CoffeeRemoteHome) PortableRemoteObject.narrow(objref,
                            CoffeeRemoteHome.class);

            CoffeeRemote coffee = home.create();

            int count = coffee.getCoffeeCount();
            System.err.println("Coffee count = " + count);

            System.err.println("Inserting 3 coffee entries...");
            coffee.insertCoffee("Mocha", 10);
            coffee.insertCoffee("Espresso", 20);
            coffee.insertCoffee("Kona", 30);

            int newCount = coffee.getCoffeeCount();
            System.err.println("Coffee count = " + newCount);
            if (count == (newCount - 3)) {
                stat.addStatus("Connector:cci Connector " + testId + " rar Test status:", stat.PASS);
            } else {
                stat.addStatus("Connector:cci Connector " + testId + " rar Test status:", stat.FAIL);
            }

            //print test summary
            stat.printSummary(testId);


        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            stat.addStatus("Connector:CCI Connector " + testId + " rar Test status:", stat.FAIL);
            ex.printStackTrace();
        }
    }


} 
