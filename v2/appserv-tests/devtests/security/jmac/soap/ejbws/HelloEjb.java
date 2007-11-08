/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.soap.ejbws;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(targetNamespace="http://ejbws.soap.jmac.security.s1asdev.sun.com", serviceName="JmacSoapEjbService")
public class HelloEjb {
    public String hello(String who) {
        String message = "HelloEjb " + who;
        System.out.println(message);
        return message;
    }
}
