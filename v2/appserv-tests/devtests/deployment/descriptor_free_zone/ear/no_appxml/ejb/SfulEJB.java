/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.noappxml.ejb;

import com.sun.s1asdev.deployment.noappxml.util.Util;
import javax.ejb.Stateful;
import javax.ejb.Remote;

@Stateful
@Remote({Sful.class})
public class SfulEJB implements Sful
{
    public String hello() {
        Util.log("In SfulEJB:hello()");
        return "hello";
    }
}
