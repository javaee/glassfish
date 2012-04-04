/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;

@Stateful
@Remote({Hello.class})
@TransactionManagement(TransactionManagementType.BEAN)
public class HelloEJB implements Hello  {

    private static final int ITERATIONS = 1;

    private SessionContext context;

    public @Resource void setSc(SessionContext sc) {
        System.out.println("In HelloEJB:setSc");
        context = sc;
    }

    @EJB private SlessBusiness2 slessBusiness2;    
    @EJB private SfulBusiness2 sfulBusiness2;
    @EJB protected SlessRemoteBusiness2 slessRemoteBusiness2;    
    @EJB public SfulRemoteBusiness2 sfulRemoteBusiness2;

    @EJB public SfulHome sfulHome;
    public Sful sful; 

    @EJB public SfulBusiness sfulBusiness;

    @EJB public SlessHome slessHome;
    public Sless sless;

    @EJB public SlessBusiness slessBusiness;

    @EJB public SfulRemoteHome sfulRemoteHome;
    public SfulRemote sfulRemote; 

    @EJB public SfulRemoteBusiness sfulRemoteBusiness;

    @EJB public SlessRemoteHome slessRemoteHome;
    public SlessRemote slessRemote;

    @EJB public SlessRemoteBusiness slessRemoteBusiness;

    private long overhead;

    private int passivateCount;
    private int activateCount;

    javax.transaction.UserTransaction ut;

    @EJB private SlessBusiness	refSless_1;
    @EJB private SlessBusiness	refSless_2;
    @EJB private SlessBusiness2	refSless2_1;
    @EJB private SlessBusiness2	refSless2_2;

    @EJB private SfulBusiness	refSful_1;
    @EJB private SfulBusiness	refSful_2;
    @EJB private SfulBusiness2	refSful2_1;
    @EJB private SfulBusiness2	refSful2_2;
    
    @EJB private DummySlessRemote	refRemoteSless_1;
    @EJB private DummySlessRemote	refRemoteSless_2;
    @EJB private DummySlessRemote2	refRemoteSless2_1;
    @EJB private DummySlessRemote2	refRemoteSless2_2;
    
    @EJB private DummyRemote	refRemoteSful_1;
    @EJB private DummyRemote	refRemoteSful_2;
    @EJB private DummyRemote2	refRemoteSful2_1;
    @EJB private DummyRemote2	refRemoteSful2_2;

    @PostConstruct
    public void create() {

	try {

            slessBusiness2.foo();
            sfulBusiness2.foo();

            SlessBusiness2 slessBusiness22 = slessBusiness.getSlessBusiness2();
            slessBusiness22.foo();

            SfulBusiness2 sfulBusiness22 = sfulBusiness.getSfulBusiness2();
            sfulBusiness22.foo();

            slessRemoteBusiness2.foo();
            slessRemoteBusiness2.bar();

            slessRemoteBusiness2.sharedRemoteLocalBusinessSuper(false);
            refSless_1.sharedRemoteLocalBusinessSuper(true);

            sfulRemoteBusiness2.foo();
            sfulRemoteBusiness2.bar();

            sful = sfulHome.create();
	    System.out.println("Created local sful objs via homes.");

            sless = slessHome.create();
	    System.out.println("Created local sless objs via homes.");

            // There are two create<METHOD> methods with the same signature.
            // The first is mapped to an @Init  method that ignores its input
            // parameter.  The second is mapped to an @Init method that sets
            // its state to the input parameter.  Test both return values to
            // ensure proper @Init mapping.

            int ignoreState = 11;
            SfulRemote sfulRemoteIgnore = sfulRemoteHome.create(ignoreState);
            int retrievedIgnoreState = sfulRemoteIgnore.getState();
            if( retrievedIgnoreState == ignoreState ) {
                throw new EJBException("Incorrect @Init mapping");
            }

            int state = 10;
            sfulRemote = sfulRemoteHome.createFoo(state);
            int retrievedState = sfulRemote.getState();
            if( retrievedState == state ) {
                System.out.println("Created remote sful objs via homes.");
            } else {
                throw new EJBException("Incorrect state = " + retrievedState
                                       + " retrieved from sfulRemote");
            }

            slessRemote = slessRemoteHome.create();
	    System.out.println("Created remote sless objs via homes.");

            ut = context.getUserTransaction();
	   	     
	System.out.println("**1** refSlessBusiness_1 : " + refSless_1);
	System.out.println("**1** refSlessBusiness_2 : " + refSless_2);
	System.out.println("**1** refSlessBusiness2_1 : " + refSless2_1);
	System.out.println("**1** refSlessBusiness2_2 : " + refSless2_2);
	System.out.println("**1** checkLocalReferences() ==> " + checkSlessLocalReferences());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    @PrePassivate public void prePassivate() {
        System.out.println("In HelloEJB::prePassivate");
        passivateCount++;
    }

    @PostActivate public void postActivate() {
        System.out.println("In HelloEJB::postActivate");

	// @@@ temporary workaround until UserTransaction serialization is fixed
	ut = context.getUserTransaction();	

        activateCount++;
    }
    
    public void shutdown() throws EJBException {

        try {
            try {
                sfulBusiness.removeRetainIfException(true);
            } catch(Exception e) {

                System.out.println("successfully caught exception from " +
                                   "removeRetainIfException");

                sfulBusiness.removeRetainIfException(false);
            }

            try {
                sfulBusiness.remove(); 
                throw new EJBException("2nd sfulBusiness remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sfulBusiness bean for the second time :" );
            }

            sful.remove();
            try {
                sful.remove(); 
                throw new EJBException("2nd sful remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sful bean for the second time :" );
            }

            // Doesn't matter how many times we call sless remove()
            sless.remove();
            sless.remove();

            try {
                sfulRemoteBusiness.removeRetainIfException(true);
            } catch(Exception e) {

                System.out.println("successfully caught exception from " +
                                   "removeRetainIfException");

                sfulRemoteBusiness.removeRetainIfException(false);
            }

            try {
                sfulRemoteBusiness.remove(); 
                throw new EJBException("2nd sfulRemoteBusiness remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sfulRemoteBusiness bean for the second time :" );
            }

            sfulRemote.remove();
            try {
                sfulRemote.remove(); 
                throw new EJBException("2nd sfulRemote remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sfulRemote bean for the second time :" );
            }

            // Doesn't matter how many times we call slessRemote remove()
            slessRemote.remove();
            slessRemote.remove();
            

        } catch(Exception e) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }


    }

    public boolean hasBeenPassivatedActivated() {
        return (passivateCount > 0) && (activateCount > 0);
    }

    public void warmup(int type) {

        try {

            slessBusiness2.foo();
            sfulBusiness2.foo();

            SlessBusiness2 slessBusiness22 = slessBusiness.getSlessBusiness2();
            slessBusiness22.foo();

            SfulBusiness2 sfulBusiness22 = sfulBusiness.getSfulBusiness2();
            sfulBusiness22.foo();

            slessRemoteBusiness2.foo();
            slessRemoteBusiness2.bar();

            sfulRemoteBusiness2.foo();
            sfulRemoteBusiness2.bar();

            warmup(type, true, true);
            warmup(type, true, false);
            warmup(type, false, true);
            warmup(type, false, false);
            
            // Measure looping and timing overhead 
            long begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
            }
            long end = System.currentTimeMillis();
            overhead = end - begin;
            
            ut = context.getUserTransaction();
            
            testLocalObjects(sful, sless);

            testEJBObjects(sfulRemote, slessRemote);
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
    }

    private void warmup(int type, boolean tx, boolean businessView) 
        throws Exception {

	// get Hotspot warmed up	
	Common bean = pre(type, tx, businessView);
	CommonRemote beanRemote = preRemote(type, tx, businessView);
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
            beanRemote.requiresNew();
	    bean.notSupported();
            beanRemote.notSupported();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
            beanRemote.required();
	    if ( tx ) {
		bean.mandatory();
                beanRemote.mandatory();
            } else {
		bean.never();
                beanRemote.never();
            }
	    bean.supports();
            beanRemote.supports();
	}
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {}
    }

    private Common pre(int type, boolean tx, boolean businessView) 
    {
	if ( tx ) try { ut.begin(); } catch ( Exception ex ) {
		ex.printStackTrace();
	}

	if ( type == Common.STATELESS )
            return businessView ? slessBusiness : sless;
	else 
            return businessView ? sfulBusiness : sful;
    }

    private CommonRemote preRemote(int type, boolean tx, boolean businessView) 
    {       
	if ( type == Common.STATELESS ) {
	    return businessView ? slessRemoteBusiness : slessRemote;
        } else {
	    return businessView ? sfulRemoteBusiness : sfulRemote;
        }
    }


    private float post(long begin, long end, boolean tx)
    {
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {
		ex.printStackTrace();
	    }
	return (float)( ((double)(end-begin-overhead))/((double)ITERATIONS) * 1000.0 );
    }

    public float requiresNew(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, true);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.requiresNew();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.requiresNew();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.requiresNew();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.requiresNew();
            }
            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
        return post(begin, end, tx);
    }

    public float notSupported(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, false);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.notSupported();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.notSupported();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.notSupported();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.notSupported();
            }
            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        } 
	return post(begin, end, tx);
    }

    public float required(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, true);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.required();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.required();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.required();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.required();
            }
            
            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
	return post(begin, end, tx);
    }

    public float mandatory(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, true);
            begin = System.currentTimeMillis();            
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.mandatory();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.mandatory();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.mandatory();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.mandatory();
            }

            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
	return post(begin, end, tx);
    }

    public float never(int type, boolean tx)
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, true);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.never();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.never();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.never();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.never();
            }
            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
	return post(begin, end, tx);
    }

    public float supports(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx, false);
            Common busBean = pre(type, tx, true);
            CommonRemote beanRemote = preRemote(type, tx, false);
            CommonRemote beanRemoteBusiness = preRemote(type, tx, true);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.supports();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                busBean.supports();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.supports();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemoteBusiness.supports();
            }

            end = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
	return post(begin, end, tx);
    }

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
	System.out.println("md = " + md);

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

        o1.hashCode();
                           
        o2.hashCode();
        
        o1.toString();

        o2.toString();
        
    }
    
    public boolean checkSlessLocalReferences() {
        boolean result = (refSless_1 != null);
        
        result = result && refSless_1.equals(refSless_1);
        result = result && refSless_1.equals(refSless_2);
        result = result && (! refSless_1.equals(refSless2_1));
        result = result && (! refSless_1.equals(refSless2_2));
        result = result && (! refSless_1.equals(null));
        result = result && (! refSless_1.equals(sfulHome));
        result = result && (! refSless_1.equals(sful));
        result = result && (! refSless_1.equals(sfulBusiness));
        result = result && (! refSless_1.equals(sfulRemote));
        result = result && (! refSless_1.equals(sfulRemoteHome));
        
        result = result && (refSless_2 != null);
        result = result && refSless_2.equals(refSless_1);
        result = result && refSless_2.equals(refSless_2);
        result = result && (! refSless_2.equals(refSless2_1));
        result = result && (! refSless_2.equals(refSless2_2));
        result = result && (! refSless_2.equals(null));
        result = result && (! refSless_2.equals(sfulHome));
        result = result && (! refSless_2.equals(sful));
        result = result && (! refSless_2.equals(sfulBusiness));
        result = result && (! refSless_2.equals(sfulRemote));
        result = result && (! refSless_2.equals(sfulRemoteHome));
        
        result = result && (refSless2_1 != null);
        result = result && refSless2_1.equals(refSless2_1);
        result = result && refSless2_1.equals(refSless2_2);
        result = result && (! refSless2_1.equals(refSless_1));
        result = result && (! refSless2_1.equals(refSless_2));
        result = result && (! refSless2_1.equals(null));
        result = result && (! refSless2_1.equals(sfulHome));
        result = result && (! refSless2_1.equals(sful));
        result = result && (! refSless2_1.equals(sfulBusiness));
        result = result && (! refSless2_1.equals(sfulRemote));
        result = result && (! refSless2_1.equals(sfulRemoteHome));
        
        result = result && (refSless2_2 != null);
        result = result && refSless2_2.equals(refSless2_2);
        result = result && refSless2_2.equals(refSless2_1);
        result = result && (! refSless2_2.equals(refSless_1));
        result = result && (! refSless2_2.equals(refSless_2));
        result = result && (! refSless2_2.equals(null));
        result = result && (! refSless2_2.equals(sfulHome));
        result = result && (! refSless2_2.equals(sful));
        result = result && (! refSless2_2.equals(sfulBusiness));
        result = result && (! refSless2_2.equals(sfulRemote));
        result = result && (! refSless2_2.equals(sfulRemoteHome));
        
        return result;
    }
    
    public boolean checkSfulLocalReferences() {
        boolean result = (refSful_1 != null);
        
        result = result && (refSful_1.equals(refSful_1));
        result = result && (! refSful_1.equals(refSful_2));
        result = result && (! refSful_1.equals(refSful2_1));
        result = result && (! refSful_1.equals(refSful2_2));
        result = result && (! refSful_1.equals(null));
        result = result && (! refSful_1.equals(sfulHome));
        result = result && (! refSful_1.equals(sful));
        result = result && (! refSful_1.equals(sfulBusiness));
        result = result && (! refSful_1.equals(sfulRemote));
        result = result && (! refSful_1.equals(sfulRemoteHome));
        result = result && (! refSful_1.equals(new Object()));
        
        result = result && (refSful_2 != null);
        result = result && (! refSful_2.equals(refSful_1));
        result = result && (refSful_2.equals(refSful_2));
        result = result && (! refSful_2.equals(refSful2_1));
        result = result && (! refSful_2.equals(refSful2_2));
        result = result && (! refSful_2.equals(null));
        result = result && (! refSful_2.equals(sfulHome));
        result = result && (! refSful_2.equals(sful));
        result = result && (! refSful_2.equals(sfulBusiness));
        result = result && (! refSful_2.equals(sfulRemote));
        result = result && (! refSful_2.equals(sfulRemoteHome));
        result = result && (! refSful_2.equals(new Object()));
        
        result = result && (refSful2_1 != null);
        result = result && (refSful2_1.equals(refSful2_1));
        result = result && (! refSful2_1.equals(refSful2_2));
        result = result && (! refSful2_1.equals(refSful_1));
        result = result && (! refSful2_1.equals(refSful_2));
        result = result && (! refSful2_1.equals(null));
        result = result && (! refSful2_1.equals(sfulHome));
        result = result && (! refSful2_1.equals(sful));
        result = result && (! refSful2_1.equals(sfulBusiness));
        result = result && (! refSful2_1.equals(sfulRemote));
        result = result && (! refSful2_1.equals(sfulRemoteHome));
        result = result && (! refSful2_1.equals(new Object()));
        
        result = result && (refSful2_2 != null);
        result = result && (refSful2_2.equals(refSful2_2));
        result = result && (! refSful2_2.equals(refSful2_1));
        result = result && (! refSful2_2.equals(refSful_1));
        result = result && (! refSful2_2.equals(refSful_2));
        result = result && (! refSful2_2.equals(null));
        result = result && (! refSful2_2.equals(sfulHome));
        result = result && (! refSful2_2.equals(sful));
        result = result && (! refSful2_2.equals(sfulBusiness));
        result = result && (! refSful2_2.equals(sfulRemote));
        result = result && (! refSful2_2.equals(sfulRemoteHome));
        result = result && (! refSful2_2.equals(new Object()));
        return result;
    }
    
    public boolean checkSlessRemoteReferences() {
        boolean result = (refRemoteSless_1 != null);
        
        result = result && (refRemoteSless_1.equals(refRemoteSless_1));
        result = result && (refRemoteSless_1.equals(refRemoteSless_2));
        result = result && (! refRemoteSless_1.equals(refRemoteSless2_1));
        result = result && (! refRemoteSless_1.equals(refRemoteSless2_2));
        result = result && (! refRemoteSless_1.equals(null));
        result = result && (! refRemoteSless_1.equals(sfulHome));
        result = result && (! refRemoteSless_1.equals(sful));
        result = result && (! refRemoteSless_1.equals(sfulBusiness));
        result = result && (! refRemoteSless_1.equals(sfulRemote));
        result = result && (! refRemoteSless_1.equals(sfulRemoteHome));
        result = result && (! refRemoteSless_1.equals(new Object()));
        
        result = result && (refRemoteSless_2 != null);
        result = result && (refRemoteSless_2.equals(refRemoteSless_1));
        result = result && (refRemoteSless_2.equals(refRemoteSless_2));
        result = result && (! refRemoteSless_2.equals(refRemoteSless2_1));
        result = result && (! refRemoteSless_2.equals(refRemoteSless2_2));
        result = result && (! refRemoteSless_2.equals(null));
        result = result && (! refRemoteSless_2.equals(sfulHome));
        result = result && (! refRemoteSless_2.equals(sful));
        result = result && (! refRemoteSless_2.equals(sfulBusiness));
        result = result && (! refRemoteSless_2.equals(sfulRemote));
        result = result && (! refRemoteSless_2.equals(sfulRemoteHome));
        result = result && (! refRemoteSless_2.equals(new Object()));
        
        result = result && (refRemoteSless2_1 != null);
        result = result && (refRemoteSless2_1.equals(refRemoteSless2_1));
        result = result && (refRemoteSless2_1.equals(refRemoteSless2_2));
        result = result && (! refRemoteSless2_1.equals(refRemoteSless_1));
        result = result && (! refRemoteSless2_1.equals(refRemoteSless_2));
        result = result && (! refRemoteSless2_1.equals(null));
        result = result && (! refRemoteSless2_1.equals(sfulHome));
        result = result && (! refRemoteSless2_1.equals(sful));
        result = result && (! refRemoteSless2_1.equals(sfulBusiness));
        result = result && (! refRemoteSless2_1.equals(sfulRemote));
        result = result && (! refRemoteSless2_1.equals(sfulRemoteHome));
        result = result && (! refRemoteSless2_1.equals(new Object()));
        
        result = result && (refRemoteSless2_2 != null);
        result = result && (refRemoteSless2_2.equals(refRemoteSless2_2));
        result = result && (refRemoteSless2_2.equals(refRemoteSless2_1));
        result = result && (! refRemoteSless2_2.equals(refRemoteSless_1));
        result = result && (! refRemoteSless2_2.equals(refRemoteSless_2));
        result = result && (! refRemoteSless2_2.equals(null));
        result = result && (! refRemoteSless2_2.equals(sfulHome));
        result = result && (! refRemoteSless2_2.equals(sful));
        result = result && (! refRemoteSless2_2.equals(sfulBusiness));
        result = result && (! refRemoteSless2_2.equals(sfulRemote));
        result = result && (! refRemoteSless2_2.equals(sfulRemoteHome));
        result = result && (! refRemoteSless2_2.equals(new Object()));
        return result;
    }
        
    public boolean checkSfulRemoteReferences() {
        boolean result = (refRemoteSful_1 != null);
        
        result = result && (refRemoteSful_1.equals(refRemoteSful_1));
        result = result && (! refRemoteSful_1.equals(refRemoteSful_2));
        result = result && (! refRemoteSful_1.equals(refRemoteSful2_1));
        result = result && (! refRemoteSful_1.equals(refRemoteSful2_2));
        result = result && (! refRemoteSful_1.equals(null));
        result = result && (! refRemoteSful_1.equals(sfulHome));
        result = result && (! refRemoteSful_1.equals(sful));
        result = result && (! refRemoteSful_1.equals(sfulBusiness));
        result = result && (! refRemoteSful_1.equals(sfulRemote));
        result = result && (! refRemoteSful_1.equals(sfulRemoteHome));
        result = result && (! refRemoteSful_1.equals(new Object()));
        
        result = result && (refRemoteSful_2 != null);
        result = result && (! refRemoteSful_2.equals(refRemoteSful_1));
        result = result && (refRemoteSful_2.equals(refRemoteSful_2));
        result = result && (! refRemoteSful_2.equals(refRemoteSful2_1));
        result = result && (! refRemoteSful_2.equals(refRemoteSful2_2));
        result = result && (! refRemoteSful_2.equals(null));
        result = result && (! refRemoteSful_2.equals(sfulHome));
        result = result && (! refRemoteSful_2.equals(sful));
        result = result && (! refRemoteSful_2.equals(sfulBusiness));
        result = result && (! refRemoteSful_2.equals(sfulRemote));
        result = result && (! refRemoteSful_2.equals(sfulRemoteHome));
        result = result && (! refRemoteSful_2.equals(new Object()));
        
        result = result && (refRemoteSful2_1 != null);
        result = result && (refRemoteSful2_1.equals(refRemoteSful2_1));
        result = result && (! refRemoteSful2_1.equals(refRemoteSful2_2));
        result = result && (! refRemoteSful2_1.equals(refRemoteSful_1));
        result = result && (! refRemoteSful2_1.equals(refRemoteSful_2));
        result = result && (! refRemoteSful2_1.equals(null));
        result = result && (! refRemoteSful2_1.equals(sfulHome));
        result = result && (! refRemoteSful2_1.equals(sful));
        result = result && (! refRemoteSful2_1.equals(sfulBusiness));
        result = result && (! refRemoteSful2_1.equals(sfulRemote));
        result = result && (! refRemoteSful2_1.equals(sfulRemoteHome));
        result = result && (! refRemoteSful2_1.equals(new Object()));
        
        result = result && (refRemoteSful2_2 != null);
        result = result && (refRemoteSful2_2.equals(refRemoteSful2_2));
        result = result && (! refRemoteSful2_2.equals(refRemoteSful2_1));
        result = result && (! refRemoteSful2_2.equals(refRemoteSful_1));
        result = result && (! refRemoteSful2_2.equals(refRemoteSful_2));
        result = result && (! refRemoteSful2_2.equals(null));
        result = result && (! refRemoteSful2_2.equals(sfulHome));
        result = result && (! refRemoteSful2_2.equals(sful));
        result = result && (! refRemoteSful2_2.equals(sfulBusiness));
        result = result && (! refRemoteSful2_2.equals(sfulRemote));
        result = result && (! refRemoteSful2_2.equals(sfulRemoteHome));
        result = result && (! refRemoteSful2_2.equals(new Object()));
        return result;
    }
    
    public DummyRemote getSfulRemoteBusiness(int num) {
        return (num == 1)
            ? refRemoteSful_1 : refRemoteSful_2;
    }
    
    public DummyRemote2 getSfulRemoteBusiness2(int num) {
        return (num == 1)
            ? refRemoteSful2_1 : refRemoteSful2_2;
    }
    
    public boolean compareRemoteRefs(Object ref1, Object ref2) {
        return ref1.equals(ref2);
    }
    
}
