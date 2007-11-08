package client;

import javax.xml.ws.WebServiceRef;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class JAXWSClient {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/server/ejb/hello";

        @WebServiceRef
        static HelloEJBService service;

        public static void main(String[] args) {
	    stat.addDescription(testId);
            JAXWSClient client = new JAXWSClient();
            client.doTest(args);
	    stat.printSummary(testId);
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(testId, stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus(testId, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
       }
}

