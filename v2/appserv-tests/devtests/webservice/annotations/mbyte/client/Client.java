package client;

import javax.xml.ws.WebServiceRef;

import com.example.hello.HelloîService;
import com.example.hello.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/mbyte/HelloîService?WSDL")
        static HelloîService service;

        public static void main(String[] args) {
	    stat.addDescription("service-with-mbyte-char");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("service-with-mbyte-char");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                String ret = port.sayHello("Appserver Tester !" + args[0]);
		if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
		}
		if(ret.indexOf(args[0]) == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
		}
                System.out.println(ret);
                ret = port.sayDoubleHello("Appserver Tester !" + args[0]);
		if(ret.indexOf("WebSvcTest-Double-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
		}
		if(ret.indexOf(args[0]) == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(args[0], stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

