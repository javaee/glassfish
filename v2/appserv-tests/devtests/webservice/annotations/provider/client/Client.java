package client;

import javax.xml.ws.WebServiceRef;

import endpoint.jaxws.HelloImplService;
import endpoint.jaxws.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");


        public static void main(String[] args) {
	    stat.addDescription("servlet-provider-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("servlet-provider-annotation");
       }

       public void doTest(String[] args) {
            try {
                HelloImpl port = new HelloImplService().getHelloImpl();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("servlet-provider-annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("servlet-provider-annotation", stat.FAIL);
            }
       }
}

