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

import org.junit.Ignore;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.hk2.tests.basic.annotations.MarkerB;
import org.glassfish.hk2.tests.basic.contracts.FactoryProvidedContractB;
import org.glassfish.hk2.tests.basic.contracts.FactoryProvidedContractC;
import org.glassfish.hk2.tests.basic.arbitrary.QualifierInjected;
import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.tests.basic.arbitrary.ConstructorQualifierInjectedClass;
import org.glassfish.hk2.tests.basic.arbitrary.QualifierInjectedClass;
import org.glassfish.hk2.tests.basic.services.ServiceB1;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.tests.basic.services.ServiceD;
import org.glassfish.hk2.tests.basic.services.ServiceC;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.tests.basic.annotations.MarkerA;
import org.glassfish.hk2.tests.basic.arbitrary.ClassX;
import org.glassfish.hk2.tests.basic.contracts.ContractA;
import org.glassfish.hk2.tests.basic.contracts.ContractB;
import org.glassfish.hk2.tests.basic.services.ConstructorQualifierInjectedService;
import org.glassfish.hk2.tests.basic.contracts.FactoryProvidedContractA;
import org.glassfish.hk2.tests.basic.services.QualifierInjectedService;
import org.glassfish.hk2.tests.basic.services.ServiceA;
import org.glassfish.hk2.tests.basic.services.ServiceB;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is a test of basic injection features. To avoid the potential influence
 * of being in the same package (e.g. implicit access to the package-private data),
 * all classes that the test works with are placed in separate nested packages.
 * 
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class BasicInjectionTest {

    static class FactoryProvidedContractAImpl implements FactoryProvidedContractA {
    }

    static class FactoryProvidedContractBImpl implements FactoryProvidedContractB {
    }

    static class FactoryProvidedContractBFactory implements Factory<FactoryProvidedContractB> {

        @Override
        public FactoryProvidedContractB get() throws ComponentException {
            return new FactoryProvidedContractBImpl();
        }
    }

    static class FactoryProvidedContractCAImpl implements FactoryProvidedContractC {
    }

    static class FactoryProvidedContractCFactory implements Factory<FactoryProvidedContractC> {

        @Override
        public FactoryProvidedContractC get() throws ComponentException {
            return new FactoryProvidedContractCAImpl();
        }
    }

    static class FactoryProvidedContractCBImpl implements FactoryProvidedContractC {
    }

    static class FieldInjectedFpcTestInstance {

        @Inject FactoryProvidedContractA a;
        @Inject FactoryProvidedContractB b;
        @Inject @MarkerA FactoryProvidedContractC c_a;
        @Inject @MarkerB FactoryProvidedContractC c_b;
        @Inject FactoryProvidedContractC c_default;
        @Inject Factory<FactoryProvidedContractA> pa;
        @Inject Factory<FactoryProvidedContractB> pb;
        @Inject @MarkerA Factory<FactoryProvidedContractC> pc_a;
        @Inject @MarkerB Factory<FactoryProvidedContractC> pc_b;
        @Inject Factory<FactoryProvidedContractC> pc_default;
    }

    static class ConstructorInjectedFpcTestInstance {

        final FactoryProvidedContractA a;
        final FactoryProvidedContractB b;
        final FactoryProvidedContractC c_a;
        final FactoryProvidedContractC c_b;
        final FactoryProvidedContractC c_default;
        final Factory<FactoryProvidedContractA> pa;
        final Factory<FactoryProvidedContractB> pb;
        final Factory<FactoryProvidedContractC> pc_a;
        final Factory<FactoryProvidedContractC> pc_b;
        final Factory<FactoryProvidedContractC> pc_default;

        ConstructorInjectedFpcTestInstance(
                @Inject FactoryProvidedContractA a,
                @Inject FactoryProvidedContractB b,
                @Inject @MarkerA FactoryProvidedContractC c_a,
                @Inject @MarkerB FactoryProvidedContractC c_b,
                @Inject FactoryProvidedContractC c_default,
                @Inject Factory<FactoryProvidedContractA> pa,
                @Inject Factory<FactoryProvidedContractB> pb,
                @Inject @MarkerA Factory<FactoryProvidedContractC> pc_a,
                @Inject @MarkerB Factory<FactoryProvidedContractC> pc_b,
                @Inject Factory<FactoryProvidedContractC> pc_default) {

            this.a = a;
            this.b = b;
            this.c_a = c_a;
            this.c_b = c_b;
            this.c_default = c_default;
            this.pa = pa;
            this.pb = pb;
            this.pc_a = pc_a;
            this.pc_b = pc_b;
            this.pc_default = pc_default;
        }
    }

    public static class TestModule implements Module {

        @Override
        public void configure(BinderFactory binderFactory) {
            binderFactory.bind(ContractA.class).to(ServiceA.class);
            binderFactory.bind(ContractB.class).to(ServiceB.class);
            binderFactory.bind(ContractB.class).annotatedWith(MarkerA.class).to(ServiceB1.class);
            binderFactory.bind().to(ServiceC.class);
            binderFactory.bind().to(ServiceD.class);
            binderFactory.bind().to(QualifierInjectedService.class);
            binderFactory.bind().to(ConstructorQualifierInjectedService.class);
            binderFactory.bind(FactoryProvidedContractA.class).toFactory(new Factory<FactoryProvidedContractA>() {

                @Override
                public FactoryProvidedContractA get() throws ComponentException {
                    return new FactoryProvidedContractAImpl();
                }
            });
            binderFactory.bind(FactoryProvidedContractB.class).toFactory(FactoryProvidedContractBFactory.class);
            binderFactory.bind(FactoryProvidedContractC.class).annotatedWith(MarkerA.class).toFactory(FactoryProvidedContractCFactory.class);
            binderFactory.bind(FactoryProvidedContractC.class).annotatedWith(MarkerB.class).toFactory(new Factory<FactoryProvidedContractC>() {

                @Override
                public FactoryProvidedContractC get() throws ComponentException {
                    return new FactoryProvidedContractCBImpl();
                }
            });
            binderFactory.bind().to(FieldInjectedFpcTestInstance.class);
            binderFactory.bind().to(ConstructorInjectedFpcTestInstance.class);
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
        assertInjectedInstance(ServiceB.class, cb);
        assertNotNull("Arbitrary class not injected into a service.", cb.getX());
    }

    @Test
    public void testConstructorAndImplicitProviderInjection() {
        final ContractA ca = services.forContract(ContractA.class).get();
        assertInjectedInstance(ServiceA.class, ca);

        final ContractB cb = ca.getB();
        assertInjectedInstance(ServiceB.class, cb);
        assertNotNull("Arbitrary class not injected into a service.", cb.getX());
    }

    @Test
    @Ignore
    public void testFactoryProvidedContractProvisioningViaServicesApi() {
        // binding defined using (annonymous) factory instance 
        final FactoryProvidedContractA a = services.forContract(FactoryProvidedContractA.class).get();
        assertInjectedInstance(FactoryProvidedContractAImpl.class, a);

        final Provider<FactoryProvidedContractA> pa = services.forContract(FactoryProvidedContractA.class).getProvider();
        assertInjectedProvider(FactoryProvidedContractAImpl.class, pa);

        // binding defined using factory class
        final FactoryProvidedContractB b = services.forContract(FactoryProvidedContractB.class).get();
        assertInjectedInstance(FactoryProvidedContractBImpl.class, b);

        final Provider<FactoryProvidedContractB> pb = services.forContract(FactoryProvidedContractB.class).getProvider();
        assertInjectedProvider(FactoryProvidedContractBImpl.class, pb);

        // binding defined using factory class and qualifier annotation
        final FactoryProvidedContractC c_a = services.forContract(FactoryProvidedContractC.class).annotatedWith(MarkerA.class).get();
        assertInjectedInstance(FactoryProvidedContractCAImpl.class, c_a);

        final Provider<FactoryProvidedContractC> pc_a = services.forContract(FactoryProvidedContractC.class).annotatedWith(MarkerA.class).getProvider();
        assertInjectedProvider(FactoryProvidedContractCAImpl.class, pc_a);

        // binding defined using factory instance and qualifier annotation
        final FactoryProvidedContractC c_b = services.forContract(FactoryProvidedContractC.class).annotatedWith(MarkerB.class).get();
        assertInjectedInstance(FactoryProvidedContractCBImpl.class, c_b);

        final Provider<FactoryProvidedContractC> pc_b = services.forContract(FactoryProvidedContractC.class).annotatedWith(MarkerB.class).getProvider();
        assertInjectedProvider(FactoryProvidedContractCBImpl.class, pc_b);

        // verifying null is returned for non-annotated binding that was not defined
        final FactoryProvidedContractC c_default = services.forContract(FactoryProvidedContractC.class).get();
        assertNull("No binding defined for the non-annotated contract. Provisioned instance should be null.", c_default);

        final Provider<FactoryProvidedContractC> pc_default = services.forContract(FactoryProvidedContractC.class).getProvider();
        assertTrue("No binding defined for the non-annotated contract. Provider or returned instance should be null.", pc_default == null || pc_default.get() == null);
    }

    @Test
    @Ignore
    public void testFactoryProvidedContractInjection() {
        final FieldInjectedFpcTestInstance fi = services.forContract(FieldInjectedFpcTestInstance.class).get();
        // binding defined using (annonymous) factory instance         
        assertInjectedInstance(FactoryProvidedContractAImpl.class, fi.a);
        assertInjectedFactory(FactoryProvidedContractAImpl.class, fi.pa);
        // binding defined using factory class
        assertInjectedInstance(FactoryProvidedContractBImpl.class, fi.b);
        assertInjectedFactory(FactoryProvidedContractBImpl.class, fi.pb);
        // binding defined using factory class and qualifier annotation
        assertInjectedInstance(FactoryProvidedContractCAImpl.class, fi.c_a);
        assertInjectedFactory(FactoryProvidedContractCAImpl.class, fi.pc_a);
        // binding defined using factory instance and qualifier annotation
        assertInjectedInstance(FactoryProvidedContractCBImpl.class, fi.c_b);
        assertInjectedFactory(FactoryProvidedContractCBImpl.class, fi.pc_b);
        // verifying null is returned for non-annotated binding that was not defined
        assertNull("No binding defined for the non-annotated contract. Provisioned instance should be null.", fi.c_default);
        assertTrue("No binding defined for the non-annotated contract. Provider or returned instance should be null.", fi.pc_default == null || fi.pc_default.get() == null);
        
        final ConstructorInjectedFpcTestInstance ci = services.forContract(ConstructorInjectedFpcTestInstance.class).get();
        // binding defined using (annonymous) factory instance         
        assertInjectedInstance(FactoryProvidedContractAImpl.class, ci.a);
        assertInjectedFactory(FactoryProvidedContractAImpl.class, ci.pa);
        // binding defined using factory class
        assertInjectedInstance(FactoryProvidedContractBImpl.class, ci.b);
        assertInjectedFactory(FactoryProvidedContractBImpl.class, ci.pb);
        // binding defined using factory class and qualifier annotation
        assertInjectedInstance(FactoryProvidedContractCAImpl.class, ci.c_a);
        assertInjectedFactory(FactoryProvidedContractCAImpl.class, ci.pc_a);
        // binding defined using factory instance and qualifier annotation
        assertInjectedInstance(FactoryProvidedContractCBImpl.class, ci.c_b);
        assertInjectedFactory(FactoryProvidedContractCBImpl.class, ci.pc_b);
        // verifying null is returned for non-annotated binding that was not defined
        assertNull("No binding defined for the non-annotated contract. Provisioned instance should be null.", ci.c_default);
        assertTrue("No binding defined for the non-annotated contract. Provider or returned instance should be null.", ci.pc_default == null || ci.pc_default.get() == null);
    }

    @Test
    public void testGetProvider() {
        // for now workaround by exposing ContractLocatorImpl.getFactory() by adding the method
        // to the Providers API interface
        final Provider<ContractA> cap = services.forContract(ContractA.class).getProvider();
        assertInjectedProvider(ServiceA.class, cap);

        final ContractB cb = cap.get().getB();
        assertInjectedInstance(ServiceB.class, cb);
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
    public void testQualifiedInjectedService() {
        QualifierInjectedService instance = services.byType(QualifierInjectedService.class).get();
        testQualifierInjectedContent(instance);
    }

    @Test
    public void testConstuctorQualifiedInjectedService() {
        ConstructorQualifierInjectedService instance = services.byType(ConstructorQualifierInjectedService.class).get();
        testQualifierInjectedContent(instance);
    }

    @Test
    public void testQualifiedInjectedClass() {
        QualifierInjectedClass instance = services.byType(QualifierInjectedClass.class).get();
        testQualifierInjectedContent(instance);
    }

    @Test
    public void testConstructorQualifiedInjectedClass() {
        ConstructorQualifierInjectedClass instance = services.byType(ConstructorQualifierInjectedClass.class).get();
        testQualifierInjectedContent(instance);
    }

    private <T> void assertInjectedInstance(Class<? extends T> expectedType, T instance) {
        assertNotNull("Provisioned instance is null.", instance);
        assertEquals("Provisioned instance unexpected type.", expectedType.getSimpleName(), instance.getClass().getSimpleName());
    }

    private <T> void assertInjectedProvider(Class<? extends T> expectedType, Provider<T> provider) {
        assertNotNull("Provisioned instance provider is null.", provider);
        assertNotNull("Provider returned null instance.", provider.get());
        assertEquals("Provider returned instance of unexpected type.", expectedType.getSimpleName(), provider.get().getClass().getSimpleName());
    }

    private <T> void assertInjectedFactory(Class<? extends T> expectedType, Factory<T> provider) {
        assertNotNull("Injected instance factory is null.", provider);
        assertNotNull("Factory returned null instance.", provider.get());
        assertEquals("Factory returned instance of unexpected type.", expectedType.getSimpleName(), provider.get().getClass().getSimpleName());
    }
}
