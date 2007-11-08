/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.tools.wsmonitoring;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

import com.sun.logging.LogDomains;

import com.sun.enterprise.webservice.monitoring.MessageListener;
import com.sun.enterprise.webservice.monitoring.MessageTrace;
import com.sun.enterprise.webservice.monitoring.Endpoint;

/**
 * This class is responsible for monitoring a particular
 * endpoint.
 *
 * @author dochez
 */
public class EndpointMonitor implements MessageListener {
    
    static final Logger sLogger=Logger.getLogger(LogDomains.WEB_LOGGER);        
    
    MessageExchange[] traces;
    int traceIndex=0;
        
    Endpoint endpoint;
        
    /** Creates a new instance of EndpointMonitor */
    public EndpointMonitor(Endpoint endpoint) {
        this.endpoint = endpoint;
        traces = new MessageExchange[5];
    }
        
    public Endpoint getEndpoint() {
        return endpoint;
    }                          
    
    public void invocationProcessed(MessageTrace request, MessageTrace response) {
        
        MessageExchange newExchange = new MessageExchange();
        newExchange.request = request;
        newExchange.response = response;
        
        traces[traceIndex++] = newExchange;
        if (traceIndex==traces.length) {
            traceIndex=0;
        }        
    }
    
        
    public MessageExchange[] getInvocationTraces() {
        
        Vector<MessageExchange> v = new Vector<MessageExchange>();
        if (traces[traceIndex]!=null) {
            for (int i=traceIndex;i<traces.length;i++) {
                v.add(traces[i]);
            }
        }
        for (int i=0;i<traceIndex;i++) {
            v.add(traces[i]);
        }
        if (v.isEmpty()) {
            return null;
        } else {
            return (MessageExchange[]) v.toArray(new MessageExchange[0]);
        }
    }
    
}
