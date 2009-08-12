/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.ejb.containers;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.api.Globals;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.v3.server.ExecutorServiceFactory;
import com.sun.logging.LogDomains;
import com.sun.ejb.base.sfsb.util.EJBServerConfigLookup;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.ejb.spi.CMPDeployer;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Service
public class EjbContainerUtilImpl
    implements PostConstruct, PreDestroy, EjbContainerUtil {

    private Logger _logger = LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    // TODO Temporary thread pool for Timer Service. Also used by various
    // container pools for periodic resize/cleanup tasks.  We should probably use
    // a common thread pool by default but allow configuration of a timer-service
    // specific one.  Should also consider using a distinct JDK timer for
    // timer service than the one used by containers for periodic work.
    private ExecutorService executorService;
    
    @Inject
    private Habitat habitat;

    @Inject
    private ServerContext serverContext;

    //@Inject
    private  EJBTimerService _ejbTimerService;

    private  Map<Long, BaseContainer> id2Container
            = new ConcurrentHashMap<Long, BaseContainer>();

    private  Timer _timer = new Timer(true);

    private  boolean _insideContainer = true;

    @Inject
    private  InvocationManager _invManager;

    @Inject
    private  InjectionManager _injectionManager;

    @Inject
    private  GlassfishNamingManager _gfNamingManager;

    @Inject
    private  ComponentEnvManager _compEnvManager;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject
    private EjbContainer ejbContainer;

    @Inject
    private GlassFishORBHelper orbHelper;

    @Inject
    private ServerEnvironmentImpl env;

    @Inject(optional=true)
    private Agent callFlowAgent;

    @Inject //Note:- Not specific to any ejb descriptor
    private EJBServerConfigLookup ejbServerConfigLookup;

    @Inject
    private EjbAsyncInvocationManager ejbAsyncInvocationManager;

    private  static EjbContainerUtil _me;

    public String getHAPersistenceType() {
        return ejbServerConfigLookup.getSfsbHaPersistenceTypeFromConfig();
    }

    public void postConstruct() {
        if (callFlowAgent == null) {
            callFlowAgent = (Agent) Proxy.newProxyInstance(EjbContainerUtilImpl.class.getClassLoader(),
                    new Class[] {Agent.class},
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method m, Object[] args) {
                            return null;
                        }
                    });
        }

        ThreadFactory tf = new EjbTimerThreadFactory();
        executorService = Executors.newCachedThreadPool(tf);

        _me = this;
    }

    public void preDestroy() {
        if( executorService != null ) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public GlassFishORBHelper getORBHelper() {
        return orbHelper;
    }

    public Habitat getDefaultHabitat() {
        return habitat;
    }

    public static boolean isInitialized() {
        return (_me != null);        
    }

    public static EjbContainerUtil getInstance() {
        if (_me == null) {
            // This situation shouldn't happen. Print the error message 
            // and the stack trace to know how did we get here.

            // Create the instance first to access the logger.
            _me = Globals.getDefaultHabitat().getComponent(
                    EjbContainerUtilImpl.class);
            _me.getLogger().log(Level.WARNING, 
                    "Internal error: EJBContainerUtilImpl was null",
                    new Throwable());
        }
        return _me;
    }

    public  Logger getLogger() {
        return _logger;
    }

    public  void setEJBTimerService(EJBTimerService es) {
        _ejbTimerService = es;
    }

    public  EJBTimerService getEJBTimerService() {
        return _ejbTimerService;
    }

    public  void registerContainer(BaseContainer container) {
        id2Container.put(container.getContainerId(), container);
    }

    public  void unregisterContainer(BaseContainer container) {
        id2Container.remove(container.getContainerId());
    }

    public  BaseContainer getContainer(long id) {
        return id2Container.get(id);
    }

    public  EjbDescriptor getDescriptor(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getEjbDescriptor() : null;
    }

    public  ClassLoader getClassLoader(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getClassLoader() : null;
    }

    public  Timer getTimer() {
        return _timer;
    }

    public  void setInsideContainer(boolean bool) {
        _insideContainer = bool;
    }

    public  boolean isInsideContainer() {
        return _insideContainer;
    }

    public  InvocationManager getInvocationManager() {
        return _invManager;
    }

    public  InjectionManager getInjectionManager() {
        return _injectionManager;
    }

    public  GlassfishNamingManager getGlassfishNamingManager() {
        return _gfNamingManager;
    }

    public  ComponentEnvManager getComponentEnvManager() {
        return _compEnvManager;
    }

    public  ComponentInvocation getCurrentInvocation() {
        return _invManager.getCurrentInvocation();
    }

    public JavaEETransactionManager getTransactionManager() {
        return txMgr;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public EjbAsyncInvocationManager getEjbAsyncInvocationManager() {
        return ejbAsyncInvocationManager;
    }

    private TxData getTxData(JavaEETransaction tx) {
        TxData txData = tx.getContainerData();

        if ( txData == null ) {
            txData = new TxData();
            tx.setContainerData(txData);
        }
        
        return txData;
    }
    
    public  ContainerSynchronization getContainerSync(Transaction jtx)
        throws RollbackException, SystemException
    {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        if( txData.sync == null ) {
            txData.sync = new ContainerSynchronization(tx, this);
            tx.registerSynchronization(txData.sync);
        }

        return txData.sync;
    }

    public void removeContainerSync(Transaction tx) {
        //No op
    }

    public void registerPMSync(Transaction jtx, Synchronization sync)
            throws RollbackException, SystemException {

        getContainerSync(jtx).addPMSynchronization(sync);
    }

    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    public ServerEnvironmentImpl getServerEnvironment() {
        return env;
    }

    public  Vector getBeans(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        if( txData.beans == null ) {
            txData.beans = new Vector();
        }

        return txData.beans;

    }

    public Object getActiveTxCache(Transaction jtx) {
    	JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);
        
        return txData.activeTxCache;
    }

    public void setActiveTxCache(Transaction jtx, Object cache) {
    	JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);
        
        txData.activeTxCache = cache;
    }
    
    public Agent getCallFlowAgent() {
        return callFlowAgent;
    }

    public void addWork(Runnable task) {

        executorService.submit(task);
    }

    public EjbDescriptor ejbIdToDescriptor(long ejbId) {

        throw new RuntimeException("Not supported yet");

    }

    public boolean isEJBLite() {
        return (habitat.getByContract(CMPDeployer.class) == null);
    }

    private static class EjbTimerThreadFactory
        implements ThreadFactory {

        private AtomicInteger threadId = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            // TODO change this to use common thread pool
            Thread th = new Thread(r, "Ejb-Timer-Thread-" + threadId.incrementAndGet());
            th.setDaemon(true);

            // Prevent any app classloader being set as CCL
            // App classloader is set by task itself
            th.setContextClassLoader(null);
            return th;
        }
    }

    // Various pieces of data associated with a tx.  Store directly
    // in J2EETransaction to avoid repeated Map<tx, data> lookups.
    private  class TxData {
        ContainerSynchronization sync;
        Vector beans;
        Object activeTxCache;
    }
    
}
