/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.loadbalancing.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.jms.*;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.naming.SerialContext;
import com.sun.enterprise.util.ORBManager;


public class EnrollerClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) { 
        EnrollerClient client = new EnrollerClient(); 

        // run the tests
        client.runTestClient(args);   
    }

    public void runTestClient(String[] args) {
        try{
            stat.addDescription("Testing loadbalancing app.");
            test01(args);
            stat.printSummary("loadbalancingAppID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01(String[] args) {
        String enrollerString = "";
	String queueString = "";
        try {  	   
	    if (args.length != 0 && args[0].equals("standalone")) {
	        enrollerString = "ejb/MyEnroller";
		queueString = "jms/SampleQueue";
	    } else {
	        enrollerString = "java:comp/env/ejb/SimpleEnroller";
		queueString = "java:comp/env/jms/SampleQueue";
	    }
	    Properties env = new Properties();
	    env.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
	    org.omg.CORBA.ORB orb = ORBManager.getORB();
	    env.put("java.naming.corba.orb", orb);
	    // Initialize the Context with JNDI specific properties
	    InitialContext ctx = new InitialContext(env);
	    System.out.println("looking up ejb/MyStudent using com.sun.jndi.cosnaming.CNCtxFactory...");
	    Object obj = ctx.lookup("ejb/MyStudent");
	    System.out.println("Looked up ejb/MyStudent with CnCtxFactory...");
	    StudentHome sH = 
	      (StudentHome) PortableRemoteObject.narrow(obj, 
							StudentHome.class);
	    Student denise1 = sH.create("111", "Tiffany Moore");
	    System.out.println("Created student id 111 for Tiffany Moore");
	    
	    Context initial = new InitialContext();
	    System.out.println("Looking up MEJB...");
	    Object objref = initial.lookup("ejb/mgmt/MEJB"); 
	    System.out.println("Looked up ejb/mgmt/MEJB");
	    System.out.println("Looking up EJB REFs whose jndi name is specified as a corbaname: url ==>");
	    System.out.println("Creating new Context 1..");
           
	    System.out.println("Using Context 1, Looking up EJB using corbaname: url with global jndi name ==>");
	   
	    objref = initial.lookup("corbaname:iiop:localhost:3700#ejb/MyStudent");
	    System.out.println("Looked up corbaname:iiop:localhost:3700#ejb/MyStudent");
            
            StudentHome sHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);
            Student denise = sHome.create("823", "Denise Smith");


	    System.out.println("Using Context 1, looking up global jndi name ==>");	    
	    Object objRef = initial.lookup("ejb/MyCourse");
	    System.out.println("Looked up ejb/MyCourse");
	    CourseHome cHome = (CourseHome) 
	      PortableRemoteObject.narrow(objRef, 
					  CourseHome.class);	    
	    Course power = cHome.create("220", "Power J2EE Programming");    

	    objref = initial.lookup(enrollerString);
	    System.out.println("Looked up " + enrollerString);
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

	    System.out.println("Looking up JMS Resource Refs ==>");
	    System.out.println("Creating new Context 2..");
	    Context initial1 = new InitialContext();            	    
	    javax.jms.Queue queue = (javax.jms.Queue) initial1.lookup(queueString);
	    System.out.println("looked up " + queueString);

	    System.out.println("Creating new Context 3...");
	    Context initial2 = new InitialContext();
	    javax.jms.QueueConnectionFactory queueConnectionFactory = 
	      (QueueConnectionFactory)
	      initial2.lookup("jms/QCFactory");
	    System.out.println("Looked up jms/QCFactory");
	    
            stat.addStatus("load balancing", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("load balancing", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    } 
} 
