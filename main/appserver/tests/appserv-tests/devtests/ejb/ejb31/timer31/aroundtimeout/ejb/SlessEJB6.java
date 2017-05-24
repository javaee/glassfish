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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.InvocationContext;


// Each interceptor list must at least have InterceptorG for
// aroundTimeoutCalled state to be set correctly.

@Stateless
@ExcludeDefaultInterceptors
public class SlessEJB6 implements Sless6
{
    boolean aroundTimeoutCalled = false;
    boolean aroundInvokeCalled = false;

    private final static int EXPECTED = 10;

    @EJB SlessEJB7 sless7;

    // Called as a timeout only. InterceptorG has 2 separate aroundXXX calls.
    @Interceptors({InterceptorA.class, InterceptorG.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-ag")    
    public void ag() {
        System.out.println("in SlessEJB6:ag().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("InterceptorG aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-ag");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-ag")    
    public void ag(Timer t) {
        System.out.println("in SlessEJB6:ag(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);
                           
        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6_Timer-ag");
    }

    // bg() (but not bg(param)) marked through ejb-jar.xml 
    // as having aroundtimeout B,G
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-bg")    
    public void bg() {
        System.out.println("in SlessEJB6:bg().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-bg");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-bg")    
    public void bg(Timer t) {
        System.out.println("in SlessEJB6:bg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);
                           
        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6_Timer-bg");
    }

    // overloaded version of interceptor-binding used in ejb-jar.xml to
    // mark all methods with name cg as having aroundtimeout C,G
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-cg")    
    public void cg() {
        System.out.println("in SlessEJB6:cg().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-cg");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-cg")    
    public void cg(Timer t) {
        System.out.println("in SlessEJB6:cg(Timer t).  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-cg");
    }

    // Kind of like ag(), in that dg() is overloaded, but it's the 
    // signature that has a parameter that is assigned aroundtimeout using
    // @Interceptor.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-dg")    
    public void dg() {
        System.out.println("in SlessEJB6:dg().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6-dg");
    }

    @Interceptors({InterceptorD.class, InterceptorG.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-dg")    
    public void dg(Timer t) {
        System.out.println("in SlessEJB6:dg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);
                           
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-dg");
    }



    // Like dg(), except that dg(Timer t) is assigned its interceptor
    // chain through ejb-jar.xml
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-eg")    
    public void eg() {
        System.out.println("in SlessEJB6:eg().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6-eg");
    }

    // Called through interface only. InterceptorG has
    // 2 separate aroundXXX calls.
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void noaroundtimeout() {
        System.out.println("in SlessEJB6:noaroundtimeout().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundInvokeCalled = false;
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-eg")    
    public void eg(Timer t) {
        System.out.println("in SlessEJB6:eg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);
                           
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-eg");
    }

    public void verify() {
        Common.checkResults("SlessEJB6", EXPECTED);
            System.out.println("verifying Sless7 tests");

            sless7.verify();
            sless7.dc();

    }

}
    

