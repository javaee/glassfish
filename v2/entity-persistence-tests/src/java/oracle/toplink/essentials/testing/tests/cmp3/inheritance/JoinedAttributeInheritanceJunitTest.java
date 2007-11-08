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

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.queryframework.DeleteAllQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.testing.framework.JoinedAttributeTestHelper;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.*;
import oracle.toplink.essentials.queryframework.ReadAllQuery;
 
public class JoinedAttributeInheritanceJunitTest extends JUnitTestCase {
        
    static protected Class[] classes = {Company.class, Vehicle.class};
    static protected Vector[] objectVectors = {null, null};
    
    protected DatabaseSession dbSession;

    public JoinedAttributeInheritanceJunitTest() {
        super();
    }
    
    public JoinedAttributeInheritanceJunitTest(String name) {
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
        // delete all Vechicles
        uow.executeQuery(new DeleteAllQuery(Vehicle.class));
        // delete all Companies
        uow.executeQuery(new DeleteAllQuery(Company.class));
        uow.commit();
        dbSessionClearCache();
    }
    
    protected void populate() {
        UnitOfWork uow = acquireUnitOfWork();
        uow.registerNewObject(InheritanceModelExamples.companyExample1());
        uow.registerNewObject(InheritanceModelExamples.companyExample2());
        uow.registerNewObject(InheritanceModelExamples.companyExample3());
        uow.commit();
        dbSessionClearCache();
        for(int i=0; i < classes.length; i++) {
            objectVectors[i] = getDbSession().readAllObjects(classes[i]);
        }
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JoinedAttributeInheritanceJunitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){               
                new InheritanceTableCreator().replaceTables(JUnitTestCase.getServerSession());
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
        
    public void testVehicleJoinCompany() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Vehicle.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(query.getExpressionBuilder().get("owner"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

    public void testVehicleJoinCompanyWherePassengerCapacity() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Vehicle.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("passengerCapacity").greaterThan(2));
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(query.getExpressionBuilder().get("owner"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

    public void testCompanyJoinVehicles() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Company.class);
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(query.getExpressionBuilder().anyOf("vehicles"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

    public void testCompanyJoinVehiclesWhereNameTOP() {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(Company.class);
        query.setSelectionCriteria(query.getExpressionBuilder().get("name").equal("TOP"));
        
        ReadAllQuery controlQuery = (ReadAllQuery)query.clone();
        
        query.addJoinedAttribute(query.getExpressionBuilder().anyOf("vehicles"));

        String errorMsg = executeQueriesAndCompareResults(controlQuery, query);
        if(errorMsg.length() > 0) {
            fail(errorMsg);
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
