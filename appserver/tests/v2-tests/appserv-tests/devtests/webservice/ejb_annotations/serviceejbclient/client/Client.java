package client;

import javax.xml.ws.WebServiceRef;

import ejb.GatewayImplService;
import ejb.GatewayImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

	private final static String desc = "ws-ejb-sevice-field-injection";

	@WebServiceRef(GatewayImplService.class)
        static GatewayImpl port;

        public static void main(String[] args) {
	    stat.addDescription(desc);
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary(desc);
       }

       public void doTest(String[] args) {
            try {
		System.out.println("Method injected reference test...");
                String ret = port.invokeMethod("Appserver Tester !");
		if(ret.indexOf("METHOD WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(desc, stat.FAIL);
                    return;
		}
                System.out.println("Server returned " + ret + " : PASSED");
		System.out.println("Field injected reference test...");
                ret = port.invokeField("Appserver Tester !");
		if(ret.indexOf("FIELD WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(desc, stat.FAIL);
                    return;
		}
                System.out.println("Server returned " + ret + " : PASSED");
		System.out.println("Dependency jndi looup reference test...");
                ret = port.invokeDependency("Appserver Tester !");
		if(ret.indexOf("JNDI WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(desc, stat.FAIL);
                    return;
		}
                System.out.println("Server returned " + ret + " : PASSED");
                stat.addStatus(desc, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(desc, stat.FAIL);
            }
       }
}

