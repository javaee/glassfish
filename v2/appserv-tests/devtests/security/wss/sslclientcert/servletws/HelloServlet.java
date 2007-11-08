/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.sslclientcert.servletws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(targetNamespace="http://servletws.sslclientcert.wss.security.s1asdev.sun.com", serviceName="WssSslClientCertServletService")
public class HelloServlet {
    @Resource WebServiceContext wsc;

    public String hello(String msg) {
        return "Hello Servlet, " + wsc.getUserPrincipal() + ": " + msg;
    }
}
