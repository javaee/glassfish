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


package oracle.toplink.essentials.testing.tests.cmp3.xml.merge.incompletemappings.owning;

import java.util.ArrayList;
import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.Address;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.Employee;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.LargeProject;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.ModelExamples;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.PhoneNumber;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.PhoneNumberPK;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.Project;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.SecurityBadge;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning.SmallProject;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsIncompleteOwningJUnitTestCase extends JUnitTestCase {
    private static Integer employeeId;
    
    public EntityMappingsIncompleteOwningJUnitTestCase() {
        super();
    }
    
    public EntityMappingsIncompleteOwningJUnitTestCase(String name) {
        super(name);
    }
    
    public void setUp() {try{super.setUp();}catch(Exception x){}}
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Owning Model");
        suite.addTest(new EntityMappingsIncompleteOwningJUnitTestCase("testCreateEmployee"));
        suite.addTest(new EntityMappingsIncompleteOwningJUnitTestCase("testReadEmployee"));
        suite.addTest(new EntityMappingsIncompleteOwningJUnitTestCase("testUpdateEmployee"));
        suite.addTest(new EntityMappingsIncompleteOwningJUnitTestCase("testDeleteEmployee"));
        
        return new TestSetup(suite) {
            
            protected void setUp(){  
            	DatabaseSession session = JUnitTestCase.getServerSession();   
                new AdvancedTableCreator().replaceTables(session);
			}
        
            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();		
            ArrayList projects = new ArrayList();
            projects.add(ModelExamples.projectExample1());
            projects.add(ModelExamples.projectExample2());
            employee.setProjects(projects);
            employee.setAddress(ModelExamples.addressExample1());
            em.persist(employee);
            employeeId = employee.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
    }
    
    public void testDeleteEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Employee.class, employeeId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Employee", em.find(Employee.class, employeeId) == null);
    }

    public void testReadEmployee() {
        Employee employee = (Employee) createEntityManager().find(Employee.class, employeeId);
        assertTrue("Error reading Employee", employee.getId() == employeeId);
    }

    public void testUpdateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = (Employee) em.find(Employee.class, employeeId);
            employee.setSecurityBadge(new SecurityBadge(69));
            em.merge(employee);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Employee newEmployee = (Employee) em.find(Employee.class, employeeId);
        assertTrue("Error updating Employee's Security Badge", newEmployee.getSecurityBadge().getBadgeNumber() == 69);
    }

	public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
