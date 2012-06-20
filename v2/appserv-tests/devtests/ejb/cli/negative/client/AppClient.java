package com.acme;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client will: 
 * <ul>
 * <li>Locates the remote interface of the enterprise bean
 * <li>Invokes business methods
 * </ul>
 */
public class AppClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) { 
        AppClient client = new AppClient(); 

        // run the tests
        client.runTestClient();   
    }

    public void runTestClient() {
        try{
            stat.addDescription("Testing ejb-cli");
            test01();
            stat.printSummary("test end");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/MyBean");
            MyBeanRemoteIntf mybean = 
                (MyBeanRemoteIntf) PortableRemoteObject.narrow(objref, 
                                                          MyBeanRemoteIntf.class);
            System.out.println("invocation result: "+ mybean.getCount(1));
            stat.addStatus("ejb-cli", stat.FAIL);
        } catch (Exception ex) {
            stat.addStatus("ejb-cli", stat.PASS);
            System.err.println("caught expected exception");
            ex.printStackTrace();
        }
    } 
} 
