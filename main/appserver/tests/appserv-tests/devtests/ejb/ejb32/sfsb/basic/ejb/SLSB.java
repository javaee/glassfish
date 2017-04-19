package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateless
public class SLSB implements Hello2 {

    @EJB private SFSB bean;

    public void testRemove() {
        System.out.println("In SLSB::testRemove()");
	bean.test("XYZ", 0);
        bean.testRemove();
        try {
        	bean.foo();
        	throw new RuntimeException("Removed bean got invoked!");
        } catch(NoSuchEJBException nse) {
        	System.out.println("SFSB has been removed, subsequent invocation got exception");
        }
    }

}
