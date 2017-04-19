/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

import javax.ejb.EJB;
import javax.ejb.RemoteHome;
import javax.ejb.CreateException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.transaction.UserTransaction;

@Stateless

// External remote ejb 3.0 reference.  Target ejb doesn't exist
// but this shouldn't cause any problems as long as reference is
// not looked up.  Remote jndi-names are not dereferenced until 
// lookup time, in *our* implementation.  This works for old-style 
// ejb-refs so this TYPE-level @EJB should have the same behavior.
@EJB(name="ejb/External", beanInterface=ExternalBusiness.class)
@Remote(Sless.class)
@RemoteHome(SlessRemoteHome.class)
public class SlessEJB 
{
    @Resource(name="null") SessionContext sc;

    public String hello() {
        System.out.println("In SlessEJB:hello()");
        try {
            Object lookup = sc.lookup(null);
        } catch(IllegalArgumentException iae) {
            System.out.println("Successfully got IllegalArgException for " +
                               "SessionContext.lookup(null)");
        }
        SessionContext sc2 = (SessionContext) sc.lookup("null");
        System.out.println("SessionContext.lookup(\"null\") succeeded");

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
