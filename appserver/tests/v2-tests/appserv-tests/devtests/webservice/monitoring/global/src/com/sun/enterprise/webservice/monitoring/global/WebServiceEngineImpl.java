/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */


package com.sun.enterprise.webservice.monitoring.global;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import java.rmi.RemoteException; 
import java.util.*;

import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.TransportInfo;
import com.sun.enterprise.webservice.monitoring.GlobalMessageListener;
import com.sun.xml.rpc.spi.runtime.MessageContext;

/**
 * Implementation of the global monitoring web service.
 *
 * @author Jerome Dochez
 */
public class WebServiceEngineImpl implements SessionBean, GlobalMessageListener {
    
    SessionContext sc;
    LinkedList<InvocationTraceImpl> traces = new LinkedList<InvocationTraceImpl>();
    Map<String, InvocationTraceImpl> current = new HashMap<String, InvocationTraceImpl>();
    GlobalMessageListener parent = null;   
    
    public WebServiceEngineImpl() {
    }

    /** Creates a new instance of SimpleServerImpl */
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In WebServiceEngineImpl::ejbCreate !!");
        WebServiceEngineFactory factory = WebServiceEngineFactory.getInstance();
        com.sun.enterprise.webservice.monitoring.WebServiceEngine  engine = factory.getEngine();
        parent = engine.getGlobalMessageListener();
        engine.setGlobalMessageListener(this);
    }    
        
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {
        System.out.println("In WebServiceEngineImpl::ejbRemove");
        WebServiceEngineFactory factory = WebServiceEngineFactory.getInstance();
        com.sun.enterprise.webservice.monitoring.WebServiceEngine  engine = factory.getEngine();
        engine.setGlobalMessageListener(parent);
    }
    
    public void ejbActivate() {
        System.out.println("In WebServiceEngineImpl::ejbActivate");
    }
    
    public void ejbPassivate() {
        System.out.println("In WebServiceEngineImpl::ejbPassivate");        
    }
    
    
    // Implementation of the WebServiceEngine Interface   
    public EndpointInfo getEndpoint(String selector) throws RemoteException {
        return null;
    }
    
    public int getEndpointsCount() throws RemoteException {
        WebServiceEngineFactory factory = WebServiceEngineFactory.getInstance();
        com.sun.enterprise.webservice.monitoring.WebServiceEngine  engine = factory.getEngine();
        java.util.Iterator iterator = engine.getEndpoints();
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        return count;
    }
        
    
    public String getEndpointsSelector(int i) throws RemoteException {
        WebServiceEngineFactory factory = WebServiceEngineFactory.getInstance();
        com.sun.enterprise.webservice.monitoring.WebServiceEngine  engine = factory.getEngine();
        java.util.Iterator iterator = engine.getEndpoints();
        Endpoint endpoint;
        while (i!=0 && iterator.hasNext()) {
            i--;
            iterator.next();
        }
        if (iterator.hasNext()) {
            endpoint = (Endpoint) iterator.next();
            return endpoint.getEndpointSelector();
        }
        else 
            return null;
    }
    
    public int getTraceCount() throws RemoteException {
        return traces.size();
    }
    
    public InvocationTrace getTrace(int i) throws RemoteException {
        return traces.get(i);
    }
    
    
        
    /** 
     * Callback when a web service request entered the web service container
     * and before any system processing is done. 
     * @param endpoint is the endpoint the web service request is targeted to
     * @return a message ID to trace the request in the subsequent callbacks
     * or null if this invocation should not be traced further.
     */
    public String preProcessRequest(Endpoint endpoint) {
        
        String mid = null;  
        InvocationTraceImpl newTrace = new InvocationTraceImpl();
        if (parent!=null) {
            mid = parent.preProcessRequest(endpoint);
        } 
        if ("WebServiceEnginePort".equals(endpoint.getDescriptor().getWsdlPort().getLocalPart())) {
            // i don't trace myself ;-)
            return mid;
        }
        System.out.println("Tracing " +endpoint.getDescriptor().getWsdlPort().getLocalPart());
        if (mid==null) {
            mid = String.valueOf(newTrace.hashCode());
        }
        
        EndpointInfo info = new EndpointInfo();
        info.setEndpointType(endpoint.getEndpointType().toString());
        info.setEndpointSelector(endpoint.getEndpointSelector());
        newTrace.setEndpointInfo(info);
        current.put(mid, newTrace);
        return mid;
    }
    
    /** 
     * Callback when a web service request is about the be delivered to the 
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call 
     * @param trace the jaxrpc message trace, transport dependent
     */
    public void processRequest(String mid, MessageContext ctx, TransportInfo info) {
        InvocationTraceImpl trace = current.get(mid);
        if (trace!=null) {
            trace.setRequest(ctx);
            trace.setRequestTI(info);
        } else {
            System.out.println("Unknown mid in request " + mid);
        }
    }
    
    /**
     * Callback when a web service response was returned by the Web Service 
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param trace jaxrpc message trace, transport dependent.
     */
    public void processResponse(String mid, MessageContext ctx) {
        
        if (parent!=null) {
            parent.processResponse(mid, ctx);
        }
        InvocationTraceImpl trace = current.get(mid);
        if (trace!=null) {
            trace.setResponse(ctx);
        } else {
            System.out.println("Unknown mid in response" + mid);
        }
    }
    
    /**
     * Callback when a web service response has finished being processed
     * by the container and was sent back to the client
     * @param mid returned by the preProcessRequest call
     * @param info the response transport dependent information
     */
    public void postProcessResponse(String mid, TransportInfo info) {
        
        if (parent!=null) {
            parent.postProcessResponse(mid, info);
        }
        InvocationTraceImpl trace = current.get(mid);
        if (trace!=null) {
            trace.setResponseTI(info);
            current.remove(mid);
            if (traces.size()>10) {
                traces.removeFirst();
            }
            traces.addLast(trace);           
        } else {
            System.out.println("Unknown mid in post response" + mid);
        }        
    }
}
