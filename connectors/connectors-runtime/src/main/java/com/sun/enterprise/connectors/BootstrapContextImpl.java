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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.WorkContextHandler;
import com.sun.logging.LogDomains;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkContext;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * BootstrapContext implementation.
 *
 * @author Qingqing Ouyang, Binod P.G
 */
public final class BootstrapContextImpl implements BootstrapContext, Serializable {

    private String poolId;
    private WorkManager wm;
    private XATerminator xa;
    private String moduleName;
    private String threadPoolId;

    private static final Logger logger =
            LogDomains.getLogger(BootstrapContextImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Constructs a <code>BootstrapContext</code> with default
     * thread pool for work manager.
     *
     * @throws ConnectorRuntimeException If there is a failure in
     *         retrieving WorkManager.
     */
    public BootstrapContextImpl (String moduleName) throws ConnectorRuntimeException{
        this.moduleName = moduleName;
        initializeWorkManager();
    }

    /**
     * Constructs a <code>BootstrapContext</code> with a specified
     * thread pool for work manager.
     *
     * @throws ConnectorRuntimeException If there is a failure in
     *         retrieving WorkManager.
     */
    public BootstrapContextImpl (String poolId, String moduleName)
                                 throws ConnectorRuntimeException{
        this.threadPoolId = poolId;
        this.moduleName = moduleName;
        initializeWorkManager();
    }

    /**
     * Creates a <code>java.util.Timer</code> instance.
     * This can cause a problem, since the timer threads are not actually
     * under appserver control. We should override the timer later.
     *
     * @return <code>java.util.Timer</code> object.
     */
    public Timer createTimer() {
        return new Timer("connectors-runtime-context");
		//TODO V3 daemon ?
    }

    /**
     * {@inheritDoc}
     */
    public boolean isContextSupported(Class<? extends WorkContext> aClass) {
        WorkContextHandler wch = ConnectorRuntime.getRuntime().getWorkContextHandler();
        return wch.isContextSupported(true, aClass.getName());
    }

    /**
     * {@inheritDoc}
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        //TODO V3
        try{
            InitialContext ic = new InitialContext();
            return (TransactionSynchronizationRegistry)ic.lookup("java:comp/TransactionSynchronizationRegistry");
        }catch(Exception e){
            logger.log(Level.WARNING, "tx.sync.registry.lookup.failed", e);            
        }
        return null;
    }

    /**
     * Retrieves the work manager.
     *
     * @return <code>WorkManager</code> instance.
     * @see com.sun.enterprise.connectors.work.CommonWorkManager
     * @see com.sun.enterprise.connectors.work.WorkManagerFactory
     */
    public WorkManager getWorkManager() {
        initializeWorkManager();
        return wm;
    }

    /**
     * initialize work manager reference
     */
    private void initializeWorkManager() {
        if (wm == null) {
            try {
                wm = ConnectorRuntime.getRuntime().getWorkManagerProxy(threadPoolId, moduleName);
            } catch(Exception e) {
           	    logger.log(Level.SEVERE, "workmanager.instantiation_error", e);
            }
        }
    }


    /**
     * Retrieves the <code>XATerminator</code> object.
     */
    public XATerminator getXATerminator() {
        initializeXATerminator();
        return xa;
    }

    /**
     * initializes XATerminator reference
     */
    private void initializeXATerminator() {
        if (xa == null) {                                   
            xa = ConnectorRuntime.getRuntime().getXATerminatorProxy(moduleName);
        }
    }
}
