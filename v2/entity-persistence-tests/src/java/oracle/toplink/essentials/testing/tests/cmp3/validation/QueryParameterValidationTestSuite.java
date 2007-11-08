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


package oracle.toplink.essentials.testing.tests.cmp3.validation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.SQLResultSetMappingTestSuite;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;

public class QueryParameterValidationTestSuite extends JUnitTestCase {
        
    public QueryParameterValidationTestSuite() {
    }
    
    public QueryParameterValidationTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        clearCache();
    }
    
    public void testParameterNameValidation(){
        Query query = createEntityManager().createQuery("Select e from Employee e where e.lastName like :name ");
        try{
            query.setParameter("l", "%ay");
        }catch (IllegalArgumentException ex){
            assertTrue("Failed to throw expected IllegalArgumentException, when incorrect parameter name is used", ex.getMessage().contains("using a name"));
            return;
        }
        fail("Failed to throw expected IllegalArgumentException, when incorrect parameter name is used");
    }
    
   
    public void testParameterPositionValidation(){
        Query query = createEntityManager().createQuery("Select e from Employee e where e.firstName like ?1 ");
        try{
            query.setParameter(2, "%ay");
        }catch (IllegalArgumentException ex){
            assertTrue("Failed to throw expected IllegalArgumentException, when incorrect parameter name is used", ex.getMessage().contains("parameter at position"));
            return;
        }
        fail("Failed to throw expected IllegalArgumentException, when incorrect parameter position is used");
    }

    public void testParameterPositionValidation2() {

        Query query = createEntityManager().createQuery("Select e from Employee e where e.firstName = ?1 AND e.lastName = ?3 ");
        try {
            query.setParameter(1, "foo");
            query.setParameter(2, "");
            query.setParameter(3, "bar");
        } catch (IllegalArgumentException ex) {
            assertTrue("Failed to throw expected IllegalArgumentException, when incorrect parameter name is used", ex.getMessage().contains("parameter at position"));
            return;
        }
        fail("Failed to throw expected IllegalArgumentException, when incorrect parameter position is used");
    }
    
  
    public static Test suite() {
        TestSuite suite = new TestSuite(QueryParameterValidationTestSuite.class);
        
        suite.setName("QueryParameterValidationTestSuite");
        
        return new TestSetup(suite) {
        
            protected void setUp(){      
            
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    

}
