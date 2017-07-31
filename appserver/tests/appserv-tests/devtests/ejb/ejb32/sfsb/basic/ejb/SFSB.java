/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import javax.ejb.*;
import javax.persistence.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;

@Stateful
@LocalBean
public class SFSB extends SuperSFSB implements Hello {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void init() {
        System.out.println("In SFSB::init()");
        FooEntity fe = new FooEntity("BAR");
        em.persist(fe);
        System.out.println("Done SFSB::init()");
    }
    
    public String test(String value, int count) throws EJBException {
	System.out.println("In SFSB::test()");
        Query q = em.createQuery("SELECT f FROM FooEntity f WHERE f.name=:name");
        q.setParameter("name", value);
        java.util.List result = q.getResultList(); 
        if (result.size() != count) 
            throw new EJBException("ERROR: Found " + result.size() + " FooEntity named " + value + ", not expected " + count);

	return "Found " + result.size() + " FooEntity named " + value;
    }

    @Remove
    public void testRemove() {
        System.out.println("In SFSB::testRemove()");
    }

    @PreDestroy
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void destroy() {
        System.out.println("In SFSB::destroy()");
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction status is not STATUS_ACTIVE: " + r.getTransactionStatus());
            }
            FooEntity fe = new FooEntity("FOO");
            em.persist(fe);
            System.out.println("Done SFSB::destroy()");
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }



}
