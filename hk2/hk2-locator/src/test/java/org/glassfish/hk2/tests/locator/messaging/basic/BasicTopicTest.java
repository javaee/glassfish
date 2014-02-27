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

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BasicTopicTest {
    @Test
    public void testEventDistributedToAllSubscribers() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
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

}
