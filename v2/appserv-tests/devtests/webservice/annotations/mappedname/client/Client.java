package client;

import javax.xml.ws.WebServiceRef;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

   /*     private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(name="ignoredName", mappedName="MyMappedName", wsdlLocation="http://localhost:8080/Hello/HelloService?WSDL")
        static HelloService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-mapped-name");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-mapped-name");
       }

       public void doTest(String[] args) {
	    boolean gotEx = false;
            try {
		javax.naming.InitialContext ic = new javax.naming.InitialContext();
		Object res = ic.lookup("java:comp/env/ignoredName");
	    } catch(Exception ex) {
		System.out.println("Caught Expected exception - " + ex.getMessage());
		gotEx = true;
	    } 
	    if(!gotEx) {
		System.out.println("Mapped name not mapped as expected");
                stat.addStatus("Simple-mapped-name", stat.FAIL);
		return;
	    }
            try {
		javax.naming.InitialContext ic = new javax.naming.InitialContext();
		Object res = ic.lookup("java:comp/env/MyMappedName");
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("Simple-mapped-name", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("Simple-mapped-name", stat.FAIL);
            }
       }
*/
}

