/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.gartner;

import javax.jws.WebService;
import javax.ejb.Stateless;

@Stateless
@WebService(targetNamespace="http://gartner.wss.security.s1asdev.sun.com")
public class PingEjb {
    private static String id = "Sun Java System Application Server 9 - (EJB Endpoint) ";

    public String ping(String text) {
        return id + text;
    }
}
