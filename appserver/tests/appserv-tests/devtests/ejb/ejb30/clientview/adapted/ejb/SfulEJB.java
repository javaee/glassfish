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

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.annotation.PreDestroy;

@Stateful
@Remote({SfulRemoteBusiness.class, SfulRemoteBusiness2.class})
@Local({SfulBusiness.class, SfulBusiness2.class})
@RemoteHome(SfulRemoteHome.class)
@LocalHome(SfulHome.class)
    public class SfulEJB implements SfulBusiness, SfulBusiness2, SfulRemoteBusiness, SfulRemoteBusiness2, java.io.Serializable
{
    int state = 0;

    private @Resource SessionContext ctx;

    public void foo() {
        System.out.println("In SfulEJB::SfulBusiness2::foo()");

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SfulBusiness2.class ||
            clazz == SfulRemoteBusiness2.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        } else {
            throw new EJBException("Wrong invoked business interface = " +
                                   clazz);
        }
    }

    public void bar() {
        System.out.println("In SfulEJB::SfulBusiness2::bar()");
    }

    public SfulBusiness2 getSfulBusiness2() {

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SfulBusiness.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        } else {
            throw new EJBException("Wrong invoked business interface = " +
                                   clazz);
        }

        try {
            ctx.getBusinessObject(java.io.Serializable.class);
            throw new EJBException("Should have gotten exception when " +
                                   "calling getBusinessObject with invalid " +
                                   "business interface");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception for invalid call " +
                               "to ctx.getBusinessObject()");
        }

        try {
            ctx.getBusinessObject(null);
            throw new EJBException("Should have gotten exception when " +
                                   "calling getBusinessObject with null " +
                                   "business interface");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception for invalid call " +
                               "to ctx.getBusinessObject()");
        }

        return (SfulBusiness2) ctx.getBusinessObject(SfulBusiness2.class);
    }

    @Init
    public void adaptedCreate() {
        System.out.println("In SfulEJB::adaptedCreate method");

        try {
            ctx.getInvokedBusinessInterface();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when invoking " +
                               "ctx.getInvokedBusinessInterface() from an " +
                               "adapted create method.");
        }

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());
    }

    @Init("create")
    public void adaptedCreate1(int ignore) {
        // Ignore input parameter.  client will use resulting unset state to
        // ensure correct mapping of @Init

        System.out.println("In SfulEJB::adaptedCreate1(int ignore) method. " 
                           + "**ignoring** state=" + state);

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());

    }

    @Init("createFoo")
    public void adaptedCreate2(int argument1) {
        state = argument1;
        System.out.println("In SfulEJB::adaptedCreate2(int arg) method " 
                           + "state=" + state);

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());

    }

    public int getState() {
        try {
            ctx.getInvokedBusinessInterface();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when invoking " +
                               "ctx.getInvokedBusinessInterface() from a " +
                               "Remote component interface method.");
        }

        return state;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void notSupported() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void required() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void requiresNew() {}

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void mandatory() {}

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void never() {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void supports() {}

    @Remove
    public void remove() {}

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException) 
        throws Exception {
        if( throwException ) {
            throw new Exception("exception from retainIfException. " +
                                "throwException = " + throwException);
        }
    }

    @PreDestroy
    public void beforeDestroy() {
        System.out.println("In @PreDestroy callback in SfulEJB");
    }
}
