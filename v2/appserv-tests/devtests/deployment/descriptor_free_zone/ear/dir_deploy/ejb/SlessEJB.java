/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.noappxml.ejb;

import com.sun.s1asdev.deployment.noappxml.util.Util;
import javax.ejb.Stateless;
import javax.ejb.Remote;

@Stateless
@Remote({Sless.class})
public class SlessEJB implements Sless
{
    public String hello() {
        Util.log("In SlessEJB:hello()");
        return "hello";
    }

}
