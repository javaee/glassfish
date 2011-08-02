/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.basic;

import org.glassfish.hk2.tests.basic.arbitrary.QualifierInjected;
import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.tests.basic.arbitrary.ConstructorQualifierInjectedClass;
import org.glassfish.hk2.tests.basic.arbitrary.QualifierInjectedClass;
import org.glassfish.hk2.tests.basic.services.ServiceB1;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.tests.basic.services.ServiceD;
import org.glassfish.hk2.tests.basic.services.ServiceC;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.tests.basic.annotations.Marker;
import org.glassfish.hk2.tests.basic.arbitrary.ClassX;
import org.glassfish.hk2.tests.basic.contracts.ContractA;
import org.glassfish.hk2.tests.basic.contracts.ContractB;
import org.glassfish.hk2.tests.basic.services.ConstructorQualifierInjectedService;
import org.glassfish.hk2.tests.basic.services.QualifierInjectedService;
import org.glassfish.hk2.tests.basic.services.ServiceA;
import org.glassfish.hk2.tests.basic.services.ServiceB;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.*;

/**
 * This is a test of basic injection features. To avoid the potential influence
 * of being in the same package (e.g. implicit access to the package-private data),
 * all classes that the test works with are placed in separate nested packages.
 * 
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class BasicInjectionTest {

    public static class TestModule implements Module {

        @Override
        public void configure(BinderFactory binderFactory) {
            binderFactory.bind(ContractA.class).to(ServiceA.class);
            binderFactory.bind(ContractB.class).to(ServiceB.class);
            binderFactory.bind(ContractB.class).annotatedWith(Marker.class).to(ServiceB1.class);
            binderFactory.bind().to(ServiceC.class);
            binderFactory.bind().to(ServiceD.class);
            binderFactory.bind().to(QualifierInjectedService.class);
            binderFactory.bind().to(ConstructorQualifierInjectedService.class);
        }
    }

    private static Services services;
    
    @BeforeClass
    public static void setup() {
        services = HK2.get().create(null, new TestModule());
    }
    
    @Test
    public void testServiceToServiceInjection() {
        /**
         * Note that while the code bellow fails right now, the
         * 
         *     services.byType(ServiceC.class).get() 
         * 
         * seems to work, but it is not in line with what I learned from
         * our recent email exchanges. Also, it is very inconvenient to
         * need to distinguish between when it is OK to use forContract(...)
         * or whether on needs to use byType(...).
         */
        //final ServiceC sc = services.forContract(ServiceC.class).get();
        final ServiceC sc = services.byType(ServiceC.class).get();

        assertNotNull("No-arg constructor service was not provided by HK2.", sc);
        assertNotNull("Service was provided but not injected properly.", sc.getSd());
    }
    
    @Test
    public void testArbitraryClassInstantiation() {
        final ClassX cx = services.byType(ClassX.class).get();

        assertNotNull("Arbitrary class was not provided by HK2.", cx);
        assertNotNull("Arbitrary class was provided but not injected properly.", cx.getSc());
    }
    
    @Test
    public void testArbitraryClassInstanceToServiceInjection() {
        final ContractB cb = services.forContract(ContractB.class).get();
        assertTrue("No-arg constructor service was not provided by HK2 or not of expected type.", cb instanceof ServiceB);
        assertNotNull("Arbitrary class not injected into a service.", cb.getX());
    }
    
    @Test
    public void testConstructorAndImplicitProviderInjection() {
        final ContractA ca = services.forContract(ContractA.class).get();
        assertTrue("Constructor-injected service not instantiated by HK2 or not of expected type.", ca instanceof ServiceA);

        final ContractB cb = ca.getB();
        assertTrue("No-arg constructor service injected as a provider was not provided by HK2 or not of expected type.", cb instanceof ServiceB);
        assertNotNull("Arbitrary class not injected into a service.", cb.getX());
    }
    
    @Test
    @Ignore
    public void testGetProvider() {
        // for now workaround by exposing ContractLocatorImpl.getFactory() by adding the method
        // to the Providers API interface
        final Provider<ContractA> cap = services.forContract(ContractA.class).getProvider();
        
        assertNotNull("Contract provider is null.", cap);
        assertNotNull("Contract provider returns null contract.", cap.get());
        assertTrue("Type of the contract returned by the provider is not expected.", cap.get() instanceof ServiceA);

        final ContractB cb = cap.get().getB();
        assertTrue("No-arg constructor service injected as a provider was not provided by HK2 or not of expected type.", cb instanceof ServiceB);
        assertNotNull("Arbitrary class not injected into a service.", cb.getX());    
    }

    private void testQualifierInjectedContent(QualifierInjected instance) throws ComponentException {
        assertNotNull("Instance not provisioned", instance);
        assertNotNull("Qualified injection point null", instance.getQb());
        // the .getSimpleName() bellow is meant to avoid the long, package-poluted JUnit assertion reports
        assertEquals("Qualified injection point of unexepceted type", ServiceB1.class.getSimpleName(), instance.getQb().getClass().getSimpleName());
        assertNotNull("Qualified injection provider not provisioned", instance.getQbf());
        assertNotNull("Qualified injection provider returned null", instance.getQbf().get());
        assertEquals("Qualified injection provider returned instance of unexpected type", ServiceB1.class.getSimpleName(), instance.getQbf().get().getClass().getSimpleName());
    }
    
    @Test
    @Ignore
    public void testQualifiedInjectedService() {
        QualifierInjectedService instance = services.byType(QualifierInjectedService.class).get();
        testQualifierInjectedContent(instance);
    }
    
    @Test
    @Ignore
    public void testConstuctorQualifiedInjectedService() {
        ConstructorQualifierInjectedService instance = services.byType(ConstructorQualifierInjectedService.class).get();
        testQualifierInjectedContent(instance);
    }
    
    @Test
    @Ignore
    public void testQualifiedInjectedClass() {
        QualifierInjectedClass instance = services.byType(QualifierInjectedClass.class).get();
        testQualifierInjectedContent(instance);
    }
    
    @Test
    @Ignore
    public void testConstructorQualifiedInjectedClass() {
        ConstructorQualifierInjectedClass instance = services.byType(ConstructorQualifierInjectedClass.class).get();
        testQualifierInjectedContent(instance);
    }
}
