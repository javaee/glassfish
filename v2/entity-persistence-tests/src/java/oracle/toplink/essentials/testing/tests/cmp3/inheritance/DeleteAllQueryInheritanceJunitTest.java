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


package oracle.toplink.essentials.testing.tests.cmp3.inheritance;

import java.util.Vector;

import junit.extensions.TestSetup;
import junit.framework.*;

import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.queryframework.DeleteAllQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.*;
import oracle.toplink.essentials.testing.framework.DeleteAllQueryTestHelper;
 
public class DeleteAllQueryInheritanceJunitTest extends JUnitTestCase {
        
    static Vector originalVehicleObjects;
    static Vector originalCompanyObjects;
    static ReportQuery reportQueryVehicles;
    static ReportQuery reportQueryCompanies; 
    {
        reportQueryVehicles = new ReportQuery(Vehicle.class, new ExpressionBuilder());
        reportQueryVehicles.setShouldRetrievePrimaryKeys(true);
        reportQueryCompanies = new ReportQuery(Company.class, new ExpressionBuilder());
        reportQueryCompanies.setShouldRetrievePrimaryKeys(true);
    }
    
    public DeleteAllQueryInheritanceJunitTest() {
        super();
    }
    
    public DeleteAllQueryInheritanceJunitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        Vector currentVehicleObjects = (Vector)getDbSession().executeQuery(reportQueryVehicles);
        Vector currentCompanyObjects = (Vector)getDbSession().executeQuery(reportQueryCompanies);
        if(!currentVehicleObjects.equals(originalVehicleObjects) || !currentCompanyObjects.equals(originalCompanyObjects)) {
            if(!currentVehicleObjects.isEmpty() || !currentCompanyObjects.isEmpty()) {
                clearVehiclesCompanies();
            }
            populateVehiclesCompanies();
            originalVehicleObjects = (Vector)getDbSession().executeQuery(reportQueryVehicles);
            originalCompanyObjects = (Vector)getDbSession().executeQuery(reportQueryCompanies);
        }
        clearCache();
    }
    
    protected static DatabaseSession getDbSession() {
        return getServerSession();   
    }
    
    protected static UnitOfWork acquireUnitOfWork() {
        return getDbSession().acquireUnitOfWork();   
    }
    
    protected static void clearVehiclesCompanies() {
        UnitOfWork uow = acquireUnitOfWork();
        // delete all Vechicles
        uow.executeQuery(new DeleteAllQuery(Vehicle.class));
        // delete all Companies
        uow.executeQuery(new DeleteAllQuery(Company.class));
        uow.commit();
        clearCache();
    }
    
    protected static void populateVehiclesCompanies() {
        UnitOfWork uow = acquireUnitOfWork();
        uow.registerNewObject(InheritanceModelExamples.companyExample1());
        uow.registerNewObject(InheritanceModelExamples.companyExample2());
        uow.registerNewObject(InheritanceModelExamples.companyExample3());
        uow.commit();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DeleteAllQueryInheritanceJunitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new InheritanceTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    // JUnit framework will automatically execute all methods starting with test...
    // The test methods' name pattern is a word "test" followed by underscore and the used selectionExpression:
    // test_selectionExpression
    
    // ALL Vehicles
    public static void test_null() {
        deleteAllQueryInternal_Deferred_Children(Vehicle.class, null);
    }
    
    // ALL Vehicles - nondeferred (execute deleteAllQuery immediately as opposed to during uow.commit)
    public static void test_nullNonDeferred() {
        deleteAllQueryInternal_NonDeferred_Children(Vehicle.class, null);
    }
    
    // Vehicles owned by TOP Company
    public static void test_ownerTOP() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp = builder.get("owner").get("name").equal("TOP");
        deleteAllQueryInternal_Deferred_Children(Vehicle.class, exp);
    }
    
    // FueledVehicles running on Petrol
    public static void test_fuelTypePetrol() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp = builder.get("fuelType").equalsIgnoreCase("Petrol");        
        deleteAllQueryInternal_Deferred_Children(FueledVehicle.class, exp);
    }
    
    // shchool buses without drivers
    public static void test_schoolBusNullDriver() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp = builder.get("description").equalsIgnoreCase("School bus").and(builder.get("busDriver").isNull()); 
        deleteAllQueryInternal_Deferred_Children(Bus.class, exp);
    }
    
    // FueledVehicles owned by Companies that also own NonFueledVehicles
    public static void test_ownerOwnsNonFueledVehicle() {
        ExpressionBuilder builder = new ExpressionBuilder();

        ExpressionBuilder subBuilder = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(NonFueledVehicle.class, subBuilder);
        rq.addAttribute("id");
        Expression subExpression = subBuilder.get("owner").equal(builder.get("owner"));
        rq.setSelectionCriteria(subExpression);

        Expression exp = builder.exists(rq);
        deleteAllQueryInternal_Deferred_Children(FueledVehicle.class, exp);
    }
    
    protected static void deleteAllQueryInternal_Deferred_Children(Class referenceClass, Expression selectionExpression) {
        deleteAllQueryInternal(referenceClass, selectionExpression, true, true);
    }
    
    protected static void deleteAllQueryInternal_NonDeferred_Children(Class referenceClass, Expression selectionExpression) {
        deleteAllQueryInternal(referenceClass, selectionExpression, false, true);
    }
    
    protected static void deleteAllQueryInternal_Deferred_NoChildren(Class referenceClass, Expression selectionExpression) {
        deleteAllQueryInternal(referenceClass, selectionExpression, true, false);
    }
    
    protected static void deleteAllQueryInternal_NonDeferred_NoChildren(Class referenceClass, Expression selectionExpression) {
        deleteAllQueryInternal(referenceClass, selectionExpression, false, false);
    }
    
    // referenceClass - the reference class of DeleteAllQuery to be tested
    // selectionExpression - selection expression of DeleteAllQuery to be tested
    // shouldDeferExecutionInUOW==true causes deferring query execution until uow.commit;
    // shouldDeferExecutionInUOW==false causes immediate query execution;
    // shouldHandleChildren==true means the test will be executed not only with the specified class,
    // but also with all its subclasses.
    // Each test will test DeleteAllQuery with the specified reference class
    // and all its subclasses
    // Example: for Vehicle.class  9 DeleteAllQueries will be tested.
    // shouldHandleChildren==false means the test will be executed with the specified class only.
    protected static void deleteAllQueryInternal(Class referenceClass, Expression selectionExpression, boolean shouldDeferExecutionInUOW, boolean handleChildren) {
        String errorMsg = DeleteAllQueryTestHelper.execute(getDbSession(), referenceClass, selectionExpression, shouldDeferExecutionInUOW, handleChildren);
        if(errorMsg != null) {
            fail(errorMsg);
        }
    }
    
    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
