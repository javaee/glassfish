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
 * add the followning below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


package oracle.toplink.essentials.testing.tests.cmp3.xml.relationships.unidirectional;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.PhoneNumber;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.Project;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.Address;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.Employee;
import oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional.ModelExamples;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
 
/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsUnidirectionalRelationshipsJUnitTestCase extends JUnitTestCase {

    protected static Integer otoEmployeeId;
    protected static Integer otoProjectId;
    protected static Integer otmEmployeeId;
    protected static Integer otmPhone1Id;
    protected static Integer otmPhone2Id;
    protected static Integer mtoEmployee1Id;
    protected static Integer mtoEmployee2Id;
    protected static Integer mtoAddressId;
    protected static Integer mtmEmployeeId;
    protected static Integer mtmProject1Id;
    protected static Integer mtmProject2Id;
    
    public EntityMappingsUnidirectionalRelationshipsJUnitTestCase() {
        super();
    }
    
    public EntityMappingsUnidirectionalRelationshipsJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Unidirectional Relationships Model");
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToOneCreate"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToOneRead"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToOneDeleteNonowning"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToOneDeleteOwning"));

        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToManyCreate"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToManyRead"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToManyDeleteNonowning"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalOneToManyDeleteOwning"));

        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToOneCreate"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToOneRead"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToOneDeleteNonowning"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToOneDeleteOwning"));

        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToManyCreate"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToManyRead"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToManyDeleteNonowning"));
        suite.addTest(new EntityMappingsUnidirectionalRelationshipsJUnitTestCase("testUnidirectionalManyToManyDeleteOwning"));
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

    public void testUnidirectionalOneToOneCreate() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();		
            Project project = ModelExamples.projectExample1();
            project.setTeamLeader(employee);

            //cascade-persist is not set
            em.persist(employee);
            em.persist(project);
            otoEmployeeId = employee.getId();
            otoProjectId = project.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    public void testUnidirectionalOneToOneRead() {
        EntityManager em = createEntityManager();
        Project project = em.find(Project.class, otoProjectId);
        assertTrue("Error reading Project", project.getId() == otoProjectId);
        assertTrue("Error reading TeamLeader of Project",project.getTeamLeader().getId() == otoEmployeeId);
    }
        
    public void testUnidirectionalOneToOneDeleteNonowning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, otoEmployeeId);
            Project project = em.find(Project.class, otoProjectId);
            assertTrue(employee != null);
            assertTrue(project != null); 
            project.setTeamLeader(null);
            em.remove(employee);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Employee", em.find(Employee.class, otoEmployeeId) == null);
    }

    public void testUnidirectionalOneToOneDeleteOwning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Project project = em.find(Project.class, otoProjectId);
            assertTrue(project != null);
            em.remove(project);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Project", em.find(Project.class, otoProjectId) == null);
    }

    public void testUnidirectionalOneToManyCreate() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();		
            PhoneNumber phone1 = ModelExamples.phoneExample1();		
            PhoneNumber phone2 = ModelExamples.phoneExample2();		
            employee.addPhoneNumber(phone1);
            employee.addPhoneNumber(phone2);

            //cascade-persist
            em.persist(employee);
            otmEmployeeId = employee.getId();
            otmPhone1Id = phone1.getId();
            otmPhone2Id = phone2.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }
    
    public void testUnidirectionalOneToManyRead() {
        EntityManager em = createEntityManager();
        Employee employee = em.find(Employee.class, otmEmployeeId);
        assertTrue("Error reading Employee", employee.getId() == otmEmployeeId);
        Collection phones = employee.getPhoneNumbers();
        assertTrue("Employee didn't have correct number of phone numbers", phones.size()==2);
        boolean phonesMatched = true;
        for(Iterator ite = phones.iterator(); ite.hasNext();){
          PhoneNumber phone = (PhoneNumber)ite.next();
          if(phone.getId()!=otmPhone1Id && phone.getId()!=otmPhone2Id)
          {
            phonesMatched = false;
          }
        }
        assertTrue("Employee didn't have correct phones", phonesMatched);
    }
    
    public void testUnidirectionalOneToManyDeleteNonowning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, otmEmployeeId);
            PhoneNumber phone = em.find(PhoneNumber.class, otmPhone1Id);
            employee.removePhoneNumber(phone);
            em.remove(phone);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting PhoneNumber", em.find(PhoneNumber.class, otmPhone1Id) == null);
    }
        
    public void testUnidirectionalOneToManyDeleteOwning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, otmEmployeeId);
            
            //if cascade-all is set, comment this line out
            employee.setPhoneNumbers(null);
            em.remove(employee);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Employee", em.find(Employee.class, otmEmployeeId) == null);
        //if cascade-all is set, uncomment this line
        //assertTrue("The phone should have been deleted.", em.find(PhoneNumber.class, otmPhone2Id) == null);
    }

    public void testUnidirectionalManyToOneCreate() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee1 = ModelExamples.employeeExample1();		
            Employee employee2 = ModelExamples.employeeExample2();		
            Address address = ModelExamples.addressExample1();
            employee1.setAddress(address);
            employee2.setAddress(address);

            //cascade-persist
            em.persist(employee1);
            em.persist(employee2);
            mtoEmployee1Id = employee1.getId();
            mtoEmployee2Id = employee2.getId();
            mtoAddressId = address.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    public void testUnidirectionalManyToOneRead() {
        EntityManager em = createEntityManager();
        Employee employee = em.find(Employee.class, mtoEmployee1Id);
        assertTrue("Error reading Employee", employee.getId() == mtoEmployee1Id);
        assertTrue("Error reading Address",employee.getAddress().getId() == mtoAddressId);
    }
    
    public void testUnidirectionalManyToOneDeleteNonowning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee1 = em.find(Employee.class, mtoEmployee1Id);
            Employee employee2 = em.find(Employee.class, mtoEmployee2Id);
            Address address = (Address) em.find(Address.class, mtoAddressId);
            employee1.setAddress(null);
            employee2.setAddress(null);
            em.remove(address);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Address", em.find(Address.class, mtoAddressId) == null);
    }
        
    public void testUnidirectionalManyToOneDeleteOwning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee1 = em.find(Employee.class, mtoEmployee1Id);
            Employee employee2 = em.find(Employee.class, mtoEmployee2Id);
            em.remove(employee1);
            em.remove(employee2);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Employee", em.find(Employee.class, mtoEmployee1Id) == null);
        assertTrue("Error deleting Employee", em.find(Employee.class, mtoEmployee2Id) == null);
    }
    
    public void testUnidirectionalManyToManyCreate() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();		
            Project project1=ModelExamples.projectExample1();
            Project project2=ModelExamples.projectExample2();
            employee.addProject(project1);            
            employee.addProject(project2);            

            //cascade-persist
            em.persist(employee);
            mtmEmployeeId = employee.getId();
            mtmProject1Id = project1.getId();
            mtmProject2Id = project2.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    public void testUnidirectionalManyToManyRead() {
        EntityManager em = createEntityManager();
        Employee employee = em.find(Employee.class, mtmEmployeeId);
        assertTrue("Error reading Employee", employee.getId() == mtmEmployeeId);
        Collection projects = employee.getProjects();
        assertTrue("Employee didn't have correct number of projects", projects.size()==2);
        boolean projectsMatched = true;
        for(Iterator ite = projects.iterator(); ite.hasNext();){
          Project project = (Project)ite.next();
          if(project.getId()!=mtmProject1Id && project.getId()!=mtmProject2Id)
          {
            projectsMatched = false;
          }
        }
        assertTrue("Employee didn't have correct projects", projectsMatched);
    }

    public void testUnidirectionalManyToManyDeleteNonowning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, mtmEmployeeId);
            Project project1 = em.find(Project.class, mtmProject1Id);
            employee.removeProject(project1);
            em.remove(project1);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Project", em.find(Project.class, mtmProject1Id) == null);
    }

    public void testUnidirectionalManyToManyDeleteOwning() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, mtmEmployeeId);
            employee.setProjects(null);
            em.remove(employee);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting Employee", em.find(Employee.class, mtmEmployeeId) == null);
        assertTrue("The project shouldn't have been deleted.", em.find(Project.class, mtmProject2Id) != null);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EntityMappingsUnidirectionalRelationshipsJUnitTestCase.suite());
    }
}
