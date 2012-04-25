/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.runlevel.internal;


import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.RunLevelService;
import org.glassfish.hk2.runlevel.RunLevelServiceIndicator;
import org.glassfish.hk2.runlevel.Sorter;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
// import org.jvnet.hk2.annotations.Priority;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Run level service tests.
 *
 * @author tbeerbower
 */
@SuppressWarnings("deprecation")
public class RunLevelServiceTest {

    // ----- Utility methods ------------------------------------------------

    private void configureLocator(ServiceLocator locator,
                                  Class<? extends TestService>[] runLevelServices) {

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        new RLSModule(runLevelServices).configure(config);

        config.commit();
    }

    private void proceedToAndWait(RunLevelServiceImpl rls, ServiceLocator locator, int runLevel) throws TimeoutException{
//        TestListener listener = locator.getService(TestListener.class);
//        synchronized (listener) {
            rls.proceedTo(runLevel);
//            listener.waitForRunLevel(runLevel, 10000);
//        }
    }


    // ----- Tests ----------------------------------------------------------

    @Test
    public void validateRunLevelNegOneInhabitants() throws Exception {

        final ServiceLocatorFactory serviceLocatorFactory = ServiceLocatorFactory.getInstance();
        ServiceLocator locator = serviceLocatorFactory.create("validateRunLevelNegOneInhabitants");

        configureLocator(locator, new Class[]{
                RunLevelServiceNegOne.class,
                RunLevelServiceOne.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        ServiceHandle<RunLevelServiceNegOne> serviceHandleNegOne = locator.getServiceHandle(RunLevelServiceNegOne.class);
        assertNotNull(serviceHandleNegOne);

        ServiceHandle<RunLevelServiceNegOne> serviceHandleOne = locator.getServiceHandle(RunLevelServiceOne.class);
        assertNotNull(serviceHandleOne);

        proceedToAndWait(rls, locator, org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_IMMEDIATE);

        assertTrue(serviceHandleNegOne.toString() + " expected to be active.", serviceHandleNegOne.isActive());
        assertFalse(serviceHandleOne.toString() + " expected to be inactive.", serviceHandleOne.isActive());
    }

    @Test
    public void inhabitantMetaDataIncludesRunLevel() throws Exception {

        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("inhabitantMetaDataIncludesRunLevel");

        configureLocator(locator, new Class[]{
                RunLevelServiceNegOne.class,
                RunLevelServiceOne.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        List<ActiveDescriptor<?>> descriptors =
                locator.getDescriptors(BuilderHelper.createContractFilter(org.glassfish.hk2.runlevel.RunLevel.class.getName()));

        assertNotNull(descriptors);
        int count = 0;
        for (ActiveDescriptor<?> descriptor : descriptors) {
            count++;
            assertNotNull(descriptor.getMetadata());
            String val = descriptor.getMetadata().get(org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_META_TAG).get(0);
            assertNotNull(descriptor.toString(), val);
            assertTrue(descriptor + " runLevel val=" + val, Integer.valueOf(val) >= org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_IMMEDIATE);
        }
        assertTrue(String.valueOf(count), count == 2);
    }

    @Test
    public void proceedToInvalidNegNum() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedToInvalidNegNum");

        configureLocator(locator, new Class[]{
                RunLevelServiceNegOne.class,
                RunLevelServiceOne.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        try {
            proceedToAndWait(rls, locator, -2);
            fail("Expected -1 to be a problem");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void proceedTo0() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedTo0");

        configureLocator(locator, new Class[]{
                RunLevelServiceOne.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 0);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 0, recorders.size());
        assertEquals(0, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());
    }

    @Test
    public void proceedUpTo5() throws Exception{
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo5_basics");

        configureLocator(locator, new Class[]{
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 5);
        assertEquals(5, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(1, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);
    }



    @Test
    public void proceedUpTo10() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo10");

        configureLocator(locator, new Class[]{
                RunLevelServiceTen.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 10);
        assertEquals(10, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 2, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);

        recorder = recorders.get(10);
        assertNotNull(recorder);
    }

    @Test
    public void proceedDownInPostConstruct() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedDownInPostConstruct");

        configureLocator(locator, new Class[]{
                RunLevelServiceProceedDownInPostConstruct.class,
                RunLevelServiceTen.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 20);
        assertEquals(5, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 2, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);

        recorder = recorders.get(10);
        assertNotNull(recorder);
    }

    @Test
    public void proceedUpInPostConstruct() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpInPostConstruct");

        configureLocator(locator, new Class[]{
                RunLevelServiceProceedUpInPostConstruct.class,
                RunLevelServiceTen.class,
                RunLevelServiceTwentyFive.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 20);
        assertEquals(25, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 4, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);

        recorder = recorders.get(10);
        assertNotNull(recorder);

        recorder = recorders.get(15);
        assertNotNull(recorder);

        recorder = recorders.get(25);
        assertNotNull(recorder);
    }

    @Test
    public void proceedUpTo49() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo49");

        configureLocator(locator, new Class[]{
                RunLevelServiceTen.class,
                RunLevelServiceTwenty.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 49);
        assertEquals(49, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 3, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);

        recorder = recorders.get(10);
        assertNotNull(recorder);

        recorder = recorders.get(20);
        assertNotNull(recorder);
    }

    @Test
    public void proceedUpTo49ThenDownTo11() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo49ThenDownTo11");

        configureLocator(locator, new Class[]{
                RunLevelServiceTen.class,
                RunLevelServiceTwenty.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 49);
        assertEquals(49, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        proceedToAndWait(rls, locator, 11);
        assertEquals(11, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(3, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);

        recorder = recorders.get(10);
        assertNotNull(recorder);
    }

    @Test
    public void proceedUpTo49ThenDownTo11ThenDownToZero() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo49ThenDownTo11ThenDownToZero");

        configureLocator(locator, new Class[]{
                RunLevelServiceTen.class,
                RunLevelServiceTwenty.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 49);
        proceedToAndWait(rls, locator, 11);
        proceedToAndWait(rls, locator, 0);

        assertEquals(0, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(3, recorders.size());
    }

    @Test
    public void serviceABC_startUp_and_shutDown() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("serviceABC_startUp_and_shutDown");

        configureLocator(locator, new Class[]{
                RunLevelServiceA.class,
                RunLevelServiceB.class,
                RunLevelServiceC.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 10);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 1, recorders.size());

        Stack<ActiveDescriptor<?>> recorder = recorders.get(10);
        assertNotNull(recorder);

        List<ActiveDescriptor<?>> activations = recorder;
        assertFalse("activations empty", activations.isEmpty());
        try {
            Iterator<ActiveDescriptor<?>> iter = activations.iterator();
            iter.remove();
            fail("expected read-only collection");
        } catch (IllegalStateException e) {
            // expected
        }

        activations = new ArrayList<ActiveDescriptor<?>>(activations);

        assertEquals("activations: " + activations, 3, activations.size());

        ActiveDescriptor<?> iB = locator.getBestDescriptor(BuilderHelper.createContractFilter(ServiceB.class.getName()));
        ActiveDescriptor<?> iA = locator.getBestDescriptor(BuilderHelper.createContractFilter(ServiceA.class.getName()));
        ActiveDescriptor<?> iC = locator.getBestDescriptor(BuilderHelper.createContractFilter(ServiceC.class.getName()));

        Iterator<ActiveDescriptor<?>> iter = activations.iterator();
        assertEquals("order is important", iC, iter.next());
        assertEquals("order is important", iB, iter.next());
        assertEquals("order is important", iA, iter.next());

        proceedToAndWait(rls, locator, 0);

        assertEquals(recorders.toString(), 1, recorders.size());
        assertNotNull(recorders.toString(), recorders.get(10));
        assertTrue(recorders.toString(), recorders.get(10).isEmpty());
    }

    @Test
    public void dependenciesFromNonRunLevelToRunLevelService() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("dependenciesFromNonRunLevelToRunLevelService");
        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = new LinkedHashMap<Integer, Stack<ActiveDescriptor<?>>>();

        configureLocator(locator, new Class[]{
                NonRunLevelWithRunLevelDepService.class,
                RunLevelServiceNonValidating.class});

        ActiveDescriptor<RunLevelDepService> descriptor = (ActiveDescriptor<RunLevelDepService>) locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelDepService.class.getName()));

        assertNotNull(descriptor);

        final ServiceHandle<RunLevelDepService> serviceHandle = locator.getServiceHandle(descriptor);

        RunLevelDepService service;

        service = serviceHandle.getService();

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 10);

        service.useRunLevelService();

        proceedToAndWait(rls, locator, 0);

        service.useRunLevelService();
    }

    @Test
    public void namedProceedUpTo5() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("namedProceedUpTo5");

        configureLocator(locator, new Class[]{
                FooRunLevelServiceImpl.class,
                RunLevelServiceFive.class,
                FooRunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class, "foo");

        proceedToAndWait(rls, locator, 5);
        assertEquals(5, rls.getCurrentRunLevel());
        assertEquals(null, rls.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(1, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);
    }

    @Test
    public void proceedUpAndDownWithListener() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpAndDownWithListener");

        configureLocator(locator, new Class[]{
                Listener.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        Listener listener = locator.getService(Listener.class);

        assertEquals(0, listener.getLastRunLevel());

        proceedToAndWait(rls, locator, 5);

        assertEquals(5, listener.getLastRunLevel());

        proceedToAndWait(rls, locator, 0);

        assertEquals(0, listener.getLastRunLevel());
    }

    @Test
    public void namedProceedUpAndDownWithListener() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("namedProceedUpAndDownWithListener");

        configureLocator(locator, new Class[]{
                FooRunLevelServiceImpl.class,
                Listener.class,
                FooListener.class,
                RunLevelServiceFive.class,
                FooRunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class, "foo");

        Listener listener = locator.getService(Listener.class);
        Listener fooListener = locator.getService(FooListener.class);

        assertEquals(0, listener.getLastRunLevel());
        assertEquals(0, fooListener.getLastRunLevel());

        proceedToAndWait(rls, locator, 5);

        assertEquals(0, listener.getLastRunLevel());
        assertEquals(5, fooListener.getLastRunLevel());

        proceedToAndWait(rls, locator, 0);

        assertEquals(0, listener.getLastRunLevel());
        assertEquals(0, fooListener.getLastRunLevel());
    }

    @Test
    public void proceedUpAndDownWithActivator() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpAndDownWithActivator");

        configureLocator(locator, new Class[]{
                Activator.class,
                RunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        Activator activator = locator.getService(Activator.class);

        assertNull(activator.getLastActivated());
        assertNull(activator.getLastDeactivated());

        proceedToAndWait(rls, locator, 5);

        assertEquals(RunLevelServiceFive.class, activator.getLastActivated().getImplementationClass());
        assertNull(activator.getLastDeactivated());

        proceedToAndWait(rls, locator, 0);

        assertEquals(RunLevelServiceFive.class, activator.getLastDeactivated().getImplementationClass());
    }

    @Test
    public void namedProceedUpAndDownWithActivator() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("namedProceedUpAndDownWithActivator");

        configureLocator(locator, new Class[]{
                FooRunLevelServiceImpl.class,
                Activator.class,
                FooActivator.class,
                RunLevelServiceFive.class,
                FooRunLevelServiceFive.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class, "foo");

        BaseActivator activator = locator.getService(Activator.class);
        BaseActivator fooActivator = locator.getService(FooActivator.class);

        assertNull(activator.getLastActivated());
        assertNull(activator.getLastDeactivated());

        assertNull(fooActivator.getLastActivated());
        assertNull(fooActivator.getLastDeactivated());

        proceedToAndWait(rls, locator, 5);

        assertNull(activator.getLastActivated());
        assertNull(activator.getLastDeactivated());

        assertEquals(FooRunLevelServiceFive.class, fooActivator.getLastActivated().getImplementationClass());
        assertNull(fooActivator.getLastDeactivated());

        proceedToAndWait(rls, locator, 0);

        assertNull(activator.getLastActivated());
        assertNull(activator.getLastDeactivated());

        assertEquals(FooRunLevelServiceFive.class, fooActivator.getLastDeactivated().getImplementationClass());
    }

    @Test @Ignore
    public void proceedUpAndDownWithSorter() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpAndDownWithSorter");

        configureLocator(locator, new Class[]{
                PrioritySorter.class,
                RunLevelServiceLowPriority.class,
                RunLevelServiceHighPriority.class,
                RunLevelServiceMedPriority.class,
                RunLevelServiceTen.class});

        RunLevelServiceImpl rls = locator.getService(RunLevelService.class);

        proceedToAndWait(rls, locator, 10);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rls.getRecorders();

        assertEquals(recorders.toString(), 1, recorders.size());

        Stack<ActiveDescriptor<?>> recorder = recorders.get(10);
        assertNotNull(recorder);

        List<ActiveDescriptor<?>> activations = new ArrayList<ActiveDescriptor<?>>(recorder);

        assertEquals(4, activations.size());

        ActiveDescriptor<?> a0 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelServiceHighPriority.class.getName()));
        ActiveDescriptor<?> a1 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelServiceTen.class.getName()));
        ActiveDescriptor<?> a2 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelServiceMedPriority.class.getName()));
        ActiveDescriptor<?> a3 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelServiceLowPriority.class.getName()));

        Iterator<ActiveDescriptor<?>> iter = activations.iterator();
        assertEquals("order is important", a0, iter.next());
        assertEquals("order is important", a1, iter.next());
        assertEquals("order is important", a2, iter.next());
        assertEquals("order is important", a3, iter.next());

        proceedToAndWait(rls, locator, 0);

        assertEquals(recorders.toString(), 1, recorders.size());
        assertNotNull(recorders.toString(), recorders.get(10));
        assertTrue(recorders.toString(), recorders.get(10).isEmpty());
    }

    // ----- inner classes --------------------------------------------------

    /**
     *
     */
    public static class RLSModule {
        private final Class<?>[] services;

        public RLSModule(Class<?>[] services) {
            this.services = services;
        }

        public void configure(DynamicConfiguration configurator) {

            // Bind the run level service context
            configurator.bind(BuilderHelper.link(RunLevelContext.class).
                    to(Context.class).to(RunLevelContext.class).in(Singleton.class.getName()).build());

            // Bind the run level service
            configurator.bind(BuilderHelper.link(RunLevelServiceImpl.class).
                    to(RunLevelService.class).in(Singleton.class.getName()).build());

            bindService(configurator, TestListener.class, Singleton.class);

            // Bind each service
            for (int i = 0; i < services.length; i++) {
                bindService(configurator, services[i], null);
            }
        }

        private void bindService(DynamicConfiguration configurator, Class<?> service, Class<? extends Annotation> scope) {
            final DescriptorBuilder descriptorBuilder = BuilderHelper.link(service);

            final org.glassfish.hk2.runlevel.RunLevel rla = service.getAnnotation(org.glassfish.hk2.runlevel.RunLevel.class);
            if (rla != null) {
                descriptorBuilder.to(org.glassfish.hk2.runlevel.RunLevel.class).
                        has(org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_META_TAG, Collections.singletonList(((Integer) rla.value()).toString())).
                        has(org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_MODE_META_TAG, Collections.singletonList(rla.mode().toString()));

                descriptorBuilder.in(org.glassfish.hk2.runlevel.RunLevel.class);
            }
            Class clazz = service;
            while (clazz != null) {
                Class<?>[] interfaces = clazz.getInterfaces();
                for (int j = 0; j < interfaces.length; j++) {
                    descriptorBuilder.to(interfaces[j]);
                }
                clazz = clazz.getSuperclass();
            }

            final RunLevelServiceIndicator runLevelServiceIndicator = service.getAnnotation(RunLevelServiceIndicator.class);
            if (runLevelServiceIndicator != null) {
                descriptorBuilder.has(RunLevelServiceIndicator.RUNLEVEL_SERVICE_NAME_META_TAG, runLevelServiceIndicator.value());
            }

            final Named named = service.getAnnotation(Named.class);
            if (named != null) {
                descriptorBuilder.named(named.value());
            }
            
            if (scope != null) {
                descriptorBuilder.in(scope);
            }

            configurator.bind(descriptorBuilder.build());
        }
    }

    public static abstract class TestService
            implements org.glassfish.hk2.api.PostConstruct, org.glassfish.hk2.api.PreDestroy {
        @Override
        public void postConstruct() {
        }

        @Override
        public void preDestroy() {
        }
    }

    @org.glassfish.hk2.runlevel.RunLevel(org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_IMMEDIATE)
    @Service
    public static class RunLevelServiceNegOne extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(1)
    @Service
    public static class RunLevelServiceOne extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(5)
    @Service
    public static class RunLevelServiceFive extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(5)
    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooRunLevelServiceFive extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    @Service
    public static class RunLevelServiceTen extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    // @Priority(1)
    @Service
    public static class RunLevelServiceHighPriority extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    // @Priority(6)
    @Service
    public static class RunLevelServiceMedPriority extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    // @Priority(9)
    @Service
    public static class RunLevelServiceLowPriority extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(15)
    @Service
    public static class RunLevelServiceProceedDownInPostConstruct extends TestService {
        @Inject
        private RunLevelServiceImpl rls;

        @Override
        public void postConstruct() {
            rls.proceedTo(5);
        }
    }

    @org.glassfish.hk2.runlevel.RunLevel(15)
    @Service
    public static class RunLevelServiceProceedUpInPostConstruct extends TestService {
        @Inject
        private RunLevelServiceImpl rls;

        @Override
        public void postConstruct() {
            rls.proceedTo(25);
        }
    }

    @org.glassfish.hk2.runlevel.RunLevel(25)
    @Service
    public static class RunLevelServiceTwentyFive extends TestService {
    }

    @org.glassfish.hk2.runlevel.RunLevel(20)
    @Service
    public static class RunLevelServiceTwenty extends TestService {
    }

    @Contract
    public interface ServiceA {
    }

    @Contract
    public interface ServiceB {
    }

    @Contract
    public interface ServiceC {
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    @Service
    public static class RunLevelServiceA extends TestService implements ServiceA {
        @Inject
        ServiceB serviceB;

        @Override
        public void postConstruct() {
            super.postConstruct();
        }

        @PreDestroy
        public void preDestroy() {
        }
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    @Service
    public static class RunLevelServiceB extends TestService implements ServiceB {
        @Inject
        ServiceC serviceC;
    }

    @org.glassfish.hk2.runlevel.RunLevel(10)
    @Service
    public static class RunLevelServiceC extends TestService implements ServiceC {
    }

    @Contract
    public interface ServiceD {
        public void doSomething();
    }

    @org.glassfish.hk2.runlevel.RunLevel(value = 10, mode = org.glassfish.hk2.runlevel.RunLevel.Mode.NON_VALIDATING)
    @Service
    public static class RunLevelServiceNonValidating extends TestService implements ServiceD {
        @Override
        public void doSomething() {
        }
    }

    @Contract
    public interface RunLevelDepService {
        public void useRunLevelService();
    }

    @Service
    public static class NonRunLevelWithRunLevelDepService extends TestService implements RunLevelDepService {
        @Inject
        ServiceD serviceD;

        public void useRunLevelService() {
            serviceD.doSomething();
        }
    }

    @Service
    @Named("foo")
    public static class FooRunLevelServiceImpl extends RunLevelServiceImpl {
    }

    @Service
    public static class TestListener implements RunLevelListener {
        private int waitForRunLevel;

        public void waitForRunLevel(int runLevel, long timeout) throws TimeoutException {
            waitForRunLevel = runLevel;

            try {
                wait(timeout);
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        @Override
        public void onCancelled(RunLevelService service, int previousProceedTo, boolean isInterrupt) {
        }

        @Override
        public void onError(RunLevelService service, Throwable error, boolean willContinue) {
        }

        @Override
        public void onProgress(RunLevelService service) {
            synchronized (this) {
                if (service.getCurrentRunLevel() == waitForRunLevel) {
                    notifyAll();
                }
            }
        }
    }

    @Service
    public static class Listener implements RunLevelListener {
        int lastRunLevel = 0;

        public int getLastRunLevel() {
            return lastRunLevel;
        }

        protected String getName() {
            return "default";
        }

        @Override
        public void onCancelled(RunLevelService service, int previousProceedTo, boolean isInterrupt) {
        }

        @Override
        public void onError(RunLevelService service, Throwable error, boolean willContinue) {
        }

        @Override
        public void onProgress(RunLevelService service) {
            lastRunLevel = service.getCurrentRunLevel();
        }
    }

    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooListener extends Listener {
        @Override
        protected String getName() {
            return "foo";
        }
    }

    public static abstract class BaseActivator implements org.glassfish.hk2.runlevel.Activator {

        ActiveDescriptor lastActivated = null;
        ActiveDescriptor lastDeactivated = null;

        protected abstract RunLevelServiceImpl getRunLevelService();

        public ActiveDescriptor getLastActivated() {
            return lastActivated;
        }

        public ActiveDescriptor getLastDeactivated() {
            return lastDeactivated;
        }

        @Override
        public void activate(ActiveDescriptor<?> activeDescriptor) {
            getRunLevelService().activate(activeDescriptor);
            lastActivated = activeDescriptor;
        }

        @Override
        public void deactivate(ActiveDescriptor<?> activeDescriptor) {
            getRunLevelService().deactivate(activeDescriptor);
            lastDeactivated = activeDescriptor;
        }

        @Override
        public void awaitCompletion() throws ExecutionException, InterruptedException, TimeoutException {
            getRunLevelService().awaitCompletion();
        }

        @Override
        public void awaitCompletion(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            getRunLevelService().awaitCompletion(timeout, unit);
        }
    }

    @Service
    public static class Activator extends BaseActivator {
        @Inject
        private RunLevelServiceImpl rls;

        @Override
        protected RunLevelServiceImpl getRunLevelService() {
            return rls;
        }
    }

    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooActivator extends BaseActivator {
        @Inject
        private FooRunLevelServiceImpl rls;

        @Override
        protected RunLevelServiceImpl getRunLevelService() {
            return rls;
        }
    }

    public static class PrioritySorter implements Sorter {

        @Inject
        private ServiceLocator locator;

        @Override
        public void sort(List<ActiveDescriptor<?>> descriptors) {
            Collections.sort(descriptors, new Comparator<ActiveDescriptor<?>>() {
                public int compare(ActiveDescriptor<?> d1, ActiveDescriptor<?> d2) {
                    return getPriority(d1) - getPriority(d2);
                }
            });
        }

        private int getPriority(ActiveDescriptor<?> descriptor) {
            return descriptor.getRanking();
            // locator.reifyDescriptor(descriptor);
            // Priority priority = descriptor.getImplementationClass().getAnnotation(Priority.class);
            // return priority != null ? priority.value() : 5;
        }
    }

    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooPrioritySorter extends PrioritySorter {
    }
}




