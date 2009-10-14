package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.Management;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	try {

	    // Ensure that MEJB is registered under all three of its JNDI names
	    System.out.println("Looking up MEJB Homes");
	    Object mh1Obj = new InitialContext().lookup("ejb/mgmt/MEJB");
	    Object mh2Obj = new InitialContext().lookup("java:global/mejb/MEJBBean");
	    Object mh3Obj = new InitialContext().lookup("java:global/mejb/MEJBBean!javax.management.j2ee.ManagementHome");

	    System.out.println("mejb home obj 1 = " + mh1Obj);
	    System.out.println("mejb home obj 2 = " + mh2Obj);
	    System.out.println("mejb home obj 3 = " + mh3Obj);

	    /**  Commenting out MEJB access now that the EJB is protected. 
	    ManagementHome mh = (ManagementHome) PortableRemoteObject.narrow(mh1Obj, ManagementHome.class);
	    System.out.println("mejb home 1 = " + mh);
	    Management m1 = mh.create();
	    System.out.println("mejb obj 1 = " + m1);

	    ManagementHome mh2 = (ManagementHome) PortableRemoteObject.narrow(mh2Obj, ManagementHome.class);
	    System.out.println("mejb home 2 = " + mh2);
	    Management m2 = mh2.create();
	    System.out.println("mejb obj 2 = " + m2);


	    System.out.println("mejb home obj 3 = " + mh3Obj);
	    ManagementHome mh3 = (ManagementHome) PortableRemoteObject.narrow(mh3Obj, ManagementHome.class);

	    Management m3 = mh3.create();
	    System.out.println("mejb obj 3 = " + m3);

	    **/

	    Hello hello = (Hello) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");


	    System.out.println("Singleton says : " + hello.hello());

	    try {
		hello.testError();
		throw new RuntimeException("Expected EJBException");
	    } catch(EJBException e) {
		System.out.println("Got expected EJBException after java.lang.Error thrown from ejb");
	    }

	     stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
