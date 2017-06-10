/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.net.URL;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import test.webservice.WebServiceTest;

/**
 *
 * @author dochez
 */

public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");    

    public static void main(String[] args) {
        stat.addDescription("webservices-web-stubs-properties");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-web-stubs-properties");        
    }

    public void doTest(String[] args) {
        try {
            dynamic(args);
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.PASS);        
        } catch(Exception e) {
            System.out.println("Failure " + e.getMessage());
            e.printStackTrace();
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.FAIL);                    
        }
    }
 
    public void dynamic(String[] args) throws Exception {
        String endpoint = args[0];
        System.out.println("Invoking dynamic proxies with endpoint at " + endpoint);
        URL wsdlURL = new URL(endpoint+"?WSDL");
        ServiceFactory factory = ServiceFactory.newInstance();
        Service service = factory.createService(wsdlURL, 
            new QName("urn:WebServiceTest","WebServiceServletTest"));
        System.out.println("Obtained Service");
        WebServiceTest intf = (WebServiceTest) service.getPort(
            new QName("urn:WebServiceTest","WebServiceTestPort"),          
            WebServiceTest.class);
        String[] params = new String[1];
        params[0] = " from client";       
        System.out.println(intf.doTest(params));
    }
    

}
