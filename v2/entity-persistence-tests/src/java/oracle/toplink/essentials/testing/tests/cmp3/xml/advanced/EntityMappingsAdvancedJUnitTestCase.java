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


package oracle.toplink.essentials.testing.tests.cmp3.xml.advanced;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.xml.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.xml.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.xml.advanced.ModelExamples;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
 
/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsAdvancedJUnitTestCase extends JUnitTestCase {
    private static Integer employeeId;
    
    public EntityMappingsAdvancedJUnitTestCase() {
        super();
    }
    
    public EntityMappingsAdvancedJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Advanced Model");
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testCreateEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testReadEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testNamedNativeQueryOnAddress"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testNamedQueryOnEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testUpdateEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testRefreshNotManagedEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testRefreshRemovedEmployee"));
        suite.addTest(new EntityMappingsAdvancedJUnitTestCase("testDeleteEmployee"));
        
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

    public void testNamedNativeQueryOnAddress() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Address address1 = ModelExamples.addressExample1();
            em.persist(address1);
            Address address2 = ModelExamples.addressExample2();
            em.persist(address2);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllXMLAddresses");
        List addresses = query.getResultList();
        assertTrue("Error executing named native query 'findAllXMLAddresses'", addresses != null);
    }

    public void testNamedQueryOnEmployee() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findAllXMLEmployeesByFirstName");
        query.setParameter("firstname", "Brady");
        Employee employee = (Employee) query.getSingleResult();
        assertTrue("Error executing named query 'findAllXMLEmployeesByFirstName'", employee != null);
    }

    public void testReadEmployee() {
        Employee employee = (Employee) createEntityManager().find(Employee.class, employeeId);
        assertTrue("Error reading Employee", employee.getId() == employeeId);
    }

    public void testUpdateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        int version = 0;
        try {
            Employee employee = (Employee) em.find(Employee.class, employeeId);
            version = employee.getVersion();
            employee.setSalary(50000);
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
        assertTrue("Error updating Employee", newEmployee.getSalary() == 50000);
        assertTrue("Version field not updated", newEmployee.getVersion() == version + 1);
    }

    public void testRefreshNotManagedEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee emp = new Employee();
            emp.setFirstName("NotManaged");
            em.refresh(emp);
            fail("entityManager.refresh(notManagedObject) didn't throw exception");
        } catch (IllegalArgumentException illegalArgumentException) {
            // expected behaviour
        } catch (RuntimeException e ) {
            throw e;
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void testRefreshRemovedEmployee() {
        // find an existing or create a new Employee
        String firstName = "testRefreshRemovedEmployee";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM XMLEmployee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            em.getTransaction().begin();
            try {
                em.persist(emp);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
            }
        }
        
        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());
                
            // delete the Employee from the db
            em.createQuery("DELETE FROM XMLEmployee e WHERE e.firstName = '"+firstName+"'").executeUpdate();

            // refresh the Employee - should fail with EntityNotFoundException
            em.refresh(emp);
            fail("entityManager.refresh(removedObject) didn't throw exception");
        } catch (EntityNotFoundException entityNotFoundException) {
            // expected behaviour
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void testContainsRemovedEmployee() {
        // find an existing or create a new Employee
        String firstName = "testContainsRemovedEmployee";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM XMLEmployee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            em.getTransaction().begin();
            try{
                em.persist(emp);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
            }
        }
        
        boolean containsRemoved = true;
        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());
            em.remove(emp);
            containsRemoved = em.contains(emp);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
        assertFalse("entityManager.contains(removedObject)==true ", containsRemoved);
    }

    public void testSubString() {
        // find an existing or create a new Employee
        String firstName = "testSubString";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM XMLEmployee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            em.getTransaction().begin();
            try{
                em.persist(emp);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
            }
        }
        
        int firstIndex = 1;
        int lastIndex = firstName.length();
        List employees = em.createQuery("SELECT object(e) FROM XMLEmployee e where e.firstName = substring(:p1, :p2, :p3)").
            setParameter("p1", firstName).
            setParameter("p2", new Integer(firstIndex)).
            setParameter("p3", new Integer(lastIndex)).
            getResultList();
            
        // clean up
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM XMLEmployee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }

        assertFalse("employees.isEmpty()==true ", employees.isEmpty());
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
