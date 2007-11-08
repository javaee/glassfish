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


import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.toplink.essentials.sessions.Session;

import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;

import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.threetier.Server;
import javax.persistence.Query;
import javax.persistence.EntityManager;

/**
 * <p>
 * <b>Purpose</b>: Test EJBQL parameter functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for EJBQL parameter functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
 
public class JUnitEJBQLParameterTestSuite extends JUnitTestCase {  
  
    static JUnitDomainObjectComparer comparer; //the global comparer object used in all tests
  
    public JUnitEJBQLParameterTestSuite()
    {
        super();
    }
  
    public JUnitEJBQLParameterTestSuite(String name)
    {
        super(name);
    }
  
    //This method is run at the start of EVERY test case method
    public void setUp()
    {

    }
  
    //This method is run at the end of EVERY test case method
    public void tearDown()
    {
        clearCache();
    }
  
    //This suite contains all tests contained in this class
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.setName("JUnitEJBQLParameterTestSuite");
        suite.addTest(new JUnitEJBQLParameterTestSuite("multipleParameterTest"));
        suite.addTest(new JUnitEJBQLParameterTestSuite("updateEnumParameter"));
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
                
                new AdvancedTableCreator().replaceTables(session);
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();
                
                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());              
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);       
            }            
        };    
    }
  
    //Test case for selecting employee from the database using parameters 
    public void multipleParameterTest()
    {          
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
        Employee employee = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());
        Vector expectedResult = new Vector();
        expectedResult.add(employee);
      
        Query query = em.createQuery("SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName = ?1 AND emp.id = ?3");
        query.setParameter(1, employee.getFirstName());
        query.setParameter(3, employee.getId());
        List result = query.getResultList();
        
        Assert.assertTrue("Multiple Parameter Test Case Failed", comparer.compareObjects(result, expectedResult));
    }
           
    // Test for GF#1123 - UPDATE with JPQL does not handle enums correctly.
    public void updateEnumParameter() {
        EntityManager em = createEntityManager();

        int nrOfEmps = executeJPQLReturningInt(
            em, "SELECT COUNT(e) FROM Employee e WHERE e.period.endDate IS NULL");

        // test query
        String update = "UPDATE Employee e SET e.status = :status, e.payScale = :payScale WHERE e.period.endDate IS NULL";
        em.getTransaction().begin();
        try {
            Query q = em.createQuery(update);
            q.setParameter("status", Employee.EmployeeStatus.FULL_TIME);
            q.setParameter("payScale", Employee.SalaryRate.SENIOR);
            int updated = q.executeUpdate();
            assertEquals("wrong number of updated instances", nrOfEmps, updated);

            // check database changes
            Query q2 = em.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.period.endDate IS NULL AND e.status = :status AND e.payScale = :payScale");
            q2.setParameter("status", Employee.EmployeeStatus.FULL_TIME);
            q2.setParameter("payScale", Employee.SalaryRate.SENIOR);
            int nr = ((Number)q2.getSingleResult()).intValue();
            assertEquals("unexpected number of changed values in the database", nrOfEmps, nr);
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
        }
    }

    /** Helper method executing a JPQL query retuning an int value. */
    private int executeJPQLReturningInt(EntityManager em, String jpql) 
    {
        Query q = em.createQuery(jpql);
        Object result = q.getSingleResult();
        return ((Number)result).intValue();
    }

    public static void main(String[] args)
    {
        junit.swingui.TestRunner.main(args);
    }
}
