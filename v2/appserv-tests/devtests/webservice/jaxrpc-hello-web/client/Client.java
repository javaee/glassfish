/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package client;

/*import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import helloservice.*;
import javax.xml.rpc.Service;
import javax.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

*/
public class Client {

 /*   private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("jaxrpc-hello-web-client");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("jaxrpc-hello-web-client");
    }
    
    public void doTest(String[] args) {
    	try {
            
            String targetEndpointAddress = args[0];
            Context ic = new InitialContext();

            MyHelloService myHelloService =
                (MyHelloService) ic.lookup(
                    "java:comp/env/service/helloservice");
 
           HelloIF helloPort = myHelloService.getHelloIFPort();
            
            
            ((Stub)helloPort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                    targetEndpointAddress);
            
            System.out.println("endpoint address = " + targetEndpointAddress);
            System.out.println("Invocation returned " + helloPort.sayHello("All"));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("jaxrpc-hello-web-client main", stat.FAIL);
            System.out.println("CAUGHT UNEXPECTED EXCEPTION: " + ex.getMessage());
        }

        stat.addStatus("jaxrpc-hello-web-client main", stat.PASS);
    }
*/
}
