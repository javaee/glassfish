package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-ejb-nointf-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-ejb-nointf-annotation");
       }

       public void doTest(String[] args) {
            try {
                HelloImpl port = service.getHelloImplPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ejb-nointf-annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-nointf-annotation", stat.FAIL);
            }
       }
}

