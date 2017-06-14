package client;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.soap.*;

import javax.xml.ws.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @Addressing
        @WebServiceRef(name="service/MyService") static AddNumbersService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices13-addressing-appclient test");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices13-addressing-appclientID");
       }

       public void doTest(String[] args) {
            try {
                com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true;

                AddNumbersPortType port = service.getAddNumbersPort();
                ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:8080/webservices13-addressing-appclient/webservice/AddNumbersService?WSDL");
                int ret = port.addNumbers(100, 200);
		if(ret != 300) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("webservices13-addressing-appclient", stat.FAIL);
                    return;
                }
                stat.addStatus("webservices13-addressing-appclient", stat.PASS);
                System.out.println("Add result = " + ret);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("webservices13-addressing-appclient", stat.FAIL);
            }
       }
}

