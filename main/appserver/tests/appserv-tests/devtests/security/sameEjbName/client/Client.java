/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.sameEjbName.client;

import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::SameEjbName test ";
    private static @EJB com.sun.s1asdev.security.sameEjbName.ejb.Hello hello1;
    private static @EJB com.sun.s1asdev.security.sameEjbName.ejb2.Hello hello2;

    public static void main(String[] args) {
        stat.addDescription("security-sameEjbName");
        String description = null;
        try {
            description = testSuite + " ejb1: rolesAllowed1";
            hello1.rolesAllowed1("Sun");
            stat.addStatus(description, stat.PASS);  

            try {
                description = testSuite + " ejb1: rolesAllowed2";
                hello1.rolesAllowed2("Sun");
                stat.addStatus(description, stat.FAIL);  
            } catch(Exception e) {
                System.out.println("Expected failure: " + e);
                stat.addStatus(description, stat.PASS);  
            }

            try {
                description = testSuite + " ejb2: rolesAllowed1";
                hello2.rolesAllowed1("Java");
                stat.addStatus(description, stat.FAIL);  
            } catch(Exception e) {
                System.out.println("Expected failure: " + e);
                stat.addStatus(description, stat.PASS);  
            }

            description = testSuite + " ejb2: rolesAllowed2";
            hello2.rolesAllowed2("Java");
            stat.addStatus(description, stat.PASS);  
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        stat.printSummary("security-sameEjbName");
    }
}
