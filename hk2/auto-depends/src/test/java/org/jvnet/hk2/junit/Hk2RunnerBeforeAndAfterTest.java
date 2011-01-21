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
package org.jvnet.hk2.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

/**
 * A suite of tests for testing {link @Before} and {link @After} test
 * annotations.
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { Hk2RunnerBeforeAndAfterTest.PositiveTest.class,
        Hk2RunnerBeforeAndAfterTest.ExceptionTest.class })
public class Hk2RunnerBeforeAndAfterTest {
  
    static boolean beforeClassCalled; 
    static boolean afterClassCalled; 

    @BeforeClass
    public static void beforeClass() {
        assertFalse("beforeClassCalled", beforeClassCalled);
        assertFalse("afterClassCalled", afterClassCalled);
    }
    
    @AfterClass
    public static void afterClass() {
        assertTrue("beforeClassCalled", beforeClassCalled);
        assertTrue("afterClassCalled", afterClassCalled);
    }

    
    /**
     * Test for correct behavior when a method annotated with {link @Before}
     * or {link @Before} throws an exception
     * 
     */
    public static class ExceptionTest {

        /**
         * Test that an exception thrown in a @Before method propagates and a
         * test failure event is fired
         */
        @Test
        public void testBeforeMethodThrowsException() {
            RunNotifier notifier = new RunNotifier();

            final List<Failure> failures = new ArrayList<Failure>();

            RunListener rl = new RunListener() {

                @Override
                public void testFailure(Failure failure) throws Exception {
                    failures.add(failure);
                }

            };

            notifier.addListener(rl);

            try {
                new Hk2Runner(TestClassWithBeforeException.class).run(notifier);
                fail("Expected exception due to exception in @Before method");
            } catch (Exception ex) {
                // expected
            }
            
            assertEquals("Should be one test failure event", 1, failures.size());
        }

        /**
         * Test that an exception thrown in a {@link @After} method propagates
         * and a test failure event is fired
         */
        @Test
        public void testAfterMethodThrowsException() throws Throwable {
            RunNotifier notifier = new RunNotifier();

            final List<Failure> failures = new ArrayList<Failure>();

            RunListener rl = new RunListener() {

                @Override
                public void testFailure(Failure failure) throws Exception {
                    failures.add(failure);
                }

            };

            notifier.addListener(rl);

            try {
                new Hk2Runner(TestClassWithAfterException.class).run(notifier);
                fail("Expected exception due to exception in @After method");
            } catch (Exception ex) {
                // expected
            }

            assertEquals("Should be one test failure event", 1, failures.size());
            
        }

        /**
         * A simple test class that throws an exception in a @Before method
         */
        public static class TestClassWithBeforeException {
            @Before
            public void beforeException() throws Exception {
                throw new Exception("exception in @Before method");
            }

            @Test
            public void test() throws Exception {
                fail("Should never reach this test");
            }
        }

        /**
         * A simple test class that throws an exception in an {@link @After}
         * method
         */
        public static class TestClassWithAfterException {
            @After
            public void afterException() throws Exception {
                throw new Exception("exception in @After method");
            }

            @Test
            public void test() throws Exception {
            }
        }
    }

    /**
     * Test class to ensure all {@link @Before} methods are called prior to each
     * test and all {@link @After} methods are called after each test
     */
    @RunWith(Hk2Runner.class)
    public static class PositiveTest implements PostConstruct, PreDestroy {

        static int postConstructCalls;
        static int preDestroyCalls;
      
        String beforeTestString = "";
        String afterTestString = "";

        @BeforeClass
        public static void beforeClass() {
            beforeClassCalled = true;
            assertEquals("postConstructCalls", 0, postConstructCalls);
            assertEquals("preDestroyCalls", 0, preDestroyCalls);
        }
  
        @BeforeClass
        @Ignore
        public static void beforeClassIgnored() {
            fail("test should be ignored");
        }
        
        @AfterClass
        public static void afterClass() {
            afterClassCalled = true;
            assertEquals("postConstructCalls", 1, postConstructCalls);
            // TODO: we may want to reconsider calling preDestroy after a test run
            assertEquals("preDestroyCalls", 0, preDestroyCalls);
        }
        
        @AfterClass
        @Ignore
        public static void afterClassIgnored() {
            fail("test should be ignored");
        }
        
        @Before
        public void before0() {
            assertEquals("postConstructCalls", 1, postConstructCalls);
            assertEquals("preDestroyCalls", 0, preDestroyCalls);
        }
        
        @Before
        public void before1() {
            beforeTestString += " b1 ";
        }

        @Before
        void before2() {
            beforeTestString += " b2 ";
        }
        
        @Before
        @Ignore
        void beforeIgnored() {
            beforeTestString += " Ignore ";
        }

        @After
        void after0() {
            assertEquals("postConstructCalls", 1, postConstructCalls);
            assertEquals("preDestroyCalls", 0, preDestroyCalls);
        }
        
        @After
        void after1() {
            afterTestString += " a1 ";
        }

        @After
        void after2() {
            afterTestString += " a2 ";
        }

        @After
        @Ignore
        void afterIgnored() {
            afterTestString += " Ignore ";
        }

        /**
         * Test that the methods annotated with {@link @Before} have run
         */
        @Test
        public void testBeforeRan() {
            assertTrue("Missing @Before call", beforeTestString.contains("b1"));
            assertTrue("Missing @Before call", beforeTestString.contains("b2"));
            assertFalse("Unexpected call to @Before with @Ignore", beforeTestString.contains("Ignore"));
        }

        /**
         * Test that the methods annotated with {@link @After} have run
         */
        @Test
        public void testAfterRan() {
            assertTrue("Missing @After call", afterTestString.contains("a1"));
            assertTrue("Missing @After call", afterTestString.contains("a2"));
            assertFalse("Unexpected call to @After with @Ignore", afterTestString.contains("Ignore"));
        }
        
        @Override
        public void postConstruct() {
            postConstructCalls++;
        }

        @Override
        public void preDestroy() {
            preDestroyCalls++;
        }
    }
}
