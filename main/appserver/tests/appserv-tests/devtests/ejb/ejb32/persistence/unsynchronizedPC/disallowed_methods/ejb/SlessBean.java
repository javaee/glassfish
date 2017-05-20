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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.TransactionRequiredException;

@Stateless
public class SlessBean implements Tester{
    @PersistenceContext(unitName="lib/unsynchpc_disallowed_methods-par.jar#em",
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;
    
    @Override
    public Map<String, Boolean> doTest() {
        Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
        
        /*
         * SPEC: The application's use of queries with pessimistic locks,
         * bulk update or delete queries, etc. result in the provider 
         * throwing the TransactionRequiredException. 
         */
        //Query with pessimistic locks
        Query query = em.createQuery("SELECT OBJECT(p) FROM Person p WHERE p.name='Tom'");
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        try {
            query.getSingleResult();
            System.out.println("testPessimisticLockQuery with on expception thrown");
            resultMap.put("TestPessimisticLockQuery", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestPessimisticLockQuery", true);
        } catch (Exception e) {
            System.out.println("testPessimisticLockQuery with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestPessimisticLockQuery", false);
        }
        
        //Bulk update
        Query updateQuery = em.createQuery("UPDATE Person p SET p.name='Jim' WHERE p.id > 100");
        try {
            updateQuery.executeUpdate();
            System.out.println("testBulkUpdate with on expception thrown");
            resultMap.put("TestBulkUpdate", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestBulkUpdate", true);
        } catch (Exception e) {
            System.out.println("testBulkUpdate with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestBulkUpdate", false);
        }
        
        //Bulk delete
        Query deleteQuery = em.createQuery("DELETE FROM Person p WHERE p.name = 'Jim'");
        try {
            deleteQuery.executeUpdate();
            System.out.println("testBulkDelete with no expception thrown");
            resultMap.put("TestBulkDelete", false);
        } catch (TransactionRequiredException tre) {
            resultMap.put("TestBulkDelete", true);
        } catch (Exception e) {
            System.out.println("testBulkDelete with unexpected expception thrown");
            e.printStackTrace();
            resultMap.put("TestBulkDelete", false);
        }
        
        
        /*
         * SPEC: The application is permitted to invoke the persist, merge, remove, 
         * and refresh entity lifecycle operations on an entity manager of type 
         * SynchronizationType.UNSYNCHRONIZED independent of whether the 
         * persistence context is joined to the current transaction.
         */
        Person person2 = new Person("Jack");
        try {
            em.persist(person2);
            resultMap.put("TestPersist", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestPersist", false);
        }
        
        try {
            em.merge(new Person("Lily"));
            resultMap.put("TestMerge", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestMerge", false);
        }
        
        Person person3 = em.find(Person.class, 2);
        person3.setName("Lucy2");
        try {
            em.refresh(person3);
            resultMap.put("TestRefresh", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestRefresh", false);
        }
        
        try {
            em.remove(person2);
            resultMap.put("TestRemove", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("TestRemove", false);
        }
        
        return resultMap;
    }
}
