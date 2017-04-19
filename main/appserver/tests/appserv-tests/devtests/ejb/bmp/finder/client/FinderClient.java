/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.bmp.finder.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1asdev.ejb.bmp.finder.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client will: 
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 */
public class FinderClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) { 
        FinderClient client = new FinderClient(); 

        // run the tests
        client.runTestClient();   
    }

    public void runTestClient() {
        try{
            stat.addDescription("Testing Bmp FinderClient app.");
            test01();
            test02();
            stat.printSummary("Summary FinderClient");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCourse");
            CourseHome cHome = 
                (CourseHome) PortableRemoteObject.narrow(objref, 
                                                         CourseHome.class);
            objref = initial.lookup("java:comp/env/ejb/SimpleStudent");
            StudentHome sHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);

            Course intro = cHome.findByPrimaryKey("777");
            System.out.println("** Found course for: 777");
            stat.addStatus("Bmp-Finder FindCourse", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("Bmp-Finder FindCourse", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    } 

    private void test02() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCourse");
            CourseHome cHome = 
                (CourseHome) PortableRemoteObject.narrow(objref, 
                                                         CourseHome.class);

            ArrayList list = cHome.findAllCourses();
            System.out.println("** Found " + list.size() + " courses");
            stat.addStatus("Bmp-Finder FindAllCourse", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("Bmp-Finder FindAllCourse", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    } 

} 
