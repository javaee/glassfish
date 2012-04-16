package org.glassfish.hk2.internal;

import org.glassfish.hk2.RunLevelActivator;
import org.glassfish.hk2.RunLevelListener;
import org.glassfish.hk2.RunLevelService;
import org.glassfish.hk2.RunLevelSorter;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorBuilder;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Priority;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.RunLevelServiceIndicator;
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
 *
 */
public class RunLevelServiceTest {

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

        proceedToAndWait(rls, locator, RunLevel.RUNLEVEL_VAL_KERNAL);

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
                locator.getDescriptors(BuilderHelper.createContractFilter(RunLevel.class.getName()));

        assertNotNull(descriptors);
        int count = 0;
        for (ActiveDescriptor<?> descriptor : descriptors) {
            count++;
            assertNotNull(descriptor.getMetadata());
            String val = descriptor.getMetadata().get(RunLevel.RUNLEVEL_VAL_META_TAG).get(0);
            assertNotNull(descriptor.toString(), val);
            assertTrue(descriptor + " runLevel val=" + val, Integer.valueOf(val) >= RunLevel.RUNLEVEL_VAL_KERNAL);
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

    @Test
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

    // ----- inner class : RLSModule ----------------------------------------

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

            final RunLevel rla = service.getAnnotation(RunLevel.class);
            if (rla != null) {
                descriptorBuilder.to(RunLevel.class).
                        has(RunLevel.RUNLEVEL_VAL_META_TAG, Collections.singletonList(((Integer) rla.value()).toString())).
                        has(RunLevel.RUNLEVEL_MODE_META_TAG, Collections.singletonList(((Integer) rla.mode().toInt()).toString()));

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


    // ----- inner class : TestService --------------------------------------

    /**
     * Abstract service.
     */
    public static abstract class TestService
            implements org.glassfish.hk2.PostConstruct, org.glassfish.hk2.PreDestroy {


        @Override
        public void postConstruct() {
        }

        @Override
        public void preDestroy() {
        }

    }


    // ----- inner class : RunLevelServiceNegOne ----------------------------

    /**
     * A run level service with a value of RunLevel.KERNEL_RUNLEVEL.
     * Should be initialized by the framework immediately.
     */
    @RunLevel(RunLevel.RUNLEVEL_VAL_KERNAL)
    @Service
    public static class RunLevelServiceNegOne extends TestService {
    }


    // ----- inner class : RunLevelServiceOne -------------------------------

    /**
     * A run level service with a value of 1.
     */
    @RunLevel(1)
    @Service
    public static class RunLevelServiceOne extends TestService {
    }

    // ----- inner class : RunLevelServiceFive -------------------------------

    /**
     * A run level service with a value of 5.
     */
    @RunLevel(5)
    @Service
    public static class RunLevelServiceFive extends TestService {
    }

    // ----- inner class : RunLevelServiceFive -------------------------------

    /**
     * A run level service with a value of 5.
     */
    @RunLevel(5)
    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooRunLevelServiceFive extends TestService {
    }

    // ----- inner class : RunLevelServiceTen -------------------------------

    /**
     * A run level service with a value of 10.
     */
    @RunLevel(10)
    @Service
    public static class RunLevelServiceTen extends TestService {
    }

    @RunLevel(10)
    @Priority(1)
    @Service
    public static class RunLevelServiceHighPriority extends TestService {
    }

    @RunLevel(10)
    @Priority(6)
    @Service
    public static class RunLevelServiceMedPriority extends TestService {
    }

    @RunLevel(10)
    @Priority(9)
    @Service
    public static class RunLevelServiceLowPriority extends TestService {
    }

    // ----- inner class : RunLevelServiceTwenty -------------------------------

    /**
     * A run level service with a value of 20.
     */
    @RunLevel(20)
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

    @RunLevel(10)
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

    @RunLevel(10)
    @Service
    public static class RunLevelServiceB extends TestService implements ServiceB {
        @Inject
        ServiceC serviceC;
    }

    @RunLevel(10)
    @Service
    public static class RunLevelServiceC extends TestService implements ServiceC {
    }

    @Contract
    public interface ServiceD {
        public void doSomething();
    }

    @RunLevel(value = 10, mode = RunLevel.Mode.NON_VALIDATING)
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

    public static abstract class BaseActivator implements RunLevelActivator {

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

    public static class PrioritySorter implements RunLevelSorter {

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
            locator.reifyDescriptor(descriptor);
            Priority priority = descriptor.getImplementationClass().getAnnotation(Priority.class);
            return priority != null ? priority.value() : 5;
        }
    }

    @Service
    @RunLevelServiceIndicator("foo")
    public static class FooPrioritySorter extends PrioritySorter {
    }

}




