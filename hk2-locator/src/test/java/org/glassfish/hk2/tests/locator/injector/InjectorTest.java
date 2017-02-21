/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.injector;

import java.lang.reflect.Method;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.MethodParameterImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InjectorTest {
    private final static String TEST_NAME = "InjectorTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new InjectorModule());
    
    /**
     * This only creates the object, it does not inject it further, and does not post construct it
     */
    @Test
    public void testCreateOnly() {
        DontManageMe dmm = locator.create(DontManageMe.class);
        Assert.assertNotNull(dmm);
        
        Assert.assertNotNull(dmm.getByConstructor());
        Assert.assertNull(dmm.getByField());
        Assert.assertNull(dmm.getByMethod());
        Assert.assertNull(dmm.getSpecialService());
        Assert.assertNull(dmm.getSecondMethod());
        Assert.assertNull(dmm.getSecondSpecial());
        Assert.assertNull(dmm.getUnknown());
        Assert.assertFalse(dmm.isPostConstructCalled());
        Assert.assertFalse(dmm.isPreDestroyCalled());
    }
    
    /**
     * This creates and injects the object, but does not post construct it
     */
    @Test
    public void testInjectOnly() {
        DontManageMe dmm = locator.create(DontManageMe.class);
        locator.inject(dmm);
        
        Assert.assertNotNull(dmm.getByConstructor());
        Assert.assertNotNull(dmm.getByField());
        Assert.assertNotNull(dmm.getByMethod());
        Assert.assertNotNull(dmm.getSpecialService());
        Assert.assertNotNull(dmm.getSecondMethod());
        Assert.assertNotNull(dmm.getSecondSpecial());
        Assert.assertNull(dmm.getUnknown());
        Assert.assertFalse(dmm.isPostConstructCalled());
        Assert.assertFalse(dmm.isPreDestroyCalled());
    }
    
    /**
     * This creates, injects and post constructs the object
     */
    @Test
    public void testPostConstructOnly() {
        DontManageMe dmm = locator.create(DontManageMe.class);
        locator.inject(dmm);
        locator.postConstruct(dmm);
        
        Assert.assertNotNull(dmm.getByConstructor());
        Assert.assertNotNull(dmm.getByField());
        Assert.assertNotNull(dmm.getByMethod());
        Assert.assertNotNull(dmm.getSpecialService());
        Assert.assertNotNull(dmm.getSecondMethod());
        Assert.assertNotNull(dmm.getSecondSpecial());
        Assert.assertNull(dmm.getUnknown());
        Assert.assertTrue(dmm.isPostConstructCalled());
        Assert.assertFalse(dmm.isPreDestroyCalled());
    }
    
    /**
     * This creates, injects, post constructs and pre destroys the object
     */
    @Test
    public void testPreDestroyOnly() {
        DontManageMe dmm = locator.create(DontManageMe.class);
        locator.inject(dmm);
        locator.postConstruct(dmm);
        locator.preDestroy(dmm);
        
        Assert.assertNotNull(dmm.getByConstructor());
        Assert.assertNotNull(dmm.getByField());
        Assert.assertNotNull(dmm.getByMethod());
        Assert.assertNotNull(dmm.getSpecialService());
        Assert.assertNotNull(dmm.getSecondMethod());
        Assert.assertNotNull(dmm.getSecondSpecial());
        Assert.assertNull(dmm.getUnknown());
        Assert.assertTrue(dmm.isPostConstructCalled());
        Assert.assertTrue(dmm.isPreDestroyCalled());
    }
    
    /**
     * This creates, injects and post constructs the object
     */
    @Test
    public void testNoPostConstructOrPreDestroy() {
        NoPostConstruct npc = locator.create(NoPostConstruct.class);
        locator.inject(npc);  // Nothing to inject, should work anyway
        locator.postConstruct(npc);  // No postConstruct, should work anyway
        locator.preDestroy(npc);  // No preDestroy, should work anyway
    }
    
    /**
     * This creates, injects and post constructs the object
     */
    @Test
    public void testImplementsLifecycleAPI() {
        ImplementsLifecycleInterfaces lii = locator.create(ImplementsLifecycleInterfaces.class);
        Assert.assertFalse(lii.isPostCalled());
        Assert.assertFalse(lii.isPreCalled());
        
        locator.postConstruct(lii);
        Assert.assertTrue(lii.isPostCalled());
        Assert.assertFalse(lii.isPreCalled());
        
        locator.preDestroy(lii);
        Assert.assertTrue(lii.isPostCalled());
        Assert.assertTrue(lii.isPreCalled());
    }
    
    /**
     * Tests an assisted injection
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAssistedInjection() throws Exception {
        AssistedInjectionService ais = new AssistedInjectionService();
        
        Method method = ais.getClass().getMethod("aMethod", Event.class,
                SpecialService.class, SimpleService.class, double.class,
                UnknownService.class);
        
        Event event = new Event();
        Double fooMe = new Double(2.71);
        
        locator.assistedInject(ais, method, new MethodParameterImpl(0, event), new MethodParameterImpl(3, fooMe));
        
        Assert.assertEquals(event, ais.getEvent());
        Assert.assertEquals(fooMe, new Double(ais.getFoo()));
        Assert.assertNotNull(ais.getSimple());
        Assert.assertNotNull(ais.getSpecial());
        Assert.assertNull(ais.getUnknown());
    }
    
    /**
     * Tests an assisted injection with a root
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    // @org.junit.Ignore
    public void testAssistedInjectionWithRoot() throws Exception {
        AssistedInjectionService ais = new AssistedInjectionService();
        
        Method method = ais.getClass().getMethod("aMethod", Event.class,
                SpecialService.class, SimpleService.class, double.class,
                UnknownService.class);
        
        Event event = new Event();
        Double fooMe = new Double(2.71);
        
        ActiveDescriptor<?> descriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(ServiceLocator.class.getName()));
        Assert.assertNotNull(descriptor);
        
        ServiceHandle<ServiceLocator> root = locator.getServiceHandle((ActiveDescriptor<ServiceLocator>) descriptor);
        
        Assert.assertTrue(root.getSubHandles().isEmpty());
        
        locator.assistedInject(ais, method, root, new MethodParameterImpl(0, event), new MethodParameterImpl(3, fooMe));
        
        Assert.assertEquals(event, ais.getEvent());
        Assert.assertEquals(fooMe, new Double(ais.getFoo()));
        Assert.assertNotNull(ais.getSimple());
        Assert.assertNotNull(ais.getSpecial());
        Assert.assertNull(ais.getUnknown());
        
        Assert.assertEquals(1, root.getSubHandles().size());
        
        ServiceHandle<?> found = root.getSubHandles().get(0);
        Assert.assertNotNull(found);
        
        ActiveDescriptor<?> foundAD = found.getActiveDescriptor();
        Assert.assertEquals(SimpleService.class.getName(), foundAD.getImplementation());
    }
}
