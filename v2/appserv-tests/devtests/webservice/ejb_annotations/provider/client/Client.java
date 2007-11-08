package client;

import javax.xml.ws.WebServiceRef;

import endpoint.jaxws.HelloImplService;
import endpoint.jaxws.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ejb-provider-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ejb-provider-annotation");
       }

       public void doTest(String[] args) {
            try {
                HelloImpl port = service.getHelloImpl();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ejb-provider-annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-provider-annotation", stat.FAIL);
            }
       }
}

