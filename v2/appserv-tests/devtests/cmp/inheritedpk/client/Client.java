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
	    stat.addDescription("inheritedpk");

            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/PKA");
            pkvalidation.AHome ahome = (pkvalidation.AHome)PortableRemoteObject.narrow(objref, pkvalidation.AHome.class);

            pkvalidation.A abean = ahome.create(1, "A1", 2000.0);
            pkvalidation.APK pk1 = (pkvalidation.APK)abean.getPrimaryKey();
            System.out.println("CREATED A WITH PK: " + pk1.id);
            System.out.println("CREATED A WITH LASTNAME: " + abean.getLastname());

            pkvalidation.APK pk = new pkvalidation.APK();
            pk.id = 1;
            abean = ahome.findByPrimaryKey(pk);
            System.out.println("FOUND: " + abean.getLastname());

            objref = initial.lookup("java:comp/env/ejb/PKB");
            pkvalidation.BHome bhome = (pkvalidation.BHome)PortableRemoteObject.narrow(objref, pkvalidation.BHome.class);

            java.sql.Date d = new java.sql.Date(System.currentTimeMillis());

            pkvalidation.B bbean = bhome.create(d, "B1");
            System.out.println("CREATED B WITH PK: " + d);

            objref = initial.lookup("java:comp/env/ejb/PKC");
            pkvalidation.CHome chome = (pkvalidation.CHome)PortableRemoteObject.narrow(objref, pkvalidation.CHome.class);

            pkvalidation.C cbean = chome.create(1, "C1");
            pkvalidation.CPK pkc = (pkvalidation.CPK)cbean.getPrimaryKey();
            System.out.println("CREATED C WITH PK: " + pkc.id);
            System.out.println("CREATED C WITH NAME: " + cbean.getName());

            pkvalidation.CPK cpk = new pkvalidation.CPK();
            cpk.id = 1;
            cbean = chome.findByPrimaryKey(cpk);
            System.out.println("FOUND C: " + cbean.getName());

	    stat.addStatus("ejbclient inheritedpk", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient inheritedpk", stat.FAIL);
        }
            stat.printSummary("inheritedpk");
    }
    
}
