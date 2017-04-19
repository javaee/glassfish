/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2full;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import javax.transaction.UserTransaction;

import java.util.Collection;
import java.util.HashSet;

public class SfulEJB 
{

    private SessionContext sc;

    private Sless sless;
    private Sless sless2;
    private SlessSub sless3;


    private Sless sless4_;
    private void setSless4(Sless sless) {
        sless4_ = sless;
    }
    
    private Sless sless5_;
    void setSless5(Sless sless) {
        sless5_ = sless;
    }

    private SlessSub sless6_;
    public void setSless6(SlessSub sless) {
        sless6_ = sless;
    }

    private String state;

    public void set(String value) {
        state = value;
    }

    public String get() {
        return state;
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        try {

            testSlessRefs(sless, sless2, sless3);

            testSlessRefs(sless4_, sless5_, sless6_);


            InitialContext ic = new InitialContext();

            Sless sless7 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless1");
            Sless sless8 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless2");
            SlessSub sless9 = (SlessSub)
                ic.lookup("java:comp/env/ejb/TypeLevelSless3");

            testSlessRefs(sless7, sless8, sless9);

            Sless sless10 = (Sless)
                sc.lookup("ejb/TypeLevelSless1");
            Sless sless11 = (Sless)
                sc.lookup("ejb/TypeLevelSless2");
            SlessSub sless12 = (SlessSub)
                sc.lookup("ejb/TypeLevelSless3");
            testSlessRefs(sless10, sless11, sless12);

            // Make sure we can getUserTransaction via SessionContext.
            // This must work if bean has bean-managed transactions.
            UserTransaction ut = sc.getUserTransaction();

            int status = ut.getStatus();
            System.out.println("context ut status = " + status);

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("get user transaction failure");
        }

        return "hello";
    }

    public void foo() {}
    public void foo(int a, String b) {
    }

    private void testSlessRefs(Sless s1, Sless s2, SlessSub s3) 
        throws Exception {

        String sless1Id = s1.getId();
        String sless2Id = s2.getId();
        String sless3Id = s3.getId();

        System.out.println("sless1Id = " + sless1Id);
        System.out.println("sless2Id = " + sless2Id);
        System.out.println("sless3Id = " + sless3Id);

        if( sless1Id.equals(sless2Id) ||
            sless1Id.equals(sless3Id) ||
            sless2Id.equals(sless3Id) ) {
            throw new EJBException("sless bean ejb linking error");
        }

        Sless r1 = s1.roundTrip(s1);
        s1.roundTrip(s2);
        s1.roundTrip(s3);

        s2.roundTrip(s1);
        Sless r2 = s2.roundTrip(s2);
        s2.roundTrip(s3);

        s3.roundTrip(s1);
        s3.roundTrip(s2);
        Sless r3 = s3.roundTrip(s3);

        sless1Id = r1.getId();
        sless2Id = r2.getId();
        sless3Id = r3.getId();

        System.out.println("sless1Id = " + sless1Id);
        System.out.println("sless2Id = " + sless2Id);
        System.out.println("sless3Id = " + sless3Id);

        if( sless1Id.equals(sless2Id) ||
            sless1Id.equals(sless3Id) ||
            sless2Id.equals(sless3Id) ) {
            throw new EJBException("remote 3.0 param passing error");
        }

        Collection c = new HashSet();
        c.add(s2);
        s1.roundTrip2(c);
        s2.roundTrip2(c);
        s3.roundTrip2(c);

        s3.hello3();
 
    }

    public void afterCreate() {
        System.out.println("In SfulEJB::afterCreate() marked as PostConstruct");
    }

    public void remove() {
        System.out.println("In SfulEJB " + state + " @Remove method");
    }

    public void removeRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB " + state + " removeRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    public void removeNotRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB " + state + 
                           "removeNotRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

}
