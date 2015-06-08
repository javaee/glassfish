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

package org.glassfish.hk2.tests.locator.customresolver;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author jwells
 *
 */
public class CustomResolverTest {
    private final static String TEST_NAME = "CustomResolverTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new CustomResolverModule());

    /**
     * Tests custom resolution
     */
    @Test
    public void testCustomInjectResolver() {
        ServiceWithCustomInjections cwci = locator.getService(ServiceWithCustomInjections.class);
        Assert.assertNotNull(cwci);

        Assert.assertTrue(cwci.isValid());
    }

    /**
     * Tests custom resolution with the resolver on the constuctor (and only valid on the constructor)
     */
    @Test
    public void testConstructorOnly() {
        ConstructorOnlyInjectedService service = locator.getService(ConstructorOnlyInjectedService.class);
        Assert.assertNotNull(service);

        Assert.assertNotNull(service.getViaConstructor());
        Assert.assertNull(service.getViaMethod());
    }

    /**
     * Tests custom resolution with the resolver on the method (and only valid on the method)
     */
    @Test
    public void testMethodOnly() {
        MethodOnlyInjectedService service = locator.getService(MethodOnlyInjectedService.class);
        Assert.assertNotNull(service);

        Assert.assertNull(service.getViaConstructor());
        Assert.assertNotNull(service.getViaMethod());
    }

    /**
     * Tests custom resolution with the resolver on both the constructor and the method
     */
    @Test
    public void testBothMethodAndConstructor() {
        ParameterInjectionService service = locator.getService(ParameterInjectionService.class);
        Assert.assertNotNull(service);

        Assert.assertNotNull(service.getViaConstructor());
        Assert.assertNotNull(service.getViaMethod());
    }

    /**
     * Tests custom resolution with two different resolvers and injection annotations
     */
    @Test
    public void testDifferentParametersInConstructor() {
        ParameterABInjectionService service = locator.getService(ParameterABInjectionService.class);
        Assert.assertNotNull(service);

        Assert.assertEquals("Parameter A", service.getParameterA());
        Assert.assertEquals("Parameter B", service.getParameterB());
        Assert.assertEquals("Parameter A", service.getAnotherParameterA());
    }
    
    /**
     * Tests custom resolution
     */
    @Test // @org.junit.Ignore
    public void testCustomInjectResolverInChild() {
        ServiceLocator child = LocatorHelper.create(locator);
        
        ServiceLocatorUtilities.addClasses(child, ServiceWithCustomInjections2.class);
        
        ServiceWithCustomInjections2 cwci = child.getService(ServiceWithCustomInjections2.class);
        Assert.assertNotNull(cwci);

        Assert.assertTrue(cwci.isValid());
        
        child.shutdown();
        
        ServiceWithCustomInjections cwci1 = locator.getService(ServiceWithCustomInjections.class);
        Assert.assertNotNull(cwci1);

        Assert.assertTrue(cwci1.isValid());
    }

}
