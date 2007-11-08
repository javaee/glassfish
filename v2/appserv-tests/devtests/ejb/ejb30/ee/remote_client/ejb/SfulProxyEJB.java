/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.remote_client;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb.SfulDriver;

@Stateful
@EJB(name="ejb/Delegate",
	mappedName="corbaname:iiop:localhost:3700#mapped_jndi_name_for_SfulDriver",
	beanInterface=SfulDriver.class)
public class SfulProxyEJB
	implements SfulProxy {

    private SfulDriver delegate;

    public boolean initialize() {

/*
	try {
	    InitialContext ctx = new InitialContext();
	    delegate = (SfulDriver) ctx.lookup("mappedSfulDriver__3_x_Internal_RemoteBusinessHome__");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	try {
	    InitialContext ctx = new InitialContext();
	    delegate = (SfulDriver) ctx.lookup("mappedSfulDriver");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
*/

	try {
	    InitialContext ctx = new InitialContext();
	    delegate = (SfulDriver) ctx.lookup("java:comp/env/ejb/Delegate");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	return (delegate != null);
    }

    public String sayHello() {
	return delegate.sayHello();
    }

    public String sayRemoteHello() {
	return delegate.sayRemoteHello();
    }

    public void doCheckpoint() {
	delegate.doCheckpoint();
    }

}
