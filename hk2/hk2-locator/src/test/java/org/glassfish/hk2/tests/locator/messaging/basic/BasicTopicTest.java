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
package org.glassfish.hk2.tests.locator.messaging.basic;

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DefaultTopicPublishResult;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BasicTopicTest {
    /**
     * Tests the most basic form of topic/subscriber
     */
    @Test
    public void testEventDistributedToAllSubscribers() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                ImmediateSubscriber.class,
                PerLookupSubscriber.class,
                SingletonSubscriber.class);
        
        FooPublisher publisher = locator.getService(FooPublisher.class);
        SingletonSubscriber singletonSubscriber = locator.getService(SingletonSubscriber.class);
        ImmediateSubscriber immediateSubscriber = locator.getService(ImmediateSubscriber.class);
        
        publisher.publishFoo(12);
        
        Foo singletonFoo = singletonSubscriber.getAndClearLastEvent();
        Assert.assertNotNull(singletonFoo);
        Assert.assertEquals(12, singletonFoo.getFooValue());
        
        Foo perLookupFoo1 = singletonSubscriber.getAndClearDependentLastEvent();
        Assert.assertNotNull(perLookupFoo1);
        Assert.assertEquals(12, perLookupFoo1.getFooValue());
        
        Foo immediateFoo = immediateSubscriber.getAndClearLastEvent();
        Assert.assertNotNull(immediateFoo);
        Assert.assertEquals(12, immediateFoo.getFooValue());
        
        Foo perLookupFoo2 = immediateSubscriber.getAndClearDependentLastEvent();
        Assert.assertNotNull(perLookupFoo2);
        Assert.assertEquals(12, perLookupFoo2.getFooValue());
    }
    
    /**
     * Tests a single subscriber with many different subscription methods
     */
    @Test
    public void testEventDistributedToAllSubscribersOnOneService() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                PerLookupService.class,
                SingletonService.class,
                SubscriberWithInjectionPoints.class);
        
        FooPublisher publisher = locator.getService(FooPublisher.class);
        
        SubscriberWithInjectionPoints subscriber = locator.getService(SubscriberWithInjectionPoints.class);
        
        publisher.publishFoo(0);
        
        subscriber.check();
        
    }
    
    /**
     * Tests a single subscriber subscribing to different Types
     */
    @Test
    public void testEventDistributionByType() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                ColorPublisher.class,
                DifferentTypesSubscriber.class);
        
        FooPublisher publisher = locator.getService(FooPublisher.class);
        ColorPublisher colorPublisher = locator.getService(ColorPublisher.class);
        
        DifferentTypesSubscriber subscriber = locator.getService(DifferentTypesSubscriber.class);
        
        // Will only activate the Foo subscription, not the Bar subscription
        publisher.publishFoo(1);
        
        Assert.assertEquals(1, subscriber.getFooValue());
        Assert.assertEquals(0, subscriber.getBarValue());
        Assert.assertNull(subscriber.getLastColorEvent());
        
        // Will activate both the Foo and Bar subscribers
        publisher.publishBar(1);
        
        Assert.assertEquals(3, subscriber.getFooValue());  // One for Foo subscriber, One for Bar subscriber, One from previous publish
        Assert.assertEquals(1, subscriber.getBarValue());  // One from the Bar subscriber
        Assert.assertNull(subscriber.getLastColorEvent());
        
        colorPublisher.publishBlackEvent();
        
        Assert.assertEquals(3, subscriber.getFooValue());  // One for Foo subscriber, One for Bar subscriber, One from previous publish
        Assert.assertEquals(1, subscriber.getBarValue());  // One from the Bar subscriber
        Assert.assertEquals(Color.BLACK, subscriber.getLastColorEvent());
        
    }
    
    /**
     * Tests a single subscriber subscribing to different Types
     */
    @Test
    public void testEventDistributionByQualifier() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                ColorPublisher.class,
                ColorSubscriber.class);
        
        ColorPublisher colorPublisher = locator.getService(ColorPublisher.class);
        
        ColorSubscriber subscriber = locator.getService(ColorSubscriber.class);
        
        colorPublisher.publishGreenEvent();
        
        Assert.assertEquals(1, subscriber.getGreenCount());
        Assert.assertEquals(0, subscriber.getRedCount());
        Assert.assertEquals(0, subscriber.getBlackCount());
        Assert.assertEquals(1, subscriber.getNotRedCount());
        
        colorPublisher.publishRedEvent();
        
        Assert.assertEquals(1, subscriber.getGreenCount());
        Assert.assertEquals(1, subscriber.getRedCount());
        Assert.assertEquals(0, subscriber.getBlackCount());
        Assert.assertEquals(1, subscriber.getNotRedCount());
        
        colorPublisher.publishBlackEvent();
        
        Assert.assertEquals(1, subscriber.getGreenCount());
        Assert.assertEquals(1, subscriber.getRedCount());
        Assert.assertEquals(1, subscriber.getBlackCount());
        Assert.assertEquals(2, subscriber.getNotRedCount());
    }
    
    /**
     * Tests that a factory that returns different classes on subsequent
     * calls gets events on all created objects
     */
    @Test
    public void testDifferentClassFactory() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                FooPublisher.class);
        
        FactoryDescriptors factoryDescriptors = BuilderHelper.link(GreekFactory.class).
            to(Greek.class.getName()).
            in(PerLookup.class.getName()).
            buildFactory(Singleton.class.getName());
        
        ServiceLocatorUtilities.addFactoryDescriptors(locator, false, factoryDescriptors);
        
        FooPublisher fooPublisher = locator.getService(FooPublisher.class);
        
        Greek greek1 = locator.getService(Greek.class);
        Greek greek2 = locator.getService(Greek.class);
        Greek greek3 = locator.getService(Greek.class);
        
        Assert.assertNotSame(greek1.getClass(), greek2.getClass());
        Assert.assertNotSame(greek1.getClass(), greek3.getClass());
        Assert.assertNotSame(greek2.getClass(), greek3.getClass());
        
        fooPublisher.publishFoo(3);
        
        Assert.assertEquals(3, greek1.getFooValue());
        Assert.assertEquals(3, greek2.getFooValue());
        Assert.assertEquals(3, greek3.getFooValue());
    }
    
    /**
     * Tests that unbound services no longer get notified
     */
    @Test
    public void testUnboundServiceNoLongerGetsNotified() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                ImmediateSubscriber.class,
                PerLookupSubscriber.class,
                SingletonSubscriber.class);
        
        FooPublisher publisher = locator.getService(FooPublisher.class);
        SingletonSubscriber singletonSubscriber = locator.getService(SingletonSubscriber.class);
        ImmediateSubscriber immediateSubscriber = locator.getService(ImmediateSubscriber.class);
        
        publisher.publishFoo(-12);
        
        // Now remove everything except the publisher
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.get(1));
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.get(2));
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.get(3));
        
        publisher.publishFoo(20);
        
        // These guys should still have -12 since they should NOT have received the
        // event setting the value to 20
        Foo singletonFoo = singletonSubscriber.getAndClearLastEvent();
        Assert.assertNotNull(singletonFoo);
        Assert.assertEquals(-12, singletonFoo.getFooValue());
        
        Foo perLookupFoo1 = singletonSubscriber.getAndClearDependentLastEvent();
        Assert.assertNotNull(perLookupFoo1);
        Assert.assertEquals(-12, perLookupFoo1.getFooValue());
        
        Foo immediateFoo = immediateSubscriber.getAndClearLastEvent();
        Assert.assertNotNull(immediateFoo);
        Assert.assertEquals(-12, immediateFoo.getFooValue());
        
        Foo perLookupFoo2 = immediateSubscriber.getAndClearDependentLastEvent();
        Assert.assertNotNull(perLookupFoo2);
        Assert.assertEquals(-12, perLookupFoo2.getFooValue());
    }
    
    /**
     * Tests that unbound services no longer get notified
     */
    @Test
    public void testDestroyedServiceNoLongerGetsNotified() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                PerLookupSubscriber.class,
                SingletonSubscriber.class);
        
        FooPublisher publisher = locator.getService(FooPublisher.class);
        
        ServiceHandle<SingletonSubscriber> singletonHandle = locator.getServiceHandle(SingletonSubscriber.class);
        SingletonSubscriber singletonSubscriber = singletonHandle.getService();
        
        publisher.publishFoo(-13);
        
        // Now destroy these services
        singletonHandle.destroy();
        
        publisher.publishFoo(21);
        
        // These guys should still have -13 since they should NOT have received the
        // event setting the value to 21
        Foo singletonFoo = singletonSubscriber.getAndClearLastEvent();
        Assert.assertNotNull(singletonFoo);
        Assert.assertEquals(-13, singletonFoo.getFooValue());
        
        Foo perLookupFoo1 = singletonSubscriber.getAndClearDependentLastEvent();
        Assert.assertNotNull(perLookupFoo1);
        Assert.assertEquals(-13, perLookupFoo1.getFooValue());
    }
    
    /**
     * Tests that a per lookup service is destroyed after being
     * given to a subscription method
     */
    @Test
    public void testPerLookupDestroyedAfterPassedToSubscriptionMethod() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator, FooPublisher.class,
                PerLookupService.class,
                ServiceWithPerLookupSubscription.class);
        
        ServiceWithPerLookupSubscription subscriber = locator.getService(ServiceWithPerLookupSubscription.class);
        FooPublisher publisher = locator.getService(FooPublisher.class);
        
        publisher.publishBar(10);
        
        Assert.assertTrue(subscriber.isSubscriptionServiceDead());
    }

}
