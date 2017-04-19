/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.security.wss.permethod.servlet;

import javax.servlet.SingleThreadModel;
import javax.xml.rpc.server.ServiceLifecycle;

public class HelloServlet implements 
			SingleThreadModel, ServiceLifecycle {

    public HelloServlet() {
        System.out.println("HelloServlet() instantiated");
    }

    public void init(Object context) {
        System.out.println("Got ServiceLifecycle::init call " + context);
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public String sayHello(String message) {
        System.out.println("sayHello invoked from servlet endpoint");
        return "reply from " + message;
    }

    public int sendSecret(String message) {
        System.out.println("sendSecret invoked from servlet endpoint");
        return message.hashCode();
    }
        
    public String getSecret(double key) {
        System.out.println("getSecret invoked from servlet endpoint");
        return "Secret-" + key;
    }
}
