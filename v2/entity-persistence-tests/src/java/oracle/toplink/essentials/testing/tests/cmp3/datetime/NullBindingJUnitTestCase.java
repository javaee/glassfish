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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.tests.cmp3.datetime;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.datetime.*;
import oracle.toplink.essentials.sessions.DatabaseSession;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * <p>
 * <b>Purpose</b>: Test binding of null values to temporal type fields
 * in TopLink's JPA implementation.
 * <p>
 * <b>Description</b>: This class creates a test suite and adds tests to the
 * suite. The database gets initialized prior to the test methods.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for binding of null values to temporal type fields
 * in TopLink's JPA implementation.
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.datetime.DateTimeTableCreator
 */
public class NullBindingJUnitTestCase extends JUnitTestCase {
    private static int datetimeId;

    public NullBindingJUnitTestCase() {
        super();
    }

    public NullBindingJUnitTestCase(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Null Binding DateTime");
        suite.addTest(new NullBindingJUnitTestCase("testCreateDateTime"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifySqlDate"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyTime"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyTimestamp"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyUtilDate"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyCalendar"));

        return new TestSetup(suite) {

            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new DateTimeTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                removeDateTime();

                clearCache();
            }
        };
    }

    /**
     * Creates the DateTime instance used in later tests.
     */
    public void testCreateDateTime() {
        EntityManager em = createEntityManager();
        DateTime dt;

        em.getTransaction().begin();
        dt = new DateTime(new java.sql.Date(0), new java.sql.Time(0), new java.sql.Timestamp(0),
                new java.util.Date(0), java.util.Calendar.getInstance());
        em.persist(dt);
        datetimeId = dt.getId();
        em.getTransaction().commit();
    }

    /**
     */
    public void testNullifySqlDate() {
        EntityManager em = createEntityManager();
        Query q;
        DateTime dt, dt2;

        try {
            em.getTransaction().begin();
            dt = em.find(DateTime.class, datetimeId);
            dt.setDate(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT dt FROM DateTime dt WHERE dt.id = " + datetimeId);
            dt2 = (DateTime) q.getSingleResult();
            assertTrue("Error setting java.sql.Date field to null", dt2.getDate() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     */
    public void testNullifyTime() {
        EntityManager em = createEntityManager();
        Query q;
        DateTime dt, dt2;

        try {
            em.getTransaction().begin();
            dt = em.find(DateTime.class, datetimeId);
            dt.setTime(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT dt FROM DateTime dt WHERE dt.id = " + datetimeId);
            dt2 = (DateTime) q.getSingleResult();
            assertTrue("Error setting java.sql.Time field to null", dt2.getTime() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     */
    public void testNullifyTimestamp() {
        EntityManager em = createEntityManager();
        Query q;
        DateTime dt, dt2;

        try {
            em.getTransaction().begin();
            dt = em.find(DateTime.class, datetimeId);
            dt.setTimestamp(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT dt FROM DateTime dt WHERE dt.id = " + datetimeId);
            dt2 = (DateTime) q.getSingleResult();
            assertTrue("Error setting java.sql.Timestamp field to null", dt2.getTimestamp() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     */
    public void testNullifyUtilDate() {
        EntityManager em = createEntityManager();
        Query q;
        DateTime dt, dt2;

        try {
            em.getTransaction().begin();
            dt = em.find(DateTime.class, datetimeId);
            dt.setUtilDate(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT dt FROM DateTime dt WHERE dt.id = " + datetimeId);
            dt2 = (DateTime) q.getSingleResult();
            assertTrue("Error setting java.util.Date field to null", dt2.getUtilDate() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     */
    public void testNullifyCalendar() {
        EntityManager em = createEntityManager();
        Query q;
        DateTime dt, dt2;

        try {
            em.getTransaction().begin();
            dt = em.find(DateTime.class, datetimeId);
            dt.setCalendar(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT dt FROM DateTime dt WHERE dt.id = " + datetimeId);
            dt2 = (DateTime) q.getSingleResult();
            assertTrue("Error setting java.util.Calendar field to null", dt2.getCalendar() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Removes the DateTime instance used in the tests.
     */
    public static void removeDateTime() {
        EntityManager em = createEntityManager();
        DateTime dt;

        em.getTransaction().begin();
        dt = em.find(DateTime.class, datetimeId);
        em.remove(dt);
        em.getTransaction().commit();
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
