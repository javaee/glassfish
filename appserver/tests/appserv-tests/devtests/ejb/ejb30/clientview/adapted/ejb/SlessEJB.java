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
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.naming.InitialContext;

@Stateless
@Remote({SlessRemoteBusiness.class, SlessRemoteBusiness2.class})
@Local({SlessBusiness.class, SlessBusiness2.class})
public class SlessEJB implements SlessBusiness, SlessBusiness2, SlessRemoteBusiness, SlessRemoteBusiness2
{

    private @Resource SessionContext ctx;

    public void foo() {
        System.out.println("In SlessEJB::SlessBusiness2::foo()");
    }

    public void bar() {
        System.out.println("In SlessEJB::SlessBusiness2::bar()");
    }

    public SlessBusiness2 getSlessBusiness2() {

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SlessBusiness.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        }

        return (SlessBusiness2) ctx.getBusinessObject(SlessBusiness2.class);
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

    /**
     * Business method declared on a super interface extended by both
     * remote and local business interface.  Test that tx attribute is
     * set appropriately for local vs. remote.  
     */
    public void sharedRemoteLocalBusinessSuper(boolean expectTx) {

        boolean hasTx = false;

        try {

            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
		//  TODO check this, it worked inV2              new InitialContext().lookup("java:pm/TransactionManager");
		              new InitialContext().lookup("java:appserver/TransactionManager");

            // Use an implementation-specific check whether there is a tx.
            // A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.

            hasTx = ( tm.getStatus() != Status.STATUS_NO_TRANSACTION );

        } catch(Exception e) {
            throw new EJBException(e);
        }
            
        if( expectTx && hasTx ) {
            System.out.println("Successfully verified tx");
        } else if( !expectTx && !hasTx ) {
            System.out.println("Successfully verified there is no tx");
        } else {
            throw new EJBException("Invalid tx status.  ExpectTx = " + 
                                   expectTx + " hasTx = " + hasTx);
        }

    }

}
