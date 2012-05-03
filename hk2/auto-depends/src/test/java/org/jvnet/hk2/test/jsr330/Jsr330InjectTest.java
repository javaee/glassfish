/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.test.jsr330;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.junit.Hk2Runner;

/**
 * Tests javax.inject.Inject based injection points of hk2 services
 */
@RunWith(Hk2Runner.class)
public class Jsr330InjectTest {

	public Jsr330InjectTest() {
	}

	@Inject
	Habitat h;

	// injection by type
	@javax.inject.Inject
	ITestService1 testService1;

	// inject a provider
	@javax.inject.Inject
	Provider<ITestService1> testService1_Provider;

	@javax.inject.Inject
	@Named("second")
	ITestService2 testService2;

    @javax.inject.Inject
    @Named("second")
    Provider<ITestService2> iTestService2Provider ;

    @javax.inject.Inject
    ITestService3 testService3;

    @javax.inject.Inject
	@Q1
	IQualified qual1;
	
	@javax.inject.Inject
	@Q2
	IQualified qual2;
	
	@javax.inject.Inject
	@Q3
	IQualified qual3;
	
    @javax.inject.Inject
    @Optional
    java.util.logging.Formatter optionalAbstractClassWithNoImpl;
	
	@Test
	public void testHabitat() {
		assertNotNull("Habitat injection failed", h);
	}

	@Test
	public void testTestService() {
		assertNotNull(testService1_Provider.get());
		assertNotNull(testService1);

		assertNotNull(testService1.getITestService2());
		
		assertNotNull(testService2);
		
		assertTrue("Instances should be distinguished by name", testService1.getITestService2() != testService2);

        ITestService2 newTestService2 = iTestService2Provider.get();

        assertNotNull(newTestService2);   // Fails here.

        assertTrue("Instances should be distinguished by name", testService1.getITestService2() != newTestService2);
	}

    @Test
    public void testConstructorInject() {
        assertNotNull(testService3);

        assertEquals(testService1, testService3.getTestService1());
        assertEquals(testService1.getITestService2(), testService3.getTestService2());
        assertEquals(optionalWithServiceAvailable, testService3.getOptionalService1());
        assertEquals(optionalWithNoServiceAvailable, testService3.getOptionalService2());
    }

    @Test
	public void testQualifiers() {
		assertNotNull(qual1);
		assertNotNull(qual2);
		assertNotNull(qual3);
		
		assertTrue(qual1 instanceof Q1Impl);
		assertTrue(qual2 instanceof Q2Impl);
		assertTrue(qual3 instanceof Q3Impl);
	}
	
	@Inject
	@Optional
	OptionalContract1 optionalWithServiceAvailable;
	
	@Inject
	@Optional
	OptionalContract2 optionalWithNoServiceAvailable;
	
	@Inject
	@Optional
	Provider<OptionalContract1> optionalProviderService;
	
	@Inject
	@Optional
	Provider<OptionalContract2> optionalProviderWithNoServiceAvailable;
	
	@Inject
	IQualified[] arrayOfServices;
	
    @Inject
    Provider<IQualified>[] arrayOfServiceProviders;
	
	// another way to do optional injection in jsr-330
	// is by injecting a Provider
	@Inject
    Provider<OptionalContract2> injectedProviderWithNoServiceAvailable;
    
	static int callsToSetterMethods=0;
	
	@Inject
	void setOptionalProvider(Provider<OptionalContract1> ops) {
	    callsToSetterMethods++;
	    assertNotNull(ops.get());
	}

	@Inject
	void setOptionalProviderWithNoServiceAvailable(Provider<OptionalContract2> ops) {
	    callsToSetterMethods++;
	    assertNull(ops.get());
	}
	   
	@Test 
	public void testOptional() {
		assertNotNull(optionalWithServiceAvailable);
		assertNull(optionalWithNoServiceAvailable);
		
		assertNotNull(optionalProviderService.get());
		
		assertNull(optionalProviderWithNoServiceAvailable.get());

        assertNull(injectedProviderWithNoServiceAvailable.get());
        
        assertEquals("Expecting all injects on methods to be called", 2, callsToSetterMethods);
        
        assertNull("No Impl should have been found", optionalAbstractClassWithNoImpl);
	}
	
    @Test
    public void testArrays() {
        assertNotNull(arrayOfServices);
        assertNotNull(arrayOfServiceProviders);

        assertEquals(3, arrayOfServices.length);
        assertEquals(3, arrayOfServiceProviders.length);

        for (IQualified i : arrayOfServices) {
            assertNotNull(i);
        }

        for (Provider<IQualified> i : arrayOfServiceProviders) {
            assertNotNull(i.get());
        }
    }
	
	@Contract
	public interface OptionalContract1 {
		
	}
	
	@Contract
	public interface OptionalContract2 {
		
	}
	
	@Service
	public static class OptionalService implements OptionalContract1 {}
	
	@Service
	public static class TestService1 implements ITestService1 {
		@Inject
		@Named("first")
		TestService2 testService2;

		public TestService1() {
		}

		@Override
		public ITestService2 getITestService2() {
			return testService2;
		}
	}

    @Service(name="first")
	public static class TestService2 implements ITestService2 {

		public TestService2() {
		}

	}
	
	@Service(name="second")
	public static class TestService2a implements ITestService2 {
		
	}

    @Service
    public static class TestService3 implements ITestService3 {

        TestService1 testService1;
        TestService2 testService2;
        OptionalContract1 optionalService1;
        OptionalContract2 optionalService2;

        @Inject
        public TestService3(TestService1 testService1,
                            @Named("first") TestService2 testService2,
                            @Optional OptionalContract1 optionalService1,
                            @Optional OptionalContract2 optionalService2) {

            this.testService1 = testService1;
            this.testService2 = testService2;
            this.optionalService1 = optionalService1;
            this.optionalService2 = optionalService2;
        }

        @Override
        public TestService1 getTestService1() {
            return testService1;
        }

        @Override
        public TestService2 getTestService2() {
            return testService2;
        }

        @Override
        public OptionalContract1 getOptionalService1() {
            return optionalService1;
        }

        @Override
        public OptionalContract2 getOptionalService2() {
            return optionalService2;
        }

    }


    @Contract
	public interface ITestService1 {
		ITestService2 getITestService2();
	}

	@Contract
	public interface ITestService2 {
	}

    @Contract
    public interface ITestService3 {
        public TestService1 getTestService1();

        public TestService2 getTestService2();

        public OptionalContract1 getOptionalService1();

        public OptionalContract2 getOptionalService2();
    }

    @Contract
	public interface IQualified {
		
	}
	
	@Service
	@Q1
	public static class Q1Impl implements IQualified {
		public Q1Impl() {}
	}

	@Service
    @Q2
	public static class Q2Impl implements IQualified {
		public Q2Impl() {}
    }

	@Service
    @Q3
	public static class Q3Impl implements IQualified {
		public Q3Impl() {}
    }
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Q1{
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Q2{
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Q3{
	}
	
	
}
