/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.stubs.proxy;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class HelloEJB implements SessionBean {

    private static final int ITERATIONS = 1;
    private SessionContext context;
    private Sful sful;
    private SfulHome sfulHome;
    private SfulRemote sfulRemote;
    private SfulRemoteHome sfulRemoteHome;

    private Sless sless;
    private SlessHome slessHome;
    private SlessRemote slessRemote;
    private SlessRemoteHome slessRemoteHome;

    private Bmp bmp;
    private BmpHome bmpHome;
    private BmpRemote bmpRemote;
    private BmpRemoteHome bmpRemoteHome;

    private long overhead;
    
    private static final String pkey = "A BMP Bean";

    javax.transaction.UserTransaction ut;

    public HelloEJB(){}

    public void ejbCreate() {

	try {
            Context ic = new InitialContext();

            Context ic1 = (Context) ic.lookup("java:comp/env");
            String ic1Name = ic1.getNameInNamespace();
            Context ic1_ = (Context) ic.lookup(ic1Name);

            Context ic2 = (Context) ic.lookup("java:comp/env/ejb");
            String ic2Name = ic2.getNameInNamespace();
            Context ic2_ = (Context) ic.lookup(ic2Name);            

	    sfulHome = (SfulHome) ic2_.lookup("Sful");
            sful = sfulHome.create();
            Object obj = ic2_.lookup("SfulRemote");
            sfulRemoteHome = (SfulRemoteHome)
                PortableRemoteObject.narrow(obj, SfulRemoteHome.class);
            sfulRemote = sfulRemoteHome.create();

	    System.out.println("Created Stateful bean.");
	   	     
	    slessHome = (SlessHome) ic1_.lookup("ejb/Sless");
            sless = slessHome.create();
            obj = ic2_.lookup("SlessRemote");
            slessRemoteHome = (SlessRemoteHome)
                PortableRemoteObject.narrow(obj, SlessRemoteHome.class);
            slessRemote = slessRemoteHome.create();

	    System.out.println("Created Stateless bean.");

	    bmpHome = (BmpHome) ic.lookup("java:comp/env/ejb/Bmp");

            bmp = bmpHome.create(pkey);
            obj = ic2_.lookup("BmpRemote");
            bmpRemoteHome = (BmpRemoteHome)
                PortableRemoteObject.narrow(obj, BmpRemoteHome.class);
            bmpRemote = (BmpRemote)
                bmpRemoteHome.findByPrimaryKey(pkey);

	    System.out.println("Created BMP bean.");
	   	     
	    ut = context.getUserTransaction();

	} catch (Exception ex) {
	    System.out.println("couldn't get all beans");
	    ex.printStackTrace();
	}
    }

    private void testRemove() throws RemoteException {
        try {
            EJBLocalObject bmp2 = bmpHome.create("foobar");
            System.out.println("Created BMP bean.");
            bmpHome.remove(bmp2.getPrimaryKey());
            System.out.println("Successfully removed local entity bean");
        } catch(Exception e) {            
            e.printStackTrace();
            throw new EJBException(e);
        }

        try {
            slessHome.remove(sless);
            throw new EJBException("expecting remove exception when " +
                                   "removing sless bean through EJBLocalHome");
        } catch(javax.ejb.RemoveException e) {
            System.out.println("Successfully caught RemoveException for " +
                               "sless local home");
            // success
        }

        try {
            sfulHome.remove(sful);
            throw new EJBException("expecting remove exception when " +
                                   "removing sful bean through EJBLocalHome");
        } catch(javax.ejb.RemoveException e) {
            System.out.println("Successfully caught RemoveException for " +
                               "sful local home");
            // success
        }

        
        try {
            EJBObject bmpRemote2 = bmpRemoteHome.create("foobar2");
            System.out.println("Created BMP bean.");
            bmpRemoteHome.remove(bmpRemote2.getPrimaryKey());

            System.out.println("Successfully removed entity bean via pk");
            EJBObject bmpRemote3 = bmpRemoteHome.create("foobar3");
            Handle r3Handle = bmpRemote3.getHandle();
            System.out.println("Created BMP bean.");
            bmpRemoteHome.remove(r3Handle);
            System.out.println("Successfully removed entity bean via handle");

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        try {
            slessRemoteHome.remove(slessRemote);
            throw new EJBException("expecting remove exception when " +
                                   "removing sless bean through EJBHome");
        } catch(javax.ejb.RemoveException e) {
            System.out.println("Successfully caught RemoveException for " +
                               "sless home");
            // success
        }

        try {
            sfulRemoteHome.remove(sfulRemote);
            throw new EJBException("expecting remove exception when " +
                                   "removing sful bean through EJBHome");
        } catch(javax.ejb.RemoveException e) {
            System.out.println("Successfully caught RemoveException for " +
                               "sful home");
            // success
        }

    }

    public void shutdown() throws EJBException {

        try {
            sful.remove();
            
            try {
                sful.remove(); 
                throw new EJBException("2nd sful remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sful bean for the second time :" );
                e.printStackTrace();
            }

            sless.remove();

            bmp.remove(); 

        } catch(Exception e) {
            e.printStackTrace();
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }

    }

    public void throwException() throws Exception {
        throw new Exception("throwException");
    }

    public void throwAppException1() throws FinderException {
        throw new FinderException("throwAppException1");
    }

    public void throwAppException2() throws FinderException {
        throw new ObjectNotFoundException("throwAppException2");
    }
    
    public void warmup(int type) throws Exception {
	warmup(type, true);
	warmup(type, false);

	// Measure looping and timing overhead 
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	}
	long end = System.currentTimeMillis();
	overhead = end - begin;

        String pkey2 = (String) bmp.getPrimaryKey();
        if( !pkey2.equals(pkey) ) {
            throw new EJBException("pkey2 " + pkey2 + " doesn't match " + pkey);
        }
        
        ut = context.getUserTransaction();
        
        testLocalObjects(sful, sless);
        testLocalObjects(sful, bmp);
        testLocalObjects(sless, bmp);
        
        testEJBObjects(sfulRemote, slessRemote);
        testEJBObjects(sfulRemote, bmpRemote);
        testEJBObjects(slessRemote, bmpRemote);

        testObjectMethods(sfulHome, slessHome);
        testObjectMethods(sfulHome, bmpHome);
        testObjectMethods(slessHome, bmpHome);

        testNotImplemented(sful);
        testNotImplemented(sless);
        testNotImplemented(bmp);

        testNotImplemented(sfulRemote);
        testNotImplemented(slessRemote);
        testNotImplemented(bmpRemote);

        testRemove();

        // skip testExceptions for sfsb since throwing a runtime exception
        // will cause the bean to be removed
        // testExceptions(sful);

        testExceptions(sless);
        testExceptions(bmp);

        testExceptions(slessRemote);
        testExceptions(bmpRemote);
    }
    
    private void testExceptions(Common c) {

        try {
            c.testException1();
            throw new EJBException("didn't get exception for testException1");
        } catch(Exception e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            c.testException2();
            throw new EJBException("didn't get exception for testException2");
        } catch(EJBException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            c.testException3();
            throw new EJBException("didn't get exception for testException3");
        } catch(FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }
                
    }

    private void testExceptions(CommonRemote c) throws RemoteException {

        try {
            c.testException1();
            throw new EJBException("didn't get exception for testException1");
        } catch(Exception e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            c.testException2();
            throw new EJBException("didn't get exception for testException2");
        } catch(RemoteException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            c.testException3();
            throw new EJBException("didn't get exception for testException3");
        } catch(FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            c.testException4();
            throw new EJBException("didn't get exception for testException4");
        } catch(FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }
                
    }

    public Object testPassByRef() throws Exception {
        System.out.println("Doing pass by ref tests for slessRemote " +
                           ", passbyref = true");
        _testPassByRef(slessRemote, true);

        System.out.println("Doing pass by ref tests for sfulRemote " +
                           ", passbyref = false");
        _testPassByRef(sfulRemote, false);

        System.out.println("Doing pass by ref tests for bmpRemote " +
                           ", passbyref = false");
        _testPassByRef(bmpRemote, false);

        // uncomment to see exception that is thrown when non-serializable
        // object is passed over non-collocated remote call
        //return new Helper2();
        return null;
    }

    private void _testPassByRef(CommonRemote c, boolean passByRef) 
      throws Exception {
        
        int intVal = 1;
        String stringVal = "HelloEJB::_testPassByRef";

        c.testPassByRef1(10);

        // testPassByRef2
        Helper1 h1 = new Helper1();
        h1.a = intVal;
        h1.b = stringVal;
        System.out.println("Before testPassByRef2 : " + h1);
        c.testPassByRef2(h1);
        System.out.println("After testPassByRef2 : " + h1);
        if( passByRef ) {
            if( (h1.a == intVal) || h1.b.equals(stringVal) ) {
                throw new EJBException("h1 mutations not present " + h1);
            }            
        } else {
            if( (h1.a != intVal) || !h1.b.equals(stringVal) ) {
                throw new EJBException("error : h1 was mutated " + h1);
            }
        }

        // testPassByRef3
        Helper2 h2 = new Helper2();
        h2.a = intVal;
        h2.b = stringVal;
        System.out.println("Before testPassByRef3 : " + h2);
        if( passByRef ) {
            c.testPassByRef3(h2);
            System.out.println("After testPassByRef3 : " + h2);
            if( (h2.a == intVal) || h2.b.equals(stringVal) ) {
                throw new EJBException("h2 mutations not present " + h2);
            }   
        } else {
            try {
                c.testPassByRef3(h2);
                System.out.println("Error : Expected exception when " + 
                                   "passing non-serializable data " + h2);
                 
                /* DON'T TREAT AS FATAL ERROR WHILE WE INVESTIGATE WHETHER
                 * THIS WAS INTENDED BEHAVIOR FOR THE ORB
                 throw new EJBException("Error : Expected exception when " + 
                 "passing non-serializable data " + 
                 h2);
                */
            } catch(EJBException ejbex) {
                throw ejbex;
            } catch(Exception e) {
                System.out.println("Caught expected exception when " + 
                                   " passing non-serializable data" +
                                   e.toString());
            }
        }

        c.testPassByRef4(c);

        c.testPassByRef5();

        if( passByRef ) {
            c.testPassByRef6();
        } else {
            try {
                Helper2 h6 = c.testPassByRef6();
                System.out.println("Error : Expected exception when " + 
                           "returning non-serializable data " + h6);
                /* see comment for testPassByRef3 above 
                throw new EJBException("Error : Expected exception when " + 
                                       "returning non-serializable data ");
                */
            } catch(EJBException ejbex) {
                throw ejbex;                           
            } catch(Exception e) {
                System.out.println("Caught expected exception when " + 
                                   " returning non-serializable data" +
                                   e.toString());
            }
        }

        c.testPassByRef7();

        c.testPassByRef8();
    }

    private void testNotImplemented(Common c) {
        try {
            c.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void testNotImplemented(CommonRemote cr) {
        try {
            cr.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void warmup(int type, boolean tx) throws Exception {
	// get Hotspot warmed up	
	Common bean = pre(type, tx);
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	    bean.notSupported();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	    if ( tx )
		bean.mandatory();
	    else
		bean.never();
	    bean.supports();
	}
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {}
    }

    private Common pre(int type, boolean tx) 
    {
	if ( tx ) try { ut.begin(); } catch ( Exception ex ) {}
	if ( type == Common.STATELESS )
	    return sless;
	else if ( type == Common.STATEFUL )
	    return sful;
	else
	    return bmp;
    }

    private CommonRemote preRemote(int type, boolean tx) 
    {       
	if ( type == Common.STATELESS )
	    return slessRemote;
	else if ( type == Common.STATEFUL )
	    return sfulRemote;
	else
	    return bmpRemote;
    }


    private float post(long begin, long end, boolean tx)
    {
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {}
	return (float)( ((double)(end-begin-overhead))/((double)ITERATIONS) * 1000.0 );
    }

    public float requiresNew(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	}
        for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.requiresNew();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float notSupported(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.notSupported();
	}
        for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.notSupported();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float required(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.required();
	}

	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float mandatory(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.mandatory();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.mandatory();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float never(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.never();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.never();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float supports(int type, boolean tx) throws RemoteException
    {
	Common bean = pre(type, tx);
        CommonRemote beanRemote = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.supports();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    beanRemote.supports();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public void setSessionContext(SessionContext sc) {
	context = sc;
    }

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    // assumes lo1 and lo2 are do not have same client identity
    public void testLocalObjects(EJBLocalObject lo1, EJBLocalObject lo2) {

        testObjectMethods(lo1, lo2);

        if( lo1.isIdentical(lo2) ) {
            throw new EJBException("isIdentical failed");
        }

        if( lo2.isIdentical(lo1) ) {
            throw new EJBException("isIdentical failed");
        }

        if( !lo1.isIdentical(lo1) ) {
            throw new EJBException("isIdentical failed");
        }

        if( !lo2.isIdentical(lo2) ) {
            throw new EJBException("isIdentical failed");
        }

        EJBLocalHome lh1 = lo1.getEJBLocalHome();
        if( lh1 == null ) {
            throw new EJBException("null lh1");
        }

        EJBLocalHome lh2 = lo2.getEJBLocalHome();
        if( lh2 == null ) {
            throw new EJBException("null lh2");
        }

    }

    // assumes lo1 and lo2 are do not have same client identity
    public void testEJBObjects(EJBObject o1, EJBObject o2) throws Exception {

        testObjectMethods(o1, o2);

        if( o1.isIdentical(o2) ) {
            throw new EJBException("isIdentical failed");
        }
        
        if( o2.isIdentical(o1) ) {
            throw new EJBException("isIdentical failed");
        }
        
        if( !o1.isIdentical(o1) ) {
            throw new EJBException("isIdentical failed");
        }

        if( !o2.isIdentical(o2) ) {
            throw new EJBException("isIdentical failed");
        }

        EJBHome h1 = o1.getEJBHome();
        if( h1 == null ) {
            throw new EJBException("null h1");
        }
        testEJBHome(h1);

        EJBHome h2 = o2.getEJBHome();
        if( h2 == null ) {
            throw new EJBException("null h2");
        }
        testEJBHome(h2);
    }

    public void testEJBHome(EJBHome home) throws Exception {

        EJBMetaData md = home.getEJBMetaData();
        if( md == null ) {
            throw new EJBException("null md");
        }

        HomeHandle hh = home.getHomeHandle();
        if( hh == null ) {
            throw new EJBException("null hh");
        }

    }

    public void testObjectMethods(Object o1, Object o2) {

        // test java.lang.Object methods that must be handled by proxy

        if( !o1.equals(o1) ) {
            throw new EJBException("o1.equals() failed");
        }

        if( !o2.equals(o2) ) {
            throw new EJBException("o2.equals() failed");
        }

        if( o1.equals(o2) ) {
            throw new EJBException("o1 shouldn't be equal() to o2");
        }

        System.out.println("o1.hashCode() = " + o1.hashCode());
                           
        System.out.println("o2.hashCode() = " + o2.hashCode());
        
        System.out.println("o1.toString() = " + o1.toString());
        System.out.println("o2.toString() = " + o2.toString());
        
    }

}
