package org.glassfish.hk2;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.jvnet.hk2.annotations.Contract;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Contract for handling the activation and deactivation of run level
 * services.
 */
@Contract
public interface RunLevelActivator {

    /**
     * Activate the run level service associated with given descriptor.
     *
     * @param activeDescriptor  the descriptor
     */
    void activate(ActiveDescriptor<?> activeDescriptor);

    /**
     * Deactivate the run level service associated with given descriptor.
     *
     * @param activeDescriptor  the descriptor
     */
    void deactivate(ActiveDescriptor<?> activeDescriptor);

    /**
     * Wait for completion of run level progression.
     *
     * @throws java.util.concurrent.ExecutionException    if the completion code threw an exception
     * @throws java.util.concurrent.TimeoutException      if the wait timed out
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     */
    void awaitCompletion() throws ExecutionException, InterruptedException,
            TimeoutException;

    /**
     * Wait for completion of run level progression.
     *
     * @param timeout  the timeout value
     * @param unit     the time unit
     *
     * @throws ExecutionException    if the completion code threw an exception
     * @throws TimeoutException      if the wait timed out
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     */
    void awaitCompletion(long timeout, TimeUnit unit) throws ExecutionException,
            InterruptedException, TimeoutException;
}
