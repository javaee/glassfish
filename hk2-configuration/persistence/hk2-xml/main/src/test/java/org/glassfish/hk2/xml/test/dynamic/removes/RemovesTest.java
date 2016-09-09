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
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Employee;
import org.glassfish.hk2.xml.test.basic.beans.Employees;
import org.glassfish.hk2.xml.test.basic.beans.Financials;
import org.glassfish.hk2.xml.test.basic.beans.OtherData;
import org.glassfish.hk2.xml.test.beans.AuthorizationProviderBean;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.HttpFactoryBean;
import org.glassfish.hk2.xml.test.beans.HttpServerBean;
import org.glassfish.hk2.xml.test.beans.HttpsFactoryBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.beans.QueueBean;
import org.glassfish.hk2.xml.test.beans.SecurityManagerBean;
import org.glassfish.hk2.xml.test.beans.TopicBean;
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
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Employee bob = employees.lookupEmployee(Commons.BOB);
        
        // Make sure it is truly there
        Assert.assertNotNull(bob);  
        Assert.assertNotNull(locator.getService(Employee.class, Commons.BOB));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, BOB_EMPLOYEE_INSTANCE));
        
        employees.removeEmployee(Commons.BOB);
        
        bob = employees.lookupEmployee(Commons.BOB);
        
        Assert.assertNull(bob);
        Assert.assertNull(locator.getService(Employee.class, Commons.BOB));
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
        Instance financialsInstance = db.getInstance(Commons.FINANCIALS_TYPE, Commons.FINANCIALS_INSTANCE);
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
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
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
            
            Assert.assertNull(locator.getService(SecurityManagerBean.class));
        }
        
        {
            Type atzProvidersType = db.getType(MergeTest.AUTHORIZATION_PROVIDER_TYPE);
            Assert.assertNotNull(atzProvidersType);
            
            Map<String, Instance> atzProvidersInstances = atzProvidersType.getInstances();
            Assert.assertNotNull(atzProvidersInstances);
            Assert.assertTrue(atzProvidersInstances.isEmpty());
            
            Assert.assertNull(locator.getService(AuthorizationProviderBean.class, MergeTest.RSA_ATZ_PROV_NAME));
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
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
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
            Assert.assertEquals(MergeTest.DAVE_NAME, daveMap.get(Commons.NAME_TAG));
            
            Assert.assertNull(locator.getService(JMSServerBean.class, MergeTest.CAROL_NAME));
            
            JMSServerBean daveJMS = locator.getService(JMSServerBean.class, MergeTest.DAVE_NAME);
            Assert.assertNotNull(daveJMS);
            Assert.assertEquals(MergeTest.DAVE_NAME, daveJMS.getName());
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
            Assert.assertEquals(MergeTest.QUEUED0_NAME, beanLikeQueue.get(Commons.NAME_TAG));
            
            Assert.assertNull(locator.getService(QueueBean.class, MergeTest.QUEUE0_NAME));
            Assert.assertNull(locator.getService(QueueBean.class, MergeTest.QUEUE1_NAME));
            Assert.assertNull(locator.getService(QueueBean.class, MergeTest.QUEUE2_NAME));
            
            MergeTest.assertQueueOfName(locator, MergeTest.QUEUED0_NAME);
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
            Assert.assertEquals(MergeTest.TOPICD0_NAME, beanLikeQueue.get(Commons.NAME_TAG));
            
            Assert.assertNull(locator.getService(TopicBean.class, MergeTest.TOPIC0_NAME));
            Assert.assertNull(locator.getService(TopicBean.class, MergeTest.TOPIC1_NAME));
            
            MergeTest.assertTopicOfName(locator, MergeTest.TOPICD0_NAME);
        }
    }
    
    /**
     * Removes an unkeyed child that has children of its own
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testRemoveOfUnkeyedListChildWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        List<HttpFactoryBean> factories = domain.getHTTPFactories();
        
        HttpFactoryBean killMe = null;
        for (HttpFactoryBean factory : factories) {
            if (factory.getNonKeyIdentifier().equals(MergeTest.GLENDOLA_NAME)) {
                killMe = factory;
            }
        }
        
        Assert.assertNotNull(killMe);
        
        // This is the test
        HttpFactoryBean killed = domain.removeHTTPFactory(killMe);
        Assert.assertEquals(killMe, killed);
        
        factories = domain.getHTTPFactories();
        Assert.assertEquals(1, factories.size());
        
        for (HttpFactoryBean factory : factories) {
            Assert.assertEquals(MergeTest.ESSEX_NAME, factory.getNonKeyIdentifier());
            
            List<HttpServerBean> servers = factory.getHttpServers();
            Assert.assertEquals(1, servers.size());
            
            for (HttpServerBean server : servers) {
                Assert.assertEquals(MergeTest.FAIRVIEW_NAME, server.getName());
                Assert.assertEquals(1234, server.getPort());
            }
        }
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type httpFactoryType = db.getType(MergeTest.HTTP_FACTORY_TYPE);
            Assert.assertNotNull(httpFactoryType);
        
            Map<String, Instance> httpFactoryInstances = httpFactoryType.getInstances();
            Assert.assertNotNull(httpFactoryInstances);
            Assert.assertEquals(1, httpFactoryInstances.size());
            
            Instance essexInstance = null;
            for (Instance i : httpFactoryInstances.values()) {
                essexInstance = i;
            }
            Assert.assertNotNull(essexInstance);
            
            Map<String, Object> essexMap = (Map<String, Object>) essexInstance.getBean();
            Assert.assertEquals(MergeTest.ESSEX_NAME, essexMap.get(Commons.NON_KEY_TAG));
            
            List<HttpFactoryBean> asServices = locator.getAllServices(HttpFactoryBean.class);
            Assert.assertEquals(1, asServices.size());
            
            HttpFactoryBean foundBean = null;
            for (HttpFactoryBean a : asServices) {
                foundBean = a;
            }
            Assert.assertNotNull(foundBean);
            
            Assert.assertEquals(MergeTest.ESSEX_NAME, foundBean.getNonKeyIdentifier());
        }
        
        {
            Type httpServerType = db.getType(MergeTest.HTTP_SERVER_TYPE);
            Assert.assertNotNull(httpServerType);
            
            Map<String, Instance> httpServerInstances = httpServerType.getInstances();
            Assert.assertNotNull(httpServerInstances);
            Assert.assertEquals(1, httpServerInstances.size());
            
            Instance httpServerInstance = null;
            for (Instance a : httpServerInstances.values()) {
                httpServerInstance = a;
            }
            Assert.assertNotNull(httpServerInstance);
            
            Map<String, Object> beanLike = (Map<String, Object>) httpServerInstance.getBean();
            Assert.assertEquals(MergeTest.FAIRVIEW_NAME, beanLike.get(Commons.NAME_TAG));
            Assert.assertEquals(new Integer(1234), beanLike.get(Commons.PORT_TAG));
            
            Assert.assertNull(locator.getService(HttpServerBean.class, MergeTest.HOLYOKE_NAME));
            Assert.assertNull(locator.getService(HttpServerBean.class, MergeTest.IROQUIS_NAME));
            
            HttpServerBean hsb = locator.getService(HttpServerBean.class, MergeTest.FAIRVIEW_NAME);
            Assert.assertNotNull(hsb);
            
            Assert.assertEquals(MergeTest.FAIRVIEW_NAME, hsb.getName());
            Assert.assertEquals(1234, hsb.getPort());
        }
    }
    
    /**
     * Removes an unkeyed child that has children of its own
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfUnkeyedArrayChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        HttpsFactoryBean factories[] = domain.getHTTPSFactories();
        HttpsFactoryBean factory = factories[0];
        
        // This is the test
        domain.removeHTTPSFactory(factory);
        
        factories = domain.getHTTPSFactories();
        Assert.assertEquals(0, factories.length);
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type httpFactoryType = db.getType(MergeTest.HTTPS_FACTORY_TYPE);
            Assert.assertNotNull(httpFactoryType);
        
            Map<String, Instance> httpFactoryInstances = httpFactoryType.getInstances();
            Assert.assertNotNull(httpFactoryInstances);
            Assert.assertEquals(0, httpFactoryInstances.size());
        }
        
        Assert.assertNull(locator.getService(HttpsFactoryBean.class));
    }
    
    /**
     * Removes a keyed child from an unkeyed parent (list)
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testRemoveOfKeyedChildOfUnkeyedParent() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        HttpFactoryBean glendolaFactory = domain.getHTTPFactories().get(1);
        glendolaFactory.removeHttpServer(MergeTest.IROQUIS_NAME);
        
        List<HttpServerBean> glendolaServers = glendolaFactory.getHttpServers();
        Assert.assertEquals(1, glendolaServers.size());
        
        for (HttpServerBean one : glendolaServers) {
            Assert.assertEquals(MergeTest.HOLYOKE_NAME, one.getName());
            Assert.assertEquals(5678, one.getPort());
        }
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type httpServerType = db.getType(MergeTest.HTTP_SERVER_TYPE);
            Assert.assertNotNull(httpServerType);
        
            Map<String, Instance> httpServerInstances = httpServerType.getInstances();
            Assert.assertNotNull(httpServerInstances);
            Assert.assertEquals(2, httpServerInstances.size());
            
            Instance fairviewInstance = null;
            Instance holyokeInstance = null;
            for (Instance value : httpServerInstances.values()) {
                Map<String, Object> bean = (Map<String, Object>) value.getBean();
                Assert.assertNotNull(bean);
                
                String httpServerName = (String) bean.get(Commons.NAME_TAG);
                if (MergeTest.FAIRVIEW_NAME.equals(httpServerName)) {
                    fairviewInstance = value;
                }
                else if (MergeTest.HOLYOKE_NAME.equals(httpServerName)) {
                    holyokeInstance = value;
                }
                else {
                    Assert.fail("After deletion the instance should not be there " + httpServerName);
                }
            }
            Assert.assertNotNull(fairviewInstance);
            Assert.assertNotNull(holyokeInstance);
        }
        
        Assert.assertNull(locator.getService(HttpServerBean.class, MergeTest.IROQUIS_NAME));
    }
    
    /**
     * Removes an unkeyed child that has children of its own with an index
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testRemoveOfUnkeyedArrayChildUsingIndex() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        // This is the test
        domain.removeHTTPSFactory(0);
        
        HttpsFactoryBean factories[] = domain.getHTTPSFactories();
        Assert.assertEquals(0, factories.length);
        
        BeanDatabase db = hub.getCurrentDatabase();
        
        {
            Type httpFactoryType = db.getType(MergeTest.HTTPS_FACTORY_TYPE);
            Assert.assertNotNull(httpFactoryType);
        
            Map<String, Instance> httpFactoryInstances = httpFactoryType.getInstances();
            Assert.assertNotNull(httpFactoryInstances);
            Assert.assertEquals(0, httpFactoryInstances.size());
        }
        
        Assert.assertNull(locator.getService(HttpsFactoryBean.class));
    }
    
    @SuppressWarnings("unchecked")
    private static void validateAcme3InitialState(Employees employees, Hub hub) {
        Assert.assertEquals(Commons.ACME, employees.getCompanyName());
        
        Financials financials = employees.getFinancials();
        Assert.assertNotNull(financials);
        
        Assert.assertEquals(Commons.ACME_SYMBOL, financials.getSymbol());
        Assert.assertEquals(Commons.NYSE, financials.getExchange());
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
        Instance financialsInstance = db.getInstance(Commons.FINANCIALS_TYPE, Commons.FINANCIALS_INSTANCE);
        Assert.assertNotNull(financialsInstance);
        
        Map<String, Object> financialsBean = (Map<String, Object>) financialsInstance.getBean();
        Assert.assertEquals(Commons.ACME_SYMBOL, financialsBean.get(Commons.SYMBOL_TAG));
        Assert.assertEquals(Commons.NYSE, financialsBean.get(Commons.EXCHANGE_TAG));
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
