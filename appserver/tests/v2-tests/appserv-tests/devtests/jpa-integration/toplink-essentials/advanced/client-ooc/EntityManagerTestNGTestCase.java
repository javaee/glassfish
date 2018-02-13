/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import oracle.toplink.essentials.testing.framework.TestNGTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.ModelExamples;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import java.util.List;

/**
 * TestNG test case(s) for testing entity manager operations using the
 * TopLink implementation of the Java Persistence API.
 * The base class TestNGTestCase provides the EntiyManagerFactory setup.
 */
public class EntityManagerTestNGTestCase extends TestNGTestCase {
    private static Integer employeeId;

    @Test
    public void createEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();
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

    @Test(dependsOnMethods = { "createEmployee" })
    public void testReadEmployee() {
        Employee employee = createEntityManager().find(Employee.class, employeeId);
        Assert.assertTrue(employee.getId() == employeeId, "Error reading Employee");
    }

    @Test
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
        Query query = em.createNamedQuery("findAllSQLAddresses");
        List addresses = query.getResultList();
        Assert.assertTrue(addresses != null, "Error executing named native query 'findSQLAllAddresses'");
    }

    @Test(dependsOnMethods = { "testReadEmployee" })
    public void testNamedQueryOnEmployee() {
        Query query = createEntityManager().createNamedQuery("findAllEmployeesByFirstName");
        query.setParameter("firstname", "Brady");
        Employee employee = (Employee) query.getSingleResult();
        Assert.assertTrue(employee != null, "Error executing named query 'findAllEmployeesByFirstName'");
    }

    @Test(dependsOnMethods = { "testNamedQueryOnEmployee" })
    public void testUpdateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, employeeId);
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
        Employee newEmployee = em.find(Employee.class, employeeId);
        Assert.assertTrue(newEmployee.getSalary() == 50000, "Error updating Employee");
    }

    @Test
    public void testRefreshNotManagedEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee emp = new Employee();
            emp.setFirstName("NotManaged");
            em.refresh(emp);
            Assert.fail("entityManager.refresh(notManagedObject) didn't throw exception");
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

    @Test
    public void testRefreshRemovedEmployee() {
        // find an existing or create a new Employee
        String firstName = "testRefreshRemovedEmployee";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
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
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();

            // refresh the Employee - should fail with EntityNotFoundException
            em.refresh(emp);
            Assert.fail("entityManager.refresh(removedObject) didn't throw exception");
        } catch (EntityNotFoundException entityNotFoundException) {
            // expected behaviour
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

     @Test(dependsOnMethods = { "testUpdateEmployee" })
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
         Assert.assertTrue(em.find(Employee.class, employeeId) == null, "Error deleting Employee");
     }

}
