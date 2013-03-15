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


import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.Sorter;
import org.glassfish.hk2.runlevel.utilities.RunLevelControllerImpl;
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
                                  Class<? extends TestService>[] RunLevelControllers) {

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        new RLSModule(RunLevelControllers).configure(config);

        config.commit();
    }

    private void proceedToAndWait(RunLevelController rlc, ServiceLocator locator, int runLevel) throws TimeoutException{
            rlc.proceedTo(runLevel);
    }


    // ----- Tests ----------------------------------------------------------

    @Test
    public void validateRunLevelNegOneInhabitants() throws Exception {

        final ServiceLocatorFactory serviceLocatorFactory = ServiceLocatorFactory.getInstance();
        ServiceLocator locator = serviceLocatorFactory.create("validateRunLevelNegOneInhabitants");

        configureLocator(locator, new Class[]{
                RunLevelControllerNegOne.class,
                RunLevelControllerOne.class});

        RunLevelController rlc = locator.getService(RunLevelController.class);

        ServiceHandle<RunLevelControllerNegOne> serviceHandleNegOne = locator.getServiceHandle(RunLevelControllerNegOne.class);
        assertNotNull(serviceHandleNegOne);

        ServiceHandle<RunLevelControllerOne> serviceHandleOne = locator.getServiceHandle(RunLevelControllerOne.class);
        assertNotNull(serviceHandleOne);

        proceedToAndWait(rlc, locator, RunLevel.RUNLEVEL_VAL_IMMEDIATE);

        assertTrue(serviceHandleNegOne.toString() + " expected to be active.", serviceHandleNegOne.isActive());
        assertFalse(serviceHandleOne.toString() + " expected to be inactive.", serviceHandleOne.isActive());
    }

    @Test
    public void inhabitantMetaDataIncludesRunLevel() throws Exception {

        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("inhabitantMetaDataIncludesRunLevel");

        configureLocator(locator, new Class[]{
                RunLevelControllerNegOne.class,
                RunLevelControllerOne.class});

        RunLevelController rlc = locator.getService(RunLevelController.class);

        final Filter filter = new Filter() {
            @Override
            public boolean matches(Descriptor d) {
                return RunLevel.class.getName().equals(d.getScope());
            }
        };

        List<ActiveDescriptor<?>> descriptors = locator.getDescriptors(filter);

        assertNotNull(descriptors);
        int count = 0;
        for (ActiveDescriptor<?> descriptor : descriptors) {
            count++;
            assertNotNull(descriptor.getMetadata());
            String val = descriptor.getMetadata().get(RunLevel.RUNLEVEL_VAL_META_TAG).get(0);
            assertNotNull(descriptor.toString(), val);
            assertTrue(descriptor + " runLevel val=" + val, Integer.valueOf(val) >= RunLevel.RUNLEVEL_VAL_IMMEDIATE);
        }
        assertTrue(String.valueOf(count), count == 2);
    }

    @Test
    public void proceedToInvalidNegNum() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedToInvalidNegNum");

        configureLocator(locator, new Class[]{
                RunLevelControllerNegOne.class,
                RunLevelControllerOne.class});

        RunLevelController rlc = locator.getService(RunLevelController.class);

        try {
            proceedToAndWait(rlc, locator, -2);
            fail("Expected -1 to be a problem");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void proceedTo0() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedTo0");

        configureLocator(locator, new Class[]{
                RunLevelControllerOne.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 0);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

        assertEquals(recorders.toString(), 0, recorders.size());
        assertEquals(0, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());
    }

    @Test
    public void proceedUpTo5() throws Exception{
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo5_basics");

        configureLocator(locator, new Class[]{
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 5);
        assertEquals(5, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

        assertEquals(1, recorders.size());
        Stack<ActiveDescriptor<?>> recorder = recorders.get(5);
        assertNotNull(recorder);
    }



    @Test
    public void proceedUpTo10() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpTo10");

        configureLocator(locator, new Class[]{
                RunLevelControllerTen.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 10);
        assertEquals(10, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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
                RunLevelControllerProceedDownInPostConstruct.class,
                RunLevelControllerTen.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 20);
        assertEquals(5, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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
                RunLevelControllerProceedUpInPostConstruct.class,
                RunLevelControllerTen.class,
                RunLevelControllerTwentyFive.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 20);
        assertEquals(25, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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
                RunLevelControllerTen.class,
                RunLevelControllerTwenty.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 49);
        assertEquals(49, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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
                RunLevelControllerTen.class,
                RunLevelControllerTwenty.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 49);
        assertEquals(49, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        proceedToAndWait(rlc, locator, 11);
        assertEquals(11, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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
                RunLevelControllerTen.class,
                RunLevelControllerTwenty.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 49);
        proceedToAndWait(rlc, locator, 11);
        proceedToAndWait(rlc, locator, 0);

        assertEquals(0, rlc.getCurrentRunLevel());
        assertEquals(null, rlc.getPlannedRunLevel());

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

        assertEquals(3, recorders.size());
    }

    @Test
    public void serviceABC_startUp_and_shutDown() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("serviceABC_startUp_and_shutDown");

        configureLocator(locator, new Class[]{
                RunLevelControllerA.class,
                RunLevelControllerB.class,
                RunLevelControllerC.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 10);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

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

        proceedToAndWait(rlc, locator, 0);

        assertEquals(recorders.toString(), 1, recorders.size());
        assertNotNull(recorders.toString(), recorders.get(10));
        assertTrue(recorders.toString(), recorders.get(10).isEmpty());
    }

    @Test
    public void dependenciesFromNonRunLevelToRunLevelController() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("dependenciesFromNonRunLevelToRunLevelController");

        configureLocator(locator, new Class[]{
                NonRunLevelWithRunLevelDepService.class,
                RunLevelControllerNonValidating.class});

        ActiveDescriptor<RunLevelDepService> descriptor = (ActiveDescriptor<RunLevelDepService>) locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelDepService.class.getName()));

        assertNotNull(descriptor);

        final ServiceHandle<RunLevelDepService> serviceHandle = locator.getServiceHandle(descriptor);

        RunLevelDepService service = serviceHandle.getService();

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 10);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

        assertEquals(recorders.toString(), 1, recorders.size());

        Stack<ActiveDescriptor<?>> recorder = recorders.get(10);
        assertNotNull(recorder);

        service.useRunLevelController();

        proceedToAndWait(rlc, locator, 0);

        assertEquals(recorders.toString(), 1, recorders.size());
        assertNotNull(recorders.toString(), recorders.get(10));
        assertTrue(recorders.toString(), recorders.get(10).isEmpty());
    }

    @Test
    public void proceedUpAndDownWithListener() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpAndDownWithListener");

        configureLocator(locator, new Class[]{
                Listener.class,
                RunLevelControllerFive.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        Listener listener = locator.getService(Listener.class);

        assertEquals(0, listener.getLastRunLevel());

        proceedToAndWait(rlc, locator, 5);

        assertEquals(5, listener.getLastRunLevel());

        proceedToAndWait(rlc, locator, 0);

        assertEquals(0, listener.getLastRunLevel());
    }

    @Test @Ignore
    public void proceedUpAndDownWithSorter() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("proceedUpAndDownWithSorter");

        configureLocator(locator, new Class[]{
                PrioritySorter.class,
                RunLevelControllerLowPriority.class,
                RunLevelControllerHighPriority.class,
                RunLevelControllerMedPriority.class,
                RunLevelControllerTen.class});

        RunLevelControllerImpl rlc = (RunLevelControllerImpl) locator.getService(RunLevelController.class);

        proceedToAndWait(rlc, locator, 10);

        HashMap<Integer, Stack<ActiveDescriptor<?>>> recorders = rlc.getRecorders();

        assertEquals(recorders.toString(), 1, recorders.size());

        Stack<ActiveDescriptor<?>> recorder = recorders.get(10);
        assertNotNull(recorder);

        List<ActiveDescriptor<?>> activations = new ArrayList<ActiveDescriptor<?>>(recorder);

        assertEquals(4, activations.size());

        ActiveDescriptor<?> a0 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelControllerHighPriority.class.getName()));
        ActiveDescriptor<?> a1 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelControllerTen.class.getName()));
        ActiveDescriptor<?> a2 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelControllerMedPriority.class.getName()));
        ActiveDescriptor<?> a3 = locator.getBestDescriptor(BuilderHelper.createContractFilter(RunLevelControllerLowPriority.class.getName()));

        Iterator<ActiveDescriptor<?>> iter = activations.iterator();
        assertEquals("order is important", a0, iter.next());
        assertEquals("order is important", a1, iter.next());
        assertEquals("order is important", a2, iter.next());
        assertEquals("order is important", a3, iter.next());

        proceedToAndWait(rlc, locator, 0);

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
            configurator.bind(BuilderHelper.link(RunLevelControllerImpl.class).
                    to(RunLevelController.class).in(Singleton.class.getName()).build());

            bindService(configurator, TestListener.class, Singleton.class);

            // Bind each service
            for (int i = 0; i < services.length; i++) {
                bindService(configurator, services[i], null);
            }
        }

        private void bindService(DynamicConfiguration configurator, Class<?> service, Class<? extends Annotation> scope) {
            final DescriptorBuilder descriptorBuilder = BuilderHelper.link(service);

            final RunLevel rla = service.getAnnotation(RunLevel.class);
            if (rla != null) {
                descriptorBuilder.//to(RunLevel.class).
                        has(RunLevel.RUNLEVEL_VAL_META_TAG, Collections.singletonList(((Integer) rla.value()).toString())).
                        has(RunLevel.RUNLEVEL_MODE_META_TAG, Collections.singletonList(((Integer) rla.mode()).toString()));

                descriptorBuilder.in(RunLevel.class);
            }
            Class clazz = service;
            while (clazz != null) {
                Class<?>[] interfaces = clazz.getInterfaces();
                for (int j = 0; j < interfaces.length; j++) {
                    descriptorBuilder.to(interfaces[j]);
                }
                clazz = clazz.getSuperclass();
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

    @RunLevel(RunLevel.RUNLEVEL_VAL_IMMEDIATE)
    @Service
    public static class RunLevelControllerNegOne extends TestService {
    }

    @RunLevel(1)
    @Service
    public static class RunLevelControllerOne extends TestService {
    }

    @RunLevel(5)
    @Service
    public static class RunLevelControllerFive extends TestService {
    }

    @RunLevel(10)
    @Service
    public static class RunLevelControllerTen extends TestService {
    }

    @RunLevel(10)
    // @Priority(1)
    @Service
    public static class RunLevelControllerHighPriority extends TestService {
    }

    @RunLevel(10)
    // @Priority(6)
    @Service
    public static class RunLevelControllerMedPriority extends TestService {
    }

    @RunLevel(10)
    // @Priority(9)
    @Service
    public static class RunLevelControllerLowPriority extends TestService {
    }

    @RunLevel(15)
    @Service
    public static class RunLevelControllerProceedDownInPostConstruct extends TestService {
        @Inject
        private RunLevelControllerImpl rlc;

        @Override
        public void postConstruct() {
            rlc.proceedTo(5);
        }
    }

    @RunLevel(15)
    @Service
    public static class RunLevelControllerProceedUpInPostConstruct extends TestService {
        @Inject
        private RunLevelControllerImpl rlc;

        @Override
        public void postConstruct() {
            rlc.proceedTo(25);
        }
    }

    @RunLevel(25)
    @Service
    public static class RunLevelControllerTwentyFive extends TestService {
    }

    @RunLevel(20)
    @Service
    public static class RunLevelControllerTwenty extends TestService {
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

    @RunLevel(10)
    @Service
    public static class RunLevelControllerA extends TestService implements ServiceA {
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

    @RunLevel(10)
    @Service
    public static class RunLevelControllerB extends TestService implements ServiceB {
        @Inject
        ServiceC serviceC;
    }

    @RunLevel(10)
    @Service
    public static class RunLevelControllerC extends TestService implements ServiceC {
    }

    @Contract
    public interface ServiceD {
        public void doSomething();
    }

    @RunLevel(value = 10, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
    @Service
    public static class RunLevelControllerNonValidating extends TestService implements ServiceD {
        @Override
        public void doSomething() {
        }
    }

    @Contract
    public interface RunLevelDepService {
        public void useRunLevelController();
    }

    @Service
    public static class NonRunLevelWithRunLevelDepService extends TestService implements RunLevelDepService {
        @Inject
        ServiceD serviceD;

        public void useRunLevelController() {
            serviceD.doSomething();
        }
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
        public void onCancelled(RunLevelController controller, int previousProceedTo, boolean isInterrupt) {
        }

        @Override
        public void onError(RunLevelController controller, Throwable error, boolean willContinue) {
        }

        @Override
        public void onProgress(RunLevelController controller) {
            synchronized (this) {
                if (controller.getCurrentRunLevel() == waitForRunLevel) {
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
        public void onCancelled(RunLevelController controller, int previousProceedTo, boolean isInterrupt) {
        }

        @Override
        public void onError(RunLevelController controller, Throwable error, boolean willContinue) {
        }

        @Override
        public void onProgress(RunLevelController controller) {
            lastRunLevel = controller.getCurrentRunLevel();
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
}




