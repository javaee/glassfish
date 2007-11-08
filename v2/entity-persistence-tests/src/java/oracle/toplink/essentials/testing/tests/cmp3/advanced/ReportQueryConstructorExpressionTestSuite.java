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


package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.queryframework.ReportQueryResult;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.exceptions.QueryException;

import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.EmployeePopulator;
import oracle.toplink.essentials.queryframework.ConstructorReportItem;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.extensions.TestSetup;

import java.util.Vector;
import java.util.Iterator;

public class ReportQueryConstructorExpressionTestSuite extends JUnitTestCase {

    protected boolean m_reset = false;    // reset gets called twice on error

    public ReportQueryConstructorExpressionTestSuite() {
    }
  
    public ReportQueryConstructorExpressionTestSuite(String name) {
        super(name);
    }
    
    public static void main(String[] args){
        junit.swingui.TestRunner.main(args);  
    }
  
        
    public void setUp () {
        m_reset = true;
        super.setUp();
        clearCache();
    }
    
    public void tearDown () {
        if (m_reset) {
            m_reset = false;
        }
        super.tearDown();
    }
    
    public static Test suite() {    
        TestSuite suite = new TestSuite(ReportQueryConstructorExpressionTestSuite.class);
        suite.setName("ReportQueryConstructorExpressionTestSuite");      

        return new TestSetup(suite) {
        
            protected void setUp(){
                           
                //get session to start setup
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                //create a new EmployeePopulator
                EmployeePopulator employeePopulator = new EmployeePopulator();
                
                new AdvancedTableCreator().replaceTables(session);
           
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);       
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testSimpleConstructorExpression(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        query.addAttribute("firstName");
        query.addAttribute("lastName");
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          

        Vector reportResults = (Vector)em.getActiveSession().executeQuery(query);
        
        employees = new ExpressionBuilder();
        query = new ReportQuery(Employee.class, employees);
        
        Class[] argTypes = new Class[]{String.class, String.class};
        query.beginAddingConstructorArguments(Employee.class, argTypes);
        query.addAttribute("firstName");
        query.addAttribute("lastName");
        query.endAddingToConstructorItem();
        Vector results = (Vector)em.getActiveSession().executeQuery(query);
        Iterator i = results.iterator();
        Iterator report = reportResults.iterator();
        while (i.hasNext()){
            Employee emp = (Employee)((ReportQueryResult)i.next()).get(Employee.class.getName());
            ReportQueryResult result = (ReportQueryResult)report.next();
            if (emp.getFirstName() != null){
                assertTrue("Null first name", result.get("firstName") != null);
                assertTrue("Wrong first name", emp.getFirstName().equals(result.get("firstName")));
            }
            if (emp.getLastName() != null){
                assertTrue("Null last name", result.get("lastName") != null);
                assertTrue("Wrong last name", emp.getLastName().equals(result.get("lastName")));
            }
            
        }
        assertTrue("Different result sizes", !(report.hasNext()));
    }

    public void testSimpleConstructorExpressionWithNamedQuery(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        query.addAttribute("firstName");
        query.addAttribute("lastName");
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          

        Vector reportResults = (Vector)em.getActiveSession().executeQuery(query);

        Vector results = (Vector)em.createNamedQuery("constuctEmployees").getResultList();
        Iterator i = results.iterator();
        Iterator report = reportResults.iterator();
        while (i.hasNext()){
            Employee emp = (Employee)i.next();
            ReportQueryResult result = (ReportQueryResult)report.next();
            if (emp.getFirstName() != null){
                assertTrue("Null first name", result.get("firstName") != null);
                assertTrue("Wrong first name", emp.getFirstName().equals(result.get("firstName")));
            }
            if (emp.getLastName() != null){
                assertTrue("Null last name", result.get("lastName") != null);
                assertTrue("Wrong last name", emp.getLastName().equals(result.get("lastName")));
            }
            
        }
        assertTrue("Different result sizes", !(report.hasNext()));
    }

    public void testMultipleTypeConstructorExpression(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        query.addAttribute("firstName");
        query.addItem("endDate", employees.get("period").get("endDate"));
        query.addAttribute("id");

        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          

        Vector reportResults = (Vector)em.getActiveSession().executeQuery(query);
        query = new ReportQuery(Employee.class, employees);

        Class[] argTypes = new Class[]{String.class, java.sql.Date.class, Integer.class};
        query.beginAddingConstructorArguments(DataHolder.class, argTypes);
        query.addAttribute("firstName");
        query.addItem("endDate", employees.get("period").get("endDate"));
        query.addAttribute("id");
        query.endAddingToConstructorItem();
        Vector results = (Vector)em.getActiveSession().executeQuery(query);
        Iterator i = results.iterator();
        Iterator report = reportResults.iterator();
        while (i.hasNext()){
            DataHolder holder = (DataHolder)((ReportQueryResult)i.next()).get(DataHolder.class.getName());
            ReportQueryResult result = (ReportQueryResult)report.next();
            if (!(holder.getString() == null && result.get("firstName") == null)){
                assertTrue("Wrong first name", holder.getString().equals(result.get("firstName")));
            }
            if (!(holder.getDate() == null && result.get("endDate") == null)){
                assertTrue("Wrong date", holder.getDate().equals(result.get("endDate")));
            }
            if (!(holder.getInteger() == null && result.get("id") == null)){
                assertTrue("Wrong integer", holder.getInteger().equals(result.get("id")));
            }
        }
        assertTrue("Different result sizes", !(report.hasNext()));
    }
    
    public void testNonExistantConstructorConstructorExpression(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        
        Class[] argTypes = new Class[]{String.class, java.sql.Date.class, Integer.class};
        query.beginAddingConstructorArguments(Employee.class, argTypes);
        query.addAttribute("firstName");
        query.addItem("endDate", employees.get("period").get("endDate"));
        query.addAttribute("id");
        query.endAddingToConstructorItem();
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          
        QueryException exception = null;
        try{
            em.getActiveSession().executeQuery(query);
        } catch (QueryException ex){
            exception = ex;
        }
        assertTrue("Exception not throw. ", exception != null);
        
    }

    public void testPrimitiveConstructorExpression(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        query.addAttribute("salary");
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          

        Vector reportResults = (Vector)em.getActiveSession().executeQuery(query);

        query = new ReportQuery(Employee.class, employees);
        Class[] argTypes = new Class[]{int.class};
        query.beginAddingConstructorArguments(DataHolder.class, argTypes);
        query.addAttribute("salary");
        query.endAddingToConstructorItem();
        Vector results = (Vector)em.getActiveSession().executeQuery(query);
        Iterator i = results.iterator();
        Iterator report = reportResults.iterator();
        while (i.hasNext()){
            DataHolder holder = (DataHolder)((ReportQueryResult)i.next()).get(DataHolder.class.getName());
            ReportQueryResult result = (ReportQueryResult)report.next();
            assertTrue("Incorrect salary ", ((Integer)result.get("salary")).intValue() == holder.getPrimitiveInt());
            
        }
        assertTrue("Different result sizes", !(report.hasNext()));
    }
    
      public void testConstructorEJBQLWithInheritance() {
        Exception exception = null;
        try {
            createEntityManager().createNamedQuery("constructLProject").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        
        assertNull("Exception was caught", exception);
    }
    
    public void testConstructorExpressionWithOtherAttributes(){
        ExpressionBuilder employees = new ExpressionBuilder();
        ReportQuery query = new ReportQuery(Employee.class, employees);
        query.addAttribute("firstName");
        query.addAttribute("lastName");
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();          

        Vector reportResults = (Vector)em.getActiveSession().executeQuery(query);

        ConstructorReportItem citem = new ConstructorReportItem("Employee");
        citem.setResultType(Employee.class);
        
        citem.addAttribute(employees.get("firstName"));
        citem.addAttribute(employees.get("lastName"));
        
        query.addConstructorReportItem(citem);

        Vector results = (Vector)em.getActiveSession().executeQuery(query);
        Iterator i = results.iterator();
        Iterator report = reportResults.iterator();
        while (i.hasNext()){
            ReportQueryResult result1 = (ReportQueryResult)i.next();
            Employee emp = (Employee) result1.get("Employee");
            //Employee emp = (Employee)i.next();
            ReportQueryResult result2 = (ReportQueryResult)report.next();
            if (emp.getFirstName() != null){
                assertTrue("Null first name in constructor query", result1.get("firstName") != null);
                assertTrue("Null first name", result2.get("firstName") != null);
                assertTrue("Wrong first name", emp.getFirstName().equals(result2.get("firstName")));
            }
            if (emp.getLastName() != null){
                assertTrue("Null last name in constructor query", result1.get("lastName") != null);
                assertTrue("Null last name", result2.get("lastName") != null);
                assertTrue("Wrong last name", emp.getLastName().equals(result2.get("lastName")));
            }
            
        }
        assertTrue("Different result sizes", !(report.hasNext()));
    }

}

