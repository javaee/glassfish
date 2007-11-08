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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.testing.models.cmp3.advanced.LargeProject;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Project;

public class ReportQueryMultipleReturnTestSuite extends JUnitTestCase {
    protected boolean m_reset = false;    // reset gets called twice on error

        
    public ReportQueryMultipleReturnTestSuite() {
    }
    
    public ReportQueryMultipleReturnTestSuite(String name) {
        super(name);
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
    
    public void testSimpleReturnDirectToField(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder empbuilder = new ExpressionBuilder();
        reportQuery.addAttribute("salary",empbuilder.get("salary"));
        reportQuery.setSelectionCriteria(empbuilder.get("salary").greaterThan(1));
        List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        Object resultItem = result.get(0);
        assertTrue("Failed to return Employees correctly, Not A Number", Number.class.isAssignableFrom(resultItem.getClass()));
        assertTrue("Failed to return Employees correctly, Not Correct Result", ((Number)resultItem).intValue() > 1);
    }
    
    public void testSimpleReturnObject(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder empbuilder = new ExpressionBuilder();
        reportQuery.addAttribute("manager",empbuilder.get("manager"));
        reportQuery.setSelectionCriteria(empbuilder.get("salary").greaterThan(1));
        List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        Object resultItem = result.get(0);
        assertTrue("Failed to return Employees correctly, Not An Employee", Employee.class.isAssignableFrom(resultItem.getClass()));
    }
    
    public void testReturnObjectAndDirectToField(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder empbuilder = new ExpressionBuilder();
        reportQuery.addAttribute("salary",empbuilder.get("salary"));
        reportQuery.addAttribute("manager",empbuilder.get("manager"));
        reportQuery.setSelectionCriteria(empbuilder.get("salary").greaterThan(1));
        List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        Object innerResult = result.get(0);
        assertTrue("Failed to return Employees correctly, Not an Object Array", Object[].class.isAssignableFrom(innerResult.getClass()));
        Object resultItem = ((Object[])innerResult)[0];
        assertTrue("Failed to return Employees correctly, Not A Number", Number.class.isAssignableFrom(resultItem.getClass()));
        assertTrue("Failed to return Employees correctly, Not Correct Result", ((Number)resultItem).intValue() > 1);
        resultItem = ((Object[])innerResult)[1];
        assertTrue("Failed to return Employees correctly, Not An Employee", Employee.class.isAssignableFrom(resultItem.getClass()));
    }
    
    public void testReturnUnrelatedObjectAndDirectToField(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(Employee.class);
        ExpressionBuilder empbuilder = new ExpressionBuilder();
        ExpressionBuilder addBuilder = new ExpressionBuilder(Address.class);
        reportQuery.addAttribute("salary",empbuilder.get("salary"));
        reportQuery.addAttribute("manager",empbuilder.get("manager"));
        reportQuery.addAttribute("adress.city",addBuilder.get("city"));
        reportQuery.setSelectionCriteria(empbuilder.get("salary").greaterThan(1));
        List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        Object innerResult = result.get(0);
        assertTrue("Failed to return Employees correctly, Not an Object Array", Object[].class.isAssignableFrom(innerResult.getClass()));
        Object resultItem = ((Object[])innerResult)[0];
        assertTrue("Failed to return Employees correctly, Not A Number", Number.class.isAssignableFrom(resultItem.getClass()));
        assertTrue("Failed to return Employees correctly, Not Correct Result", ((Number)resultItem).intValue() > 1);
        resultItem = ((Object[])innerResult)[1];
        assertTrue("Failed to return Employees correctly, Not An Employee", Employee.class.isAssignableFrom(resultItem.getClass()));
        resultItem = ((Object[])innerResult)[2];
        assertTrue("Failed to return Employees correctly, Not a City", String.class.isAssignableFrom(resultItem.getClass()));
    }
    
    public void testInheritanceMultiTableException(){
        try{
            ReportQuery reportQuery = new ReportQuery();
            reportQuery.returnWithoutReportQueryResult();
            reportQuery.setReferenceClass(Project.class);
            ExpressionBuilder empbuilder = new ExpressionBuilder();
            reportQuery.addAttribute("project",empbuilder);
            List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        }catch (QueryException ex){
           return; 
        }
        fail("Failed to throw exception, ReportItems must not have multi-table inheritance.");
    }
    
    public void testReturnRootObject(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(LargeProject.class);
        ExpressionBuilder empbuilder = new ExpressionBuilder();
        reportQuery.addAttribute("project",empbuilder);
        List result = (List)((EntityManagerImpl)createEntityManager()).getActiveSession().executeQuery(reportQuery);
        Object resultItem = result.get(0);
        assertTrue("Failed to return Project as expression root correctly, Not A Project", LargeProject.class.isAssignableFrom(resultItem.getClass()));
    }
    
    public static Test suite() {
        return new TestSuite(ReportQueryMultipleReturnTestSuite.class) {
        
            protected void setUp(){               
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    

}
