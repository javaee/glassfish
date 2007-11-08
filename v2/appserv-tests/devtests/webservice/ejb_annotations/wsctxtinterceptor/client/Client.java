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
	    stat.addDescription("wsctxt-invocationhandlerctxt");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("wsctxt-invocationhandlerctxt");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                String ret = port.sayHello("Appserver Tester !");
		if(ret.indexOf("1234") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
		}
		if(ret.indexOf("45") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
		}
		if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("wsctxt-invocationhandlerctxt", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
            }
       }
}

