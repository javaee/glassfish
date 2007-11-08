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


package com.sun.persistence.runtime.query.impl;

import java.util.GregorianCalendar;
import java.util.Date;

import javax.persistence.Query.TemporalType;

import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;

import junit.framework.TestCase;

/**
 * This test only checks that those setParameter methods which are not
 * applicable to native queries are implemented correctly.  Other query -
 * related methods are checked in EJBQLQueryImplTest.
 *
 * @author db13166
 */
public class NativeQueryImplTest extends TestCase {
    // Yes, this is invalid.  But we don't check for that when the query is
    // created.
    private static final String qstr =
        "select * from Employee e where e.empId = :id";

    private static final String expectedMsg = "JDO75602: Invalid named parameter 'id': Named parameters are not supported for native queries.";

    private NativeQueryImpl q;

    public NativeQueryImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        q = new NativeQueryImpl(qstr, new MockPersistenceManagerInternal());
    }

    /**
     * Test of setParameter method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    public void testSetParameterStringObject() {
        try {
            q.setParameter("id", new Integer(1));
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals(expectedMsg, ex.getMessage());
        } catch (Throwable ex) {
            fail("Instead of IllegalArgumentException, got " + ex);
        }
    }

    /**
     * Test of setParameter method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    public void testSetParameterStringDateTemporalType() {
        try {
            q.setParameter("id", new Date(), TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals(expectedMsg, ex.getMessage());
        } catch (Throwable ex) {
            fail("Instead of IllegalArgumentException, got " + ex);
        }
    }

    /**
     * Test of setParameter method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    public void testSetParameterStringCalendarTemporalType() {
        try {
            q.setParameter("id", new GregorianCalendar(), TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals(expectedMsg, ex.getMessage());
        } catch (Throwable ex) {
            fail("Instead of IllegalArgumentException, got " + ex);
        }
    }

    /**
     * Test of isEJBQLQuery method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    public void testIsEJBQLQuery() {
        if (q.isEJBQLQuery()) {
            fail("Query mistakenly that it is EJBQLQueryImpl");
        }
    }
}
