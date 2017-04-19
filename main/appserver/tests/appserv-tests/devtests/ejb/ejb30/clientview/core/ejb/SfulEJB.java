/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.core;

import javax.ejb.*;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class SfulEJB implements SessionBean
{

    private SessionContext sc_ = null;

    private int ejbCreateSfulCount = 0;
    public SfulEJB(){}

    @PostConstruct
    public void ejbCreateSful() {

        System.out.println("in SfulEJB::ejbCreateSful. count = " +
                           ejbCreateSfulCount);
        ejbCreateSfulCount++;

    }

    public void notSupported() {}
    public void required() {}
    public void requiresNew() {}
    public void mandatory() {}
    public void never() {}
    public void supports() {}

    public void setSessionContext(SessionContext sc)
    {
        sc_ = sc;
    }

    @PreDestroy
    public void ejbRemove() 
    { 
        System.out.println("In SfulEJB.ejbRemove(). about to throw exception");
        throw new RuntimeException("test cleanup for case where " +
                                   "ejbRemove throws an exception");
    }

    @PrePassivate
    public void ejbActivate() 
    {}

    @PostActivate
    public void ejbPassivate()
    {}

 public void testException1() throws Exception {
        throw new Exception("testException1");
    }

    // will throw ejb exception
    public void testException2() {
        throw new EJBException("testException2");
    }

    // throws some checked exception which is a subclass of the declared
    // checked exception
    public void testException3() throws javax.ejb.FinderException {
        throw new ObjectNotFoundException("testException3");
    }

    // throws some checked exception
    public void testException4() throws javax.ejb.FinderException {
        throw new FinderException("testException4");
    }


    public void testPassByRef1(int a) {

    }

    public void testPassByRef2(Helper1 helper1) {
        helper1.a++;
        helper1.b = helper1.b + "SfulEJB::testPassByRef2";
    }

    public void testPassByRef3(Helper2 helper2) {
        helper2.a++;
        helper2.b = helper2.b + "SfulEJB::testPassByRef3";
    }

    public void testPassByRef4(CommonRemote cr) {

    }

    public Helper1 testPassByRef5() {
        Helper1 h1 = new Helper1();
        h1.a = 1;
        h1.b = "SfulEJB::testPassByRef5";
        return h1;
    }

    public Helper2 testPassByRef6() {
        Helper2 h2 = new Helper2();
        h2.a = 1;
        h2.b = "SfulEJB::testPassByRef6";
        return h2;    
    }

    public CommonRemote testPassByRef7() {
        return (CommonRemote) sc_.getEJBObject();
    }

    public int testPassByRef8() {
        return 8;
    }

}
