/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2full;

import javax.ejb.CreateException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.transaction.UserTransaction;

public class SlessEJB 
{


    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello from SlessEJB";
    }

    public String hello2() throws javax.ejb.CreateException {
        throw new javax.ejb.CreateException();
    }

    public String getId() {
        return "SlessEJB";
    }

    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB::roundTrip2 " + 
                           collectionOfSless);
        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());  
        }
        return collectionOfSless;
    }
}
