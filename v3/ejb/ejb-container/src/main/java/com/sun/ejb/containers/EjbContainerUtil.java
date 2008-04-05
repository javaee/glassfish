package com.sun.ejb.containers;

import com.sun.enterprise.container.common.spi.JavaEETransaction;
import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.iiop.security.GSSUtils;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.PostConstruct;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Contract
public interface EjbContainerUtil {

    public  Logger getLogger();

    public  void setEJBTimerService(EJBTimerService es);

    public  EJBTimerService getEJBTimerService();

    public  void registerContainer(BaseContainer container);

    public  void unregisterContainer(BaseContainer container);

    public  BaseContainer getContainer(long id);

    public  EjbDescriptor getDescriptor(long id);

    public  ClassLoader getClassLoader(long id);

    public  Timer getTimer();

    public  void setInsideContainer(boolean bool);

    public  boolean isInsideContainer();

    public  InvocationManager getInvocationManager();

    public  InjectionManager getInjectionManager();

    public  GlassfishNamingManager getGlassfishNamingManager();

    public  ComponentEnvManager getComponentEnvManager();

    public  ComponentInvocation getCurrentInvocation();

    public JavaEETransactionManager getTransactionManager();

    public ServerContext getServerContext();

    public  ContainerSynchronization getContainerSync(Transaction jtx)
        throws RollbackException, SystemException;

    public void removeContainerSync(Transaction tx);

    public EjbContainer getEjbContainer();

    public Application findApplicationByName(String appName);

    public ServerEnvironment getServerEnvironment();

    public Agent getCallFlowAgent();
    
    public Vector getBeans(Transaction jtx);

    public void addWork(Runnable task);
    
}
