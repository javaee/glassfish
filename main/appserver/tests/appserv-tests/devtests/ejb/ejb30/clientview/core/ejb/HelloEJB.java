/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.core;

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
public class HelloEJB implements Hello {

    private static final int ITERATIONS = 1;

    private SessionContext context;
    private @Resource void setSc(SessionContext sc) {
        System.out.println("In HelloEJB:setSc");
        context = sc;
    }

    // in addition to public

    @EJB private SfulHome sfulHome;
    private Sful sful;

    @EJB private SfulRemoteHome sfulRemoteHome;
    private SfulRemote sfulRemote;

    @EJB public SlessHome slessHome;
    private Sless sless;

    @EJB public SlessRemoteHome slessRemoteHome;
    private SlessRemote slessRemote;

    @EJB public BmpHome bmpHome;

    @EJB(beanName="BmpBean") 
    public BmpHomeSuper bmpHomeSuper;

    private Bmp bmp;

    @EJB public BmpRemoteHome bmpRemoteHome;
    private BmpRemote bmpRemote;

    private long overhead;
    
    private static final String pkey = "A BMP Bean";

    @Resource javax.transaction.UserTransaction ut;

    @PostConstruct
    public void create() {

	try {

            sful = sfulHome.createSful();
            sfulRemote = sfulRemoteHome.createSful();
	    System.out.println("Created loca/remote sful objs via homes.");

            bmp = bmpHome.create(pkey);
            bmpRemote = (BmpRemote)
                bmpRemoteHome.findByPrimaryKey(pkey);
	    System.out.println("Created BMP bean.");

            sless = slessHome.create();
            slessRemote = slessRemoteHome.create();
	    System.out.println("Created loca/remote sless objs via homes.");


            //            ut = context.getUserTransaction();
	   	     
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private void testRemove() throws RemoteException {
        try {
            EJBLocalObject bmp2 = (EJBLocalObject) bmpHome.create("foobar");
            System.out.println("Created BMP bean.");
            bmpHome.remove(bmp2.getPrimaryKey());
            System.out.println("Successfully removed local entity bean");
        } catch(Exception e) {            
            e.printStackTrace();
            throw new EJBException(e);
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

    }

    public void shutdown() throws EJBException {

        try {
            ((EJBLocalObject)sful).remove();
            
            try {
                ((EJBLocalObject)sful).remove(); 
                throw new EJBException("2nd sful remove should have caused exception");
            } catch(Exception e) {
                System.out.println("Successfully caught exception when attempting to "+
                                   " remove sful bean for the second time :" );
                e.printStackTrace();
            }

            ((EJBLocalObject)sless).remove();

            ((EJBLocalObject)bmp).remove(); 

        } catch(Exception e) {
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
    
    public void warmup(int type) {

        try {
            warmup(type, true);
            warmup(type, false);
            
            // Measure looping and timing overhead 
            long begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
            }
            long end = System.currentTimeMillis();
            overhead = end - begin;
            
            String pkey2 = (String) ((EJBLocalObject)bmp).getPrimaryKey();
            if( !pkey2.equals(pkey) ) {
                throw new EJBException("pkey2 " + pkey2 + " doesn't match " + pkey);
            }
            
            ut = context.getUserTransaction();
            
            testLocalObjects(((EJBLocalObject)sful), ((EJBLocalObject)sless));
            testLocalObjects(((EJBLocalObject)sful), ((EJBLocalObject)bmp));
            testLocalObjects(((EJBLocalObject)sless),((EJBLocalObject)bmp));
            
            testEJBObjects(sfulRemote, slessRemote);
            testEJBObjects(sfulRemote, bmpRemote);
            testEJBObjects(slessRemote, bmpRemote);
            
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
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
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

    public Object testPassByRef() {
        try {
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
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

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

    private void warmup(int type, boolean tx) {
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

    public float requiresNew(int type, boolean tx) 
    {
        long begin = 0;
        long end = 0;
        try {
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.requiresNew();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.requiresNew();
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
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.notSupported();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.notSupported();
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
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.required();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.required();
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
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();            
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.mandatory();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.mandatory();
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
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.never();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.never();
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
            Common bean = pre(type, tx);
            CommonRemote beanRemote = preRemote(type, tx);
            begin = System.currentTimeMillis();
            for ( int i=0; i<ITERATIONS; i++ ) {
                bean.supports();
            }
            for ( int i=0; i<ITERATIONS; i++ ) {
                beanRemote.supports();
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
