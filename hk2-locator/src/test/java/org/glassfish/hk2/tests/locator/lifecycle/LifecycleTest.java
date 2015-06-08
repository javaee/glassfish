/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.lifecycle;

import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class LifecycleTest {
    private final static String TEST_NAME = "LifecycleTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new LifecycleModule());
    
    private final static String MESSAGE_ONE = "One";
    private final static String MESSAGE_TWO = "Two";
    
    /**
     * Tests basic lifecycle notification
     */
    @Test
    public void testBasicLifecycleNotification() {
        Notifier alice = locator.getService(Notifier.class, Notifier.DEFAULT_NAME);
        
        // This notification should not be there, since the notifyee isn't instantiated yet
        alice.notify("NOT THERE");
        
        ServiceHandle<KnownInjecteeNotifyee> knownInjecteeHandle = locator.getServiceHandle(
                KnownInjecteeNotifyee.class);
        KnownInjecteeNotifyee knownInjectee = knownInjecteeHandle.getService();
        
        Assert.assertTrue(knownInjectee.getNotifications().isEmpty());
        
        alice.notify(MESSAGE_ONE);
        
        List<String> notifications = knownInjectee.getNotifications();
        Assert.assertEquals(1, notifications.size());
        
        String notification = notifications.get(0);
        
        Assert.assertTrue("Unknown notification message: " + notification, notification.contains(Notifier.DEFAULT_NAME));
        Assert.assertTrue("Unknown notification message: " + notification, notification.contains(MESSAGE_ONE));
        
        // Now destroy the known injectee, verify it no longer receives notifications
        knownInjecteeHandle.destroy();
        
        alice.notify(MESSAGE_TWO);
        
        // should NOT have changed
        notifications = knownInjectee.getNotifications();
        Assert.assertEquals(1, notifications.size());
        
        notification = notifications.get(0);
        
        Assert.assertTrue("Unknown notification message: " + notification, notification.contains(Notifier.DEFAULT_NAME));
        Assert.assertTrue("Unknown notification message: " + notification, notification.contains(MESSAGE_ONE)); 
    }
    
    @Test
    public void testTrueCreateOrderForDescriptors() {
        OrderedLifecycleListener ordered = locator.getService(OrderedLifecycleListener.class);
        ordered.clear();
        
        // We are getting Fire here, but Fire depends on Wind which depends on Earth so
        // the true creation ordering is Earth, Wind and Fire
        Fire fire = locator.getService(Fire.class);
        Assert.assertNotNull(fire);
        
        List<ActiveDescriptor<?>> orderedList = ordered.getOrderedList();
        
        Assert.assertEquals(3, orderedList.size());
        
        ActiveDescriptor<?> earthDescriptor = orderedList.get(0);
        Assert.assertEquals(earthDescriptor.getImplementation(), Earth.class.getName());
        
        ActiveDescriptor<?> windDescriptor = orderedList.get(1);
        Assert.assertEquals(windDescriptor.getImplementation(), Wind.class.getName());
        
        ActiveDescriptor<?> fireDescriptor = orderedList.get(2);
        Assert.assertEquals(fireDescriptor.getImplementation(), Fire.class.getName());
    }
    
    @Test
    public void testTrueCreateOrderForAddActiveDescriptorAsClass() {
        OrderedLifecycleListener ordered = locator.getService(OrderedLifecycleListener.class);
        ordered.clear();
        
        // We are getting Space here, but Space depends on Sand which depends on Water so
        // the true creation ordering is Water, Sand and Space
        Space space = locator.getService(Space.class);
        Assert.assertNotNull(space);
        
        List<ActiveDescriptor<?>> orderedList = ordered.getOrderedList();
        
        Assert.assertEquals(3, orderedList.size());
        
        ActiveDescriptor<?> waterDescriptor = orderedList.get(0);
        Assert.assertEquals(waterDescriptor.getImplementation(), Water.class.getName());
        
        ActiveDescriptor<?> sandDescriptor = orderedList.get(1);
        Assert.assertEquals(sandDescriptor.getImplementation(), Sand.class.getName());
        
        ActiveDescriptor<?> spaceDescriptor = orderedList.get(2);
        Assert.assertEquals(spaceDescriptor.getImplementation(), Space.class.getName());
    }

}
