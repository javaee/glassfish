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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.EntityManager;

import javax.persistence.Query;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.expressions.ExpressionMath;
import oracle.toplink.essentials.sessions.Session;

import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;

import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.PhoneNumber;
import oracle.toplink.essentials.testing.models.cmp3.advanced.SmallProject;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.threetier.Server;

/**
 * <p>
 * <b>Purpose</b>: Test simple EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for simple EJBQL functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
 
public class JUnitEJBQLSimpleTestSuite extends JUnitTestCase {  
  
  static JUnitDomainObjectComparer comparer;        //the global comparer object used in all tests
  
  public JUnitEJBQLSimpleTestSuite()
  {
    super();
  }
  
  public JUnitEJBQLSimpleTestSuite(String name)
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
    suite.setName("JUnitEJBQLSimpleTestSuite");
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleJoinFetchTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleJoinFetchTest2"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("baseTestCase"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleABSTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleBetweenAndTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleBetweenTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleConcatTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleConcatTestWithParameters"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleConcatTestWithConstants1"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleDistinctTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleDistinctNullTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleDistinctMultipleResultTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleDoubleOrTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEqualsBracketsTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEqualsTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEqualsTestWithJoin"));    
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEqualsWithAs"));    
    suite.addTest(new JUnitEJBQLSimpleTestSuite("collectionMemberIdentifierEqualsTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("abstractSchemaIdentifierEqualsTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("abstractSchemaIdentifierNotEqualsTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleInOneDotTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleInTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleLengthTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleLikeTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleLikeTestWithParameter"));  
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleLikeEscapeTestWithParameter"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNotBetweenTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNotEqualsVariablesInteger"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNotInTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNotLikeTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleOrFollowedByAndTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleOrFollowedByAndTestWithStaticNames"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleOrTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleParameterTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleParameterTestChangingParameters"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseAbsTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseConcatTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseEqualsTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseLengthTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseParameterTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseSqrtTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleReverseSubstringTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleSqrtTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleSubstringTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNullTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleNotNullTest"));              
    suite.addTest(new JUnitEJBQLSimpleTestSuite("distinctTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("conformResultsInUnitOfWorkTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleModTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleIsEmptyTest"));   
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleIsNotEmptyTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleApostrohpeTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEscapeUnderscoreTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("simpleEnumTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("smallProjectMemberOfProjectsTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("smallProjectNOTMemberOfProjectsTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectCountOneToOneTest"));    //bug 4616218
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectOneToOneTest"));         //employee.address doesnt not work
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectPhonenumberDeclaredInINClauseTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectSimpleMemberOfWithParameterTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectSimpleNotMemberOfWithParameterTest")); 
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectSimpleBetweenWithParameterTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectSimpleInWithParameterTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectAverageQueryForByteColumnTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectUsingLockModeQueryHintTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("multipleExecutionOfNamedQueryTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectNamedNativeQueryWithPositionalParameterTest"));
    suite.addTest(new JUnitEJBQLSimpleTestSuite("selectNativeQueryWithPositionalParameterTest"));
    
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

//GF Bug#404
//1.  Fetch join now works with LAZY.  The fix is to trigger the value holder during object registration.  The test is to serialize
//the results and deserialize it, then call getPhoneNumbers().size().  It used to throw an exception because the value holder 
//wasn't triggered and the data was in a transient attribute that was lost during serialization
//2.  Test both scenarios of using the cache and bypassing the cache
    public void simpleJoinFetchTest(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        simpleJoinFetchTest(em);
    }
        
    //bug#6130550:  
    // tests that Fetch join works when returning objects that may already have been loaded in the em/uow (without the joined relationships)
    // Builds on simpleJoinFetchTest
    public void simpleJoinFetchTest2() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        //preload employees into the cache so that phonenumbers are not prefetched
        String ejbqlString = "SELECT e FROM Employee e";
        List result = em.createQuery(ejbqlString).getResultList();
        // run the simpleJoinFetchTest and verify all employees have phonenumbers fetched.
        simpleJoinFetchTest(em);
    }

    public void simpleJoinFetchTest(oracle.toplink.essentials.ejb.cmp3.EntityManager em) {
        Exception exception = null;
        String ejbqlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.phoneNumbers";

        //use the cache
        List result = em.createQuery(ejbqlString).getResultList();
                
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteStream);
                
            stream.writeObject(result);
            stream.flush();
            byte arr[] = byteStream.toByteArray();
            ByteArrayInputStream inByteStream = new ByteArrayInputStream(arr);
            ObjectInputStream inObjStream = new ObjectInputStream(inByteStream);

            List deserialResult = (List) inObjStream.readObject();
            for (Iterator iterator = deserialResult.iterator(); iterator.hasNext();) {
                Employee emp = (Employee)iterator.next();
                emp.getPhoneNumbers().size();
            }        
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNull("Exception was caught when using cache "+ exception, exception);                 
                
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setShouldReturnWithoutReportQueryResult(true);
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder builder = reportQuery.getExpressionBuilder();
        List joins = new ArrayList(1);
        joins.add(builder.anyOfAllowingNone("phoneNumbers"));
        reportQuery.addItem("emp", builder, joins);
        Vector expectedResult = (Vector)em.getUnitOfWork().executeQuery(reportQuery);

        if (!comparer.compareObjects(result, expectedResult)) {
            this.fail("simpleJoinFetchTest Failed when using cache, collections do not match: " + result + " expected: " + expectedResult);
        }

        //Bypass the cache
        clearCache();
        em.clear();

        result = em.createQuery(ejbqlString).getResultList();

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteStream);

            stream.writeObject(result);
            stream.flush();
            byte arr[] = byteStream.toByteArray();
            ByteArrayInputStream inByteStream = new ByteArrayInputStream(arr);
            ObjectInputStream inObjStream = new ObjectInputStream(inByteStream);

            List deserialResult = (List) inObjStream.readObject();
            for (Iterator iterator = deserialResult.iterator(); iterator.hasNext();) {
                Employee emp = (Employee)iterator.next();
                emp.getPhoneNumbers().size();
            }        
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNull("Exception was caught when bypassing cache", exception);                 

        clearCache();

        expectedResult = (Vector)em.getUnitOfWork().executeQuery(reportQuery);
         
        if (!comparer.compareObjects(result, expectedResult)) {
            this.fail("simpleJoinFetchTest Failed when not using cache, collections do not match: " + result + " expected: " + expectedResult);
        }
    }
    
  //Test case for selecting ALL employees from the database 
  public void baseTestCase()
  {          
      oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      List expectedResult = em.getActiveSession().readAllObjects(Employee.class);
      
      clearCache();
      
      List result = em.createQuery("SELECT OBJECT(emp) FROM Employee emp").getResultList();                            
     
      Assert.assertTrue("Base Test Case Failed", comparer.compareObjects(result, expectedResult));        
  }
  
  //Test case for ABS function in EJBQL
  public void simpleABSTest()
  { 
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();             
        
        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());
        
        clearCache();
        
	    String ejbqlString;

	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "ABS(emp.salary) = ";
	    ejbqlString = ejbqlString + expectedResult.getSalary();        
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("ABS test failed", comparer.compareObjects(result, expectedResult));
        
  }
  
  //Test case for AND function in EJBQL
  public void simpleBetweenAndTest()
  {
      	BigDecimal empId = new BigDecimal(0);
        
	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee employee = (Employee) (em.getActiveSession().readAllObjects(Employee.class).lastElement());
	    
	    ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.get("id").between(empId, employee.getId());
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause); 
	    
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
	    clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id BETWEEN " + empId + "AND " + employee.getId();
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Between And test failed", comparer.compareObjects( result, expectedResult));   
        
  }
  
  //Test case for Between function in EJBQL
  public void simpleBetweenTest()
  {
        BigDecimal empId = new BigDecimal(0);
        
	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee employee = (Employee) (em.getActiveSession().readAllObjects(Employee.class).lastElement());
        
	    ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.get("id").between(empId, employee.getId());
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause); 
	    
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id BETWEEN " + empId.toString() + "AND " + employee.getId().toString();
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Between test failed", comparer.compareObjects(result, expectedResult));   
  }
  
  //Test case for concat function in EJBQL
  public void simpleConcatTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());
        
        clearCache();
        
	    String partOne, partTwo;
	    String ejbqlString;

	    partOne = expectedResult.getFirstName().substring(0, 2);
	    partTwo = expectedResult.getFirstName().substring(2);

	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "emp.firstName = ";
	    ejbqlString = ejbqlString + "CONCAT(\"";
	    ejbqlString = ejbqlString + partOne;
	    ejbqlString = ejbqlString + "\", \"";
	    ejbqlString = ejbqlString + partTwo;
	    ejbqlString = ejbqlString + "\")";
        
        List result =  em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Concat test failed", comparer.compareObjects(result, expectedResult));
  }

  //Test case for concat function in EJBQL taking parameters
  public void simpleConcatTestWithParameters()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());

        clearCache();

	    String partOne, partTwo;
	    String ejbqlString;

	    partOne = expectedResult.getFirstName().substring(0, 2);
	    partTwo = expectedResult.getFirstName().substring(2);

	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "emp.firstName = ";
	    ejbqlString = ejbqlString + "CONCAT(";
	    ejbqlString = ejbqlString + ":partOne";
	    ejbqlString = ejbqlString + ", ";
	    ejbqlString = ejbqlString + ":partTwo";
	    ejbqlString = ejbqlString + ")";

        List result =  em.createQuery(ejbqlString).setParameter("partOne", partOne).setParameter("partTwo", partTwo).getResultList();

        Assert.assertTrue("Concat test failed", comparer.compareObjects(result, expectedResult));
  }


  //Test case for concat function with constants in EJBQL
  public void simpleConcatTestWithConstants1()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee emp = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());

	    String partOne;
	    String ejbqlString;

	    partOne = emp.getFirstName();

        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").concat("Smith").like(partOne + "Smith");
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);       
        
        clearCache();
        
	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "CONCAT(emp.firstName,\"Smith\") LIKE ";
	    ejbqlString = ejbqlString + "\"" + partOne + "Smith\"";    
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Concat test with constraints failed", comparer.compareObjects(result, expectedResult));           
  }
  
    //Test case for double OR function in EJBQL
     //Test case for double OR function in EJBQL
     public void simpleDistinctTest(){
         oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
         String ejbqlString = "SELECT DISTINCT e FROM Employee e JOIN FETCH e.phoneNumbers ";
         List result = em.createQuery(ejbqlString).getResultList();
         Set testSet = new HashSet();
         for (Iterator iterator = result.iterator(); iterator.hasNext();){
             Employee emp = (Employee)iterator.next();
             assertFalse("Result was not distinct", testSet.contains(emp));
             testSet.add(emp);
         }
     }
     
    public void simpleDistinctNullTest(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        Employee emp = (Employee) em.createQuery("SELECT e from Employee e").getResultList().get(0);
        String oldFirstName = emp.getFirstName();
        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());
            emp.setFirstName(null);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        try{
            String ejbqlString = "SELECT DISTINCT e.firstName FROM Employee e WHERE e.lastName = '"+ emp.getLastName() +"'";
            List result = em.createQuery(ejbqlString).getResultList();
            assertTrue("Failed to return null value", result.contains(null));
        }finally{
            try{
                em.getTransaction().begin();
                emp = em.find(Employee.class, emp.getId());
                emp.setFirstName(oldFirstName);
                em.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw ex;
            }
            
        }
    }
    
    public void simpleDistinctMultipleResultTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        String ejbqlString = "SELECT DISTINCT e, e.firstName FROM Employee e JOIN FETCH e.phoneNumbers ";
        List result = em.createQuery(ejbqlString).getResultList();
        Set testSet = new HashSet();
        for (Iterator iterator = result.iterator(); iterator.hasNext();){
            String ids = "";
            Object[] row = (Object[])iterator.next();
            Employee emp = (Employee)row[0];
            String string = (String)row[1];
            ids = "_" + emp.getId() + "_" + string;
            assertFalse("Result was not distinct", testSet.contains(ids));
            testSet.add(ids);
        }
    }
    
  //Test case for double OR function in EJBQL
  public void simpleDoubleOrTest()
  {
        Employee emp1, emp2, emp3;
        
	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        emp1 = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());
	    emp2 = (Employee) (em.getActiveSession().readAllObjects(Employee.class).elementAt(1));        
	    emp3 = (Employee) (em.getActiveSession().readAllObjects(Employee.class).elementAt(2));        
	    
        clearCache();
        
	    Vector expectedResult = new Vector();
	    expectedResult.add(emp1);
        expectedResult.add(emp2);
        expectedResult.add(emp3);
   
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id = " + emp1.getId() + "OR emp.id = " + emp2.getId() + "OR emp.id = " + emp3.getId();
	    
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Double OR test failed", comparer.compareObjects(result, expectedResult));           
  }
  
  //Test case for equals brackets in EJBQL
  public void simpleEqualsBracketsTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());       
        
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "( emp.firstName = ";
        ejbqlString = ejbqlString + "\"" + expectedResult.getFirstName() + "\")";
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Equals brackets test failed", comparer.compareObjects(result,expectedResult));           
  }
  
  //Test case for equals in EJBQL
  public void simpleEqualsTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());       
        
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "emp.firstName = ";
        ejbqlString = ejbqlString + "\"" + expectedResult.getFirstName() + "\"";
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Equals test failed", comparer.compareObjects(expectedResult,result));
  }
  
  //Test case for equals with join in EJBQL
  public void simpleEqualsTestWithJoin()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
        
        ExpressionBuilder builder = new ExpressionBuilder();
    	Expression whereClause = builder.anyOf("managedEmployees").get("address").get("city").equal("Ottawa");
		
        Vector expectedResult = em.getActiveSession().readAllObjects(Employee.class,whereClause);            	
        
        clearCache();
        
    	String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp, IN(emp.managedEmployees) managedEmployees " +
			"WHERE managedEmployees.address.city = 'Ottawa'";
            
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Equals test with Join failed", comparer.compareObjects(result, expectedResult));
  }
  
  public void simpleEqualsWithAs()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        Employee expectedResult = (Employee) (em.getActiveSession().readAllObjects(Employee.class).firstElement());        
	    
        clearCache();
        
        Vector employeesUsed = new Vector();
	    employeesUsed.add(expectedResult);
	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee AS emp WHERE emp.id = " + expectedResult.getId();
        
        List result = em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Equals test with As failed", comparer.compareObjects(expectedResult,result));
  }
  
  public void collectionMemberIdentifierEqualsTest()
  {     
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
                
        ExpressionBuilder employees = new ExpressionBuilder();
        Expression exp = employees.get("firstName").equal("Bob");
        exp = exp.and(employees.get("lastName").equal("Smith"));
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class, exp).firstElement();
	    
        clearCache();
        
        PhoneNumber phoneNumber = (PhoneNumber) ((Vector) expectedResult.getPhoneNumbers()).firstElement();
	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp, IN (emp.phoneNumbers) phone " + 
	                         "WHERE phone = ?1";     

        List result = em.createQuery(ejbqlString).setParameter(1,phoneNumber).getResultList();        
        
        Assert.assertTrue("CollectionMemberIdentifierEqualsTest failed", comparer.compareObjects(expectedResult,result));    
  }
  
  public void abstractSchemaIdentifierEqualsTest()
  {
      oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
                
	  Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
      
      clearCache();
      
      String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp = ?1";
      
      List result = em.createQuery(ejbqlString).setParameter(1,expectedResult).getResultList();
        
      Assert.assertTrue("abstractSchemaIdentifierEqualsTest failed", comparer.compareObjects(expectedResult,result));    
  }
  
  public void abstractSchemaIdentifierNotEqualsTest()
  {
      oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
      
      Vector expectedResult = (Vector) em.getActiveSession().readAllObjects(Employee.class);
      
      clearCache();      
      
      Employee emp = (Employee) expectedResult.firstElement();
      
      expectedResult.removeElementAt(0);      
      
      String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp <> ?1";   
      
      List result = em.createQuery(ejbqlString).setParameter(1,emp).getResultList();
      
      Assert.assertTrue("abstractSchemaIdentifierNotEqualsTest failed", comparer.compareObjects(result, expectedResult));   
  }
  
  public void simpleInOneDotTest()
  {     
        //select a specifif employee using Expr Bob Smithn
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ReadObjectQuery roq = new ReadObjectQuery(Employee.class);
        
        ExpressionBuilder empBldr = new ExpressionBuilder();
        
        Expression exp1 = empBldr.get("firstName").equal("Bob");
        Expression exp2 = empBldr.get("lastName").equal("Smith");
        
        roq.setSelectionCriteria(exp1.and(exp2));
        
        Employee expectedResult = (Employee) em.getActiveSession().executeQuery(roq);
        
        clearCache();
        
        PhoneNumber empPhoneNumbers = (PhoneNumber) ((Vector) expectedResult.getPhoneNumbers()).elementAt(0);
	    
	    String ejbqlString = "SelecT OBJECT(emp) from Employee emp, in (emp.phoneNumbers) phone " + 
	                         "Where phone.areaCode = \"" + empPhoneNumbers.getAreaCode() + "\"" + "AND emp.firstName = \"" + expectedResult.getFirstName() + "\""; 
        ejbqlString = ejbqlString + "AND emp.lastName = \"" + expectedResult.getLastName() + "\"";
    	
	    Employee result =  (Employee) em.createQuery(ejbqlString).getSingleResult();        
        
        Assert.assertTrue("Simple In Dot Test failed", comparer.compareObjects(result, expectedResult));           
  }
  
  public void selectAverageQueryForByteColumnTest() {     
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();         
        
	    String ejbqlString = "Select AVG(emp.salary)from Employee emp"; 
	    Object result = em.createQuery(ejbqlString).getSingleResult();        
        
        Assert.assertTrue("AVG result type [" + result.getClass() + "] not of type Double", result.getClass() == Double.class);
  }
  
  public void simpleInTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id IN (" + expectedResult.getId().toString() + ")";
        
        List result =  em.createQuery(ejbqlString).getResultList();        

        Assert.assertTrue("Simple In Test failed", comparer.compareObjects(result, expectedResult));       
  }
  
  public void simpleLengthTest()
  {     
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Assert.assertFalse("Warning SQL doesnot support LENGTH function",  ((Session) JUnitTestCase.getServerSession()).getPlatform().isSQLServer());
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String ejbqlString;
	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "LENGTH ( emp.firstName     ) = ";
	    ejbqlString = ejbqlString + expectedResult.getFirstName().length();
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Length Test failed", comparer.compareObjects(result, expectedResult));       
  }
  
  
  public void simpleLikeTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String partialFirstName = expectedResult.getFirstName().substring(0, 3) + "%";
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName LIKE \"" + partialFirstName + "\"";
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Simple Like Test failed", comparer.compareObjects(result, expectedResult)); 
  }
  
  public void simpleLikeTestWithParameter()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        String partialFirstName = "%" + emp.getFirstName().substring(0, 3) + "%";
        
        ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    
	    Vector parameters = new Vector();
	    parameters.add(partialFirstName);
	    
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("firstName").like(partialFirstName);
	    raq.setSelectionCriteria(whereClause);
	    Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName LIKE ?1";
        
        List result = em.createQuery(ejbqlString).setParameter(1,partialFirstName).getResultList();
        
        Assert.assertTrue("Simple Like Test with Parameter failed", comparer.compareObjects(result, expectedResult)); 
  }
  
  public void simpleLikeEscapeTestWithParameter()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
		Address expectedResult = new Address();
		expectedResult.setCity("TAIYUAN");
		expectedResult.setCountry("CHINA");
		expectedResult.setProvince("SHANXI");
		expectedResult.setPostalCode("030024");
		expectedResult.setStreet("234 RUBY _Way");
		
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        uow.registerObject(expectedResult);
        uow.commit();        

		//test the apostrophe
        String ejbqlString = "SELECT OBJECT(address) FROM Address address WHERE address.street LIKE :pattern ESCAPE :esc";
        String patternString = null;
        Character escChar = null;
        // \ is always treated as escape in MySQL.  Therefore ESCAPE '\' is considered a syntax error
        if (((EntityManagerImpl)em.getDelegate()).getServerSession().getPlatform().isMySQL()) {
            patternString = "234 RUBY $_Way";
            escChar = new Character('$');              
        } else {
            patternString = "234 RUBY \\_Way";
            escChar = new Character('\\');
        }
        
        List result = em.createQuery(ejbqlString).setParameter("pattern", patternString).
            setParameter("esc", escChar).getResultList();
    
        Assert.assertTrue("Simple Escape Underscore test failed", comparer.compareObjects(result, expectedResult)); 
  }
  
  public void simpleNotBetweenTest()
  { 
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).firstElement();
        Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).lastElement();
            
        ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("id").between(emp1.getId(), emp2.getId()).not();
	    
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq);
	    
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "emp.id NOT BETWEEN ";
	    ejbqlString = ejbqlString + emp1.getId().toString();
	    ejbqlString = ejbqlString + " AND ";
    	ejbqlString = ejbqlString + emp2.getId().toString();
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Simple Not Between Test failed", comparer.compareObjects(result, expectedResult)); 
      
  }
  
  public void simpleNotEqualsVariablesInteger()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector expectedResult = em.getActiveSession().readAllObjects(Employee.class);
        
        clearCache();
        
        Employee emp = (Employee) expectedResult.elementAt(0);
        
        expectedResult.removeElementAt(0);
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id <> " + emp.getId();
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Like Test with Parameter failed", comparer.compareObjects(result, expectedResult));      
        
  }
  
  public void simpleNotInTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        ExpressionBuilder builder = new ExpressionBuilder();
	    
        Vector idVector = new Vector();
	    idVector.add(emp.getId());        
	    
	    Expression whereClause = builder.get("id").notIn(idVector);
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id NOT IN (" + emp.getId().toString() + ")";    
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Not In Test failed", comparer.compareObjects(result, expectedResult));      
  }
  
  public void simpleNotLikeTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        String partialFirstName = emp.getFirstName().substring(0, 3) + "%";
	    
	    ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.get("firstName").notLike(partialFirstName);
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();       
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName NOT LIKE \"" + partialFirstName + "\"";    
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Not Like Test failed", comparer.compareObjects(result, expectedResult));      
  }
  
  public void simpleOrFollowedByAndTest()
  {
   	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        Employee emp3 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(2);
                
        Vector expectedResult = new Vector();
	    expectedResult.add(emp1);       
	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id = " + emp1.getId() + " OR emp.id = " + emp2.getId() + " AND emp.id = " + emp3.getId();    
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Simple Or followed by And Test failed", comparer.compareObjects(result, expectedResult));      
    
  }
  
  public void simpleOrFollowedByAndTestWithStaticNames()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();             
        
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").equal("John").or(
        builder.get("firstName").equal("Bob").and(builder.get("lastName").equal("Smith")));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);      
        
	    clearCache();
	    
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName = \"John\" OR emp.firstName = \"Bob\" AND emp.lastName = \"Smith\"";    
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Or followed by And With Static Names Test failed", comparer.compareObjects(result, expectedResult));            
  }
  
  public void simpleOrTest()
  {
   	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
	    
	    Vector expectedResult = new Vector();
	    expectedResult.add(emp1);
        expectedResult.add(emp2);
	    	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.id = " + emp1.getId() + "OR emp.id = " + emp2.getId();
        
        List result = em.createQuery(ejbqlString).getResultList();
        clearCache();
  
        Assert.assertTrue("Simple Or Test failed", comparer.compareObjects(result, expectedResult));        
  }
  
  public void simpleParameterTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        String parameterName = "firstName";
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").equal(builder.getParameter(parameterName));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.addArgument(parameterName);
        
	    Vector parameters = new Vector();
	    parameters.add(expectedResult.getFirstName());
	    
	    Vector employees = (Vector)getServerSession().executeQuery(raq, parameters);    
	    
        clearCache();
                
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE " + "emp.firstName = ?1 ";
        
        List result = em.createQuery(ejbqlString).setParameter(1,parameters.get(0)).getResultList();      
        

        
        Assert.assertTrue("Simple Parameter Test failed", comparer.compareObjects(result, expectedResult));        
  }
  
  public void simpleParameterTestChangingParameters()
  {
        
   	    oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp1 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        Employee emp2 = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(1);
        
        String parameterName = "firstName";
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").equal(builder.getParameter(parameterName));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.addArgument(parameterName);
        
	    Vector firstParameters = new Vector();
	    firstParameters.add(emp1.getFirstName());
	    Vector secondParameters = new Vector();
	    secondParameters.add(emp2.getFirstName());
	    
	    Vector firstEmployees = (Vector)getServerSession().executeQuery(raq, firstParameters);
	    clearCache();
        Vector secondEmployees = (Vector)getServerSession().executeQuery(raq, secondParameters);
        clearCache();
        Vector expectedResult = new Vector();
        expectedResult.addAll(firstEmployees);
        expectedResult.addAll(secondEmployees);    
	    
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE " + "emp.firstName = ?1 ";
        
        List firstResultSet = em.createQuery(ejbqlString).setParameter(1,firstParameters.get(0)).getResultList();
        clearCache();
        List secondResultSet = em.createQuery(ejbqlString).setParameter(1,secondParameters.get(0)).getResultList();
        clearCache();
        Vector result = new Vector();
        result.addAll(firstResultSet);
        result.addAll(secondResultSet);
        
        Assert.assertTrue("Simple Parameter Test Changing Parameters failed", comparer.compareObjects(result, expectedResult));        
        
  }
  
  public void simpleReverseAbsTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE "+ expectedResult.getSalary() + " = ABS(emp.salary)";
                
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Reverse Abs test failed", comparer.compareObjects(result, expectedResult));        
        
  }
  
  public void simpleReverseConcatTest()
  {
      	oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String partOne = expectedResult.getFirstName().substring(0, 2);
	    String partTwo = expectedResult.getFirstName().substring(2);

	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "CONCAT(\"";
	    ejbqlString = ejbqlString + partOne;
	    ejbqlString = ejbqlString + "\", \"";
	    ejbqlString = ejbqlString + partTwo;
	    ejbqlString = ejbqlString + "\")";
	    ejbqlString = ejbqlString + " = emp.firstName";
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Reverse Concat test failed", comparer.compareObjects(result, expectedResult));                
  }
  
  public void simpleReverseEqualsTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "\"" + expectedResult.getFirstName() + "\"";
        ejbqlString = ejbqlString + " = emp.firstName";   
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Reverse Equals test failed", comparer.compareObjects(result, expectedResult));                
  }
  
  public void simpleReverseLengthTest()
  {
      	oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + expectedResult.getFirstName().length();
	    ejbqlString = ejbqlString + " = LENGTH(emp.firstName)";
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Reverse Length test failed", comparer.compareObjects(result, expectedResult));                       
        
  }
  
  public void simpleReverseParameterTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee emp = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String parameterName = "firstName";
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").equal(builder.getParameter(parameterName));
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.addArgument(parameterName);
        
	    Vector parameters = new Vector();
	    parameters.add(emp.getFirstName());
	    
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq, parameters);
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "?1 = emp.firstName ";
        
        List result = em.createQuery(ejbqlString).setParameter(1,parameters.get(0)).getResultList();

        Assert.assertTrue("Simple Reverse Parameter test failed", comparer.compareObjects(result, expectedResult));   
  }
  
  public void simpleReverseSqrtTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();              
        
        ExpressionBuilder expbldr = new ExpressionBuilder();
        Expression whereClause = expbldr.get("firstName").equal("SquareRoot").and(expbldr.get("lastName").equal("TestCase1"));
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);

        double salarySquareRoot = Math.sqrt((new Double(((Employee) expectedResult.firstElement()).getSalary()).doubleValue()));
        
        clearCache();
             
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + salarySquareRoot;
	    ejbqlString = ejbqlString + " = SQRT(emp.salary)";
        
        List result = em.createQuery(ejbqlString).getResultList();
     
        
        Assert.assertTrue("Simple Reverse Square Root test failed", comparer.compareObjects(result, expectedResult));   
        
  }
  
  public void simpleReverseSubstringTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
        String firstNamePart;
	    String ejbqlString;

	    firstNamePart = expectedResult.getFirstName().substring(0, 2);
	    ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "\"" + firstNamePart + "\"";
        ejbqlString = ejbqlString + " = SUBSTRING(emp.firstName, 1, 2)";
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Simple Reverse SubString test failed", comparer.compareObjects(result, expectedResult));   

  }
  
  
  public void simpleSqrtTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();              
        
        ExpressionBuilder expbldr = new ExpressionBuilder();
        Expression whereClause = expbldr.get("firstName").equal("SquareRoot").and(expbldr.get("lastName").equal("TestCase1"));
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        double salarySquareRoot = Math.sqrt((new Double(((Employee) expectedResult.firstElement()).getSalary()).doubleValue()));
        
        clearCache();
             
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "SQRT(emp.salary) = ";
	    ejbqlString = ejbqlString + salarySquareRoot;
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Square Root test failed", comparer.compareObjects(result, expectedResult));   

  }
  
  public void simpleSubstringTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readAllObjects(Employee.class).elementAt(0);
        
        clearCache();
        
	    String firstNamePart = expectedResult.getFirstName().substring(0, 2);
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
	    ejbqlString = ejbqlString + "SUBSTRING(emp.firstName, 1, 2) = \"";//changed from 0, 2 to 1, 2(ZYP)
	    ejbqlString = ejbqlString + firstNamePart + "\"";
        
        List result = em.createQuery(ejbqlString).getResultList();
        Assert.assertTrue("Simple SubString test failed", comparer.compareObjects(result, expectedResult));   
  }
  
  public void simpleNullTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee nullEmployee = new Employee();
        nullEmployee.setFirstName(null);
        nullEmployee.setLastName("Test");
        
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        uow.registerObject(nullEmployee);
        uow.commit();
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
 
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").isNull();
        raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName IS NULL";
        
        List result = em.createQuery(ejbqlString).getResultList();      
        
        uow = clientSession.acquireUnitOfWork();
        uow.deleteObject(nullEmployee);
        uow.commit();

        Assert.assertTrue("Simple Null test failed", comparer.compareObjects(result, expectedResult));   
      
  }
  
  public void simpleNotNullTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        Employee nullEmployee = new Employee();
        nullEmployee.setFirstName(null);
        nullEmployee.setLastName("Test");
        
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        uow.registerObject(nullEmployee);
        uow.commit();
        
        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
 
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").isNull().not();
        raq.setSelectionCriteria(whereClause);
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();        
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.firstName IS NOT NULL";
        List result = em.createQuery(ejbqlString).getResultList();
        
        uow = clientSession.acquireUnitOfWork();
        uow.deleteObject(nullEmployee);
        uow.commit();
  
        Assert.assertTrue("Simple Not Null test failed", comparer.compareObjects(result, expectedResult));   
  }
  
  public void distinctTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        ReadAllQuery raq = new ReadAllQuery();
        
        ExpressionBuilder employee = new ExpressionBuilder();
        Expression whereClause = employee.get("lastName").equal("Smith");
       
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.useDistinct();
        
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();           
        
        String ejbqlString = "SELECT DISTINCT OBJECT(emp) FROM Employee emp WHERE emp.lastName = \'Smith\'";
        List result = em.createQuery(ejbqlString).getResultList();
  
        Assert.assertTrue("Distinct test failed", comparer.compareObjects(result, expectedResult));   
  }
  
  public void multipleExecutionOfNamedQueryTest(){
      //bug 5279859
       EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();    
       Query query = em.createNamedQuery("findEmployeeByPostalCode");
       query.setParameter("postalCode", "K1T3B9");
       try{
           query.getResultList();
       }catch (RuntimeException ex){
           fail("Failed to execute query, exception resulted on first execution, not expected");
       }
      try{
          query.getResultList();
      }catch (RuntimeException ex){
          fail("Failed to execute query, exception resulted on second execution");
      }
      query = em.createNamedQuery("findEmployeeByPostalCode");
      query.setParameter("postalCode", "K1T3B9");
      try{
          query.getResultList();
      }catch (RuntimeException ex){
          fail("Failed to execute query, exception resulted on first execution, of second use of named query");
      }
      query.setMaxResults(100000);
      try{
          query.getResultList();
      }catch (RuntimeException ex){
          fail("Failed to execute query, exception resulted after setting max results (forcing reprepare)");
      }
      
  }
  
  public void conformResultsInUnitOfWorkTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        ReadObjectQuery readObjectQuery = new ReadObjectQuery();
        
        readObjectQuery.setReferenceClass(Employee.class);
        readObjectQuery.setEJBQLString("SELECT OBJECT(emp) FROM Employee emp WHERE emp.id = ?1");
        readObjectQuery.conformResultsInUnitOfWork();
        readObjectQuery.addArgument("1", Integer.class); 

    
        //ServerSession next
        Server serverSession =((EntityManagerImpl)em.getDelegate()).getServerSession().getProject().createServerSession();
		serverSession.setSessionLog(getServerSession().getSessionLog());
        serverSession.login();
        UnitOfWork unitOfWork = serverSession.acquireUnitOfWork();
        Employee newEmployee = new Employee();
        newEmployee.setId(new Integer(9000));
        unitOfWork.registerObject(newEmployee);
  
        Vector testV = new Vector();
        testV.addElement(new Integer(9000));

        Employee result = (Employee)unitOfWork.executeQuery(readObjectQuery, testV);
        
        Assert.assertTrue("Conform Results In Unit of Work using ServerSession failed", comparer.compareObjects(result, newEmployee));       
        
        serverSession.logout();
  }
  
  public void simpleModTest()
  {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
    
        Assert.assertFalse("Warning SQL/Sybase doesnot support MOD function",  ((Session) JUnitTestCase.getServerSession()).getPlatform().isSQLServer() || ((Session) JUnitTestCase.getServerSession()).getPlatform().isSybase());
        
        ReadAllQuery raq = new ReadAllQuery();
        ExpressionBuilder employee = new ExpressionBuilder();
        Expression whereClause = ExpressionMath.mod(employee.get("salary"), 2).greaterThan(0);
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE MOD(emp.salary, 2) > 0";        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Mod test failed", comparer.compareObjects(result, expectedResult));

        // Test MOD(fieldAccess, fieldAccess) glassfish issue 2771

        expectedResult = em.getActiveSession().readAllObjects(Employee.class);
        clearCache();
        
        ejbqlString = "SELECT emp FROM Employee emp WHERE MOD(emp.salary, emp.salary) = 0";        
        result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Mod test(2) failed", comparer.compareObjects(result, expectedResult));  
  }
  
    public void simpleIsEmptyTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.isEmpty("phoneNumbers");

	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.phoneNumbers IS EMPTY";
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple Is empty test failed", comparer.compareObjects(result, expectedResult));   
    }

    public void simpleIsNotEmptyTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ExpressionBuilder builder = new ExpressionBuilder();
	    Expression whereClause = builder.notEmpty("phoneNumbers");
	    
	    ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)em.getActiveSession().executeQuery(raq);
        
        clearCache();
        
	    String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE emp.phoneNumbers IS NOT EMPTY";
        
        List result = em.createQuery(ejbqlString).getResultList();
        
        Assert.assertTrue("Simple is not empty test failed", comparer.compareObjects(result, expectedResult));   
        
    }
    
    public void simpleApostrohpeTest()
    {   
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector addresses = em.getActiveSession().readAllObjects(Address.class);
        
        clearCache();
        
        Address expectedResult = new Address();
        
        Iterator addressesIterator = addresses.iterator();
		while(addressesIterator.hasNext()){
			expectedResult = (Address)addressesIterator.next();
			if(expectedResult.getStreet().indexOf("Lost") != -1){
				break;
			}
		}

        String ejbqlString = "SELECT OBJECT(address) FROM Address address WHERE ";
        ejbqlString = ejbqlString + "address.street = '234 I''m Lost Lane'";
        
        List result = em.createQuery(ejbqlString).getResultList();
                
        Assert.assertTrue("Simple apostrophe test failed", comparer.compareObjects(result, expectedResult));   
        
    }
    
    public void simpleEscapeUnderscoreTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
		Address expectedResult = new Address();
		expectedResult.setCity("Perth");
		expectedResult.setCountry("Canada");
		expectedResult.setProvince("ONT");
		expectedResult.setPostalCode("Y3Q2N9");
		expectedResult.setStreet("234 Wandering _Way");
		
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        uow.registerObject(expectedResult);
        uow.commit();        

		//test the apostrophe
        String ejbqlString = "SELECT OBJECT(address) FROM Address address WHERE ";
        // \ is always treated as escape in MySQL.  Therefore ESCAPE '\' is considered a syntax error
        if (((EntityManagerImpl)em.getDelegate()).getServerSession().getPlatform().isMySQL()) {
            ejbqlString = ejbqlString + "address.street LIKE '234 Wandering $_Way' ESCAPE '$'";
        } else {
            ejbqlString = ejbqlString + "address.street LIKE '234 Wandering \\_Way' ESCAPE '\\'";            
        }
        
        List result = em.createQuery(ejbqlString).getResultList();
    
        Assert.assertTrue("Simple Escape Underscore test failed", comparer.compareObjects(result, expectedResult)); 
    }
    
    public void smallProjectMemberOfProjectsTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ReadAllQuery query = new ReadAllQuery();
        Expression selectionCriteria = new ExpressionBuilder().anyOf("projects").equal(
			new ExpressionBuilder(SmallProject.class));
        query.setSelectionCriteria(selectionCriteria);
        query.setReferenceClass(Employee.class);
        query.dontUseDistinct(); //gf 1395 changed jpql to not use distinct on joins
		
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(query);        
        
        clearCache();
        
        //setup the EJBQL to do the same
        String ejbqlString = "SELECT OBJECT(employee) FROM Employee employee, SmallProject sp WHERE ";
        ejbqlString = ejbqlString + "sp MEMBER OF employee.projects";
        
        List result = em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple small Project Member Of Projects test failed", comparer.compareObjects(result, expectedResult)); 
        
    }
    
    public void smallProjectNOTMemberOfProjectsTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        //query for those employees with Project named "Enterprise" (which should be
		//a SmallProject)
		ReadObjectQuery smallProjectQuery = new ReadObjectQuery();
		smallProjectQuery.setReferenceClass(SmallProject.class);
		smallProjectQuery.setSelectionCriteria(
			new ExpressionBuilder().get("name").equal("Enterprise"));
		SmallProject smallProject = (SmallProject)em.getActiveSession().executeQuery(smallProjectQuery);
			
		ReadAllQuery query = new ReadAllQuery();
		query.addArgument("smallProject");
		Expression selectionCriteria = new ExpressionBuilder().noneOf("projects", 
			new ExpressionBuilder().equal(new ExpressionBuilder().getParameter("smallProject")));
		
		query.setSelectionCriteria(selectionCriteria);
		query.setReferenceClass(Employee.class);

		Vector arguments = new Vector();
		arguments.add(smallProject);
		Vector expectedResult = (Vector)em.getActiveSession().executeQuery(query, arguments);


		//setup the EJBQL to do the same
        String ejbqlString = "SELECT OBJECT(employee) FROM Employee employee WHERE ";
        ejbqlString = ejbqlString + "?1 NOT MEMBER OF employee.projects";
        
        List result = em.createQuery(ejbqlString).setParameter(1,smallProject).getResultList();

        Assert.assertTrue("Simple small Project NOT Member Of Projects test failed", comparer.compareObjects(result, expectedResult)); 
        
    }
    //This test demonstrates the bug 4616218, waiting for bug fix
    public void selectCountOneToOneTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ReportQuery query = new ReportQuery();		
		query.setReferenceClass(PhoneNumber.class);
		//need to specify Long return type
		query.addCount("COUNT", new ExpressionBuilder().get("owner").distinct(), Long.class);
        query.returnSingleAttribute();
		query.dontRetrievePrimaryKeys();
		query.setName("selectEmployeesThatHavePhoneNumbers");

        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(query);
        
        clearCache();
                
		//setup the EJBQL to do the same
        String ejbqlString = "SELECT COUNT(DISTINCT phone.owner) FROM PhoneNumber phone";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
    
        Assert.assertTrue("Simple Select Count One To One test failed", expectedResult.elementAt(0).equals(result.get(0)));         
               
    }
    
    public void selectOneToOneTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ReadAllQuery query = new ReadAllQuery();		
		query.setReferenceClass(Address.class);
		query.useDistinct();
		ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
		Expression selectionCriteria = new ExpressionBuilder(Address.class).equal( 
			employeeBuilder.get("address")).and(
				employeeBuilder.get("lastName").like("%Way%")
			);
		query.setSelectionCriteria(selectionCriteria);
    	Vector expectedResult = (Vector)em.getActiveSession().executeQuery(query);	
        
        clearCache();
        
		//setup the EJBQL to do the same
        String ejbqlString = "SELECT DISTINCT employee.address FROM Employee employee WHERE employee.lastName LIKE '%Way%'";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();
 
        Assert.assertTrue("Simple Select One To One test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    
    public void selectPhonenumberDeclaredInINClauseTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        ReadAllQuery query = new ReadAllQuery();
		ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
		Expression phoneAnyOf = employeeBuilder.anyOf("phoneNumbers");
		ExpressionBuilder phoneBuilder = new ExpressionBuilder(PhoneNumber.class);
		Expression selectionCriteria = phoneBuilder.equal(employeeBuilder.anyOf("phoneNumbers")).and(
			phoneAnyOf.get("number").notNull());
		query.setSelectionCriteria(selectionCriteria);
		query.setReferenceClass(PhoneNumber.class);
		query.addAscendingOrdering("number");
		query.addAscendingOrdering("areaCode");
		
        Vector expectedResult = (Vector)em.getActiveSession().executeQuery(query);
		
        clearCache();
        
		//setup the EJBQL to do the same
        String ejbqlString = "Select Distinct Object(p) from Employee emp, IN(emp.phoneNumbers) p WHERE ";
        ejbqlString = ejbqlString + "p.number IS NOT NULL ORDER BY p.number, p.areaCode";
        
        List result = (List) em.createQuery(ejbqlString).getResultList();

        Assert.assertTrue("Simple select Phonenumber Declared In IN Clause test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void selectSimpleMemberOfWithParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Employee expectedResult = (Employee)em.getActiveSession().readObject(Employee.class);
        
        PhoneNumber phone = new PhoneNumber();
        phone.setAreaCode("613");
        phone.setNumber("1234567");
        phone.setType("cell");                
        
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        PhoneNumber phoneClone = (PhoneNumber) uow.registerObject(phone);
        Employee empClone = (Employee) uow.registerObject(expectedResult);
        
        phoneClone.setOwner(empClone);
        empClone.addPhoneNumber(phoneClone);        
        uow.registerObject(phone);
        uow.commit();

        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp " +
            "WHERE ?1 MEMBER OF emp.phoneNumbers";
            
        Vector parameters = new Vector();
	    parameters.add(phone);

        List result = em.createQuery(ejbqlString).setParameter(1,phone).getResultList();

        uow = clientSession.acquireUnitOfWork();
        uow.deleteObject(phone);        
        uow.commit();
                               
        Assert.assertTrue("Select simple member of with parameter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void selectSimpleNotMemberOfWithParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector expectedResult = (Vector)em.getActiveSession().readAllObjects(Employee.class);
        
        clearCache();
        
        Employee emp = (Employee) expectedResult.get(0);
        expectedResult.remove(0);
        
        PhoneNumber phone = new PhoneNumber();
        phone.setAreaCode("613");
        phone.setNumber("1234567");
        phone.setType("cell");

        
        Server serverSession = (Server) JUnitTestCase.getServerSession();
        Session clientSession = serverSession.acquireClientSession();
        UnitOfWork uow = clientSession.acquireUnitOfWork();
        emp = (Employee)uow.readObject(emp);
        PhoneNumber phoneClone = (PhoneNumber)uow.registerObject(phone);
        phoneClone.setOwner(emp);
        emp.addPhoneNumber(phoneClone);
        uow.commit();
    
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp " +
            "WHERE ?1 NOT MEMBER OF emp.phoneNumbers";
            
        Vector parameters = new Vector();
	    parameters.add(phone);

        List result = em.createQuery(ejbqlString).setParameter(1,phone).getResultList();

        uow = clientSession.acquireUnitOfWork();
        uow.deleteObject(phone);
        uow.commit();
            
        Assert.assertTrue("Select simple Not member of with parameter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void selectUsingLockModeQueryHintTest() {
        Exception exception = null;
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector employees = (Vector)em.getActiveSession().readAllObjects(Employee.class);
        Employee emp1 = (Employee)employees.lastElement();
        Employee emp2 = new Employee();
        
        try {
            javax.persistence.Query query = em.createNamedQuery("findEmployeeByPK");
            query.setParameter("id", emp1.getId());
            query.setHint("lockMode", new Short((short) 1));
        
            emp2 = (Employee) query.getSingleResult();
        } catch (Exception e) {
            exception = e;
        }
  
        Assert.assertNull("An exception was caught: " + exception, exception);
        Assert.assertTrue("The query did not return the same employee.", emp1.getId() == emp2.getId());
    }
    
    public void selectSimpleBetweenWithParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector employees = (Vector)em.getActiveSession().readAllObjects(Employee.class);
        
        BigDecimal empId1 = new BigDecimal(0);
	    
        Employee emp2 = (Employee)employees.lastElement();
        
        ReadAllQuery raq = new ReadAllQuery();
	    raq.setReferenceClass(Employee.class);	    
	    ExpressionBuilder eb = new ExpressionBuilder();
	    Expression whereClause = eb.get("id").between(empId1, emp2.getId());	    
	    raq.setSelectionCriteria(whereClause);
	    
	    Vector expectedResult = (Vector)getServerSession().executeQuery(raq);
	    
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "emp.id BETWEEN ?1 AND ?2";
        
        List result = (List) em.createQuery(ejbqlString).setParameter(1,empId1).setParameter(2,emp2.getId()).getResultList();
  
        Assert.assertTrue("Simple select between with parameter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    public void selectSimpleInWithParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        
        Vector employees = (Vector)em.getActiveSession().readAllObjects(Employee.class);
        
        BigDecimal empId1 = new BigDecimal(0);
            
        Employee emp2 = (Employee)employees.lastElement();
        
        ReadAllQuery raq = new ReadAllQuery();
            raq.setReferenceClass(Employee.class);          
            ExpressionBuilder eb = new ExpressionBuilder();
            Vector vec = new Vector();
            vec.add(empId1);
            vec.add(emp2.getId());
            
            Expression whereClause = eb.get("id").in(vec);            
            raq.setSelectionCriteria(whereClause);
            
            Vector expectedResult = (Vector)getServerSession().executeQuery(raq);
            
        clearCache();
        
        String ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "emp.id IN (?1, ?2)";
        
        List result = (List) em.createQuery(ejbqlString).setParameter(1,empId1).setParameter(2,emp2.getId()).getResultList();
    
        Assert.assertTrue("Simple select between with parameter test failed", comparer.compareObjects(result, expectedResult));                 
        
    }
    
    //Test case for ABS function in EJBQL
    public void simpleEnumTest()

    { 
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();             
        
	String ejbqlString;

        ejbqlString = "SELECT emp FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "emp.status =  oracle.toplink.essentials.testing.models.cmp3.advanced.Employee.EmployeeStatus.FULL_TIME";
        List result = em.createQuery(ejbqlString).getResultList();
        
    }
        
    public void selectNamedNativeQueryWithPositionalParameterTest() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        List results_QuestionMark_Number = null;
        List results_QuestionMark = null;
        javax.persistence.Query query;
        String errorMsg ="";
        
        boolean shouldCompareResults = true;
        try {
            query = em.createNamedQuery("findAllSQLAddressesByCity_QuestionMark_Number");
            query.setParameter(1, "Ottawa");
    
            results_QuestionMark_Number = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCity_QuestionMark_Number: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        try {
            query = em.createNamedQuery("findAllSQLAddressesByCity_QuestionMark");
            query.setParameter(1, "Ottawa");
        
            results_QuestionMark = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCity_QuestionMark: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        if(shouldCompareResults) {
            if(results_QuestionMark_Number.size() != results_QuestionMark.size()) {
                errorMsg = errorMsg + ("findAllSQLAddressesByCity_QuestionMark_Number and findAllSQLAddressesByCity_QuestionMark produced non-equal results");
            }
        }
        
        shouldCompareResults = true;
        try {
            query = em.createNamedQuery("findAllSQLAddressesByCityAndCountry_QuestionMark_Number");
            query.setParameter(1, "Ottawa");
            query.setParameter(2, "Canada");
        
            results_QuestionMark_Number = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCityAndCountry_QuestionMark_Number: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        try {
            query = em.createNamedQuery("findAllSQLAddressesByCityAndCountry_QuestionMark");
            query.setParameter(1, "Ottawa");
            query.setParameter(2, "Canada");
        
            results_QuestionMark = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCityAndCountry_QuestionMark: " + e.getMessage();
            shouldCompareResults = false;
        }
        if(shouldCompareResults) {
            if(results_QuestionMark_Number.size() != results_QuestionMark.size()) {
                errorMsg = errorMsg + ("findAllSQLAddressesByCityAndCountry_QuestionMark_Number and findAllSQLAddressesByCityAndCountry_QuestionMark produced non-equal results");
            }
        }

        if(errorMsg.length() > 0) {
            Assert.fail(errorMsg);
        }
        em.close();
    }
    
    public void selectNativeQueryWithPositionalParameterTest() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        List results_QuestionMark_Number = null;
        List results_QuestionMark = null;
        javax.persistence.Query query;
        String errorMsg ="";
        
        boolean shouldCompareResults = true;
        try {
            query = em.createNativeQuery("select * from CMP3_ADDRESS where city=?1", Address.class);
            query.setParameter(1, "Ottawa");
    
            results_QuestionMark_Number = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCity_QuestionMark_Number: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        try {
            query = em.createNativeQuery("select * from CMP3_ADDRESS where city=?", Address.class);
            query.setParameter(1, "Ottawa");
        
            results_QuestionMark = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCity_QuestionMark: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        if(shouldCompareResults) {
            if(results_QuestionMark_Number.size() != results_QuestionMark.size()) {
                errorMsg = errorMsg + ("findAllSQLAddressesByCity_QuestionMark_Number and findAllSQLAddressesByCity_QuestionMark produced non-equal results");
            }
        }
        
        shouldCompareResults = true;
        try {
            query = em.createNativeQuery("select * from CMP3_ADDRESS where city=?1 and country=?2", Address.class);
            query.setParameter(1, "Ottawa");
            query.setParameter(2, "Canada");
        
            results_QuestionMark_Number = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCityAndCountry_QuestionMark_Number: " + e.getMessage() +"\n";
            shouldCompareResults = false;
        }
        try {
            query = em.createNativeQuery("select * from CMP3_ADDRESS where city=? and country=?", Address.class);
            query.setParameter(1, "Ottawa");
            query.setParameter(2, "Canada");
        
            results_QuestionMark = query.getResultList();
        } catch (Exception e) {
            errorMsg = errorMsg + "findAllSQLAddressesByCityAndCountry_QuestionMark: " + e.getMessage();
            shouldCompareResults = false;
        }
        if(shouldCompareResults) {
            if(results_QuestionMark_Number.size() != results_QuestionMark.size()) {
                errorMsg = errorMsg + ("findAllSQLAddressesByCityAndCountry_QuestionMark_Number and findAllSQLAddressesByCityAndCountry_QuestionMark produced non-equal results");
            }
        }

        if(errorMsg.length() > 0) {
            Assert.fail(errorMsg);
        }
        em.close();
    }
    
  public static void main(String[] args)
  {
    junit.swingui.TestRunner.main(args);
    
  }
}
