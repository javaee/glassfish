/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.testing.test;

import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Tests basic functionality
 * 
 * @author jwells
 */
public class RunnerTest extends HK2Runner {
    /** Alice as a name */
    public final static String ALICE = "Alice";
    /** Bob as a name */
    public final static String BOB = "bob";
    private final static String CAROL = "Carol";  // default Named
    
    @Inject
    private SimpleService3 injectMe;
    
    @Before
    public void before() {
        LinkedList<String> packages = new LinkedList<String>();
        packages.add(this.getClass().getPackage().getName());
        
        initialize(this.getClass().getName(), packages, null);
    }
    
    /**
     * Tests that services in this package are found
     */
    @Test
    public void testAllPackageServicesInLocator() {
        SimpleService0 ss0 = testLocator.getService(SimpleService0.class);
        Assert.assertNotNull(ss0);
        
        SimpleService1 ss1 = testLocator.getService(SimpleService1.class);
        Assert.assertNotNull(ss1);
    }
    
    /**
     * Tests a class that is JIT resolved
     */
    @Test
    public void testJITInjectedClass() {
        
        ServiceInjectedWithGuyNotInPackage nonPackageService = testLocator.getService(ServiceInjectedWithGuyNotInPackage.class);
        Assert.assertNotNull(nonPackageService);
        Assert.assertNotNull(nonPackageService.getAltService());
    }
    
    /**
     * Tests a class via contract and qualifier
     */
    @Test
    public void testByContractAndQualifier() {
        SimpleService ss = testLocator.getService(SimpleService.class, new IAmAQualifierImpl());
        Assert.assertNotNull(ss);
        
        Assert.assertTrue(ss instanceof SimpleService2);
    }
    
    /**
     * Tests a class with a name on Service
     */
    @Test
    public void testNameFromService() {
        SimpleService ss = testLocator.getService(SimpleService.class, ALICE);
        Assert.assertNotNull(ss);
        
        Assert.assertTrue(ss instanceof Alice);
    }
    
    /**
     * Tests a class with a name on Named (explicitly set)
     */
    @Test
    public void testNameFromNamedExplicit() {
        SimpleService ss = testLocator.getService(SimpleService.class, BOB);
        Assert.assertNotNull(ss);
        
        Assert.assertTrue(ss instanceof Bob);
    }
    
    /**
     * Tests a class with a name on Named (default set)
     */
    @Test
    public void testNameFromNamedDefault() {
        SimpleService ss = testLocator.getService(SimpleService.class, CAROL);
        Assert.assertNotNull(ss);
        
        Assert.assertTrue(ss instanceof Carol);
    }
    
    /**
     * Tests that the test itself gets injected
     */
    @Test
    public void testTestInjection() {
        Assert.assertNotNull(injectMe);
    }
    
    /**
     * Ensures that a complex test service is properly found
     */
    @Test
    public void testComplexService() {
        SimpleContract0 sc0 = testLocator.getService(SimpleContract0.class);
        
        Assert.assertNotNull(sc0);
        
    }
}
