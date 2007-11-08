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
package oracle.toplink.essentials.testing.tests.cmp3.relationships;

import junit.framework.*;
import junit.extensions.TestSetup;

import javax.persistence.*;

import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.relationships.*;
import oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany.EntityA;
import oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany.EntityB;
import oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany.EntityC;
import oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany.EntityD;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

public class UniAndBiDirectionalMappingTestSuite extends JUnitTestCase {
    public UniAndBiDirectionalMappingTestSuite() {}
    
    public UniAndBiDirectionalMappingTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        new RelationshipsTableManager().replaceTables(JUnitTestCase.getServerSession());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("UniAndBiDirectionalMappingTestSuite");
        suite.addTest(new UniAndBiDirectionalMappingTestSuite("selfReferencingManyToManyCreateTest"));
        suite.addTest(new UniAndBiDirectionalMappingTestSuite("manyToManyDeleteNonOwningSideTest"));
        suite.addTest(new UniAndBiDirectionalMappingTestSuite("manyToManyDeleteOwningSideTest"));
        suite.addTest(new UniAndBiDirectionalMappingTestSuite("unidirectionalOneToManyDeleteTest"));
        
        return new TestSetup(suite) {
        
            protected void setUp() {               
                DatabaseSession session = JUnitTestCase.getServerSession();
                new RelationshipsTableManager().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
     
    public void selfReferencingManyToManyCreateTest() throws Exception {
        EntityManager em = createEntityManager();
        
        em.getTransaction().begin();
  
        Customer owen = new Customer();
        owen.setName("Owen Pelletier");
        owen.setCity("Ottawa");
        em.persist(owen);
        int owenId = owen.getCustomerId();
        
        Customer kirty = new Customer();
        kirty.setName("Kirsten Pelletier");
        kirty.setCity("Ottawa");
        kirty.addCCustomer(owen);
        em.persist(kirty);
        int kirtyId = kirty.getCustomerId();
        
        Customer guy = new Customer();
        guy.setName("Guy Pelletier");
        guy.setCity("Ottawa");
        guy.addCCustomer(owen);
        guy.addCCustomer(kirty);
        kirty.addCCustomer(guy); // guess I'll allow this one ... ;-)
        em.persist(guy);
        int guyId = guy.getCustomerId();
        
        em.getTransaction().commit();
        
        clearCache();
        
        Customer newOwen = em.find(Customer.class, owenId);
        Customer newKirty = em.find(Customer.class, kirtyId);
        Customer newGuy = em.find(Customer.class, guyId);
        
        assertTrue("Owen doesn't have any controlled customers.", newOwen.getCCustomers().isEmpty());
        assertFalse("Kirty has controlled customers.", newKirty.getCCustomers().isEmpty());
        assertFalse("Guy has controlled customers.", newGuy.getCCustomers().isEmpty());

        em.close();
    }
    
    // gf879: Dependency issues if both sides of a ManyToMany are removed
    public void manyToManyDeleteNonOwningSideTest() throws Exception {
        EntityManager em = createEntityManager();
        
        em.getTransaction().begin();
  
        EntityA a1 = new EntityA();
        a1.setName("EntityA1");
        EntityB b1 = new EntityB();
        b1.setName("EntityB1");

        a1.getBs().add(b1);
        b1.getAs().add(a1);

        em.persist(a1);

        em.getTransaction().commit();

        Integer idA1 = a1.getId();
        Integer idB1 = b1.getId();
        
        em.getTransaction().begin();
        // remove the owning side
        em.remove(b1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            
            throw ex;
        }
    
        clearCache();
        
        assertTrue("EntityA a1 should have been removed!", 
                em.createQuery("SELECT a FROM EntityA a WHERE a.id = ?1").setParameter(1, idA1).getResultList().size() == 0);
        assertTrue("EntityB b1 should have been removed!", 
                em.createQuery("SELECT b FROM EntityB b WHERE b.id = ?1").setParameter(1, idB1).getResultList().size() == 0);

        em.close();
    }
    
    // gf879: Dependency issues if both sides of a ManyToMany are removed
    public void manyToManyDeleteOwningSideTest() throws Exception {
        EntityManager em = createEntityManager();
        
        em.getTransaction().begin();
  
        EntityA a1 = new EntityA();
        a1.setName("EntityA1");
        EntityB b1 = new EntityB();
        b1.setName("EntityB1");

        a1.getBs().add(b1);
        b1.getAs().add(a1);

        em.persist(a1);

        em.getTransaction().commit();

        Integer idA1 = a1.getId();
        Integer idB1 = b1.getId();
        
        em.getTransaction().begin();
        
        // remove the relationship
        a1.getBs().remove(b1);
        b1.getAs().remove(a1);

        // remove the non-owning side
        em.remove(a1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            
            throw ex;
        }
    
        clearCache();
        
        assertTrue("EntityA a1 should have been removed!", 
                em.createQuery("SELECT a FROM EntityA a WHERE a.id = ?1").setParameter(1, idA1).getResultList().size() == 0);
        assertTrue("EntityB b1 should not have been removed!", 
                em.createQuery("SELECT b FROM EntityB b WHERE b.id = ?1").setParameter(1, idB1).getResultList().size() != 0);

        em.close();
    }
    
    // gf2991: Dependency issues if both sides of an uni-directional OneToMany are removed
    public void unidirectionalOneToManyDeleteTest() throws Exception {
        EntityManager em = createEntityManager();
        
        em.getTransaction().begin();
  
        EntityC c1 = new EntityC();
        c1.setName("EntityC1");
        EntityD d1 = new EntityD();
        d1.setName("EntityD1");

        c1.getDs().add(d1);

        em.persist(c1);

        em.getTransaction().commit();

        Integer idC1 = c1.getId();
        Integer idD1 = d1.getId();
        
        em.getTransaction().begin();
        
        // remove the owning side
        em.remove(c1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            
            throw ex;
        }
    
        clearCache();
        
        assertTrue("EntityC c1 should have been removed!", 
                em.createQuery("SELECT c FROM EntityC c WHERE c.id = ?1").setParameter(1, idC1).getResultList().size() == 0);
        assertTrue("EntityD d1 should have been removed!", 
                em.createQuery("SELECT d FROM EntityD d WHERE d.id = ?1").setParameter(1, idD1).getResultList().size() == 0);

        em.close();
    }
}