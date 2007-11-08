package oracle.toplink.essentials.testing.tests.ejb.ejbqltesting;

import java.math.BigDecimal;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import junit.framework.Assert;
import oracle.toplink.essentials.ejb.cmp3.EntityManager;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.ReportQueryResult;
import oracle.toplink.essentials.queryframework.UpdateAllQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmploymentPeriod;
import oracle.toplink.essentials.testing.models.cmp3.advanced.PhoneNumber;
import oracle.toplink.essentials.testing.models.cmp3.relationships.Customer;
import oracle.toplink.essentials.testing.models.cmp3.relationships.CustomerDetails;
import oracle.toplink.essentials.testing.models.cmp3.relationships.Order;
import oracle.toplink.essentials.testing.models.cmp3.relationships.RelationshipsExamples;
import oracle.toplink.essentials.testing.models.cmp3.relationships.RelationshipsTableManager;
import oracle.toplink.essentials.testing.models.cmp3.relationships.SalesPerson;

public class JUnitEJBQLExamplesTestSuite extends JUnitTestCase
{
    static JUnitDomainObjectComparer comparer;    
    static EmployeePopulator employeePopulator;
    
    public JUnitEJBQLExamplesTestSuite()
    {
        super();
    }
  
    public JUnitEJBQLExamplesTestSuite(String name)
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
        suite.setName("JUnitEJBQLExamplesTestSuite");
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findAllOrders"));    
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findEmployeesInOntario"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findAllProvinceWithEmployees"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findAllEmployeesWithPhoneNumbers"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findAllEmployeesWithOutPhoneNumbers"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findAllEmployeesWithCellPhones"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findOrdersWithDifferentBilledCustomer"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("findEmployeeWithWorkPhone2258812"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("parameterTest"));        
        suite.addTest(new JUnitEJBQLExamplesTestSuite("getOrderLargerThan"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("getSalesPersonForOrders"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("getOrderForCustomer"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testOuterJoin"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testExistsExpression"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testAllExpressions"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testCountInSubQuery"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testGroupByHavingExpression"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testGroupByHavingCount"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testConstructorQuery"));   
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testSumExpression"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testAvgExpression"));        
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testOrderByExpression"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testCountInSubQuery"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testOrderByExpressionWithSelect"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testDeleteExpression"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testComplexDeleteExpression"));
       // suite.addTest(new JUnitEJBQLExamplesTestSuite("testCountExpression"));    //bug 5166658
       // suite.addTest(new JUnitEJBQLExamplesTestSuite("testUpdateExpression"));   //bug 5159164, 5159198        
        //Bug5097278 
        suite.addTest(new JUnitEJBQLExamplesTestSuite("updateAllTest"));
        //Bug5040609  
        suite.addTest(new JUnitEJBQLExamplesTestSuite("namedQueryCloneTest"));
        //Bug4924639  
        suite.addTest(new JUnitEJBQLExamplesTestSuite("aggregateParameterTest"));
        // Bug 5090182
        suite.addTest(new JUnitEJBQLExamplesTestSuite("testEJBQLQueryString"));
        suite.addTest(new JUnitEJBQLExamplesTestSuite("updateEmbeddedFieldTest"));
        
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
                employeePopulator = new EmployeePopulator();
                
                RelationshipsExamples relationshipExamples = new RelationshipsExamples();
                
                new AdvancedTableCreator().replaceTables(session);
                
                new RelationshipsTableManager().replaceTables(session);
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();
                
                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());              
                
                
                //Populate the advanced model
                employeePopulator.buildExamples();
                //populate the relationships model and persist as well
                relationshipExamples.buildExamples(session);
                                
                //Persist the advanced model examples in the database
                employeePopulator.persistExample(session);     
            }            
        };    
  }
  
  public void findAllOrders() 
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      List expectedResult = em.getActiveSession().readAllObjects(Order.class);
      
      String ejbqlString = "SELECT o FROM OrderBean o";
      List result = em.createQuery(ejbqlString).getResultList();
      // 4 orders returned
      Assert.assertEquals("Find all orders test failed: data validation error", result.size(), 4);
      Assert.assertTrue("Find all orders test failed", comparer.compareObjects(expectedResult,result));
  } 
  
  public void findEmployeesInOntario() 
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.get("address").get("province").equal("ONT");      
          
      List expectedResult = em.getActiveSession().readAllObjects(Employee.class,whereClause);
      
      String ejbqlString = "SELECT e FROM Employee e WHERE e.address.province='ONT'";
      List result = em.createQuery(ejbqlString).getResultList();
      //9 employees returned
      Assert.assertEquals("Find Employees in Ontario test failed: data validation error", result.size(), 9);
      Assert.assertTrue("Find Employees in Ontario test failed", comparer.compareObjects(expectedResult,result));
      
  }
    
  public void findAllProvinceWithEmployees()
  {
      boolean testPass = false;
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.get("address").get("province");
      
      ReportQuery rq = new ReportQuery();
      rq.returnWithoutReportQueryResult();
      rq.setReferenceClass(Employee.class);
      rq.addItem("province", whereClause);
      rq.useDistinct();
      
      List expectedResult = (List)em.getActiveSession().executeQuery(rq);
      
      String ejbqlString = "SELECT DISTINCT e.address.province FROM Employee e";
      List result = em.createQuery(ejbqlString).getResultList();
      
      if(expectedResult.equals(result))
        testPass = true;
      //5 provinces returned
      Assert.assertEquals("Find Province with employees test failed: data validation error", result.size(), 5);
      Assert.assertTrue("Find Province with employees test failed", testPass);
  }  
  
  public void findAllEmployeesWithPhoneNumbers()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.isEmpty("phoneNumbers").not();
      
      ReadAllQuery raq = new ReadAllQuery(Employee.class);
      raq.setSelectionCriteria(whereClause);
      raq.useDistinct();
      
      List expectedResult = (List)em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT DISTINCT e FROM Employee e, IN (e.phoneNumbers) l";
      List firstResult = em.createQuery(ejbqlString).getResultList();

      String alternateEjbqlString = "SELECT e FROM Employee e WHERE e.phoneNumbers IS NOT EMPTY";
      List secondResult = em.createQuery(alternateEjbqlString).getResultList();
      //14 employees returned
      Assert.assertEquals("Ejbql statements returned different results: data validation error", firstResult.size(), 14);
      Assert.assertTrue("Equivalent Ejbql statements returned different results", comparer.compareObjects(secondResult,firstResult));      
      Assert.assertTrue("Find all employees with phone numbers test failed", comparer.compareObjects(expectedResult,firstResult));
  }
  
  public void findAllEmployeesWithOutPhoneNumbers()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.isEmpty("phoneNumbers");
      
      ReadAllQuery raq = new ReadAllQuery(Employee.class);
      raq.setSelectionCriteria(whereClause);
      raq.useDistinct();
      
      List expectedResult = (List)em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT DISTINCT e FROM Employee e WHERE e.phoneNumbers IS EMPTY";
      List result = em.createQuery(ejbqlString).getResultList();
      //1 employee w/o phone number returned
      Assert.assertEquals("Find all employees WITHOUT phone numbers test failed: data validation error", result.size(), 1);
      Assert.assertTrue("Find all employees WITHOUT phone numbers test failed", comparer.compareObjects(expectedResult,result));
  }
  
  public void findAllEmployeesWithCellPhones()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.anyOf("phoneNumbers").get("type").equal("Cellular");
      
      ReadAllQuery raq = new ReadAllQuery(Employee.class);
      raq.setSelectionCriteria(whereClause);
      raq.useDistinct();
      
      List expectedResult = (List)em.getActiveSession().executeQuery(raq);
      
          String ejbqlString = "SELECT DISTINCT e FROM Employee e JOIN e.phoneNumbers p " +
      "WHERE p.type = 'Cellular'";
      List firstResult = em.createQuery(ejbqlString).getResultList();
      String alternateEjbqlString = "SELECT DISTINCT e FROM Employee e INNER JOIN e.phoneNumbers p " +
      "WHERE p.type = 'Cellular'";
      List secondResult = em.createQuery(alternateEjbqlString).getResultList();
      //4 employees returned
      Assert.assertEquals("Find all employees with cellular phone numbers test failed: data validation error", firstResult.size(), 4);
      Assert.assertTrue("Find all employees with cellular phone numbers test failed: two equivalent ejb queries return different results", comparer.compareObjects(secondResult,firstResult));
      Assert.assertTrue("Find all employees with cellular phone numbers test failed", comparer.compareObjects(expectedResult,secondResult));
  }
  
  public void findOrdersWithDifferentBilledCustomer()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.get("customer").equal(builder.get("billedCustomer")).not();
      
      ReadAllQuery raq = new ReadAllQuery(Order.class);
      raq.setSelectionCriteria(whereClause);
      
      List expectedResult = (List)em.getActiveSession().executeQuery(raq);    
      
      String ejbqlString = "SELECT o FROM OrderBean o WHERE o.customer <> o.billedCustomer";
      List firstResult = em.createQuery(ejbqlString).getResultList();
      
      String alternateEjbqlString = "SELECT o FROM OrderBean o WHERE NOT o.customer.customerId = o.billedCustomer.customerId";      
      List secondResult = em.createQuery(alternateEjbqlString).getResultList(); 
      //2 orders returned
      Assert.assertTrue("Find orders with different billed customers test failed: two equivalent ejb queries return different results", comparer.compareObjects(secondResult,firstResult));
      Assert.assertTrue("Find orders with different billed customers test failed", comparer.compareObjects(expectedResult,firstResult)); 
  }
  
  public void findEmployeeWithWorkPhone2258812()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause1 = builder.anyOf("phoneNumbers").get("type").equal("Work");
      Expression whereClause2 = builder.anyOf("phoneNumbers").get("number").equal("2258812");
            
      ReadAllQuery raq = new ReadAllQuery(Employee.class);
      raq.setSelectionCriteria(whereClause1.and(whereClause2));   
      raq.useDistinct();
      
      List expectedResult = (List)em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT DISTINCT e FROM Employee e JOIN e.phoneNumbers p " +
      "WHERE p.type = 'Work' AND p.number = '2258812' ";
      List result = em.createQuery(ejbqlString).getResultList();
      //8 employees
      Assert.assertEquals("Find employee with 2258812 number test failed: data validation error", result.size(), 8);     
      Assert.assertTrue("Find employee with 2258812 number test failed", comparer.compareObjects(expectedResult,result));
  }
  
  public void parameterTest()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      List employeeList = (List) em.getActiveSession().readAllObjects(Employee.class);
      Employee expectedEmployee = (Employee) employeeList.get(0);
      int i = 1;
      while(expectedEmployee.getPhoneNumbers().size() == 0) 
      {
        expectedEmployee = (Employee) employeeList.get(i);
        i++;
      }           
      String phoneNumber = ((PhoneNumber) expectedEmployee.getPhoneNumbers().iterator().next()).getNumber();      
      String ejbqlString = "SELECT DISTINCT e FROM Employee e, IN(e.phoneNumbers) p WHERE p.number = ?1";
      String alternateEjbqlString = "SELECT DISTINCT e FROM Employee e, IN(e.phoneNumbers) p WHERE p.number = :number";
      
      List firstResult = em.createQuery(ejbqlString).setParameter(1,phoneNumber).getResultList();
      List secondResult = em.createQuery(alternateEjbqlString).setParameter("number",phoneNumber).getResultList();
      //random test cant duplicate
      Assert.assertTrue("Parameter test failed: two equivalent ejb queries return different results", comparer.compareObjects(secondResult,firstResult));
      Assert.assertTrue("Parameter test failed", comparer.compareObjects(expectedEmployee,firstResult));
  }
  
  
  public void getOrderLargerThan()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      ExpressionBuilder builder1 = new ExpressionBuilder(Order.class);
      ExpressionBuilder builder2 = new ExpressionBuilder(Order.class);
      Expression o1Quantity = builder1.get("quantity");
      Expression o2Quantity = builder2.get("quantity");
      Expression quantityComparison = o1Quantity.greaterThan(o2Quantity);
      Expression o2CustomerName = builder2.get("customer").get("name");
      Expression nameComparison = o2CustomerName.equal("Jane Smith");
      Expression whereClause = quantityComparison.and(nameComparison);
      
      ReadAllQuery raq = new ReadAllQuery();
      raq.setSelectionCriteria(whereClause);      
      raq.setReferenceClass(Order.class);
      raq.useDistinct();
      List expectedResult = (List) em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT DISTINCT o1 FROM OrderBean o1, OrderBean o2 WHERE o1.quantity > o2.quantity AND" 
      + " o2.customer.name = 'Jane Smith' ";
      List result = em.createQuery(ejbqlString).getResultList();
      //only 1 order
      Assert.assertEquals("Get order larger than test failed: data validation error", result.size(), 1);
      Assert.assertTrue("Get order larger than test failed", comparer.compareObjects(expectedResult,result));
  }
  
  public void getOrderForCustomer() 
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.get("name").equal("Jane Smith");      
      
      ReadAllQuery raq = new ReadAllQuery(Customer.class);      
      raq.setSelectionCriteria(whereClause);
            
      Customer expectedCustomer = (Customer) (((List) em.getActiveSession().executeQuery(raq)).get(0));
      SalesPerson salesPerson = ((Order)(expectedCustomer.getOrders().iterator().next())).getSalesPerson();
      
      String ejbqlString = "SELECT DISTINCT c FROM Customer c JOIN c.orders o JOIN o.salesPerson s WHERE s.id = " + salesPerson.getId();
      List firstResult = em.createQuery(ejbqlString).getResultList();
      String alternateEjbqlString = "SELECT DISTINCT c FROM Customer c, IN(c.orders) o WHERE o.salesPerson.id = " + salesPerson.getId();       
      List secondResuslt = em.createQuery(alternateEjbqlString).getResultList();
      
      //only 1 order for this customer
      Assert.assertEquals("Get order for customer test failed: data validation error", firstResult.size(), 1);
      Assert.assertTrue("Get order for customer test failed: two equivalent ejb queries return different results", comparer.compareObjects(secondResuslt,firstResult));
      Assert.assertTrue("Get order for customer test failed", comparer.compareObjects(expectedCustomer,firstResult));
  }
  
  public void getSalesPersonForOrders()    
  {     
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      List expectedResult = em.getActiveSession().readAllObjects(SalesPerson.class);
        
      String ejbqlString = "SELECT DISTINCT o.salesPerson FROM Customer AS c, IN(c.orders) o";
      List result = em.createQuery(ejbqlString).getResultList();
      //2 sales person  
      Assert.assertEquals("Get SalesPerson for Orders test failed: data validation error", result.size(), 2);
      Assert.assertTrue("Get SalesPerson for Orders test failed", comparer.compareObjects(expectedResult,result));
  }  
  
  public void testOuterJoin()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();  
      ExpressionBuilder builder = new ExpressionBuilder();
      Expression whereClause = builder.anyOfAllowingNone("phoneNumbers").get("type").equal("Cellular");
      ReadAllQuery raq = new ReadAllQuery(Employee.class);
      raq.setSelectionCriteria(whereClause);
      List expectedResult = (List) em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT e FROM Employee e LEFT JOIN e.phoneNumbers p " +
      "WHERE p.type = 'Cellular'";
      List firstResult = em.createQuery(ejbqlString).getResultList();      
      String alternateEjbqlString = "SELECT e FROM Employee e LEFT OUTER JOIN e.phoneNumbers p " +
      "WHERE p.type = 'Cellular'";
      List secondResult = em.createQuery(alternateEjbqlString).getResultList();            
      //return 4 employees with cell phones
      Assert.assertEquals("Get SalesPerson for Orders test failed: data validation error", firstResult.size(), 4);
      Assert.assertTrue("Get Outer Join test failed: two equivalent ejb queries return different results", comparer.compareObjects(secondResult,firstResult));
      Assert.assertTrue("Get Outer Join test failed", comparer.compareObjects(expectedResult,firstResult));
  }
  
  public void testExistsExpression()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();  
      boolean testPass = false;         
      ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
      ExpressionBuilder managerBuilder = new ExpressionBuilder(Employee.class);
      ReportQuery mainQuery = new ReportQuery();
      ReportQuery subQuery = new ReportQuery();
      
      subQuery.setReferenceClass(Employee.class);
      Expression managerExpression = employeeBuilder.get("manager").get("id").equal(managerBuilder.get("id"));      
      subQuery.addAttribute("id");      
      subQuery.setSelectionCriteria(managerExpression);
      Expression employeeExpression = employeeBuilder.exists(subQuery);
      
      mainQuery.setReferenceClass(Employee.class);
      mainQuery.setSelectionCriteria(employeeExpression);
      mainQuery.addAttribute("id");
      mainQuery.returnWithoutReportQueryResult();      
      List expectedResult = (List) em.getActiveSession().executeQuery(mainQuery);      
      
      String ejbqlString = "SELECT DISTINCT emp.id FROM Employee emp WHERE EXISTS ( SELECT managedEmp.id FROM Employee managedEmp WHERE managedEmp = emp.manager)";
      List result = em.createQuery(ejbqlString).getResultList();
      
      if(result.containsAll(expectedResult) && expectedResult.containsAll(result))
        testPass = true;
      //8 employees with managers
      Assert.assertEquals("Exists Expression test failed: data validation error", result.size(), 8);
      Assert.assertTrue("Exists Expression test failed", testPass);
  }
  
  public void testAllExpressions()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      boolean testPass = false;
      ExpressionBuilder mainQueryBuilder = new ExpressionBuilder(Employee.class);
      ExpressionBuilder subQueryBuilder = new ExpressionBuilder(Employee.class);
      ReportQuery mainQuery = new ReportQuery();
      mainQuery.setReferenceClass(Employee.class);
      ReportQuery subQuery = new ReportQuery();
      subQuery.setReferenceClass(Employee.class);
      
      Expression subQueryExpression = subQueryBuilder.get("address").get("city").equal(mainQueryBuilder.get("address").get("city")).and(subQueryBuilder.get("salary").lessThan(1000));
      subQuery.setSelectionCriteria(subQueryExpression);
      subQuery.addAttribute("salary");
      Expression mainQueryExpression = mainQueryBuilder.get("salary").greaterThan(mainQueryBuilder.all(subQuery));            
      mainQuery.addAttribute("id");
      mainQuery.setSelectionCriteria(mainQueryExpression);
      mainQuery.returnWithoutReportQueryResult();
      List expectedResult = (List) em.getActiveSession().executeQuery(mainQuery);
      
      String ejbqlString = "SELECT emp.id FROM Employee emp WHERE emp.salary > ALL ( SELECT e.salary FROM Employee e WHERE e.address.city = emp.address.city AND e.salary < 1000)";
      List result = em.createQuery(ejbqlString).getResultList();
      
      if(result.containsAll(expectedResult) && expectedResult.containsAll(result))
        testPass = true;
      
      Assert.assertEquals("All Expression test failed: data validation error", result.size(), 12);
      Assert.assertTrue("All Expression test failed", testPass);
  }
  
  public void testCountInSubQuery()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      boolean testPass = false;
      ReportQuery mainQuery = new ReportQuery(Customer.class, new ExpressionBuilder());
      ReportQuery subQuery = new ReportQuery(Order.class,new ExpressionBuilder());      
      subQuery.setSelectionCriteria(subQuery.getExpressionBuilder().get("customer").get("customerId").equal(mainQuery.getExpressionBuilder().get("customerId")));
      subQuery.addCount("orderId");
      mainQuery.setSelectionCriteria(mainQuery.getExpressionBuilder().subQuery(subQuery).greaterThan(0));
      mainQuery.addAttribute("customerId");
      mainQuery.returnWithoutReportQueryResult();
      List expectedResult = (List) em.getActiveSession().executeQuery(mainQuery);      
      String ejbqlString = "SELECT c.customerId FROM Customer c WHERE (SELECT COUNT(o) FROM c.orders o) > 0";
      List result = em.createQuery(ejbqlString).getResultList();      
      if(result.containsAll(expectedResult) && expectedResult.containsAll(result))
        testPass = true;   
      
      Assert.assertEquals("Count subquery test failed: data validation error", result.size(), 2);
      Assert.assertTrue("Count subquery test failed", testPass);
  }
  
  public void testGroupByHavingExpression()
  {
     EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
     boolean testPass = true;
     
     ReadAllQuery raq = new ReadAllQuery(Employee.class, new ExpressionBuilder());
     Expression whereClause1 = raq.getExpressionBuilder().get("firstName").equal("Bob");
     Expression whereClause2 = raq.getExpressionBuilder().get("lastName").equal("Smith");
     Expression whereClause3 = raq.getExpressionBuilder().get("firstName").equal("John");
     Expression whereClause4 = raq.getExpressionBuilder().get("lastName").equal("Way");
     
     raq.setSelectionCriteria((whereClause1.and(whereClause2)).or(whereClause3.and(whereClause4)));  
     List employees = (List)em.getActiveSession().executeQuery(raq);
     int firstManagerId = ((Employee)employees.get(0)).getId();
     int secondManagerId = ((Employee)employees.get(1)).getId();
     int expectedEmployeesManaged = ((Employee)employees.get(0)).getManagedEmployees().size() + ((Employee)employees.get(1)).getManagedEmployees().size(); 
     Vector managerVector = new Vector();
     managerVector.add(firstManagerId);
     managerVector.add(secondManagerId);
     ReportQuery query = new ReportQuery(Employee.class,new ExpressionBuilder());
     query.returnWithoutReportQueryResult();
     query.addGrouping(query.getExpressionBuilder().get("manager").get("id"));
     query.setHavingExpression(query.getExpressionBuilder().get("manager").get("id").in(managerVector));
     query.addAttribute("managerId", query.getExpressionBuilder().get("manager").get("id"));
     query.addAverage("salary", Double.class);
     query.addCount("id", Long.class);
     
     
     List expectedResult = (List) em.getActiveSession().executeQuery(query);      
     
     String ejbqlString = "SELECT e.manager.id, avg(e.salary), count(e) FROM Employee e" + 
                             " GROUP BY e.manager.id HAVING e.manager.id IN (" + firstManagerId + "," + secondManagerId + ")";
     
     List result = em.createQuery(ejbqlString).getResultList();     
     int employeesManaged=0;     
     Iterator expectedResultIterator = expectedResult.iterator();
     Iterator resultIterator = result.iterator();
     if(expectedResult.size() == result.size()) 
     {        
         while(resultIterator.hasNext()){
            Object objectArray[] =  (Object[]) expectedResultIterator.next();
            Object otherObjectArray[] = (Object[]) resultIterator.next();
            testPass = testPass && objectArray[0].equals(otherObjectArray[0]);
            testPass = testPass && objectArray[1].equals(otherObjectArray[1]);
            testPass = testPass && objectArray[2].equals(otherObjectArray[2]);
            employeesManaged =  ((Long) objectArray[2]).intValue() + employeesManaged ;
        }
     } else {
          testPass = false;
     }  
     
     Assert.assertEquals("GroupBy Having expression test failed: data validation error", employeesManaged, expectedEmployeesManaged);     
     Assert.assertTrue("GroupBy Having expression test failed", testPass);     
     
  }
  
  public void testGroupByHavingCount()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      boolean testPass = true;
      ReportQuery query = new ReportQuery(Employee.class,new ExpressionBuilder());
      query.returnWithoutReportQueryResult();
      query.addGrouping(query.getExpressionBuilder().get("address").get("province"));
      query.addAttribute("province", query.getExpressionBuilder().get("address").get("province"));
      query.addCount("provinces",query.getExpressionBuilder().get("address").get("province"), Long.class);
      query.setHavingExpression(query.getExpressionBuilder().get("address").get("province").count().greaterThan(3));      
      List expectedResult = (List) em.getActiveSession().executeQuery(query);      

      String ejbqlString = "SELECT e.address.province, COUNT(e) FROM Employee e GROUP BY e.address.province HAVING COUNT(e.address.province) > 3";
      List result = em.createQuery(ejbqlString).getResultList();
      
     Iterator expectedResultIterator = expectedResult.iterator();
     Iterator resultIterator = result.iterator();
     if(expectedResult.size() == result.size()) 
     {        
         while(resultIterator.hasNext()){
            Object objectArray[] =  (Object[]) expectedResultIterator.next();
            Object otherObjectArray[] = (Object[]) resultIterator.next();            
            testPass = testPass && objectArray[0].equals(otherObjectArray[0]);
            testPass = testPass && objectArray[1].equals(otherObjectArray[1]);            
         }        
     } else {
          testPass = false;
     }
     Assert.assertEquals("GroupBy Having count expression test failed: data validation error", result.size(), 1);
     Assert.assertTrue("GroupBy Having Count expression test failed", testPass);      
  }
  
  public void testConstructorQuery()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      String ejbqlString = "SELECT NEW oracle.toplink.essentials.testing.models.cmp3.relationships.CustomerDetails(c.customerId, o.quantity) FROM Customer " +
                            "c JOIN c.orders o WHERE o.quantity > 100";
                            
      List custDetails = (List)em.createQuery(ejbqlString).getResultList();
      Assert.assertTrue("Constructor query test failed: not an instance of CustomerDetail", custDetails.get(0) instanceof CustomerDetails);      
      Assert.assertEquals("Constructor query test failed, expecting only 1 customer with order > 100", custDetails.size(),1);
  }
  
  public void testSumExpression()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      ReportQuery query = new ReportQuery(Employee.class,new ExpressionBuilder());
      Expression whereClause = query.getExpressionBuilder().get("address").get("province").equal("QUE");
      query.addSum("areaCodeSums", query.getExpressionBuilder().anyOf("phoneNumbers").get("id"),Long.class);
      query.setSelectionCriteria(whereClause);
      query.returnWithoutReportQueryResult();            
      Long expectedResult = (Long) ((List) em.getActiveSession().executeQuery(query)).get(0);
      String ejbqlString = "SELECT SUM(p.id) FROM Employee e JOIN e.phoneNumbers p JOIN e.address a" +
                            " WHERE a.province = 'QUE' ";                           
      Long result = (Long) em.createQuery(ejbqlString).getSingleResult();  
      Assert.assertEquals("Average expression test failed", expectedResult, result);
  }
  
  public void testAvgExpression()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      ReportQuery query = new ReportQuery(Order.class,new ExpressionBuilder());
      query.addAverage("average quantity", query.getExpressionBuilder().get("quantity"),Double.class);      
      query.returnSingleResult();
      Double expectedResult = (Double) ((ReportQueryResult) em.getActiveSession().executeQuery(query)).get("average quantity");      
      String ejbqlString = "SELECT AVG(o.quantity) FROM OrderBean o";
      Double result = (Double) em.createQuery(ejbqlString).getSingleResult();        
      Assert.assertEquals("Average expression test failed", expectedResult, result);
  }
  
  //bug 5166658
  public void testCountExpression()
  {
       EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
       
       ReportQuery query = new ReportQuery(Employee.class, new ExpressionBuilder());
       Expression whereClause1 = query.getExpressionBuilder().get("address").get("province").equal("QUE");
       Expression whereClause2 = query.getExpressionBuilder().get("address").get("city").equal("Montreal");
       query.setSelectionCriteria(whereClause1.and(whereClause2));
       query.addCount("areaCodeCount", query.getExpressionBuilder().anyOf("phoneNumbers").get("areaCode"),Long.class);
       query.returnSingleResult();
       Long expectedResult = (Long) ((ReportQueryResult)em.getActiveSession().executeQuery(query)).get("areaCodeCount");
       
       String ejbqlString = "SELECT COUNT(p.areaCode) FROM Employee e JOIN e.phoneNumbers p JOIN e.address a " +
                            " WHERE a.province='QUE' AND a.city='Montreal'";
       Long result = (Long) em.createQuery(ejbqlString).getSingleResult();        

       String alternateEjbqlString = "SELECT COUNT(p) FROM Employee e JOIN e.phoneNumbers p JOIN e.address a " +
                            " WHERE a.province='QUE' AND a.city='Montreal' AND p.areaCode IS NOT NULL";
       BigDecimal alternateResult = (BigDecimal) em.createQuery(alternateEjbqlString).getSingleResult();
       
       Assert.assertTrue("Count expression test failed: data validation error, ReportQuery returned 0", expectedResult.intValue() > 0);
       Assert.assertTrue("Count expression test failed: data validation error, first JPQL returned 0", result.intValue() > 0);
       Assert.assertTrue("Count expression test failed: data validation error, second JPQL returned 0", alternateResult.intValue() > 0);
       Assert.assertTrue("Count expression test failed: two equivalent ejb queries return different results", alternateResult.equals(result));
       Assert.assertEquals("Count expression test failed", expectedResult, result);
  }
  
  public void testOrderByExpression()
  {      
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      
      ReadAllQuery raq = new ReadAllQuery(PhoneNumber.class, new ExpressionBuilder());      
      Expression whereClause = raq.getExpressionBuilder().get("owner").get("address").get("province").equal("ONT");
      raq.setSelectionCriteria(whereClause);
      raq.addOrdering(raq.getExpressionBuilder().get("areaCode"));
      raq.addOrdering(raq.getExpressionBuilder().get("type"));
      List expectedResult = (List) em.getActiveSession().executeQuery(raq);
      
      String ejbqlString = "SELECT p FROM Employee e JOIN e.phoneNumbers p JOIN e.address a WHERE a.province = 'ONT' " +
      "ORDER BY p.areaCode, p.type";    
      List result = em.createQuery(ejbqlString).getResultList();
      Assert.assertEquals("OrderBy expression test failed: data validation error", result.size(), 13);
      Assert.assertTrue("OrderBy expression test failed", comparer.compareObjects(expectedResult, result));      
  }
  
  public void testOrderByExpressionWithSelect()
  {
      EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
      boolean testPass = true;
      ReportQuery query = new ReportQuery(PhoneNumber.class, new ExpressionBuilder());
      Expression whereClause = query.getExpressionBuilder().get("owner").get("address").get("province").equal("ONT");
      query.setSelectionCriteria(whereClause);                  
      query.addOrdering(query.getExpressionBuilder().get("areaCode"));
      query.addOrdering(query.getExpressionBuilder().get("type"));
      query.addAttribute("areaCode", query.getExpressionBuilder().get("areaCode"));
      query.addAttribute("type", query.getExpressionBuilder().get("type"));
      //removed as distinct no longer used on joins in JPQL
      //query.useDistinct();
      query.returnWithoutReportQueryResult();
      List expectedResult = (List) em.getActiveSession().executeQuery(query);

      String ejbqlString = "SELECT p.areaCode, p.type FROM Employee e JOIN e.phoneNumbers p JOIN e.address a WHERE a.province = 'ONT' " +
      "ORDER BY p.areaCode, p.type";          
      List result = em.createQuery(ejbqlString).getResultList();
      Iterator expectedResultIterator = expectedResult.iterator();
      Iterator resultIterator = result.iterator();
      if(expectedResult.size() == result.size()) 
      {        
         while(resultIterator.hasNext()){
            Object objectArray[] =  (Object[]) expectedResultIterator.next();
            Object otherObjectArray[] = (Object[]) resultIterator.next();            
            testPass = testPass && objectArray[0].equals(otherObjectArray[0]);
            testPass = testPass && objectArray[1].equals(otherObjectArray[1]);                      
         }        
      } else {
          testPass = false;
      }

      Assert.assertEquals("OrderBy expression test failed: data validation error", result.size(), 13);
      Assert.assertTrue("OrderBy with Select expression test failed", testPass);
   }
   
   public void testDeleteExpression()
   {
       EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
       String ejbqlString = "DELETE FROM Customer c WHERE c.name='Karen McDonald' ";
       em.getTransaction().begin();
       int result = em.createQuery(ejbqlString).executeUpdate();    
       Assert.assertEquals("Delete Expression test failed: customer to delete not found", 1, result);
       em.flush();       
       
       ReadAllQuery raq = new ReadAllQuery(Customer.class, new ExpressionBuilder());
       Expression whereClause = raq.getExpressionBuilder().get("name").equal("Karen McDonald");
       raq.setSelectionCriteria(whereClause);
       List customerFound = (List) em.getActiveSession().executeQuery(raq);       
       Assert.assertEquals("Delete Expression test failed", 0, customerFound.size());    
       
       em.getTransaction().rollback();
   }
   
   // Bug 5090182
   public void testEJBQLQueryString() {
        List<Object[]> emps = createEntityManager().createQuery("SELECT e, a FROM Employee e, Address a WHERE e.address = a").getResultList(); 
        assertFalse("No employees were read, debug and look at the SQL that was generated. ", emps.isEmpty());
   }
   
   public void testComplexDeleteExpression()
   {
       EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();        
       String ejbqlString = "DELETE FROM Customer c WHERE c.name='Karen McDonald' AND c.orders IS EMPTY";
       em.getTransaction().begin();
       int result = em.createQuery(ejbqlString).executeUpdate();
       Assert.assertEquals("Complex Delete Expression test failed: customer to delete not found", 1, result);
       em.flush();  
       
       ReadAllQuery raq = new ReadAllQuery(Customer.class, new ExpressionBuilder());
       Expression whereClause1 = raq.getExpressionBuilder().get("name").equal("Karen McDonald");
       Expression whereClause2 = raq.getExpressionBuilder().isEmpty("orders");
       raq.setSelectionCriteria(whereClause1.and(whereClause2));
       List customerFound = (List) em.getActiveSession().executeQuery(raq);       
       Assert.assertEquals("Complex Delete Expression test failed", 0, customerFound.size());    
       
       em.getTransaction().rollback();    
   } 
   
   //bug 5159164, 5159198   
   public void testUpdateExpression()
   {
       EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();        
       int result = 0;
       UpdateAllQuery uaq = new UpdateAllQuery(Customer.class, new ExpressionBuilder());
       uaq.addUpdate(uaq.getExpressionBuilder().get("name"), "Test Case");
       Expression whereClause1 = uaq.getExpressionBuilder().get("name").equal("Jane Smith");       
       
       ReportQuery innerQuery = new ReportQuery(Customer.class, new ExpressionBuilder(Customer.class));
       innerQuery.addCount("orders", innerQuery.getExpressionBuilder().anyOf("orders").get("orderId"));
       
       Expression whereClause2 = uaq.getExpressionBuilder().subQuery(innerQuery).greaterThan(0);       
       uaq.setSelectionCriteria(whereClause1.and(whereClause2));
       em.getTransaction().begin();
       List expectedResult = (List) em.getActiveSession().executeQuery(uaq);     
       em.getTransaction().commit();
       
       String ejbqlString = "UPDATE Customer c SET c.name = 'Test Case' WHERE c.name = 'Jane Smith' " +
       "AND 0 < (SELECT COUNT(o) FROM Customer cust JOIN cust.orders o)";
       
       try {
            result = em.createQuery(ejbqlString).executeUpdate();
       } catch (Exception e) 
       {
           e.printStackTrace();
       }       
       Assert.assertEquals("Update expression test failed", 1, expectedResult);
       Assert.assertEquals("Update Expression test failed", result , expectedResult);
   }
  
    //Bug5097278 Test case for updating the manager of ALL employees that have a certain address 
    public void updateAllTest()
    {          
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();

        String empName = "Saunders";
        String manName = "Smitty";
        
        String ejbqlString = "SELECT DISTINCT e FROM Employee e WHERE e.lastName = '"+ empName +"'";
        Employee employee = (Employee)em.createQuery(ejbqlString).getSingleResult();
        Address addr = em.find(Address.class, employee.getAddress().getId());
        
        String ejbqlString2 = "SELECT DISTINCT e FROM Employee e WHERE e.lastName = '"+ manName +"'";
        Employee manager = (Employee)em.createQuery(ejbqlString2).getSingleResult();        
        

        em.getTransaction().begin();
        
        em.createQuery("UPDATE Employee e SET e.manager = :manager " + "WHERE e.address = :addr ")
               .setParameter("manager", manager)
               .setParameter("addr", addr)
               .executeUpdate();

        em.getTransaction().commit();

         String ejbqlString3 = "SELECT DISTINCT e.manager FROM Employee e WHERE e.lastName = '"+ empName +"'";         
         String result = ((Employee) em.createQuery(ejbqlString3).getSingleResult()).getLastName();
         
         Assert.assertTrue("UpdateAll test failed", result.equals(manName));                 
    
    }

    public void updateEmbeddedFieldTest()
    {
 
        EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(1905, 11, 31, 0, 0, 0);
        java.sql.Date startDate = new java.sql.Date(startCalendar.getTime().getTime()); 
        try {
            em.getTransaction().begin();
        
            em.createQuery("UPDATE Employee e SET e.period.startDate= :startDate")
               .setParameter("startDate", startDate)
               .executeUpdate();

            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail("UpdateEmbeddedFieldTest failed");
        }
    }


    //Bug5040609  Test if EJBQuery makes a clone of the original DatabaseQuery from the session
    public void namedQueryCloneTest() 
    {
        EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();            
        
        List result1 = em.createNamedQuery("findAllCustomers").getResultList();

        List result2 = em.createNamedQuery("findAllCustomers").setMaxResults(1).getResultList(); 
        
        List result3 = em.createNamedQuery("findAllCustomers").getResultList();

        Assert.assertEquals("Named query clone test failed: the first result should be 4", result1.size(), 4);
        Assert.assertEquals("Named query clone test failed: the second result should be 1", result2.size(), 1);
        Assert.assertEquals("Named query clone test failed: the third result should be 4", result3.size(), 4);
    } 
        
    //Bug5040609 Test case for aggregates as parameters in EJBQL
    public void aggregateParameterTest()
    {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();         
                 
        ExpressionBuilder builder = new ExpressionBuilder(); 
        ReportQuery query = new ReportQuery(oracle.toplink.essentials.testing.models.cmp3.advanced.Employee.class, builder);
        query.returnWithoutReportQueryResult(); 
        query.addItem("employee", builder); 
         
        oracle.toplink.essentials.testing.models.cmp3.advanced.EmploymentPeriod period = new EmploymentPeriod();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.set(1901, 11, 31, 0, 0, 0);
        endCalendar.set(1995, 0, 12, 0, 0, 0);
        period.setStartDate(new java.sql.Date(startCalendar.getTime().getTime())); 
        period.setEndDate(new java.sql.Date(endCalendar.getTime().getTime())); 
        Expression exp = builder.get("period").equal(builder.getParameter("period"));
        query.setSelectionCriteria(exp); 
        query.addArgument("period", EmploymentPeriod.class); 
        
        Vector args = new Vector(); 
        args.add(period); 

        List expectedResult = (Vector)em.getActiveSession().executeQuery(query, args);   
        
        List result = em.createQuery("SELECT e FROM Employee e WHERE e.period = :period ")
                       .setParameter("period", period)
                       .getResultList();

        Assert.assertTrue("aggregateParameterTest failed", comparer.compareObjects(expectedResult,result));
    }
    
  public static void main(String[] args)
  {
    junit.swingui.TestRunner.main(args);
    
  }
  
}
