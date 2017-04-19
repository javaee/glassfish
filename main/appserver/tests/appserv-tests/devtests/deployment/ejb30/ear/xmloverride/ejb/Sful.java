/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.deployment.ejb30.ear.xmloverride;

import javax.ejb.Remote;
	
@Remote
public interface Sful
{
    public String hello();

    public String goodNight(String message);

    public String goodNight(String message1, String message2);

    public String bye();
}
