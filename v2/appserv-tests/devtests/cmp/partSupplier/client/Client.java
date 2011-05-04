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
            stat.addDescription("partSupplier");

            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SPSession");
            Data.SPSessionHome home =
            (Data.SPSessionHome)PortableRemoteObject.narrow(objref,
            Data.SPSessionHome.class);

            Data.SPSession myspsession = home.create();

            myspsession.createPartsAndSuppliers();
            System.out.println("Created " + myspsession.checkAllParts() + " Parts.");
            System.out.println("Created " + myspsession.checkAllSuppliers() + " Suppliers.");

            System.out.println("Removing Part 200...");
            myspsession.removePart(new java.lang.Integer(200));

            System.out.println("Removing Supplier 145/145...");
            myspsession.removeSupplier(new java.lang.Integer(145), new java.lang.Integer(145));

            System.out.println("Left " + myspsession.checkAllParts() + " Parts.");
            System.out.println("Left " + myspsession.checkAllSuppliers() + " Suppliers.");
            stat.addStatus("ejbclient partSupplier", stat.PASS);

            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            stat.addStatus("ejbclient partSupplier", stat.FAIL);

        }
          stat.printSummary("partSupplier");

    }
    
}
