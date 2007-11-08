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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Vector;
import javax.persistence.Query;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;


import oracle.toplink.essentials.queryframework.ReportQuery;

import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.relationships.Customer;
import oracle.toplink.essentials.testing.models.cmp3.relationships.RelationshipsExamples;
import oracle.toplink.essentials.testing.models.cmp3.relationships.RelationshipsTableManager;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Test complex aggregate EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for complex aggregate EJBQL functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
 
//This test suite demonstrates the bug 4616218, waiting for bug fix
public class JUnitEJBQLComplexAggregateTestSuite extends JUnitTestCase
{
    static JUnitDomainObjectComparer comparer;        //the global comparer object used in all tests
  
    public JUnitEJBQLComplexAggregateTestSuite()
    {
        super();
    }
  
    public JUnitEJBQLComplexAggregateTestSuite(String name)
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
        suite.setName("JUnitEJBQLComplexAggregateTestSuite");
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexAVGTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountDistinctWithGroupByAndHavingTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountDistinctWithGroupByTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountDistinctWithGroupByTest2"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexHavingWithAggregate"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountWithGroupByTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexDistinctCountTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexMaxTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexMinTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexSumTest"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountDistinctOnBaseQueryClass"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountOnJoinedVariableSimplePK"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountOnJoinedVariableCompositePK"));
        suite.addTest(new JUnitEJBQLComplexAggregateTestSuite("complexCountOnJoinedVariableOverManyToManySelfRefRelationship"));
        
        return new TestSetup(suite) {
     
            //This method is run at the end of the SUITE only
            protected void tearDown() {
                clearCache();
            }
            
            //This method is run at the start of the SUITE only
            protected void setUp() {
                clearCache();
                
                //get session to start setup
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                //create a new EmployeePopulator
                EmployeePopulator employeePopulator = new EmployeePopulator();
                
                new AdvancedTableCreator().replaceTables(session);

                RelationshipsExamples relationshipExamples = new RelationshipsExamples();
                new RelationshipsTableManager().replaceTables(session);
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();
                
                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());              
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);  

                //populate the relationships model and persist as well
                relationshipExamples.buildExamples(session);     
            }            
        };    
  }
  
    public void complexAVGTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        
        Expression exp = expbldr.get("lastName").equal("Smith");
        
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.useDistinct();
        rq.addAverage("salary", Double.class);
        
        String ejbqlString = "SELECT AVG(DISTINCT emp.salary) FROM Employee emp WHERE emp.lastName = \"Smith\"";      
        
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Double expectedResult = (Double)expectedResultVector.get(0);
        
        clearCache();
        
        Double result = (Double) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex AVG test failed", expectedResult, result);
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * with Count and group by
     */
    public void complexCountDistinctWithGroupByAndHavingTest()
    {
        String havingFilterString = "Toronto";
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        //Need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        rq.setHavingExpression(exp2.equal(havingFilterString));
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city HAVING a.city =?1";
        Query q = em.createQuery(ejbqlString3);
        q.setParameter(1,havingFilterString);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT test failed", comparer.compareObjects(result, expectedResult));                      
    }


    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * where DISTINCT is used with Count and group by
     */
    public void complexCountDistinctWithGroupByTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        //need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT(Distinct) with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * where DISTINCT is used with Count and group by
     */
    public void complexCountDistinctWithGroupByTest2()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        
        //need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT1",exp, Long.class);
        rq.addCount("COUNT2",exp.get("lastName").distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( e ), COUNT( DISTINCT e.lastName ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT(Distinct) with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    /**
     * Test for partial fix of GF 932. 
     */
    public void complexHavingWithAggregate()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        // Test using the project id in COUNT, GROUP BY and HAVING
        ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
        ReportQuery rq = new ReportQuery(Employee.class, employeeBuilder);
        Expression projects = employeeBuilder.anyOf("projects");
        Expression pid = projects.get("id");
        Expression count = pid.count();
        rq.addAttribute("id", pid);
        rq.addAttribute("COUNT", count, Long.class);
        rq.addGrouping(pid);
        rq.setHavingExpression(count.greaterThan(1));
        rq.setShouldReturnWithoutReportQueryResult(true);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        String jpql = 
            "SELECT p.id, COUNT(p.id) FROM Employee e JOIN e.projects p " + 
            "GROUP BY p.id HAVING COUNT(p.id)>1";
        List result = em.createQuery(jpql).getResultList();

        Assert.assertTrue("Complex HAVING with aggregate function failed", 
                          comparer.compareObjects(result, expectedResult));   

        // Test using the project itself in COUNT, GROUP BY and HAVING
        employeeBuilder = new ExpressionBuilder(Employee.class);
        rq = new ReportQuery(Employee.class, employeeBuilder);
        projects = employeeBuilder.anyOf("projects");
        count = projects.count();
        rq.addAttribute("projects", projects);
        rq.addAttribute("COUNT", count, Long.class);
        rq.addGrouping(projects);
        rq.setHavingExpression(count.greaterThan(1));
        rq.setShouldReturnWithoutReportQueryResult(true);
        expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        jpql = 
            "SELECT p, COUNT(p) FROM Employee e JOIN e.projects p " + 
            "GROUP BY p HAVING COUNT(p)>1";
        result = em.createQuery(jpql).getResultList();

        Assert.assertTrue("Complex HAVING with aggregate function failed", 
                          comparer.compareObjects(result, expectedResult));
    }
    
    public void complexCountTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        
        Expression exp = expbldr.get("lastName").equal("Smith");
        
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.addCount("COUNT", expbldr, Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT COUNT(emp) FROM Employee emp WHERE emp.lastName = \"Smith\"";    
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex COUNT test failed", expectedResult, result);
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * with Count and group by
     */
    public void complexCountWithGroupByTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
        //Need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    public void complexDistinctCountTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);

        Expression exp = expbldr.get("lastName").equal("Smith");
   
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.useDistinct();
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.addCount("COUNT", expbldr.get("lastName").distinct(), Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT COUNT(DISTINCT emp.lastName) FROM Employee emp WHERE emp.lastName = \"Smith\"";
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex DISTINCT COUNT test failed", expectedResult, result);
    }
    
    public void complexMaxTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().maximum());
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Number expectedResult = (Number) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT MAX(DISTINCT emp.salary) FROM Employee emp";
        Number result = (Number) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex MAX test failed", expectedResult, result);
    }
    
    public void complexMinTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().minimum());
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Number expectedResult = (Number) expectedResultVector.get(0);

        String ejbqlString = "SELECT MIN(DISTINCT emp.salary) FROM Employee emp";
        Number result = (Number) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex MIN test failed", expectedResult, result);  
        
    }
    
    public void complexSumTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().sum(), Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT SUM(DISTINCT emp.salary) FROM Employee emp";
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex SUMtest failed", expectedResult, result);
        
    }

    /**
     * Test case glassfish issue 2725: 
     */
    public void complexCountDistinctOnBaseQueryClass()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        Long expectedResult = Long.valueOf(em.getActiveSession().readAllObjects(Employee.class).size());
        
        String jpql = "SELECT COUNT(DISTINCT e) FROM Employee e";
        Query q = em.createQuery(jpql);
        Long result = (Long) q.getSingleResult();

        Assert.assertEquals("Complex COUNT DISTINCT on base query class ", expectedResult, result);
    }
    
    /**
     * Test case glassfish issue 2497: 
     */
    public void complexCountOnJoinedVariableSimplePK()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        // Need to create the expected result manually, because using the
        // TopLink query API would run into the same issue 2497.
        List expectedResult = Arrays.asList(new Long[] { Long.valueOf(1), Long.valueOf(0), 
                                                         Long.valueOf(0), Long.valueOf(1) });
        Collections.sort(expectedResult);

        String jpql = "SELECT COUNT(o) FROM Customer c LEFT JOIN c.orders o GROUP BY c.name";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT on joined variable simple PK", expectedResult, result);

        jpql = "SELECT COUNT(DISTINCT o) FROM Customer c LEFT JOIN c.orders o GROUP BY c.name";
        q = em.createQuery(jpql);
        result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT DISTINCT on joined variable simple PK", expectedResult, result);
    }

    /**
     * Test case glassfish issue 2497: 
     */
    public void complexCountOnJoinedVariableCompositePK()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        // Need to create the expected result manually, because using the
        // TopLink query API would run into the same issue 2497.
        List expectedResult = Arrays.asList(new Long[] { Long.valueOf(2), Long.valueOf(5), Long.valueOf(3) });
        Collections.sort(expectedResult);

        String jpql = "SELECT COUNT(p) FROM Employee e LEFT JOIN e.phoneNumbers p WHERE e.lastName LIKE 'S%' GROUP BY e.lastName";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT on outer joined variable composite PK", expectedResult, result);

        // COUNT DISTINCT with inner join
        jpql = "SELECT COUNT(DISTINCT p) FROM Employee e JOIN e.phoneNumbers p WHERE e.lastName LIKE 'S%' GROUP BY e.lastName";
        q = em.createQuery(jpql);
        result = (List) q.getResultList();
        Collections.sort(result);
  
        Assert.assertEquals("Complex DISTINCT COUNT on inner joined variable composite PK", expectedResult, result);
}

    /**
     * Test case glassfish issue 2440: 
     * On derby a JPQL query including a LEFT JOIN on a ManyToMany
     * relationship field of the same class (self-referencing relationship)
     * runs into a NPE in SQLSelectStatement.appendFromClauseForOuterJoin.
     */
    public void complexCountOnJoinedVariableOverManyToManySelfRefRelationship()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = 
            (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        Long zero = Long.valueOf(0);
        List expectedResult = Arrays.asList(new Long[] { zero, zero, zero, zero });

        String jpql = "SELECT COUNT(cc) FROM Customer c LEFT JOIN c.cCustomers cc GROUP BY c.name";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();

        Assert.assertEquals("Complex COUNT on joined variable over ManyToMany self refrenceing relationship failed", 
                            expectedResult, result);
    }
    
    public static void main(String[] args)
    {
         junit.swingui.TestRunner.main(args);
    }
}
