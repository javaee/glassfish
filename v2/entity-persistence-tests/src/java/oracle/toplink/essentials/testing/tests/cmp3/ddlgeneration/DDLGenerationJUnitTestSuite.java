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
package oracle.toplink.essentials.testing.tests.cmp3.ddlgeneration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.*;
import oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.schema.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

/**
 * JUnit test case(s) for DDL generation.
 */
public class DDLGenerationJUnitTestSuite extends JUnitTestCase {
    // the persistence unit name which is used in this test suite
    private static final String DDL_PU = "ddlGeneration";

    public DDLGenerationJUnitTestSuite() {
        super();
    }

    public DDLGenerationJUnitTestSuite(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DDLGenerationJUnitTestSuite.class);

        return new TestSetup(suite) {

            protected void setUp() {
                // Trigger DDL generation
                //TODO: Let's add a flag which do not disregard DDL generation errors.
                //TODO: This is required to ensure that DDL generation has succeeded.
                EntityManager em = createEntityManager(DDL_PU);
                em.close();
            }

            protected void tearDown() {
                clearCache(DDL_PU);
            }
        };
    }

    // Test for GF#1392
    // If there is a same name column for the entity and many-to-many table, wrong pk constraint generated.
    public void testDDLPkConstraintErrorIncludingRelationTableColumnName() {
        EntityManager em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {

            CKeyEntityC c = new CKeyEntityC(new CKeyEntityCPK("Manager"));
            em.persist(c);

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            fail("DDL generation may generate wrong Primary Key constraint, thrown:" + e);
        } finally {
            em.close();
        }
    }

    // Test for relationships using candidate(unique) keys
    public void testDDLUniqueKeysAsJoinColumns() {
        CKeyEntityAPK aKey;
        CKeyEntityBPK bKey;
        
        EntityManager em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            CKeyEntityA a = new CKeyEntityA("Wonseok", "Kim");
            long seq = System.currentTimeMillis(); // just to get unique value :-)
            CKeyEntityB b = new CKeyEntityB(new CKeyEntityBPK(seq, "B1209"));
            //set unique keys
            b.setUnq1("u0001");
            b.setUnq2("u0002");

            a.setUniqueB(b);
            b.setUniqueA(a);
            
            em.persist(a);
            em.persist(b);

            em.getTransaction().commit();
            
            aKey = a.getKey();
            bKey = b.getKey();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
        //clearCache(DDL_PU);

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            CKeyEntityA a = em.find(CKeyEntityA.class, aKey);
            assertNotNull(a);

            CKeyEntityB b = a.getUniqueB();
            assertNotNull(b);

            assertEquals(b.getUnq1(), "u0001");
            assertEquals(b.getUnq2(), "u0002");

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
        //clearCache(DDL_PU);

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {

            CKeyEntityB b = em.find(CKeyEntityB.class, bKey);
            assertNotNull(b);

            CKeyEntityA a = b.getUniqueA();
            assertNotNull(a);
            assertEquals(a.getKey(), aKey);

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }

    }

    // Test to check if unique constraints are generated correctly
    public void testDDLUniqueConstraintsByAnnotations() {
        if(!getServerSession(DDL_PU).getPlatform().supportsUniqueKeyConstraints()) {
            return;
        }
        UniqueConstraintsEntity1 ucEntity;
        
        EntityManager em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = em.find(UniqueConstraintsEntity1.class, 1);
            if(ucEntity == null) {
                ucEntity = new UniqueConstraintsEntity1(1);
                ucEntity.setColumns(1, 1, 1, 1);
                em.persist(ucEntity);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
            throw e;
        }
        
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity1(2);
            ucEntity.setColumns(1, 2, 2, 2);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity1(2);
            ucEntity.setColumns(2, 1, 2, 2);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
        
        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity1(2);
            ucEntity.setColumns(2, 2, 1, 1);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity1(2);
            ucEntity.setColumns(2, 2, 1, 2);
            em.persist(ucEntity);
            em.flush();
        } catch (PersistenceException e) {
            fail("Unique constraint violation is not expected, thrown:" + e);
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
    }
    
    // Test to check if unique constraints are generated correctly
    public void testDDLUniqueConstraintsByXML() {
        if(!getServerSession(DDL_PU).getPlatform().supportsUniqueKeyConstraints()) {
            return;
        }
        UniqueConstraintsEntity2 ucEntity;
        
        EntityManager em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = em.find(UniqueConstraintsEntity2.class, 1);
            if(ucEntity == null) {
                ucEntity = new UniqueConstraintsEntity2(1);
                ucEntity.setColumns(1, 1, 1, 1);
                em.persist(ucEntity);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
            throw e;
        }
        
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity2(2);
            ucEntity.setColumns(1, 2, 2, 2);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity2(2);
            ucEntity.setColumns(2, 1, 2, 2);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
        
        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity2(2);
            ucEntity.setColumns(2, 2, 1, 1);
            em.persist(ucEntity);
            em.flush();
            
            fail("Unique constraint violation is expected");
        } catch (PersistenceException e) {
            //expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }

        em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        try {
            ucEntity = new UniqueConstraintsEntity2(2);
            ucEntity.setColumns(2, 2, 1, 2);
            em.persist(ucEntity);
            em.flush();
        } catch (PersistenceException e) {
            fail("Unique constraint violation is not expected, thrown:" + e);
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
    }

    // test if the primary key columns of subclass entity whose root entity has EmbeddedId 
    // are generated properly in joined inheritance strategy
    // Issue: GF#1391
    public void testDDLSubclassEmbeddedIdPkColumnsInJoinedStrategy() {
        EntityManager em = createEntityManager(DDL_PU);
        em.getTransaction().begin();
        // let's see if a subclass entity is persisted and found well
        try {
            long seq = System.currentTimeMillis(); // just to get unique value :-)
            String code = "B1215";
            CKeyEntityB2 b = new CKeyEntityB2(new CKeyEntityBPK(seq, code));
            //set unique keys
            b.setUnq1(String.valueOf(seq));
            b.setUnq2(String.valueOf(seq));

            em.persist(b);
            em.flush();
            String query = "SELECT b FROM CKeyEntityB2 b WHERE b.key.seq = :seq AND b.key.code = :code";
            Object result = em.createQuery(query).setParameter("seq", seq).setParameter("code", code)
                            .getSingleResult();
            assertNotNull(result);
            
            em.getTransaction().rollback();
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    
    // gf2821: Schema missing in SQL towards owning side in @ManyToMany entity relation
    // gf3001: A @ManyToMany/@JoinTable mapping with schema many generate ddl without schema
    public void testDDLJoinTableSchema() throws Exception {
        EntityManager em = createEntityManager(DDL_PU);
        
        em.getTransaction().begin();
  
        try {
            EntityA a1 = new EntityA();
            a1.setName("EntityA1");
            EntityB b1 = new EntityB();
            b1.setName("EntityB1");

            a1.getEntityBs().add(b1);
            b1.getEntityAs().add(a1);

            em.persist(a1);
            em.persist(b1);

            // test for gf3001:
            // the join table entry is being created using the fully qualified table name
            // this fails, if the join table was created in the default schema
            em.flush();

            Integer idA1 = a1.getId();
            Integer idB1 = b1.getId();

            // clear internal caches
            clearCache(DDL_PU);
            em.clear();
        
            EntityA a2 = (EntityA) em.createNamedQuery("EntityA.findById").setParameter(1, idA1).getSingleResult();
            EntityB b2 = (EntityB) em.createNamedQuery("EntityB.findById").setParameter(1, idB1).getSingleResult();

            // test for gf2821:
            // queries for navigation must use fully qualified table name
            assertFalse("Failed to retrieve the relationship!", a2.getEntityBs().isEmpty());
            assertFalse("Failed to retrieve the relationship!", b2.getEntityAs().isEmpty());

        } catch (RuntimeException e) {
            fail("Failed because of a RuntimeException.\n" +
                 "In order to create tables in the schema MYSCHEMA, some databases require that you create a new user called MYSCHEMA.\n" +
                 "Nested exception: " + e);            
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
