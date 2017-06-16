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

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;
import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

import java.util.Collection;
import java.util.Iterator;

@Stateless
@Remote({Sless.class})
public class SlessEJB2 implements Sless, SessionBean
{

    private SessionContext sc_ = null;
    private boolean postConstructCalled = false;

    @PostConstruct
    public void ejbCreate() {
        System.out.println("In SlessEJB2::ejbCreate()");        
        postConstructCalled = true;
    }

    public String hello() {
        System.out.println("In SlessEJB2:hello()");

        if( !postConstructCalled ) {
            throw new EJBException("post construct wasn't called");
        }

        try {
            sc_.getUserTransaction();
            throw new EJBException("should have gotten exception when accessing SessionContext.getUserTransaction()");
        } catch(IllegalStateException ise) {
            System.out.println("Got expected exception when accessing SessionContext.getUserTransaction()");
        }

        return "hello from SlessEJB2";
    }

    public String hello2() throws javax.ejb.CreateException {
        throw new javax.ejb.CreateException();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getId() {

        try {
            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
                new InitialContext().lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in SlessEJB2::getId()");
            } else {
                throw new EJBException("Invalid tx status for TX_NOT_SUPPORTED" +
                                       " method SlessEJB2::getId() : " + txStatus);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        return "SlessEJB2";
    }
    
    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB2::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB2::roundTrip2 " + 
                           collectionOfSless);

        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());  
        }

        return collectionOfSless;
    }

    public void setSessionContext(SessionContext sc)
    {
        sc_ = sc;
    }

    public void ejbRemove() 
    {}

    public void ejbActivate() 
    {}

    public void ejbPassivate()
    {}


}
