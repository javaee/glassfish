package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

	@WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
	static void setService(HelloImplService s) {
		service = s;
	}

        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ws-ejb-method-injection");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ws-ejb-method-injection");
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
                stat.addStatus("ws-ejb-method-injection", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ws-ejb-method-injection", stat.FAIL);
            }
       }
}

