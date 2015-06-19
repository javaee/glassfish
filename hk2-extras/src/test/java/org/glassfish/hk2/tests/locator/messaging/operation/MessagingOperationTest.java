/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.messaging.operation;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * These are tests for the combination of HK2 messaging and
 * HK2 Operation scope working together
 * 
 * @author jwells
 *
 */
public class MessagingOperationTest {
    private final static EventReceivingOperation OPERATION = new EventReceivingOperationImpl();
    
    private static ServiceLocator createLocator(Class<?>... clazzes) {
        ServiceLocator locator = Utilities.getUniqueLocator(clazzes);
        ExtrasUtilities.enableOperations(locator);
        ExtrasUtilities.enableTopicDistribution(locator);
        
        return locator;
    }
    
    /**
     * Tests that events are not sent to closed operation
     * services
     */
    @Test
    public void testEventsNotSentToClosedOperation() {
        ServiceLocator locator = createLocator(
                EventReceivingOperationContext.class,
                EventReceivingService.class,
                Publisher.class);
        
        OperationManager manager = locator.getService(OperationManager.class);
        Publisher publisher = locator.getService(Publisher.class);
        
        OperationHandle<EventReceivingOperation> opHandle = manager.createAndStartOperation(OPERATION);
        EventReceivingService ers = locator.getService(EventReceivingService.class);
        int id0 = ers.doOperation();  // unproxies it
        
        publisher.publish(0);
        
        opHandle.closeOperation();
        
        // Second instance
        opHandle = manager.createAndStartOperation(OPERATION);
        ers = locator.getService(EventReceivingService.class);
        
        int id1 = ers.doOperation();
        
        publisher.publish(1);
        
        opHandle.closeOperation();
        
        publisher.publish(2);
        
        Map<Integer, List<Integer>> eventMap = EventReceivingService.getEventMap();
        
        Assert.assertEquals(2, eventMap.size());
        
        List<Integer> firstEvents = eventMap.get(id0);
        Assert.assertEquals(1, firstEvents.size());
        
        Assert.assertEquals(0, firstEvents.get(0).intValue());
        
        List<Integer> secondEvents = eventMap.get(id1);
        Assert.assertEquals(1, secondEvents.size());
        
        Assert.assertEquals(1, secondEvents.get(0).intValue());
    }
    
    /**
     * Tests that events are not sent to closed operation
     * services
     */
    @Test @org.junit.Ignore
    public void testEventsNotSentToClosedOperationWithFactory() {
        ServiceLocator locator = createLocator(
                EventReceivingOperationContext.class,
                EventReceivingFactory.class,
                Publisher.class);
        
        OperationManager manager = locator.getService(OperationManager.class);
        Publisher publisher = locator.getService(Publisher.class);
        
        OperationHandle<EventReceivingOperation> opHandle = manager.createAndStartOperation(OPERATION);
        
        // Causes factory to get invoked
        File firstFile = locator.getService(File.class);  
        
        publisher.publish(0);
        
        opHandle.closeOperation();
        
        // Second instance
        opHandle = manager.createAndStartOperation(OPERATION);
        
        // Causes different factory to get invoked
        File secondFile = locator.getService(File.class); 
        
        publisher.publish(1);
        
        opHandle.closeOperation();
        
        publisher.publish(2);
        
        Map<Integer, List<Integer>> eventMap = EventReceivingService.getEventMap();
        
        Assert.assertEquals(2, eventMap.size());
        
        List<Integer> firstEvents = eventMap.get(0);
        Assert.assertEquals(1, firstEvents.size());
        
        Assert.assertEquals(0, firstEvents.get(0).intValue());
        
        List<Integer> secondEvents = eventMap.get(1);
        Assert.assertEquals(1, secondEvents.size());
        
        Assert.assertEquals(1, secondEvents.get(0).intValue());
    }
    
    private static class EventReceivingOperationImpl extends AnnotationLiteral<EventReceivingOperation> implements EventReceivingOperation {
    }

}
