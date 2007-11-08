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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.*;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import junit.extensions.TestSetup;

 
public class ExtendedPersistenceContextJUnitTestSuite extends JUnitTestCase {
        
    public ExtendedPersistenceContextJUnitTestSuite() {
        super();
    }
    
    public ExtendedPersistenceContextJUnitTestSuite(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new ExtendedPersistenceContextJUnitTestSuite("testExtendedPersistenceContext"));

        return new TestSetup(suite) {
        
            protected void setUp(){               
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
                //create a new EmployeePopulator
                EmployeePopulator employeePopulator = new EmployeePopulator();         
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(JUnitTestCase.getServerSession());     
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
   
    // JUnit framework will automatically execute all methods starting with test...    
    public void testExtendedPersistenceContext() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Employee e");
        List result = query.getResultList();
        if (result.isEmpty()){
            fail("Database not setup correctly");
        }
        Object obj = result.get(0);
        em.getTransaction().commit();
        assertTrue("Extended PersistenceContext did not continue to maintain object after commit.", em.contains(obj));
        em.close();
    }

    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
