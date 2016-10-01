/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.xml.test.dynamic.adds;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Employee;
import org.glassfish.hk2.xml.test.basic.beans.Employees;
import org.glassfish.hk2.xml.test.basic.beans.Financials;
import org.glassfish.hk2.xml.test.basic.beans.NamedBean;
import org.glassfish.hk2.xml.test.basic.beans.OtherData;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests adding the root and children
 * 
 * @author jwells
 *
 */
public class AddsTest {
    private final static String DAVE = "Dave";
    private final static String EMPLOYEE_TYPE = "/employees/employee";
    private final static String OTHER_DATA_TYPE = "/employees/other-data";
    private final static String DAVE_INSTANCE = "employees.Dave";
    
    private final static String ATT_SYMBOL = "ATT";
    private final static String NASDAQ = "Nasdaq";
    
    private final static long ALICE_ID = 12L;
    private final static long BOB_ID = 14L;
    private final static long CAROL_ID = 16L;
    
    private final static String DATA1 = "Spiner";
    private final static String DATA2 = "10100101";  // A5
    
    /**
     * Tests that we can call createAndAdd successfully on a root with no required elements
     */
    @Test
    // @org.junit.Ignore
    public void testCreateAndAdd() {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle = xmlService.createEmptyHandle(Employees.class);
        Assert.assertNull(rootHandle.getRoot());
        
        rootHandle.addRoot();
        Employees root = rootHandle.getRoot();
        
        Assert.assertNotNull(root);
        Assert.assertNull(root.getFinancials());
        Assert.assertEquals(0, root.getEmployees().size());
        Assert.assertNull(root.getCompanyName());
    }
    
    private void addToExistingTree(ServiceLocator locator, Hub hub, XmlRootHandle<Employees> rootHandle, boolean inRegistry, boolean inHub) {
        Employees employees = rootHandle.getRoot();
        
        employees.addEmployee(DAVE);
        employees.setAStringThatWillBeSetToNull(null);
        
        Employee daveDirect = employees.lookupEmployee(DAVE);
        Assert.assertNotNull(daveDirect);
        
        if (inRegistry) {
            Employee daveService = locator.getService(Employee.class, DAVE);
            Assert.assertNotNull(daveService);
        }
        else {
            Assert.assertNull(locator.getService(Employee.class, DAVE));
        }
        
        if (inHub) {
            Assert.assertNotNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, DAVE_INSTANCE));
        }
        else {
            Assert.assertNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, DAVE_INSTANCE));
        }
        
        if (inHub) {
            // Check the changes
            RecordingBeanUpdateListener listener = locator.getService(RecordingBeanUpdateListener.class);
            
            List<Change> allChanges = listener.latestCommit;
            Assert.assertNotNull(allChanges);
            Assert.assertEquals(2, allChanges.size());
            
            {
                Change change0 = allChanges.get(0);
                
                Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change0.getChangeCategory());
                
                Type changeType = change0.getChangeType();
                Assert.assertEquals("/employees/employee", changeType.getName());
                
                Assert.assertEquals("employees.Dave", change0.getInstanceKey());
                
                Assert.assertNotNull(change0.getInstanceValue());
                Assert.assertNull(change0.getOriginalInstanceValue());
                Assert.assertNull(change0.getModifiedProperties());
            }
            
            {
                Change change1 = allChanges.get(1);
                
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change1.getChangeCategory());
                
                Type changeType = change1.getChangeType();
                Assert.assertEquals("/employees", changeType.getName());
                
                Assert.assertEquals("employees", change1.getInstanceKey());
                
                Assert.assertNotNull(change1.getInstanceValue());
                Assert.assertNotNull(change1.getOriginalInstanceValue());
                
                List<PropertyChangeEvent> modifiedProperties = change1.getModifiedProperties();
                Assert.assertNotNull(modifiedProperties);
                Assert.assertEquals(1, modifiedProperties.size());
                
                Assert.assertEquals("employee", modifiedProperties.get(0).getPropertyName());
            }
        }
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add (no copy or overlay)
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTree() throws Exception {
        ServiceLocator locator = Utilities.createLocator(RecordingBeanUpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        
        addToExistingTree(locator, hub, rootHandle, true, true);
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add (no copy or overlay) not in Hub
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTreeNoHub() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class, true, false);
        
        addToExistingTree(locator, hub, rootHandle, true, false);
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add (no copy or overlay) not in ServiceLocator
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTreeNoHk2Service() throws Exception {
        ServiceLocator locator = Utilities.createLocator(RecordingBeanUpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class, false, true);
        
        addToExistingTree(locator, hub, rootHandle, false, true);
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add (no copy or overlay) not in ServiceLocator
     * or Hub
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTreeNoHk2ServiceOrHub() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class, false, false);
        
        addToExistingTree(locator, hub, rootHandle, false, false);
    }
    
    
    
    /**
     * Tests that we can add to an existing tree with just a basic add
     * with an unkeyed field
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTreeUnKeyed() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        employees.addOtherData(0);
        
        OtherData found = null;
        for (OtherData other : employees.getOtherData()) {
            Assert.assertNull(found);
            found = other;
        }
        
        Assert.assertNotNull(found);
        
        OtherData otherService = locator.getService(OtherData.class);
        Assert.assertNotNull(otherService);
        
        Assert.assertEquals(found, otherService);
        
        Type type = hub.getCurrentDatabase().getType(OTHER_DATA_TYPE);
        
        Instance foundInstance = null;
        for (Instance i : type.getInstances().values()) {
            Assert.assertNull(foundInstance);
            foundInstance = i;
        }
        
        Assert.assertNotNull(foundInstance);
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add
     * with an direct stanza
     */
    @Test
    // @org.junit.Ignore
    public void testAddToExistingTreeDirect() throws Exception {
        ServiceLocator locator = Utilities.createLocator(RecordingBeanUpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME2_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertNull(employees.getFinancials());
        
        employees.addFinancials();
        
        Financials financials = employees.getFinancials();
        
        Assert.assertNotNull(financials);
        Assert.assertNull(financials.getExchange());
        Assert.assertNull(financials.getSymbol());
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(Commons.FINANCIALS_TYPE, Commons.FINANCIALS_INSTANCE));
        
        RecordingBeanUpdateListener listener = locator.getService(RecordingBeanUpdateListener.class);
        
        List<Change> allChanges = listener.latestCommit;
        Assert.assertNotNull(allChanges);
        Assert.assertEquals(3, allChanges.size());
        
        {
            Change change0 = allChanges.get(0);
            
            Assert.assertEquals(ChangeCategory.ADD_TYPE, change0.getChangeCategory());
            
            Type changeType = change0.getChangeType();
            Assert.assertEquals("/employees/financials", changeType.getName());
            
            Assert.assertNull(change0.getInstanceKey());
            Assert.assertNull(change0.getInstanceValue());
            Assert.assertNull(change0.getOriginalInstanceValue());
            Assert.assertNull(change0.getModifiedProperties());
        }
        
        {
            Change change1 = allChanges.get(1);
            
            Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change1.getChangeCategory());
            
            Type changeType = change1.getChangeType();
            Assert.assertEquals("/employees/financials", changeType.getName());
            
            Assert.assertEquals("employees.financials", change1.getInstanceKey());
            
            Assert.assertNotNull(change1.getInstanceValue());
            Assert.assertNull(change1.getOriginalInstanceValue());
            Assert.assertNull(change1.getModifiedProperties());
        }
        
        {
            Change change2 = allChanges.get(2);
            
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change2.getChangeCategory());
            
            Type changeType = change2.getChangeType();
            Assert.assertEquals("/employees", changeType.getName());
            
            Assert.assertEquals("employees", change2.getInstanceKey());
            
            Assert.assertNotNull(change2.getInstanceValue());
            Assert.assertNotNull(change2.getOriginalInstanceValue());
            
            List<PropertyChangeEvent> modifiedProperties = change2.getModifiedProperties();
            Assert.assertNotNull(modifiedProperties);
            Assert.assertEquals(1, modifiedProperties.size());
            
            Assert.assertEquals("financials", modifiedProperties.get(0).getPropertyName());
            Assert.assertNull(modifiedProperties.get(0).getOldValue());
            Assert.assertNotNull(modifiedProperties.get(0).getNewValue());
        }
    }
    
    private static Employee createEmployee(XmlService xmlService, String name, long id) {
        Employee employee = xmlService.createBean(Employee.class);
        
        employee.setName(name);
        employee.setId(id);
        
        return employee;
    }
    
    private static OtherData createOtherData(XmlService xmlService, String data) {
        OtherData other = xmlService.createBean(OtherData.class);
        
        other.setData(data);
        
        return other;
    }
    
    private static void checkEmployee(Employee employee, String name, long id) {
        Assert.assertNotNull(employee);
        Assert.assertEquals(name, employee.getName());
        Assert.assertEquals(id, employee.getId());
    }
    
    private static void checkOtherData(OtherData other, String data) {
        Assert.assertNotNull(other);
        Assert.assertEquals(data, other.getData());
    }
    
    private static void checkFinancials(Financials fin, String exchange, String symbol) {
        Assert.assertNotNull(fin);
        Assert.assertEquals(exchange, fin.getExchange());
        Assert.assertEquals(symbol, fin.getSymbol());
    }

    /**
     * Creates an entire tree unassociated with a root then sets it as
     * the root
     */
    @Test
    // @org.junit.Ignore
    public void testAddOneLevelComplexRoot() {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle = xmlService.createEmptyHandle(Employees.class);
        Assert.assertNull(rootHandle.getRoot());
        
        Employees employees = xmlService.createBean(Employees.class);
        Financials financials = xmlService.createBean(Financials.class);
        
        financials.setExchange(NASDAQ);
        financials.setSymbol(ATT_SYMBOL);
        
        employees.setFinancials(financials);
        
        Employee alice = createEmployee(xmlService, Commons.ALICE, ALICE_ID);
        Employee bob = createEmployee(xmlService, Commons.BOB, BOB_ID);
        Employee carol = createEmployee(xmlService, Commons.CAROL, CAROL_ID);
        
        employees.addEmployee(alice);
        employees.addEmployee(carol);
        employees.addEmployee(bob, 1);
        
        OtherData data1 = createOtherData(xmlService, DATA1);
        OtherData data2 = createOtherData(xmlService, DATA2);
        
        employees.addOtherData(data2);
        employees.addOtherData(data1, 0);
        
        rootHandle.addRoot(employees);
        
        Employees root = rootHandle.getRoot();
        
        Assert.assertNotNull(root);
        
        checkFinancials(root.getFinancials(), NASDAQ, ATT_SYMBOL);
        
        checkEmployee(root.getEmployees().get(0), Commons.ALICE, ALICE_ID);
        checkEmployee(root.getEmployees().get(1), Commons.BOB, BOB_ID);
        checkEmployee(root.getEmployees().get(2), Commons.CAROL, CAROL_ID);
        
        checkOtherData(root.getOtherData().get(0), DATA1);
        checkOtherData(root.getOtherData().get(1), DATA2);
        
        checkEmployee(locator.getService(Employee.class, Commons.ALICE), Commons.ALICE, ALICE_ID);
        checkEmployee(locator.getService(Employee.class, Commons.BOB), Commons.BOB, BOB_ID);
        checkEmployee(locator.getService(Employee.class, Commons.CAROL), Commons.CAROL, CAROL_ID);
        
        int lcv = 0;
        for (OtherData other : locator.getAllServices(OtherData.class)) {
            if (lcv == 0) {
                checkOtherData(other, DATA1);
            }
            else if (lcv == 1){
                checkOtherData(other, DATA2);
            }
            else {
                Assert.fail("Too many OtherData");
            }
            lcv++;
        }
        
        Assert.assertEquals(2, lcv);
        
        checkFinancials(locator.getService(Financials.class), NASDAQ, ATT_SYMBOL);
    }
    
    private final static String UNO = "uno";
    private final static String DOS = "dos";
    private final static String TRES = "tres";
    private final static String QUATRO = "quatro";
    
    /**
     * Tests that we can add to an existing tree with just a basic add where the
     * add methods return the item added
     */
    @Test
    // @org.junit.Ignore
    public void testAddsThatReturnValues() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class, false, false);
        
        Employees employees = rootHandle.getRoot();
        
        NamedBean tresBean = employees.addName(TRES);
        Assert.assertNotNull(tresBean);
        Assert.assertEquals(TRES, tresBean.getName());
        
        NamedBean quatro = xmlService.createBean(NamedBean.class);
        quatro.setName(QUATRO);
        
        NamedBean quatroBean = employees.addName(quatro);
        Assert.assertNotNull(quatroBean);
        Assert.assertEquals(QUATRO, quatroBean.getName());
        
        NamedBean uno = xmlService.createBean(NamedBean.class);
        uno.setName(UNO);
        
        NamedBean unoBean = employees.addName(uno, 0);
        Assert.assertNotNull(unoBean);
        Assert.assertEquals(UNO, unoBean.getName());
        
        NamedBean dosBean = employees.addName(DOS, 1);
        Assert.assertNotNull(dosBean);
        Assert.assertEquals(DOS, dosBean.getName());
        
        NamedBean[] allBeans = employees.getNames();
        
        Assert.assertEquals(4, allBeans.length);
        
        Assert.assertEquals(unoBean, allBeans[0]);
        Assert.assertEquals(dosBean, allBeans[1]);
        Assert.assertEquals(tresBean, allBeans[2]);
        Assert.assertEquals(quatroBean, allBeans[3]);
    }
    
    @Singleton
    private static class RecordingBeanUpdateListener implements BeanDatabaseUpdateListener {
        private List<Change> latestPrepares;
        private List<Change> latestCommit;
        private List<Change> latestRollback;

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#prepareDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void prepareDatabaseChange(BeanDatabase currentDatabase,
                BeanDatabase proposedDatabase, Object commitMessage,
                List<Change> changes) {
            latestPrepares = changes;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#commitDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void commitDatabaseChange(BeanDatabase oldDatabase,
                BeanDatabase currentDatabase, Object commitMessage,
                List<Change> changes) {
            latestCommit = changes;
            
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#rollbackDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void rollbackDatabaseChange(BeanDatabase currentDatabase,
                BeanDatabase proposedDatabase, Object commitMessage,
                List<Change> changes) {
            latestRollback = changes;
            
        }
        
    }
}
