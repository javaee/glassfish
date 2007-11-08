package client;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPEjbConsumer {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

	private static String testId = "jbi-serviceengine/server/ejb/hello";
        @WebServiceRef
        static HelloEJBService service;

        public static void main(String[] args) {
	    stat.addDescription(testId);
            SOAPEjbConsumer client = new SOAPEjbConsumer();
            client.doTest(args);
	    stat.printSummary(testId);
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                // Get Stub
                BindingProvider stub = (BindingProvider)port;
                //String endpointURI ="http://localhost:8080/soap/ejb/noname/helloendpoint";
                String endpointURI ="http://localhost:12015/HelloEJBPort";
                stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                      endpointURI);
                System.out.println(" After setting endpoint address URI");
                String ret = port.sayHello("Appserver Tester !");
		if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(testId, stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus(testId, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
       }
}

