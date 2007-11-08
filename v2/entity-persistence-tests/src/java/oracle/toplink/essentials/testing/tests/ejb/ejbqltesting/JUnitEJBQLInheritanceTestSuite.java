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
package oracle.toplink.essentials.testing.tests.ejb.ejbqltesting;

import javax.persistence.Query;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;

import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.queryframework.DatabaseQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Project;
import oracle.toplink.essentials.testing.models.cmp3.advanced.SmallProject;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Engineer;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.InheritancePopulator;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.InheritanceTableCreator;

/**
 * <p>
 * <b>Purpose</b>: Test inheritance EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for inheritance EJBQL functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
public class JUnitEJBQLInheritanceTestSuite extends JUnitTestCase {
    static JUnitDomainObjectComparer comparer;        //the global comparer object used in all tests
  
    public JUnitEJBQLInheritanceTestSuite() {
        super();
    }
  
    public JUnitEJBQLInheritanceTestSuite(String name) {
        super(name);
    }
  
    //This method is run at the end of EVERY test case method
    public void tearDown() {
        clearCache();
    }
  
    //This suite contains all tests contained in this class
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("JUnitEJBQLInheritanceTestSuite");
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testStraightReadSuperClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testStraightReadSubClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinSuperClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinSubClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinFetchSuperClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinFetchSubClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testLeftJoinFetchSubClass"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinedInheritance"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinedInheritanceWithLeftOuterJoin1"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinedInheritanceWithLeftOuterJoin2"));
        suite.addTest(new JUnitEJBQLInheritanceTestSuite("testJoinedInheritanceWithLeftOuterJoin3"));
        
        return new TestSetup(suite) {
     
            //This method is run at the end of the SUITE only
            protected void tearDown() {
                clearCache();
            }
            
            //This method is run at the start of the SUITE only
            protected void setUp() {
                //get session to start setup
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                //create a new EmployeePopulator
                EmployeePopulator employeePopulator = new EmployeePopulator();
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();

                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());   
                
                new AdvancedTableCreator().replaceTables(session);
                new InheritanceTableCreator().replaceTables(session);
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);
                
                //Populate the tables
                InheritancePopulator inheritancePopulator = new InheritancePopulator();
                inheritancePopulator.buildExamples();
                
                //Persist the examples in the database
                inheritancePopulator.persistExample(session);       
            }            
        };    
  }

    public void testStraightReadSuperClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Project project = (Project)em.createQuery("SELECT p from Project p").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(Project.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(project.getId()));
        
        Project tlProject = (Project)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("SuperClass Inheritance Test Failed", comparer.compareObjects(project, tlProject));                    
    }
    
    public void testStraightReadSubClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        SmallProject project = (SmallProject)em.createQuery("SELECT s from SmallProject s").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(SmallProject.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(project.getId()));
        
        SmallProject tlProject = (SmallProject)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("Subclass Inheritance Test Failed", comparer.compareObjects(project, tlProject));                 
    }

    public void testJoinSuperClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp = (Employee)em.createQuery("SELECT e from Employee e JOIN e.projects p where e.lastName is not null").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(Employee.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(emp.getId()));
        tlQuery.addJoinedAttribute(tlQuery.getExpressionBuilder().anyOf("projects"));
        
        Employee tlEmp = (Employee)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("Join superclass Inheritance Test Failed", comparer.compareObjects(emp, tlEmp));                 
    }

    public void testJoinSubClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Engineer emp = (Engineer)em.createQuery("SELECT e from Engineer e JOIN e.bestFriend b WHERE e.title is not null").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(Engineer.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(emp.getId()));
        tlQuery.addJoinedAttribute(tlQuery.getExpressionBuilder().get("bestFriend"));
        
        Engineer tlEmp = (Engineer)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("Join Subclass Inheritance Test Failed", comparer.compareObjects(emp, tlEmp));                 
    }

    public void testJoinFetchSuperClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp = (Employee)em.createQuery("SELECT e from Employee e JOIN FETCH e.projects").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(Employee.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(emp.getId()));
        tlQuery.addJoinedAttribute(tlQuery.getExpressionBuilder().anyOf("projects"));
        
        Employee tlEmp = (Employee)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("Join superclass Inheritance Test Failed", comparer.compareObjects(emp, tlEmp));                 
    }

    public void testJoinFetchSubClass() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Engineer emp = (Engineer)em.createQuery("SELECT e from Engineer e JOIN FETCH e.bestFriend").getResultList().get(0);
        clearCache();
        ReadObjectQuery tlQuery = new ReadObjectQuery(Engineer.class);
        tlQuery.setSelectionCriteria(tlQuery.getExpressionBuilder().get("id").equal(emp.getId()));
        tlQuery.addJoinedAttribute(tlQuery.getExpressionBuilder().get("bestFriend"));
        
        Engineer tlEmp = (Engineer)em.getActiveSession().executeQuery(tlQuery);
        Assert.assertTrue("Join Subclass Inheritance Test Failed", comparer.compareObjects(emp, tlEmp));                 
    }
    
    public void testLeftJoinFetchSubClass() {
        // Added for GF issue # 3159.
        // Previously this query would throw a Query exception (invalidTableForFieldInExpression)
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        em.createQuery("SELECT DISTINCT e FROM Company c INNER JOIN c.engineers e LEFT JOIN FETCH e.desktops LEFT JOIN FETCH e.laptops WHERE c.name = 'XYZ' AND e.name = 'Lucky'").getResultList();
    }

    /**
     * Checks, that the selection criteria for joined inheritance is well-formed,
     * i.e. all tables are joined.
     * See issue 860.
     */
    public void testJoinedInheritance() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        String ejbqlString = "SELECT OBJECT(b) FROM BBB b WHERE b.foo = ?1";
        // query throws exception, if result not unique!
        em.createQuery(ejbqlString).setParameter(1, "bar").getSingleResult();
    }
    
    public void testJoinedInheritanceWithLeftOuterJoin1() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String ejbqlString = "SELECT t0.maxSpeed, t0.color, t0.description, t0.fuelCapacity, t0.fuelType, t0.id, t0.passengerCapacity, t1.name, t1.id FROM SportsCar t0 LEFT OUTER JOIN t0.owner t1";
        try {
            em.createQuery(ejbqlString).getResultList();
        } catch (Exception e) {
            fail("Error occurred on a left outer join sql expression on a joined inheritance test: " + e.getCause());
        }
    }
    
    public void testJoinedInheritanceWithLeftOuterJoin2() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String ejbqlString = "SELECT t0.color, t0.description, t0.fuelCapacity, t0.fuelType, t0.id, t0.passengerCapacity, t1.name, t1.id FROM FueledVehicle t0 LEFT OUTER JOIN t0.owner t1";
        try {
            em.createQuery(ejbqlString).getResultList();
        } catch (Exception e) {
            fail("Error occurred on a left outer join sql expression on a joined inheritance test: " + e.getCause());
        }
    }
    
    public void testJoinedInheritanceWithLeftOuterJoin3() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String ejbqlString = "SELECT t0.color, t0.description, t0.fuelCapacity, t0.fuelType, t0.id, t0.passengerCapacity, t1.name, t1.id FROM Bus t0 LEFT OUTER JOIN t0.busDriver t1";
        try {
            em.createQuery(ejbqlString).getResultList();
        } catch (Exception e) {
            fail("Error occurred on a left outer join sql expression on a joined inheritance test: " + e.getCause());
        }
    }

    public static void main(String[] args) {
         junit.swingui.TestRunner.main(args);
    }

    // Helper methods and classes for constructor query test cases
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
