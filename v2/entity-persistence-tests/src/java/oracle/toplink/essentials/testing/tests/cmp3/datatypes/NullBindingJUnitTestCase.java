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
package oracle.toplink.essentials.testing.tests.cmp3.datatypes;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.datatypes.*;
import oracle.toplink.essentials.sessions.DatabaseSession;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>
 * <b>Purpose</b>: Test binding of null values to primitive wrapper and LOB type fields
 * in TopLink's JPA implementation.
 * <p>
 * <b>Description</b>: This class creates a test suite and adds tests to the
 * suite. The database gets initialized prior to the test methods.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for binding of null values to primitive wrapper and LOB type fields
 * in TopLink's JPA implementation.
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.datatypes.DataTypesTableCreator
 */
public class NullBindingJUnitTestCase extends JUnitTestCase {
    private static int wrapperId;
    private static int byteArrayId;
    private static int pByteArrayId;
    private static int characterArrayId;
    private static int pCharArrayId;

    public NullBindingJUnitTestCase() {
        super();
    }

    public NullBindingJUnitTestCase(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Null Binding DataTypes");
        suite.addTest(new NullBindingJUnitTestCase("testCreateWrapperTypes"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyBigDecimal"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyBigInteger"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyBoolean"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyByte"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyCharacter"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyShort"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyInteger"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyLong"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyFloat"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyDouble"));
        suite.addTest(new NullBindingJUnitTestCase("testNullifyString"));
        suite.addTest(new NullBindingJUnitTestCase("testCreateByteArrayType"));
        suite.addTest(new NullBindingJUnitTestCase("testCreatePrimitiveByteArrayType"));
        suite.addTest(new NullBindingJUnitTestCase("testCreateCharacterArrayType"));
        suite.addTest(new NullBindingJUnitTestCase("testCreateCharArrayType"));

        return new TestSetup(suite) {

            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new DataTypesTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                removeWrapperTypes();
                removeByteArrayType();
                removePrimitiveByteArrayType();
                removeCharacterArrayType();
                removeCharArrayType();

                clearCache();
            }
        };
    }

    /**
     * Creates the WrapperTypes instance used in later tests.
     */
    public void testCreateWrapperTypes() {
        EntityManager em = createEntityManager();
        WrapperTypes wt;

        em.getTransaction().begin();
        wt = new WrapperTypes(BigDecimal.ZERO, BigInteger.ZERO, Boolean.FALSE,
                new Byte("0"), '\0', new Short("0"),
                0, 0L, new Float(0.0), 0.0, "A String");
        em.persist(wt);
        wrapperId = wt.getId();
        em.getTransaction().commit();
    }

    /**
     */
    public void testNullifyBigDecimal() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setBigDecimalData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting BigDecimal field to null", wt2.getBigDecimalData() == null);
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
    public void testNullifyBigInteger() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setBigIntegerData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting BigInteger field to null", wt2.getBigIntegerData() == null);
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
    public void testNullifyBoolean() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setBooleanData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Boolean field to null", wt2.getBooleanData() == null);
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
    public void testNullifyByte() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setByteData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Byte field to null", wt2.getByteData() == null);
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
    public void testNullifyCharacter() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setCharacterData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Character field to null", wt2.getCharacterData() == null);
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
    public void testNullifyShort() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setShortData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Short field to null", wt2.getShortData() == null);
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
    public void testNullifyInteger() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setIntegerData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Integer field to null", wt2.getIntegerData() == null);
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
    public void testNullifyLong() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setLongData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Long field to null", wt2.getLongData() == null);
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
    public void testNullifyFloat() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setFloatData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Float field to null", wt2.getFloatData() == null);
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
    public void testNullifyDouble() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setDoubleData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting Double field to null", wt2.getDoubleData() == null);
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
    public void testNullifyString() {
        EntityManager em = createEntityManager();
        Query q;
        WrapperTypes wt, wt2;

        try {
            em.getTransaction().begin();
            wt = em.find(WrapperTypes.class, wrapperId);
            wt.setStringData(null);
            em.getTransaction().commit();
            q = em.createQuery("SELECT wt FROM WrapperTypes wt WHERE wt.id = " + wrapperId);
            wt2 = (WrapperTypes) q.getSingleResult();
            assertTrue("Error setting String field to null", wt2.getStringData() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Creates a ByteArrayType instance.
     * Note that the Byte[] field is null.
     */
    public void testCreateByteArrayType() {
        EntityManager em = createEntityManager();
        Query q;
        ByteArrayType bat, bat2;

        try {
            em.getTransaction().begin();
            bat = new ByteArrayType();
            em.persist(bat);
            byteArrayId = bat.getId();
            em.getTransaction().commit();
            q = em.createQuery("SELECT bat FROM ByteArrayType bat WHERE bat.id = " + byteArrayId);
            bat2 = (ByteArrayType) q.getSingleResult();
            assertTrue("Error setting Byte[] field to null", bat2.getByteArrayData() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Creates a PrimitiveByteArrayType instance.
     * Note that the byte[] field is null.
     */
    public void testCreatePrimitiveByteArrayType() {
        EntityManager em = createEntityManager();
        Query q;
        PrimitiveByteArrayType pbat, pbat2;

        try {
            em.getTransaction().begin();
            pbat = new PrimitiveByteArrayType();
            em.persist(pbat);
            pByteArrayId = pbat.getId();
            em.getTransaction().commit();
            q = em.createQuery("SELECT pbat FROM PrimitiveByteArrayType pbat WHERE pbat.id = " + pByteArrayId);
            pbat2 = (PrimitiveByteArrayType) q.getSingleResult();
            assertTrue("Error setting byte[] field to null", pbat2.getPrimitiveByteArrayData() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Creates a CharacterArrayType instance.
     * Note that the Character[] field is null.
     */
    public void testCreateCharacterArrayType() {
        EntityManager em = createEntityManager();
        Query q;
        CharacterArrayType cat, cat2;

        try {
            em.getTransaction().begin();
            cat = new CharacterArrayType();
            em.persist(cat);
            characterArrayId = cat.getId();
            em.getTransaction().commit();
            q = em.createQuery("SELECT cat FROM CharacterArrayType cat WHERE cat.id = " + characterArrayId);
            cat2 = (CharacterArrayType) q.getSingleResult();
            assertTrue("Error setting Character[] field to null", cat2.getCharacterArrayData() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Creates a CharArrayType instance.
     * Note that the char[] field is null.
     */
    public void testCreateCharArrayType() {
        EntityManager em = createEntityManager();
        Query q;
        CharArrayType pcat, pcat2;

        try {
            em.getTransaction().begin();
            pcat = new CharArrayType();
            em.persist(pcat);
            pCharArrayId = pcat.getId();
            em.getTransaction().commit();
            q = em.createQuery("SELECT pcat FROM CharArrayType pcat WHERE pcat.id = " + pCharArrayId);
            pcat2 = (CharArrayType) q.getSingleResult();
            assertTrue("Error setting char[] field to null", pcat2.getPrimitiveCharArrayData() == null);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * Removes the WrapperTypes instance used in the tests.
     */
    public static void removeWrapperTypes() {
        EntityManager em = createEntityManager();
        WrapperTypes wt;

        em.getTransaction().begin();
        wt = em.find(WrapperTypes.class, wrapperId);
        em.remove(wt);
        em.getTransaction().commit();
    }

    /**
     * Removes the ByteArrayType instance used in the tests.
     */
    public static void removeByteArrayType() {
        EntityManager em = createEntityManager();
        ByteArrayType bat;

        em.getTransaction().begin();
        bat = em.find(ByteArrayType.class, byteArrayId);
        em.remove(bat);
        em.getTransaction().commit();
    }

    /**
     * Removes the PrimitiveByteArrayType instance used in the tests.
     */
    public static void removePrimitiveByteArrayType() {
        EntityManager em = createEntityManager();
        PrimitiveByteArrayType pbat;

        em.getTransaction().begin();
        pbat = em.find(PrimitiveByteArrayType.class, pByteArrayId);
        em.remove(pbat);
        em.getTransaction().commit();
    }

    /**
     * Removes the CharacterArrayType instance used in the tests.
     */
    public static void removeCharacterArrayType() {
        EntityManager em = createEntityManager();
        CharacterArrayType cat;

        em.getTransaction().begin();
        cat = em.find(CharacterArrayType.class, characterArrayId);
        em.remove(cat);
        em.getTransaction().commit();
    }

    /**
     * Removes the CharArrayType instance used in the tests.
     */
    public static void removeCharArrayType() {
        EntityManager em = createEntityManager();
        CharArrayType pcat;

        em.getTransaction().begin();
        pcat = em.find(CharArrayType.class, pCharArrayId);
        em.remove(pcat);
        em.getTransaction().commit();
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
