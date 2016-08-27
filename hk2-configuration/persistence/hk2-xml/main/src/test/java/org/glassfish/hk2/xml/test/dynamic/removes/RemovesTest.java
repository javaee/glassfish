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
package org.glassfish.hk2.xml.test.dynamic.removes;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.Employee;
import org.glassfish.hk2.xml.test.basic.Employees;
import org.glassfish.hk2.xml.test.basic.Financials;
import org.glassfish.hk2.xml.test.basic.OtherData;
import org.glassfish.hk2.xml.test.basic.UnmarshallTest;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests removal of stuff
 * 
 * @author jwells
 *
 */
public class RemovesTest {
    public final static String EMPLOYEE_TYPE = "/employees/employee";
    public final static String BOB_EMPLOYEE_INSTANCE = "employees.Bob";
    
    public final static String ACME3_FILE = "Acme3.xml";
    
    public final static String INDEX0 = "Index0";
    public final static String INDEX1 = "Index1";
    public final static String INDEX2 = "Index2";
    public final static String INDEX3 = "Index3";
    
    public final static String OTHER_DATA_TYPE = "/employees/other-data";
    private final static String DATA_KEY = "data";
    
    private final static String QUEUED0_INSTANCE_NAME = "domain.Dave.QueueD0";
    private final static String TOPICD0_INSTANCE_NAME = "domain.Dave.TopicD0";
    
    /**
     * Tests remove of a keyed child with no sub-children
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfNamedChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Employee bob = employees.lookupEmployee(UnmarshallTest.BOB);
        
        // Make sure it is truly there
        Assert.assertNotNull(bob);  
        Assert.assertNotNull(locator.getService(Employee.class, UnmarshallTest.BOB));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, BOB_EMPLOYEE_INSTANCE));
        
        employees.removeEmployee(UnmarshallTest.BOB);
        
        bob = employees.lookupEmployee(UnmarshallTest.BOB);
        
        Assert.assertNull(bob);
        Assert.assertNull(locator.getService(Employee.class, UnmarshallTest.BOB));
        Assert.assertNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, BOB_EMPLOYEE_INSTANCE));
    }
    
    /**
     * Tests remove of an un-keyed child with no sub-children
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfIndexedChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(ACME3_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        validateAcme3InitialState(employees, hub);
        
        // Make sure bad indexes do not change anything.  Bad index negative
        Assert.assertFalse(employees.removeOtherData(-1));
        validateAcme3InitialState(employees, hub);
        
        // Bad index too high
        Assert.assertFalse(employees.removeOtherData(4));
        validateAcme3InitialState(employees, hub);
        
        Assert.assertTrue(employees.removeOtherData(2));
        
        List<OtherData> otherDatum = employees.getOtherData();
        Assert.assertEquals(3, otherDatum.size());
        
        Assert.assertEquals(INDEX0, otherDatum.get(0).getData());
        Assert.assertEquals(INDEX1, otherDatum.get(1).getData());
        // Index 2 was removed!
        Assert.assertEquals(INDEX3, otherDatum.get(2).getData());
        
        checkHubDataInstanceOrder(hub, INDEX0, INDEX1, INDEX3);
        
        // Remove the last thing in the list
        Assert.assertTrue(employees.removeOtherData(2));
        otherDatum = employees.getOtherData();
        
        Assert.assertEquals(INDEX0, otherDatum.get(0).getData());
        Assert.assertEquals(INDEX1, otherDatum.get(1).getData());
        // Index 2 was removed!
        
        checkHubDataInstanceOrder(hub, INDEX0, INDEX1);
        
        // Remove the first thing in the list
        Assert.assertTrue(employees.removeOtherData(0));
        otherDatum = employees.getOtherData();
        
        Assert.assertEquals(INDEX1, otherDatum.get(0).getData());
        
        checkHubDataInstanceOrder(hub, INDEX1);
        
        // Remove the last thing in the list
        Assert.assertTrue(employees.removeOtherData(0));
        otherDatum = employees.getOtherData();
        
        Assert.assertTrue(otherDatum.isEmpty());
        
        checkHubDataInstanceOrder(hub);
        
        // Make sure we can remove something we've added
        employees.addOtherData(0);
        otherDatum = employees.getOtherData();
        OtherData zeroEntry = otherDatum.get(0);
        zeroEntry.setData(INDEX0);
        
        checkHubDataInstanceOrder(hub, INDEX0);
        
        Assert.assertTrue(employees.removeOtherData(0));
        otherDatum = employees.getOtherData();
        
        Assert.assertTrue(otherDatum.isEmpty());
        
        checkHubDataInstanceOrder(hub);
    }
    
    /**
     * Tests remove of a direct child without children
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfDirectNodeWithoutChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(ACME3_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        validateAcme3InitialState(employees, hub);
        
        Financials removed = employees.removeFinancials();
        Assert.assertNotNull(removed);
        
        Assert.assertNull(employees.getFinancials());
        
        BeanDatabase db = hub.getCurrentDatabase();
        Instance financialsInstance = db.getInstance(UnmarshallTest.FINANCIALS_TYPE, UnmarshallTest.FINANCIALS_INSTANCE);
        Assert.assertNull(financialsInstance);
    }
    
    /**
     * Tests removing a direct node that has children of its own
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfDirectNodeWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub);
        
        DomainBean domain = rootHandle.getRoot();
        
        Assert.assertTrue(domain.removeSecurityManager());
        
        Assert.assertNull(domain.getSecurityManager());
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type securityManagerType = db.getType(MergeTest.SECURITY_MANAGER_TYPE);
            Assert.assertNotNull(securityManagerType);
        
            Map<String, Instance> securityManagerInstances = securityManagerType.getInstances();
            Assert.assertNotNull(securityManagerInstances);
            Assert.assertTrue(securityManagerInstances.isEmpty());
        }
        
        {
            Type atzProvidersType = db.getType(MergeTest.AUTHORIZATION_PROVIDER_TYPE);
            Assert.assertNotNull(atzProvidersType);
            
            Map<String, Instance> atzProvidersInstances = atzProvidersType.getInstances();
            Assert.assertNotNull(atzProvidersInstances);
            Assert.assertTrue(atzProvidersInstances.isEmpty());
        }
    }
    
    /**
     * Tests removing a child instance represented by arrays that
     * itself has children of its own
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testRemoveOfArrayNodeWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub);
        
        DomainBean domain = rootHandle.getRoot();
        
        JMSServerBean removed = domain.removeJMSServer(MergeTest.CAROL_NAME);
        Assert.assertNotNull(removed);
        Assert.assertEquals(MergeTest.CAROL_NAME, removed.getName());
        
        JMSServerBean jmsServers[] = domain.getJMSServers();
        Assert.assertNotNull(jmsServers);
        
        Assert.assertEquals(1, jmsServers.length);
        Assert.assertEquals(MergeTest.DAVE_NAME, jmsServers[0].getName());
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type jmsServerType = db.getType(MergeTest.JMS_SERVER_TYPE);
            Assert.assertNotNull(jmsServerType);
        
            Map<String, Instance> jmsServerInstances = jmsServerType.getInstances();
            Assert.assertNotNull(jmsServerInstances);
            Assert.assertEquals(1, jmsServerInstances.size());
            
            Instance daveInstance = jmsServerInstances.get(MergeTest.DAVE_INSTANCE);
            Assert.assertNotNull(daveInstance);
            
            Map<String, Object> daveMap = (Map<String, Object>) daveInstance.getBean();
            Assert.assertEquals(MergeTest.DAVE_NAME, daveMap.get(UnmarshallTest.NAME_TAG));
        }
        
        {
            Type queueType = db.getType(MergeTest.QUEUE_TYPE);
            Assert.assertNotNull(queueType);
            
            Map<String, Instance> queueInstances = queueType.getInstances();
            Assert.assertNotNull(queueInstances);
            Assert.assertEquals(1, queueInstances.size());
            
            Instance queueInstance = queueInstances.get(QUEUED0_INSTANCE_NAME);
            Assert.assertNotNull(queueInstance);
            
            Map<String, Object> beanLikeQueue = (Map<String, Object>) queueInstance.getBean();
            Assert.assertEquals(MergeTest.QUEUED0_NAME, beanLikeQueue.get(UnmarshallTest.NAME_TAG));
        }
        
        {
            Type topicType = db.getType(MergeTest.TOPIC_TYPE);
            Assert.assertNotNull(topicType);
            
            Map<String, Instance> topicInstances = topicType.getInstances();
            Assert.assertNotNull(topicInstances);
            Assert.assertEquals(1, topicInstances.size());
            
            Instance topicInstance = topicInstances.get(TOPICD0_INSTANCE_NAME);
            Assert.assertNotNull(topicInstance);
            
            Map<String, Object> beanLikeQueue = (Map<String, Object>) topicInstance.getBean();
            Assert.assertEquals(MergeTest.TOPICD0_NAME, beanLikeQueue.get(UnmarshallTest.NAME_TAG));
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void validateAcme3InitialState(Employees employees, Hub hub) {
        Assert.assertEquals(UnmarshallTest.ACME, employees.getCompanyName());
        
        Financials financials = employees.getFinancials();
        Assert.assertNotNull(financials);
        
        Assert.assertEquals(UnmarshallTest.ACME_SYMBOL, financials.getSymbol());
        Assert.assertEquals(UnmarshallTest.NYSE, financials.getExchange());
        Assert.assertNull(financials.getCountry());
        
        List<OtherData> otherDatum = employees.getOtherData();
        Assert.assertEquals(4, otherDatum.size());
        
        Assert.assertEquals(INDEX0, otherDatum.get(0).getData());
        Assert.assertEquals(INDEX1, otherDatum.get(1).getData());
        Assert.assertEquals(INDEX2, otherDatum.get(2).getData());
        Assert.assertEquals(INDEX3, otherDatum.get(3).getData());
        
        checkHubDataInstanceOrder(hub, INDEX0, INDEX1, INDEX2, INDEX3);
        
        // Check that financials is in the hub
        BeanDatabase db = hub.getCurrentDatabase();
        Instance financialsInstance = db.getInstance(UnmarshallTest.FINANCIALS_TYPE, UnmarshallTest.FINANCIALS_INSTANCE);
        Assert.assertNotNull(financialsInstance);
        
        Map<String, Object> financialsBean = (Map<String, Object>) financialsInstance.getBean();
        Assert.assertEquals(UnmarshallTest.ACME_SYMBOL, financialsBean.get(UnmarshallTest.SYMBOL_TAG));
        Assert.assertEquals(UnmarshallTest.NYSE, financialsBean.get(UnmarshallTest.EXCHANGE_TAG));
    }
    
    @SuppressWarnings("unchecked")
    private static void checkHubDataInstanceOrder(Hub hub, String... expecteds) {
        BeanDatabase db = hub.getCurrentDatabase();
        Type otherDataType = db.getType(OTHER_DATA_TYPE);
        
        Set<String> hashedExpecteds = new HashSet<String>();
        for (String expected : expecteds) {
            hashedExpecteds.add(expected);
        }
        
        Map<String, Instance> instances = otherDataType.getInstances();
        Assert.assertEquals(expecteds.length, instances.size());
        Assert.assertEquals(expecteds.length, hashedExpecteds.size());
        
        for (Instance instance : instances.values()) {
            Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
            String data = (String) beanLikeMap.get(DATA_KEY);
            
            Assert.assertTrue(hashedExpecteds.contains(data));
        }
        
    }

}
