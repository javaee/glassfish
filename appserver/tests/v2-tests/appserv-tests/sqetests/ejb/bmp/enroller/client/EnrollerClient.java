/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.ejb.bmp.enroller.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client will: 
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 */
public class EnrollerClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) { 
        EnrollerClient client = new EnrollerClient(); 

        // run the tests
        client.runTestClient();   
    }

    public void runTestClient() {
        try{
            stat.addDescription("Testing bmp enroller app.");
            test01();
            stat.printSummary("enrollerAppID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleStudent");
            StudentHome sHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);

            Student denise = sHome.create("823", "Denise Smith");

            objref = initial.lookup("java:comp/env/ejb/SimpleCourse");
            CourseHome cHome = 
                (CourseHome) PortableRemoteObject.narrow(objref, 
                                                         CourseHome.class);

            Course power = cHome.create("220", "Power J2EE Programming");

            objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");
            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            enroller.enroll("823", "220");
            enroller.enroll("823", "333");
            enroller.enroll("823", "777");
            enroller.enroll("456", "777");
            enroller.enroll("388", "777");

            System.out.println(denise.getName() + ":");
            ArrayList courses = denise.getCourseIds();
            Iterator i = courses.iterator();
            while (i.hasNext()) {
                String courseId = (String)i.next();
                Course course = cHome.findByPrimaryKey(courseId);
                System.out.println(courseId + " " + course.getName());
            }
            System.out.println();
 
            Course intro = cHome.findByPrimaryKey("777");
            System.out.println(intro.getName() + ":");
            courses = intro.getStudentIds();
            i = courses.iterator();
            while (i.hasNext()) {
                String studentId = (String)i.next();
                Student student = sHome.findByPrimaryKey(studentId);
                System.out.println(studentId + " " + student.getName());
            }
          
            stat.addStatus("enroller bmp", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("enroller bmp", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    } 
} 
