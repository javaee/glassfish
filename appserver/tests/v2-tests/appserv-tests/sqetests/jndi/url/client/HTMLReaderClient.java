/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.jndi.url.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.jndi.url.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class HTMLReaderClient {

   private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {
       try {
           stat.addDescription("Testing HTMLReader");
           Context initial = new InitialContext();
           System.out.println("looking up objref");
           Object objref = initial.lookup("java:comp/env/ejb/SimpleHTMLReader");

           System.out.println("getting home...");
           HTMLReaderHome home = 
               (HTMLReaderHome)PortableRemoteObject.narrow(objref, 
                                            HTMLReaderHome.class);

           System.out.println("creating bean...");
           HTMLReader htmlReader = home.create();
           System.out.println("getting contents...");
           StringBuffer contents = htmlReader.getContents();
           System.out.println("The contents of the HTML page follows:\n");
           System.out.print(contents);
           stat.addStatus("HTMLReader Test", stat.PASS);
       } catch (Exception ex) {
           stat.addStatus("HTMLReader Test", stat.FAIL);
           System.err.println("Caught an unexpected exception!");
           System.out.println("check the url being accessed in sun-ejb-jar.xml.");
           ex.printStackTrace();
       }
       stat.printSummary("urlID");
       System.exit(0);
   } 
} 
