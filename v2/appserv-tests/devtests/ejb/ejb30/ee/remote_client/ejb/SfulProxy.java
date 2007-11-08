/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.remote_client;

import javax.ejb.Remote;

@Remote
public interface SfulProxy {

    public boolean initialize();

    public String sayHello();

    public String sayRemoteHello();

    public void doCheckpoint();

}
