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


package oracle.toplink.essentials.testing.tests.cmp3.inherited;

import java.util.Date;
import java.util.Vector;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Alpine;
import oracle.toplink.essentials.testing.models.cmp3.inherited.BeerConsumer;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inherited.InheritedTableManager;
 
public class OrderedListJunitTest extends JUnitTestCase {
    private static Integer m_beerConsumerId;
    
    public OrderedListJunitTest() {
        super();
    }
    
    public OrderedListJunitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("OrderedListJunitTest");
        suite.addTest(new OrderedListJunitTest("testInitialize"));
        suite.addTest(new OrderedListJunitTest("test1"));
        suite.addTest(new OrderedListJunitTest("testInitialize"));
        suite.addTest(new OrderedListJunitTest("test2"));
        suite.addTest(new OrderedListJunitTest("testInitialize"));
        suite.addTest(new OrderedListJunitTest("test3"));
        
        return new TestSetup(suite) {
        
            protected void setUp() {               
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                new InheritedTableManager().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testInitialize() {
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            BeerConsumer beerConsumer = new BeerConsumer();
            beerConsumer.setName("Guy Pelletier");
            em.persist(beerConsumer);
            m_beerConsumerId = beerConsumer.getId();

            Alpine alpine1 = new Alpine();
            alpine1.setBestBeforeDate(new Date(2005, 8, 17));
            alpine1.setAlcoholContent(5);
            alpine1.setClassification(Alpine.Classification.STRONG);
            beerConsumer.addAlpineBeerToConsume(alpine1);
            
            Alpine alpine2 = new Alpine();
            alpine2.setBestBeforeDate(new Date(2005, 8, 19));
            alpine2.setAlcoholContent(4);
            alpine2.setClassification(Alpine.Classification.STRONG);
            beerConsumer.addAlpineBeerToConsume(alpine2);
            
            Alpine alpine3 = new Alpine();
            alpine3.setBestBeforeDate(new Date(2005, 8, 21));
            alpine3.setAlcoholContent(3);
            alpine3.setClassification(Alpine.Classification.STRONG);
            beerConsumer.addAlpineBeerToConsume(alpine3);
            
            Alpine alpine4 = new Alpine();
            alpine4.setBestBeforeDate(new Date(2005, 8, 23));
            alpine4.setAlcoholContent(2);
            alpine4.setClassification(Alpine.Classification.BITTER);
            beerConsumer.addAlpineBeerToConsume(alpine4);

            Alpine alpine5 = new Alpine();
            alpine5.setBestBeforeDate(new Date(2005, 8, 25));
            alpine5.setAlcoholContent(1);
            alpine5.setClassification(Alpine.Classification.SWEET);
            beerConsumer.addAlpineBeerToConsume(alpine5);
            
            em.getTransaction().commit();
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("An exception was caught during create operation: [" + e.getMessage() + "]");
        }
        
    }
    
    public void test1() {
        BeerConsumer beerConsumer = null;
        Alpine alpine1 = null;
        Alpine alpine2 = null;
        EntityManager em = createEntityManager();
        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
            
            em.getTransaction().begin();
        
            beerConsumer = (BeerConsumer) em.merge(beerConsumer);

            alpine1 = beerConsumer.removeAlpineBeerToConsume(1);
            alpine2 = beerConsumer.removeAlpineBeerToConsume(1);
            
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("An exception was caught during test1 : [" + ex.getMessage() + "]");
        }
            
        // Read the beerConsumer back from the cache.
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
        Vector alpinesFromCache =  (Vector) beerConsumer.getAlpineBeersToConsume();
        
        assertTrue("Incorrect number of alpines in the list", alpinesFromCache.size() == 3);
        assertFalse("Alpine 1 was not removed from the list", alpinesFromCache.contains(alpine1));
        assertFalse("Alpine 2 was not removed from the list", alpinesFromCache.contains(alpine2));
            
    }
    
    public void test2() {
        BeerConsumer beerConsumer = null;
        Alpine alpine1 = null;
        Alpine alpine2 = null;
        EntityManager em = createEntityManager();
        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
            
            em.getTransaction().begin();
        
            beerConsumer = (BeerConsumer) em.merge(beerConsumer);

            alpine1 = beerConsumer.moveAlpineBeerToConsume(2, 4);
            alpine2 = beerConsumer.moveAlpineBeerToConsume(1, 3);
            
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("An exception was caught while adding the new alpine at a specific index: [" + ex.getMessage() + "]");
        }
            
        // Read the beerConsumer back from the cache.
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
        Vector alpinesFromCache =  (Vector) beerConsumer.getAlpineBeersToConsume();
        
        assertTrue("Incorrect number of alpines in the list", alpinesFromCache.size() == 5);
        assertTrue("Alpine 1 was not at the correct index.", alpinesFromCache.indexOf(alpine1) == 4);
        assertTrue("Alpine 2 was not at the correct index.", alpinesFromCache.indexOf(alpine2) == 3);
    }
    
    public void test3() {
        
        BeerConsumer beerConsumer = null;
        Alpine alpine1 = null, alpine2 = null, alpine3 = null, alpine4 = null;
        EntityManager em = createEntityManager();

        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
            
            em.getTransaction().begin();
        
            beerConsumer = (BeerConsumer) em.merge(beerConsumer);

            alpine1 = beerConsumer.moveAlpineBeerToConsume(4, 1);
            
            alpine2 = beerConsumer.removeAlpineBeerToConsume(0);
            
            alpine3 = beerConsumer.moveAlpineBeerToConsume(1, 3);
            
            alpine4 = new Alpine();
            alpine4.setBestBeforeDate(new Date(2005, 8, 29));
            alpine4.setAlcoholContent(7);
            alpine4.setClassification(Alpine.Classification.SWEET);
            beerConsumer.addAlpineBeerToConsume(alpine4, 3);
                
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("An exception was caught while adding the new alpine at a specific index: [" + ex.getMessage() + "]");
        }
            
        // Read the beerConsumer back from the cache.
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId);
        Vector alpinesFromCache =  (Vector) beerConsumer.getAlpineBeersToConsume();
        
        assertTrue("Incorrect number of alpines in the list", alpinesFromCache.size() == 5);
        assertTrue("Alpine 1 was not at the correct index.", alpinesFromCache.indexOf(alpine1) == 0);
        assertFalse("Alpine 2 was not removed from the list", alpinesFromCache.contains(alpine2));
        assertTrue("Alpine 3 was not at the correct index.", alpinesFromCache.indexOf(alpine3) == 4);
        assertTrue("Alpine 4 was not at the correct index.", alpinesFromCache.indexOf(alpine4) == 3);
        
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
