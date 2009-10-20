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

package com.sun.enterprise.transaction.jts;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.xa.XAResource;

import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;
import com.sun.enterprise.transaction.spi.RecoveryResourceListener;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;

import com.sun.jts.CosTransactions.DelegatedRecoveryManager;
import com.sun.jts.CosTransactions.RecoveryManager;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Resource recovery manager to recover transactions.
 *
 * @author Jagadish Ramu
 */
@Service
public class ResourceRecoveryManagerImpl implements PostConstruct, ResourceRecoveryManager {

    @Inject
    private TransactionService txnService;

    @Inject 
    private Habitat habitat;

    private JavaEETransactionManager txMgr;

    private Collection<RecoveryResourceHandler> recoveryResourceHandlers;

    private RecoveryResourceRegistry recoveryListenersRegistry;

    private static Logger _logger = 
            LogDomains.getLogger(JavaEETransactionManagerSimplified.class,
            LogDomains.JTA_LOGGER);
    private static StringManager localStrings = 
            StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private volatile boolean lazyRecovery = false;
    private volatile boolean configured = false;


    public void postConstruct() {
        // Recover XA resources if the auto-recovery flag in tx service is set to true
        recoverXAResources();
    }

    /**
     * recover incomplete transactions
     * @param delegated indicates whether delegated recovery is needed
     * @param logPath transaction log directory path
     * @return boolean indicating the status of transaction recovery
     * @throws Exception when unable to recover
     */
    public boolean recoverIncompleteTx(boolean delegated, String logPath) throws Exception {
        boolean result = false; 
        Map<RecoveryResourceHandler, Vector> handlerToXAResourcesMap = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Performing recovery of incomplete Tx...");
            }

            configure();
            Vector xaresList = new Vector();

            //TODO V3 will handle ThirdPartyXAResources also (v2 is not so). Is this fine ?
            handlerToXAResourcesMap = getAllRecoverableResources(xaresList);

            int size = xaresList.size();
            XAResource[] xaresArray = new XAResource[size];
            for (int i = 0; i < size; i++) {
                xaresArray[i] = (XAResource) xaresList.elementAt(i);
            }
            if (_logger.isLoggable(Level.FINE)) {
                String msg = localStrings.getString("xaresource.recovering", new Object[]
                    {"Recovering {0} XA resources...", String.valueOf(size)});

                _logger.log(Level.FINE, msg);
            }
            if (!delegated) {
                RecoveryManager.recoverIncompleteTx(xaresArray);
                result = true;
            } else {
                result = DelegatedRecoveryManager.delegated_recover(logPath, xaresArray);
            }

            return result;
        } finally {
            try {
                closeAllResources(handlerToXAResourcesMap);
            } catch (Exception ex1) {
                _logger.log(Level.WARNING, "xaresource.recover_error", ex1);
            }
        }
    }

    /**
     * close all resources provided using their handlers
     * @param resourcesToHandlers map that holds handlers and their resources
     */
    private void closeAllResources(Map<RecoveryResourceHandler, Vector> resourcesToHandlers) {
        if (resourcesToHandlers != null) {
            Set<Map.Entry<RecoveryResourceHandler, Vector>> entries = resourcesToHandlers.entrySet();
            for (Map.Entry<RecoveryResourceHandler, Vector> entry : entries) {
                RecoveryResourceHandler handler = entry.getKey();
                Vector resources = entry.getValue();
                handler.closeConnections(resources);
            }
        }
    }

    /**
     * get all recoverable resources
     * @param xaresList xa resources
     * @return recovery-handlers and their resources
     */
    private Map<RecoveryResourceHandler, Vector> getAllRecoverableResources(Vector xaresList) {
        Map<RecoveryResourceHandler, Vector> resourcesToHandlers = 
                new HashMap<RecoveryResourceHandler, Vector>();

        for (RecoveryResourceHandler handler : recoveryResourceHandlers) {
            //TODO V3 FINE LOG
            Vector resources = new Vector();
            handler.loadXAResourcesAndItsConnections(xaresList, resources);
            resourcesToHandlers.put(handler, resources);
        }
        return resourcesToHandlers;
    }

    /**
     * recover the xa-resources
     * @param force boolean to indicate if it has to be forced.
     */
    public void recoverXAResources(boolean force) {
        if (force) {
            try {
                //TODO V3, v2 has txnService.isAutomaticRecovery
                if (!Boolean.valueOf(txnService.getAutomaticRecovery())) {
                    return;
                }
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "ejbserver.recovery",
                            "Perform recovery of XAResources...");
                }

                configure();

                /** TBD - Not needed for PE. When used when does it need to register?
                RecoveryManager.registerTransactionRecoveryService(
                        habitat.getByContract(TransactionRecovery.class));
                **/
                
                Vector xaresList = new Vector();
                Map<RecoveryResourceHandler, Vector> resourcesToHandler = 
                        getAllRecoverableResources(xaresList);

                int size = xaresList.size();
                XAResource[] xaresArray = new XAResource[size];
                for (int i = 0; i < size; i++) {
                    xaresArray[i] = (XAResource) xaresList.elementAt(i);
                }

                recoveryStarted();
                if (_logger.isLoggable(Level.FINE)) {
                    String msg = localStrings.getString("xaresource.recovering",
                        new Object[]{"Recovering {0} XA resources...", String.valueOf(size)});

                    _logger.log(Level.FINE, msg);
                }
                txMgr.recover(xaresArray);
                recoveryCompleted();

                closeAllResources(resourcesToHandler);
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "xaresource.recover_error", ex);
            }
        }
    }

    /**
     * notifies the listeners that recovery has started
     */
    private void recoveryStarted() {
        Set<RecoveryResourceListener> listeners =
                recoveryListenersRegistry.getListeners();

        for (RecoveryResourceListener rrl : listeners) {
            rrl.recoveryStarted();
        }
    }

    /**
     * notifies the listeners that recovery has completed
     */
    private void recoveryCompleted() {
        Set<RecoveryResourceListener> listeners =
                recoveryListenersRegistry.getListeners();

        for (RecoveryResourceListener rrl : listeners) {
            rrl.recoveryCompleted();
        }
    }

    /**
     * to enable lazy recovery, setting lazy to "true" will
     *
     * @param lazy boolean
     */
    public void setLazyRecovery(boolean lazy) {
        lazyRecovery = lazy;
    }

    /**
     * to recover xa resources
     */
    public void recoverXAResources() {
        recoverXAResources(!lazyRecovery);
    }

    private void configure() {
        if (configured) {
            return;
        }

        recoveryResourceHandlers = habitat.getAllByContract(RecoveryResourceHandler.class);
        txMgr = habitat.getByContract(JavaEETransactionManager.class);
        recoveryListenersRegistry = habitat.getComponent(RecoveryResourceRegistry.class);
        if (recoveryListenersRegistry == null) throw new IllegalStateException();

        configured = true;
    }
}
