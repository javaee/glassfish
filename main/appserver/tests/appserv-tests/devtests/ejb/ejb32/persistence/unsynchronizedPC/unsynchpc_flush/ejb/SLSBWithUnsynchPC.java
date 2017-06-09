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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.unsynchpc_flush.ejb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;
import javax.persistence.TransactionRequiredException;


@Stateless
public class SLSBWithUnsynchPC implements Tester {
    @PersistenceContext(unitName="lib/unsynchpc_flush-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    @Override
    public boolean flushBeforeJoin() {
        System.out.println("I am in flushBeforeJoin");
        
        Person p = new Person("Tom");
        em.persist(p);
        
        try {
            System.out.println("is jonined before flush: " + em.isJoinedToTransaction());
            //flush before the unsynchronized PC join transaction
            em.flush();
            System.out.println("flushed with no exceptions thrown");
            return false;
        } catch (TransactionRequiredException tre) {
            /* SPEC: A persistence context of type SynchronizationType.UNSYNCHRONIZED must 
             * not be flushed to the database unless it is joined to a transaction
             */
            //Expected exception thrown
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //Unexpected exception type
            return false;
        }
    }
    
    @Override
    public boolean flushAfterJoin() {
        System.out.println("I am in flushAfterJoin");

        Person p = new Person("Tom2");
        em.persist(p);

        System.out.println("is jonined before joinTransaction: " + em.isJoinedToTransaction());
        //Join transaction
        em.joinTransaction();
        try {
            System.out.println("is jonined before flush: " + em.isJoinedToTransaction());
            /* SPEC: After the persistence context has been joined to a transaction, 
             * changes in a persistence context can be flushed to the database explicitly 
             * by the application
             */
            em.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void autoFlushByProvider(String name) {
        System.out.println("I am in autoFlushByProvider");
        
        Person p = new Person(name);
        em.persist(p);
        
        System.out.println("is jonined before joinTransaction: " + em.isJoinedToTransaction());
        //Join transaction
        em.joinTransaction();
    }
    
    @Override
    public boolean isPersonFound(String name) {
        System.out.println("I am in isPersonFound");
        return em.find(Person.class, name) != null;
    }
}
