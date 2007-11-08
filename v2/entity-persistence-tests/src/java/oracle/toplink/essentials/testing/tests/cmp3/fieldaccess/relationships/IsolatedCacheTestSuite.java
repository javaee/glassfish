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
package oracle.toplink.essentials.testing.tests.cmp3.fieldaccess.relationships;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.persistence.*;

import oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.base.RepeatableWriteUnitOfWork;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.fieldaccess.relationships.IsolatedItem;

import oracle.toplink.essentials.testing.models.cmp3.fieldaccess.relationships.*;

public class IsolatedCacheTestSuite extends JUnitTestCase {
    public IsolatedCacheTestSuite() {}
    
    public IsolatedCacheTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        new RelationshipsTableManager().replaceTables(JUnitTestCase.getServerSession());
    }
    
    public static Test suite() {
        return new TestSuite(IsolatedCacheTestSuite.class) {
            protected void setUp(){}
            protected void tearDown(){}
        };
    }
     
    public void testCacheIsolationDBQueryHit() throws Exception {
        EntityManager em = createEntityManager();
        
        // Step 1 - get an isolated item in the cache.
        em.getTransaction().begin();
        
        IsolatedItem item = new IsolatedItem();
        item.setDescription("A phoney item");
        item.setName("Phoney name");
        em.persist(item);
    
        em.getTransaction().commit();
        
        // Step 2 - clear the entity manager and see if the item still exists
        // in the uow cache.
        em.getTransaction().begin();
        
        em.clear();
        RepeatableWriteUnitOfWork uow = (RepeatableWriteUnitOfWork) ((EntityManagerImpl) em.getDelegate()).getActivePersistenceContext(em.getTransaction());
        
        assertFalse("The isolated item was not cleared from the shared cache", uow.getIdentityMapAccessor().containsObjectInIdentityMap(item));
        assertFalse("The isolated item was not cleared from the uow cache", uow.getParent().getIdentityMapAccessor().containsObjectInIdentityMap(item));
        
        em.getTransaction().commit();
        em.close();
    }
}
