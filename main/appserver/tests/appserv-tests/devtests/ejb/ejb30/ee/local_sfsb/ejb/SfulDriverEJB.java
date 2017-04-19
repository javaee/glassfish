/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful(mappedName="AAbbCCejb/SfulLocalRemoteRef")
@EJB(name="ejb/Sful", beanInterface=Sful.class)
public class SfulDriverEJB
    implements SfulDriver {

    private Sful ref1;
    private Sful ref2;
    @EJB
    private SfulGreeter sfulGreeter;

    public String sayHello() {
        System.out.println("In SfulDriverEJB:sayHello()");
        return "Hello";
    }

    public boolean initialize() {
	boolean result = false;

	ref1 = ref2 = createSful();

        ref1.setSfulRef(ref2);
	return (ref1 != null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean doRefAliasingTest() {
	boolean result = false;
	for (int i=0; i<10; i++) {
	    ref1.incrementCounter();
	}

	return (ref1.getCounter() == ref2.getCounter());
    }

    public void doCheckpoint() {
    }

    public void checkGetRef() {
	Sful ref = ref1.getSfulRef();
        ref.getCounter();
    }

    public void createManySfulEJBs(int count) {
/*
	while (count-- > 0) {
	    Sful sf = (Sful) createSful();
	}
*/
    }

    private Sful createSful() {
	Sful sful = null;
        try {
	    InitialContext ctx = new InitialContext();
	    sful = (Sful) ctx.lookup("java:comp/env/ejb/Sful");
        } catch (Exception ex) {
	    ex.printStackTrace();
        }

	return sful;
    }

    public boolean useSfulGreeter() {
	return (sfulGreeter.getCounter() == ref1.getCounter());
    }
}
