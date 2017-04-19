/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.web.jsp;

import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Remote({Dummy.class})
public class DummyEJB implements Dummy {

    public String hello() {
        return "Hello World";
    }
}
