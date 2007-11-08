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
	    stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("Simple-Annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("Simple-Annotation", stat.FAIL);
            }
       }
}

