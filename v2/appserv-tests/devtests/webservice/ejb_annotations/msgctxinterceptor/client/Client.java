package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloEJBService;
import endpoint.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef
        static HelloEJBService service;

        public static void main(String[] args) {
	    stat.addDescription("msgctxt-invocationhandlerctxt");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("msgctxt-invocationhandlerctxt");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                String ret = port.sayHello("Appserver Tester !");
		if(ret == null) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("msgctxt-invocationhandlerctxt", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("msgctxt-invocationhandlerctxt", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("msgctxt-invocationhandlerctxt", stat.FAIL);
            }
       }
}

