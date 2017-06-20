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
                        Object objref = initial.lookup("java:comp/env/ejb/RemoteA");
            cascadeDelete.AHome ahome =
            (cascadeDelete.AHome)PortableRemoteObject.narrow(objref,
            cascadeDelete.AHome.class);

            objref = initial.lookup("java:comp/env/ejb/RemoteB");
            cascadeDelete.BHome bhome =
            (cascadeDelete.BHome)PortableRemoteObject.narrow(objref,
            cascadeDelete.BHome.class);

            objref = initial.lookup("java:comp/env/ejb/RemoteC");
            cascadeDelete.CHome chome =
            (cascadeDelete.CHome)PortableRemoteObject.narrow(objref,
            cascadeDelete.CHome.class);

            objref = initial.lookup("java:comp/env/ejb/RemoteD");
            cascadeDelete.DHome dhome =
            (cascadeDelete.DHome)PortableRemoteObject.narrow(objref,
            cascadeDelete.DHome.class);


            cascadeDelete.A abean = ahome.create(new Integer(1), "A1");

            cascadeDelete.B bbean = bhome.create(new Integer(100), "B100");
            bbean = bhome.create(new Integer(200), "B200");

            cascadeDelete.C cbean = chome.create(new Integer(100), "C100");
            cbean = chome.create(new Integer(200), "C200");

            cascadeDelete.D dbean = dhome.create(new Integer(1000), "D1000");
            dbean = dhome.create(new Integer(1100), "D1100");
            dbean = dhome.create(new Integer(2000), "D2000");
            dbean = dhome.create(new Integer(2200), "D2200");

            abean.addAll();

            System.out.println("Created " + ahome.findAll().size() + " As.");
            System.out.println("Created " + bhome.findAll().size() + " Bs.");
            System.out.println("Created " + chome.findAll().size() + " Cs.");
            System.out.println("Created " + dhome.findAll().size() + " Ds.");

            System.out.println("Removing last C...");
            cbean.remove();

            System.out.println("Left " + ahome.findAll().size() + " As.");
            System.out.println("Left " + bhome.findAll().size() + " Bs.");
            System.out.println("Left " + chome.findAll().size() + " Cs.");
            System.out.println("Left " + dhome.findAll().size() + " Ds.");


            System.out.println("Removing A...");
            abean.remove();

            System.out.println("Left " + ahome.findAll().size() + " As.");
            System.out.println("Left " + bhome.findAll().size() + " Bs.");
            System.out.println("Left " + chome.findAll().size() + " Cs.");
            System.out.println("Left " + dhome.findAll().size() + " Ds.");

            stat.addStatus("ejbclient cascadeDelete", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient cascadeDelete", stat.FAIL);
        }
	  stat.printSummary("cascadeDelete");

    }
    
}
