/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    
    private static SimpleReporterAdapter stat =
	new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
	    stat.addDescription("copySemantics");

            Context initial = new InitialContext();
                        Object objref = initial.lookup("java:comp/env/ejb/RemoteA1");
            test.AHome ahome = (test.AHome)PortableRemoteObject.narrow(objref, test.AHome.class);

            test.A abean = ahome.create(new Integer(1), "A1", new java.util.Date(600000000000L), 
                new byte[]{'A', 'B', 'C'});

            abean.test();
	    stat.addStatus("ejbclient copySemantics", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient copySemantics", stat.FAIL);
        }

	  stat.printSummary("copySemantics");
    }
    
}
