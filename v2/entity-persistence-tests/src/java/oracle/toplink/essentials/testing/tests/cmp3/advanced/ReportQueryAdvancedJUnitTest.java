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

import java.util.Vector;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.queryframework.DeleteAllQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.ReportQueryResult;
import oracle.toplink.essentials.queryframework.UpdateAllQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.UnitOfWork;

import oracle.toplink.essentials.testing.models.cmp3.advanced.*;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ReportQueryAdvancedJUnitTest  extends JUnitTestCase {

    static protected Class[] classes = {Employee.class, Address.class, PhoneNumber.class, Project.class};
    static protected Vector[] objectVectors = {null, null, null, null};
    
    static protected EmployeePopulator populator = new EmployeePopulator();

    public ReportQueryAdvancedJUnitTest() {
        super();
    }
    
    public ReportQueryAdvancedJUnitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
        if(!compare()) {
            clear();
            populate();
        }
    }
    
    protected static DatabaseSession getDbSession() {
        return getServerSession();   
    }
    
    protected static UnitOfWork acquireUnitOfWork() {
        return getDbSession().acquireUnitOfWork();   
    }
    
    protected static void clear() {
        UnitOfWork uow = acquireUnitOfWork();

        UpdateAllQuery updateEmployees = new UpdateAllQuery(Employee.class);
        updateEmployees.addUpdate("manager", null);
        updateEmployees.addUpdate("address", null);
        uow.executeQuery(updateEmployees);
    
        UpdateAllQuery updateProjects = new UpdateAllQuery(Project.class);
        updateProjects.addUpdate("teamLeader", null);
        uow.executeQuery(updateProjects);
    
        uow.executeQuery(new DeleteAllQuery(PhoneNumber.class));
        uow.executeQuery(new DeleteAllQuery(Address.class));
        uow.executeQuery(new DeleteAllQuery(Employee.class));
        uow.executeQuery(new DeleteAllQuery(Project.class));

        uow.commit();
        clearCache();
    }
    
    protected static void populate() {
        populator.buildExamples();
        populator.persistExample(getDbSession());
        clearCache();
        for(int i=0; i < classes.length; i++) {
            objectVectors[i] = getDbSession().readAllObjects(classes[i]);
        }
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ReportQueryAdvancedJUnitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){               
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testPhoneCountGroupByOwner() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(PhoneNumber.class, builder);
        Expression groupingExp = builder.get("owner");
        reportQuery.addItem("owner", groupingExp);
        reportQuery.addItem("phonesCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);
        
        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Employee employee = (Employee)reportResult.get("owner");
            int count = ((Number)reportResult.get("phonesCount")).intValue();
            if(employee.getPhoneNumbers().size() != count) {
                fail(employee.toString() + " has " + employee.getPhoneNumbers().size() + " phones, ReportQuery returned " + count);
            }
        }
    }

    public void testPhoneCountGroupByOwnersAddress() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(PhoneNumber.class, builder);
        Expression groupingExp = builder.get("owner").get("address");
        reportQuery.addItem("ownerAddress", groupingExp);
        reportQuery.addItem("phonesCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Address address = (Address)reportResult.get("ownerAddress");
            Employee employee = (Employee)getDbSession().readObject(Employee.class, (new ExpressionBuilder()).get("address").equal(address));
            int count = ((Number)reportResult.get("phonesCount")).intValue();
            if(employee.getPhoneNumbers().size() != count) {
                fail(employee.toString() + " has " + employee.getPhoneNumbers().size() + " phones, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMembers() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers");
        reportQuery.addItem("projectTeamMember", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Employee employee = (Employee)reportResult.get("projectTeamMember");
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMemberAddress() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers").get("address");
        reportQuery.addItem("projectTeamMemberAddress", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Address address = (Address)reportResult.get("projectTeamMemberAddress");
            Employee employee = (Employee)getDbSession().readObject(Employee.class, (new ExpressionBuilder()).get("address").equal(address));
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMemberPhone() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers").anyOf("phoneNumbers");
        reportQuery.addItem("projectTeamMemberPhone", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            PhoneNumber phone = (PhoneNumber)reportResult.get("projectTeamMemberPhone");
            Employee employee = phone.getOwner();
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    protected static boolean compare() {
        for(int i=0; i < classes.length; i++) {
            if(!compare(i)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean compare(int i) {
        if(objectVectors[i] == null) {
            return false;
        }
        Vector currentVector = getDbSession().readAllObjects(classes[i]);
        if(currentVector.size() != objectVectors[i].size()) {
            return false;
        }
        ClassDescriptor descriptor = getDbSession().getDescriptor(classes[i]);
        for(int j=0; j < currentVector.size(); j++) {
            Object obj1 = objectVectors[i].elementAt(j);
            Object obj2 = currentVector.elementAt(j);
            if(!descriptor.getObjectBuilder().compareObjects(obj1, obj2, (oracle.toplink.essentials.internal.sessions.AbstractSession)getDbSession())) {
                return false;
            }
        }
        return true;
    }
}
