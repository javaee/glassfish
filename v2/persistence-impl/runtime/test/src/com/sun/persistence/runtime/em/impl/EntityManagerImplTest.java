/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.em.impl;

import junit.framework.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;

/**
 *
 * @author Dave Bristor
 */
public class EntityManagerImplTest extends TestCase {
    /** Sample EJBQL query. */
    private static final String ejbqlQueryString
        = "select object(d) from Department d where d.deptid = ?1";
    
    /** Sample SQL query. */
    private static final String nativeQueryString
        = "SELECT * FROM DEPARTMENT WHERE ID = ?1";
    
    /** For testing native query result class */
    static class DepartmentResults { }

    /** Unit under test. */
    private EntityManager em = new EntityManagerImpl(
        new MockEntityManagerFactoryInternal(),
        new MockPersistenceManagerInternal());
    
    public EntityManagerImplTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EntityManagerImplTest.class);
        
        return suite;
    }

    /**
     * Test of persist method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testPersist() {
    }

    /**
     * Test of merge method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testMerge() {
    }

    /**
     * Test of remove method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testRemove() {
    }

    /**
     * Test of find method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testFindStringObject() {
    }
    
    /**
     * Test of find method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testFindClassObject() {
    }

    /**
     * Test of flush method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testFlush() {
    }

    /**
     * Test of createQuery method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCreateQuery() {
        Query q = em.createQuery(ejbqlQueryString);
        QueryInternal qi = (QueryInternal) q;
        assertEquals(ejbqlQueryString, qi.getQuery());
    }

    /**
     * Test of createNamedQuery method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCreateNamedQuery() {
    }

    /**
     * Test of createNativeQuery method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCreateNativeQueryString() {
        Query q = em.createNativeQuery(nativeQueryString);
        QueryInternal qi = (QueryInternal) q;
        assertEquals(nativeQueryString, qi.getQuery());
    }

    /**
     * Test of createNativeQuery method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCreateNativeQueryStringClass() {
        Query q = em.createNativeQuery(nativeQueryString,
                DepartmentResults.class);
        QueryInternal qi = (QueryInternal) q;
        assertEquals(nativeQueryString, qi.getQuery());
        // XXX TBD assert that result class is OK
    }

    /**
     * Test of createNativeQuery method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCreateNativeQueryStringString() {
           Query q = em.createNativeQuery(nativeQueryString,
                "nameOfResultSetMapping");
        QueryInternal qi = (QueryInternal) q;
        assertEquals(nativeQueryString, qi.getQuery());
        // XXX TBD assert that result set mapping is OK
    }

    /**
     * Test of refresh method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testRefresh() {
    }

    /**
     * Test of contains method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testContains() {
    }

    /**
     * Test of getPersistenceManager method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testGetPersistenceManager() {
    }

    /**
     * Test of currentTransaction method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testCurrentTransaction() {
    }

    /**
     * Test of ensureIsEntity method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testEnsureIsEntity() {
    }

    /**
     * Test of ensureIsNotDetached method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testEnsureIsNotDetached() {
    }

    /**
     * Test of ensureIsNotRemoved method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testEnsureIsNotRemoved() {
    }

    /**
     * Test of ensureHasTransactionContext method, of class com.sun.persistence.runtime.em.impl.EntityManagerImpl.
     */
    public void testEnsureHasTransactionContext() {
    }
}
