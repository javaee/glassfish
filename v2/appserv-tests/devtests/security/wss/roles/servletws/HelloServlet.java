/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles.servletws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(targetNamespace="http://servletws.roles.wss.security.s1asdev.sun.com", serviceName="WssRolesServletService")
public class HelloServlet {
    @Resource WebServiceContext wsContext;

    public String hello(String who) {
        if (!wsContext.isUserInRole("javaee") ||
                wsContext.isUserInRole("ejbuser")) {
            throw new RuntimeException("not of role javaee or of role ejbuser");
        }

        return wsContext.getUserPrincipal() + "Hello, " + who;
    }
}
