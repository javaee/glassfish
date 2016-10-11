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
package org.glassfish.hk2.hk2Config.xml.test0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.hk2Config.xml.test.utilities.LocatorUtilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.hk2Config.test.beans.KingdomConfig;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phyla;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phylum;
import org.glassfish.hk2.xml.hk2Config.test.beans.PropertyValue;
import org.glassfish.hk2.xml.hk2Config.test.beans.ScientistBean;
import org.glassfish.hk2.xml.hk2Config.test.beans.pv.NamedPropertyValue;
import org.glassfish.hk2.xml.hk2Config.test.customizers.KingdomCustomizer;
import org.glassfish.hk2.xml.hk2Config.test.customizers.PhylaCustomizer;
import org.glassfish.hk2.xml.spi.ConfigBeanProxyCustomizerImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBagCustomizerImpl;

public class OldConfigTest {
    public static final String KINGDOM_FILE = "kingdom1.xml";
    
    public static final String ALICE_NAME = "Alice";
    public static final String BOB_NAME = "Bob";
    public static final String DARWIN_NAME = "Darwin";
    
    public static final String NATURALIST_FIELD = "naturalist";
    
    private static final String USERNAME_PROP_KEY = "username";
    private static final String USERNAME_PROP_VALUE = "sa";
    private static final String PASSWORD_PROP_KEY = "password";
    private static final String PASSWORD_PROP_VALUE = "sp";
    
    private static final String SHELL_TYPE_CHITIN = "chitin";
    
    private static final String P1 = "P1";
    private static final String P2 = "P2";
    private static final String P3 = "P3";
    private static final String P4 = "P4";
    
    private static final String V1 = "V1";
    private static final String V2 = "V2";
    private static final String V3 = "V3";
    private static final String V4 = "V4";
    
    private final static File OUTPUT_FILE = new File("output.xml");
    private final static String LOOK_FOR_ME = "<attribution>ama</attribution>";
    
    @Before
    public void before() {
        if (OUTPUT_FILE.exists()) {
            boolean didDelete = OUTPUT_FILE.delete();
            Assert.assertTrue(didDelete);
        }
    }
    
    @After
    public void after() {
        if (OUTPUT_FILE.exists()) {
            OUTPUT_FILE.delete();
        }
    }
    
    /**
     * See that the simple names and methods work
     * 
     * @throws Exception
     */
    @Test
    public void testSimplePropertiesAndNamesAreCallable() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshal(url.toURI(), KingdomConfig.class, true, false);
        KingdomConfig kingdom1 = rootHandle.getRoot();
        
        assertOriginalStateKingdom1(kingdom1, null, locator);
    }
    
    /**
     * Tests that the duck-type customizers can be called
     * 
     * @throws Exception
     */
    @Test
    public void testCustomizedMethodsAreCallable() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class,
                ConfigBeanProxyCustomizerImpl.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshal(url.toURI(), KingdomConfig.class, true, false);
        KingdomConfig kingdom1 = rootHandle.getRoot();
        
        assertOriginalStateKingdom1(kingdom1, null, locator);
        
        Assert.assertNull(kingdom1.getParent());
        Assert.assertNull(kingdom1.getParent(KingdomConfig.class));
        Assert.assertNotNull(kingdom1.createChild(Phyla.class));
        try {
           kingdom1.deepCopy(kingdom1);
           Assert.fail("Not implemented");
        }
        catch (IllegalStateException ae) {
            // expected
        }
        
        Phyla phyla = kingdom1.getPhyla();
        
        Assert.assertEquals(kingdom1, phyla.getParent());
        Assert.assertEquals(kingdom1, phyla.getParent(KingdomConfig.class));
        Assert.assertNotNull(phyla.createChild(Phylum.class));
        try {
           phyla.deepCopy(kingdom1);
           Assert.fail("Not implemented");
        }
        catch (IllegalStateException ae) {
            // expected
        }
        
        Phylum alice = phyla.getPhylumByName(ALICE_NAME);
        Assert.assertNotNull(alice);
        
        Assert.assertEquals(phyla, alice.getParent());
        Assert.assertEquals(phyla, alice.getParent(Phyla.class));
        try {
           alice.deepCopy(kingdom1);
           Assert.fail("Not implemented");
        }
        catch (IllegalStateException ae) {
            // expected
        }
        
        Assert.assertEquals(USERNAME_PROP_VALUE, alice.getPropertyValue(USERNAME_PROP_KEY));
        Assert.assertEquals(PASSWORD_PROP_VALUE, alice.getPropertyValue(PASSWORD_PROP_KEY));
        
        List<Phylum> all = phyla.getPhylumByType(Phylum.class);
        Assert.assertEquals(1, all.size());
        
        for (Phylum phylum : all) {
            Assert.assertEquals(ALICE_NAME, phylum.getName());
            
            Assert.assertEquals(USERNAME_PROP_VALUE, phylum.getPropertyValue(USERNAME_PROP_KEY));
            Assert.assertEquals(PASSWORD_PROP_VALUE, phylum.getPropertyValue(PASSWORD_PROP_KEY));
        }
        
        Map<String, PropertyValue> props = new HashMap<String, PropertyValue>();
        props.put("name", new NamedPropertyValue(BOB_NAME));
        
        Phylum bob = phyla.createPhylum(props);
        Assert.assertNotNull(bob);
        Assert.assertEquals(BOB_NAME, bob.getName());
        
        all = phyla.getPhylum();
        Assert.assertEquals(2, all.size());
        
        int lcv = 0;
        for (Phylum phylum : all) {
            if (lcv == 0) {
                Assert.assertEquals(ALICE_NAME, phylum.getName());
                
                Assert.assertEquals(USERNAME_PROP_VALUE, phylum.getPropertyValue(USERNAME_PROP_KEY));
                Assert.assertEquals(PASSWORD_PROP_VALUE, phylum.getPropertyValue(PASSWORD_PROP_KEY));
            }
            else if (lcv == 1) {
                Assert.assertEquals(BOB_NAME, phylum.getName());
                
                Assert.assertNull(phylum.getPropertyValue(USERNAME_PROP_KEY));
                Assert.assertNull(phylum.getPropertyValue(PASSWORD_PROP_KEY));
            }
            lcv++;
        }
    }
    
    private final static String ADDED_NAME = "added";
    private final static String ADDED_VALUE = "added value";
    
    /**
     * Tests that the duck-type customizers can be called
     * 
     * @throws Exception
     */
    @Test
    public void testAddRemoveAndLookupOfProperty() throws Exception {
        ServiceLocator locator = LocatorUtilities.createLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshal(url.toURI(), KingdomConfig.class, true, false);
        KingdomConfig kingdom1 = rootHandle.getRoot();
        
        assertOriginalStateKingdom1(kingdom1, null, locator);
        
        Assert.assertNull(kingdom1.lookupProperty(ADDED_NAME));
        
        List<Property> allProps = kingdom1.getProperty();
        Assert.assertEquals(3, allProps.size());
        
        Property property = xmlService.createBean(Property.class);
        property.setName(ADDED_NAME);
        property.setValue(ADDED_VALUE);
        
        kingdom1.addProperty(property);
        
        Property found = kingdom1.lookupProperty(ADDED_NAME);
        Assert.assertNotNull(found);
        
        Assert.assertEquals(ADDED_NAME, found.getName());
        Assert.assertEquals(ADDED_VALUE, found.getValue());
        Assert.assertNull(found.getDescription());
        
        allProps = kingdom1.getProperty();
        Assert.assertEquals(4, allProps.size());
        
        Assert.assertEquals(found, allProps.get(3));
        
        Property removed = kingdom1.removeProperty(ADDED_NAME);
        Assert.assertNotNull(removed);
        
        Assert.assertEquals(found, removed);
        
        Assert.assertNull(kingdom1.lookupProperty(ADDED_NAME));
        
        allProps = kingdom1.getProperty();
        Assert.assertEquals(3, allProps.size());
    }
    
    private final static String AMA = "ama";
    
    /**
     * Tests that the file can be marshalled
     * 
     * @throws Exception
     */
    @Test
    public void testMarshall() throws Exception {
        ServiceLocator locator = LocatorUtilities.createDomLocator(
                PropertyBagCustomizerImpl.class,
                KingdomCustomizer.class,
                PhylaCustomizer.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(KINGDOM_FILE);
        
        XmlRootHandle<KingdomConfig> rootHandle = xmlService.unmarshal(url.toURI(), KingdomConfig.class, true, true);
        KingdomConfig kingdom1 = rootHandle.getRoot();
        
        assertOriginalStateKingdom1(kingdom1, hub, locator);
        
        kingdom1.setAttribution(AMA);
        
        FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);
        try {
          rootHandle.marshal(fos);
        }
        finally {
            fos.close();
        }
        
        checkFile();
    }
    
    private void checkFile() throws Exception {
        boolean found = false;
        FileReader reader = new FileReader(OUTPUT_FILE);
        BufferedReader buffered = new BufferedReader(reader);
        
        try {
            String line;
            while ((line = buffered.readLine()) != null) {
                if (line.contains(LOOK_FOR_ME)) {
                    found = true;
                }
            }
        }
        finally {
            buffered.close();
            reader.close();
        }
        
        Assert.assertTrue(found);
    }
    
    public static String KINGDOM_TYPE = "/kingdom";
    public static String PHYLA_TYPE = "/kingdom/phyla";
    public static String PHYLUM_TYPE = "/kingdom/phyla/phylum";
    public static String PHYLUM_PROP_TYPE = "/kingdom/phyla/phylum/property";
    public static String KINGDOM_PROP_TYPE = "/kingdom/property";
    public static String SCIENTIST_TYPE = "/kingdom/scientist";
    
    public static String KINGDOM_INSTANCE = "kingdom";
    public static String PHYLA_INSTANCE = "kingdom.phyla";
    public static String ALICE_INSTANCE = "kingdom.phyla.Alice";
    public static String BOB_INSTANCE = "kingdom.phyla.Bob";
    public static String PHYLUM_ALICE_PASSWORD_INSTANCE = "kingdom.phyla.Alice.password";
    public static String PHYLUM_ALICE_USERNAME_INSTANCE = "kingdom.phyla.Alice.username";
    public static String P1_INSTANCE = "kingdom.P1";
    public static String P2_INSTANCE = "kingdom.P2";
    public static String P3_INSTANCE = "kingdom.P3";
    public static String DARWIN_INSTANCE = "kingdom.Darwin";
    
    public static String NAME_TAG = "name";
    public static String SHELL_TAG = "shell-type";
    public static String VALUE_TAG = "value";
    public static String FIELD_TAG = "field";
    
    public static String PASSWORD_KEY = "password";
    public static String PASSWORD_VALUE = "sp";
    public static String USERNAME_KEY = "username";
    public static String USERNAME_VALUE = "sa";
    
    @SuppressWarnings("unchecked")
    public static void assertOriginalStateKingdom1(KingdomConfig kingdom1, Hub hub, ServiceLocator locator) {
        if (locator != null) {
            KingdomConfig locatorKingdom = locator.getService(KingdomConfig.class);
            Assert.assertEquals(locatorKingdom, kingdom1);
        }
        
        Assert.assertNotNull(kingdom1);
        
        Assert.assertEquals(0L, kingdom1.getCreatedOn());
        Assert.assertEquals(0L, kingdom1.getUpdatedOn());
        Assert.assertEquals(0L, kingdom1.getDeletedOn());
        
        Phyla phyla = kingdom1.getPhyla();
        Assert.assertNotNull(phyla);
        
        if (locator != null) {
            Phyla locatorPhyla = locator.getService(Phyla.class);
            Assert.assertEquals(locatorPhyla, phyla);
        }
        
        Assert.assertNull(kingdom1.getAttribution());
        
        List<Phylum> phylums = phyla.getPhylum();
        Assert.assertEquals(1, phylums.size());
        
        for (Phylum phylum : phylums) {
            Assert.assertEquals(ALICE_NAME, phylum.getName());
            
            Assert.assertEquals(USERNAME_PROP_VALUE, phylum.getPropertyValue(USERNAME_PROP_KEY));
            Assert.assertEquals(PASSWORD_PROP_VALUE, phylum.getPropertyValue(PASSWORD_PROP_KEY));
            Assert.assertEquals(2, phylum.getNumGermLayers());
            Assert.assertEquals(true, phylum.isSoftBodied());
            Assert.assertEquals(0L, phylum.getCreatedOn());
            Assert.assertEquals(0L, phylum.getUpdatedOn());
            Assert.assertEquals(0L, phylum.getDeletedOn());
            Assert.assertEquals(SHELL_TYPE_CHITIN, phylum.getShellType());
        }
        
        Assert.assertEquals(P1, kingdom1.getProperty().get(0).getName());
        Assert.assertEquals(V1, kingdom1.getProperty().get(0).getValue());
        
        Assert.assertEquals(P2, kingdom1.getProperty().get(1).getName());
        Assert.assertEquals(V2, kingdom1.getProperty().get(1).getValue());
        
        Assert.assertEquals(P3, kingdom1.getProperty().get(2).getName());
        Assert.assertEquals(V3, kingdom1.getProperty().get(2).getValue());
        
        Assert.assertEquals(V1, kingdom1.getPropertyValue(P1));
        Assert.assertEquals(V2, kingdom1.getPropertyValue(P2));
        Assert.assertEquals(V3, kingdom1.getPropertyValue(P3));
        
        // Check that "defaulting" works
        Assert.assertEquals(V4, kingdom1.getPropertyValue(P4, V4));
        
        ScientistBean scientists[] = kingdom1.getScientists();
        Assert.assertEquals(1, scientists.length);
        
        for (ScientistBean scientist : scientists) {
            Assert.assertEquals(DARWIN_NAME, scientist.getName());
            Assert.assertEquals(NATURALIST_FIELD, scientist.getField());
        }
        
        if (hub == null) return;
        BeanDatabase db = hub.getCurrentDatabase();
        
        // Below is the verification for the Hub versions of the beans
       
        {
            Instance domainInstance = db.getInstance(KINGDOM_TYPE, KINGDOM_INSTANCE);
            Assert.assertNotNull(domainInstance);
        }
        
        {
            Instance phylaInstance = db.getInstance(PHYLA_TYPE, PHYLA_INSTANCE);
            Assert.assertNotNull(phylaInstance);
        }
        
        {
            Instance aliceInstance = hub.getCurrentDatabase().getInstance(PHYLUM_TYPE, ALICE_INSTANCE);
            Assert.assertNotNull(aliceInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) aliceInstance.getBean();
            Assert.assertEquals(ALICE_NAME, aliceMap.get(NAME_TAG));
            Assert.assertEquals(SHELL_TYPE_CHITIN, aliceMap.get(SHELL_TAG));
        }
        
        {
            Instance alicePWInstance = hub.getCurrentDatabase().getInstance(PHYLUM_PROP_TYPE, PHYLUM_ALICE_PASSWORD_INSTANCE);
            Assert.assertNotNull(alicePWInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) alicePWInstance.getBean();
            Assert.assertEquals(PASSWORD_KEY, aliceMap.get(NAME_TAG));
            Assert.assertEquals(PASSWORD_VALUE, aliceMap.get(VALUE_TAG));
        }
        
        {
            Instance aliceUNInstance = hub.getCurrentDatabase().getInstance(PHYLUM_PROP_TYPE, PHYLUM_ALICE_USERNAME_INSTANCE);
            Assert.assertNotNull(aliceUNInstance);
            
            Map<String, Object> aliceMap = (Map<String, Object>) aliceUNInstance.getBean();
            Assert.assertEquals(USERNAME_KEY, aliceMap.get(NAME_TAG));
            Assert.assertEquals(USERNAME_VALUE, aliceMap.get(VALUE_TAG));
        }
        
        {
            Instance p1Instance = hub.getCurrentDatabase().getInstance(KINGDOM_PROP_TYPE, P1_INSTANCE);
            Assert.assertNotNull(p1Instance);
            
            Map<String, Object> propMap = (Map<String, Object>) p1Instance.getBean();
            Assert.assertEquals(P1, propMap.get(NAME_TAG));
            Assert.assertEquals(V1, propMap.get(VALUE_TAG));
        }
        
        {
            Instance p2Instance = hub.getCurrentDatabase().getInstance(KINGDOM_PROP_TYPE, P2_INSTANCE);
            Assert.assertNotNull(p2Instance);
            
            Map<String, Object> propMap = (Map<String, Object>) p2Instance.getBean();
            Assert.assertEquals(P2, propMap.get(NAME_TAG));
            Assert.assertEquals(V2, propMap.get(VALUE_TAG));
        }
        
        {
            Instance p2Instance = hub.getCurrentDatabase().getInstance(KINGDOM_PROP_TYPE, P3_INSTANCE);
            Assert.assertNotNull(p2Instance);
            
            Map<String, Object> propMap = (Map<String, Object>) p2Instance.getBean();
            Assert.assertEquals(P3, propMap.get(NAME_TAG));
            Assert.assertEquals(V3, propMap.get(VALUE_TAG));
        }
        
        {
            Instance scienceInstance = hub.getCurrentDatabase().getInstance(SCIENTIST_TYPE, DARWIN_INSTANCE);
            Assert.assertNotNull(scienceInstance);
            
            Map<String, Object> propMap = (Map<String, Object>) scienceInstance.getBean();
            Assert.assertEquals(DARWIN_NAME, propMap.get(NAME_TAG));
            Assert.assertEquals(NATURALIST_FIELD, propMap.get(FIELD_TAG));
        }
    }
}
