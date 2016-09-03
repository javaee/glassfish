/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.test.basic;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.glassfish.hk2.xml.lifecycle.config.Service;
import org.glassfish.hk2.xml.lifecycle.config.Tenant;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Employee;
import org.glassfish.hk2.xml.test.basic.beans.Employees;
import org.glassfish.hk2.xml.test.basic.beans.EverythingBagel;
import org.glassfish.hk2.xml.test.basic.beans.Financials;
import org.glassfish.hk2.xml.test.basic.beans.FooBarBean;
import org.glassfish.hk2.xml.test.basic.beans.GreekEnum;
import org.glassfish.hk2.xml.test.basic.beans.Museum;
import org.glassfish.hk2.xml.test.basic.beans.OtherData;
import org.glassfish.hk2.xml.test.basic.beans.RootWithCycle;
import org.glassfish.hk2.xml.test.basic.beans.TypeBean;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for unmarshalling xml into the hk2 hub
 * 
 * @author jwells
 */
public class UnmarshallTest {
    private final Commons commons = new Commons();
    public final static String MUSEUM1_FILE = "museum1.xml";
    public final static String ACME1_FILE = "Acme1.xml";
    public final static String ACME2_FILE = "Acme2.xml";
    private final static String SAMPLE_CONFIG_FILE = "sample-config.xml";
    private final static String CYCLE = "cycle.xml";
    private final static String TYPE1_FILE = "type1.xml";
    
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
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testInterfaceJaxbUnmarshalling(locator);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testBeanLikeMapOfInterface() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testBeanLikeMapOfInterface(locator);
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
        commons.testInterfaceJaxbUnmarshallingWithChildren(locator);
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
        commons.testComplexUnmarshalling(locator);
    }
    
    private final static String ASSOCIATION_PARTITION1_TYPE = "/lifecycle-config/environments/environment/associations/association/partition1";
    private final static String ASSOCIATION_PARTITION2_TYPE = "/lifecycle-config/environments/environment/associations/association/partition2";
    private final static String ASSOCIATION_PARTITION_INSTANCE_PREFIX = "lifecycle-config.environments.cokeenv.associations.";
    private final static String ASSOCIATION_PARTITION1_0_INSTANCE_APPENDIX = ".part1-0";
    private final static String ASSOCIATION_PARTITION2_0_INSTANCE_APPENDIX = ".part2-0";
    private final static String ASSOCIATION_PARTITION1_1_INSTANCE_APPENDIX = ".part1-1";
    private final static String ASSOCIATION_PARTITION2_1_INSTANCE_APPENDIX = ".part2-1";
    
    private final static String PART1_0_NAME = "part1-0";
    private final static String PART2_0_NAME = "part2-0";
    private final static String PART1_1_NAME = "part1-1";
    private final static String PART2_1_NAME = "part2-1";
    
    /**
     * Associations has unkeyed children of type Association.  We
     * get them and make sure they have unique keys generated
     * by the system
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testUnkeyedChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testUnkeyedChildren(locator);
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
        commons.testSameClassTwoChildren(locator);
    }
    
    /**
     * Tests that an xml hierarchy with a cycle can be unmarshalled
     * 
     * @throws Exception
     */
    @Test
    public void testBeanCycle() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testBeanCycle(locator);
    }
    
    /**
     * Tests every scalar type that can be read
     * 
     * @throws Exception
     */
    @Test
    public void testEveryType() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testEveryType(locator);
    }
    
    /**
     * Tests that the annotation is fully copied over on the method
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testAnnotationWithEverythingCopied() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testAnnotationWithEverythingCopied(locator);
    }
    
    /**
     * Tests that a list child with no elements returns an empty list (not null)
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testEmptyListChildReturnsEmptyList() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testEmptyListChildReturnsEmptyList(locator);
    }
    
    /**
     * Tests that a list child with no elements returns an empty array (not null)
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testEmptyArrayChildReturnsEmptyArray() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testEmptyArrayChildReturnsEmptyArray(locator);
    }
    
    /**
     * Tests that a byte[] child gets properly translated
     * (into itself, for now)
     * 
     * @throws Exception
     */
    @Test @org.junit.Ignore
    public void testByteArrayNonChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        commons.testByteArrayNonChild(locator);
    }
}
