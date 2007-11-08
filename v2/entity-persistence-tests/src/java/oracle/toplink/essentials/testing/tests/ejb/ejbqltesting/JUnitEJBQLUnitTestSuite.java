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
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.mappings.DirectToFieldMapping;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.ReportQueryResult;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.PhoneNumber;
import javax.persistence.Query;


/**
 * <p>
 * <b>Purpose</b>: Test Unit EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for EJBQL functionality
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
 
 
//This test suite demonstrates the bug 4616218, waiting for bug fix
public class JUnitEJBQLUnitTestSuite extends JUnitTestCase
{ 
  static JUnitDomainObjectComparer comparer; 
  
  public JUnitEJBQLUnitTestSuite()
  {
      super();
  }

  public JUnitEJBQLUnitTestSuite(String name)
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
    suite.setName("JUnitEJBQLUnitTestSuite");
    suite.addTest(new JUnitEJBQLUnitTestSuite("testSelectPhoneNumberAreaCode"));   
    suite.addTest(new JUnitEJBQLUnitTestSuite("testSelectPhoneNumberAreaCodeWithEmployee"));   
    suite.addTest(new JUnitEJBQLUnitTestSuite("testSelectPhoneNumberNumberWithEmployeeWithExplicitJoin"));   
    suite.addTest(new JUnitEJBQLUnitTestSuite("testSelectPhoneNumberNumberWithEmployeeWithFirstNameFirst"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testSelectEmployeeWithSameParameterUsedMultipleTimes"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testOuterJoinOnOneToOne"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testOuterJoinPolymorphic"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testFirstResultOnNamedQuery"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testInvertedSelectionCriteriaNullPK"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testInvertedSelectionCriteriaInvalidQueryKey"));
    suite.addTest(new JUnitEJBQLUnitTestSuite("testDistinctSelectForEmployeeWithNullAddress"));

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
    
    public Vector getAttributeFromAll(String attributeName, Vector objects, Class referenceClass){
	   
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        ClassDescriptor descriptor = em.getActiveSession().getClassDescriptor(referenceClass);
	    DirectToFieldMapping mapping = (DirectToFieldMapping)descriptor.getMappingForAttributeName(attributeName);
	    
	    Vector attributes = new Vector();
	    Object currentObject;
	    for(int i = 0; i < objects.size(); i++) {
            currentObject = objects.elementAt(i);
            if(currentObject.getClass() == ReportQueryResult.class) {
                attributes.addElement(
                    ((ReportQueryResult)currentObject).get(attributeName));
            } else {
                attributes.addElement(
                    mapping.getAttributeValueFromObject(currentObject));
            }
	    }
	    return attributes;
	}
    
    public void testFirstResultOnNamedQuery(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        clearCache();

        Query query = em.createNamedQuery("findAllEmployeesByFirstName");
        List initialList = query.setParameter("firstname", "Nancy").setFirstResult(0).getResultList();
        
        List secondList = query.setParameter("firstname", "Nancy").setFirstResult(1).getResultList();

        Iterator i = initialList.iterator();
        while (i.hasNext()){
        	assertTrue("Employee with incorrect name returned on first query.", ((Employee)i.next()).getFirstName().equals("Nancy"));
        }
        i = secondList.iterator();
        while (i.hasNext()){
        	assertTrue("Employee with incorrect name returned on second query.", ((Employee)i.next()).getFirstName().equals("Nancy"));
        }
    }
    
    public void testOuterJoinOnOneToOne(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        clearCache();
        em.getTransaction().begin();
        int initialSize = em.createQuery("SELECT e from Employee e JOIN e.address a").getResultList().size(); 
        Employee emp = new Employee();
        emp.setFirstName("Steve");
        emp.setLastName("Harp");
        em.persist(emp);
        em.flush();
        List result = em.createQuery("SELECT e from Employee e LEFT OUTER JOIN e.address a").getResultList();
        assertTrue("Outer join was not properly added to the query", initialSize + 1 == result.size());
        em.getTransaction().rollback();
    }

    public void testOuterJoinPolymorphic(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        clearCache();
        List resultList = null;
        try{
            resultList = em.createQuery("SELECT p FROM Project p").getResultList();
        } catch (Exception exception){
            fail("Exception caught while executing polymorphic query.  This may mean that outer join is not working correctly on your database platfrom: " + exception.toString());            
        }
        assertTrue("Incorrect number of projects returned.", resultList.size() == 15);
    }

  //This test case demonstrates the bug 4616218
  public void testSelectPhoneNumberAreaCode()
  {
        
        ExpressionBuilder employeeBuilder = new ExpressionBuilder();
		Expression phones = employeeBuilder.anyOf("phoneNumbers");
    	Expression whereClause = phones.get("areaCode").equal("613");
    	    
    	ReportQuery rq = new ReportQuery();
    	rq.setSelectionCriteria(whereClause);
		rq.addAttribute("areaCode", new ExpressionBuilder().anyOf("phoneNumbers").get("areaCode"));
    	rq.setReferenceClass(Employee.class);    
        rq.dontUseDistinct(); // distinct no longer used on joins in JPQL for gf bug 1395
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        Vector expectedResult = getAttributeFromAll("areaCode", (Vector)em.getActiveSession().executeQuery(rq),Employee.class);
        
        clearCache();
        
        List result = em.createQuery("SELECT phone.areaCode FROM Employee employee, IN (employee.phoneNumbers) phone " + 
		    "WHERE phone.areaCode = \"613\"").getResultList();                     
      
        Assert.assertTrue("SimpleSelectPhoneNumberAreaCode test failed !", comparer.compareObjects(result,expectedResult));    	
  }
  
  
    public void testSelectPhoneNumberAreaCodeWithEmployee()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        ExpressionBuilder employees = new ExpressionBuilder();
        Expression exp = employees.get("firstName").equal("Bob");
        exp = exp.and(employees.get("lastName").equal("Smith"));
        Employee emp = (Employee) em.getActiveSession().readAllObjects(Employee.class, exp).firstElement();
    
        PhoneNumber phone = (PhoneNumber) ((Vector)emp.getPhoneNumbers()).firstElement();
        String areaCode = phone.getAreaCode();
        String firstName = emp.getFirstName();

		ExpressionBuilder employeeBuilder = new ExpressionBuilder();
		Expression phones = employeeBuilder.anyOf("phoneNumbers");
    	Expression whereClause = phones.get("areaCode").equal(areaCode).and(
    	phones.get("owner").get("firstName").equal(firstName));
    	    
    	ReportQuery rq = new ReportQuery();
    	rq.setSelectionCriteria(whereClause);
    	rq.addAttribute("areaCode", phones.get("areaCode"));
		rq.setReferenceClass(Employee.class);
    	rq.dontMaintainCache();
    	
        Vector expectedResult = getAttributeFromAll("areaCode", (Vector)em.getActiveSession().executeQuery(rq),Employee.class);   	
        
    	clearCache();
    
    	String ejbqlString;
    	ejbqlString = "SELECT phone.areaCode FROM Employee employee, IN (employee.phoneNumbers) phone " + 
		    "WHERE phone.areaCode = \"" + areaCode + "\" AND phone.owner.firstName = \"" + firstName + "\"";    
        
        List result = em.createQuery(ejbqlString).getResultList();                     
        
        Assert.assertTrue("SimpleSelectPhoneNumberAreaCodeWithEmployee test failed !", comparer.compareObjects(result,expectedResult));
        
    }
    
    public void testSelectPhoneNumberNumberWithEmployeeWithExplicitJoin()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        ExpressionBuilder employees = new ExpressionBuilder();
        Expression exp = employees.get("firstName").equal("Bob");
        exp = exp.and(employees.get("lastName").equal("Smith"));
        Employee emp = (Employee) em.getActiveSession().readAllObjects(Employee.class, exp).firstElement();
    
        PhoneNumber phone = (PhoneNumber) ((Vector)emp.getPhoneNumbers()).firstElement();
        String areaCode = phone.getAreaCode();
        String firstName = emp.getFirstName();
        
        ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
		Expression phones = employeeBuilder.anyOf("phoneNumbers");
    	Expression whereClause = phones.get("areaCode").equal(areaCode).and(
    	    phones.get("owner").get("id").equal(employeeBuilder.get("id")).and(
    	        employeeBuilder.get("firstName").equal(firstName)));

    	
    	ReportQuery rq = new ReportQuery();
		rq.addAttribute("number", new ExpressionBuilder().anyOf("phoneNumbers").get("number"));
    	rq.setSelectionCriteria(whereClause);
		rq.setReferenceClass(Employee.class);
    	
        Vector expectedResult = getAttributeFromAll("number", (Vector)em.getActiveSession().executeQuery(rq),Employee.class);
        
    	clearCache();    	
    
    	String ejbqlString;
    	ejbqlString = "SELECT phone.number FROM Employee employee, IN (employee.phoneNumbers) phone " + 
    		"WHERE phone.areaCode = \"" + areaCode + "\" AND (phone.owner.id = employee.id AND employee.firstName = \"" + firstName + "\")";
    
        List result = em.createQuery(ejbqlString).getResultList();                     
        
        Assert.assertTrue("SimpleSelectPhoneNumberAreaCodeWithEmployee test failed !", comparer.compareObjects(result,expectedResult));
        
    }
    
    public void testSelectPhoneNumberNumberWithEmployeeWithFirstNameFirst()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        ExpressionBuilder employees = new ExpressionBuilder();
        Expression exp = employees.get("firstName").equal("Bob");
        exp = exp.and(employees.get("lastName").equal("Smith"));
        Employee emp = (Employee) em.getActiveSession().readAllObjects(Employee.class, exp).firstElement();
    
        PhoneNumber phone = (PhoneNumber) ((Vector)emp.getPhoneNumbers()).firstElement();
        String areaCode = phone.getAreaCode();
        String firstName = emp.getFirstName();
        
        ExpressionBuilder employeeBuilder = new ExpressionBuilder();
		Expression phones = employeeBuilder.anyOf("phoneNumbers");
    	Expression whereClause = phones.get("owner").get("firstName").equal(firstName).and(
            	phones.get("areaCode").equal(areaCode));
    	
    	ReportQuery rq = new ReportQuery();
    	rq.setSelectionCriteria(whereClause);
    	rq.addAttribute("number", phones.get("number"));
		rq.setReferenceClass(Employee.class);
		
        Vector expectedResult = getAttributeFromAll("number", (Vector)em.getActiveSession().executeQuery(rq),Employee.class);	
        
        clearCache();
        
    	String ejbqlString;
    	ejbqlString = "SELECT phone.number FROM Employee employee, IN(employee.phoneNumbers) phone " + 
		    "WHERE phone.owner.firstName = \"" + firstName + "\" AND phone.areaCode = \"" + areaCode + "\"";
            
        List result = em.createQuery(ejbqlString).getResultList();                     
        
        Assert.assertTrue("SimpleSelectPhoneNumberAreaCodeWithEmployee test failed !", comparer.compareObjects(result,expectedResult));
        
    }

    public void testSelectEmployeeWithSameParameterUsedMultipleTimes() {
        Exception exception = null;
        
        try {
            String ejbqlString = "SELECT emp FROM Employee emp WHERE emp.id > :param1 OR :param1 IS null";
            createEntityManager().createQuery(ejbqlString).setParameter("param1", new Integer(1)).getResultList();
        } catch (Exception e) {
            exception = e;
        }
        
        Assert.assertNull("Exception was caught.", exception);
    }
    
    /**
     * Prior to the fix for GF 2333, the query in this test would a Null PK exception
     *
     */
    public void testInvertedSelectionCriteriaNullPK(){
        Exception exception = null;
        try {
            String jpqlString = "SELECT e, p FROM Employee e, PhoneNumber p WHERE p.id = e.id AND e.firstName = 'Bob'";
            List resultList = createEntityManager().createQuery(jpqlString).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        
        Assert.assertNull("Exception was caught.", exception);
    }
    
    /**
     * Prior to the fix for GF 2333, the query in this test would generate an invalid query key exception
     *
     */
    public void testInvertedSelectionCriteriaInvalidQueryKey(){
        Exception exception = null;
        try {
            String jpqlString = "select e, a from Employee e, Address a where a.city = 'Ottawa' and e.address.country = a.country";
            List resultList = createEntityManager().createQuery(jpqlString).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        
        Assert.assertNull("Exception was caught.", exception);
    }
    
    /*
     * For GF3233, Distinct process fail with NPE when relationship has NULL-valued target.
     */
    public void testDistinctSelectForEmployeeWithNullAddress(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        try {
            em.getTransaction().begin();
            Employee emp = new Employee();
            emp.setFirstName("Dummy");
            emp.setLastName("Person");
            em.persist(emp);
            em.flush();
            List resultList = em.createQuery("SELECT DISTINCT e.address FROM Employee e").getResultList();
        }finally{
            em.getTransaction().rollback();
        }
    }

    
  public static void main(String[] args)
  {
    junit.swingui.TestRunner.main(args);
  }
}
