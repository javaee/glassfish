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
	    stat.addDescription("ejb-oneway-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ejb-oneway-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                for (int i=0;i<10;i++) {
                    port.sayHello("Appserver Tester !");
                }
                stat.addStatus("ejb-oneway-annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-oneway-annotation", stat.FAIL);
            }
       }
}

