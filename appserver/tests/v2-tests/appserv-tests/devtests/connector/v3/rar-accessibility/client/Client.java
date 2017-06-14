package com.sun.s1asdev.connector.rar_accessibility_test.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.connector.rar_accessibility_test.ejb.SimpleSession;
import com.sun.s1asdev.connector.rar_accessibility_test.ejb.SimpleSessionHome;

import javax.naming.InitialContext;

public class Client {

    public static void main(String[] args)
            throws Exception {

        int expectedCount = Integer.parseInt(args[0]);
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "rar-accesibility";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);

        stat.addDescription("Running rar_accessibility connector test ");
        SimpleSession bean = simpleSessionHome.create();

            try{
                if(bean.test1(expectedCount)){
                    stat.addStatus(testSuite + " test :  ", stat.PASS);
                }else{
                    stat.addStatus(testSuite + " test :  ", stat.FAIL);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        stat.printSummary();
    }
}
