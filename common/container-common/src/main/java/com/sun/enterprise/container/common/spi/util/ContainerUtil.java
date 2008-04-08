package com.sun.enterprise.container.common.spi.util;

import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.annotations.Contract;

import java.util.Timer;

@Contract
public interface ContainerUtil {

    /**
     * Utility method to get hold of InvocationManager
     * @return InvocationManager
     */
    public InvocationManager getInvocationManager();

    /**
     * Utility method to return ComponentEnvManager
     * @return ComponentEnvManager
     */
    public ComponentEnvManager getComponentEnvManager();

    public CallFlowAgent getCallFlowAgent();
    
    /**
     * Utility method to return a JDK Timer. Containers must
     *  use this timer instead of creating their own
     * @return Timer
     */
    public Timer getTimer();

    /**
     * Utility method to schedule an asynchronous task. The
     *  implementation will prbaby choose a worker thread
     *  from a threadpool and execute the given runnable
     *  using the thread.
     *
     * @param runnable
     */
    public void scheduleTask(Runnable runnable);
    
}
