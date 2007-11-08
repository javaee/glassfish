/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.deployment.ejb30.ear.security;

import javax.ejb.Remote;
	
@Remote
public interface Sful
{
    public String hello();

    public String goodAfternoon();

    public String goodNight();
}
