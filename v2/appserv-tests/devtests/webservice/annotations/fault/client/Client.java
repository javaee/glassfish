package client;

import javax.xml.ws.WebServiceRef;

import com.example.hello.MyService;
import com.example.hello.MyHello;
import com.example.hello.MyException_Exception;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/fault/webservice/HelloService?WSDL")
        static MyService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-fault");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-fault");
       }

       public void doTest(String[] args) {
            MyHello port = null;
            try {
System.out.println("Service" + service);
                port = service.getMyHelloPort();
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
            boolean gotMyEx = false;
            try {
                port.sayHello(null);
            } catch(MyException_Exception mex) {
                System.out.println("Got MyException as expected");
                gotMyEx = true;
            }
            if(!gotMyEx) {
                System.out.println("Did not get MyException thro SOAP fault");
                stat.addStatus(args[0], stat.FAIL);
                return;
            }
            try {
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
                stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

