package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.JavaEEContainer;
import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.ContainerUtil;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PreDestroy;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Timer;

@Service
public class ContainerUtilImpl
    implements ContainerUtil, PreDestroy {

    @Inject
    private InvocationManager invMgr;

    @Inject
    private ComponentEnvManager compEnvMgr;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject
    private CallFlowAgent callFlowAgent;
    
    private static Timer _timer = new Timer(true);

    private static ContainerUtil _util;

    public ContainerUtilImpl() {
        _util = this;
    }

    public static ContainerUtil getContainerUtil() {
        return _util;
    }

    public InvocationManager getInvocationManager() {
        return invMgr;
    }

    public ComponentEnvManager getComponentEnvManager() {
        return compEnvMgr;
    }

    public JavaEETransactionManager getJavaEETransactionManager() {
        return txMgr;
    }
    
    public EntityManager lookupExtendedEntityManager(EntityManagerFactory emf) {
        EntityManager em = null;

        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if( (inv != null) &&
            (inv.getInvocationType() == ComponentInvocation.ComponentInvocationType.EJB_INVOCATION )) {
            Object obj = inv.getContainer();
            if (obj instanceof JavaEEContainer) {
                em = ((JavaEEContainer) obj).lookupExtendedEntityManager(emf);
            }
        }

        return em;
    }

    public CallFlowAgent getCallFlowAgent() {
        return callFlowAgent;
    }
    public Timer getTimer() {
        return _timer;
    }

    public void scheduleTask(Runnable runnable) {
        //TODO: Get hold of a worker threadpool and run this runnable
        //TODO: Should we take the ContextClassLoader as parameter
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        _timer.cancel();
    }
}
