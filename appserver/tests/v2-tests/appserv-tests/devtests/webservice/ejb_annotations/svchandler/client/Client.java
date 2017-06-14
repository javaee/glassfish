package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloEJBService;
import endpoint.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

       // @WebServiceRef
        //static HelloEJBService service;

        public static void main(String[] args) {
	    stat.addDescription("handler-chain-annotation");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("handler-chain-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port =new HelloEJBService().getHelloEJBPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello injectedValue") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("handler-chain-annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("handler-chain-annotation", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("handler-chain-annotation", stat.FAIL);
            }
       }
}

