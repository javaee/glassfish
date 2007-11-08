/*
 * EndpointMonitorRegistry.java
 *
 * Created on March 16, 2005, 9:46 AM
 */

package com.sun.enterprise.tools.wsmonitoring;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.EndpointLifecycleListener;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.deployment.WebServiceEndpoint;

/**
 *
 * @author dochez
 */
public class EndpointMonitorRegistry implements EndpointLifecycleListener {
    
    static EndpointMonitorRegistry instance;
    Map<String, EndpointMonitor> monitoredEndpoints;
    
    
    /** Creates a new instance of EndpointMonitorRegistry */
    EndpointMonitorRegistry() {
    }
    
    public static EndpointMonitorRegistry getInstance() {
        if (instance==null) {
            instance = new EndpointMonitorRegistry();
            instance.init();
        }
        return instance;
    }
    
    private void init() {
        monitoredEndpoints = new HashMap<String, EndpointMonitor>();
        WebServiceEngine engine = WebServiceEngineFactory.getInstance().getEngine();
        Iterator<Endpoint> createdEndpoints = engine.getEndpoints();
        while (createdEndpoints.hasNext()) {
            Endpoint endpoint = createdEndpoints.next();
            endpointAdded(endpoint);
        }
        engine.addLifecycleListener(this);
    }
    
    /**
     * Notification of a new Web Service endpoint installation in the 
     * appserver. 
     * @param endpoint endpoint to register SOAPMessageListener if needed.
     */
    public void endpointAdded(Endpoint endpoint) {

        EndpointMonitor monitor = new EndpointMonitor(endpoint);
        endpoint.addListener(monitor);
        monitoredEndpoints.put(endpoint.getEndpointSelector(), monitor);        
    }
    
    /**
     * Notification of a Web Service endpoint removal from the appserver
     * @param endpoint handler to register SOAPMessageListener if needed.
     */
    public void endpointRemoved(Endpoint endpoint) {
        
        EndpointMonitor monitor = monitoredEndpoints.remove(endpoint.getEndpointSelector());
        if (monitor!=null) {
            endpoint.removeListener(monitor);
        }        
    }
    
    public EndpointMonitor getEndpointMonitor(String selector) {
        return monitoredEndpoints.get(selector);
    }
}
