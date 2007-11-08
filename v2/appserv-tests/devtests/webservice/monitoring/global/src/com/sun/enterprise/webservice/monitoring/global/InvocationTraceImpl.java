/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */


package com.sun.enterprise.webservice.monitoring.global;

import java.io.ByteArrayOutputStream;
import com.sun.enterprise.webservice.monitoring.TransportInfo;
import com.sun.xml.rpc.spi.runtime.MessageContext;
import com.sun.xml.rpc.spi.runtime.SOAPMessageContext;

/**
 *
 * @author dochez
 */
public class InvocationTraceImpl extends InvocationTrace {
    
    
    TransportInfo requestTI;
    TransportInfo responseTI;
    
    
    /** Creates a new instance of InvocationTrace */
    public InvocationTraceImpl() {
    }
    
    void setRequestTI(TransportInfo info) {
        requestTI = info;
    }
    
    void setResponseTI(TransportInfo info) {
        responseTI = info;
    }
    
    void setRequest(MessageContext messageCtx) {
        
        if (messageCtx instanceof SOAPMessageContext) {
            SOAPMessageContext soapMessageCtx = (SOAPMessageContext) messageCtx;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();        
            try {
                soapMessageCtx.getMessage().writeTo(baos);       
            } catch(Exception e) {
                e.printStackTrace();
            }    
        
            request = baos.toString();          
        }        
    }
    
    void setResponse(MessageContext messageCtx) {
        if (messageCtx instanceof SOAPMessageContext) {
            SOAPMessageContext soapMessageCtx = (SOAPMessageContext) messageCtx;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();        
            try {
                soapMessageCtx.getMessage().writeTo(baos);       
            } catch(Exception e) {
                e.printStackTrace();
            }    
        
            response = baos.toString();          
        }                
    }
        
}
