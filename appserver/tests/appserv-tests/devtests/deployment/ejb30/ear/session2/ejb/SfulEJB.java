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

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.interceptor.Interceptors;
import javax.ejb.EJBs;
import javax.ejb.Remove;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

import java.util.Collection;
import java.util.HashSet;

@Stateful
@TransactionManagement(TransactionManagementType.BEAN)

@EJBs( 
 { @EJB(name="ejb/TypeLevelSless1", beanName="SlessEJB", 
       beanInterface=Sless.class),
   @EJB(name="ejb/TypeLevelSless2", beanName="SlessEJB2", 
       beanInterface=Sless.class) ,
   @EJB(name="ejb/SfulEJB2", beanInterface=Sful2.class)
 })

@Interceptors(MyCallbackHandler.class)
@EJB(name="ejb/TypeLevelSless3", beanInterface=SlessSub.class)
@Remote({Sful.class})
// ejb is not required to put business interface in implements clause
public class SfulEJB 
{

    private @Resource SessionContext sc;

    private @EJB(beanName="SlessEJB") Sless sless;
    private @EJB(beanName="SlessEJB2") Sless sless2;
    private @EJB(beanName="SlessEJB3") SlessSub sless3;

    private Sless sless4;
    private @EJB(beanName="SlessEJB") void setSless4(Sless sless) {
        sless4 = sless;
    }
    
    private Sless sless5;
    @EJB(beanName="SlessEJB2") void setSless5(Sless sless) {
        sless5 = sless;
    }

    private SlessSub sless6;
    public @EJB void setSless6(SlessSub sless) {
        sless6 = sless;
    }

    @Resource
    private void setSessionContext(SessionContext context) {

        try {
            context.getUserTransaction();
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "context.getUserTransaction() in " +
                               "setContext method of BMT SFSB");
        }

        try {
            context.getTimerService();
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "context.getTimerService() in " +
                               "setContext method");
        }

        try {
            context.getBusinessObject(Sful.class);
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "getBusinessObject() in " +
                               "setContext method");
        }

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

            testSlessRefs(sless4, sless5, sless6);


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

            // Do bogus lookup to ensure exception is thrown
            try {
                sc.lookup("abcdef");
                throw new Exception("lookup should have thrown exception");
            } catch(IllegalArgumentException iae) {
                System.out.println("Got expected exception for bogus lookup");
            }

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("hello failure", e);
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

    @PostConstruct
    public void afterCreate() {
        System.out.println("In SfulEJB::afterCreate() marked as PostConstruct");

        // jndi lookup should work within postconstruct
        Sless sless = (Sless) sc.lookup("ejb/TypeLevelSless1");

        // Access to getBusinessObject is allowed here
        sc.getBusinessObject(Sful.class);

    }

    @Remove
    public void remove() {
        System.out.println("In SfulEJB " + state + " @Remove method");
        // Access to getBusinessObject is allowed here
        sc.getBusinessObject(Sful.class);

    }

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB " + state + " removeRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeNotRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB " + state + 
                           "removeNotRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeMethodThrowSysException(boolean throwException) {

        System.out.println("In SfulEJB " + state + 
                           "removeMethodThrowSysException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new EJBException
                ("throwing system exception from @Remove method");
        }
    }

    public void doRemoveMethodSessionSyncTests() {

        Sful2 sful2_1 = (Sful2) sc.lookup("ejb/SfulEJB2");
        Sful2 sful2_2 = (Sful2) sc.lookup("ejb/SfulEJB2");
        Sful2 sful2_3 = (Sful2) sc.lookup("ejb/SfulEJB2");

        sful2_1.hello();
        try {
            sful2_1.removeRetainIfException(true);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // all SessionSynch callbacks should have happened
        if( SfulEJB2.afterBeginCalled &&
            SfulEJB2.beforeCompletionCalled &&
            SfulEJB2.afterCompletionCalled ) {
            System.out.println("Got expected SessionSynch behavior for " +
                               "removeRetainIfException(true)");                               
        } else {
            throw new EJBException("SessionSynch failure for " +
                                   "removeRetainIfException(true)");
        }

        // invocation should still work since bean wasn't removed
        sful2_1.hello();

        try {
            sful2_1.removeRetainIfException(false);
        } catch(Exception e) {
            throw new EJBException(e);
        }

        // bean should have been removed and completion-time SessionSynch
        // callbacks should not have been called.
        if( SfulEJB2.afterBeginCalled &&
            !SfulEJB2.beforeCompletionCalled &&
            !SfulEJB2.afterCompletionCalled ) {
            System.out.println("Got expected SessionSynch behavior for " +
                               "removeRetainIfException(false)");         
        } else {
            throw new EJBException("SessionSynch failure for " +
                                   "removeRetainIfException(true)");
        }

        



    }


}
