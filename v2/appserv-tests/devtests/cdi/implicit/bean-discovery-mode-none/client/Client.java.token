

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("simple-ejb-implicit-cdi-bdm-none");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("simple-ejb-implicit-cdi-bdm-none");
    }

    public Client (String[] args) {
    }

    private static @EJB(mappedName="Sless") Sless sless;


    public void doTest() {

        try {
            InitialContext ic = new InitialContext();
            org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:comp/ORB");
            Sless sless = (Sless) ic.lookup("Sless");

            String response = null;

            try {
                response = sless.hello();
                stat.addStatus("invoking stateless", stat.FAIL);
            } catch (Exception expected) {
                stat.addStatus("invoking stateless", stat.PASS);
            }

            System.out.println("test complete");

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

    	return;
    }

}

