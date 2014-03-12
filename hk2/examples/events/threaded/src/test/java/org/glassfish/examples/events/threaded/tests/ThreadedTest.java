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
package org.glassfish.examples.events.threaded.tests;

import junit.framework.Assert;

import org.glassfish.examples.events.threaded.ThreadedEventDistributor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

/**
 * Tests for the ThreadedEventDistributor
 * 
 * @author jwells
 *
 */
public class ThreadedTest {
    private final static ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
    
    private final ServiceLocator locator = factory.create(null);
    
    /**
     * Ensures that the event is given to the subscriber on a different thread
     * from the publisher
     * 
     * @throws InterruptedException
     */
    @Test
    public void testEventDeliveredOnADifferentThread() throws InterruptedException {
        // Adds in the default event implementation
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                EventPublisherService.class,
                EventSubscriberService.class,
                ThreadedEventDistributor.class);
        
        EventSubscriberService subscriber = locator.getService(EventSubscriberService.class);
        EventPublisherService publisher = locator.getService(EventPublisherService.class);
        
        // This is my current thread, should  NOT be the same thread as the subscriber method call
        long myThreadId = Thread.currentThread().getId();
        
        // Publish the event
        publisher.publishEvent();
        
        long subscriberThreadId = subscriber.getEventThread();
        
        Assert.assertNotSame("Should have had different threadId from " + myThreadId,
                myThreadId, subscriberThreadId);
    }
}
