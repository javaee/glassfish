package client;

import javax.xml.ws.WebServiceRef;
import javax.naming.Context;
import javax.naming.InitialContext;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@WebServiceRef(name="service/helloport", wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL", type=endpoint.HelloImpl.class, value=HelloImplService.class)
public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        static HelloImpl port;

        public static void main(String[] args) {
	    stat.addDescription("ws-portdependency-no-injection");
            try {
	            Context ic = new InitialContext();

        	    port = (HelloImpl) ic.lookup("java:comp/env/service/helloport");
	    } catch(Throwable t) {
		t.printStackTrace();
		System.out.println("Dependency lookup failed : " + t.getMessage());
                stat.addStatus("ws-dependency-no-injection", stat.FAIL);		
	    }
	    System.out.println("Port is " + port);
	    if (port!=null) {
	            Client client = new Client();
	            client.doTest(args);
	    }
	    stat.printSummary("ws-portdependency-no-injection");
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("ws-portdependency-no-injection", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ws-portdependency-no-injection", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ws-portdependency-no-injection", stat.FAIL);
            }
       }
}

