package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

	private final static String desc = "ws-ejb-port-method-injection";

	@WebServiceRef(HelloImplService.class)
	static void setPort(HelloImpl s) {
		port = s;
	}

        static HelloImpl port;

        public static void main(String[] args) {
	    stat.addDescription(desc);
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary(desc);
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(desc, stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus(desc, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(desc, stat.FAIL);
            }
       }
}

