package client;

import javax.xml.ws.WebServiceRef;

import javax.xml.ws.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(name="service/MyService") static AddNumbersService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                AddNumbersPortType port = service.getAddNumbersPortType();
                ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:8080/prepkged-svc-1/webservice/AddNumbersService?WSDL");
                int ret = port.addNumbers(100, 200);
		if(ret != 300) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("pre-pkged-appclient", stat.FAIL);
                    return;
                }
                System.out.println("Add result = " + ret);
                stat.addStatus("pre-pkged-appclient", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("pre-pkged-appclient", stat.FAIL);
            }
       }
}

