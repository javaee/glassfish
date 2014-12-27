/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.lifecycle.config.LifecycleConfig;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for unmarshalling xml into the hk2 hub
 * 
 * @author jwells
 */
public class UnmarshallTest {
    private final static String MUSEUM1_FILE = "museum1.xml";
    private final static String ACME1_FILE = "Acme1.xml";
    
    private final static String BEN_FRANKLIN = "Ben Franklin";
    private final static String ACME = "Acme";
    private final static String BOB = "Bob";
    private final static String CAROL = "Carol";
    private final static String ACME_SYMBOL = "acme";
    private final static String NYSE = "NYSE";
    
    private final static int HUNDRED_INT = 100;
    private final static int HUNDRED_TEN_INT = 110;
    
    private final static long HUNDRED_LONG = 100L;
    private final static long HUNDRED_ONE_LONG = 101L;
    
    private final static String COMPANY_NAME_TAG = "company-name";
    private final static String EMPLOYEE_TAG = "employee";
    private final static String NAME_TAG = "name";
    private final static String ID_TAG = "id";
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshall(url.toURI(), Museum.class);
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(HUNDRED_INT, museum.getId());
        Assert.assertEquals(BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(HUNDRED_TEN_INT, museum.getAge());
        
        Museum asService = locator.getService(Museum.class);
        Assert.assertNotNull(asService);
        
        Assert.assertEquals(museum, asService);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an class
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test @org.junit.Ignore
    public void testClassJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(MUSEUM1_FILE);
        
        XmlRootHandle<MuseumBean> rootHandle = xmlService.unmarshall(url.toURI(), MuseumBean.class);
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(HUNDRED_INT, museum.getId());
        Assert.assertEquals(BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(HUNDRED_TEN_INT, museum.getAge());
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an class
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test @org.junit.Ignore
    public void testClassJaxbUnmarshallingWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(ACME1_FILE);
        
        XmlRootHandle<EmployeesBean> rootHandle = xmlService.unmarshall(url.toURI(), EmployeesBean.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertEquals(ACME, employees.getCompanyName());
        
        Assert.assertEquals(2, employees.getEmployees().size());
        
        boolean first = true;
        for (Employee employee : employees.getEmployees()) {
            if (first) {
                first = false;
                Assert.assertEquals(HUNDRED_LONG, employee.getId());
                Assert.assertEquals(BOB, employee.getName());
            }
            else {
                Assert.assertEquals(HUNDRED_ONE_LONG, employee.getId());
                Assert.assertEquals(CAROL, employee.getName());
                
            }
        }
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test // @org.junit.Ignore
    public void testBeanLikeMapOfInterface() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertTrue(employees instanceof XmlHk2ConfigurationBean);
        XmlHk2ConfigurationBean hk2Configuration = (XmlHk2ConfigurationBean) employees;
        
        Map<String, Object> beanLikeMap = hk2Configuration._getBeanLikeMap();
        Assert.assertEquals(ACME, beanLikeMap.get(COMPANY_NAME_TAG));
        
        List<Employee> employeeChildList = (List<Employee>) beanLikeMap.get(EMPLOYEE_TAG);
        Assert.assertNotNull(employeeChildList);
        Assert.assertEquals(2, employeeChildList.size());
        
        boolean first = true;
        for (Employee employee : employeeChildList) {
            Assert.assertTrue(employee instanceof XmlHk2ConfigurationBean);
            XmlHk2ConfigurationBean employeeConfiguration = (XmlHk2ConfigurationBean) employee;
            
            Map<String, Object> employeeBeanLikeMap = employeeConfiguration._getBeanLikeMap();
            
            if (first) {
                first = false;
                
                Assert.assertEquals(HUNDRED_LONG, employeeBeanLikeMap.get(ID_TAG));
                Assert.assertEquals(BOB, employeeBeanLikeMap.get(NAME_TAG));
            }
            else {
                Assert.assertEquals(HUNDRED_ONE_LONG, employeeBeanLikeMap.get(ID_TAG));
                Assert.assertEquals(CAROL, employeeBeanLikeMap.get(NAME_TAG));
            }
        }
        
        Assert.assertNotNull(locator.getService(Employees.class));
        
        Assert.assertNotNull(locator.getService(Employee.class, BOB));
        Assert.assertNotNull(locator.getService(Employee.class, CAROL));
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshallingWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertEquals(ACME, employees.getCompanyName());
        
        Assert.assertEquals(2, employees.getEmployees().size());
        
        boolean first = true;
        for (Employee employee : employees.getEmployees()) {
            if (first) {
                first = false;
                Assert.assertEquals(HUNDRED_LONG, employee.getId());
                Assert.assertEquals(BOB, employee.getName());
            }
            else {
                Assert.assertEquals(HUNDRED_ONE_LONG, employee.getId());
                Assert.assertEquals(CAROL, employee.getName());
            }
        }
        
        Financials financials = employees.getFinancials();
        Assert.assertNotNull(financials);
        
        Assert.assertEquals(ACME_SYMBOL, financials.getSymbol());
        Assert.assertEquals(NYSE, financials.getExchange());
        
        Assert.assertEquals(Employees.class, rootHandle.getRootClass());
        Assert.assertEquals(url, rootHandle.getURI().toURL());
    }
    
    private final static String LIFECYCLE_ROOT_TYPE = "/lifecycle-config";
    private final static String LIFECYCLE_ROOT_INSTANCE = "lifecycle-config";
    private final static String LIFECYCLE_RUNTIMES_TYPE = "/lifecycle-config/runtimes";
    private final static String LIFECYCLE_RUNTIMES_INSTANCE = "lifecycle-config.runtimes";
    private final static String LIFECYCLE_TENANTS_TYPE = "/lifecycle-config/tenants";
    private final static String LIFECYCLE_TENANTS_INSTANCE = "lifecycle-config.tenants";
    private final static String LIFECYCLE_ENVIRONMENTS_TYPE = "/lifecycle-config/environments";
    private final static String LIFECYCLE_ENVIRONMENTS_INSTANCE = "lifecycle-config.environments";
    
    private final static String LIFECYCLE_RUNTIME_TYPE = "/lifecycle-config/runtimes/runtime";
    private final static String LIFECYCLE_RUNTIME_wlsRuntime_INSTANCE = "lifecycle-config.runtimes.wlsRuntime";
    private final static String LIFECYCLE_RUNTIME_DatabaseTestRuntime_INSTANCE = "lifecycle-config.runtimes.DatabaseTestRuntime";
    
    /**
     * Tests a more complex XML format.  This test will ensure
     * all elements are in the Hub with expected names
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testComplexUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource("sample-config.xml");
        
        XmlRootHandle<LifecycleConfig> rootHandle = xmlService.unmarshall(url.toURI(), LifecycleConfig.class);
        LifecycleConfig lifecycleConfig = rootHandle.getRoot();
        Assert.assertNotNull(lifecycleConfig);
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_ROOT_TYPE, LIFECYCLE_ROOT_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIMES_TYPE, LIFECYCLE_RUNTIMES_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_TENANTS_TYPE, LIFECYCLE_TENANTS_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_ENVIRONMENTS_TYPE, LIFECYCLE_ENVIRONMENTS_INSTANCE));
        
        // Runtime
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIME_TYPE, LIFECYCLE_RUNTIME_wlsRuntime_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIME_TYPE, LIFECYCLE_RUNTIME_DatabaseTestRuntime_INSTANCE));
    }
    
    private final static String FOOBAR_ROOT_TYPE = "/foobar";
    private final static String FOOBAR_ROOT_INSTANCE = "foobar";
    
    private final static String FOOBAR_FOO_TYPE = "/foobar/foo";
    private final static String FOOBAR_FOO1_INSTANCE = "foobar.foo1";
    private final static String FOOBAR_FOO2_INSTANCE = "foobar.foo2";
    
    private final static String FOOBAR_BAR_TYPE = "/foobar/bar";
    private final static String FOOBAR_BAR1_INSTANCE = "foobar.bar1";
    private final static String FOOBAR_BAR2_INSTANCE = "foobar.bar2";
    
    
    /**
     * Foobar has two children, foo and bar, both of which are of type DataBean
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testSameClassTwoChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource("foobar.xml");
        
        XmlRootHandle<FooBarBean> rootHandle = xmlService.unmarshall(url.toURI(), FooBarBean.class);
        FooBarBean foobar = rootHandle.getRoot();
        Assert.assertNotNull(foobar);
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_ROOT_TYPE, FOOBAR_ROOT_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_FOO_TYPE, FOOBAR_FOO1_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_FOO_TYPE, FOOBAR_FOO2_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_BAR_TYPE, FOOBAR_BAR1_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_BAR_TYPE, FOOBAR_BAR2_INSTANCE));
    }
}
