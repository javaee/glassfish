package client;

import javax.xml.ws.WebServiceRef;
import javax.naming.Context;
import javax.naming.InitialContext;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@WebServiceRef(name="service/helloservice", wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL", type=endpoint.HelloImplService.class)
public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ws-dependency-no-injection");
            try {
	            Context ic = new InitialContext();

        	    service = (HelloImplService) ic.lookup("java:comp/env/service/helloservice");
	    } catch(Throwable t) {
		t.printStackTrace();
		System.out.println("Dependency lookup failed : " + t.getMessage());
                stat.addStatus("ws-dependency-no-injection", stat.FAIL);		
	    }
	    System.out.println("Service is " + service);
	    if (service!=null) {
	            Client client = new Client();
	            client.doTest(args);
	    }
	    stat.printSummary("ws-dependency-no-injection");
       }

       public void doTest(String[] args) {
            try {
                HelloImpl port = service.getHelloImplPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("ws-ejb-method-injection", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ws-dependency-no-injection", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ws-dependency-no-injection", stat.FAIL);
            }
       }
}

