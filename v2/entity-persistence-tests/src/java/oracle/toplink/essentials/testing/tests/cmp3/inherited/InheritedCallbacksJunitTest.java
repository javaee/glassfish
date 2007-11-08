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

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Beer;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Alpine;
import oracle.toplink.essentials.testing.models.cmp3.inherited.BeerConsumer;
import oracle.toplink.essentials.testing.models.cmp3.inherited.InheritedTableManager;
 
public class InheritedCallbacksJunitTest extends JUnitTestCase {
    private static Integer m_Id;
    
    public InheritedCallbacksJunitTest() {
        super();
    }
    
    public InheritedCallbacksJunitTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("InheritedCallbacksJunitTest");
        suite.addTest(new InheritedCallbacksJunitTest("testPreAndPostPersistAlpine"));
        suite.addTest(new InheritedCallbacksJunitTest("testPreAndPostPersistBeerConsumer"));
        suite.addTest(new InheritedCallbacksJunitTest("testPostLoadOnFind"));
        suite.addTest(new InheritedCallbacksJunitTest("testPostLoadOnRefresh"));
        suite.addTest(new InheritedCallbacksJunitTest("testPreAndPostUpdate"));
        suite.addTest(new InheritedCallbacksJunitTest("testPreAndPostRemove"));

        return new TestSetup(suite) {
        
            protected void setUp(){               
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                new InheritedTableManager().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testPreAndPostPersistAlpine() {
        int beerPrePersistCount = Beer.BEER_PRE_PERSIST_COUNT;
        int alpinePrePersistCount = Alpine.ALPINE_PRE_PERSIST_COUNT;
        
        Alpine alpine = null;
        EntityManager em = createEntityManager();
        
        try {
            em.getTransaction().begin();
            alpine = new Alpine();
            alpine.setBestBeforeDate(new java.util.Date(2007, 8, 17));
            alpine.setAlcoholContent(5.0);
            
            em.persist(alpine);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        verifyNotCalled(beerPrePersistCount, Beer.BEER_PRE_PERSIST_COUNT, "PrePersist");
        verifyCalled(alpinePrePersistCount, Alpine.ALPINE_PRE_PERSIST_COUNT, "PrePersist");
    }
    
    public void testPreAndPostPersistBeerConsumer() {
        BeerConsumer beerConsumer = null;
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            beerConsumer = new BeerConsumer();
            beerConsumer.setName("A consumer to delete eventually");
            em.persist(beerConsumer);
            m_Id = beerConsumer.getId();
        
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        verifyCalled(0, beerConsumer.pre_persist_count, "PrePersist");
        verifyCalled(0, beerConsumer.post_persist_count, "PostPersist");
    }
    
    public void testPostLoadOnFind() {
        BeerConsumer beerConsumer = (BeerConsumer) createEntityManager().find(BeerConsumer.class, m_Id);
        
        verifyCalled(0, beerConsumer.post_load_count, "PostLoad");
    }
    
    public void testPostLoadOnRefresh() {
        BeerConsumer beerConsumer = null;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_Id);
            em.refresh(beerConsumer);
            
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        verifyCalled(0, beerConsumer.post_load_count, "PostLoad");
    }
    
    public void testPreAndPostUpdate() {
        BeerConsumer beerConsumer = null;
        int count1, count2 = 0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_Id);
            count1 = beerConsumer.pre_update_count;
            beerConsumer.setName("An updated name");
            count2 = beerConsumer.post_update_count;
            em.getTransaction().commit();    
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        verifyCalled(count1, beerConsumer.pre_update_count, "PreUpdate");
        verifyCalled(count2, beerConsumer.post_update_count, "PostUpdate");
    }
    
    public void testPreAndPostRemove() {
        BeerConsumer beerConsumer = null;
        int count1, count2 = 0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        try {
            beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_Id);
            count1 = beerConsumer.pre_remove_count;
            em.remove(beerConsumer);
            count2 = beerConsumer.post_remove_count;
            em.getTransaction().commit();    
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        verifyCalled(count1, beerConsumer.pre_remove_count, "PreRemove");
        verifyCalled(count2, beerConsumer.post_remove_count, "PostRemove");
    }
    
    public void verifyCalled(int countBefore, int countAfter, String callback) {
        assertFalse("The callback method [" + callback + "] was not called", countBefore == countAfter);
        assertTrue("The callback method [" + callback + "] was called more than once", countAfter == (countBefore + 1));
    }
    
    public void verifyNotCalled(int countBefore, int countAfter, String callback) {
        assertTrue("The callback method [" + callback + "] was called.", countBefore == countAfter);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
