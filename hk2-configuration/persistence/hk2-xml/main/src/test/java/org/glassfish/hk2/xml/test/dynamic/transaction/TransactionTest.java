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
package org.glassfish.hk2.xml.test.dynamic.transaction;

import java.net.URL;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.xml.api.XmlHandleTransaction;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.beans.MachineBean;
import org.glassfish.hk2.xml.test.beans.ServerBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 */
public class TransactionTest {
    private final static String ALT_SUBNET = "0.0.255.255";
    private final static String MIXED_METAPHOR = "Mixed Metaphor";
    
    /**
     * Modifies two properties in one transaction
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test 
    // @org.junit.Ignore
    public void testModifyTwoPropertiesInOneTransaction() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        XmlHandleTransaction<DomainBean> transaction = rootHandle.lockForTransaction();
        try {
            Assert.assertEquals(rootHandle, transaction.getRootHandle());
            
            domain.setSubnetwork(ALT_SUBNET);
            domain.setTaxonomy(MIXED_METAPHOR);
        }
        finally {
            transaction.commit();
        }
        
        Assert.assertEquals(ALT_SUBNET, domain.getSubnetwork());
        Assert.assertEquals(MIXED_METAPHOR, domain.getTaxonomy());
        
        {
            Instance domainInstance = hub.getCurrentDatabase().getInstance(MergeTest.DOMAIN_TYPE, MergeTest.DOMAIN_INSTANCE);
            Assert.assertNotNull(domainInstance);
        
            Map<String, Object> domainMap = (Map<String, Object>) domainInstance.getBean();
            Assert.assertEquals(ALT_SUBNET, domainMap.get(MergeTest.SUBNET_TAG));
            Assert.assertEquals(MIXED_METAPHOR, domainMap.get(MergeTest.TAXONOMY_TAG));
        }
    }
    
    /**
     * Modifies two properties in one transaction but abandons the changes
     * 
     * @throws Exception
     */
    @Test 
    // @org.junit.Ignore
    public void testModifyTwoPropertiesInOneTransactionAbandon() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        XmlHandleTransaction<DomainBean> transaction = rootHandle.lockForTransaction();
        try {
            Assert.assertEquals(rootHandle, transaction.getRootHandle());
            
            domain.setSubnetwork(ALT_SUBNET);
            domain.setTaxonomy(MIXED_METAPHOR);
        }
        finally {
            transaction.abandon();
        }
        
        // Nothing should have changed at all
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
    }
    
    /**
     * Adds beans, removes beans and modifies the properties of
     * a few beans, ensures they all get done
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test 
    // @org.junit.Ignore
    public void testAddRemoveModifySuccess() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        XmlHandleTransaction<DomainBean> transaction = rootHandle.lockForTransaction();
        try {
            addRemoveAndModify(xmlService, domain);
        }
        finally {
            transaction.commit();
        }
        
        Assert.assertNull(domain.lookupJMSServer(MergeTest.DAVE_NAME));
        
        MachineBean eddie = domain.lookupMachine(MergeTest.EDDIE_NAME);
        Assert.assertNotNull(eddie);
        Assert.assertEquals(MergeTest.EDDIE_NAME, eddie.getName());
        
        ServerBean server1 = eddie.lookupServer(MergeTest.SERVER1_NAME);
        Assert.assertEquals(MergeTest.SERVER1_NAME, server1.getName());
        
        // First modify
        Assert.assertEquals(ALT_SUBNET, domain.getSubnetwork());
        
        JMSServerBean carol = domain.lookupJMSServer(MergeTest.CAROL_NAME);
        
        // This is another modify, not on the same bean
        Assert.assertEquals(MergeTest.LZ_COMPRESSION, carol.getCompressionAlgorithm());
        
        {
            Instance domainInstance = hub.getCurrentDatabase().getInstance(MergeTest.DOMAIN_TYPE, MergeTest.DOMAIN_INSTANCE);
            Assert.assertNotNull(domainInstance);
        
            Map<String, Object> domainMap = (Map<String, Object>) domainInstance.getBean();
            Assert.assertEquals(ALT_SUBNET, domainMap.get(MergeTest.SUBNET_TAG));
            Assert.assertNull(domainMap.get(MergeTest.TAXONOMY_TAG));
        }
        
        {
            Instance daveInstance = hub.getCurrentDatabase().getInstance(MergeTest.JMS_SERVER_TYPE, MergeTest.DAVE_INSTANCE);
            Assert.assertNull(daveInstance);
        }
        
        MergeTest.assertNameOnlyBean(hub, MergeTest.MACHINE_TYPE, MergeTest.EDDIE_INSTANCE, MergeTest.EDDIE_NAME);
        MergeTest.assertNameOnlyBean(hub, MergeTest.SERVER_TYPE, MergeTest.SERVER1_INSTANCE, MergeTest.SERVER1_NAME);
        
        {
            Instance carolInstance = hub.getCurrentDatabase().getInstance(MergeTest.JMS_SERVER_TYPE, MergeTest.JMS_SERVER_CAROL_INSTANCE);
            Assert.assertNotNull(carolInstance);
            
            Map<String, Object> carolMap = (Map<String, Object>) carolInstance.getBean();
            Assert.assertEquals(MergeTest.LZ_COMPRESSION, carolMap.get(MergeTest.COMPRESSION_TAG));
        }
        
        Assert.assertNull(locator.getService(JMSServerBean.class, MergeTest.DAVE_NAME));
        Assert.assertNotNull(locator.getService(MachineBean.class, MergeTest.EDDIE_NAME));
        Assert.assertNotNull(locator.getService(ServerBean.class, MergeTest.SERVER1_NAME));
    }
    
    /**
     * Adds beans, removes beans and modifies the properties of
     * a few beans, ensures none of them get done
     * 
     * @throws Exception
     */
    @Test 
    // @org.junit.Ignore
    public void testAddRemoveModifyAbandon() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean domain = rootHandle.getRoot();
        
        XmlHandleTransaction<DomainBean> transaction = rootHandle.lockForTransaction();
        try {
            addRemoveAndModify(xmlService, domain);
        }
        finally {
            transaction.abandon();
        }
        
        // Make sure nothing actually happened
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
    }
    
    /**
     * Does this from the original state
     * 
     * @param domain
     */
    private static void addRemoveAndModify(XmlService xmlService, DomainBean domain) {
        // This is the remove
        JMSServerBean dave = domain.removeJMSServer(MergeTest.DAVE_NAME);
        Assert.assertNotNull(dave);
        
        MachineBean eddie = xmlService.createBean(MachineBean.class);
        eddie.setName(MergeTest.EDDIE_NAME);
        
        ServerBean server1 = xmlService.createBean(ServerBean.class);
        server1.setName(MergeTest.SERVER1_NAME);
        
        eddie.addServer(server1);
        
        // This is the add
        domain.addMachine(eddie);
        
        // This is the modify
        domain.setSubnetwork(ALT_SUBNET);
        
        JMSServerBean carol = domain.lookupJMSServer(MergeTest.CAROL_NAME);
        
        // This is another modify, not on the same bean
        carol.setCompressionAlgorithm(MergeTest.LZ_COMPRESSION);
    }

}
