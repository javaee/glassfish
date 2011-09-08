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
package org.glassfish.hk2.tests.basic.resolving;

import org.glassfish.hk2.tests.basic.resolving.injected.*;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.TypeLiteral;
import org.glassfish.hk2.inject.Injector;
import org.glassfish.hk2.tests.basic.annotations.*;
import org.glassfish.hk2.tests.basic.arbitrary.*;
import org.glassfish.hk2.tests.basic.contracts.*;
import org.glassfish.hk2.tests.basic.scopes.*;
import org.glassfish.hk2.tests.basic.services.*;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.glassfish.hk2.tests.basic.AssertionUtils.*;

/**
 * This is a test of basic injection features. To avoid the potential influence
 * of being in the same package (e.g. implicit access to the package-private data),
 * all classes that the test works with are placed in separate nested packages.
 * 
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class CustomInjectionTest {

    public static InstanceBoundContract boundContractInstance = new InstanceBoundContract() {
    };
    public static InstanceBoundService boundServiceInstance = new InstanceBoundService();

    public static class BasicTestModule implements Module {

        @Override
        public void configure(BinderFactory binderFactory) {
            // basic, generic & qualified bindings
            binderFactory.bind(ContractA.class).to(ServiceA.class);
            binderFactory.bind(ContractB.class).to(ServiceB.class);
            binderFactory.bind(ContractB.class).annotatedWith(MarkerA.class).to(ServiceB1.class);
            binderFactory.bind().to(ServiceC.class);
            binderFactory.bind().to(ServiceD.class);
            binderFactory.bind().to(ClassX.class);
            binderFactory.bind(new TypeLiteral<GenericContract<String>>() {
            }).to(GenericContractStringImpl.class);

            // instance bindings
            binderFactory.bind(InstanceBoundContract.class).toInstance(boundContractInstance);
            binderFactory.bind().toInstance(boundServiceInstance);

            // scoped bindings
            binderFactory.bind(CustomScope.class).toInstance(new CustomScope());
            binderFactory.bind().to(CustomScopeInjectedClass.class).in(CustomScope.class);

            // factory provided bindings
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

            // injected test class bindings
            binderFactory.bind().to(FieldInjectedTypeBinidngTestClass.class);
            binderFactory.bind().to(QualifierInjectedService.class);
            binderFactory.bind().to(FieldInjectedFactoryBindingTestClass.class);
        }
    }

    public static class ConstructorInjectionTestModule implements Module {

        @Override
        public void configure(BinderFactory binderFactory) {
            binderFactory.bind().to(ConstructorInjectedTypeBinidngTestClass.class);
            binderFactory.bind().to(ConstructorQualifierInjectedService.class);
            binderFactory.bind().to(ConstructorInjectedFactoryBindingTestClass.class);
        }
    }
    private static Services services;

    @BeforeClass
    public static void setup() {
        services = HK2.get().create(null, new ContextInjectionResolver.ContextInjectionModule(), new BasicTestModule());
    }

    @Test
    public void testTypeBindingFieldInjection() {
        final FieldInjectedTypeBinidngTestClass fi = services.byType(FieldInjectedTypeBinidngTestClass.class).get();
        fi.assertInjection();
    }

    @Test
    public void testTypeBindingConstructorInjection() {
        Services s = HK2.get().create(services, new ConstructorInjectionTestModule());

        final ConstructorInjectedTypeBinidngTestClass ci = s.byType(ConstructorInjectedTypeBinidngTestClass.class).get();
        ci.assertInjection();

    }

    @Test
    public void testInstanceBindingFieldInjection() {
        final FieldInjectedInstanceBinidngTestClass fi = services.forContract(FieldInjectedInstanceBinidngTestClass.class).get();
        fi.assertInjection();
    }

    @Test
    public void testInstanceBindingConstructorInjection() {
        Services s = HK2.get().create(services, new ConstructorInjectionTestModule());
        final ConstructorInjectedInstanceBindingTestClass ci = s.forContract(ConstructorInjectedInstanceBindingTestClass.class).get();
        ci.assertInjection();
    }

    @Test
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
    public void testFactoryProvidedContractFieldInjection() {
        final FieldInjectedFactoryBindingTestClass fi = services.byType(FieldInjectedFactoryBindingTestClass.class).get();
        fi.assertInjection();
    }

    @Test
    public void testFactoryProvidedContractConstructorInjection() {
        Services s = HK2.get().create(services, new ConstructorInjectionTestModule());
        final ConstructorInjectedFactoryBindingTestClass ci = s.byType(ConstructorInjectedFactoryBindingTestClass.class).get();
        ci.assertInjection();

    }

    @Test
    public void testInjectorFieldInjection() {
        Injector injector = services.forContract(Injector.class).get();

        FieldInjectedTypeBinidngTestClass fi = new FieldInjectedTypeBinidngTestClass();
        injector.inject(fi);
        fi.assertInjection();
    }

    @Test
    public void testInjectorConstructorInjection() {
        Services s = HK2.get().create(services, new ConstructorInjectionTestModule());
        Injector injector = s.forContract(Injector.class).get();

        ConstructorInjectedTypeBinidngTestClass ci = injector.inject(ConstructorInjectedTypeBinidngTestClass.class);
        ci.assertInjection();
    }

    @Test
    public void testConstructorBasedInjectionOnNonStaticInnerClass() {
        Services s = HK2.get().create(services, new ConstructorInjectionTestModule());
        Injector injector = s.forContract(Injector.class).get();
        class TestClass {

            final ContractA ca;
            final Factory<ContractA> cap;

            @Context
            public TestClass(
                    ContractA ca,
                    Factory<ContractA> cap) {
                this.ca = ca;
                this.cap = cap;
            }
        }
        TestClass ci = injector.inject(TestClass.class);

        assertInjectedInstance(ServiceA.class, ci.ca);
        assertInjectedInstance(ServiceB.class, ci.ca.getB());
        assertInjectedInstance(ClassX.class, ci.ca.getB().getX());

        assertInjectedFactory(ServiceA.class, ci.cap);
        assertInjectedInstance(ServiceB.class, ci.cap.get().getB());
        assertInjectedInstance(ClassX.class, ci.cap.get().getB().getX());
    }
}
