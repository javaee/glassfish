/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.ssl.servletws;

import javax.jws.WebService;

@WebService(targetNamespace="http://servletws.ssl.wss.security.s1asdev.sun.com", serviceName="WssSslServletService")
public class HelloServlet {
    public String hello(String who) {
        return "Hello, SSL " + who;
    }
}
