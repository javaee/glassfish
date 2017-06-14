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
            stat.addDescription("unknownpk");

            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/UNPK2");
            unknownpk.A2Home a2home = (unknownpk.A2Home)PortableRemoteObject.narrow(objref, unknownpk.A2Home.class);

            unknownpk.A2 a2bean = a2home.create("A2");
            Object pk2 = a2bean.getPrimaryKey();
            System.out.println("CREATED 2.x WITH PK: " + pk2);
            System.out.println("CREATED 2.x: " + a2bean.getName());

            a2bean = a2home.findByPrimaryKey(pk2);
            System.out.println("FOUND 2.x: " + a2bean.getName());

            objref = initial.lookup("java:comp/env/ejb/UNPK1");
            unknownpk.A1Home a1home = (unknownpk.A1Home)PortableRemoteObject.narrow(objref, unknownpk.A1Home.class);

            unknownpk.A1 a1bean = a1home.create("A1");
            Object pk1 = a1bean.getPrimaryKey();
            System.out.println("CREATED 1.1 WITH PK: " + pk1);
            System.out.println("CREATED 1.1: " + a1bean.getName());

            a1bean = a1home.findByPrimaryKey(pk1);
            System.out.println("FOUND 1.1: " + a1bean.getName());

	    stat.addStatus("ejbclient unknownpk", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient unknownpk", stat.FAIL);
        }
          stat.printSummary("unknownpk");

    }
    
}
