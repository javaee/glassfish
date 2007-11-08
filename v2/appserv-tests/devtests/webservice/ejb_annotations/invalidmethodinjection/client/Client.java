package client;

import javax.xml.ws.WebServiceRef;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
	static void setService(HelloImplService s) {
	
		System.out.println("Injection sucessful with "+s.getClass().toString());
		service = s;
	}

	@WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
	static int setFoo(HelloImplService s) {
		service1 = s;
		return 0;
	}

	@WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
	static void myService(String foo, HelloImplService s) {
		service2 = s;
	}

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
	void setMyService(HelloImplService s) {
	
		service3 = s;
	}


        static HelloImplService service1=null;
        static HelloImplService service2=null;
        static HelloImplService service3=null;

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
        HelloImplService service4=null;

        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ws-ejb-invalidmethodinjection");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ws-ejb-invalidmethodinjection");
       }

       public void doTest(String[] args) {
            try {
		if (service1!=null || service2!=null || service3!=null) {
		    System.out.println("Failed : invalid injection method got injected !");
                    stat.addStatus("ws-ejb-invalidmethodinjection", stat.FAIL);
                } else {
		    System.out.println("Success : invalid references were not injected");
 	        }
                HelloImpl port = service.getHelloImplPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("ws-ejb-invalidmethodinjection", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("ws-ejb-invalidmethodinjection", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ws-ejb-invalidmethodinjection", stat.FAIL);
            }
       }
}

