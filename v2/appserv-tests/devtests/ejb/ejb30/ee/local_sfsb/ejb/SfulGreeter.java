/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import javax.ejb.Local;
	
@Local
public interface SfulGreeter
{
    public String sayHello(String name);

    public int getCounter();

}
