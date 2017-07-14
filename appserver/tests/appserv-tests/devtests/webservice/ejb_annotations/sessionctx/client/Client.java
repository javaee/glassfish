package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://HTTP_HOST:HTTP_PORT/HelloImplService/HelloImpl?WSDL")
        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-ejb-sessionctx");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-ejb-sessionctx");
       }

       public void doTest(String[] args) {
            try {
                HelloImpl port = service.getHelloImplPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello ANONYMOUS") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("ejb-sessionctx", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ejb-sessionctx", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-sessionctx", stat.FAIL);
            }
       }
}

