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
package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import java.util.Arrays;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;

import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Golfer;
import oracle.toplink.essentials.testing.models.cmp3.advanced.GolferPK;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Vegetable;
import oracle.toplink.essentials.testing.models.cmp3.advanced.VegetablePK;
import oracle.toplink.essentials.testing.models.cmp3.advanced.WorldRank;

public class AdvancedJunitTest extends JUnitTestCase {
    public AdvancedJunitTest() {
        super();
    }
    
    public AdvancedJunitTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AdvancedJunitTest.class);

        return new TestSetup(suite) {
            protected void setUp() { 
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }

    public void testGF894() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            for (int i = 0; ; i++) {
                GolferPK golferPK = new GolferPK(i);
                Golfer golfer = em.find(Golfer.class, golferPK);
            
                if (golfer == null) {
                    golfer = new Golfer();
                    golfer.setGolferPK(golferPK);
                
                    WorldRank worldRank = new WorldRank();
                    worldRank.setId(i);
                    golfer.setWorldRank(worldRank);
                
                    em.persist(worldRank);
                    em.persist(golfer);
                    em.getTransaction().commit();
                
                    break;
                } 
            }
        } catch (Exception e) {
            fail("An exception was caught: [" + e.getMessage() + "]");
        }
        
        em.close();
    }
    
    public void testGF1818() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Vegetable vegetable = new Vegetable();
            vegetable.setId(new VegetablePK("Carrot", "Orange"));
            vegetable.setCost(2.09);
        
            em.persist(vegetable);
            em.getTransaction().commit();
            
        } catch (Exception e) {
            fail("An exception was caught: [" + e.getMessage() + "]");
        }
        
        em.close();
    }
    
    public void testGF1894() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Employee emp = new Employee();
        emp.setFirstName("Guy");
        emp.setLastName("Pelletier");
        
        Address address = new Address();
        address.setCity("College Town");
        
        emp.setAddress(address);
            
        try {   
            Employee empClone = em.merge(emp);
            assertNotNull("The id field for the merged new employee object was not generated.", empClone.getId());
            em.getTransaction().commit();
            
            Employee empFromDB = em.find(Employee.class, empClone.getId());
            assertNotNull("The version locking field for the merged new employee object was not updated after commit.", empFromDB.getVersion());
            
            em.getTransaction().begin();
            Employee empClone2 = em.merge(empFromDB);
            assertTrue("The id field on a existing merged employee object was modified on a subsequent merge.", empFromDB.getId().equals(empClone2.getId()));
            em.getTransaction().commit();
        } catch (javax.persistence.OptimisticLockException e) {
            fail("An optimistic locking exception was caught on the merge of a new object. An insert should of occurred instead.");
        }
        
        em.close();
    }

    // GF1673, 2674 Java SE 6 classloading error for String[] field
    public void testStringArrayField() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        VegetablePK pk = new VegetablePK("Tomato", "Red");
        String[] tags = {"California", "XE"};
        try {
            Vegetable vegetable = new Vegetable();
            vegetable.setId(pk);
            vegetable.setCost(2.09);
            vegetable.setTags(tags);
        
            em.persist(vegetable);
            em.getTransaction().commit();
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
        
        em = createEntityManager();
        em.getTransaction().begin();
        Vegetable vegetable;
        try {
            vegetable = em.find(Vegetable.class, pk);
            
            assertNotNull(vegetable);
            assertTrue(Arrays.equals(tags, vegetable.getTags()));
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
