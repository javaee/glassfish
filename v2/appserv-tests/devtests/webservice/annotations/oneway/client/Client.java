package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloService;
import endpoint.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/Hello/HelloService?WSDL")
        static HelloService service;

        public static void main(String[] args) {
	    stat.addDescription("oneway-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("oneway-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    port.sayHello("Appserver Tester !");
                }
                stat.addStatus("Oneway-Annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("Oneway-Annotation", stat.FAIL);
            }
       }
}

