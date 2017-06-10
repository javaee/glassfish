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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import javax.ejb.Stateless;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


// Each interceptor list must at least have InterceptorG for
// aroundInvokeCalled state to be set correctly.

@Stateless
@ExcludeDefaultInterceptors
public class SlessEJB6 implements Sless6
{
    boolean aroundInvokeCalled = false;

    
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void ag() {
        System.out.println("in SlessEJB6:ag().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void ag(int param1) {
        System.out.println("in SlessEJB6:ag(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);
                           
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    // bg() (but not bg(param)) marked through ejb-jar.xml 
    // as having interceptors B,G
    public void bg() {
        System.out.println("in SlessEJB6:bg().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void bg(int param1) {
        System.out.println("in SlessEJB6:bg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);
                           
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    // overloaded version of interceptor-binding used in ejb-jar.xml to
    // mark all methods with name cg as having interceptors C,G
    public void cg() {
        System.out.println("in SlessEJB6:cg().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void cg(int param1) {
        System.out.println("in SlessEJB6:cg(int).  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void cg(String param1, double param2) {
        System.out.println("in SlessEJB6:cg(String, double).  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }


    // Kind of like ag(), in that dg() is overloaded, but it's the 
    // signature that has a parameter that is assigned interceptors using
    // @Interceptor.
    public void dg() {
        System.out.println("in SlessEJB6:dg().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    @Interceptors({InterceptorD.class, InterceptorG.class})
    public void dg(int param1) {
        System.out.println("in SlessEJB6:dg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);
                           
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }



    // Like dg(), except that dg(int param) is assigned its interceptor
    // chain through ejb-jar.xml
    public void eg() {
        System.out.println("in SlessEJB6:eg().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    public void eg(int param1) {
        System.out.println("in SlessEJB6:eg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);
                           
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    

}
    

