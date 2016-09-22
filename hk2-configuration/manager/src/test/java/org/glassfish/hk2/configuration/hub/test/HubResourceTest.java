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
package org.glassfish.hk2.configuration.hub.test;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.CommitFailedException;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.PrepareFailedException;
import org.glassfish.hk2.configuration.hub.api.RollbackFailedException;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class HubResourceTest extends HubTestBase {
    private final static String EMPTY_TYPE = "EmptyType";
    private final static String TYPE_TWELVE = "TypeTwelve";
    
    private final static String NAME_PROPERTY = "name";
    
    private final static String ALICE = "Alice";
    private final static String BOB = "Bob";
    private final static String CAROL = "Carol";
    private final static String DAVE = "Dave";
    
    public final static String PREPARE_FAIL_MESSAGE = "Expected prepare exception";
    public final static String COMMIT_FAIL_MESSAGE = "Expected commit exception";
    
    private Map<String, Object> oneFieldBeanLikeMap = new HashMap<String, Object>();
    
    @Before
    public void before() {
        super.before();
        
        oneFieldBeanLikeMap.put(NAME_PROPERTY, ALICE);
    }
    
    /**
     * Tests we can add an empty type to the database
     */
    @Test
    @org.junit.Ignore
    public void testAddEmptyType() {
        Assert.assertNull(hub.getCurrentDatabase().getType(EMPTY_TYPE));
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.addType(EMPTY_TYPE);
        
        DynamicConfiguration cd = dcs.createDynamicConfiguration();
        
        cd.registerTwoPhaseResources(wbd.getTwoPhaseResource());
        
        cd.commit();
        
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
    
    private static void checkInstances(HashSet<String> checkMe) {
        Assert.assertTrue(checkMe.contains(ALICE));
        Assert.assertTrue(checkMe.contains(BOB));
        Assert.assertTrue(checkMe.contains(CAROL));
        Assert.assertEquals(3, checkMe.size());
        
        checkMe.clear();
    }
    
    /**
     * Tests that all listeners are called, sunny day scenario
     */
    @Test
    @org.junit.Ignore
    public void testAllListenersPrepareAndCommitInvoked() {
        AbstractCountingListener listener1 = new AbstractCountingListener();
        AbstractCountingListener listener2 = new AbstractCountingListener();
        AbstractCountingListener listener3 = new AbstractCountingListener();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener1));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener2));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener3));
        
        try {
            GenericJavaBean newBean = new GenericJavaBean();
            
            addTypeAndInstance(TYPE_TWELVE, DAVE, newBean, true);
            
            Assert.assertEquals(1, listener1.getNumPreparesCalled());
            Assert.assertEquals(1, listener1.getNumCommitsCalled());
            Assert.assertEquals(0, listener1.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener2.getNumPreparesCalled());
            Assert.assertEquals(1, listener2.getNumCommitsCalled());
            Assert.assertEquals(0, listener2.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener3.getNumPreparesCalled());
            Assert.assertEquals(1, listener3.getNumCommitsCalled());
            Assert.assertEquals(0, listener3.getNumRollbackCalled());
        }
        finally {
            for (ActiveDescriptor<?> removeMe : added) {
                ServiceLocatorUtilities.removeOneDescriptor(testLocator, removeMe);
            }
            
            removeType(TYPE_TWELVE, true);
        }
        
    }
    
    /**
     * Tests that all listeners are called, sunny day scenario
     */
    @Test
    @org.junit.Ignore
    public void testMiddleListenerThrowsExceptionInPrepare() {
        AbstractCountingListener listener1 = new AbstractCountingListener();
        PrepareFailListener listener2 = new PrepareFailListener();
        AbstractCountingListener listener3 = new AbstractCountingListener();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener1));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener2));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener3));
        
        try {
            GenericJavaBean newBean = new GenericJavaBean();
            
            try {
                addTypeAndInstance(TYPE_TWELVE, DAVE, newBean, true);
                Assert.fail("Prepare threw exception, but commit succeeded");
            }
            catch (MultiException me) {
                Assert.assertTrue(me.toString().contains(PREPARE_FAIL_MESSAGE));
                
                boolean found = false;
                for (Throwable inner : me.getErrors()) {
                    if (inner instanceof PrepareFailedException) {
                        Assert.assertFalse("Should only be ONE instance of PrepareFailedException, but there is at least two in " + me, found);
                        found = true;
                    }
                }
                
                Assert.assertTrue(found);
            }
            
            Assert.assertEquals(1, listener1.getNumPreparesCalled());
            Assert.assertEquals(0, listener1.getNumCommitsCalled());
            Assert.assertEquals(1, listener1.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener2.getNumPreparesCalled());
            Assert.assertEquals(0, listener2.getNumCommitsCalled());
            Assert.assertEquals(0, listener2.getNumRollbackCalled());
            
            Assert.assertEquals(0, listener3.getNumPreparesCalled());
            Assert.assertEquals(0, listener3.getNumCommitsCalled());
            Assert.assertEquals(0, listener3.getNumRollbackCalled());
        }
        finally {
            for (ActiveDescriptor<?> removeMe : added) {
                ServiceLocatorUtilities.removeOneDescriptor(testLocator, removeMe);
            }
            
            removeType(TYPE_TWELVE, true);
        }
    }
    
    /**
     * Tests that all listeners are called when one fails in commit
     */
    @Test
    @org.junit.Ignore
    public void testMiddleListenerThrowsExceptionInCommit() {
        AbstractCountingListener listener1 = new AbstractCountingListener();
        CommitFailListener listener2 = new CommitFailListener();
        AbstractCountingListener listener3 = new AbstractCountingListener();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener1));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener2));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener3));
        
        try {
            GenericJavaBean newBean = new GenericJavaBean();
            
            try {
                addTypeAndInstance(TYPE_TWELVE, DAVE, newBean, true);
                Assert.fail("Commit threw exception, but commit succeeded");
            }
            catch (MultiException me) {
                Assert.assertTrue(me.toString().contains(COMMIT_FAIL_MESSAGE));
                
                boolean found = false;
                for (Throwable inner : me.getErrors()) {
                    if (inner instanceof CommitFailedException) {
                        Assert.assertFalse("Should only be ONE instance of CommitFailedException, but there is at least two in " + me, found);
                        found = true;
                    }
                }
                
                Assert.assertTrue(found);
            }
            
            Assert.assertEquals(1, listener1.getNumPreparesCalled());
            Assert.assertEquals(1, listener1.getNumCommitsCalled());
            Assert.assertEquals(0, listener1.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener2.getNumPreparesCalled());
            Assert.assertEquals(1, listener2.getNumCommitsCalled());
            Assert.assertEquals(0, listener2.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener3.getNumPreparesCalled());
            Assert.assertEquals(1, listener3.getNumCommitsCalled());
            Assert.assertEquals(0, listener3.getNumRollbackCalled());
        }
        finally {
            for (ActiveDescriptor<?> removeMe : added) {
                ServiceLocatorUtilities.removeOneDescriptor(testLocator, removeMe);
            }
            
            removeType(TYPE_TWELVE, true);
        }
    }
    
    /**
     * Tests that all listeners are called when one fails in prepare and
     * several others fail in rollback
     */
    @Test
    @org.junit.Ignore
    public void testAnExceptionInPrepareAndSeveralRollbacksAllGetReported() {
        RollbackFailListener listener1 = new RollbackFailListener();
        AbstractCountingListener listener2 = new AbstractCountingListener();
        RollbackFailListener listener3 = new RollbackFailListener();
        PrepareFailListener listener4 = new PrepareFailListener();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener1));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener2));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener3));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener4));
        
        try {
            GenericJavaBean newBean = new GenericJavaBean();
            
            try {
                addTypeAndInstance(TYPE_TWELVE, DAVE, newBean, true);
                Assert.fail("Prepare threw exception, but commit succeeded");
            }
            catch (MultiException me) {
                Assert.assertTrue(me.toString().contains(PREPARE_FAIL_MESSAGE));
                
                boolean found = false;
                int rollbackErrorsReported = 0;
                for (Throwable inner : me.getErrors()) {
                    if (inner instanceof PrepareFailedException) {
                        Assert.assertFalse("Should only be ONE instance of PrepareFailedException, but there is at least two in " + me, found);
                        found = true;
                    }
                    else if (inner instanceof RollbackFailedException) {
                        rollbackErrorsReported++;
                    }
                }
                
                Assert.assertTrue(found);
                Assert.assertEquals(2, rollbackErrorsReported);
            }
            
            Assert.assertEquals(1, listener1.getNumPreparesCalled());
            Assert.assertEquals(0, listener1.getNumCommitsCalled());
            Assert.assertEquals(1, listener1.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener2.getNumPreparesCalled());
            Assert.assertEquals(0, listener2.getNumCommitsCalled());
            Assert.assertEquals(1, listener2.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener3.getNumPreparesCalled());
            Assert.assertEquals(0, listener3.getNumCommitsCalled());
            Assert.assertEquals(1, listener3.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener4.getNumPreparesCalled());
            Assert.assertEquals(0, listener4.getNumCommitsCalled());
            Assert.assertEquals(0, listener4.getNumRollbackCalled());
        }
        finally {
            for (ActiveDescriptor<?> removeMe : added) {
                ServiceLocatorUtilities.removeOneDescriptor(testLocator, removeMe);
            }
            
            removeType(TYPE_TWELVE, true);
        }
    }
    
    /**
     * Tests that all commit errors are reported
     */
    @Test
    @org.junit.Ignore
    public void testMultipleCommitErrorsAllGetReported() {
        CommitFailListener listener1 = new CommitFailListener();
        CommitFailListener listener2 = new CommitFailListener();
        
        LinkedList<ActiveDescriptor<?>> added = new LinkedList<ActiveDescriptor<?>>();
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener1));
        added.add(ServiceLocatorUtilities.addOneConstant(testLocator, listener2));
        
        try {
            GenericJavaBean newBean = new GenericJavaBean();
            
            try {
                addTypeAndInstance(TYPE_TWELVE, DAVE, newBean, true);
                Assert.fail("Prepare threw exception, but commit succeeded");
            }
            catch (MultiException me) {
                Assert.assertTrue(me.toString().contains(COMMIT_FAIL_MESSAGE));
                
                int commitErrorsReported = 0;
                for (Throwable inner : me.getErrors()) {
                    if (inner instanceof CommitFailedException) {
                        commitErrorsReported++;
                    }
                }
                
                Assert.assertEquals(2, commitErrorsReported);
            }
            
            Assert.assertEquals(1, listener1.getNumPreparesCalled());
            Assert.assertEquals(1, listener1.getNumCommitsCalled());
            Assert.assertEquals(0, listener1.getNumRollbackCalled());
            
            Assert.assertEquals(1, listener2.getNumPreparesCalled());
            Assert.assertEquals(1, listener2.getNumCommitsCalled());
            Assert.assertEquals(0, listener2.getNumRollbackCalled());
        }
        finally {
            for (ActiveDescriptor<?> removeMe : added) {
                ServiceLocatorUtilities.removeOneDescriptor(testLocator, removeMe);
            }
            
            removeType(TYPE_TWELVE, true);
        }
    }

}
