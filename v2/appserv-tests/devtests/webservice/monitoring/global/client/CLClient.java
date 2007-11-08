/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

import java.net.URL;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;
        
/**
 * Command line client for the global monitoring web service
 *
 * @author Jerome Dochez
 */
public class CLClient {
    
    public static void main(String[] args) {
        CLClient client = new CLClient();
        client.doTest(args);
    }
    
    /** Creates a new instance of CLClient */
    public CLClient() {
    }
    
    private void doTest(String[] args) {
        try {
            System.out.println("Running in " + System.getProperty("java.version"));
            ServiceFactory factory = ServiceFactory.newInstance();
            GlobalMonitoring_Impl service = new GlobalMonitoring_Impl();
            System.out.println("Obtained Service " + service);
            WebServiceEngine intf = (WebServiceEngine) service.getPort(
                    new QName("urn:SunAppServerMonitoring","WebServiceEnginePort"),
                    WebServiceEngine.class);
            if (intf!=null) {
                System.out.println("There are " + intf.getEndpointsCount() + " endpoints available");
                int endpointsCount = intf.getEndpointsCount();
                for (int i=0;i<endpointsCount;i++) {
                    String selector = intf.getEndpointsSelector(i);
                    System.out.println("Web Service " + i + " URL is " + selector);
                }
            }
            int tracesCount = intf.getTraceCount();
            System.out.println("Traces = " + tracesCount);
            
                   
            for (int i=0;i<tracesCount;i++) {
                InvocationTrace trace = intf.getTrace(i);
                System.out.println("=====Trace " + i + " request " + trace.getRequest());
                System.out.println("=====Trace " + i + " response" + trace.getResponse());
                
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
