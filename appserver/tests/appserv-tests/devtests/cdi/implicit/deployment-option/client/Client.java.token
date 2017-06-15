

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("simple-ejb-implicit-cdi-deployment-opt");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("simple-ejb-implicit-cdi-deployment-opt");
    }

    public Client (String[] args) {
    }

    private static @EJB(mappedName="Sless") Sless sless;

    public void doTest() {

        try {

            System.out.println("Creating InitialContext()");
            InitialContext ic = new InitialContext();
            org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:comp/ORB");
            Sless sless = (Sless) ic.lookup("Sless");

            String response = null;

            response = sless.hello();
            testResponse("invoking stateless", response);

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

    	return;
    }

    private void testResponse(String testDescription, String response){
        // Expecting a null response because the injection should fail since implicit bean discovery
        // is disabled by the deployment property implicitCdiEnabled=false
        stat.addStatus(testDescription, (response == null ? stat.PASS : stat.FAIL));
    }

}

