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

import junit.framework.*;
import junit.extensions.TestSetup;

import javax.persistence.*;

import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.fieldaccess.relationships.*;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.fieldaccess.relationships.Customer;

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
        suite.addTest(new UniAndBiDirectionalMappingTestSuite("selfReferencingManyToManyTest"));
        
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
     
    public void selfReferencingManyToManyTest() throws Exception {
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
        
        assertTrue("Owen has controlled customers .", newOwen.getCCustomers().isEmpty());
        assertFalse("Kirty did not have any controlled customers.", newKirty.getCCustomers().isEmpty());
        assertFalse("Guy did not have any controlled customers.", newGuy.getCCustomers().isEmpty());

        em.close();
    }
}