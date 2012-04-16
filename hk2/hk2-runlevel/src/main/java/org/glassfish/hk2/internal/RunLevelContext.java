package org.glassfish.hk2.internal;

import org.glassfish.hk2.RunLevelException;
import org.glassfish.hk2.RunLevelService;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.RunLevel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Run level context.
 *
 * @author tbeerbower
 */
@Singleton
public class RunLevelContext implements Context<RunLevel> {
    private Map<String, Map<ActiveDescriptor<?>, Object>> backingMaps =
            new HashMap<String, Map<ActiveDescriptor<?>, Object>>();
    
    @Inject
    private IterableProvider<RunLevelService> allRunLevelServices;


    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#getScope()
    */
    @Override
    public Class<? extends Annotation> getScope() {
        return RunLevel.class;
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
    */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T findOrCreate(ActiveDescriptor<T> activeDescriptor,
                              ServiceHandle<?> root) {

        Map<ActiveDescriptor<?>, Object> backingStore = getBackingMap(activeDescriptor);

        if (backingStore.containsKey(activeDescriptor)) {
            return (T) backingStore.get(activeDescriptor);
        }

        String scope = RunLevelServiceImpl.getRunLevelServiceName(activeDescriptor);
        RunLevelService runLevelService =  allRunLevelServices.named(scope).get();
        if (runLevelService == null) {
            runLevelService = allRunLevelServices.get();
        }

        RunLevel.Mode mode = RunLevelServiceImpl.getRunLevelMode(activeDescriptor);
        
        if (mode == RunLevel.Mode.VALIDATING) {
            verifyState(activeDescriptor, runLevelService);
        }

        T retVal = activeDescriptor.create(root);
        backingStore.put(activeDescriptor, retVal);

        if (mode == RunLevel.Mode.VALIDATING) {
            runLevelService.recordActivation(activeDescriptor);
        }

        return retVal;
    }

    /**
     * Verifies that the state of the RunLevelService is appropriate for this
     * instance activation.
     *
     * @param descriptor  the descriptor
     * @param service     the run level service
     *
     * @throws RunLevelException  if the verification fails
     */
    public void verifyState(ActiveDescriptor<?> descriptor, RunLevelService service) throws RunLevelException {

        Integer runLevel = RunLevelServiceImpl.getRunLevelValue(descriptor);

        String scope = RunLevelServiceImpl.getRunLevelServiceName(descriptor);

        Integer planned = service.getPlannedRunLevel();
        Integer current = service.getCurrentRunLevel();

        if (!(!(planned == null && current == null) &&
                ((planned == null || runLevel <= planned) &&
                        (current == null || runLevel <= (current + 1))))) {
            throw new RunLevelException("unable to activate " + this +
                    "; minimum expected RunLevel is: " + runLevel +
                    "; planned is: " + planned +
                    "; current is: " + current);
        }
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#find(org.glassfish.hk2.api.ActiveDescriptor)
    */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(ActiveDescriptor<T> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore = getBackingMap(activeDescriptor);

        return (T) backingStore.get(activeDescriptor);
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#isActive()
    */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * Deactivate the given descriptor.
     *
     * @param activeDescriptor  the descriptor
     * @param <T>               the type of the descriptor
     */
    public <T> void deactivate(ActiveDescriptor<T> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore = getBackingMap(activeDescriptor);

        if (backingStore.containsKey(activeDescriptor)) {
            activeDescriptor.dispose((T) backingStore.get(activeDescriptor));
            backingStore.remove(activeDescriptor);
        }
    }

    private Map<ActiveDescriptor<?>, Object> getBackingMap(ActiveDescriptor<?> descriptor) {

        String scope = RunLevelServiceImpl.getRunLevelServiceName(descriptor);

        Map<ActiveDescriptor<?>, Object> retVal = backingMaps.get(scope);
        if (retVal == null) {
            retVal = new HashMap<ActiveDescriptor<?>, Object>();

            backingMaps.put(scope, retVal);
        }

        return retVal;
    }
}
