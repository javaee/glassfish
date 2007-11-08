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

import java.util.HashMap;
import java.util.Vector;

import junit.extensions.TestSetup;
import junit.framework.*;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.expressions.ExpressionMath;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.queryframework.DeleteAllQuery;
import oracle.toplink.essentials.queryframework.UpdateAllQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.*;
import oracle.toplink.essentials.testing.framework.UpdateAllQueryTestHelper;
import oracle.toplink.essentials.threetier.ServerSession;
 
public class UpdateAllQueryAdvancedJunitTest extends JUnitTestCase {
        
    static protected Class[] classes = {Employee.class, Address.class, PhoneNumber.class, Project.class};
    static protected Vector[] objectVectors = {null, null, null, null};
    
    static protected EmployeePopulator populator = new EmployeePopulator();

    public UpdateAllQueryAdvancedJunitTest() {
        super();
    }
    
    public UpdateAllQueryAdvancedJunitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        clearCache();
        super.setUp();
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
        TestSuite suite = new TestSuite(UpdateAllQueryAdvancedJunitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){               
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public static void testFirstNamePrefixBLAForAll() {
        ExpressionBuilder builder = new ExpressionBuilder();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);        
        updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));
        updateAllQueryInternal(updateQuery);
    }    
    
    //This test created for bug 3307, the prepared temporary table query should pickup the
    //defined column definition, for databaseplatform that not support temporary table will bypass the verification.
    public static void testOverriddenColumnDefinition() {
       Exception e = null;
       ServerSession session = getServerSession();
       ClassDescriptor employeeClassDescriptor = session.getProject().getDescriptor(Employee.class);
       
       //set column definition for field 'F_NAME'
       Vector fields= employeeClassDescriptor.getFields();
       DatabaseField originalEmployeeFirstNameDatabaseField = null;
       for(int i=0;i<fields.size();i++){
           if(((DatabaseField)fields.get(i)).getName().equals("F_NAME")){
              originalEmployeeFirstNameDatabaseField = (DatabaseField)fields.get(i);
           }
       }
       String originalColumnDefinitionForFirstName = originalEmployeeFirstNameDatabaseField.getColumnDefinition();
       originalEmployeeFirstNameDatabaseField.setColumnDefinition("DUMMY");
 
       //Execute updateAll query to ensure the temporary table being created.
       String preparedSQL="";
       try{
           UnitOfWork uow=session.acquireUnitOfWork();

            ExpressionBuilder builder = new ExpressionBuilder();
            UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);        
            updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));

            uow.executeQuery(updateQuery);
            uow.commit();
          
            preparedSQL = updateQuery.getSQLString();
       }catch(RuntimeException e1){
           //Database exception expected to be caught in here as the result of the execution of create temporary table query 
    	   //with invalid column definition "DUMMY" at the database platform that support temp storage, 
		   //it however does not happened for unknown issue, therefore, verify the test result by checking whether or 
		   //not the prepared sql contains "DUMMY" string.
       }finally{
          //Need reset to original column definition. 
          originalEmployeeFirstNameDatabaseField.setColumnDefinition(originalColumnDefinitionForFirstName);
       }
       
       //Verify, only platform that support temporary table need to be verified result.
       if(session.getPlatform().shouldAlwaysUseTempStorageForModifyAll()){
           Assert.assertTrue("Exception was not caught which supposed to be caught.", preparedSQL.indexOf("DUMMY")>0);
       }
    }    


    
    public static void testFirstNamePrefixBLAForSalary() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression selectionExpression = builder.get("salary").lessThan(20000);
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testDoubleSalaryForAll() {
        ExpressionBuilder builder = new ExpressionBuilder();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);
        updateQuery.addUpdate("salary", ExpressionMath.multiply(builder.get("salary"), new Integer(2)));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testDoubleSalaryForSalary() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression selectionExpression = builder.get("salary").lessThan(20000);
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("salary", ExpressionMath.multiply(builder.get("salary"), new Integer(2)));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testFirstNamePrefixBLADoubleSalaryForAll() {
        ExpressionBuilder builder = new ExpressionBuilder();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);
        updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));
        updateQuery.addUpdate("salary", ExpressionMath.multiply(builder.get("salary"), new Integer(2)));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testFirstNamePrefixBLADoubleSalaryForSalary() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression selectionExpression = builder.get("salary").lessThan(20000);
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));
        updateQuery.addUpdate("salary", ExpressionMath.multiply(builder.get("salary"), new Integer(2)));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testFirstNamePrefixBLADoubleSalaryForSalaryForFirstName() {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression selectionExpression = builder.get("salary").lessThan(20000).and(builder.get("firstName").like("J%"));
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("firstName", Expression.fromLiteral("'BLA'", null).concat(builder.get("firstName")));
        updateQuery.addUpdate("salary", ExpressionMath.multiply(builder.get("salary"), new Integer(2)));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testAssignManagerName() {
        ExpressionBuilder builder = new ExpressionBuilder();    
        Expression selectionExpression = builder.get("manager").notNull();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("firstName", builder.get("manager").get("firstName"));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testAssignNullToAddress() {
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);
        updateQuery.addUpdate("address", null);
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testAssignObjectToAddress() {
        Address address = new Address();
        address.setCountry("Canada");
        address.setProvince("Ontario");
        address.setCity("Ottawa");
        address.setStreet("O'Connor");
        UnitOfWork uow = acquireUnitOfWork();
        uow.registerNewObject(address);
        uow.commit();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);
        updateQuery.addUpdate("address", address);
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testAssignExpressionToAddress() {
        ExpressionBuilder builder = new ExpressionBuilder();    
        Expression selectionExpression = builder.get("manager").notNull();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate("address", builder.get("manager").get("address"));
        updateAllQueryInternal(updateQuery);
    }    
    
    public static void testAggregate() {
        ExpressionBuilder builder = new ExpressionBuilder();    
        Expression selectionExpression = builder.get("manager").notNull();
        UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class, selectionExpression);
        updateQuery.addUpdate(builder.get("period").get("startDate"), builder.get("period").get("endDate"));
        updateQuery.addUpdate(builder.get("period").get("endDate"), builder.get("period").get("startDate"));
        updateAllQueryInternal(updateQuery);
    }
    
    protected static void updateAllQueryInternal(Class referenceClass, HashMap updateClauses, Expression selectionExpression) {
        String errorMsg = UpdateAllQueryTestHelper.execute(getDbSession(), referenceClass, updateClauses, selectionExpression);
        if(errorMsg != null) {
            fail(errorMsg);
        }
    }
    
    protected static void updateAllQueryInternal(UpdateAllQuery uq) {
        String errorMsg = UpdateAllQueryTestHelper.execute(getDbSession(), uq);
        if(errorMsg != null) {
            fail(errorMsg);
        }
    }
    
    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
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
