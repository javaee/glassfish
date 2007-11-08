/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.webservice.ksink.googleclient;

import java.util.Map;

import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.namespace.QName;

import javax.naming.InitialContext;

public class ClientHandler extends GenericHandler {
  
    public void destroy() {
        System.out.println("In ClientHandler::destroy()");
    }

    public QName[] getHeaders() {
        return new QName[0];
    }

    public boolean handleFault(MessageContext context) {
        System.out.println("In ClientHandler::handleFault()");
        return true;
    }
        
    public boolean handleRequest(MessageContext context) {
        System.out.println("In ClientHandler::handleRequest()");
        return true;
    }
        
    public boolean handleResponse(MessageContext context) {
        System.out.println("In ClientHandler::handleResponse()");
        return true;
    } 
        
    public void init(HandlerInfo config) {
        System.out.println("In ClientHandler::init()");
        try {
            InitialContext ic = new InitialContext();
            String googleKey = (String) ic.lookup("java:comp/env/googlekey");
            System.out.println("google key = " + googleKey);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Handler init params = " +
                           config.getHandlerConfig());
    } 
   
}
