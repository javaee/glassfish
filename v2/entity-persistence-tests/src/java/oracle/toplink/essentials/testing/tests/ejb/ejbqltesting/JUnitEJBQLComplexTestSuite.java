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

package oracle.toplink.essentials.testing.tests.ejb.ejbqltesting;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.persistence.Query;

import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;

import oracle.toplink.essentials.sessions.Session;

import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;

import oracle.toplink.essentials.queryframework.ReportQuery;

import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Man;
import oracle.toplink.essentials.testing.models.cmp3.advanced.PartnerLinkPopulator;

import oracle.toplink.essentials.testing.models.cmp3.advanced.LargeProject;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Project;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;

/**
 * <p>
 * <b>Purpose</b>: Test complex EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for complex EJBQL functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */

public class JUnitEJBQLComplexTestSuite extends JUnitTestCase 
{
    static JUnitDomainObjectComparer comparer;        //the global comparer object used in all tests
  
    public JUnitEJBQLComplexTestSuite()
    {
        super();
    }
  
    public JUnitEJBQLComplexTestSuite(String name)
    {
        super(name);
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
        suite.setName("JUnitEJBQLComplexTestSuite");
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexABSTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexABSWithParameterTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("compexInTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexLengthTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexLikeTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNotInTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNotLikeTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexParameterTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexReverseAbsTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexReverseLengthTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexReverseParameterTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexReverseSqrtTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexSqrtTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexStringInTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexStringNotInTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexSubstringTest"));    
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexLocateTest"));    
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNestedOneToManyUsingInClause"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexUnusedVariableTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexJoinTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexMultipleJoinOfSameRelationship"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexMultipleLeftOuterJoinOfSameRelationship"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexFetchJoinTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexOneToOneFetchJoinTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexSelectRelationshipTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexConstructorTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexConstructorVariableTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexConstructorRelationshipTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexConstructorAggregatesTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexConstructorCountOnJoinedVariableTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexResultPropertiesTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexInSubqueryTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexExistsTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNotExistsTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexMemberOfTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNotMemberOfTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNavigatingEmbedded"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNavigatingTwoLevelOfEmbeddeds"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexNamedQueryResultPropertiesTest"));
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexOuterJoinQuery"));
	/* Removed because this functionality requires implementation in ReportQuery
        suite.addTest(new JUnitEJBQLComplexTestSuite("complexInheritanceTest"));
	

        suite.addTest(new JUnitEJBQLComplexTestSuite("complexInheritanceUsingNamedQueryTest"));
    */
        
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

                //create a new PartnerLinkPopulator
                PartnerLinkPopulator partnerLinkPopulator = new PartnerLinkPopulator();
                
                new AdvancedTableCreator().replaceTables(session);
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();
                
                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());              
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);       

                //Populate the tables
                partnerLinkPopulator.buildExamples();
                
                //Persist the examples in the database
                partnerLinkPopulator.persistExample(session);       
            }            
        };    
  }


    public void complexABSTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).lastElement();
        
        clearCache();
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "(ABS(emp.salary) = ";
	    ejbqlString = ejbqlString + emp1.getSalary() + ")";
        ejbqlString = ejbqlString + " OR (ABS(emp.salary) = ";
        ejbqlString = ejbqlString + emp2.getSalary() + ")";
        
        Vector expectedResult = new Vector();
        expectedResult.add(emp1);
        expectedResult.add(emp2);
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
   
        Assert.assertTrue("Complex ABS test failed", comparer.compareObjects(result, expectedResult));                 
        
    }

    public void complexABSWithParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        clearCache();
        Query q = em.createQuery(
            "SELECT emp FROM Employee emp WHERE emp.salary = ABS(:sal)");
        q.setParameter("sal", -emp.getSalary());
        List<Employee> result = (List)q.getResultList();
        boolean found = false;
        for (Employee e : result) {
            if (e.equals(emp)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Complex ABS with parameter test failed", found);                 
    }
    
    public void compexInTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        Employee emp3 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(2);
        
	    Vector expectedResult = new Vector();
	    Vector idVector = new Vector();
	    idVector.add(emp1.getId());
	    idVector.add(emp2.getId());
	    idVector.add(emp3.getId());
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("id").in(idVector);
	    raq.setSelectionCriteria(whereClause);
	    expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
	    clearCache();
    	String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id IN (";
	    ejbqlString = ejbqlString + emp1.getId().toString() + ", "; 
	    ejbqlString = ejbqlString + emp2.getId().toString() + ", "; 
	    ejbqlString = ejbqlString + emp3.getId().toString();
	    ejbqlString = ejbqlString + ")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex IN test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexLengthTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Assert.assertFalse("Warning SQL doesnot support LENGTH function",  ((Session) JUnitTestCase.getServerSession()).getPlatform().isSQLServer());
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        clearCache();
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "(LENGTH(emp.firstName) = ";
	    ejbqlString = ejbqlString + expectedResult.getFirstName().length() + ")";
        ejbqlString = ejbqlString + " AND ";
        ejbqlString = ejbqlString + "(LENGTH(emp.lastName) = ";
        ejbqlString = ejbqlString + expectedResult.getLastName().length() + ")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Complex Length test failed", comparer.compareObjects(result, expectedResult));                 
    }
    
    public void complexLikeTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        
        String firstName = emp.getFirstName();
	    String partialFirstName = emp.getFirstName().substring(0, 1);
	    partialFirstName = partialFirstName + "_";
	    partialFirstName = partialFirstName + firstName.substring(2, 4);
	    partialFirstName = partialFirstName + "%";
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("firstName").like(partialFirstName);
	    raq.setSelectionCriteria(whereClause);
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
	    clearCache();
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName LIKE \"" + partialFirstName + "\"";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();  
        
        Assert.assertTrue("Complex Like test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexNotInTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        Employee emp3 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(2);
        
        ExpressionBuilder builder = new ExpressionBuilder();
        
        Vector idVector = new Vector();
	    idVector.add(emp1.getId());   
	    idVector.add(emp2.getId());        
	    idVector.add(emp3.getId());        
	    
	    Expression whereClause = builder.get("id").notIn(idVector);
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq);
	    clearCache();
	    
    	String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id NOT IN (";
	    ejbqlString = ejbqlString + emp1.getId().toString() + ", "; 
	    ejbqlString = ejbqlString + emp2.getId().toString() + ", "; 
	    ejbqlString = ejbqlString + emp3.getId().toString();
	    ejbqlString = ejbqlString + ")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Complex Not IN test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexNotLikeTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        
        String firstName = emp.getFirstName();
	    String partialFirstName = emp.getFirstName().substring(0, 1);
	    partialFirstName = partialFirstName + "_";
	    partialFirstName = partialFirstName + firstName.substring(2, 4);
	    partialFirstName = partialFirstName + "%";
	    
	    ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.get("firstName").notLike(partialFirstName);
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
	    clearCache();	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName NOT LIKE \"" + partialFirstName + "\"";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Complex Not LIKE test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
                
        String firstName = "firstName";
        String lastName = "lastName";
        
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get(firstName).equal(builder.getParameter(firstName));
        whereClause = whereClause.and(builder.get(lastName).equal(builder.getParameter(lastName)));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.addArgument(firstName);
        raq.addArgument(lastName);
        
	    Vector parameters = new Vector();
	    parameters.add(emp.getFirstName());
	    parameters.add(emp.getLastName());
	    
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq, parameters);
	    clearCache();
        
	    emp = (Employee)expectedResult.firstElement();
	    
	    // Set up the EJBQL using the retrieved employees
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "emp.firstName = ?1 ";
	    ejbqlString = ejbqlString + " AND ";
	    ejbqlString = ejbqlString + "emp.lastName = ?2";
        
        List result = (List) em.createQuery(ejbqlString).setParameter(1,emp.getFirstName()).setParameter(2,emp.getLastName()).getResultList();
    
        Assert.assertTrue("Complex Paramter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexReverseAbsTest()
    {
       oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        clearCache();
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + emp1.getSalary();
	    ejbqlString = ejbqlString + " = ABS(emp.salary)";
	    ejbqlString = ejbqlString + " OR ";
        ejbqlString = ejbqlString + emp2.getSalary();
        ejbqlString = ejbqlString + " = ABS(emp.salary)";
          
        Vector expectedResult = new Vector();
        expectedResult.add(emp1);
        expectedResult.add(emp2);
        
        List result = (List) em.createQuery(ejbqlString).getResultList();      
        
        Assert.assertTrue("Complex reverse ABS test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexReverseLengthTest()
    {
        
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Assert.assertFalse("Warning SQL doesnot support LENGTH function",  ((Session) JUnitTestCase.getServerSession()).getPlatform().isSQLServer());
        
        Employee expectedResult = (Employee) em.getActiveSession().readAllObjects(Employee.class).firstElement();
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + expectedResult.getFirstName().length();
        ejbqlString = ejbqlString + " = LENGTH(emp.firstName)";
	    ejbqlString = ejbqlString + " AND ";
        ejbqlString = ejbqlString + expectedResult.getLastName().length();
        ejbqlString = ejbqlString + " = LENGTH(emp.lastName)";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
                        
        Assert.assertTrue("Complex reverse Length test failed", comparer.compareObjects(result, expectedResult));                         
    }
    
    public void complexReverseParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        
        String firstName = "firstName";
        String lastName = "lastName";
        
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get(firstName).equal(builder.getParameter(firstName));
        whereClause = whereClause.and(builder.get(lastName).equal(builder.getParameter(lastName)));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.addArgument(firstName);
        raq.addArgument(lastName);
        
	    Vector parameters = new Vector();
	    parameters.add(emp.getFirstName());
	    parameters.add(emp.getLastName());
	    
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq, parameters);
	    
        clearCache();
        
	    emp = (Employee)expectedResult.firstElement();
	    
	    // Set up the EJBQL using the retrieved employees
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "?1 = emp.firstName";
	    ejbqlString = ejbqlString + " AND ";
	    ejbqlString = ejbqlString + "?2 = emp.lastName";
        
        List result = (List) em.createQuery(ejbqlString).setParameter(1,emp.getFirstName()).setParameter(2,emp.getLastName()).getResultList();

        Assert.assertTrue("Complex Reverse Paramter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexReverseSqrtTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        ReadAllQuery raq = new ReadAllQuery();
        ExpressionBuilder expbldr = new ExpressionBuilder();
        Expression whereClause1 = expbldr.get("lastName").equal("TestCase1");
        Expression whereClause2 = expbldr.get("lastName").equal("TestCase2");
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause1.or(whereClause2));
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
        Employee emp1 = (Employee) expectedResult.elementAt(0);
        Employee emp2 = (Employee) expectedResult.elementAt(1);
        
	    double salarySquareRoot1 = Math.sqrt((new Double(emp1.getSalary()).doubleValue()));
	    double salarySquareRoot2 = Math.sqrt((new Double(emp2.getSalary()).doubleValue()));
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + salarySquareRoot1;
	    ejbqlString = ejbqlString + " = SQRT(emp.salary)";
		ejbqlString = ejbqlString + " OR ";
		ejbqlString = ejbqlString + salarySquareRoot2;
		ejbqlString = ejbqlString + " = SQRT(emp.salary)";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Complex Reverse Square Root test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void complexSqrtTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        ReadAllQuery raq = new ReadAllQuery();
        ExpressionBuilder expbldr = new ExpressionBuilder();
        Expression whereClause1 = expbldr.get("lastName").equal("TestCase1");
        Expression whereClause2 = expbldr.get("lastName").equal("TestCase2");
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause1.or(whereClause2));
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
        Employee emp1 = (Employee) expectedResult.elementAt(0);
        Employee emp2 = (Employee) expectedResult.elementAt(1);
        
        double salarySquareRoot1 = Math.sqrt((new Double(emp1.getSalary()).doubleValue()));
	    double salarySquareRoot2 = Math.sqrt((new Double(emp2.getSalary()).doubleValue()));

	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "(SQRT(emp.salary) = ";
	    ejbqlString = ejbqlString + salarySquareRoot1 + ")";
		ejbqlString = ejbqlString + " OR ";
		ejbqlString = ejbqlString + "(SQRT(emp.salary) = ";
		ejbqlString = ejbqlString + salarySquareRoot2 + ")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Complex Square Root test failed", comparer.compareObjects(result, expectedResult));      
    }
    
    public void complexStringInTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        Employee emp3 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(2);
        
        Vector fnVector = new Vector();
	    fnVector.add(emp1.getFirstName());
	    fnVector.add(emp2.getFirstName());
	    fnVector.add(emp3.getFirstName());
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("firstName").in(fnVector);
	    raq.setSelectionCriteria(whereClause);
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq);
	    
        clearCache();
        
    	String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName IN (";
	    ejbqlString = ejbqlString + "\"" + emp1.getFirstName() + "\"" + ", "; 
	    ejbqlString = ejbqlString + "\"" + emp2.getFirstName() + "\"" + ", "; 
	    ejbqlString = ejbqlString + "\"" + emp3.getFirstName() + "\"" ;
	    ejbqlString = ejbqlString + ")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Complex String In test failed", comparer.compareObjects(result, expectedResult));      
        
    }
    
    public void complexStringNotInTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
	    Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        Employee emp3 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(2);
        
	    
	    ExpressionBuilder builder = new ExpressionBuilder();
	    
        Vector nameVector = new Vector();
	    nameVector.add(emp1.getFirstName());   
	    nameVector.add(emp2.getFirstName());        
	    nameVector.add(emp3.getFirstName());        
	    
	    
	    Expression whereClause = builder.get("firstName").notIn(nameVector);
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
    	String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName NOT IN (";
	    ejbqlString = ejbqlString + "\"" + emp1.getFirstName() + "\"" + ", "; 
	    ejbqlString = ejbqlString + "\"" + emp2.getFirstName() + "\"" + ", "; 
	    ejbqlString = ejbqlString + "\"" + emp3.getFirstName() + "\"" ;
	    ejbqlString = ejbqlString + ")";
        
         List result = (List) em.createQuery(ejbqlString).getResultList();

         Assert.assertTrue("Complex String Not In test failed", comparer.compareObjects(result, expectedResult));      
        
    }
    
    public void complexSubstringTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        
        String firstNamePart, lastNamePart;
	    String ejbqlString;

	    firstNamePart = expectedResult.getFirstName().substring(0, 2);
        
	    lastNamePart = expectedResult.getLastName().substring(0, 1);
        
	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "(SUBSTRING(emp.firstName, 1, 2) = ";//changed from 0, 2 to 1, 2(ZYP)
	    ejbqlString = ejbqlString + "\"" + firstNamePart + "\")";
        ejbqlString = ejbqlString + " AND ";
        ejbqlString = ejbqlString + "(SUBSTRING(emp.lastName, 1, 1) = ";//changed from 0, 1 to 1, 1(ZYP)
        ejbqlString = ejbqlString + "\"" + lastNamePart + "\")";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Sub String test failed", comparer.compareObjects(result, expectedResult));              
    }
    
    public void complexLocateTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String jpql = "SELECT e FROM Employee e WHERE e.firstName = 'Emanual' AND e.lastName = 'Smith'";
        Employee expectedResult = (Employee)em.createQuery(jpql).getSingleResult();

        jpql = "SELECT e FROM Employee e WHERE LOCATE('manual', e.firstName) = 2 AND e.lastName = 'Smith'";
        Employee result = (Employee)em.createQuery(jpql).getSingleResult();
        Assert.assertTrue("Complex LOCATE(String, String) test failed", result.equals(expectedResult));
        
        jpql = "SELECT e FROM Employee e WHERE LOCATE('a', e.firstName, 4) = 6 AND e.lastName = 'Smith'";
        result = (Employee)em.createQuery(jpql).getSingleResult();
        Assert.assertTrue("Complex LOCATE(String, String) test failed", result.equals(expectedResult));
    }
    
    public void complexNestedOneToManyUsingInClause()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        
        ExpressionBuilder builder = new ExpressionBuilder();
    	Expression whereClause = builder.anyOf("managedEmployees").anyOf("projects").
    	    get("name").equal("Enterprise");
    	ReadAllQuery readQuery = new ReadAllQuery();
    	readQuery.dontMaintainCache();
    	readQuery.setReferenceClass(Employee.class);
        readQuery.setSelectionCriteria(whereClause);

    	Vector expectedResult = (Vector)em.getActiveSession().executeQuery(readQuery);
    	
        clearCache();
        
    	String ejbqlString;
    	ejbqlString = "SELECT OBJECT(emp) FROM Employee emp, " +
    	    "IN(emp.managedEmployees) mEmployees, IN(mEmployees.projects) projects " +
    	    "WHERE projects.name = 'Enterprise'";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Nested One To Many Using In Clause test failed", comparer.compareObjects(result, expectedResult));              
            
    }

    public void complexUnusedVariableTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        ReportQuery reportQuery = new ReportQuery();
    	reportQuery.dontMaintainCache();
        reportQuery.setShouldReturnWithoutReportQueryResult(true);
    	reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder builder = reportQuery.getExpressionBuilder();
        reportQuery.addNonFetchJoinedAttribute(builder.get("address"));
        reportQuery.addItem("emp", builder);
    	Vector expectedResult = (Vector)em.getActiveSession().executeQuery(reportQuery);
    	
        clearCache();
        
    	String ejbqlString;
    	ejbqlString = "SELECT emp FROM Employee emp JOIN emp.address a";
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Unused Variable test failed", comparer.compareObjects(result, expectedResult));              
            
    }

    public void complexJoinTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        Collection emps = em.getActiveSession().readAllObjects(Employee.class);
        Employee empWithManager = null;
        Employee empWithOutManager = null;
        // find an employee w/ and w/o manager
        for (Iterator i = emps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Employee manager = e.getManager();
            if (manager != null) {
                if (empWithManager == null) {
                    empWithManager = e;
                }
            } else {
                if (empWithOutManager == null) {
                    empWithOutManager = e;
                }
            }
            if ((empWithManager != null) && (empWithOutManager != null)) {
                break;
            }
        }

        // Select the related manager of empWithOutManager and empWithManager
        // This should return empWithManager's manager, because the manager
        // identification variable m is defined as inner join
        String ejbqlString = "SELECT m FROM Employee emp JOIN emp.manager m WHERE emp.id IN (:id1, :id2)";
        Query query = em.createQuery(ejbqlString);
        query.setParameter("id1", empWithOutManager.getId());
        query.setParameter("id2", empWithManager.getId());
        List result = (List) query.getResultList();
        List expectedResult = Arrays.asList(new Employee[] {empWithManager.getManager()});
        Assert.assertTrue("Complex Join test failed", comparer.compareObjects(result, expectedResult));

        // Select the related manager of empWithOutManager and empWithManager 
        // This should return empWithManager's manager, because the manager
        // identification variable m is defined as outer join
        ejbqlString = "SELECT m FROM Employee emp LEFT OUTER JOIN emp.manager m WHERE emp.id IN (:id1, :id2)";
        query = em.createQuery(ejbqlString);
        query.setParameter("id1", empWithOutManager.getId());
        query.setParameter("id2", empWithManager.getId());
        result = (List) query.getResultList();
        expectedResult = Arrays.asList(new Employee[] {empWithManager.getManager(), null});
        Assert.assertTrue("Complex Join test failed", comparer.compareObjects(result, expectedResult));
    }

    /**
     * glassfish issue 2867
     */
    public void complexMultipleJoinOfSameRelationship()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String jpql = "SELECT p1, p2 FROM Employee emp JOIN emp.phoneNumbers p1 JOIN emp.phoneNumbers p2 " +
                      "WHERE p1.type = 'Pager' AND p2.areaCode = '613'";
        Query query = em.createQuery(jpql);
        Object[] result = (Object[]) query.getSingleResult();
        Assert.assertTrue("Complex multiple JOIN of same relationship test failed", 
                          (result[0] != result[1]));
    }
    /**
     * glassfish issue 3580
     */
    public void complexMultipleLeftOuterJoinOfSameRelationship()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String jpql = "SELECT p1, p2 FROM Employee emp LEFT JOIN emp.phoneNumbers p1 LEFT JOIN emp.phoneNumbers p2 " +
                      "WHERE p1.type = 'Pager' AND p2.areaCode = '613'";
        Query query = em.createQuery(jpql);
        Object[] result = (Object[]) query.getSingleResult();
        Assert.assertTrue("Complex multiple JOIN of same relationship test failed", 
                          (result[0] != result[1]));
    }

    public void complexFetchJoinTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
         
        Employee emp = (Employee)em.getServerSession().readAllObjects(Employee.class).firstElement();
        ReportQuery reportQuery = new ReportQuery();
    	reportQuery.dontMaintainCache();
        reportQuery.setShouldReturnWithoutReportQueryResult(true);
    	reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder builder = reportQuery.getExpressionBuilder();
        List joins = new ArrayList(1);
        joins.add(builder.get("address"));
        reportQuery.addItem("emp", builder, joins);    
    	Vector expectedResult = (Vector)em.getServerSession().executeQuery(reportQuery);
    	
        clearCache();
        
    	String ejbqlString;
    	ejbqlString = "SELECT emp FROM Employee emp JOIN FETCH emp.address";
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Fetch Join test failed", comparer.compareObjects(result, expectedResult));              
            
    }

    /**
     * Testing glassfish issue 2881
     */    
    public void complexOneToOneFetchJoinTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        List<Man> allMen = em.getServerSession().readAllObjects(Man.class);
        List<Integer> allMenIds = new ArrayList(allMen.size());
        for (Man man : allMen) {
            allMenIds.add((man != null) ? man.getId() : null);
        }
        Collections.sort(allMenIds);
        clearCache();
        
    	String ejbqlString = "SELECT m FROM Man m LEFT JOIN FETCH m.partnerLink";
        List<Man> result = (List) em.createQuery(ejbqlString).getResultList();
        List<Integer> ids = new ArrayList(result.size());
        for (Man man : result) {
            ids.add((man != null) ? man.getId() : null);
        }
        Collections.sort(ids);

        // compare ids, because comparer does not know class Man
        Assert.assertEquals("Complex OneToOne Fetch Join test failed", 
                            allMenIds, ids);
    }
    
    public void complexSelectRelationshipTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
        
        Collection emps = em.getActiveSession().readAllObjects(Employee.class);
        Employee empWithManager = null;
        Employee empWithOutManager = null;
        // find an employee w/ and w/o manager
        for (Iterator i = emps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Employee manager = e.getManager();
            if (manager != null) {
                if (empWithManager == null) {
                    empWithManager = e;
                }
            } else {
                if (empWithOutManager == null) {
                    empWithOutManager = e;
                }
            }
            if ((empWithManager != null) && (empWithOutManager != null)) {
                break;
            }
        }

        // constructor query including relationship field
    	String ejbqlString = "SELECT emp.manager FROM Employee emp WHERE emp.id = :id";
        Query query = em.createQuery(ejbqlString);

        // execute query using employee with manager
        query.setParameter("id", empWithManager.getId());
        Employee result = (Employee)query.getSingleResult();
        Assert.assertEquals("Select Relationship Test Case Failed (employee with manager)", 
                            empWithManager.getManager(), result);

        // execute query using employee with manager
        query.setParameter("id", empWithOutManager.getId());
        result = (Employee)query.getSingleResult();
        Assert.assertNull("Select Relationship Test Case Failed (employee without manager)",
                          result);
    }

    public void complexConstructorTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();

        // simple constructor query
    	String ejbqlString = "SELECT NEW oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite.EmployeeDetail(emp.firstName, emp.lastName) FROM Employee emp WHERE emp.id = :id";
        Query query = em.createQuery(ejbqlString);
        query.setParameter("id", emp.getId());
        EmployeeDetail result = (EmployeeDetail)query.getSingleResult();
        EmployeeDetail expectedResult = new EmployeeDetail(emp.getFirstName(), emp.getLastName());

        Assert.assertTrue("Constructor Test Case Failed", result.equals(expectedResult));
    }

    public void complexConstructorVariableTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();

        // constructor query using a variable as argument
    	String jpqlString = "SELECT NEW oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite.EmployeeDetail(emp) FROM Employee emp WHERE emp.id = :id";
        Query query = em.createQuery(jpqlString);
        query.setParameter("id", emp.getId());
        EmployeeDetail result = (EmployeeDetail)query.getSingleResult();
        EmployeeDetail expectedResult = new EmployeeDetail(emp);

        Assert.assertTrue("Constructor with variable argument Test Case Failed", result.equals(expectedResult));
    }

    public void complexConstructorRelationshipTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
        
        Collection emps = em.getActiveSession().readAllObjects(Employee.class);
        Employee empWithManager = null;
        Employee empWithOutManager = null;
        // find an employee w/ and w/o manager
        for (Iterator i = emps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Employee manager = e.getManager();
            if (manager != null) {
                if (empWithManager == null) {
                    empWithManager = e;
                }
            } else {
                if (empWithOutManager == null) {
                    empWithOutManager = e;
                }
            }
            if ((empWithManager != null) && (empWithOutManager != null)) {
                break;
            }
        }

        // constructor query including relationship field
    	String ejbqlString = "SELECT NEW oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite.EmployeeDetail(emp.firstName, emp.lastName, emp.manager) FROM Employee emp WHERE emp.id = :id";
        Query query = em.createQuery(ejbqlString);

        // execute query using employee with manager
        query.setParameter("id", empWithManager.getId());
        EmployeeDetail result = (EmployeeDetail)query.getSingleResult();
        EmployeeDetail expectedResult = new EmployeeDetail(
            empWithManager.getFirstName(), empWithManager.getLastName(), 
            empWithManager.getManager());
        Assert.assertTrue("Constructor Relationship Test Case Failed (employee with manager)", 
                          result.equals(expectedResult));

        // execute query using employee with manager
        query.setParameter("id", empWithOutManager.getId());
        result = (EmployeeDetail)query.getSingleResult();
        expectedResult = new EmployeeDetail(
            empWithOutManager.getFirstName(), empWithOutManager.getLastName(), 
            empWithOutManager.getManager());
        Assert.assertTrue("Constructor Relationship Test Case Failed (employee without manager)", 
                          result.equals(expectedResult));
    }

    public void complexConstructorAggregatesTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 

        Collection emps = em.getActiveSession().readAllObjects(Employee.class);
        Employee emp = null;
        // find an employee with managed employees
        for (Iterator i = emps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Collection managed = e.getManagedEmployees();
            if ((managed != null) && (managed.size() > 0)) {
                emp = e;
                break;
            }
        }

        // constructor query using aggregates
        String ejbqlString = "SELECT NEW oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite.LongHolder(SUM(emp.salary), COUNT(emp)) FROM Employee emp WHERE emp.manager.id = :id";
        Query query = em.createQuery(ejbqlString);
        query.setParameter("id", emp.getId());
        LongHolder result = (LongHolder)query.getSingleResult();

        // calculate expected result
        Collection managed = emp.getManagedEmployees();
        int count = 0;
        int sum = 0;
        if (managed != null) {
            count = managed.size();
            for (Iterator i = managed.iterator(); i.hasNext();) {
                Employee e = (Employee)i.next();
                sum += e.getSalary();
            }
        }
        LongHolder expectedResult = new LongHolder(new Long(sum), new Long(count));
        
        Assert.assertTrue("Constructor with aggregates argument Test Case Failed", result.equals(expectedResult));
    }

    public void complexConstructorCountOnJoinedVariableTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 

        // find all employees with managed employees
        Collection emps = em.getActiveSession().readAllObjects(Employee.class);
        List<EmployeeDetail> expectedResult = new ArrayList<EmployeeDetail>();
        for (Iterator i = emps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Collection managed = e.getManagedEmployees();
            if ((managed != null) && (managed.size() > 0)) {
                EmployeeDetail d = new EmployeeDetail(
                    e.getFirstName(), e.getLastName(), new Long(managed.size()));
                expectedResult.add(d);
            }
        }
        
        // constructor query using aggregates
        String jpql = "SELECT NEW oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite.EmployeeDetail(emp.firstName, emp.lastName, COUNT(m)) FROM Employee emp JOIN emp.managedEmployees m GROUP BY emp.firstName, emp.lastName";
        Query query = em.createQuery(jpql);
        List<EmployeeDetail> result = query.getResultList();

        Assert.assertTrue("Constructor with aggregates argument Test Case Failed", 
                          comparer.compareObjects(result, expectedResult));
    }
    
    public void complexResultPropertiesTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        String ejbql = "SELECT e FROM Employee e ORDER BY e.id";
        Query query = em.createQuery(ejbql);
        List allEmps = query.getResultList();
        int nrOfEmps = allEmps.size();
        List result = null;
        List expectedResult = null;
        int firstResult = 2;
        int maxResults = nrOfEmps - 1;

        // Test setFirstResult
        query = em.createQuery(ejbql);
        query.setFirstResult(firstResult);
        result = query.getResultList();
        expectedResult = allEmps.subList(firstResult, nrOfEmps);
        Assert.assertTrue("Query.setFirstResult Test Case Failed", result.equals(expectedResult));

        // Test setMaxResults
        query = em.createQuery(ejbql);
        query.setMaxResults(maxResults);
        result = query.getResultList();
        expectedResult = allEmps.subList(0, maxResults);
        Assert.assertTrue("Query.setMaxResult Test Case Failed", result.equals(expectedResult));

        // Test setFirstResult and setMaxResults
        maxResults = nrOfEmps - firstResult - 1;
        query = em.createQuery(ejbql);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        result = query.getResultList();
        expectedResult = allEmps.subList(firstResult, nrOfEmps - 1);
        Assert.assertTrue("Query.setFirstResult and Query.setMaxResults Test Case Failed", result.equals(expectedResult));
        
    }

    public void complexNamedQueryResultPropertiesTest() 
    {
        //This new added test case is for glassFish bug 2689 
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Query query = em.createNamedQuery("findAllEmployeesOrderById");
        List allEmps = query.getResultList();
        int nrOfEmps = allEmps.size();
        int firstResult = 2;

        // Test case 1. setFirstResult and MaxResults
        Query query1 = em.createNamedQuery("findAllEmployeesOrderById");
        query1.setFirstResult(firstResult);
        query1.setMaxResults(nrOfEmps - 1);
        List result1 = query1.getResultList();
        List expectedResult1 = allEmps.subList(firstResult, nrOfEmps);
        Assert.assertTrue("Query1 set FirstResult and MaxResults Test Case Failed", result1.equals(expectedResult1));

        // Test case 2. The expected result should be exactly same as test case 1 
        // since the firstResult and maxresults setting keep unchange.
        result1 = query1.getResultList();
        expectedResult1= allEmps.subList(firstResult, nrOfEmps);
        Assert.assertTrue("Query1 without setting Test Case Failed", result1.equals(expectedResult1));

        // Test case 3. The FirstResult and MaxResults are changed for same query1.
        query1.setFirstResult(1);
        query1.setMaxResults(nrOfEmps - 2);
        result1 = query1.getResultList();
        expectedResult1 = allEmps.subList(1, nrOfEmps-1);
        Assert.assertTrue("Query1.setFirstResult Test Case Failed", result1.equals(expectedResult1));
        
        
        // Test case 4. Create new query2, the query2 setting should be nothing to do
        // with query1's. In this case, query2 should use default values. 
        Query query2 = em.createNamedQuery("findAllEmployeesOrderById");
        List result2 = query2.getResultList();
        List expectedResult2 = allEmps.subList(0, nrOfEmps);
        Assert.assertTrue("Query2 without setting", result2.equals(expectedResult2));

        // Test case 5. Create query3, only has FirstResult set as zero. the maxReults use
        // default value.
        Query query3 = em.createNamedQuery("findAllEmployeesOrderById");
        query3.setFirstResult(0);
        List result3 = query3.getResultList();
        List expectedResult3 = allEmps.subList(0, nrOfEmps);
        Assert.assertTrue("Query3.set FirstResult and MaxResults Test Case Failed", result3.equals(expectedResult3));

        //Test case 6. Create query 4. firstResult should use default one. 
        Query query4 = em.createNamedQuery("findAllEmployeesOrderById");
        query4.setMaxResults(nrOfEmps-3);
        List result4 = query4.getResultList();
        List expectedResult4 = allEmps.subList(0, nrOfEmps-3);
        Assert.assertTrue("Query4 set MaxResult only Test Case Failed", result4.equals(expectedResult4));

    }
    public void complexInSubqueryTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        ReportQuery reportQuery = new ReportQuery();
        reportQuery.dontMaintainCache();
        reportQuery.setShouldReturnWithoutReportQueryResult(true);
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder builder = reportQuery.getExpressionBuilder();
        reportQuery.setSelectionCriteria(builder.get("address").get("city").equal("Ottawa"));
        reportQuery.addItem("id", builder.get("id"));    
        Vector expectedResult = (Vector)em.getServerSession().executeQuery(reportQuery);
     
        String ejbqlString = "SELECT e.id FROM Employee e WHERE e.address.city IN (SELECT a.city FROM e.address a WHERE a.city = 'Ottawa')";
        List result = (List) em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Complex IN Subquery Test Case Failed", result.equals(expectedResult));
    }
    
    public void complexExistsTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Collection allEmps = em.getActiveSession().readAllObjects(Employee.class);
        List expectedResult = new ArrayList();
        // find an employees with projects
        for (Iterator i = allEmps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Collection projects = e.getProjects();
            if ((projects != null) && (projects.size() > 0)) {
                expectedResult.add(e.getId());
            }
        }

        String ejbqlString = "SELECT e.id FROM Employee e WHERE EXISTS (SELECT p FROM e.projects p)";
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Not Exists test failed", comparer.compareObjects(result, expectedResult)); 
        
    }
    
    public void complexNotExistsTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Collection allEmps = em.getActiveSession().readAllObjects(Employee.class);
        List expectedResult = new ArrayList();
        // find an employees with projects
        for (Iterator i = allEmps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Collection projects = e.getProjects();
            if ((projects == null) || (projects.size() == 0)) {
                expectedResult.add(e.getId());
            }
        }

        String ejbqlString = "SELECT e.id FROM Employee e WHERE NOT EXISTS (SELECT p FROM e.projects p)";
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Complex Not Exists test failed", comparer.compareObjects(result, expectedResult)); 
        
    }

    public void complexMemberOfTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Collection allEmps = em.getActiveSession().readAllObjects(Employee.class);

        // MEMBER OF using self-referencing relationship
        // return employees who are incorrectly entered as reporting to themselves
        List expectedResult = new ArrayList();
        String ejbqlString = "SELECT e FROM Employee e WHERE e MEMBER OF e.managedEmployees";
        List result = (List) em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Complex MEMBER OF test failed", 
                          comparer.compareObjects(result, expectedResult)); 
        
        // find an employees with projects
        for (Iterator i = allEmps.iterator(); i.hasNext();) {
            Employee e = (Employee)i.next();
            Collection projects = e.getProjects();
            if ((projects != null) && (projects.size() > 0)) {
                expectedResult.add(e);
            }
        }
        // MEMBER of using identification variable p that is not the base
        // variable of the query 
        ejbqlString = "SELECT DISTINCT e FROM Employee e, Project p WHERE p MEMBER OF e.projects";
        result = (List) em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Complex MEMBER OF test failed", 
                          comparer.compareObjects(result, expectedResult)); 
    }
    
    public void complexNotMemberOfTest() 
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Collection allEmps = em.getActiveSession().readAllObjects(Employee.class);
        List expectedResult = new ArrayList();
        String ejbqlString = "SELECT e FROM Employee e WHERE e NOT MEMBER OF e.managedEmployees";
        List result = (List) em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Complex MEMBER OF test failed", comparer.compareObjects(result, allEmps)); 
    }
    
    public void complexInheritanceTest()
    {
    
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        ((AbstractSession) em.getActiveSession()).addAlias("ProjectBaseClass", getServerSession().getDescriptor(Project.class));
        
        Project expectedResult = (Project)em.getActiveSession().readAllObjects(Project.class).firstElement();
        String projectName = expectedResult.getName();
	    ReadObjectQuery roq = new ReadObjectQuery();
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("name").equal(projectName);
	    roq.setSelectionCriteria(whereClause);
	    roq.setReferenceClass(LargeProject.class);
	    LargeProject proj = (LargeProject)em.getActiveSession().executeQuery(roq);

        //Set criteria for EJBQL and call super-class method to construct the EJBQL query
	    String ejbqlString = "SELECT OBJECT(project) FROM ProjectBaseClass project WHERE project.name = \"" + projectName +"\"";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
        
        ((AbstractSession)em.getActiveSession()).getAliasDescriptors().remove("ProjectBaseClass");
 
        Assert.assertTrue("Complex Inheritance test failed", comparer.compareObjects(result, expectedResult));                  

    }
    
    public void complexInheritanceUsingNamedQueryTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        Project expectedResult = (Project)em.getActiveSession().readAllObjects(Project.class).firstElement();
	    
        String argument = expectedResult.getName();
	    
	    //set up query, using query framework, to return a Project object which will be compared
	    //against the Project object which is returned by the EJBQL query
	    ReadObjectQuery roq = new ReadObjectQuery();
	    roq.setReferenceClass(LargeProject.class);
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("name").equal(argument);
	    roq.setSelectionCriteria(whereClause);
	    Project proj = (Project)getServerSession().executeQuery(roq);
        
        String queryName = "findLargeProjectByNameEJBQL";
        
        Session uow = (Session)em.getActiveSession();
        
        if (!(em.getActiveSession().containsQuery(queryName))) {
            ((AbstractSession)em.getActiveSession()).addAlias("ProjectBaseClass", getServerSession().getDescriptor(Project.class));

            //Named query must be built and registered with the session
            ReadObjectQuery query = new ReadObjectQuery();
            query.setEJBQLString("SELECT OBJECT(project) FROM ProjectBaseClass project WHERE project.name = ?1");
            query.setName(queryName);
            query.addArgument("1");
            query.setReferenceClass(Project.class);       
            uow.addQuery("findLargeProjectByNameEJBQL", query);
        }
        
        Project result = (Project)uow.executeQuery("findLargeProjectByNameEJBQL",argument);
        
        em.getActiveSession().removeQuery("findLargeProjectByBudgetEJBQL");
        ((AbstractSession)em.getActiveSession()).getAliasDescriptors().remove("ProjectBaseClass");
  
        Assert.assertTrue("Complex Inheritance using named query test failed", comparer.compareObjects(result, expectedResult));                  
        
    }

    public void complexNavigatingEmbedded ()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
    	String jpqlString = "SELECT e.formerEmployment.formerCompany FROM Employee e WHERE e.formerEmployment.formerCompany = 'Former company'";
        Query query = em.createQuery(jpqlString);
        List result = query.getResultList();

        String expected = "Former company";
        Assert.assertTrue("Complex navigation of embedded in the select/where clause failed", result.contains(expected));
    }
    
    public void complexNavigatingTwoLevelOfEmbeddeds ()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager(); 
    	String jpqlString = "SELECT emp.formerEmployment.period.startDate FROM Employee emp";
        Query query = em.createQuery(jpqlString);
        List result = query.getResultList();

        Calendar cal = Calendar.getInstance();
        cal.set(1990, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date expected = new Date(cal.getTime().getTime());
        Assert.assertTrue("Complex navigation of two level of embeddeds in the select clause failed", result.contains(expected));
    }

    /**
     * Test glassfish issue 3041.
     */
    public void complexOuterJoinQuery()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        // JPQL query using one INNER JOIN and three OUTER JOINs

        String jpql = 
            "SELECT t3.firstName, t2.number, t4.firstName, t1.name " + 
            "FROM Employee t0 " +
            "INNER JOIN t0.projects t1 " +
            "LEFT OUTER JOIN t0.phoneNumbers t2 " + 
            "LEFT OUTER JOIN t1.teamLeader t3 " + 
            "LEFT OUTER JOIN t1.teamMembers t4 " + 
            "WHERE t0.firstName = 'Nancy' AND t0.lastName = 'White' " + 
            "ORDER BY t4.firstName ASC ";
        Query q = em.createQuery(jpql);
        List<Object[]> result = q.getResultList();
        
        // check expected result
        // {[null, "5551234", "Marcus", "Swirly Dirly"],
        //  [null, "5551234", "Nancy", "Swirly Dirly"]}
        Assert.assertEquals("Complex outer join query (1): unexpected result size", 2, result.size());
        Object[] result0 = result.get(0);
        Assert.assertNull  ("Complex outer join query (1): unexpected result value (0, 0):", result0[0]);
        Assert.assertEquals("Complex outer join query (1): unexpected result value (0, 1):", result0[1], "5551234");
        Assert.assertEquals("Complex outer join query (1): unexpected result value (0, 2):", result0[2], "Marcus");
        Assert.assertEquals("Complex outer join query (1): unexpected result value (0, 3):", result0[3], "Swirly Dirly");
        Object[] result1 = result.get(1);
        Assert.assertNull  ("Complex outer join query (1): unexpected result value (1, 0):", result1[0]);
        Assert.assertEquals("Complex outer join query (1): unexpected result value (1, 1):", result1[1], "5551234");
        Assert.assertEquals("Complex outer join query (1): unexpected result value (1, 2):", result1[2], "Nancy");
        Assert.assertEquals("Complex outer join query (1): unexpected result value (1, 3):", result1[3], "Swirly Dirly");


        // JPQL query using only OUTER JOINs

        jpql = 
            "SELECT t3.firstName, t2.number, t4.firstName, t1.name " + 
            "FROM Employee t0 " +
            "LEFT OUTER JOIN t0.projects t1 " +
            "LEFT OUTER JOIN t0.phoneNumbers t2 " + 
            "LEFT OUTER JOIN t1.teamLeader t3 " + 
            "LEFT OUTER JOIN t1.teamMembers t4 " +
            "WHERE t0.firstName = 'Jill' AND t0.lastName = 'May' " +
            "ORDER BY t2.number ASC ";
        q = em.createQuery(jpql);
        result = q.getResultList();

        // check expected result
        // {[null, 2255943, null, null]
        //  [null, 2258812, null, null]}
        Assert.assertEquals("Complex outer join query (2): unexpected result size", 2, result.size());
        result0 = result.get(0);
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (0, 0):", result0[0]);
        Assert.assertEquals("Complex outer join query (2): unexpected result value (0, 1):", result0[1], "2255943");
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (0, 2):", result0[2]);
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (0, 3):", result0[3]);
        result1 = result.get(1);
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (1, 0):", result1[0]);
        Assert.assertEquals("Complex outer join query (2): unexpected result value (1, 1):", result1[1], "2258812");
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (1, 2):", result1[2]);
        Assert.assertNull  ("Complex outer join query (2): unexpected result value (1, 3):", result1[3]);
                
    }

    public static void main(String[] args)
    {
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
    
    public static class EmployeeDetail {
        public String firstName;
        public String lastName;
        public Employee manager;
        public Long count;
        public EmployeeDetail(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        public EmployeeDetail(String firstName, String lastName, Employee manager) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.manager = manager;
        }
        public EmployeeDetail(Employee e) {
            this.firstName = e.getFirstName();
            this.lastName = e.getLastName();
            this.manager = e.getManager();
        }
        public EmployeeDetail(String firstName, String lastName, Long count) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.count = count;
        }
        public int hashCode() {
            int result = 0;
            result += (firstName != null) ? firstName.hashCode() : 0;
            result += (lastName != null) ? lastName.hashCode() : 0;
            result += (manager != null) ? manager.hashCode() : 0;
            result += (count != null) ? count.hashCode() : 0;
            return result;
        }
        public boolean equals(Object o) {
            if ((o == null) || (!(o instanceof EmployeeDetail))) {
                return false;
            }
            EmployeeDetail other = (EmployeeDetail) o;
            return JUnitEJBQLComplexTestSuite.equals(this.firstName, other.firstName) &&
                JUnitEJBQLComplexTestSuite.equals(this.lastName, other.lastName) &&
                JUnitEJBQLComplexTestSuite.equals(this.manager, other.manager) &&
                JUnitEJBQLComplexTestSuite.equals(this.count, other.count);
        }
        public String toString() {
            return "EmployeeDetail(" + firstName + ", " + lastName + 
                                   ", " + manager + ", " + count + ")";
        }
    }
    
    public static class LongHolder {
        public Long value1;
        public Long value2;
        public LongHolder(Long value1, Long value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
        public int hashCode() {
            int result = 0;
            result += value1 != null ? value1.hashCode() : 0;
            result += value2 != null ? value2.hashCode() : 0;
            return result;
        }
        public boolean equals(Object o) {
            if ((o == null) || (!(o instanceof LongHolder))) {
                return false;
            }
            LongHolder other = (LongHolder) o;
            return JUnitEJBQLComplexTestSuite.equals(this.value1, other.value1) && 
                JUnitEJBQLComplexTestSuite.equals(this.value2, other.value2);
        }    
        public String toString() {
            return "LongHolder(" + value1 + ", " + value2 + ")";
        }
    }
}
