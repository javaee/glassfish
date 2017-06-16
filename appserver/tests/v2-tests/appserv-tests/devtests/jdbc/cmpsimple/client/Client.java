package com.sun.s1asdev.jdbc.cmpsimple.client;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.s1asdev.jdbc.cmpsimple.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args) {
       
 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "cmpsimple";
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/BlobTestBean");
            BlobTestHome bhome = (BlobTestHome)
                PortableRemoteObject.narrow(objref, BlobTestHome.class);

            System.out.println("START");

            BlobTest bean = bhome.create(new Integer(100), "FOO");
            System.out.println("Created: " +bean.getPrimaryKey());
            
            System.out.println("Testing new...");
            bean = bhome.findByPrimaryKey(new Integer(100));
            System.out.println(new String(bean.getName()));

            System.out.println("Testing old...");
            bean = bhome.findByPrimaryKey(new Integer(1));
            System.out.println(new String(bean.getName()));

	    stat.addStatus(testSuite + " test : ", stat.PASS);

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus(testSuite +  "test : ", stat.FAIL);
        }

	stat.printSummary();

    }
    
}
