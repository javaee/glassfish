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

import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.tests.basic.annotations.*;
import org.glassfish.hk2.tests.basic.arbitrary.*;
import org.glassfish.hk2.tests.basic.contracts.*;
import org.glassfish.hk2.tests.basic.scopes.*;
import org.glassfish.hk2.tests.basic.services.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;

import static org.junit.Assert.*;

/**
 * This is a test of basic injection features. To avoid the potential influence
 * of being in the same package (e.g. implicit access to the package-private data),
 * all classes that the test works with are placed in separate nested packages.
 * 
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class BasicInjectionTest {
    public static InstanceBoundContract boundContractInstance = new InstanceBoundContract(){};
    public static InstanceBoundService boundServiceInstance = new InstanceBoundService();
    
    static class FieldInjectedTypeBinidngTestClass {
        @Inject ServiceC sc;
        @Inject ClassX cx;
        @Inject ContractB cb;
        @Inject ContractA ca;
        @Inject Factory<ServiceC> scp;
        @Inject Factory<ClassX> cxp;
        @Inject Factory<ContractB> cbp;
        @Inject Factory<ContractA> cap;
    }
    
    static class ConstructorInjectedTypeBinidngTestClass {
        final ServiceC sc;
        final ClassX cx;
        final ContractB cb;
        final ContractA ca;
        final Factory<ServiceC> scp;
        final Factory<ClassX> cxp;
        final Factory<ContractB> cbp;
        final Factory<ContractA> cap;

        public ConstructorInjectedTypeBinidngTestClass(
                @Inject ServiceC sc, 
                @Inject ClassX cx, 
                @Inject ContractB cb, 
                @Inject ContractA ca, 
                @Inject Factory<ServiceC> scp, 
                @Inject Factory<ClassX> cxp, 
                @Inject Factory<ContractB> cbp, 
                @Inject Factory<ContractA> cap) {
            this.sc = sc;
            this.cx = cx;
            this.cb = cb;
            this.ca = ca;
            this.scp = scp;
            this.cxp = cxp;
            this.cbp = cbp;
            this.cap = cap;
        }        
    }
    
    static class FieldInjectedInstanceBinidngTestClass {
        @Inject InstanceBoundContract i;
        @Inject Factory<InstanceBoundContract> ip;
        @Inject InstanceBoundService s;
        @Inject Factory<InstanceBoundService> sp;
    }
    
    static class ConstructorInjectedInstanceBindingTestClass {
        final InstanceBoundContract i;
        final Factory<InstanceBoundContract> ip;
        final InstanceBoundService s;
        final Factory<InstanceBoundService> sp;

        public ConstructorInjectedInstanceBindingTestClass(
                @Inject InstanceBoundContract i, 
                @Inject Factory<InstanceBoundContract> ip, 
                @Inject InstanceBoundService s, 
                @Inject Factory<InstanceBoundService> sp) {
            this.i = i;
            this.ip = ip;
            this.s = s;
            this.sp = sp;
        }
    }

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

    static class FieldInjectedFactoryBindingTestClass {

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

    static class ConstructorInjectedFactoryBindingTestClass {

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

        ConstructorInjectedFactoryBindingTestClass(
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
            // basic & qualified bindings
            binderFactory.bind(ContractA.class).to(ServiceA.class);
            binderFactory.bind(ContractB.class).to(ServiceB.class);
            binderFactory.bind(ContractB.class).annotatedWith(MarkerA.class).to(ServiceB1.class);
            binderFactory.bind().to(ServiceC.class);
            binderFactory.bind().to(ServiceD.class);
            
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
            binderFactory.bind().to(ConstructorInjectedTypeBinidngTestClass.class);
            binderFactory.bind().to(QualifierInjectedService.class);
            binderFactory.bind().to(ConstructorQualifierInjectedService.class);
            binderFactory.bind().to(FieldInjectedFactoryBindingTestClass.class);
            binderFactory.bind().to(ConstructorInjectedFactoryBindingTestClass.class);
        }
    }
    private static Services services;

    @BeforeClass
    public static void setup() {
        services = HK2.get().create(null, new TestModule());
    }

    @Test
    public void testTypeBindingProvisioningViaServicesApi() {
        final ServiceC sc = services.forContract(ServiceC.class).get();
        assertInjectedInstance(ServiceC.class, sc);
        
        final ClassX cx = services.forContract(ClassX.class).get();
        assertInjectedInstance(ClassX.class, cx);
 
        final ContractB cb = services.forContract(ContractB.class).get();
        assertInjectedInstance(ServiceB.class, cb);
        assertInjectedInstance(ClassX.class, cb.getX());

        final ContractA ca = services.forContract(ContractA.class).get();
        assertInjectedInstance(ServiceA.class, ca);
        assertInjectedInstance(ServiceB.class, ca.getB());
        assertInjectedInstance(ClassX.class, ca.getB().getX());
        
        final Provider<ServiceC> scp = services.forContract(ServiceC.class).getProvider();
        assertInjectedProvider(ServiceC.class, scp);
        
        final Provider<ClassX> cxp = services.forContract(ClassX.class).getProvider();
        assertInjectedProvider(ClassX.class, cxp);
 
        final Provider<ContractB> cbp = services.forContract(ContractB.class).getProvider();
        assertInjectedProvider(ServiceB.class, cbp);
        assertInjectedInstance(ClassX.class, cbp.get().getX());

        final Provider<ContractA> cap = services.forContract(ContractA.class).getProvider();
        assertInjectedProvider(ServiceA.class, cap);
        assertInjectedInstance(ServiceB.class, cap.get().getB());
        assertInjectedInstance(ClassX.class, cap.get().getB().getX());
    }

    @Test
    public void testTypeBindingInjection() {
        final FieldInjectedTypeBinidngTestClass fi = services.forContract(FieldInjectedTypeBinidngTestClass.class).get();
        assertInjectedInstance(ServiceC.class, fi.sc);
        assertInjectedInstance(ClassX.class, fi.cx);
 
        assertInjectedInstance(ServiceB.class, fi.cb);
        assertInjectedInstance(ClassX.class, fi.cb.getX());

        assertInjectedInstance(ServiceA.class, fi.ca);
        assertInjectedInstance(ServiceB.class, fi.ca.getB());
        assertInjectedInstance(ClassX.class, fi.ca.getB().getX());
        
        assertInjectedFactory(ServiceC.class, fi.scp);
        
        assertInjectedFactory(ClassX.class, fi.cxp);
 
        assertInjectedFactory(ServiceB.class, fi.cbp);
        assertInjectedInstance(ClassX.class, fi.cbp.get().getX());

        assertInjectedFactory(ServiceA.class, fi.cap);
        assertInjectedInstance(ServiceB.class, fi.cap.get().getB());
        assertInjectedInstance(ClassX.class, fi.cap.get().getB().getX());
        
        final ConstructorInjectedTypeBinidngTestClass ci = services.forContract(ConstructorInjectedTypeBinidngTestClass.class).get();
        assertInjectedInstance(ServiceC.class, ci.sc);
        assertInjectedInstance(ClassX.class, ci.cx);
 
        assertInjectedInstance(ServiceB.class, ci.cb);
        assertInjectedInstance(ClassX.class, ci.cb.getX());

        assertInjectedInstance(ServiceA.class, ci.ca);
        assertInjectedInstance(ServiceB.class, ci.ca.getB());
        assertInjectedInstance(ClassX.class, ci.ca.getB().getX());
        
        assertInjectedFactory(ServiceC.class, ci.scp);
        
        assertInjectedFactory(ClassX.class, ci.cxp);
 
        assertInjectedFactory(ServiceB.class, ci.cbp);
        assertInjectedInstance(ClassX.class, ci.cbp.get().getX());

        assertInjectedFactory(ServiceA.class, ci.cap);
        assertInjectedInstance(ServiceB.class, ci.cap.get().getB());
        assertInjectedInstance(ClassX.class, ci.cap.get().getB().getX());
    }

    @Test
    public void testInstanceBindingProvisioningViaServicesApi() {
        final InstanceBoundContract i1 = services.forContract(InstanceBoundContract.class).get();
        final InstanceBoundContract i2 = services.forContract(InstanceBoundContract.class).getProvider().get();
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, i1);
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, i2);
        
        final InstanceBoundService s1 = services.forContract(InstanceBoundService.class).get();
        final InstanceBoundService s2 = services.forContract(InstanceBoundService.class).getProvider().get();
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, s1);
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, s2);
    }

    @Test
    public void testInstanceBindingInjection() {
        final FieldInjectedInstanceBinidngTestClass fi = services.forContract(FieldInjectedInstanceBinidngTestClass.class).get();
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, fi.i);
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, fi.ip.get());
        
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, fi.s);
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, fi.sp.get());

        final ConstructorInjectedInstanceBindingTestClass ci = services.forContract(ConstructorInjectedInstanceBindingTestClass.class).get();
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, ci.i);
        assertSame("Provisioned contract instance not the same as the one used in instance binding definition", boundContractInstance, ci.ip.get());
        
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, ci.s);
        assertSame("Provisioned service instance not the same as the one used in instance binding definition", boundServiceInstance, ci.sp.get());
    }

    @Test
    public void testQualifiedInjection() {
        assertQualifierInjectedContent(services.forContract(QualifierInjectedService.class).get());
        assertQualifierInjectedContent(services.forContract(ConstructorQualifierInjectedService.class).get());
        assertQualifierInjectedContent(services.forContract(QualifierInjectedClass.class).get());
        assertQualifierInjectedContent(services.forContract(ConstructorQualifierInjectedClass.class).get());
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
        final FieldInjectedFactoryBindingTestClass fi = services.forContract(FieldInjectedFactoryBindingTestClass.class).get();
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
        
        final ConstructorInjectedFactoryBindingTestClass ci = services.forContract(ConstructorInjectedFactoryBindingTestClass.class).get();
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
    public void testScopes() {
        final CustomScope customScope = services.forContract(CustomScope.class).get();

        CustomScopeInjectedClass customScopeInjectedClass;
        
        customScope.enter();
        customScopeInjectedClass = services.forContract(CustomScopeInjectedClass.class).get();
        assertNotNull("Instance not provisioned", customScopeInjectedClass);
        customScope.leave();
        
        try {
            customScopeInjectedClass = services.forContract(CustomScopeInjectedClass.class).get();
        } catch (IllegalStateException ex) {
            assertEquals(ex.getMessage(), CustomScope.OUT_OF_SCOPE_MESSAGE);
            return;
        }
        
        fail("IllegalStateException expected to be raised when trying to access a custom-scoped biding outside of the scope.");
    }

    private void assertQualifierInjectedContent(QualifierInjected instance) throws ComponentException {
        assertNotNull("Instance not provisioned", instance);
        assertNotNull("Qualified injection point null", instance.getQb());
        // the .getSimpleName() bellow is meant to avoid the long, package-poluted JUnit assertion reports
        assertEquals("Qualified injection point of unexepceted type", ServiceB1.class.getSimpleName(), instance.getQb().getClass().getSimpleName());
        assertNotNull("Qualified injection provider not provisioned", instance.getQbf());
        assertNotNull("Qualified injection provider returned null", instance.getQbf().get());
        assertEquals("Qualified injection provider returned instance of unexpected type", ServiceB1.class.getSimpleName(), instance.getQbf().get().getClass().getSimpleName());
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
