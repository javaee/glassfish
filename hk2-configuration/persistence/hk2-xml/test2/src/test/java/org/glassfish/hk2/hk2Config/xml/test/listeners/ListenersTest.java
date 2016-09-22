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
package org.glassfish.hk2.hk2Config.xml.test.listeners;

import java.net.URL;
import java.util.Map;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.hk2Config.xml.test.utilities.LocatorUtilities;
import org.glassfish.hk2.hk2Config.xml.test0.OldConfigTest;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.hk2Config.test.beans.KingdomConfig;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phyla;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phylum;
import org.glassfish.hk2.xml.hk2Config.test.beans.ScientistBean;
import org.glassfish.hk2.xml.hk2Config.test.customizers.AuditableListener;
import org.glassfish.hk2.xml.hk2Config.test.customizers.KingdomCustomizer;
import org.glassfish.hk2.xml.hk2Config.test.customizers.PhylaCustomizer;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.config.types.PropertyBagCustomizerImpl;

/**
 * @author jwells
 *
 */
public class ListenersTest {
    public static final String CAROL_NAME = "Carol";
    public static final String DAVE_NAME = "Dave";
    public static final String LINN_NAME = "Linnaeus";
    public static final String BIOLOGY_FIELD = "Biology";
    
    public static final String EXPECTED_MESSAGE = "I hate Dave";
    public static final String EXPECTED_MESSAGE2 = "Carol fails with IllegalStateException";
    public static final String EXPECTED_MESSAGE3 = "The proverbial hater shall always hate";
    
    /**
     * Tests a basic listener for update
     */
    @Test
    // @org.junit.Ignore
    public void testBasicUpdate() throws Exception {    
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phylum ph = locator.getService(Phylum.class, OldConfigTest.ALICE_NAME);
        
        long originalUpdated = ph.getUpdatedOn();
        
        ph.setNumGermLayers(15);
        
        long newUpdated = ph.getUpdatedOn();
        
        Assert.assertTrue(newUpdated > originalUpdated);
    }
    
    /**
     * Tests a basic listener for create
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testBasicCreate() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener(), new DaveHatingListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phylum bob = xmlService.createBean(Phylum.class);
        bob.setName(OldConfigTest.BOB_NAME);
        
        Phyla phyla = kingdom.getPhyla();
        phyla.addPhylum(bob);
        
        bob = phyla.getPhylumByName(OldConfigTest.BOB_NAME);
        
        Assert.assertTrue(bob.getCreatedOn() > 0L);
        
        // Check that its there in the hub
        {
            Instance bobInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.PHYLUM_TYPE, OldConfigTest.BOB_INSTANCE);
            Assert.assertNotNull(bobInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) bobInstance.getBean();
            Assert.assertEquals(OldConfigTest.BOB_NAME, aliceMap.get(OldConfigTest.NAME_TAG));
        }
    }
    
    /**
     * Tests a basic listener for create for array child
     */
    @Test
    // @org.junit.Ignore
    public void testBasicCreateAnArray() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener(), new DaveHatingListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        ScientistBean linnaeus = xmlService.createBean(ScientistBean.class);
        linnaeus.setName(LINN_NAME);
        
        linnaeus = kingdom.addScientist(linnaeus);
        
        Assert.assertTrue(linnaeus.getCreatedOn() > 0L);
        Assert.assertEquals(0L, linnaeus.getUpdatedOn());
        
        long kingdomUpdated = kingdom.getUpdatedOn();
        Assert.assertTrue(kingdomUpdated > 0L);
        
        linnaeus.setField(BIOLOGY_FIELD);
        
        Assert.assertTrue(linnaeus.getUpdatedOn() > 0);
        
        // A spot check to make sure this hasn't moved when a child was modified
        Assert.assertEquals(kingdomUpdated, kingdom.getUpdatedOn());
    }
    
    /**
     * Tests a basic listener for remove
     */
    @Test
    // @org.junit.Ignore
    public void testBasicRemove() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phylum alice = kingdom.getPhyla().getPhylumByName(OldConfigTest.ALICE_NAME);
        Phyla phyla = kingdom.getPhyla();
        
        alice = phyla.deletePhylum(alice);
        Assert.assertNotNull(alice);
        
        Assert.assertTrue(alice.getDeletedOn() > 0L);
        Assert.assertTrue(phyla.getUpdatedOn() > 0L);
    }
    
    /**
     * Tests a remove and re-add of a direct child
     */
    @Test
    // @org.junit.Ignore
    public void testBasicRemoveAndAddOfDirectChild() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phyla phyla = kingdom.getPhyla();
        kingdom.setPhyla(null);
        
        Assert.assertTrue(phyla.getDeletedOn() > 0L);
        Assert.assertTrue(kingdom.getUpdatedOn() > 0L);
        
        phyla = xmlService.createBean(Phyla.class);
        kingdom.setPhyla(phyla);
        phyla = kingdom.getPhyla();
        
        Assert.assertTrue(phyla.getCreatedOn() > 0L);
        Assert.assertTrue(kingdom.getUpdatedOn() > 0L);
    }
    
    /**
     * Tests a basic listener for remove
     */
    @Test
    // @org.junit.Ignore
    public void testBasicRemoveArray() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        ScientistBean darwin = kingdom.lookupScientist(OldConfigTest.DARWIN_NAME);
        ScientistBean removed = kingdom.removeScientist(kingdom.getScientists()[0]);
        
        Assert.assertNotNull(removed);
        Assert.assertEquals(darwin, removed);
        Assert.assertEquals(0, kingdom.getScientists().length);
        
        Assert.assertTrue(darwin.getDeletedOn() > 0L);
    }
    
    /**
     * Tests a multi-tier listener for remove and add
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testMultiTierRemoveAndAdd() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        {
            Phylum alice = kingdom.getPhyla().getPhylumByName(OldConfigTest.ALICE_NAME);
            Phyla phyla = kingdom.getPhyla();
        
            kingdom.setPhyla(null);
        
            // Alice was never truly deleted, so no event for it
            Assert.assertEquals(0, alice.getDeletedOn());
        
            // But phyla truly was
            Assert.assertTrue(phyla.getDeletedOn() > 0L);
        }
        
        {
            Instance phylaInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.PHYLA_TYPE, OldConfigTest.PHYLA_INSTANCE);
            Assert.assertNull(phylaInstance);
        }
        
        {
            Instance aliceInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.PHYLUM_TYPE, OldConfigTest.ALICE_INSTANCE);
            Assert.assertNull(aliceInstance);
        }
        
        {
            Phylum bob = xmlService.createBean(Phylum.class);
            bob.setName(OldConfigTest.BOB_NAME);
            
            Phyla phyla = xmlService.createBean(Phyla.class);
            phyla.addPhylum(bob);
        
            kingdom.setPhyla(phyla);
            
            phyla = kingdom.getPhyla();
            bob = phyla.getPhylumByName(OldConfigTest.BOB_NAME);
        
            // Bob was added so it also gets a high created on
            Assert.assertTrue(bob.getCreatedOn() > 0L);
        
            // But phyla truly was
            Assert.assertTrue(phyla.getCreatedOn() > 0L);
        }
        
        {
            Instance phylaInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.PHYLA_TYPE, OldConfigTest.PHYLA_INSTANCE);
            Assert.assertNotNull(phylaInstance);
        }
        
        {
            Instance bobInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.PHYLUM_TYPE, OldConfigTest.BOB_INSTANCE);
            Assert.assertNotNull(bobInstance);
            
            Map<String, Object> bobMap = (Map<String, Object>) bobInstance.getBean();
            Assert.assertEquals(OldConfigTest.BOB_NAME, bobMap.get(OldConfigTest.NAME_TAG));
            Assert.assertNull(bobMap.get(OldConfigTest.SHELL_TAG));
        }
    }
    
    /**
     * Tests that when Dave is added nothing happens (because of the listener)
     */
    @Test
    // @org.junit.Ignore
    public void testFailedCreate() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class,
                DaveHatingListener.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener(), new DaveHatingListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phylum dave = xmlService.createBean(Phylum.class);
        dave.setName(DAVE_NAME);
        
        Phyla phyla = kingdom.getPhyla();
        
        try {
            phyla.addPhylum(dave);
            Assert.fail("Should not have succeeded");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(EXPECTED_MESSAGE));
        }
        
        // Nothing should have happened
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
    }
    
    /**
     * Tests that when a field is updated with Dave it doesn't happen
     * and subsequent listeners are not called
     */
    @Test
    // @org.junit.Ignore
    public void testFailedUpdate() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class,
                DaveHatingListener.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new DaveHatingListener(), new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phyla phyla = kingdom.getPhyla();
        Phylum alice = phyla.getPhylumByName(OldConfigTest.ALICE_NAME);
        
        try {
            alice.setShellType(DAVE_NAME);
            Assert.fail("Should not have succeeded");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(EXPECTED_MESSAGE));
        }
        
        // Nothing should have happened
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
    }
    
    /**
     * Tests that when a field is updated with Carol it doesn't happen
     * and subsequent listeners are called because it failed with a different
     * exception
     */
    @Test
    // @org.junit.Ignore
    public void testFailedUpdateOtherListenersCalled() throws Exception {
        ServiceLocator locator = LocatorUtilities.createDomLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class,
                DaveHatingListener.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new DaveHatingListener(), new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        Phyla phyla = kingdom.getPhyla();
        Phylum alice = phyla.getPhylumByName(OldConfigTest.ALICE_NAME);
        
        try {
            alice.setShellType(CAROL_NAME);
            Assert.fail("Should not have succeeded");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(EXPECTED_MESSAGE2));
        }
        
        // This shows that the subsequent listener (auditable) was invoked
        Assert.assertTrue(alice.getUpdatedOn() > 0);
    }
    
    /**
     * Tests that when a field is updated with Carol it doesn't happen
     * and subsequent listeners are called because it failed with a different
     * exception
     */
    @SuppressWarnings("unchecked")
    @Test
    @org.junit.Ignore
    public void testPropertyNotChangedOnVeto() throws Exception {
        ServiceLocator locator = LocatorUtilities.createDomLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, true);
        rootHandle.addChangeListener(new AuditableListener(), new HaterGonnaHate());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom, hub);
        
        ScientistBean darwin = kingdom.getScientists()[0];
        
        try {
            darwin.setField(BIOLOGY_FIELD);
            Assert.fail("Should have failed because hater gonna hate");
        }
        catch (MultiException expected) {
            // expected
        }
        
        {
            Instance scienceInstance = hub.getCurrentDatabase().getInstance(OldConfigTest.SCIENTIST_TYPE, OldConfigTest.DARWIN_INSTANCE);
            Assert.assertNotNull(scienceInstance);
            
            Map<String, Object> propMap = (Map<String, Object>) scienceInstance.getBean();
            Assert.assertEquals(OldConfigTest.DARWIN_NAME, propMap.get(OldConfigTest.NAME_TAG));
            Assert.assertEquals(OldConfigTest.NATURALIST_FIELD, propMap.get(OldConfigTest.FIELD_TAG));
            Assert.assertEquals(new Long(0), propMap.get("updated-on"));
        }
        
        Assert.assertEquals(OldConfigTest.NATURALIST_FIELD, darwin.getField());
        Assert.assertEquals(0L, darwin.getUpdatedOn());
    }

}
