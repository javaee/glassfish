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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.persistence.Query;

import junit.extensions.TestSetup;
import junit.framework.*;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.queryframework.DeleteAllQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.UpdateAllQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.framework.JoinedAttributeTestHelper;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.*;
import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;
 
public class JoinedAttributeAdvancedJunitTest extends JUnitTestCase {
        
    static protected Class[] classes = {Employee.class, Address.class, PhoneNumber.class, Project.class};
    static protected Vector[] objectVectors = {null, null, null, null};
    
    static protected EmployeePopulator populator = new EmployeePopulator();
    protected DatabaseSession dbSession;

    public JoinedAttributeAdvancedJunitTest() {
        super();
    }
    
    public JoinedAttributeAdvancedJunitTest(String name) {
        super(name);
    }
    
    // This method is designed to make sure that the tests always work in the same environment:
    // db has all the objects produced by populate method - and no other objects of the relevant classes.
    // In order to enforce that the first test populates the db and caches the objects in static collections,
    // the following test reads all the objects from the db, compares them with the cached ones - if they are the
    // same (the case if the tests run directly one after another) then no population occurs.
    public void setUp() {
        super.setUp();
        dbSessionClearCache();
        if(!compare()) {
            clear();
            populate();
        }
        dbSessionClearCache();
    }
    
    // the session is cached to avoid extra dealing with SessionManager - 
    // without session caching each test caused at least 5 ClientSessins being acquired / released.
    protected DatabaseSession getDbSession() {
        if(dbSession == null) {
            dbSession = getServerSession();
        }
        return dbSession;
    }
    
    protected UnitOfWork acquireUnitOfWork() {
        return getDbSession().acquireUnitOfWork();   
    }
    
    protected void clear() {
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
        dbSessionClearCache();
    }
    
    protected void populate() {
        populator.buildExamples();
        populator.persistExample(getDbSession());
        dbSessionClearCache();
        for(int i=0; i < classes.length; i++) {
            objectVectors[i] = getDbSession().readAllObjects(classes[i]);
        }
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JoinedAttributeAdvancedJunitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){               
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
            }
        };
    }
    
    public void tearDown() {
        dbSessionClearCache();
        dbSession = null;
        super.tearDown();
    }
    //BUG 3324: "Select E from Emp E join E.managedEmps join fetch E.addr" TopLink fails to process the fetch join
     public void testComplexMultipleFetchJoin() {
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        
        // find an employee w/ and w/o manager
        ReadObjectQuery nullManagerQuery = new ReadObjectQuery(Employee.class);
        nullManagerQuery.setSelectionCriteria(nullManagerQuery.getExpressionBuilder().get("manager").isNull());
        Employee empWithOutManager = (Employee)em.getActiveSession().executeQuery(nullManagerQuery);
        empWithOutManager.getAddress();
        em.clear();
        
        ReadObjectQuery empWithManagerQuery = new ReadObjectQuery(Employee.class);
        empWithManagerQuery.setSelectionCriteria(empWithManagerQuery.getExpressionBuilder().get("manager").notNull());
        Employee empWithManager = (Employee)em.getActiveSession().executeQuery(empWithManagerQuery);
        empWithManager.getAddress();

        em.clear();
        // Select the  empWithOutManager and empWithManager employees
        // This should only return empWithManager, because the manager is queried using an inner join
        String ejbqlString = "SELECT emp FROM Employee emp JOIN emp.manager m JOIN FETCH emp.address WHERE emp.id IN (:id1, :id2)";
        Query query = em.createQuery(ejbqlString);
        query.setParameter("id1", empWithOutManager.getId());
        query.setParameter("id2", empWithManager.getId());
        List result = (List) query.getResultList();
        List expectedResult = Arrays.asList(new Employee[] {empWithManager});
        String errorMsg1 = JoinedAttributeTestHelper.compareCollections(expectedResult, result, em.getActiveSession().getDescriptor(Employee.class), (AbstractSession)getDbSession());
        if(errorMsg1.length() > 0) {
            fail(errorMsg1);
        }
        em.clear();
        /// Select the  empWithOutManager and empWithManager employees
        // This should both, because the manager is queried using an outer join
        ejbqlString = "SELECT emp FROM Employee emp LEFT OUTER JOIN emp.manager m JOIN FETCH emp.address WHERE emp.id IN (:id1, :id2)";
        query = em.createQuery(ejbqlString);
        query.setParameter("id1", empWithOutManager.getId());
        query.setParameter("id2", empWithManager.getId());
        result = (List) query.getResultList();
        expectedResult = Arrays.asList(new Employee[] {empWithManager, empWithOutManager});
        String errorMsg2 = JoinedAttributeTestHelper.compareCollections(expectedResult, result, em.getActiveSession().getDescriptor(Employee.class), (AbstractSession)getDbSession());
        if(errorMsg2.length() > 0) {
            fail(errorMsg2);
        }
    }
        
/*    public void testProjectJoinTeamMembers() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        ArrayList joins = new ArrayList();
        joins.add(query.getExpressionBuilder().anyOf("teamMembers"));
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamLeaderJoinAddressWhereTeamLeaderNotNull() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        Expression teamLeader = query.getExpressionBuilder().get("teamLeader");
        query.setSelectionCriteria(teamLeader.notNull());
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        ArrayList joins = new ArrayList();
        joins.add(teamLeader);
        Expression teamLeaderAddress = teamLeader.get("address");
        joins.add(teamLeaderAddress);
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamMembersJoinAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        joins.add(teamMembersAddress);
        
        query.addItem("proejct", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamMembersJoinAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        joins.add(teamMembersAddress);
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamMembersOuterJoinAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        joins.add(teamMembersAddress);
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamMembersOuterJoinAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        joins.add(teamMembersAddress);
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProblemReporterProjectJoinTeamMembersJoinAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("name").equal("Problem Reporter"));
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        joins.add(teamMembersAddress);
        
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testEmployeeJoinProjects() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        ArrayList list = new ArrayList();
        list.add(query.getExpressionBuilder().anyOf("projects"));

        query.addItem("employee", query.getExpressionBuilder(), list);
        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testEmployeeJoinProjectsJoinTeamLeaderJoinAddressWhereManagerIsNull() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("manager").isNull());
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        ArrayList joins = new ArrayList();
        Expression projects = query.getExpressionBuilder().anyOf("projects");
        joins.add(projects);
        Expression teamLeader = projects.get("teamLeader");
        joins.add(teamLeader);
        Expression teamLeaderAddress = teamLeader.get("address");
        joins.add(teamLeaderAddress);
        query.addItem("employee", query.getExpressionBuilder(), joins);
        
        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamLeaderAddressTeamMembersAddressPhonesWhereProjectName() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Project.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("name").equal("Problem Reporting System").
            or(query.getExpressionBuilder().get("name").equal("Bleep Blob")));
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        ArrayList joins = new ArrayList();
        Expression teamLeader = query.getExpressionBuilder().getAllowingNull("teamLeader");
        joins.add(teamLeader);
        Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
        joins.add(teamLeaderAddress);
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        joins.add(teamMembersAddress);
        Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
        joins.add(teamMembersPhones);
        query.addItem("project", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testEmployeeOuterJoinAddressPhoneProjectsTeamLeaderAddressTeamMembersPhones() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);
        
        ReportQuery controlQuery = (ReportQuery)query.clone();
        
        // Note that without the following two lines address and phones are not read not for all Employees:
        // once an Employee is built (without Address and Phones)
        // it's not going to be rebuilt (get Address and Phones) when it's
        // up again either as a teamLeader or teamMember.
        // That means that only Employees read first indirectly (either as teamLeaders or
        // teamMembers would've got Phones and Addresses).
        ArrayList joins = new ArrayList();
        joins.add(query.getExpressionBuilder().getAllowingNull("address"));
        joins.add(query.getExpressionBuilder().anyOfAllowingNone("phoneNumbers"));
        
        Expression projects = query.getExpressionBuilder().anyOfAllowingNone("projects");
        joins.add(projects);
        Expression teamLeader = projects.getAllowingNull("teamLeader");
        joins.add(teamLeader);
        Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
        joins.add(teamLeaderAddress);
        Expression teamMembers = projects.anyOfAllowingNone("teamMembers");
        joins.add(teamMembers);
        Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
        joins.add(teamMembersPhones);
        query.addItem("employee", query.getExpressionBuilder(), joins);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
*/
    public void testProjectJoinTeamMembers() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(query.getExpressionBuilder().anyOf("teamMembers"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamLeaderJoinAddressWhereTeamLeaderNotNull() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        Expression teamLeader = query.getExpressionBuilder().get("teamLeader");
        query.setSelectionCriteria(teamLeader.notNull());
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(teamLeader);
        Expression teamLeaderAddress = teamLeader.get("address");
        query.addJoinedAttribute(teamLeaderAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamMembersJoinAddress() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        query.addJoinedAttribute(teamMembersAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamMembersJoinAddress() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        query.addJoinedAttribute(teamMembersAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamMembersOuterJoinAddress() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        query.addJoinedAttribute(teamMembersAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectJoinTeamMembersOuterJoinAddress() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        query.addJoinedAttribute(teamMembersAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProblemReporterProjectJoinTeamMembersJoinAddress() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("name").equal("Problem Reporter"));
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamMembers = query.getExpressionBuilder().anyOf("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.get("address");
        query.addJoinedAttribute(teamMembersAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testEmployeeJoinProjects() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Employee.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        query.addJoinedAttribute(query.getExpressionBuilder().anyOf("projects"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    //test fetch join works in a uow as well as the session.
    public void testEmployeeJoinProjectsOnUOW() {
        ReadAllQuery controlQuery = new ReadAllQuery();
        controlQuery.setReferenceClass(Employee.class);
        
        ReadAllQuery queryWithJoins = (ReadAllQuery)controlQuery.clone();
        queryWithJoins.addJoinedAttribute(queryWithJoins.getExpressionBuilder().anyOf("projects"));
        
        UnitOfWork uow = acquireUnitOfWork();
        //loads objects into cache without using joins
        uow.executeQuery(controlQuery);
        ((UnitOfWorkImpl)uow).setShouldCascadeCloneToJoinedRelationship(true);
        Collection results = (Collection)uow.executeQuery(queryWithJoins);
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        Collection controlledResults = (Collection)getDbSession().executeQuery(queryWithJoins);
        String errorMsg = JoinedAttributeTestHelper.compareCollections(controlledResults, results, controlQuery.getDescriptor(), (AbstractSession)getDbSession());
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }    
    
    public void testEmployeeJoinProjectsJoinTeamLeaderJoinAddressWhereManagerIsNull() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Employee.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("manager").isNull());
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        Expression projects = query.getExpressionBuilder().anyOf("projects");
        query.addJoinedAttribute(projects);
        Expression teamLeader = projects.get("teamLeader");
        query.addJoinedAttribute(teamLeader);
        Expression teamLeaderAddress = teamLeader.get("address");
        query.addJoinedAttribute(teamLeaderAddress);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testProjectOuterJoinTeamLeaderAddressTeamMembersAddressPhonesWhereProjectName() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Project.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("name").equal("Problem Reporting System").
            or(query.getExpressionBuilder().get("name").equal("Bleep Blob")));
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        Expression teamLeader = query.getExpressionBuilder().getAllowingNull("teamLeader");
        query.addJoinedAttribute(teamLeader);
        Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
        query.addJoinedAttribute(teamLeaderAddress);
        Expression teamMembers = query.getExpressionBuilder().anyOfAllowingNone("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersAddress = teamMembers.getAllowingNull("address");
        query.addJoinedAttribute(teamMembersAddress);
        Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
        query.addJoinedAttribute(teamMembersPhones);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testEmployeeOuterJoinAddressPhoneProjectsTeamLeaderAddressTeamMembersPhones() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Employee.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        // Note that without the following two lines address and phones are not read not for all Employees:
        // once an Employee is built (without Address and Phones)
        // it's not going to be rebuilt (get Address and Phones) when it's
        // up again either as a teamLeader or teamMember.
        // That means that only Employees read first indirectly (either as teamLeaders or
        // teamMembers would've got Phones and Addresses).
        query.addJoinedAttribute(query.getExpressionBuilder().getAllowingNull("address"));
        query.addJoinedAttribute(query.getExpressionBuilder().anyOfAllowingNone("phoneNumbers"));
        
        Expression projects = query.getExpressionBuilder().anyOfAllowingNone("projects");
        query.addJoinedAttribute(projects);
        Expression teamLeader = projects.getAllowingNull("teamLeader");
        query.addJoinedAttribute(teamLeader);
        Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
        query.addJoinedAttribute(teamLeaderAddress);
        Expression teamMembers = projects.anyOfAllowingNone("teamMembers");
        query.addJoinedAttribute(teamMembers);
        Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
        query.addJoinedAttribute(teamMembersPhones);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    //test fetch join works in a uow as well as the session.
    public void testEmployeeOuterJoinAddressPhoneProjectsTeamLeaderAddressTeamMembersPhonesOnUOW() {
        ReadAllQuery controlQuery = new ReadAllQuery();
        controlQuery.setReferenceClass(Employee.class);
        
        ReadAllQuery queryWithJoins = (ReadAllQuery)controlQuery.clone();
        
        // Note that without the following two lines address and phones are not read not for all Employees:
        // once an Employee is built (without Address and Phones)
        // it's not going to be rebuilt (get Address and Phones) when it's
        // up again either as a teamLeader or teamMember.
        // That means that only Employees read first indirectly (either as teamLeaders or
        // teamMembers would've got Phones and Addresses).
        queryWithJoins.addJoinedAttribute(queryWithJoins.getExpressionBuilder().getAllowingNull("address"));
        queryWithJoins.addJoinedAttribute(queryWithJoins.getExpressionBuilder().anyOfAllowingNone("phoneNumbers"));
        
        Expression projects = queryWithJoins.getExpressionBuilder().anyOfAllowingNone("projects");
        queryWithJoins.addJoinedAttribute(projects);
        Expression teamLeader = projects.getAllowingNull("teamLeader");
        queryWithJoins.addJoinedAttribute(teamLeader);
        Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
        queryWithJoins.addJoinedAttribute(teamLeaderAddress);
        Expression teamMembers = projects.anyOfAllowingNone("teamMembers");
        queryWithJoins.addJoinedAttribute(teamMembers);
        Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
        queryWithJoins.addJoinedAttribute(teamMembersPhones);

        UnitOfWork uow = acquireUnitOfWork();
        //loads objects into cache without using joins
        uow.executeQuery(controlQuery);
        ((UnitOfWorkImpl)uow).setShouldCascadeCloneToJoinedRelationship(true);
        Collection results = (Collection)uow.executeQuery(queryWithJoins);
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        Collection controlledResults = (Collection)JoinedAttributeTestHelper.getControlResultsFromControlQuery(controlQuery, queryWithJoins, (AbstractSession)uow);
        
        String errorMsg = JoinedAttributeTestHelper.compareCollections(controlledResults, results, controlQuery.getDescriptor(), (AbstractSession)getDbSession());
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

public void testEmployeeJoinManagerAddressOuterJoinManagerAddress() {
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("lastName").equal("Way").
                                    or(query.getExpressionBuilder().get("lastName").equal("Jones")));
        
        List list = new ArrayList();
        ReportQuery controlQuery = (ReportQuery)query.clone();
        Expression manager = query.getExpressionBuilder().get("manager");
        list.add(manager);
        Expression managersAddress = manager.get("address");
        list.add(managersAddress);
        Expression managersManager = manager.getAllowingNull("manager");
        list.add(managersManager);
        Expression managersManagerAddress = managersManager.get("address");
        list.add(managersManagerAddress);
        query.addItem("employee", query.getExpressionBuilder(), list);

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testTwoUnrelatedResultWithOneToManyJoins() {
        ReadAllQuery raq = new ReadAllQuery(Employee.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("lastName").equal("Way").or(raq.getExpressionBuilder().get("lastName").equal("Jones")));
        Employee emp = (Employee)((Vector)getDbSession().executeQuery(raq)).firstElement();
        emp.getPhoneNumbers();
        for (Iterator iterator = emp.getPhoneNumbers().iterator(); iterator.hasNext();){
            ((PhoneNumber)iterator.next()).getOwner();
        }
        
        raq = new ReadAllQuery(Address.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("city").like("%ttawa%"));
        Address addr = (Address)((Vector)getDbSession().executeQuery(raq)).firstElement();
        addr.getEmployees();
        for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
            ((Employee)iterator.next()).getAddress();
        }
        
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);

        ExpressionBuilder eb = new ExpressionBuilder(Address.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(emp.getId()).and(eb.get("id").equal(addr.getId())));
        
        List list = new ArrayList();
        list.add(query.getExpressionBuilder().anyOf("phoneNumbers"));
        query.addItem("employee", query.getExpressionBuilder(), list);

        list = new ArrayList();
        list.add(eb.anyOf("employees"));
        query.addItem("address", eb, list);
        
        Vector result = (Vector)getDbSession().executeQuery(query);
        
        DeleteAllQuery deleteAll = new DeleteAllQuery(PhoneNumber.class);
        deleteAll.setSelectionCriteria(deleteAll.getExpressionBuilder().get("owner").get("id").equal(emp.getId()));
        UnitOfWork uow = getDbSession().acquireUnitOfWork();
        uow.executeQuery(deleteAll);

        UpdateAllQuery updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("address").get("id").equal(addr.getId()));
        uow.executeQuery(updall);
        
        uow.commit();
        
        try{
            Employee emp2 = (Employee)((Object[])result.firstElement())[0];
            Address addr2 = (Address)((Object[])result.firstElement())[1];
    
            assertTrue("PhoneNumbers were not joined correctly, emp.getPhoneNumbers().size = " + emp.getPhoneNumbers().size() + " emp2.getPhoneNumbers().size = " + emp2.getPhoneNumbers().size(), (emp.getPhoneNumbers().size() == emp2.getPhoneNumbers().size()));
            assertTrue("Employees were not joined correctly, addr.employees.size = " + addr.getEmployees().size() + "addr2.employees.size = " + addr2.getEmployees().size(), (addr.getEmployees().size() == addr2.getEmployees().size()));
        }finally{
            
            getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
            uow = getDbSession().acquireUnitOfWork();
            Address addrClone = (Address)uow.readObject(addr);
            for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
                Employee empClone = (Employee)uow.readObject(iterator.next());
                empClone.setAddress(addrClone);
                addrClone.getEmployees().add(empClone);
            }
            Employee empClone = (Employee)uow.readObject(emp);
            for (Iterator iter = emp.getPhoneNumbers().iterator(); iter.hasNext();){
                empClone.addPhoneNumber((PhoneNumber)iter.next());
            }
            uow.commit();
        }

    }
    
    public void testMultipleUnrelatedResultWithOneToManyJoins() {
        ReadAllQuery raq = new ReadAllQuery(Employee.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().notEmpty("phoneNumbers"));
        Employee emp = (Employee)((Vector)getDbSession().executeQuery(raq)).firstElement();
        emp.getPhoneNumbers();
        for (Iterator iterator = emp.getPhoneNumbers().iterator(); iterator.hasNext();){
            ((PhoneNumber)iterator.next()).getOwner();
        }
        
        raq = new ReadAllQuery(Address.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("city").like("%ttawa%"));
        Address addr = (Address)((Vector)getDbSession().executeQuery(raq)).firstElement();
        addr.getEmployees();
        for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
            Employee addrEmp = (Employee)iterator.next();
            addrEmp.getAddress();
            addrEmp.getPhoneNumbers().size(); // as the report query will join in all phones to all emps, make sure we can compare.
        }
        
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Address.class);
        
        ExpressionBuilder eb = new ExpressionBuilder(Employee.class);

        List list = new ArrayList();
        list.add(eb.anyOf("phoneNumbers"));
        query.addItem("employee", eb, list);

        list = new ArrayList();
        list.add(query.getExpressionBuilder().anyOf("employees"));
        query.addItem("address", query.getExpressionBuilder(), list);

        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(addr.getId()));
        
        
        Vector result = (Vector)getDbSession().executeQuery(query);
        
        DeleteAllQuery deleteAll = new DeleteAllQuery(PhoneNumber.class);
        deleteAll.setSelectionCriteria(deleteAll.getExpressionBuilder().get("owner").get("id").equal(emp.getId()));
        UnitOfWork uow = getDbSession().acquireUnitOfWork();
        uow.executeQuery(deleteAll);

        UpdateAllQuery updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("address").get("id").equal(addr.getId()));
        uow.executeQuery(updall);
        
        uow.commit();
        
        try{
            Employee emp2 = null;
            Address addr2 = null;
            for (Iterator iterator = result.iterator(); iterator.hasNext();){
                Object [] items = (Object[])iterator.next();
                emp2 = (Employee)items[0];
                if (emp2.getId().equals(emp.getId())){
                    addr2 = (Address)items[1];
                    break;
                }
            }
            assertTrue("PhoneNumbers were not joined correctly, emp.getPhoneNumbers().size = " + emp.getPhoneNumbers().size() + " emp2.getPhoneNumbers().size = " + emp2.getPhoneNumbers().size(), (emp.getPhoneNumbers().size() == emp2.getPhoneNumbers().size()));
            assertTrue("Employees were not joined correctly, addr.employees.size = " + addr.getEmployees().size() + "addr2.employees.size = " + addr2.getEmployees().size(), (addr.getEmployees().size() == addr2.getEmployees().size()));
        }finally{
            
            getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
            uow = getDbSession().acquireUnitOfWork();
            Address addrClone = (Address)uow.readObject(addr);
            for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
                Employee empClone = (Employee)uow.readObject(iterator.next());
                empClone.setAddress(addrClone);
                addrClone.getEmployees().add(empClone);
            }
            Employee empClone = (Employee)uow.readObject(emp);
            for (Iterator iter = emp.getPhoneNumbers().iterator(); iter.hasNext();){
                empClone.addPhoneNumber((PhoneNumber)iter.next());
            }
            uow.commit();
        }

    }
    
    public void testTwoUnrelatedResultWithOneToOneJoins() {
        ReadAllQuery raq = new ReadAllQuery(Employee.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("lastName").equal("Way").or(raq.getExpressionBuilder().get("lastName").equal("Jones")));
        Employee emp = (Employee)((Vector)getDbSession().executeQuery(raq)).firstElement();
        emp.getAddress();
        
        raq = new ReadAllQuery(Address.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("city").like("%ttawa%"));
        Address addr = (Address)((Vector)getDbSession().executeQuery(raq)).firstElement();
        addr.getEmployees();
        for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
            ((Employee)iterator.next()).getAddress();
        }
        
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);

        ExpressionBuilder eb = new ExpressionBuilder(Address.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(emp.getId()).and(eb.get("id").equal(addr.getId())));
        
        List list = new ArrayList();
        list.add(query.getExpressionBuilder().get("address"));
        query.addItem("employee", query.getExpressionBuilder(), list);

        list = new ArrayList();
        list.add(eb.anyOf("employees"));
        query.addItem("address", eb, list);
        
        Vector result = (Vector)getDbSession().executeQuery(query);
        
        UpdateAllQuery updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("id").equal(emp.getId()));
        UnitOfWork uow = getDbSession().acquireUnitOfWork();
        uow.executeQuery(updall);

        updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("address").get("id").equal(addr.getId()));
        uow.executeQuery(updall);
        
        uow.commit();
        
        try{
            Employee emp2 = (Employee)((Object[])result.firstElement())[0];
            Address addr2 = (Address)((Object[])result.firstElement())[1];

            assertTrue("Address were not joined correctly, emp.getAddress() = null", (emp2.getAddress() != null));
            assertTrue("Employees were not joined correctly, addr.employees.size = " + addr.getEmployees().size() + "addr2.employees.size = " + addr2.getEmployees().size(), (addr.getEmployees().size() == addr2.getEmployees().size()));

            }finally{
                
  /*              getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
                uow = getDbSession().acquireUnitOfWork();
                emp.setVersion(emp.getVersion() + 1);
                Address addrClone = (Address)uow.readObject(addr);
                for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
                    Employee empClone = (Employee)uow.readObject(iterator.next());
                    empClone.setAddress(addrClone);
                    addrClone.getEmployees().add(empClone);
                }
                Employee empClone = (Employee)uow.readObject(emp);
                empClone.setAddress((Address)uow.readObject(emp.getAddress()));
                uow.commit();
    */        }

    }
    
    public void testTwoUnrelatedResultWithOneToOneJoinsWithExtraItem() {
        ReadAllQuery raq = new ReadAllQuery(Employee.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("lastName").equal("Way").or(raq.getExpressionBuilder().get("lastName").equal("Jones")));
        Employee emp = (Employee)((Vector)getDbSession().executeQuery(raq)).firstElement();
        emp.getAddress();
        
        raq = new ReadAllQuery(Address.class);
        raq.setSelectionCriteria(raq.getExpressionBuilder().get("city").like("%ttawa%"));
        Address addr = (Address)((Vector)getDbSession().executeQuery(raq)).firstElement();
        addr.getEmployees();
        for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
            ((Employee)iterator.next()).getAddress();
        }
        
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        
        ReportQuery query = new ReportQuery();
        query.setShouldReturnWithoutReportQueryResult(true);
        query.setReferenceClass(Employee.class);

        ExpressionBuilder eb = new ExpressionBuilder(Address.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(emp.getId()).and(eb.get("id").equal(addr.getId())));
        
        List list = new ArrayList();
        list.add(query.getExpressionBuilder().get("address"));
        query.addItem("employee", query.getExpressionBuilder(), list);
        query.addItem("employee_name", query.getExpressionBuilder().get("firstName"));

        list = new ArrayList();
        list.add(eb.anyOf("employees"));
        query.addItem("address", eb, list);
        
        Vector result = (Vector)getDbSession().executeQuery(query);
        
        UpdateAllQuery updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("id").equal(emp.getId()));
        UnitOfWork uow = getDbSession().acquireUnitOfWork();
        uow.executeQuery(updall);

        updall = new UpdateAllQuery(Employee.class);
        updall.addUpdate("address", null);
        updall.setSelectionCriteria(updall.getExpressionBuilder().get("address").get("id").equal(addr.getId()));
        uow.executeQuery(updall);
        
        uow.commit();
        
        Employee emp2 = (Employee)((Object[])result.firstElement())[0];
        Address addr2 = (Address)((Object[])result.firstElement())[2];
        try{
            assertTrue("Address were not joined correctly, emp.getAddress() = null", (emp2.getAddress() != null));
            assertTrue("Employees were not joined correctly, addr.employees.size = " + addr.getEmployees().size() + "addr2.employees.size = " + addr2.getEmployees().size(), (addr.getEmployees().size() == addr2.getEmployees().size()));
            if (!emp2.getFirstName().equals(((Object[])result.firstElement())[1])){
                fail("Failed to return employee name as an seperate item");
            }
    
        }finally{
            
            getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
            uow = getDbSession().acquireUnitOfWork();
            emp.setVersion(emp.getVersion() + 1);
            Address addrClone = (Address)uow.readObject(addr);
            for (Iterator iterator = addr.getEmployees().iterator(); iterator.hasNext();){
                Employee empClone = (Employee)uow.readObject(iterator.next());
                empClone.setAddress(addrClone);
                addrClone.getEmployees().add(empClone);
            }
            Employee empClone = (Employee)uow.readObject(emp);
            empClone.setAddress((Address)uow.readObject(emp.getAddress()));
            uow.commit();
        }
    }

    protected String executeQueriesAndCompareResults(ObjectLevelReadQuery controlQuery, ObjectLevelReadQuery queryWithJoins) {
        return JoinedAttributeTestHelper.executeQueriesAndCompareResults(controlQuery, queryWithJoins, (AbstractSession)getDbSession());
    }

    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }

    protected boolean compare() {
        for(int i=0; i < classes.length; i++) {
            if(!compare(i)) {
                return false;
            }
        }
        return true;
    }

    protected boolean compare(int i) {
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

    public void dbSessionClearCache() {
        getDbSession().getIdentityMapAccessor().initializeAllIdentityMaps();
    }
}
