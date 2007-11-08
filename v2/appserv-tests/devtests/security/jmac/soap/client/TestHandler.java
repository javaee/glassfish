package com.sun.s1asdev.security.jmac.soap.client;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.*;

public class TestHandler implements SOAPHandler<SOAPMessageContext> {
    
    public Set<QName> getHeaders() {
        return null;
    }
    
    public void init() {
    }

    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("Calling client handler");
        try {
            boolean outbound = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            String prefix;
            if (outbound) {
                prefix = "OutboundHandler ";
                System.out.println("Calling outbound client handler");
            } else {
                prefix = "InboundHandler ";
                System.out.println("Calling inbound client handler");
            }
            SOAPMessage message = context.getMessage();
            SOAPBody body = message.getSOAPBody();
            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild().getFirstChild();
            paramElement.setValue(prefix + paramElement.getValue());
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }
    
    public void destroy() {
    }
    
    public void close(MessageContext context) {
    }
}
