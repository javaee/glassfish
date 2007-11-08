/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;

import java.util.*;
import java.lang.reflect.*;
import java.rmi.Remote;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.transaction.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import com.sun.ejb.*;
import com.sun.enterprise.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.distributedtx.J2EETransaction;
import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;
import com.sun.enterprise.log.Log;
import com.sun.enterprise.security.SecurityContext;

import com.sun.ejb.containers.builder.BaseContainerBuilder;
import com.sun.ejb.containers.builder.StatefulContainerBuilder;

import com.sun.ejb.containers.util.LongHashMap;
import com.sun.enterprise.util.LocalStringManagerImpl;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.ConfigContext;

import java.util.logging.*;
import com.sun.logging.*;

import com.sun.ejb.spi.container.ContainerService;
import com.sun.ejb.base.container.ContainerServiceImpl;
import com.sun.ejb.base.io.IOUtils;

import com.sun.ejb.spi.distributed.DistributedEJBServiceFactory;
import com.sun.ejb.spi.distributed.DistributedEJBTimerService;
import com.sun.ejb.base.distributed.AdminEJBTimerEventListenerImpl;

import com.sun.enterprise.server.event.*;
import com.sun.enterprise.util.EntityManagerFactoryWrapper;

/**
 * A factory for containers. Called from JarManagerImpl during 
 * deployment of EJB JARs and everytime the J2EEServer starts up.
 * There is exactly one instance of ContainerFactoryImpl per JVM.
 *
 * @author Darpan Dinker
 */

import com.sun.ejb.containers.util.PoolCacheTimer;

public final class ContainerFactoryImpl implements ContainerFactory {
    private boolean debugMonitoring=false; 
    private long debugMonitoringPeriodMS = 60 * 1000L;

    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    private static final boolean debug = false;
    public static final byte HOME_KEY = (byte)0xff;
    public static final byte[] homeInstanceKey = {HOME_KEY};

    private EJBTimerService ejbTimerService;

    private PMTransactionManagerImpl pmtm;

    LongHashMap containers = new LongHashMap(128);

    private ThreadLocal threadLocalContext = new ThreadLocal();

    private static PoolCacheTimer _timer = new PoolCacheTimer();
    
    private PoolCacheTimer _localTimer;

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ContainerFactoryImpl.class);

    private static ContainerService _containerService;

    public ContainerFactoryImpl()
    {

        // Create the PMTransactionManagerImpl, this is looked up
        // thru JNDI by the PM at the name "java:pm/TransactionManager".
        pmtm = new PMTransactionManagerImpl();

        _localTimer = _timer;

        getDebugMonitoringDetails();  
        
        if(debugMonitoring) {
            _timer.schedule(new DebugMonitor(), 0L, debugMonitoringPeriodMS);
        }

        _containerService = new ContainerServiceImpl();
        _containerService.initializeService();

        IOUtils.setJ2EEObjectStreamFactory(
            _containerService.getJ2EEObjectStreamFactory());
        
    }

    public static ContainerService getContainerService() {
        return _containerService;
    }

    public EntityManager lookupExtendedEntityManager(EntityManagerFactory factory) {

        Switch theSwitch = Switch.getSwitch();

        InvocationManager invMgr = theSwitch.getInvocationManager();
        ComponentInvocation inv = invMgr.getCurrentInvocation();

        EntityManager em = null;
        
        if( (inv != null) && 
            (inv.getInvocationType() == ComponentInvocation.EJB_INVOCATION )) {

            EjbDescriptor ejbDesc = (EjbDescriptor)
                theSwitch.getDescriptorFor(inv.getContainerContext());

            if( (ejbDesc instanceof EjbSessionDescriptor) &&
                ( ((EjbSessionDescriptor)ejbDesc).isStateful() ) ) {
                em = ((SessionContextImpl)inv.context).
                    getExtendedEntityManager(factory);
            }
        }

        return em;
    }

    public void initEJBTimerService() throws Exception {
        ejbTimerService = null;
    }

    /**
     * Set EJB Timer Service.  Can be null if timer service is being disabled.
     */
    public void setEJBTimerService(EJBTimerService ejbTimerService) {
        this.ejbTimerService = ejbTimerService;	

        DistributedEJBServiceFactory.setDistributedEJBTimerService( 
            (DistributedEJBTimerService)ejbTimerService );

        if( null != ejbTimerService ) {
            //Register the Admin event listener to satisfy requests from the 
            //admin cli. The AdminEJBTimerListerner is created as a singleton
            AdminEJBTimerEventListenerImpl.getEjbTimerEventListener();
        }
    }

    public void restoreEJBTimers() throws Exception {
        if( ejbTimerService != null ) {
            ejbTimerService.restoreTimers();
        }
    }

    public void shutdownEJBTimerService() {
        if( ejbTimerService != null ) {
            ejbTimerService.shutdown();
        }
    }

    private void getDebugMonitoringDetails() { 
        try{
            Properties props = System.getProperties();
            String str=props.getProperty("MONITOR_EJB_CONTAINER");
            if( null != str) {
		str = str.toLowerCase();
                debugMonitoring = Boolean.valueOf(str).booleanValue();
		String period =
		    props.getProperty("MONITOR_EJB_TIME_PERIOD_SECONDS");
		if (period != null) {
                debugMonitoringPeriodMS =
		    (new Long(period).longValue())* 1000;
		}
            }
        } catch(Exception e) {
            _logger.log(Level.INFO,
                "ContainerFactoryImpl.getDebugMonitoringDetails(), " +
                " Exception when trying to " + 
                "get the System properties - ", e);
        }
    }

    public static java.util.Timer getTimer() {
        return _timer;
    }

    public TransactionManager getTransactionMgr()
    {
        return pmtm;
    }

    public Container createContainer(EjbDescriptor ejbDescriptor, 
				     ClassLoader loader, 
				     com.sun.enterprise.SecurityManager sm,
				     ConfigContext dynamicConfigContext)
	     throws Exception 
    {
        BaseContainer container = null;
        boolean hasHome = true;
        String commitOption = null;
        String appid = ejbDescriptor.getApplication().getRegistrationName();
        String archiveuri = ejbDescriptor.getEjbBundleDescriptor().
            getModuleDescriptor().getArchiveUri();
            
        String modulename = 
            com.sun.enterprise.util.io.FileUtils.makeFriendlyFilename(archiveuri);
        String ejbname = ejbDescriptor.getName();

        IASEjbExtraDescriptors iased = null;
        //Server svr = null;
        Config cfg = null;
        EjbContainer ejbContainerDesc = null;

        try {
            // instantiate container class
            if (ejbDescriptor instanceof EjbSessionDescriptor) {
                EjbSessionDescriptor sd = (EjbSessionDescriptor)ejbDescriptor;
                if ( sd.isStateless() ) {
                    container = new StatelessSessionContainer(ejbDescriptor, loader);
                } else {
                    //container = new StatefulSessionContainer(ejbDescriptor, loader);
		    BaseContainerBuilder builder =
			new StatefulContainerBuilder();
		    builder.buildContainer(ejbDescriptor, loader,
			dynamicConfigContext);
		    container = builder.getContainer();
		    //containers.put(ejbDescriptor.getUniqueId(), container);
		    //builder.completeInitialization(sm);
                }
            } else if ( ejbDescriptor instanceof EjbMessageBeanDescriptor ) {
                container = new MessageBeanContainer(ejbDescriptor, loader);
		// Message-driven beans don't have a home or remote interface.
                hasHome = false;
            } else {
                if (((EjbEntityDescriptor)ejbDescriptor).getIASEjbExtraDescriptors()
                    .isIsReadOnlyBean()) { 

                    EjbEntityDescriptor robDesc = (EjbEntityDescriptor) ejbDescriptor;                    
                    container = new ReadOnlyBeanContainer (ejbDescriptor, loader);
                } else 
                    if ((ejbDescriptor.getLocalHomeClassName() != null) &&
                        (ejbDescriptor.getLocalHomeClassName()
                         .equals("com.sun.ejb.containers.TimerLocalHome"))) {
                        container = new TimerBeanContainer(ejbDescriptor, loader);
                    } else {
                        iased = ((EjbEntityDescriptor)ejbDescriptor).
                            getIASEjbExtraDescriptors();
                        if (iased != null) {
                            commitOption = iased.getCommitOption();    	
                        }
                        if (commitOption == null) {
                            try {
                                ServerContext sc = 
                                    ApplicationServer.getServerContext();

                                cfg = ServerBeansFactory.getConfigBean
                                    (sc.getConfigContext());
 
                            }  catch (ConfigException ex) {
                                _logger.log(Level.WARNING, 
                                            "ejb.createContainer_exception", ex);
                            }

                            ejbContainerDesc = cfg.getEjbContainer();
 
                            commitOption = ejbContainerDesc.getCommitOption();  
                        }
                        if (commitOption.equals("A")) {
                            _logger.log(Level.WARNING, 
                                        "ejb.commit_option_A_not_supported",
                                        new Object []{ejbDescriptor.getName()}
                                        );
                            container = 
                                new EntityContainer(ejbDescriptor, loader);
                        } else if (commitOption.equals("C")) {
                            _logger.log(Level.FINE, "Using commit option C for: " 
                                        + ejbDescriptor.getName());
                            container = new CommitCEntityContainer(ejbDescriptor,
                                                                   loader);
                        } else {
                            _logger.log(Level.FINE,"Using commit option B for: " + 
                                        ejbDescriptor.getName());
                            container = new EntityContainer(ejbDescriptor, loader);
                        }
                    }
            }

            containers.put(ejbDescriptor.getUniqueId(), container);
		
            container.setSecurityManager(sm);
    
            // Initialize home after putting the container into containers,
            // so that any calls from ProtocolManager during home initialization
            // (e.g. is_a during PRO.narrow) will work.
            if ( hasHome ) {
                container.initializeHome();
            }

            container.setDebugMonitorFlag(debugMonitoring);

            return container;
        } catch ( InvalidNameException ex ) {
            _logger.log(Level.SEVERE,"ejb.create_container_exception", ex.toString());
            _logger.log(Level.SEVERE,"Invalid jndiName for" + "appId=" + appid +
                        "; moduleName=" + modulename + "; ejbName=" + ejbname); 
            _logger.log(Level.SEVERE,"jndiName=" +  ejbDescriptor.getJndiName());
            
            // removes the ejb from containers table
            try {
                removeContainer(ejbDescriptor.getUniqueId());
            } catch (Exception e) { 
                _logger.log(Level.FINE, "", e);            
            }
            
            throw ex;
        } catch (UnsupportedOperationException unSupEx) {
            throw unSupEx;
        } catch ( Exception ex ) {
            _logger.log(Level.SEVERE,"ejb.create_container_exception", ex.toString());
            _logger.log(Level.SEVERE,"appId=" + appid + " moduleName=" + modulename + 
                        " ejbName=" + ejbname);
            if (debug) {
                _logger.log(Level.SEVERE,  ex.getMessage(), ex);
            }
            
            // removes the ejb from containers table
            try {
                removeContainer(ejbDescriptor.getUniqueId());
            } catch (Exception e) { 
                _logger.log(Level.FINE, "", e);            
            }
            
            throw ex;
        }
    }


    /**
     * Get the container instance corresponding to the given EJB id.
     * Called from the POAProtocolMgr when an invocation arrives for
     * the home/remote object (and other callers).
     */
    public Container getContainer(long ejbId) {
        return (Container)containers.get(ejbId);
    }


    /**
     * Remove the container instance corresponding to the given EJB id.
     */
    public void removeContainer(long ejbId) {
        containers.remove(ejbId);
    }

    /**
     * List all container instances in this JVM.
     */
    public Enumeration listContainers() {
        return containers.elements();
    }


    /**
     * Return the EjbDescriptor for the given ejbId.
     * Called from the ProtocolManager.
     */
    public EjbDescriptor getEjbDescriptor(long ejbId)
    {
        Container c = (Container)containers.get(ejbId);

        if ( c == null ) {
            return null;
        }
        return c.getEjbDescriptor();
    }

    public Object getEJBContextObject(String contextType) {

        InvocationManager invMgr = Switch.getSwitch().getInvocationManager();

        ComponentInvocation currentInv = invMgr.getCurrentInvocation();

        if(currentInv == null) {
            throw new IllegalStateException("no current invocation");
        } else if (currentInv.getInvocationType() != 
                   ComponentInvocation.EJB_INVOCATION) {
            throw new IllegalStateException
                ("Illegal invocation type for EJB Context : " 
                 + currentInv.getInvocationType());
        }
        
        Object returnObject = currentInv.context;

        if( contextType.equals("javax.ejb.TimerService") ) {
            if( ejbTimerService == null ) {
                throw new IllegalStateException("EJB Timer Service not " +
                                                "available");
            }
            returnObject = new EJBTimerServiceWrapper
                (ejbTimerService, (EJBContextImpl) currentInv.context);
        }

        return returnObject;
    }

    EJBTimerService getEJBTimerService() {
        return ejbTimerService;
    }
    

    /**
     * Get/create a ContainerSynchronization object for the given tx.
     * Called only from BaseContainer.
     */
    ContainerSynchronization getContainerSync(Transaction tx)
        throws RollbackException, SystemException
    {
        TxData txData = (TxData) ((J2EETransaction) tx).getContainerData();
            
        if ( txData == null ) {
            txData = new TxData();
            ((J2EETransaction)tx).setContainerData(txData);
        }

        if( txData.sync == null ) {
            txData.sync = new ContainerSynchronization(tx, this);
            tx.registerSynchronization(txData.sync);
        }

        return txData.sync;
    }
	
    void removeContainerSync(Transaction tx) {
    }

    Vector getBeans(Transaction tx) {

        TxData txData = (TxData) ((J2EETransaction) tx).getContainerData();
            
        if ( txData == null ) {
            txData = new TxData();
            ((J2EETransaction)tx).setContainerData(txData);
        }

        if( txData.beans == null ) {
            txData.beans = new Vector();
        }
        
        return txData.beans;

    }

    class DebugMonitor
	extends java.util.TimerTask {
        public void run() {
            try {
                Enumeration enumContainers = listContainers();
                if( null == enumContainers ) {
                    _logger.log(Level.INFO, 
                   "MONITORING:: No containers available to report monitoring stats"); 
                    return;
                }
                while(enumContainers.hasMoreElements()) {
                    BaseContainer container =
			(BaseContainer) enumContainers.nextElement();
		    container.logMonitoredComponentsData();
		    /*
                    if(container instanceof EntityContainer) {
                        _logger.log(Level.INFO, "MONITORING::" + 
                           ((EntityContainer)container).getMonitorAttributeValues() );
                    } else if (container instanceof StatefulSessionContainer) { 
                        _logger.log(Level.INFO, "MONITORING::" + 
                           ((StatefulSessionContainer)container).
                                    getMonitorAttributeValues() );
                    } else if (container instanceof StatelessSessionContainer) {
                        _logger.log(Level.INFO, "MONITORING::" + 
                           ((StatelessSessionContainer)container).
                                    getMonitorAttributeValues() );
                    } else if (container instanceof MessageBeanContainer) {
                        _logger.log(Level.INFO, "MONITORING::" + 
                           ((MessageBeanContainer)container).
                                    getMonitorAttributeValues() );
                    }
		    */
                }	
            } catch (Throwable th) {
                _logger.log(Level.FINE, "Exception thrown", th);
            }
        }
    }

    // Various pieces of data associated with a tx.  Store directly
    // in J2EETransaction to avoid repeated Map<tx, data> lookups.
    private static class TxData {
        ContainerSynchronization sync;
        Vector beans;
    }

} //ContainerFactoryImpl


class BeanContext {
    ClassLoader previousClassLoader;
    boolean classLoaderSwitched;
    SecurityContext previousSecurityContext;
}

class ArrayListStack
    extends ArrayList
{
    /**
     * Creates a stack with the given initial size
     */
    public ArrayListStack(int size) {
        super(size);
    }
    
    /**
     * Creates a stack with a default size
     */
    public ArrayListStack() {
        super();
    }

    /**
     * Pushes an item onto the top of this stack. This method will internally
     * add elements to the <tt>ArrayList</tt> if the stack is full.
     *
     * @param   obj   the object to be pushed onto this stack.
     * @see     java.util.ArrayList#add
     */
    public void push(Object obj) {
        super.add(obj);
    }

    /**
     * Removes the object at the top of this stack and returns that 
     * object as the value of this function. 
     *
     * @return     The object at the top of this stack (the last item 
     *             of the <tt>ArrayList</tt> object). Null if stack is empty.
     */
    public Object pop() {
        int sz = super.size();
        return (sz > 0) ? super.remove(sz-1) : null;
    }
    
    /**
     * Tests if this stack is empty.
     *
     * @return  <code>true</code> if and only if this stack contains 
     *          no items; <code>false</code> otherwise.
     */
    public boolean empty() {
        return super.size() == 0;
    }

    /**
     * Looks at the object at the top of this stack without removing it 
     * from the stack. 
     *
     * @return     the object at the top of this stack (the last item 
     *             of the <tt>ArrayList</tt> object).  Null if stack is empty.
     */
    public Object peek() {
        int sz = size();
        return (sz > 0) ? super.get(sz-1) : null;
    }



} //ArrayListStack

