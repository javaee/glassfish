package client;

import java.util.Map;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/HelloImplService/HelloImpl?WSDL")
        static HelloImplService service;

        public static void main(String[] args) {
            String username=null;
            String password=null;
            boolean successExpected=true;
            String description;
	    if (args.length>1) {
                username=args[0];
                password=args[1];
		description="webservices-ejb-rolesAllowed-annotation-positive";
                if (args.length>2) {
                    description="webservices-ejb-rolesAllowed-annotation-negative-2";
                    successExpected = !(args[2].equalsIgnoreCase("FAILURE"));
                } 
            } else {
                successExpected = false;
		description="webservices-ejb-rolesAllowed-annotation-negative";
	    }
	    stat.addDescription(description);
            Client client = new Client();
            client.doTest(description, username, password, successExpected);
	    stat.printSummary(description);
       }

       public void doTest(String desc, String username, String password, boolean successExpected) {
           
            try {
                HelloImpl port = service.getHelloImplPort();
                if (username!=null && password!=null) {
                    BindingProvider bd = (BindingProvider) port;
                    Map<String, Object> requestContext = bd.getRequestContext();
                    requestContext.put("javax.xml.ws.security.auth.username",username);
                    requestContext.put("javax.xml.ws.security.auth.password",password);
                }
                
                // @PermitAll invocation, it should always work
                try {
                    String ret = port.permitAll("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(desc, stat.FAIL);
                        return;
                    }
                    System.out.println("@PermitAll method invocation passed - good...");
                } catch(Exception e) {
                    if (successExpected) {
                        System.out.println("@PermitAll method invocation failed - TEST FAILED");
                        stat.addStatus(desc, stat.FAIL);
                    } else {
                        System.out.println("@PermitAll method invocation failed - good...");                        
                    }
                }
                
                // @DenyAll invocation, it should always faile
                try {
                    String ret = port.denyAll("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") != -1) {
                        System.out.println("@DenyAll Method invocation succeeded, should have failed - TEST FAILED ");
                        stat.addStatus(desc, stat.FAIL);
                        return;
                    }
                } catch(Exception e) {
                    System.out.println("@DenyAll method invocation failed - good...");
                }
                
                // role based invocation
                String ret = port.roleBased("Appserver Tester !");
                if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(desc, stat.FAIL);
                    return;
                }
                System.out.println(ret);
		if (successExpected)
	                stat.addStatus(desc, stat.PASS);
		else 
	                stat.addStatus(desc, stat.FAIL);
                
            } catch(Throwable t) {
		if (successExpected) {
	                t.printStackTrace();
	                stat.addStatus(desc, stat.FAIL);
		} else {
			System.out.println("Got expected failure " + t.getMessage()); 
	                stat.addStatus(desc, stat.PASS);
		}
            }
       }
}

