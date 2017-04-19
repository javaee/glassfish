/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.ssl.ejbws;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(targetNamespace="http://ejbws.ssl.wss.security.s1asdev.sun.com", serviceName="WssSslEjbService")
public class HelloEjb {
    public String hello(String who) {
        return "Hello, SSL " + who;
    }
}
