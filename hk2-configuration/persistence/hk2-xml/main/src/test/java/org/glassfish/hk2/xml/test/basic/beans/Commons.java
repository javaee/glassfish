/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.basic.beans;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.lifecycle.config.Association;
import org.glassfish.hk2.xml.lifecycle.config.Associations;
import org.glassfish.hk2.xml.lifecycle.config.Environment;
import org.glassfish.hk2.xml.lifecycle.config.LifecycleConfig;
import org.glassfish.hk2.xml.lifecycle.config.Partition;
import org.glassfish.hk2.xml.lifecycle.config.Runtime;
import org.glassfish.hk2.xml.lifecycle.config.Runtimes;
import org.glassfish.hk2.xml.lifecycle.config.Service;
import org.glassfish.hk2.xml.lifecycle.config.Tenant;
import org.junit.Assert;

/**
 * @author jwells
 *
 */
public class Commons {
    public final static String MUSEUM1_FILE = "museum1.xml";
    public final static String ACME1_FILE = "Acme1.xml";
    public final static String ACME2_FILE = "Acme2.xml";
    public final static String SAMPLE_CONFIG_FILE = "sample-config.xml";
    public final static String CYCLE_FILE = "cycle.xml";
    public final static String TYPE1_FILE = "type1.xml";
    public final static String FOOBAR_FILE = "foobar.xml";
    public final static String REFERENCE1_FILE = "reference1.xml";
    
    public final static String BEN_FRANKLIN = "Ben Franklin";
    public final static String ACME = "Acme";
    public final static String ALICE = "Alice";
    public final static String BOB = "Bob";
    public final static String CAROL = "Carol";
    public final static String DAVE = "Dave";
    public final static String ENGLEBERT = "Englebert";
    public final static String FRANK = "Frank";
    public final static String ACME_SYMBOL = "acme";
    public final static String NYSE = "NYSE";
    private final static String COKE_TENANT = "coke";
    private final static String HRPROD_SERVICE = "HRProd";
    
    public final static String FINANCIALS_TYPE = "/employees/financials";
    public final static String FINANCIALS_INSTANCE = "employees.financials";
    
    public final static String EMPLOYEES_TYPE = "/employees";
    public final static String EMPLOYEES_INSTANCE_NAME = "employees";
    
    public final static String EMPLOYEE_TYPE = "/employees/employee";
    public final static String DAVE_EMPLOYEE_INSTANCE = "employees.Dave";
    
    public final static int HUNDRED_INT = 100;
    public final static int HUNDRED_TEN_INT = 110;
    
    public final static long HUNDRED_LONG = 100L;
    public final static long HUNDRED_ONE_LONG = 101L;
    
    public final static String COMPANY_NAME_TAG = "company-name";
    public final static String EMPLOYEE_TAG = "employee";
    public final static String NAME_TAG = "name";
    public final static String ID_TAG = "id";
    private final static String COKE_ENV = "cokeenv";
    public final static String SYMBOL_TAG = "symbol";
    public final static String EXCHANGE_TAG = "exchange";
    public final static String NON_KEY_TAG = "non-key-identifier";
    public final static String PORT_TAG = "port";
    public final static String PUBLIC_KEY_TAG = "public-key-location";
    
    public final static String TYPES_FILE = "types/types.xml";
    
    private Commons() {}
    
    public static void testInterfaceJaxbUnmarshalling(ServiceLocator locator, URI uri) throws Exception {
        testInterfaceJaxbUnmarshalling(locator, uri, null);
    }
    
    public static void testInterfaceJaxbUnmarshalling(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testInterfaceJaxbUnmarshalling(locator, null, reader);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    private static void testInterfaceJaxbUnmarshalling(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Museum> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Museum.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Museum.class, true, true);
        }
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(HUNDRED_INT, museum.getId());
        Assert.assertEquals(BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(HUNDRED_TEN_INT, museum.getAge());
        
        Museum asService = locator.getService(Museum.class);
        Assert.assertNotNull(asService);
        
        Assert.assertEquals(museum, asService);
    }
    
    public static void testBeanLikeMapOfInterface(ServiceLocator locator, URI uri) throws Exception {
        testBeanLikeMapOfInterface(locator, uri, null);
    }
    
    public static void testBeanLikeMapOfInterface(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testBeanLikeMapOfInterface(locator, null, reader);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static void testBeanLikeMapOfInterface(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
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
    
    public static void testInterfaceJaxbUnmarshallingWithChildren(ServiceLocator locator, URI uri) throws Exception {
        testInterfaceJaxbUnmarshallingWithChildren(locator, uri, null);
    }
    
    public static void testInterfaceJaxbUnmarshallingWithChildren(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testInterfaceJaxbUnmarshallingWithChildren(locator, null, reader);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    private static void testInterfaceJaxbUnmarshallingWithChildren(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
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
        if (uri != null) {
            Assert.assertEquals(uri, rootHandle.getURI());
        }
        else {
            Assert.assertNull(rootHandle.getURI());
        }
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FINANCIALS_TYPE, FINANCIALS_INSTANCE));
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
    
    private final static String WLS_RUNTIME_NAME = "wlsRuntime";
    
    public static void testComplexUnmarshalling(ServiceLocator locator, URI uri) throws Exception {
        testComplexUnmarshalling(locator, uri, null);
    }
    
    public static void testComplexUnmarshalling(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testComplexUnmarshalling(locator, null, reader);
    }
    
    /**
     * Tests a more complex XML format.  This test will ensure
     * all elements are in the Hub with expected names
     * 
     * @throws Exception
     */
    private static void testComplexUnmarshalling(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        XmlRootHandle<LifecycleConfig> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, LifecycleConfig.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, LifecycleConfig.class, true, true);
        }
        LifecycleConfig lifecycleConfig = rootHandle.getRoot();
        Assert.assertNotNull(lifecycleConfig);
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_ROOT_TYPE, LIFECYCLE_ROOT_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIMES_TYPE, LIFECYCLE_RUNTIMES_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_TENANTS_TYPE, LIFECYCLE_TENANTS_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_ENVIRONMENTS_TYPE, LIFECYCLE_ENVIRONMENTS_INSTANCE));
        
        // Runtime
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIME_TYPE, LIFECYCLE_RUNTIME_wlsRuntime_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(LIFECYCLE_RUNTIME_TYPE, LIFECYCLE_RUNTIME_DatabaseTestRuntime_INSTANCE));
        
        Tenant tenant = locator.getService(Tenant.class, COKE_TENANT);
        Assert.assertNotNull(tenant);
        
        Service hrProdService = tenant.lookupService(HRPROD_SERVICE);
        Assert.assertNotNull(hrProdService);
        Assert.assertEquals(HRPROD_SERVICE, hrProdService.getName());
    }
    
    private final static String ASSOCIATION_TYPE = "/lifecycle-config/environments/environment/associations/association";
    private final static String ASSOCIATION_INSTANCE_PREFIX = "lifecycle-config.environments.cokeenv.associations.";
    
    public static void testUnkeyedChildren(ServiceLocator locator, URI uri) throws Exception {
        testUnkeyedChildren(locator, uri, null);
    }
    
    public static void testUnkeyedChildren(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testUnkeyedChildren(locator, null, reader);
    }
    
    /**
     * Associations has unkeyed children of type Association.  We
     * get them and make sure they have unique keys generated
     * by the system
     * 
     * @throws Exception
     */
    private static void testUnkeyedChildren(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        XmlRootHandle<LifecycleConfig> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, LifecycleConfig.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, LifecycleConfig.class, true, true);
        }
        LifecycleConfig lifecycleConfig = rootHandle.getRoot();
        Assert.assertNotNull(lifecycleConfig);
        
        // Lets look at an unkeyed child
        Environment cokeEnv = locator.getService(Environment.class, COKE_ENV);
        Assert.assertNotNull(cokeEnv);
        
        // Lets get the generated unique IDs for the unkeyed children
        Associations associations = cokeEnv.getAssociations();
        Assert.assertNotNull(associations);
        
        String generatedKey1 = null;
        String generatedKey2 = null;
        for (Association association : associations.getAssociations()) {
            XmlHk2ConfigurationBean bean = (XmlHk2ConfigurationBean) association;
            Assert.assertNull(bean._getKeyPropertyName());
            
            if (generatedKey1 == null) {
                generatedKey1 = bean._getKeyValue();
            }
            else if (generatedKey2 == null) {
                generatedKey2 = bean._getKeyValue();
            }
            else {
                Assert.fail("Should only have been two associations, but we found at least three");
            }
        }
        
        Assert.assertNotNull(generatedKey1);
        Assert.assertNotNull(generatedKey2);
        Assert.assertNotEquals(generatedKey1, generatedKey2);
        
        // Given the generated key we can now construct the paths to the children
        // Lets get them from the hub
        String association0_instance_name = ASSOCIATION_INSTANCE_PREFIX + generatedKey1;
        String association1_instance_name = ASSOCIATION_INSTANCE_PREFIX + generatedKey2;
        
        Instance as_0 = hub.getCurrentDatabase().getInstance(ASSOCIATION_TYPE, association0_instance_name);
        Instance as_1 = hub.getCurrentDatabase().getInstance(ASSOCIATION_TYPE, association1_instance_name);
        
        Assert.assertNotNull(as_0);
        Assert.assertNotNull(as_1);
    }
    
    private final static String FOOBAR_ROOT_TYPE = "/foobar";
    private final static String FOOBAR_ROOT_INSTANCE = "foobar";
    
    private final static String FOOBAR_FOO_TYPE = "/foobar/foo";
    private final static String FOOBAR_FOO1_INSTANCE = "foobar.foo1";
    private final static String FOOBAR_FOO2_INSTANCE = "foobar.foo2";
    
    private final static String FOOBAR_BAR_TYPE = "/foobar/bar";
    private final static String FOOBAR_BAR1_INSTANCE = "foobar.bar1";
    private final static String FOOBAR_BAR2_INSTANCE = "foobar.bar2";
    
    public static void testSameClassTwoChildren(ServiceLocator locator, URI uri) throws Exception {
        testSameClassTwoChildren(locator, uri, null);
    }
    
    public static void testSameClassTwoChildren(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testSameClassTwoChildren(locator, null, reader);
    }
    
    /**
     * Foobar has two children, foo and bar, both of which are of type DataBean
     * 
     * @throws Exception
     */
    private static void testSameClassTwoChildren(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        XmlRootHandle<FooBarBean> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, FooBarBean.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, FooBarBean.class, true, true);
        }
        FooBarBean foobar = rootHandle.getRoot();
        Assert.assertNotNull(foobar);
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_ROOT_TYPE, FOOBAR_ROOT_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_FOO_TYPE, FOOBAR_FOO1_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_FOO_TYPE, FOOBAR_FOO2_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_BAR_TYPE, FOOBAR_BAR1_INSTANCE));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(FOOBAR_BAR_TYPE, FOOBAR_BAR2_INSTANCE));
    }
    
    public static void testBeanCycle(ServiceLocator locator, URI uri) throws Exception {
        testBeanCycle(locator, uri, null);
    }
    
    public static void testBeanCycle(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testBeanCycle(locator, null, reader);
    }
    
    /**
     * Tests that an xml hierarchy with a cycle can be unmarshalled
     * 
     * @throws Exception
     */
    private static void testBeanCycle(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<RootWithCycle> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, RootWithCycle.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, RootWithCycle.class, true, true);
        }
        RootWithCycle cycle = rootHandle.getRoot();
        
        Assert.assertNotNull(cycle);
        Assert.assertNotNull(cycle.getLeafWithCycle());
        Assert.assertNotNull(cycle.getLeafWithCycle().getRootWithCycle());
        Assert.assertNull(cycle.getLeafWithCycle().getRootWithCycle().getLeafWithCycle());
    }
    
    public static void testEveryType(ServiceLocator locator, URI uri) throws Exception {
        testEveryType(locator, uri, null);
    }
    
    public static void testEveryType(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testEveryType(locator, null, reader);
    }
    
    /**
     * Tests every scalar type that can be read
     * 
     * @throws Exception
     */
    private static void testEveryType(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<TypeBean> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, TypeBean.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, TypeBean.class, true, true);
        }
        TypeBean types = rootHandle.getRoot();
        
        Assert.assertNotNull(types);
        Assert.assertEquals(13, types.getIType());
        Assert.assertEquals(-13L, types.getJType());
        Assert.assertEquals(true, types.getZType());
        Assert.assertEquals((byte) 120, types.getBType());
        Assert.assertEquals((short) 161, types.getSType());
        Assert.assertEquals(0, Float.compare((float) 3.14, types.getFType()));
        Assert.assertEquals(0, Double.compare(2.71828, types.getDType()));
        Assert.assertEquals(new QName("bar"), types.getQNameLocalOnly());
        Assert.assertEquals(new QName("http://www.acme.org/jmxoverjms", "foo", "xos"), types.getQName());
        Assert.assertEquals(new QName("http://www.eagles.com/CarsonCity", "phillies", "sox"), types.getQNameElementPrefix());
        Assert.assertNull(types.getQNameUnknownPrefix());
    }
    
    public static void testAnnotationWithEverythingCopied(ServiceLocator locator, URI uri) throws Exception {
        testAnnotationWithEverythingCopied(locator, uri, null);
    }
    
    public static void testAnnotationWithEverythingCopied(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testAnnotationWithEverythingCopied(locator, null, reader);
    }
    
    /**
     * Tests that the annotation is fully copied over on the method
     * 
     * @throws Exception
     */
    private static void testAnnotationWithEverythingCopied(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
        Employees employees = rootHandle.getRoot();
        
        Method setBagelMethod = employees.getClass().getMethod("setBagelPreference", new Class<?>[] { int.class });
        EverythingBagel bagel = setBagelMethod.getAnnotation(EverythingBagel.class);
        
        Assert.assertEquals((byte) 13, bagel.byteValue());
        Assert.assertTrue(bagel.booleanValue());
        Assert.assertEquals('e', bagel.charValue());
        Assert.assertEquals((short) 13, bagel.shortValue());
        Assert.assertEquals(13, bagel.intValue());
        Assert.assertEquals(13L, bagel.longValue());
        Assert.assertEquals(0, Float.compare((float) 13.00, bagel.floatValue()));
        Assert.assertEquals(0, Double.compare(13.00, bagel.doubleValue()));
        Assert.assertEquals("13", bagel.stringValue());
        Assert.assertEquals(Employees.class, bagel.classValue());
        Assert.assertEquals(GreekEnum.BETA, bagel.enumValue());
        
        Assert.assertTrue(Arrays.equals(new byte[] { 13, 14 }, bagel.byteArrayValue()));
        Assert.assertTrue(Arrays.equals(new boolean[] { true, false }, bagel.booleanArrayValue()));
        Assert.assertTrue(Arrays.equals(new char[] { 'e', 'E' }, bagel.charArrayValue()));
        Assert.assertTrue(Arrays.equals(new short[] { 13, 14 }, bagel.shortArrayValue()));
        Assert.assertTrue(Arrays.equals(new int[] { 13, 14 }, bagel.intArrayValue()));
        Assert.assertTrue(Arrays.equals(new long[] { 13, 14 }, bagel.longArrayValue()));
        Assert.assertTrue(Arrays.equals(new String[] { "13", "14" }, bagel.stringArrayValue()));
        Assert.assertTrue(Arrays.equals(new Class[] { String.class, double.class }, bagel.classArrayValue()));
        Assert.assertTrue(Arrays.equals(new GreekEnum[] { GreekEnum.GAMMA, GreekEnum.ALPHA }, bagel.enumArrayValue()));
        
        // The remaining need to be compared manually (not with Arrays)
        Assert.assertEquals(0, Float.compare((float) 13.00, bagel.floatArrayValue()[0]));
        Assert.assertEquals(0, Float.compare((float) 14.00, bagel.floatArrayValue()[1]));
        
        Assert.assertEquals(0, Double.compare(13.00, bagel.doubleArrayValue()[0]));
        Assert.assertEquals(0, Double.compare(14.00, bagel.doubleArrayValue()[1]));
    }
    
    public static void testEmptyListChildReturnsEmptyList(ServiceLocator locator, URI uri) throws Exception {
        testEmptyListChildReturnsEmptyList(locator, uri, null);
    }
    
    public static void testEmptyListChildReturnsEmptyList(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testEmptyListChildReturnsEmptyList(locator, null, reader);
    }
    
    /**
     * Tests that a list child with no elements returns an empty list (not null)
     * 
     * @throws Exception
     */
    private static void testEmptyListChildReturnsEmptyList(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
        Employees employees = rootHandle.getRoot();
        
        List<OtherData> noChildrenList = employees.getNoChildList();
        Assert.assertNotNull(noChildrenList);
        Assert.assertTrue(noChildrenList.isEmpty());
    }
    
    public static void testEmptyArrayChildReturnsEmptyArray(ServiceLocator locator, URI uri) throws Exception {
        testEmptyArrayChildReturnsEmptyArray(locator, uri, null);
    }
    
    public static void testEmptyArrayChildReturnsEmptyArray(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testEmptyArrayChildReturnsEmptyArray(locator, null, reader);
    }
    
    /**
     * Tests that a list child with no elements returns an empty array (not null)
     * 
     * @throws Exception
     */
    private static void testEmptyArrayChildReturnsEmptyArray(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
        Employees employees = rootHandle.getRoot();
        
        OtherData[] noChildrenList = employees.getNoChildArray();
        Assert.assertNotNull(noChildrenList);
        Assert.assertEquals(0, noChildrenList.length);
    }
    
    public static void testByteArrayNonChild(ServiceLocator locator, URI uri) throws Exception {
        testByteArrayNonChild(locator, uri, null);
    }
    
    public static void testByteArrayNonChild(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testByteArrayNonChild(locator, null, reader);
    }
    
    /**
     * Tests that a byte[] child gets properly translated
     * (into itself, for now)
     * 
     * @throws Exception
     */
    private static void testByteArrayNonChild(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        // URL url = getClass().getClassLoader().getResource(ACME2_FILE);
        
        XmlRootHandle<Employees> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, Employees.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, Employees.class, true, true);
        }
        Employees employees = rootHandle.getRoot();
        
        byte[] encrypted = employees.getEncryptedCredentials();
        Assert.assertNotNull(encrypted);
        
        String asString = new String(encrypted);
        
        Assert.assertEquals("Garbledeguk", asString);
    }
    
    public static void testJaxbStyleReference(ServiceLocator locator, URI uri) throws Exception {
        testJaxbStyleReference(locator, uri, null);
    }
    
    public static void testJaxbStyleReference(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testJaxbStyleReference(locator, null, reader);
    }
    
    /**
     * Tests references that use the JAXB XmlID and XmlIDRef annotations
     * 
     * @throws Exception
     */
    private static void testJaxbStyleReference(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<LifecycleConfig> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, LifecycleConfig.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, LifecycleConfig.class, true, true);
        }
        LifecycleConfig lifecycleConfig = rootHandle.getRoot();
        Assert.assertNotNull(lifecycleConfig);
        
        // Lets look at an unkeyed child
        Environment cokeEnv = locator.getService(Environment.class, COKE_ENV);
        Assert.assertNotNull(cokeEnv);
        
        // Lets get the generated unique IDs for the unkeyed children
        Associations associations = cokeEnv.getAssociations();
        Assert.assertNotNull(associations);
        
        Assert.assertEquals(2, associations.getAssociations().size());
        Association association0 = associations.getAssociations().get(0);
        Association association1 = associations.getAssociations().get(1);
        
        Assert.assertNotNull(association0);
        Assert.assertNotNull(association1);
        
        Runtimes runtimes = lifecycleConfig.getRuntimes();
        Runtime runtime = runtimes.lookupRuntime(WLS_RUNTIME_NAME);
        
        Partition allTwos = runtime.lookupPartition("222");
        Partition allThrees = runtime.lookupPartition("333");
        Partition allFives = runtime.lookupPartition("555");
        Partition allSevens = runtime.lookupPartition("777");
        
        Assert.assertNotNull(allTwos);
        Assert.assertNotNull(allThrees);
        Assert.assertNotNull(allFives);
        Assert.assertNotNull(allSevens);
        
        Partition a0_p1 = association0.getPartition1();
        Partition a0_p2 = association0.getPartition2();
        
        Partition a1_p1 = association1.getPartition1();
        Partition a1_p2 = association1.getPartition2();
        
        Assert.assertNotNull(a0_p1);
        Assert.assertNotNull(a0_p2);
        Assert.assertNotNull(a1_p1);
        Assert.assertNotNull(a1_p2);
        
        // Here is the actual test, make sure the references refer to their referee
        Assert.assertEquals(a0_p1, allTwos);
        Assert.assertEquals(a0_p2, allThrees);
        Assert.assertEquals(a1_p1, allFives);
        Assert.assertEquals(a1_p2, allSevens);
    }
    
    public static void testJaxbStyleForwardReference(ServiceLocator locator, URI uri) throws Exception {
        testJaxbStyleForwardReference(locator, uri, null);
    }
    
    public static void testJaxbStyleForwardReference(ServiceLocator locator, XMLStreamReader reader) throws Exception {
        testJaxbStyleForwardReference(locator, null, reader);
    }
    
    /**
     * Tests references before and after the reference
     * 
     * @throws Exception
     */
    private static void testJaxbStyleForwardReference(ServiceLocator locator, URI uri, XMLStreamReader reader) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<ReferenceMaster> rootHandle;
        if (uri != null) {
            rootHandle = xmlService.unmarshal(uri, ReferenceMaster.class);
        }
        else {
            rootHandle = xmlService.unmarshal(reader, ReferenceMaster.class, true, true);
        }
        ReferenceMaster referenceMaster = rootHandle.getRoot();
        Assert.assertNotNull(referenceMaster);
        
        AboveBean alice = referenceMaster.getAboves().get(0);
        AboveBean bob = referenceMaster.getAboves().get(1);
        
        BelowBean carol = referenceMaster.getBelows().get(0);
        BelowBean dave = referenceMaster.getBelows().get(1);
        
        Assert.assertNotNull(alice);
        Assert.assertNotNull(bob);
        Assert.assertNotNull(carol);
        Assert.assertNotNull(dave);
        
        {
            TiesBean tie0 = referenceMaster.getTies().get(0);
        
            AboveBean aliceFromTie = tie0.getAbove();
            BelowBean carolFromTie = tie0.getBelow();
        
            Assert.assertEquals(alice, aliceFromTie);
            Assert.assertEquals(carol, carolFromTie);
        }
        
        {
            TiesBean tie1 = referenceMaster.getTies().get(1);
        
            AboveBean bobFromTie = tie1.getAbove();
            BelowBean daveFromTie = tie1.getBelow();
        
            Assert.assertEquals(bob, bobFromTie);
            Assert.assertEquals(dave, daveFromTie);
        }
        
        {
            BackwardTiesBean tie2 = referenceMaster.getBackwardTies().get(0);
        
            AboveBean aliceFromTie = tie2.getAbove();
            BelowBean daveFromTie = tie2.getBelow();
        
            Assert.assertEquals(alice, aliceFromTie);
            Assert.assertEquals(dave, daveFromTie);
        }
        
        {
            BackwardTiesBean tie3 = referenceMaster.getBackwardTies().get(1);
        
            AboveBean bobFromTie = tie3.getAbove();
            BelowBean carolFromTie = tie3.getBelow();
        
            Assert.assertEquals(bob, bobFromTie);
            Assert.assertEquals(carol, carolFromTie);
        }
        
        
        
    }
}
