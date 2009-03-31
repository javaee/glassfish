package com.sun.s1asdev.ejb.ejb30.clientview.core.client;

import java.io.*;
import java.util.*;
import javax.naming.InitialContext;
import javax.ejb.*;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.ejb30.clientview.core.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    @EJB private static Hello hr;
    @EJB private static BmpRemoteHome bmpRemoteHome;
    
    private static SfulRemoteHome sfulRemoteHome;

    @EJB public static void setStatefulRemoteHome(SfulRemoteHome srh) {
        sfulRemoteHome = srh;
    }

    private static SlessRemoteHome slessRemoteHome;

    @EJB(beanName="SlessBean") 
    private static void setStatelessRemoteHome(SlessRemoteHome srh) {
        slessRemoteHome = srh;
    }

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-clientview-core");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-clientview-coreID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {

	    if( hr == null ) {

		System.out.println("In stand-alone mode");
		InitialContext ic = new InitialContext();
		hr = (Hello) ic.lookup("ejb/ejb_ejb30_clientview_core_CoreApp");
		bmpRemoteHome = (BmpRemoteHome) ic.lookup("ejb/ejb_ejb30_clientview_core_Bmp");
		sfulRemoteHome = (SfulRemoteHome) ic.lookup("ejb/ejb_ejb30_clientview_core_Sful");
		slessRemoteHome = (SlessRemoteHome) ic.lookup("ejb/ejb_ejb30_clientview_core_Sless");

	    }


            System.out.println("testing injected BmpRemoteHome");
            BmpRemote bmpRemote = bmpRemoteHome.create("client1");
            bmpRemote = bmpRemoteHome.findByPrimaryKey("client1");

            EJBMetaData md = bmpRemoteHome.getEJBMetaData();
	    System.out.println("metadata = " + md);
            
            System.out.println("testing injected SlessRemoteHome");
            SlessRemote slessRemote = slessRemoteHome.create();
            slessRemote.required();

            System.out.println("testing injected SfulRemoteHome");
            SfulRemote sfulRemote = sfulRemoteHome.createSful();
            sfulRemote.required();
            
            System.out.println("testing Remote 3.0 Hello intf");
            hr.testPassByRef();

	    // invoke method on the EJB
	    doProxyTest(hr);

            testExceptions(hr);

            hr.shutdown();

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

    private void testExceptions(Hello h) throws Exception {

        try {
            h.throwException();
        } catch(Exception e) {
            if( e.getClass() == Exception.class ) {
                System.out.println("Successfully caught exception " + 
                                   e.getClass() + " " + e.getMessage());
            } else {
                throw e;
            }
        }

        try {
            h.throwAppException1();
            throw new Exception("didn't get exception for testException2");
        } catch(javax.ejb.FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            h.throwAppException2();
            throw new Exception("didn't get exception for testException3");
        } catch(javax.ejb.FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

    }

    private void testNotImplemented(Common c) {
        try {
            c.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void testNotImplemented(CommonRemote cr) {
        try {
            cr.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void doProxyTest(Hello hr) 
	throws Exception
    {
	System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL);
	runTests(Common.STATEFUL, hr);

	System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL);
	runTests(Common.STATELESS, hr);

	System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
	hr.warmup(Common.BMP);
	runTests(Common.BMP, hr);
    }

    private void runTests(int type, Hello hr)
	throws Exception
    {
      
        hr.notSupported(type, true);
        hr.notSupported(type, false);
        hr.supports(type, true);
	hr.supports(type, false);
        hr.required(type, true);
	hr.required(type, false);
        hr.requiresNew(type, true);
	hr.requiresNew(type, false);
        hr.mandatory(type, true);
	hr.never(type, false);
    }
}

