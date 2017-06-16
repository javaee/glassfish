package client;

import javax.xml.ws.WebServiceRef;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

	private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef static HttpTestService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = new HttpTestService().getHelloPort();
		HelloRequest req = new HelloRequest();
		req.setString("From Vijay ");
		HelloResponse resp = port.hello(req);
		if(resp.getString().indexOf("From Vijay") == -1) {
		    System.out.println("Unexpected greeting " + resp.getString());
		    stat.addStatus(args[0], stat.FAIL);
		}
		if(resp.getString().indexOf("Hello1") != -1) {
		    System.out.println("Unexpected greeting " + resp.getString());
		    stat.addStatus(args[0], stat.FAIL);
		}
		System.out.println(resp.getString());
                Hello1 port1 = service.getHello1Port();
		Hello1Request req1 = new Hello1Request();
		req1.setString("From Vijay ");
		Hello1Response resp1 = port1.hello1(req1);
		if(resp1.getString().indexOf("From Vijay") == -1) {
		    System.out.println("Unexpected greeting " + resp1.getString());
		    stat.addStatus(args[0], stat.FAIL);
		}
		if(resp1.getString().indexOf("Hello1") == -1) {
		    System.out.println("Unexpected greeting " + resp1.getString());
		    stat.addStatus(args[0], stat.FAIL);
		}
		System.out.println(resp1.getString());
		stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
		    stat.addStatus(args[0], stat.FAIL);
            }
       }
}

