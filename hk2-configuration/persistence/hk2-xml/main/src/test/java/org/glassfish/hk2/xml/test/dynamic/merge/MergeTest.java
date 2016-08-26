/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.Museum;
import org.glassfish.hk2.xml.test.basic.UnmarshallTest;
import org.glassfish.hk2.xml.test.beans.AuthorizationProviderBean;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.beans.MachineBean;
import org.glassfish.hk2.xml.test.beans.QueueBean;
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
    private final static String RSA_ATZ_PROV_NAME = "RSA";
    private final static String RSA_DOM_PFX = "rsa";
    private final static String ALICE_NAME = "Alice";
    private final static String BOB_NAME = "Bob";
    public final static String CAROL_NAME = "Carol";
    private final static String TOPIC0_NAME = "Topic0";
    private final static String TOPIC1_NAME = "Topic1";
    private final static String QUEUE0_NAME = "Queue0";
    private final static String QUEUE1_NAME = "Queue1";
    private final static String QUEUE2_NAME = "Queue2";
    
    private final static String ALICE_ADDRESS = "10.0.0.1";
    private final static String ALICE_SERVER0_NAME = "Server-0";
    private final static int ALICE_SERVER0_PORT = 12345;
    
    private final static String DOMAIN_TYPE = "/domain";
    private final static String DOMAIN_INSTANCE = "domain";
    private final static String MACHINE_TYPE = "/domain/machine";
    private final static String SERVER_TYPE = "/domain/machine/server";
    public final static String SECURITY_MANAGER_TYPE = "/domain/security-manager";
    public final static String AUTHORIZATION_PROVIDER_TYPE = "/domain/security-manager/authorization-provider";
    public final static String JMS_SERVER_TYPE = "/domain/jms-server";
    public final static String TOPIC_TYPE = "/domain/jms-server/topic";
    public final static String QUEUE_TYPE = "/domain/jms-server/queue";
    
    private final static String ALICE_INSTANCE = "domain.Alice";
    private final static String BOB_INSTANCE = "domain.Bob";
    private final static String SERVER0_INSTANCE = "domain.Alice.Server-0";
    private final static String SECURITY_MANAGER_INSTANCE = "domain.security-manager";
    private final static String RSA_INSTANCE = "domain.security-manager.RSA";
    private final static String JMS_SERVER_INSTANCE = "domain.Carol";
    private final static String TOPIC0_INSTANCE = "domain.Carol.Topic0";
    private final static String TOPIC1_INSTANCE = "domain.Carol.Topic1";
    private final static String QUEUE0_INSTANCE = "domain.Carol.Queue0";
    private final static String QUEUE1_INSTANCE = "domain.Carol.Queue1";
    private final static String QUEUE2_INSTANCE = "domain.Carol.Queue2";
    
    private final static String ADDRESS_TAG = "address";
    private final static String PORT_TAG = "port";
    private final static String ATZ_DOMAIN_PFX_TAG = "domain-pfx";
    
    /**
     * Modifies two properties with one transaction in a merge
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testMergeModifyTwoPropertiesOneTransaction() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshall(url.toURI(), Museum.class);
        
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
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, museumOld.getName());
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museumOld.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(RawSetsTest.MUSEUM_TYPE, RawSetsTest.MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, beanLikeMap.get(UnmarshallTest.NAME_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(UnmarshallTest.ID_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(RawSetsTest.AGE_TAG));  // The test
        
        List<Change> changes = listener.getChanges();
        Assert.assertNotNull(changes);
        
        Assert.assertEquals(1, changes.size());
        
        for (Change change : changes) {
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
            
            List<PropertyChangeEvent> events = change.getModifiedProperties();
            
            Assert.assertEquals(2, events.size());
            boolean gotId = false;
            boolean gotAge = false;
            for (PropertyChangeEvent event : events) {
                if ("age".equals(event.getPropertyName())) {
                    gotAge = true;
                }
                else if ("id".equals(event.getPropertyName())) {
                    gotId = true;
                }
            }
            
            Assert.assertTrue(gotId);
            Assert.assertTrue(gotAge);
        }
    }
    
    /**
     * Adds a child to the top bean
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test 
    @org.junit.Ignore
    public void testMergeModifyAddADirectChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        verifyDomain1Xml(rootHandle, hub);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOld = rootHandle.getRoot();
        
        MachineBean addedBean = xmlService.createBean(MachineBean.class);
        addedBean.setName(BOB_NAME);
        
        domainCopy.addMachine(addedBean);
        
        // Ensure that the modification of the copy did NOT affect the parent!
        verifyDomain1Xml(rootHandle, hub);
        
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
            Assert.assertEquals(BOB_NAME, bobMap.get(UnmarshallTest.NAME_TAG));
            Assert.assertNull(bobMap.get(ADDRESS_TAG));
        }
        
        // TODO: Check that we have the proper set of changes (one modify, one add)
    }
    
    public static void verifyDomain1Xml(XmlRootHandle<DomainBean> rootHandle, Hub hub) {
        DomainBean root = rootHandle.getRoot();
        
        verifyDomain1XmlDomain(root, hub);
    }
    
    public static void verifyDomain1Xml(XmlRootHandle<DomainBean> original, XmlRootCopy<DomainBean> rootHandle, Hub hub) {
        Assert.assertEquals(original, rootHandle.getParent());
        
        DomainBean root = rootHandle.getChildRoot();
        
        verifyDomain1XmlDomain(root, hub);
    }
    
    @SuppressWarnings("unchecked")
    private static void verifyDomain1XmlDomain(DomainBean root, Hub hub) {
        Assert.assertEquals("Failing bean is " + root, DOMAIN1_NAME, root.getName());
        
        SecurityManagerBean securityManager = root.getSecurityManager();
        Assert.assertNotNull(securityManager);
        
        List<AuthorizationProviderBean> atzProviders = securityManager.getAuthorizationProviders();
        Assert.assertNotNull(atzProviders);
        Assert.assertEquals(1, atzProviders.size());
        
        for (AuthorizationProviderBean atzProvider : atzProviders) {
            Assert.assertEquals(RSA_ATZ_PROV_NAME, atzProvider.getName());
            Assert.assertEquals(RSA_DOM_PFX, atzProvider.getAtzDomainPrefix());
        }
        
        Assert.assertNull(securityManager.getSSLManager());
        
        List<MachineBean> machines = root.getMachines();
        Assert.assertNotNull(machines);
        Assert.assertEquals(1, machines.size());
        
        for (MachineBean machine : machines) {
            Assert.assertEquals(ALICE_NAME, machine.getName());
            Assert.assertEquals(ALICE_ADDRESS, machine.getAddress());
            
            List<ServerBean> servers = machine.getServers();
            Assert.assertNotNull(servers);
            Assert.assertEquals(1, servers.size());
            
            for (ServerBean server : servers) {
                Assert.assertEquals(ALICE_SERVER0_NAME, server.getName());
                Assert.assertEquals(ALICE_SERVER0_PORT, server.getPort());   
            }
        }
        
        JMSServerBean jmsServers[] = root.getJMSServers();
        Assert.assertEquals(1, jmsServers.length);
        
        for (JMSServerBean jmsServer : jmsServers) {
            Assert.assertEquals("Did not find name in " + jmsServer, CAROL_NAME, jmsServer.getName());
            
            List<TopicBean> topics = jmsServer.getTopics();
            Assert.assertEquals(2, topics.size());
            
            Assert.assertEquals(TOPIC0_NAME, topics.get(0).getName());
            Assert.assertEquals(TOPIC1_NAME, topics.get(1).getName());
            
            QueueBean queues[] = jmsServer.getQueues();
            
            Assert.assertEquals(QUEUE0_NAME, queues[0].getName());
            Assert.assertEquals(QUEUE1_NAME, queues[1].getName());
            Assert.assertEquals(QUEUE2_NAME, queues[2].getName());
        }
        
        // Below is the verification for the Hub versions of the beans
        
        {
            Instance domainInstance = hub.getCurrentDatabase().getInstance(DOMAIN_TYPE, DOMAIN_INSTANCE);
            Assert.assertNotNull(domainInstance);
        
            // TODO: When domain has attributes check them here
            // Map<String, Object> domainMap = (Map<String, Object>) domainInstance.getBean();
        }
        
        {
            Instance machineAliceInstance = hub.getCurrentDatabase().getInstance(MACHINE_TYPE, ALICE_INSTANCE);
            Assert.assertNotNull(machineAliceInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) machineAliceInstance.getBean();
            Assert.assertEquals(ALICE_NAME, aliceMap.get(UnmarshallTest.NAME_TAG));
            Assert.assertEquals(ALICE_ADDRESS, aliceMap.get(ADDRESS_TAG));
        }
        
        {
            Instance aliceServer0Instance = hub.getCurrentDatabase().getInstance(SERVER_TYPE, SERVER0_INSTANCE);
            Assert.assertNotNull(aliceServer0Instance);
            
            Map<String, Object> server0Map = (Map<String, Object>) aliceServer0Instance.getBean();
            Assert.assertEquals(ALICE_SERVER0_NAME, server0Map.get(UnmarshallTest.NAME_TAG));
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
            
            Assert.assertEquals(RSA_ATZ_PROV_NAME, rsaMap.get(UnmarshallTest.NAME_TAG));
            Assert.assertEquals(RSA_DOM_PFX, rsaMap.get(ATZ_DOMAIN_PFX_TAG));
        }
        
        assertNameOnlyBean(hub, JMS_SERVER_TYPE, JMS_SERVER_INSTANCE, CAROL_NAME);
        
        assertNameOnlyBean(hub, TOPIC_TYPE, TOPIC0_INSTANCE, TOPIC0_NAME);
        assertNameOnlyBean(hub, TOPIC_TYPE, TOPIC1_INSTANCE, TOPIC1_NAME);
        
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE0_INSTANCE, QUEUE0_NAME);
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE1_INSTANCE, QUEUE1_NAME);
        assertNameOnlyBean(hub, QUEUE_TYPE, QUEUE2_INSTANCE, QUEUE2_NAME);
    }
    
    @SuppressWarnings("unchecked")
    private static void assertNameOnlyBean(Hub hub, String type, String instance, String expectedName) {
        Instance namedInstance = hub.getCurrentDatabase().getInstance(type, instance);
        Assert.assertNotNull("Could not find instance of " + type + "," + instance, namedInstance);
        
        Map<String, Object> namedMap = (Map<String, Object>) namedInstance.getBean();
        
        Assert.assertEquals(expectedName, namedMap.get(UnmarshallTest.NAME_TAG));
    }

}
