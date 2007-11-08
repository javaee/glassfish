/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.sslclientcert.ejbws;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@Stateless
@WebService(targetNamespace="http://ejbws.sslclientcert.wss.security.s1asdev.sun.com", serviceName="WssSslClientCertEjbService")
public class HelloEjb {
    @Resource private SessionContext sc;
    @Resource WebServiceContext wsc;

    public String hello(String msg) {
        return "Hello Ejb, " + sc.getCallerPrincipal() + ": " + msg ;
    }

    public String hello2(String msg) {
        return "Hello Ejb 2, " + wsc.getUserPrincipal() + ": " + msg ;
    }
}
