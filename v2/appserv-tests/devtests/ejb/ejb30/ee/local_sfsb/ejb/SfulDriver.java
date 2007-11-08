/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import javax.ejb.Remote;

@Remote
public interface SfulDriver {

    public String sayHello();

    public boolean initialize();

    public boolean doRefAliasingTest();

    public void doCheckpoint();

    public void createManySfulEJBs(int count);

    public void checkGetRef();

    public boolean useSfulGreeter();
}
