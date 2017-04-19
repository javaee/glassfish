/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
public class SfulEJB
	implements Sful, SfulGreeter
{

    private Sful ref;

    private int counter = 0;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

    public String sayHello(String val) {
        return "Hello, " + val;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void incrementCounter() {
	counter++;
    }

    public int getCounter() {
	return counter;
    }

    public void setSfulRef(Sful ref) {
	this.ref = ref;
    }

    public Sful getSfulRef() {
	return ref;
    }

}
