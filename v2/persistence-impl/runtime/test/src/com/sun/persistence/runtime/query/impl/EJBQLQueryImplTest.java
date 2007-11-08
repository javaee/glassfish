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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.FlushModeType;
import javax.persistence.Query.TemporalType;

import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;

import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.persistence.runtime.query.TemporalParameter;

import junit.framework.TestCase;

/**
 * 
 * @author Dave Bristor
 */
public class EJBQLQueryImplTest extends TestCase {
    private static final String qstr =
        "select e from Employee e where e.empId = ?1";
    
    // Parameter keys, by name and position.
    private static final String paramName = "hello";
    private static final int paramPos = 3;
    private static final String dateName = "fred";
    private static final int datePos = 5;
    private static final int calendarPos = 2;
    private static final String calendarName = "wilma";
 
    // Parameter value.
    private static final String value = "world";
    
    private static final int maxResults = 7;
    private static final int firstResult = 4;
    
    private static final Date date = new Date();
    private static final Calendar calendar =
        new GregorianCalendar(2004, 2, 14);
    
    /** Used by tests that check "set" operations. */
    private EJBQLQueryImpl qq;
    
    /** Used by tests that check "get" operations; initialized in setUp(). */
    private EJBQLQueryImpl q;
    private EJBQLQueryImpl nq; // for queries with named parameters

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        // Initialize qq for testSet* operations.
        qq = new EJBQLQueryImpl(qstr, null);

        // Initialize q, nq for testGet* operations.
        q = new EJBQLQueryImpl(qstr, null);
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
        q.setHint(paramName, value);
        q.setFlushMode(FlushModeType.COMMIT);

        nq = new EJBQLQueryImpl(qstr, null);

        // Initialize q with some positional parameters
        q.setParameter(paramPos, value);
        q.setParameter(datePos, date, TemporalType.DATE);
        q.setParameter(calendarPos, calendar, TemporalType.TIME);

        // Initialize nq with some named parameters
        nq.setParameter(paramName, value);
        nq.setParameter(dateName, date, TemporalType.DATE);
        nq.setParameter(calendarName, calendar, TemporalType.TIME);
    }

    /*
     * Test constructor.
     */
    
    public void testCreateFromString() {
        assertEquals(qstr, q.getQuery());
    }
    
    public void testCreateFromQuery() {
        QueryInternal x = new EJBQLQueryImpl(q);
        assertEquals(qstr, x.getQuery());
    }

    /*
     * Test setXYZ methods.
     */
    
    public void testSetFirstResult() {
        try {
            qq.setFirstResult(-1);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex){
            // expected
        } catch (Throwable ex){
            fail("Caught unexpected exception " + ex);
        }
        qq.setFirstResult(0);
        qq.setFirstResult(1);
    }
    
    public void testSetMaxResults() {
        try {
            qq.setMaxResults(-1);
            fail();
        } catch (IllegalArgumentException ex){
            // expected
        } catch (Throwable ex){
            fail("Caught unexpected exception " + ex);
        }
        qq.setMaxResults(0);
        qq.setMaxResults(1);
    }

    public void testSetHint() {
        try {
            qq.setHint(null, value);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        qq.setHint(paramName, value);
        qq.setHint(paramName, null);
    }

    public void testSetParameterName() {
        try {
            qq.setParameter(null, value);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        qq.setParameter(paramName, value);
        qq.setParameter(paramName, null);
    }

    public void testSetParameterNameDate() {
        try {
            qq.setParameter(null, date, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        qq.setParameter(paramName, date, TemporalType.TIME);
    }

    public void testSetParameterNameCalendar() {
        try {
            qq.setParameter(null, calendar, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        qq.setParameter(paramName, calendar, TemporalType.TIMESTAMP);
    }

    public void testSetParameterPos() {
        try {
            qq.setParameter(-1, value);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        try {
            qq.setParameter(0, value);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }        
        qq.setParameter(1, value);
        qq.setParameter(99, null);
    }
    
    public void testSetParameterPosDate() {
        try {
            qq.setParameter(-1, date, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        try {
            qq.setParameter(0, date, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }        
        qq.setParameter(1, date, TemporalType.TIME);
        qq.setParameter(99, date, TemporalType.TIMESTAMP);
    }
    
    public void testSetParameterPosCalendar() {
        try {
            qq.setParameter(-1, calendar, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
        try {
            qq.setParameter(0, calendar, TemporalType.DATE);
            fail("Didn't throw expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }        
        qq.setParameter(1, calendar, TemporalType.TIME);
        qq.setParameter(99, calendar, TemporalType.TIMESTAMP);
    }

    /**
     * Test that it is <em>not</em> possible to set a named parameter
     * and then a positional parameter.  EJB Persistence spec section 3.6.4.
     */
    public void testSetNamedAndPositionalParameters() {
        try {
            qq.setParameter(paramName, value);
            qq.setParameter(paramPos, value);
            fail("Didn't throw exception when setting a named and then a positional param");
        } catch (IllegalArgumentException ex) {
            // expected
            // TBD: Check for exact nature of exception
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
    }

    /**
     * Test that it is <em>not</em> possible to set a positional parameter
     * and then a named parameter.  EJB Persistence spec section 3.6.4.
     */
    public void testSetPositionalAndNamedParameters() {
        try {
            qq.setParameter(paramPos, value);
            qq.setParameter(paramName, value);
            fail("Didn't throw exception when setting a positional and then a named param");
        } catch (IllegalArgumentException ex) {
            // expected
            // TBD: Check for exact nature of exception
        } catch (Throwable ex) {
            fail("Caught unexpected exception " + ex);
        }
    }

    /*
     * Test getXYZ methods
     */
    
    public void testGetQuery() {
        assertEquals(qstr, q.getQuery());
    }

    public void testGetMaxResults() {
        assertEquals(maxResults, q.getMaxResults());
    }

    public void testGetFirstResult() {
        assertEquals(firstResult, q.getFirstResult());
    }

    public void testGetHint() {
        Object v = q.getHint(paramName);
        assertSame(v, value);
    }

    public void testIsHint() {
        assertTrue(q.isHint(paramName));
        assertFalse(q.isHint(value));
    }

    public void testGetParameterName() {
        Object v = nq.getParameter(paramName);
        assertSame(value, v);
        
        v = nq.getParameter(dateName);
        TemporalParameter tp = (TemporalParameter) v;
        Date d = (Date)(tp.getValue());

        assertSame(date, d);
        assertEquals(TemporalType.DATE, tp.getTemporalType());
        
        v = nq.getParameter(calendarName);
        tp = (TemporalParameter) v;
        Calendar c = (Calendar)(tp.getValue());
        assertSame(calendar, c);
        assertEquals(TemporalType.TIME, tp.getTemporalType());
    }

    public void testGetParameterPos() {
        Object v = q.getParameter(paramPos);
        assertSame(value, v);
        
        v = q.getParameter(datePos);
        TemporalParameter tp = (TemporalParameter) v;
        Date d = (Date)(tp.getValue());
        assertSame(date, d);
        assertEquals(TemporalType.DATE, tp.getTemporalType());
        
        v = q.getParameter(calendarPos);
        tp = (TemporalParameter) v;
        Calendar c = (Calendar)(tp.getValue());
        assertSame(calendar, c);
        assertEquals(TemporalType.TIME, tp.getTemporalType());
    }
    
    public void testGetParameterType() {
        JavaModel model = RuntimeJavaModelFactory.getInstance().getJavaModel(null);
        
        JavaType v = q.getParameterType("?" + paramPos, model);
        assertTrue(v == model.getJavaType(value.getClass()));
        
        v = nq.getParameterType(":" + paramName, model);
        assertSame(v, model.getJavaType(value.getClass()));
    }

    public void testGetFlushMode() {
        assertEquals(q.getFlushMode(), FlushModeType.COMMIT);
    }
    
    public void testIsEJBQLQuery() {
        assertTrue(q.isEJBQLQuery());
    }

    /*
     * Test query execution methods
     */
    
    /**
     * Test of getResultList method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    /*public void testGetResultList() {
     }*/

    /**
     * Test of getSingleResult method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    /*public void testGetSingleResult() {
     }*/

    /**
     * Test of executeUpdate method, of class com.sun.persistence.runtime.query.impl.NativeQueryImpl.
     */
    /*public void testExecuteUpdate() {
     }*/
}
