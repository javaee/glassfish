/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import javax.ejb.Local;
	
@Local
public interface Sful
{
    public String hello();

    public void incrementCounter();

    public int getCounter();

    public void setSfulRef(Sful ref);

    public Sful getSfulRef();

}
