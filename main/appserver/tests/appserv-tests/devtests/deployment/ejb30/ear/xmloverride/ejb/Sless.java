/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.ear.xmloverride;

import javax.ejb.Remote;

@Remote
public interface Sless
{
    public String hello();

    public String goodMorning();

    public String goodBye();
}
