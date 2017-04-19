/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful(mappedName="jndi_SfulDriver")
@EJBs({
  @EJB(name="ejb/RemoteSful", beanInterface=RemoteSful.class)
})
public class SfulDriverEJB
    implements SfulDriver, SfulFacade {

    @EJB
    private RemoteSful remoteSful;

    public String sayHello() {
        return "Hello";
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String sayRemoteHello() {
	return remoteSful.sayNamaste();
    }


    public void doCheckpoint() {
    }

}
