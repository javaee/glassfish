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
import javax.ejb.SessionSynchronization;
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
public class SfulEJB2 implements Sful2, SessionSynchronization
{

    // use some package-local mutable static state to check whether
    // session synch callbacks are called correctly for @Remove methods.
    // This provides a simple way to check the results since the bean 
    // instance is no longer available to the caller.  The caller must
    // always at most one SFSBs of this bean type at a time for this
    // to work.  
    static boolean afterBeginCalled = false;
    static boolean beforeCompletionCalled = false;
    static boolean afterCompletionCalled = false;

    private @Resource SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB2:hello()");

        return "hello";
    }

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           " removeRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeNotRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           "removeNotRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeMethodThrowSysException(boolean throwException) {

        System.out.println("In SfulEJB2 " + 
                           "removeMethodThrowSysException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new EJBException
                ("throwing system exception from @Remove method");
        }
    }

    public void afterBegin() {

        afterBeginCalled = true;
        beforeCompletionCalled = false;
        afterCompletionCalled = false;
        
        System.out.println("In SfulEJB2::afterBegin()");
    }

    public void beforeCompletion() {
        System.out.println("In SfulEJB2::beforeCompletion()");
        beforeCompletionCalled = true;
    }

    public void afterCompletion(boolean committed) {
        afterCompletionCalled = true;
        System.out.println("In SfulEJB2::afterCompletion()");
    }

}
