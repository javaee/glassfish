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
package org.glassfish.hk2.xml.test.dynamic.merge;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Museum;
import org.glassfish.hk2.xml.test.beans.AuthorizationProviderBean;
import org.glassfish.hk2.xml.test.beans.DiagnosticsBean;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.HttpFactoryBean;
import org.glassfish.hk2.xml.test.beans.HttpServerBean;
import org.glassfish.hk2.xml.test.beans.HttpsFactoryBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.beans.MachineBean;
import org.glassfish.hk2.xml.test.beans.QueueBean;
import org.glassfish.hk2.xml.test.beans.SSLManagerBean;
import org.glassfish.hk2.xml.test.beans.SecurityManagerBean;
import org.glassfish.hk2.xml.test.beans.ServerBean;
import org.glassfish.hk2.xml.test.beans.TopicBean;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class MergeTest {
    public final static String DOMAIN1_FILE = "domain1.xml";
    
    private final static String DOMAIN1_NAME = "domain1";
    public final static String RSA_ATZ_PROV_NAME = "RSA";
    private final static String RSA_DOM_PFX = "rsa";
    private final static String ALICE_NAME = "Alice";
    private final static String BOB_NAME = "Bob";
    public final static String CAROL_NAME = "Carol";
    public final static String DAVE_NAME = "Dave";
    public final static String EDDIE_NAME = "Eddie";
    
    public final static String TOPIC0_NAME = "Topic0";
    public final static String TOPIC1_NAME = "Topic1";
    public final static String TOPIC2_NAME = "Topic2";
    public final static String QUEUE0_NAME = "Queue0";
    public final static String QUEUE1_NAME = "Queue1";
    public final static String QUEUE2_NAME = "Queue2";
    
    public final static String TOPICD0_NAME = "TopicD0";
    public final static String QUEUED0_NAME = "QueueD0";
    
    public final static String ESSEX_NAME = "Essex";
    public final static String FAIRVIEW_NAME = "Fairview";
    public final static String GLENDOLA_NAME = "Glendola";
    public final static String HOLYOKE_NAME = "Holyoke";
    public final static String IROQUIS_NAME = "Iroquis";
    public final static String LIBERTY_NAME = "Liberty";
    
    private final static String ALICE_ADDRESS = "10.0.0.1";
    private final static String ALICE_SERVER0_NAME = "Server-0";
    private final static int ALICE_SERVER0_PORT = 12345;
    private final static String DEFAULT_SUBNET = "0.0.0.255";
    public final static String SERVER1_NAME = "Server-1";
    
    public final static String DOMAIN_TYPE = "/domain";
    public final static String DOMAIN_INSTANCE = "domain";
    public final static String MACHINE_TYPE = "/domain/machine";
    public final static String SERVER_TYPE = "/domain/machine/server";
    public final static String SECURITY_MANAGER_TYPE = "/domain/security-manager";
    public final static String AUTHORIZATION_PROVIDER_TYPE = "/domain/security-manager/authorization-provider";
    public final static String JMS_SERVER_TYPE = "/domain/jms-server";
    public final static String TOPIC_TYPE = "/domain/jms-server/topic";
    public final static String QUEUE_TYPE = "/domain/jms-server/queue";
    public final static String HTTP_FACTORY_TYPE = "/domain/http-factory";
    public final static String HTTP_SERVER_TYPE = "/domain/http-factory/http-server";
    public final static String HTTPS_FACTORY_TYPE = "/domain/https-factory";
    public final static String SSL_MANAGER_TYPE = "/domain/security-manager/ssl-manager";
    public final static String DIAGNOSTICS_TYPE = "/domain/diagnostics";
    
    private final static String ALICE_INSTANCE = "domain.Alice";
    private final static String BOB_INSTANCE = "domain.Bob";
    public final static String DAVE_INSTANCE = "domain.Dave";
    public final static String EDDIE_INSTANCE = "domain.Eddie";
    private final static String SERVER0_INSTANCE = "domain.Alice.Server-0";
    public final static String SERVER1_INSTANCE = "domain.Eddie.Server-1";
    public final static String SECURITY_MANAGER_INSTANCE = "domain.security-manager";
    private final static String RSA_INSTANCE = "domain.security-manager.RSA";
    public final static String JMS_SERVER_CAROL_INSTANCE = "domain.Carol";
    private final static String TOPIC0_INSTANCE = "domain.Carol.Topic0";
    private final static String TOPIC1_INSTANCE = "domain.Carol.Topic1";
    private final static String TOPIC2_INSTANCE = "domain.Bob.Topic2";
    private final static String QUEUE0_INSTANCE = "domain.Carol.Queue0";
    private final static String QUEUE0_BOB_INSTANCE = "domain.Bob.Queue0";
    private final static String QUEUE1_INSTANCE = "domain.Carol.Queue1";
    private final static String QUEUE2_INSTANCE = "domain.Carol.Queue2";
    public final static String SSL_MANAGER_INSTANCE_NAME = "domain.security-manager.ssl-manager";
    public final static String DIAGNOSTICS_INSTANCE = "domain.diagnostics";
    
    private final static String ADDRESS_TAG = "address";
    private final static String PORT_TAG = "port";
    private final static String ATZ_DOMAIN_PFX_TAG = "domain-pfx";
    public final static String SUBNET_TAG = "subnetwork";
    public final static String TAXONOMY_TAG = "taxonomy";
    public final static String COMPRESSION_TAG = "compression-algorithm";
    
    public final static String LZ_COMPRESSION = "LZ";
    
    /**
     * Modifies two properties with one transaction in a merge
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    @org.junit.Ignore
    public void testMergeModifyTwoPropertiesOneTransaction() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshal(url.toURI(), Museum.class);
        
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        // All above just verifying the pre-state
        XmlRootCopy<Museum> copy = rootHandle.getXmlRootCopy();
        Museum museumCopy = copy.getChildRoot();
        Museum museumOld = rootHandle.getRoot();
        
        museumCopy.setAge(RawSetsTest.ONE_OH_ONE_INT);  // getting younger?
        museumCopy.setId(RawSetsTest.ONE_OH_ONE_INT);  // different from original!
        
        // Ensure that the modification of the copy did NOT affect the parent!
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        // Now do the merge
        copy.merge();
        
        // Now make sure new values show up
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museumOld.getId());
        Assert.assertEquals(Commons.BEN_FRANKLIN, museumOld.getName());
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museumOld.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(RawSetsTest.MUSEUM_TYPE, RawSetsTest.MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(Commons.BEN_FRANKLIN, beanLikeMap.get(Commons.NAME_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(Commons.ID_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(RawSetsTest.AGE_TAG));  // The test
        
        List<Change> changes = listener.getChanges();
        Assert.assertNotNull(changes);
        
        Assert.assertEquals(2, changes.size());
        
        boolean gotId = false;
        boolean gotAge = false;
        for (Change change : changes) {
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
            
            List<PropertyChangeEvent> events = change.getModifiedProperties();
            
            Assert.assertEquals(1, events.size());
            
            for (PropertyChangeEvent event : events) {
                if ("age".equals(event.getPropertyName())) {
                    gotAge = true;
                }
                else if ("id".equals(event.getPropertyName())) {
                    gotId = true;
                }
            }
        }
        
        Assert.assertTrue(gotId);
        Assert.assertTrue(gotAge);
    }
    
    /**
     * Adds a list child
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test 
    @org.junit.Ignore
    public void testMergeModifyAddAListChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOld = rootHandle.getRoot();
        
        MachineBean addedBean = xmlService.createBean(MachineBean.class);
        addedBean.setName(BOB_NAME);
        
        domainCopy.addMachine(addedBean);
        
        // Ensure that the modification of the copy did NOT affect the parent!
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // Now do the merge
        copy.merge();
        
        // Now make sure new values show up
        List<MachineBean> machines = domainOld.getMachines();
        Assert.assertEquals(2, machines.size());
        
        boolean foundAlice = false;
        boolean foundBob = false;
        for (MachineBean machine : machines) {
            if (machine.getName().equals(BOB_NAME)) {
                if (foundBob) {
                    Assert.fail("There were more than one bob in the list of machines: " + machines);
                }
                foundBob = true;
            }
            else if (machine.getName().equals(ALICE_NAME)) {
                if (foundAlice) {
                    Assert.fail("There were more than one alice in the list of machines: " + machines);
                }
                foundAlice = true;
            }
        }
        
        Assert.assertTrue("Added child was not found: " + machines, foundBob);
        Assert.assertTrue("Existing child was not found: " + machines, foundAlice);
        
        {
            // Check hub for bob
            Instance machineBobInstance = hub.getCurrentDatabase().getInstance(MACHINE_TYPE, BOB_INSTANCE);
            Assert.assertNotNull(machineBobInstance);
            
            Map<String, Object> bobMap = (Map<String, Object>) machineBobInstance.getBean();
            Assert.assertEquals(BOB_NAME, bobMap.get(Commons.NAME_TAG));
            Assert.assertNull(bobMap.get(ADDRESS_TAG));
        }
        
        List<Change> hubChanges = listener.getChanges();
        Assert.assertEquals(2, hubChanges.size());
        
        boolean foundAdd = false;
        boolean foundMod = false;
        for (Change change : hubChanges) {
            if (ChangeCategory.ADD_INSTANCE.equals(change.getChangeCategory())) {
                Assert.assertEquals(BOB_INSTANCE, change.getInstanceKey());
                foundAdd = true;
            }
            else if (ChangeCategory.MODIFY_INSTANCE.equals(change.getChangeCategory())) {
                Assert.assertEquals(DOMAIN_INSTANCE, change.getInstanceKey());
                foundMod = true;
            }
            else {
                Assert.fail("Unknown change found: " + change);
            }
        }
        
        Assert.assertTrue(foundAdd);
        Assert.assertTrue(foundMod);
    }
    
    /**
     * Adds a array child
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test 
    @org.junit.Ignore
    public void testMergeModifyAddAnArrayChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOld = rootHandle.getRoot();
        
        JMSServerBean addedBean = xmlService.createBean(JMSServerBean.class);
        addedBean.setName(BOB_NAME);
        
        TopicBean addedTopic = xmlService.createBean(TopicBean.class);
        addedTopic.setName(TOPIC2_NAME);
        addedBean.addTopic(addedTopic);
        
        QueueBean addedQueue = xmlService.createBean(QueueBean.class);
        addedQueue.setName(QUEUE0_NAME);
        addedBean.addQueue(addedQueue);
        
        domainCopy.addJMSServer(addedBean);
        
        // Ensure that the modification of the copy did NOT affect the parent!
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // Now do the merge
        copy.merge();
        
        // Now make sure new values show up
        JMSServerBean jmsServers[] = domainOld.getJMSServers();
        Assert.assertEquals(3, jmsServers.length);
        
        boolean foundCarol = false;
        boolean foundDave = false;
        boolean foundBob = false;
        for (JMSServerBean jmsServer : jmsServers) {
            if (jmsServer.getName().equals(BOB_NAME)) {
                if (foundBob) {
                    Assert.fail("There were more than one bob in the list of machines: " + Arrays.toString(jmsServers));
                }
                foundBob = true;
                
                Assert.assertNotNull(jmsServer.lookupTopic(TOPIC2_NAME));
                Assert.assertNotNull(jmsServer.lookupQueue(QUEUE0_NAME));
            }
            else if (jmsServer.getName().equals(CAROL_NAME)) {
                if (foundCarol) {
                    Assert.fail("There were more than one alice in the list of machines: " + Arrays.toString(jmsServers));
                }
                foundCarol = true;
            }
            else if (jmsServer.getName().equals(DAVE_NAME)) {
                if (foundDave) {
                    Assert.fail("There were more than one alice in the list of machines: " + Arrays.toString(jmsServers));
                }
                foundDave = true;
            }
            else {
                Assert.fail("Unknown JMSServer: " + jmsServer);
            }
        }
        
        Assert.assertTrue("Added child was not found: " + Arrays.toString(jmsServers), foundBob);
        Assert.assertTrue("Existing child was not found: " + Arrays.toString(jmsServers), foundCarol);
        Assert.assertTrue("Existing child was not found: " + Arrays.toString(jmsServers), foundDave);
        
        {
            // Check hub for bob
            Instance machineBobInstance = hub.getCurrentDatabase().getInstance(JMS_SERVER_TYPE, BOB_INSTANCE);
            Assert.assertNotNull(machineBobInstance);
            
            Map<String, Object> bobMap = (Map<String, Object>) machineBobInstance.getBean();
            Assert.assertEquals(BOB_NAME, bobMap.get(Commons.NAME_TAG));
            Assert.assertNull(bobMap.get(ADDRESS_TAG));
        }
        
        {
            // Check hub for topic2
            Instance topic2Instance = hub.getCurrentDatabase().getInstance(TOPIC_TYPE, TOPIC2_INSTANCE);
            Assert.assertNotNull(topic2Instance);
            
            Map<String, Object> topic2Map = (Map<String, Object>) topic2Instance.getBean();
            Assert.assertEquals(TOPIC2_NAME, topic2Map.get(Commons.NAME_TAG));
        }
        
        {
            // Check hub for bob's Queue0
            Instance queue0Instance = hub.getCurrentDatabase().getInstance(QUEUE_TYPE, QUEUE0_BOB_INSTANCE);
            Assert.assertNotNull(queue0Instance);
            
            Map<String, Object> queue0Map = (Map<String, Object>) queue0Instance.getBean();
            Assert.assertEquals(QUEUE0_NAME, queue0Map.get(Commons.NAME_TAG));
        }
        
        List<Change> hubChanges = listener.getChanges();
        Assert.assertEquals("Changes=" + hubChanges, 6, hubChanges.size());
        
        for (int lcv = 0; lcv < 6; lcv++) {
            Change change = hubChanges.get(lcv);
            
            switch(lcv) {
            case 0:
                Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(BOB_INSTANCE, change.getInstanceKey());
                break;
            case 1:
                Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(TOPIC2_INSTANCE, change.getInstanceKey());
                break;
            case 2:
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(BOB_INSTANCE, change.getInstanceKey());
                break;
            case 3:
                Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(QUEUE0_BOB_INSTANCE, change.getInstanceKey());
                break;
            case 4:
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(BOB_INSTANCE, change.getInstanceKey());
                break;
            case 5:
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(DOMAIN_INSTANCE, change.getInstanceKey());
                break;
            default:
                Assert.fail("Too many changes: " + lcv + " change=" + change);
            }
        }
    }
    
    /**
     * Removes a direct child (and all children underneath)
     * 
     * @throws Exception
     */
    @Test 
    @org.junit.Ignore
    public void testRemoveDirectChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOld = rootHandle.getRoot();
        
        Assert.assertTrue(domainCopy.removeSecurityManager());
        
        // Ensure that the modification of the copy did NOT affect the parent!
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // Now do the merge
        copy.merge();
        
        // Now make sure old values are gone
        Assert.assertNull(domainOld.getSecurityManager());
        
        assertNotInHub(hub, SECURITY_MANAGER_TYPE, SECURITY_MANAGER_INSTANCE);
        assertNotInHub(hub, AUTHORIZATION_PROVIDER_TYPE, RSA_INSTANCE);
        
        List<Change> hubChanges = listener.getChanges();
        Assert.assertEquals("Did not get expected changes, got " + hubChanges, 3, hubChanges.size());
        
        for (int lcv = 0; lcv < hubChanges.size(); lcv++) {
            Change change = hubChanges.get(lcv);
            
            switch (lcv) {
            case 0:
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(DOMAIN_INSTANCE, change.getInstanceKey());
                break;
            case 1:
                Assert.assertEquals(ChangeCategory.REMOVE_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(SECURITY_MANAGER_INSTANCE, change.getInstanceKey());
                break;
            case 2:
                Assert.assertEquals(ChangeCategory.REMOVE_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(RSA_INSTANCE, change.getInstanceKey());
                break;
            default:
                Assert.fail("Uknown change " + lcv + " was " + change);
            }
        }
        
    }
    
    /**
     * Adds a direct child (and all children underneath)
     * 
     * @throws Exception
     */
    @Test 
    @org.junit.Ignore
    public void testAddDirectChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOld = rootHandle.getRoot();
        
        DiagnosticsBean diagnostics = xmlService.createBean(DiagnosticsBean.class);
        diagnostics.setName(ESSEX_NAME);
        
        domainCopy.setDiagnostics(diagnostics);
        
        // Ensure that the modification of the copy did NOT affect the parent!
        verifyDomain1Xml(rootHandle, hub, locator);
        
        // Now do the merge
        copy.merge();
        
        // Now make sure new value is available
        diagnostics = domainOld.getDiagnostics();
        Assert.assertNotNull(diagnostics);
        
        assertNameOnlyBean(hub, DIAGNOSTICS_TYPE, DIAGNOSTICS_INSTANCE, ESSEX_NAME);
        
        List<Change> hubChanges = listener.getChanges();
        Assert.assertEquals("Did not get expected changes, got " + hubChanges, 3, hubChanges.size());
        
        for (int lcv = 0; lcv < hubChanges.size(); lcv++) {
            Change change = hubChanges.get(lcv);
            
            switch (lcv) {
            case 0:
                Assert.assertEquals(ChangeCategory.ADD_TYPE, change.getChangeCategory());
                Assert.assertEquals(DIAGNOSTICS_TYPE, change.getChangeType().getName());
                break;
            case 1:
                Assert.assertEquals(ChangeCategory.ADD_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(DIAGNOSTICS_INSTANCE, change.getInstanceKey());
                Assert.assertEquals(DIAGNOSTICS_TYPE, change.getChangeType().getName());
                break;
            case 2:
                Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
                Assert.assertEquals(DOMAIN_INSTANCE, change.getInstanceKey());
                Assert.assertEquals(DOMAIN_TYPE, change.getChangeType().getName());
                break;
            default:
                Assert.fail("Uknown change " + lcv + " was " + change);
            }
        }
        
    }
    
    public static void verifyDomain1Xml(XmlRootHandle<DomainBean> rootHandle, Hub hub, ServiceLocator locator, boolean didDefault) {
        DomainBean root = rootHandle.getRoot();
        
        verifyDomain1XmlDomain(root, hub, locator, didDefault);
    }
    
    public static void verifyDomain1Xml(XmlRootHandle<DomainBean> rootHandle, Hub hub, ServiceLocator locator) {
        DomainBean root = rootHandle.getRoot();
        
        verifyDomain1XmlDomain(root, hub, locator, false);
    }
    
    public static void verifyDomain1Xml(XmlRootHandle<DomainBean> original, XmlRootCopy<DomainBean> rootHandle, Hub hub,
            ServiceLocator locator) {
        Assert.assertEquals(original, rootHandle.getParent());
        
        DomainBean root = rootHandle.getChildRoot();
        
        verifyDomain1XmlDomain(root, hub, locator, false);
    }
    
    @SuppressWarnings("unchecked")
    private static void verifyDomain1XmlDomain(DomainBean root, Hub hub, ServiceLocator locator, boolean didDefault) {
        Assert.assertEquals("Failing bean is " + root, DOMAIN1_NAME, root.getName());
        
        Assert.assertEquals(DEFAULT_SUBNET, root.getSubnetwork());
        Assert.assertNull(root.getTaxonomy());
        
        SecurityManagerBean securityManager = root.getSecurityManager();
        Assert.assertNotNull(securityManager);
        
        List<AuthorizationProviderBean> atzProviders = securityManager.getAuthorizationProviders();
        Assert.assertNotNull(atzProviders);
        Assert.assertEquals(1, atzProviders.size());
        
        if (!didDefault) {
            Assert.assertNull(securityManager.getSSLManager());
        }
        else {
            SSLManagerBean sslManager = securityManager.getSSLManager();
            Assert.assertNotNull(sslManager);
            
            Assert.assertNull(sslManager.getPublicKeyLocation());
            Assert.assertEquals(SSLManagerBean.FORT_KNOX, sslManager.getSSLPrivateKeyLocation());
        }
        
        MachineBean aliceRef = null;
        for (AuthorizationProviderBean atzProvider : atzProviders) {
            Assert.assertEquals(RSA_ATZ_PROV_NAME, atzProvider.getName());
            Assert.assertEquals(RSA_DOM_PFX, atzProvider.getAtzDomainPrefix());
            aliceRef = atzProvider.getMachine();
        }
        Assert.assertNotNull(aliceRef);
        
        List<MachineBean> machines = root.getMachines();
        Assert.assertNotNull(machines);
        Assert.assertEquals(1, machines.size());
        
        MachineBean alice = null;
        ServerBean myReference = null;
        for (MachineBean machine : machines) {
            alice = machine;
            Assert.assertEquals(aliceRef, machine);
            
            Assert.assertEquals(ALICE_NAME, machine.getName());
            Assert.assertEquals(ALICE_ADDRESS, machine.getAddress());
            
            List<ServerBean> servers = machine.getServers();
            Assert.assertNotNull(servers);
            Assert.assertEquals(1, servers.size());
            
            for (ServerBean server : servers) {
                Assert.assertEquals(ALICE_SERVER0_NAME, server.getName());
                Assert.assertEquals(ALICE_SERVER0_PORT, server.getPort());
                myReference = server;
            }
        }
        
        JMSServerBean jmsServers[] = root.getJMSServers();
        Assert.assertEquals(2, jmsServers.length);
        
        int lcv = 0;
        for (JMSServerBean jmsServer : jmsServers) {
            if (lcv == 0) {
                Assert.assertEquals("Did not find name in " + jmsServer, CAROL_NAME, jmsServer.getName());
                
                ServerBean serverReference = jmsServer.getServer();
                Assert.assertEquals(myReference, serverReference);
            
                List<TopicBean> topics = jmsServer.getTopics();
                Assert.assertEquals(2, topics.size());
            
                Assert.assertEquals(TOPIC0_NAME, topics.get(0).getName());
                Assert.assertEquals(TOPIC1_NAME, topics.get(1).getName());
            
                QueueBean queues[] = jmsServer.getQueues();
                Assert.assertEquals(3, queues.length);
            
                Assert.assertEquals(QUEUE0_NAME, queues[0].getName());
                Assert.assertEquals(QUEUE1_NAME, queues[1].getName());
                Assert.assertEquals(QUEUE2_NAME, queues[2].getName());
                
                Assert.assertNull(jmsServer.getCompressionAlgorithm());
            }
            else if (lcv == 1) {
                Assert.assertEquals("Did not find name in " + jmsServer, DAVE_NAME, jmsServer.getName());
                
                List<TopicBean> topics = jmsServer.getTopics();
                Assert.assertEquals(1, topics.size());
            
                Assert.assertEquals(TOPICD0_NAME, topics.get(0).getName());
            
                QueueBean queues[] = jmsServer.getQueues();
                Assert.assertEquals(1, queues.length);
            
                Assert.assertEquals(QUEUED0_NAME, queues[0].getName());
                
                Assert.assertNull(jmsServer.getCompressionAlgorithm());
            }
            lcv++;
        }
        
        List<HttpFactoryBean> httpFactories = root.getHTTPFactories();
        Assert.assertEquals(2, httpFactories.size());
        
        String factory0Identifier = null;
        String factory1Identifier = null;
        
        lcv = 0;
        for (HttpFactoryBean httpFactory : httpFactories) {
            if (lcv == 0) {
                factory0Identifier = ((XmlHk2ConfigurationBean) httpFactory)._getInstanceName();
                
                Assert.assertEquals(ESSEX_NAME, httpFactory.getNonKeyIdentifier());
                
                List<HttpServerBean> httpServers = httpFactory.getHttpServers();
                Assert.assertEquals(1, httpServers.size());
                
                for (HttpServerBean httpServer : httpServers) {
                    Assert.assertEquals(FAIRVIEW_NAME, httpServer.getName());
                    Assert.assertEquals(1234, httpServer.getPort());
                }
            }
            else {
                factory1Identifier = ((XmlHk2ConfigurationBean) httpFactory)._getInstanceName();
                
                Assert.assertEquals(GLENDOLA_NAME, httpFactory.getNonKeyIdentifier());
                
                List<HttpServerBean> httpServers = httpFactory.getHttpServers();
                Assert.assertEquals(2, httpServers.size());
                
                int lcv2 = 0;
                for (HttpServerBean httpServer : httpServers) {
                    if (lcv2 == 0) {
                        Assert.assertEquals(HOLYOKE_NAME, httpServer.getName());
                        Assert.assertEquals(5678, httpServer.getPort());
                    }
                    else {
                        Assert.assertEquals(IROQUIS_NAME, httpServer.getName());
                        Assert.assertEquals(5679, httpServer.getPort());
                    }
                    lcv2++;
                }
                
            }
            
            lcv++;
        }
        
        HttpsFactoryBean httpsFactories[] = root.getHTTPSFactories();
        Assert.assertEquals(1, httpsFactories.length);
        
        Assert.assertEquals(LIBERTY_NAME, httpsFactories[0].getNonKeyIdentifier());
        
        String httpsInstanceName = ((XmlHk2ConfigurationBean) httpsFactories[0])._getInstanceName();
        
        
        if (hub == null || locator == null) return;
        
        // Below is the verification for the Hub versions of the beans
        {
            Instance domainInstance = hub.getCurrentDatabase().getInstance(DOMAIN_TYPE, DOMAIN_INSTANCE);
            Assert.assertNotNull(domainInstance);
        
            Map<String, Object> domainMap = (Map<String, Object>) domainInstance.getBean();
            Assert.assertNull(domainMap.get(SUBNET_TAG));
            Assert.assertNull(domainMap.get(TAXONOMY_TAG));
            
            Object domainInstanceMetadata = domainInstance.getMetadata();
            Assert.assertNotNull(domainInstanceMetadata);
            Assert.assertEquals(root, domainInstanceMetadata);
        }
        
        {
            Instance machineAliceInstance = hub.getCurrentDatabase().getInstance(MACHINE_TYPE, ALICE_INSTANCE);
            Assert.assertNotNull(machineAliceInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) machineAliceInstance.getBean();
            Assert.assertEquals(ALICE_NAME, aliceMap.get(Commons.NAME_TAG));
            Assert.assertEquals(ALICE_ADDRESS, aliceMap.get(ADDRESS_TAG));
            
            Object machineAliceInstanceMetadata = machineAliceInstance.getMetadata();
            Assert.assertNotNull(machineAliceInstanceMetadata);
            Assert.assertEquals(alice, machineAliceInstanceMetadata);
        }
        
        {
            Instance aliceServer0Instance = hub.getCurrentDatabase().getInstance(SERVER_TYPE, SERVER0_INSTANCE);
            Assert.assertNotNull(aliceServer0Instance);
            
            Map<String, Object> server0Map = (Map<String, Object>) aliceServer0Instance.getBean();
            Assert.assertEquals(ALICE_SERVER0_NAME, server0Map.get(Commons.NAME_TAG));
            Assert.assertEquals(ALICE_SERVER0_PORT, server0Map.get(PORT_TAG));
        }
        
        {
            Instance securityManagerInstance = hub.getCurrentDatabase().getInstance(SECURITY_MANAGER_TYPE, SECURITY_MANAGER_INSTANCE);
            Assert.assertNotNull(securityManagerInstance);
            
            Map<String, Object> securityManagerMap = (Map<String, Object>) securityManagerInstance.getBean();
            Assert.assertNotNull(securityManagerMap);
            
            // TODO if security manager gets some fields check them here
        }
        
        {
            Instance rsaInstance = hub.getCurrentDatabase().getInstance(AUTHORIZATION_PROVIDER_TYPE, RSA_INSTANCE);
            Assert.assertNotNull(rsaInstance);
            
            Map<String, Object> rsaMap = (Map<String, Object>) rsaInstance.getBean();
            
            Assert.assertEquals(RSA_ATZ_PROV_NAME, rsaMap.get(Commons.NAME_TAG));
            Assert.assertEquals(RSA_DOM_PFX, rsaMap.get(ATZ_DOMAIN_PFX_TAG));
        }
        
        {
            Instance sslManagerInstance = hub.getCurrentDatabase().getInstance(SSL_MANAGER_TYPE, SSL_MANAGER_INSTANCE_NAME);
            if (didDefault) {
                Assert.assertNotNull(sslManagerInstance);
                
                Map<String, Object> sslManagerMap = (Map<String, Object>) sslManagerInstance.getBean();
                
                Assert.assertNull(sslManagerMap.get(Commons.PUBLIC_KEY_TAG));
            }
            else {
                Assert.assertNull(sslManagerInstance);
            }
        }
        
        assertNameOnlyBean(hub, JMS_SERVER_TYPE, JMS_SERVER_CAROL_INSTANCE, CAROL_NAME);
        
        assertNameOnlyBean(hub, TOPIC_TYPE, TOPIC0_INSTANCE, TOPIC0_NAME);
        assertNameOnlyBean(hub, TOPIC_TYPE, TOPIC1_INSTANCE, TOPIC1_NAME);
        
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE0_INSTANCE, QUEUE0_NAME);
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE1_INSTANCE, QUEUE1_NAME);
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE2_INSTANCE, QUEUE2_NAME);
        
        {
            Instance f0 = hub.getCurrentDatabase().getInstance(HTTP_FACTORY_TYPE, factory0Identifier);
            Assert.assertNotNull(f0);
            
            Map<String, Object> beanLike = (Map<String, Object>) f0.getBean();
            
            Assert.assertEquals(ESSEX_NAME, beanLike.get(Commons.NON_KEY_TAG));
        }
        
        {
            Instance f1 = hub.getCurrentDatabase().getInstance(HTTP_FACTORY_TYPE, factory1Identifier);
            Assert.assertNotNull(f1);
            
            Map<String, Object> beanLike = (Map<String, Object>) f1.getBean();
            
            Assert.assertEquals(GLENDOLA_NAME, beanLike.get(Commons.NON_KEY_TAG));
        }
        
        {
            Instance s1 = assertNameOnlyBean(hub, HTTP_SERVER_TYPE, factory0Identifier + "." + FAIRVIEW_NAME, FAIRVIEW_NAME);
            
            Map<String, Object> beanLike = (Map<String, Object>) s1.getBean();
            
            Assert.assertEquals(new Integer(1234), beanLike.get(Commons.PORT_TAG));
        }
        
        {
            Instance s1 = assertNameOnlyBean(hub, HTTP_SERVER_TYPE, factory1Identifier + "." + HOLYOKE_NAME, HOLYOKE_NAME);
            
            Map<String, Object> beanLike = (Map<String, Object>) s1.getBean();
            
            Assert.assertEquals(new Integer(5678), beanLike.get(Commons.PORT_TAG));
        }
        
        {
            Instance s1 = assertNameOnlyBean(hub, HTTP_SERVER_TYPE, factory1Identifier + "." + IROQUIS_NAME, IROQUIS_NAME);
            
            Map<String, Object> beanLike = (Map<String, Object>) s1.getBean();
            
            Assert.assertEquals(new Integer(5679), beanLike.get(Commons.PORT_TAG));
        }
        
        {
            Instance f1 = hub.getCurrentDatabase().getInstance(HTTPS_FACTORY_TYPE, httpsInstanceName);
            Assert.assertNotNull(f1);
            
            Map<String, Object> beanLike = (Map<String, Object>) f1.getBean();
            
            Assert.assertEquals(LIBERTY_NAME, beanLike.get(Commons.NON_KEY_TAG));
        }
        
        assertNotInHub(hub, DIAGNOSTICS_TYPE, DIAGNOSTICS_INSTANCE);
        
        assertDomain1Services(locator, locator, didDefault);
    }
    
    public static void verifyDomain1XmlDomainNotThere(Hub hub, ServiceLocator locator) {
        assertNotInHub(hub, DOMAIN_TYPE, DOMAIN_INSTANCE);
        assertNotInHub(hub, MACHINE_TYPE, ALICE_INSTANCE);
        assertNotInHub(hub, SERVER_TYPE, SERVER0_INSTANCE);
        assertNotInHub(hub, SECURITY_MANAGER_TYPE, SECURITY_MANAGER_INSTANCE);
        assertNotInHub(hub, AUTHORIZATION_PROVIDER_TYPE, RSA_INSTANCE);
        assertNotInHub(hub, JMS_SERVER_TYPE, JMS_SERVER_CAROL_INSTANCE);
        assertNotInHub(hub, JMS_SERVER_TYPE, JMS_SERVER_CAROL_INSTANCE);
        assertNotInHub(hub, TOPIC_TYPE, TOPIC0_INSTANCE);
        assertNotInHub(hub, TOPIC_TYPE, TOPIC1_INSTANCE);
        assertNotInHub(hub, QUEUE_TYPE, QUEUE0_INSTANCE);
        assertNotInHub(hub, QUEUE_TYPE, QUEUE1_INSTANCE);
        assertNotInHub(hub, QUEUE_TYPE, QUEUE2_INSTANCE);
        
        assertNotDomain1Services(locator);
    }
    
    private static void assertNotInHub(Hub hub, String type, String instance) {
        Assert.assertNull(hub.getCurrentDatabase().getInstance(type, instance));
    }
    
    public static void assertDomain1Services(ServiceLocator locator) {
        assertDomain1Services(locator, locator, false);
    }
    
    public static void assertDomain1Services(ServiceLocator locator, ServiceLocator fromLocator, boolean didDefault) {
        // TODO:  Why does root not have a name?
        DomainBean db = locator.getService(DomainBean.class);
        Assert.assertNotNull(db);
        Assert.assertEquals(DOMAIN1_NAME, db.getName());
        assertServiceComesFromSameLocator(db, fromLocator);
        
        SecurityManagerBean smb = locator.getService(SecurityManagerBean.class);
        Assert.assertNotNull(smb);
        assertServiceComesFromSameLocator(smb, fromLocator);
        
        AuthorizationProviderBean rsa = locator.getService(AuthorizationProviderBean.class, RSA_ATZ_PROV_NAME);
        Assert.assertNotNull(rsa);
        Assert.assertEquals(RSA_ATZ_PROV_NAME, rsa.getName());
        assertServiceComesFromSameLocator(rsa, fromLocator);
        
        MachineBean mb = locator.getService(MachineBean.class, ALICE_NAME);
        Assert.assertNotNull(mb);
        Assert.assertEquals(ALICE_NAME, mb.getName());
        assertServiceComesFromSameLocator(mb, fromLocator);
        
        ServerBean sb = locator.getService(ServerBean.class, ALICE_SERVER0_NAME);
        Assert.assertNotNull(sb);
        Assert.assertEquals(ALICE_SERVER0_NAME, sb.getName());
        assertServiceComesFromSameLocator(sb, fromLocator);
        
        JMSServerBean carolJMS = locator.getService(JMSServerBean.class, CAROL_NAME);
        Assert.assertNotNull(carolJMS);
        Assert.assertEquals(CAROL_NAME, carolJMS.getName());
        assertServiceComesFromSameLocator(carolJMS, fromLocator);
        
        JMSServerBean daveJMS = locator.getService(JMSServerBean.class, DAVE_NAME);
        Assert.assertNotNull(daveJMS);
        Assert.assertEquals(DAVE_NAME, daveJMS.getName());
        assertServiceComesFromSameLocator(daveJMS, fromLocator);
        
        assertTopicOfName(locator, fromLocator, TOPIC0_NAME);
        assertTopicOfName(locator, fromLocator, TOPIC1_NAME);
        assertTopicOfName(locator, fromLocator, TOPICD0_NAME);
        
        assertQueueOfName(locator, fromLocator, QUEUE0_NAME);
        assertQueueOfName(locator, fromLocator, QUEUE1_NAME);
        assertQueueOfName(locator, fromLocator, QUEUE2_NAME);
        assertQueueOfName(locator, fromLocator, QUEUED0_NAME);
        
        List<HttpFactoryBean> httpFactories = locator.getAllServices(HttpFactoryBean.class);
        Assert.assertTrue(2 == httpFactories.size() || 4 == httpFactories.size());
        
        HttpFactoryBean essexFactory = null;
        HttpFactoryBean glendolaFactory = null;
        int lcv = 0;
        for (HttpFactoryBean httpFactory : httpFactories) {
            assertServiceComesFromSameLocator(httpFactory, fromLocator);
            
            if (httpFactory.getNonKeyIdentifier().equals(ESSEX_NAME)) {
                essexFactory = httpFactory;
            }
            else if (httpFactory.getNonKeyIdentifier().equals(GLENDOLA_NAME)) {
                glendolaFactory = httpFactory;
            }
            else {
                Assert.fail("Unknown factory: " + httpFactory);
            }
            
            lcv++;
            if (lcv >= 2) break;
        }
        
        Assert.assertNotNull(essexFactory);
        Assert.assertNotNull(glendolaFactory);
        
        assertHttpServerOfName(locator, fromLocator, FAIRVIEW_NAME, 1234);
        assertHttpServerOfName(locator, fromLocator, HOLYOKE_NAME, 5678);
        assertHttpServerOfName(locator, fromLocator, IROQUIS_NAME, 5679);
        
        // There is only one https-factory in the standard document so we can just use the single service
        HttpsFactoryBean httpsFactory = locator.getService(HttpsFactoryBean.class);
        Assert.assertNotNull(httpsFactory);
        
        Assert.assertEquals(LIBERTY_NAME, httpsFactory.getNonKeyIdentifier());
        assertServiceComesFromSameLocator(httpsFactory, fromLocator);
        
        SSLManagerBean sslManager = locator.getService(SSLManagerBean.class);
        if (didDefault) {
            Assert.assertNotNull(sslManager);
            
            Assert.assertNull(sslManager.getPublicKeyLocation());
            Assert.assertEquals(SSLManagerBean.FORT_KNOX, sslManager.getSSLPrivateKeyLocation());
            
            assertServiceComesFromSameLocator(sslManager, fromLocator);
        }
        else {
            Assert.assertNull(sslManager);
        }
        
        Assert.assertNull(fromLocator.getService(DiagnosticsBean.class));
    }
    
    private static void assertServiceComesFromSameLocator(Object service, ServiceLocator locator) {
        Assert.assertTrue(service instanceof XmlHk2ConfigurationBean);
        XmlHk2ConfigurationBean configBean = (XmlHk2ConfigurationBean) service;
        
        ActiveDescriptor<?> descriptor = configBean._getSelfDescriptor();
        Assert.assertNotNull(descriptor);
        
        Long locatorId = locator.getLocatorId();
        Long serviceId = descriptor.getLocatorId();
        
        Assert.assertEquals(locatorId, serviceId);
    }
    
    public static void assertQueueOfName(ServiceLocator locator, String expectedName) {
        assertQueueOfName(locator, locator, expectedName);
    }
    
    public static void assertQueueOfName(ServiceLocator locator, ServiceLocator fromLocator, String expectedName) {
        QueueBean qb = locator.getService(QueueBean.class, expectedName);
        Assert.assertNotNull(qb);
        Assert.assertEquals(expectedName, qb.getName());
        
        assertServiceComesFromSameLocator(qb, fromLocator);
    }
    
    public static void assertHttpServerOfName(ServiceLocator locator, ServiceLocator fromLocator, String expectedName, int expectedPort) {
        HttpServerBean hsb = locator.getService(HttpServerBean.class, expectedName);
        Assert.assertNotNull(hsb);
        Assert.assertEquals(expectedName, hsb.getName());
        Assert.assertEquals(expectedPort, hsb.getPort());
        
        assertServiceComesFromSameLocator(hsb, fromLocator);
    }
    
    public static void assertTopicOfName(ServiceLocator locator, String expectedName) {
        assertTopicOfName(locator, locator, expectedName);
    }
    
    public static void assertTopicOfName(ServiceLocator locator, ServiceLocator fromLocator, String expectedName) {
        TopicBean tb = locator.getService(TopicBean.class, expectedName);
        Assert.assertNotNull(tb);
        Assert.assertEquals(expectedName, tb.getName());
        
        assertServiceComesFromSameLocator(tb, fromLocator);
    }
    
    @SuppressWarnings("unchecked")
    public static Instance assertNameOnlyBean(Hub hub, String type, String instance, String expectedName) {
        Instance namedInstance = hub.getCurrentDatabase().getInstance(type, instance);
        Assert.assertNotNull("Could not find instance of " + type + "," + instance, namedInstance);
        
        Map<String, Object> namedMap = (Map<String, Object>) namedInstance.getBean();
        
        Assert.assertEquals(expectedName, namedMap.get(Commons.NAME_TAG));
        
        return namedInstance;
    }
    
    /**
     * This is a slightly tricky method because it must ensure that the service NOT
     * found is only not found in the current locator (there may be services
     * of the same name and stuff in parents)
     * 
     * @param locator
     */
    private static void assertNotDomain1Services(ServiceLocator locator) {
        assertLocalServiceNotThere(DomainBean.class, null, locator);
        assertLocalServiceNotThere(SecurityManagerBean.class, null, locator);
        assertLocalServiceNotThere(AuthorizationProviderBean.class, RSA_ATZ_PROV_NAME, locator);
        assertLocalServiceNotThere(MachineBean.class, ALICE_NAME, locator);
        assertLocalServiceNotThere(ServerBean.class, ALICE_SERVER0_NAME, locator);
        assertLocalServiceNotThere(JMSServerBean.class, CAROL_NAME, locator);
        assertLocalServiceNotThere(JMSServerBean.class, DAVE_NAME, locator);
        assertLocalServiceNotThere(TopicBean.class, TOPIC0_NAME, locator);
        assertLocalServiceNotThere(TopicBean.class, TOPIC1_NAME, locator);
        assertLocalServiceNotThere(TopicBean.class, TOPICD0_NAME, locator);
        assertLocalServiceNotThere(QueueBean.class, QUEUE0_NAME, locator);
        assertLocalServiceNotThere(QueueBean.class, QUEUE1_NAME, locator);
        assertLocalServiceNotThere(QueueBean.class, QUEUE2_NAME, locator);
        assertLocalServiceNotThere(QueueBean.class, QUEUED0_NAME, locator);
    }
    
    private static void assertLocalServiceNotThere(Class<?> serviceClass, String serviceName, ServiceLocator localLocator) {
        IndexedFilter serviceFilter = new LocalServiceOnlyFilter(serviceClass, serviceName, localLocator);
        List<?> allServices = localLocator.getAllServices(serviceFilter);
        Assert.assertEquals(0, allServices.size());
    }
    
    private final static class LocalServiceOnlyFilter implements IndexedFilter {
        private final Class<?> serviceClass;
        private final String name;
        private final ServiceLocator localLocator;
        
        private LocalServiceOnlyFilter(Class<?> serviceClass, String name, ServiceLocator localLocator) {
            this.serviceClass = serviceClass;
            this.name = name;
            this.localLocator = localLocator;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Filter#matches(org.glassfish.hk2.api.Descriptor)
         */
        @Override
        public boolean matches(Descriptor d) {
            if (!d.getLocatorId().equals(localLocator.getLocatorId())) return false;
            
            return true;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getAdvertisedContract()
         */
        @Override
        public String getAdvertisedContract() {
            return serviceClass.getName();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getName()
         */
        @Override
        public String getName() {
            return name;
        }
        
    }

}
