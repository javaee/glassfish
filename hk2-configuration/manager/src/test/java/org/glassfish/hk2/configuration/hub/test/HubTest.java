/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.hub.test;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.ManagerUtilities;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * @author jwells
 *
 */
public class HubTest extends HK2Runner {
    private final static String EMPTY_TYPE = "EmptyType";
    private final static String ONE_INSTANCE_TYPE = "OneInstanceType";
    private final static String TYPE_TWO = "TypeTwo";
    private final static String TYPE_THREE = "TypeThree";
    private final static String TYPE_FOUR = "TypeFour";
    private final static String TYPE_FIVE = "TypeFive";
    private final static String TYPE_SIX = "TypeSix";
    private final static String TYPE_SEVEN = "TypeSeven";
    private final static String TYPE_EIGHT = "TypeEight";
    private final static String TYPE_NINE = "TypeNine";
    private final static String TYPE_TEN = "TypeTen";
    private final static String TYPE_ELEVEN = "TypeEleven";
    
    private final static String NAME_PROPERTY = "name";
    private final static String OTHER_PROPERTY = "other";
    
    private final static String ALICE = "Alice";
    private final static String BOB = "Bob";
    private final static String CAROL = "Carol";
    
    private final static String OTHER_PROPERTY_VALUE1 = "value1";
    private final static String OTHER_PROPERTY_VALUE2 = "value2";
    
    private Hub hub;
    private Map<String, Object> oneFieldBeanLikeMap = new HashMap<String, Object>();
    
    @Before
    public void before() {
        super.before();
        
        // This is necessary to make running in an IDE easier
        ManagerUtilities.enableConfigurationHub(testLocator);
        
        this.hub = testLocator.getService(Hub.class);
        
        oneFieldBeanLikeMap.put(NAME_PROPERTY, ALICE);
    }
    
    /**
     * Tests we can add an empty type to the database
     */
    @Test
    public void testAddEmptyType() {
        Assert.assertNull(hub.getCurrentDatabase().getType(EMPTY_TYPE));
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.addType(EMPTY_TYPE);
        
        wbd.commit();
        
        try {
            Type emptyType = hub.getCurrentDatabase().getType(EMPTY_TYPE);
            
            Assert.assertNotNull(emptyType);
            Assert.assertEquals(0, emptyType.getInstances().size());
        }
        finally {
            // Cleanup
            wbd = hub.getWriteableDatabaseCopy();
            wbd.removeType(EMPTY_TYPE);
            wbd.commit();
        }
        
    }
    
    /**
     * Tests we can add an empty type to the database with a listener
     */
    @Test
    public void testAddEmptyTypeWithListener() {
        Assert.assertNull(hub.getCurrentDatabase().getType(EMPTY_TYPE));
        
        GenericBeanDatabaseUpdateListener listener = new GenericBeanDatabaseUpdateListener();
        hub.addListener(listener);
        
        WriteableBeanDatabase wbd = null;
        
        try {
            Hub hub = testLocator.getService(Hub.class);
            
            wbd = hub.getWriteableDatabaseCopy();
            wbd.addType(EMPTY_TYPE);
        
            wbd.commit();
        
            Type emptyType = hub.getCurrentDatabase().getType(EMPTY_TYPE);
            
            Assert.assertNull(listener.getLastCommitMessage());
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            Change change = changes.get(0);
            
            Assert.assertEquals(Change.ChangeCategory.ADD_TYPE, change.getChangeCategory());
            Assert.assertEquals(emptyType.getName(), change.getChangeType().getName());
            Assert.assertEquals(0, change.getChangeType().getInstances().size());
            Assert.assertNull(change.getInstanceKey());
            Assert.assertNull(change.getInstanceValue());
            Assert.assertNull(change.getModifiedProperties());
            Assert.assertNull(change.getOriginalInstanceValue());
        }
        finally {
            // Cleanup
            if (wbd != null) {
                wbd = hub.getWriteableDatabaseCopy();
                wbd.removeType(EMPTY_TYPE);
                wbd.commit();
            }
            
            hub.removeListener(listener);
        }
        
    }
    
    /**
     * Tests adding a type with one instance
     */
    @Test
    public void addNewTypeWithOneInstance() {
        Assert.assertNull(hub.getCurrentDatabase().getType(ONE_INSTANCE_TYPE));
        
        GenericBeanDatabaseUpdateListener listener = new GenericBeanDatabaseUpdateListener();
        hub.addListener(listener);
        
        WriteableBeanDatabase wbd = null;
        
        try {
        
            wbd = hub.getWriteableDatabaseCopy();
            WriteableType wt = wbd.addType(ONE_INSTANCE_TYPE);
            
            wt.addInstance(ALICE, oneFieldBeanLikeMap);
        
            Object commitMessage = new Object();
            wbd.commit(commitMessage);
        
            Type oneInstanceType = hub.getCurrentDatabase().getType(ONE_INSTANCE_TYPE);
            
            Assert.assertEquals(commitMessage, listener.getLastCommitMessage());
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(2, changes.size());
            
            {
                Change typeChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.ADD_TYPE, typeChange.getChangeCategory());
                Assert.assertEquals(oneInstanceType.getName(), typeChange.getChangeType().getName());
                Assert.assertEquals(1, typeChange.getChangeType().getInstances().size());
                Assert.assertNull(typeChange.getInstanceKey());
                Assert.assertNull(typeChange.getInstanceValue());
                Assert.assertNull(typeChange.getModifiedProperties());
                Assert.assertNull(typeChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(1);
            
                Assert.assertEquals(Change.ChangeCategory.ADD_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(oneInstanceType.getName(), instanceChange.getChangeType().getName());
                Assert.assertEquals(1, instanceChange.getChangeType().getInstances().size());
                Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                Assert.assertEquals(oneFieldBeanLikeMap, instanceChange.getInstanceValue().getBean());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
        }
        finally {
            // Cleanup
            if (wbd != null) {
                wbd = hub.getWriteableDatabaseCopy();
                wbd.removeType(ONE_INSTANCE_TYPE);
                wbd.commit();
            }
            
            hub.removeListener(listener);
        }
        
    }
    
    private void addType(String typeName) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        wbd.addType(typeName);
        
        wbd.commit();
    }
    
    private void addTypeAndInstance(String typeName, String instanceKey, Object instanceValue) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
        wt.addInstance(instanceKey, instanceValue);
        
        wbd.commit();
    }
    
    private void addTypeAndInstance(String typeName, String instanceKey, Object instanceValue, Object metadata) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
        wt.addInstance(instanceKey, instanceValue, metadata);
        
        wbd.commit();
    }
    
    private void removeType(String typeName) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        wbd.removeType(typeName);
        
        wbd.commit();
    }
    
    /**
     * Tests adding an instance to an existing a type
     */
    @Test
    public void addInstanceToExistingType() {
        addType(ONE_INSTANCE_TYPE);
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
        
            wbd = hub.getWriteableDatabaseCopy();
            WriteableType wt = wbd.getWriteableType(ONE_INSTANCE_TYPE);
            Assert.assertNotNull(wt);
            
            wt.addInstance(ALICE, oneFieldBeanLikeMap);
        
            wbd.commit();
        
            Type oneInstanceType = hub.getCurrentDatabase().getType(ONE_INSTANCE_TYPE);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.ADD_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(oneInstanceType.getName(), instanceChange.getChangeType().getName());
                Assert.assertEquals(1, instanceChange.getChangeType().getInstances().size());
                Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                Assert.assertEquals(oneFieldBeanLikeMap, instanceChange.getInstanceValue().getBean());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            if (wbd != null) {
                removeType(ONE_INSTANCE_TYPE);
            }
            
        }
    }
    
    /**
     * Tests adding an instance to an existing a type
     */
    @Test
    public void testModifyProperty() {
        GenericJavaBean oldBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1);
        addTypeAndInstance(TYPE_TWO, ALICE, oldBean);
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
        
            wbd = hub.getWriteableDatabaseCopy();
            WriteableType wt = wbd.getWriteableType(TYPE_TWO);
            Assert.assertNotNull(wt);
            
            GenericJavaBean newBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE2);
            PropertyChangeEvent[] result = wt.modifyInstance(ALICE, newBean,
                    new PropertyChangeEvent(newBean, OTHER_PROPERTY, OTHER_PROPERTY_VALUE1, OTHER_PROPERTY_VALUE2));
            
            Assert.assertEquals(1, result.length);
            Assert.assertEquals(result[0].getNewValue(), OTHER_PROPERTY_VALUE2);
            Assert.assertEquals(result[0].getOldValue(), OTHER_PROPERTY_VALUE1);
            Assert.assertEquals(result[0].getPropertyName(), OTHER_PROPERTY);
            Assert.assertEquals(result[0].getSource(), newBean);
        
            wbd.commit();
        
            Type typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.MODIFY_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TWO, instanceChange.getChangeType().getName());
                Assert.assertEquals(1, instanceChange.getChangeType().getInstances().size());
                Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                Assert.assertEquals(newBean, instanceChange.getInstanceValue().getBean());
                Assert.assertEquals(oldBean, instanceChange.getOriginalInstanceValue().getBean());
                
                List<PropertyChangeEvent> propertyChanges = instanceChange.getModifiedProperties();
                Assert.assertNotNull(propertyChanges);
                Assert.assertEquals(1, propertyChanges.size());
                
                PropertyChangeEvent pce = propertyChanges.get(0);
                
                Assert.assertEquals(OTHER_PROPERTY, pce.getPropertyName());
                Assert.assertEquals(OTHER_PROPERTY_VALUE1, pce.getOldValue());
                Assert.assertEquals(OTHER_PROPERTY_VALUE2, pce.getNewValue());
                Assert.assertEquals(newBean, pce.getSource());
            }
            
            typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            GenericJavaBean bean = (GenericJavaBean) typeTwo.getInstance(ALICE).getBean();
            
            Assert.assertEquals(ALICE, bean.getName());
            Assert.assertEquals(OTHER_PROPERTY_VALUE2, bean.getOther());
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            if (wbd != null) {
                removeType(TYPE_TWO);
            }
            
        }
    }
    
    /**
     * Tests modifying a property with automatically generated change events
     */
    @Test
    public void testModifyPropertyWithAutomaticPropertyChangeEvent() {
        GenericJavaBean oldBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1);
        addTypeAndInstance(TYPE_TWO, ALICE, oldBean);
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
        
            wbd = hub.getWriteableDatabaseCopy();
            WriteableType wt = wbd.getWriteableType(TYPE_TWO);
            Assert.assertNotNull(wt);
            
            GenericJavaBean newBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE2);
            PropertyChangeEvent[] result = wt.modifyInstance(ALICE, newBean);
            
            Assert.assertEquals(1, result.length);
            Assert.assertEquals(result[0].getNewValue(), OTHER_PROPERTY_VALUE2);
            Assert.assertEquals(result[0].getOldValue(), OTHER_PROPERTY_VALUE1);
            Assert.assertEquals(result[0].getPropertyName(), OTHER_PROPERTY);
            Assert.assertEquals(result[0].getSource(), newBean);
        
            wbd.commit();
        
            Type typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.MODIFY_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TWO, instanceChange.getChangeType().getName());
                Assert.assertEquals(1, instanceChange.getChangeType().getInstances().size());
                Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                Assert.assertEquals(newBean, instanceChange.getInstanceValue().getBean());
                Assert.assertEquals(oldBean, instanceChange.getOriginalInstanceValue().getBean());
                
                List<PropertyChangeEvent> propertyChanges = instanceChange.getModifiedProperties();
                Assert.assertNotNull(propertyChanges);
                Assert.assertEquals(1, propertyChanges.size());
                
                PropertyChangeEvent pce = propertyChanges.get(0);
                
                Assert.assertEquals(OTHER_PROPERTY, pce.getPropertyName());
                Assert.assertEquals(OTHER_PROPERTY_VALUE1, pce.getOldValue());
                Assert.assertEquals(OTHER_PROPERTY_VALUE2, pce.getNewValue());
                Assert.assertEquals(newBean, pce.getSource());
            }
            
            typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            GenericJavaBean bean = (GenericJavaBean) typeTwo.getInstance(ALICE).getBean();
            
            Assert.assertEquals(ALICE, bean.getName());
            Assert.assertEquals(OTHER_PROPERTY_VALUE2, bean.getOther());
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            if (wbd != null) {
                removeType(TYPE_TWO);
            }
            
        }
    }
    
    /**
     * Tests findOrAddWriteableType and other accessors
     */
    @Test
    public void testFindOrAdd() {
        GenericJavaBean addedBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1);
        addTypeAndInstance(TYPE_TWO, ALICE, addedBean);
        
        WriteableBeanDatabase wbd = null;
        
        try {
            wbd = hub.getWriteableDatabaseCopy();
            
            GenericJavaBean gjb = (GenericJavaBean) wbd.getInstance(TYPE_TWO, ALICE).getBean();
            Assert.assertNotNull(gjb);
            Assert.assertEquals(addedBean, gjb);
        
            WriteableType wt = wbd.findOrAddWriteableType(TYPE_TWO);
            Assert.assertNotNull(wt);
            
            gjb = (GenericJavaBean) wt.getInstance(ALICE).getBean();
            Assert.assertNotNull(gjb);
            Assert.assertEquals(addedBean, gjb);
            
            WriteableType wt3 = wbd.findOrAddWriteableType(TYPE_THREE);
            Assert.assertNotNull(wt3);
            
            gjb = (GenericJavaBean) wt3.getInstance(ALICE);
            Assert.assertNull(gjb);
        }
        finally {
            // Cleanup
            
            if (wbd != null) {
                removeType(TYPE_TWO);
            }
            
        }
    }
    
    /**
     * Tests removing an instance
     */
    @Test
    public void testRemoveInstance() {
        addTypeAndInstance(TYPE_TWO, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        addTypeAndInstance(TYPE_TWO, BOB, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
        
            wbd = hub.getWriteableDatabaseCopy();
            WriteableType wt = wbd.getWriteableType(TYPE_TWO);
            Assert.assertNotNull(wt);
            
            GenericJavaBean removed = (GenericJavaBean) wt.removeInstance(ALICE).getBean();
            Assert.assertNotNull(removed);
            Assert.assertEquals(ALICE, removed.getName());
        
            wbd.commit();
        
            Type typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TWO, instanceChange.getChangeType().getName());
                Assert.assertEquals(1, instanceChange.getChangeType().getInstances().size());
                Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                Assert.assertEquals(removed, instanceChange.getInstanceValue().getBean());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            typeTwo = hub.getCurrentDatabase().getType(TYPE_TWO);
            
            GenericJavaBean bean = (GenericJavaBean) typeTwo.getInstance(ALICE);
            Assert.assertNull(bean);
            
            // Make sure Bob is still there though!
            bean = (GenericJavaBean) typeTwo.getInstance(BOB).getBean();
            Assert.assertNotNull(bean);
            Assert.assertEquals(BOB, bean.getName());
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            if (wbd != null) {
                removeType(TYPE_TWO);
            }
            
        }
    }
    
    /**
     * Tests removing an type that has current instances
     */
    @Test
    public void testRemoveTypeWithInstances() {
        addTypeAndInstance(TYPE_FOUR, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        addTypeAndInstance(TYPE_FOUR, BOB, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
        
            wbd = hub.getWriteableDatabaseCopy();
            
            Type removed = wbd.removeType(TYPE_FOUR);
            Assert.assertNotNull(removed);
            Assert.assertEquals(TYPE_FOUR, removed.getName());
        
            wbd.commit();
        
            Type typeFour = hub.getCurrentDatabase().getType(TYPE_FOUR);
            Assert.assertNull(typeFour);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(3, changes.size());
            
            boolean firstChangeWasAlice = false;
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_FOUR, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                if (instanceChange.getInstanceKey().equals(ALICE)) {
                    firstChangeWasAlice = true;
                }
                else if (instanceChange.getInstanceKey().equals(BOB)) {
                    firstChangeWasAlice = false;
                }
                else {
                    Assert.fail("Unknown instance name " + instanceChange.getInstanceKey());
                }
                
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(1);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_FOUR, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                if (firstChangeWasAlice) {
                    Assert.assertEquals(BOB, instanceChange.getInstanceKey());
                }
                else {
                    Assert.assertEquals(ALICE, instanceChange.getInstanceKey());
                }
                
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(2);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_TYPE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_FOUR, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
        }
    }
    
    private static void checkInstances(HashSet<String> checkMe) {
        Assert.assertTrue(checkMe.contains(ALICE));
        Assert.assertTrue(checkMe.contains(BOB));
        Assert.assertTrue(checkMe.contains(CAROL));
        Assert.assertEquals(3, checkMe.size());
        
        checkMe.clear();
    }
    
    /**
     * Tests removing an type with multiple types and multiple instances
     */
    @Test
    public void testRemoveMultipleTypeWithMultipleInstances() {
        addTypeAndInstance(TYPE_EIGHT, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        addTypeAndInstance(TYPE_NINE, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        addTypeAndInstance(TYPE_TEN, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        addTypeAndInstance(TYPE_ELEVEN, ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1));
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            
            wbd = hub.getWriteableDatabaseCopy();
            
            wbd.findOrAddWriteableType(TYPE_EIGHT).addInstance(BOB, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
            wbd.findOrAddWriteableType(TYPE_NINE).addInstance(BOB, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
            wbd.findOrAddWriteableType(TYPE_TEN).addInstance(BOB, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
            
            wbd.findOrAddWriteableType(TYPE_EIGHT).addInstance(CAROL, new GenericJavaBean(CAROL, OTHER_PROPERTY_VALUE1));
            wbd.findOrAddWriteableType(TYPE_NINE).addInstance(CAROL, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
            wbd.findOrAddWriteableType(TYPE_TEN).addInstance(CAROL, new GenericJavaBean(BOB, OTHER_PROPERTY_VALUE1));
            
            wbd.commit();
            
            hub.addListener(listener);
            
            wbd = hub.getWriteableDatabaseCopy();
            
            Type removed8 = wbd.removeType(TYPE_EIGHT);
            Assert.assertNotNull(removed8);
            Assert.assertEquals(TYPE_EIGHT, removed8.getName());
            
            Type removed10 = wbd.removeType(TYPE_TEN);
            Assert.assertNotNull(removed10);
            Assert.assertEquals(TYPE_TEN, removed10.getName());
            
            Type removed9 = wbd.removeType(TYPE_NINE);
            Assert.assertNotNull(removed9);
            Assert.assertEquals(TYPE_NINE, removed9.getName());
            
            wbd.commit();
        
            Type typeEight = hub.getCurrentDatabase().getType(TYPE_EIGHT);
            Assert.assertNull(typeEight);
            
            Type typeNine = hub.getCurrentDatabase().getType(TYPE_NINE);
            Assert.assertNull(typeNine);
            
            Type typeTen = hub.getCurrentDatabase().getType(TYPE_TEN);
            Assert.assertNull(typeTen);
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(12, changes.size());
            
            HashSet<String> instanceKeys = new HashSet<String>();
            {
                Change instanceChange = changes.get(0);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_EIGHT, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(1);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_EIGHT, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(2);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_EIGHT, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            checkInstances(instanceKeys);
            
            {
                Change instanceChange = changes.get(3);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_TYPE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_EIGHT, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(4);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TEN, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(5);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TEN, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(6);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TEN, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            checkInstances(instanceKeys);
            
            {
                Change instanceChange = changes.get(7);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_TYPE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_TEN, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(8);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_NINE, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(9);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_NINE, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            {
                Change instanceChange = changes.get(10);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_INSTANCE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_NINE, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                instanceKeys.add(instanceChange.getInstanceKey());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            checkInstances(instanceKeys);
            
            {
                Change instanceChange = changes.get(11);
            
                Assert.assertEquals(Change.ChangeCategory.REMOVE_TYPE, instanceChange.getChangeCategory());
                Assert.assertEquals(TYPE_NINE, instanceChange.getChangeType().getName());
                Assert.assertEquals(0, instanceChange.getChangeType().getInstances().size());
                Assert.assertNull(instanceChange.getModifiedProperties());
                Assert.assertNull(instanceChange.getOriginalInstanceValue());
            }
            
            
        }
        finally {
            // Cleanup
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            removeType(TYPE_ELEVEN);
        }
    }
    
    /**
     * Tests that I can set, get and set again and get again
     */
    @Test
    public void testMetadataOnType() {
        addType(TYPE_FIVE);
        
        try {
            Type five = hub.getCurrentDatabase().getType(TYPE_FIVE);
            Assert.assertNull(five.getMetadata());
        
            Object o1 = new Object();
            five.setMetadata(o1);
        
            Assert.assertEquals(o1, five.getMetadata());
        
            Object o2 = new Object();
            five.setMetadata(o2);
        
            Assert.assertEquals(o2, five.getMetadata());
        
            five.setMetadata(null);
            Assert.assertNull(five.getMetadata());
        }
        finally {
            removeType(TYPE_FIVE);
        }
        
    }
    
    /**
     * Tests that I can set, get and set again and get again
     */
    @Test
    public void testMetadataOnInstance() {
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
            
            wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType wt = wbd.findOrAddWriteableType(TYPE_SIX);
            
            Object metadata = new Object();
            wt.addInstance(ALICE, new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1), metadata);
            
            wbd.commit();
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(2, changes.size());
            
            Change instanceChange = changes.get(1);
            
            Assert.assertEquals(Change.ChangeCategory.ADD_INSTANCE, instanceChange.getChangeCategory());
            Instance instance = instanceChange.getInstanceValue();
            Assert.assertNotNull(instance);
            
            Assert.assertEquals(metadata, instance.getMetadata());
            
            Instance directInstance = hub.getCurrentDatabase().getInstance(TYPE_SIX, ALICE);
            Assert.assertNotNull(directInstance);
            
            Assert.assertEquals(metadata, directInstance.getMetadata());
            
            Object metadata2 = new Object();
            directInstance.setMetadata(metadata2);
            
            Assert.assertEquals(metadata2, directInstance.getMetadata());
            
            directInstance.setMetadata(null);
            Assert.assertNull(directInstance.getMetadata());
        }
        finally {
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            removeType(TYPE_SIX);
        }
        
    }
    
    /**
     * Tests that I can set, get and set again and get again
     */
    @Test
    public void testMetadataOnPropertyChange() {
        Object originalMetadata = new Object();
        GenericJavaBean oldBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE1);
        addTypeAndInstance(TYPE_SEVEN, ALICE, oldBean, originalMetadata);
        
        GenericBeanDatabaseUpdateListener listener = null;
        WriteableBeanDatabase wbd = null;
        
        try {
            listener = new GenericBeanDatabaseUpdateListener();
            hub.addListener(listener);
            
            GenericJavaBean newBean = new GenericJavaBean(ALICE, OTHER_PROPERTY_VALUE2);
            
            wbd = hub.getWriteableDatabaseCopy();
            
            WriteableType wt = wbd.getWriteableType(TYPE_SEVEN);
            
            wt.modifyInstance(ALICE, newBean);
            
            wbd.commit();
            
            List<Change> changes = listener.getLastSetOfChanges();
            
            Assert.assertEquals(1, changes.size());
            
            Change instanceChange = changes.get(0);
            
            Assert.assertEquals(Change.ChangeCategory.MODIFY_INSTANCE, instanceChange.getChangeCategory());
            Instance instance = instanceChange.getInstanceValue();
            Instance originalInstance = instanceChange.getOriginalInstanceValue();
            
            Assert.assertNotNull(instance);
            Assert.assertNotNull(originalInstance);
            
            Assert.assertEquals(originalMetadata, instance.getMetadata());
            Assert.assertEquals(originalMetadata, originalInstance.getMetadata());
        }
        finally {
            if (listener != null) {
                hub.removeListener(listener);
            }
            
            removeType(TYPE_SEVEN);
        }
        
    }

}
