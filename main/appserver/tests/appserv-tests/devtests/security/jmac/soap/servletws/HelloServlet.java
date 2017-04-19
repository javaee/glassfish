/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.soap.servletws;

import javax.jws.WebService;

@WebService(targetNamespace="http://servletws.soap.jmac.security.s1asdev.sun.com", serviceName="JmacSoapServletService")
public class HelloServlet {
    public String hello(String who) {
        String message = "HelloServlet " + who;
        System.out.println(message);
        return message;

    }
}
