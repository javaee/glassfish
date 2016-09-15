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

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.hk2Config.xml.test.utilities.LocatorUtilities;
import org.glassfish.hk2.hk2Config.xml.test0.OldConfigTest;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.hk2Config.test.beans.KingdomConfig;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phyla;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phylum;
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
    public static final String DAVE_NAME = "Dave";
    public static final String EXPECTED_MESSAGE = "I hate Dave";
    
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
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, false);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
        
        Phylum ph = locator.getService(Phylum.class, OldConfigTest.ALICE_NAME);
        
        long originalUpdated = ph.getUpdatedOn();
        
        ph.setNumGermLayers(15);
        
        long newUpdated = ph.getUpdatedOn();
        
        Assert.assertTrue(newUpdated > originalUpdated);
    }
    
    /**
     * Tests a basic listener for create
     */
    @Test
    // @org.junit.Ignore
    public void testBasicCreate() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, false);
        rootHandle.addChangeListener(new AuditableListener(), new DaveHatingListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
        
        Phylum bob = xmlService.createBean(Phylum.class);
        bob.setName(OldConfigTest.BOB_NAME);
        
        Phyla phyla = kingdom.getPhyla();
        phyla.addPhylum(bob);
        
        bob = phyla.getPhylumByName(OldConfigTest.BOB_NAME);
        
        Assert.assertTrue(bob.getCreatedOn() > 0L);
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
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, false);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
        
        Phylum alice = kingdom.getPhyla().getPhylumByName(OldConfigTest.ALICE_NAME);
        Phyla phyla = kingdom.getPhyla();
        
        alice = phyla.deletePhylum(alice);
        Assert.assertNotNull(alice);
        
        Assert.assertTrue(alice.getDeletedOn() > 0L);
    }
    
    /**
     * Tests a multi-tier listener for remove and add
     */
    @Test
    // @org.junit.Ignore
    public void testMultiTierRemoveAndAdd() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, false);
        rootHandle.addChangeListener(new AuditableListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
        
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
        
        URL url = getClass().getClassLoader().getResource(OldConfigTest.KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshall(url.toURI(), KingdomConfig.class, true, false);
        rootHandle.addChangeListener(new AuditableListener(), new DaveHatingListener());
        
        KingdomConfig kingdom = rootHandle.getRoot();
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
        
        Phylum bob = xmlService.createBean(Phylum.class);
        bob.setName(DAVE_NAME);
        
        Phyla phyla = kingdom.getPhyla();
        
        try {
            phyla.addPhylum(bob);
            Assert.fail("Should not have succeeded");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(EXPECTED_MESSAGE));
        }
        
        // Nothing should have happened
        OldConfigTest.assertOriginalStateKingdom1(kingdom);
    }

}
