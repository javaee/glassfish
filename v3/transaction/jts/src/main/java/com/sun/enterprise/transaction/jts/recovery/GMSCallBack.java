/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.transaction.jts.recovery;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.jts.jta.TransactionServiceProperties;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.DefaultTransactionService;

import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;

import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;
import com.sun.enterprise.transaction.spi.RecoveryEventListener;

import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.FailureRecoverySignal;
import com.sun.enterprise.ee.cms.core.Signal;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import org.jvnet.hk2.component.Habitat;

public class GMSCallBack implements CallBack {

    private static final String component = "TRANSACTION-RECOVERY-SERVICE";
    private static final String TXLOGLOCATION = "TX_LOG_DIR";

    // Use a class from com.sun.jts subpackage
    static Logger _logger = LogDomains.getLogger(TransactionServiceProperties.class, LogDomains.TRANSACTION_LOGGER);
    private RecoveryResourceRegistry recoveryListenersRegistry;
    private ResourceRecoveryManager recoveryManager;
    private Servers servers;

    private int waitTime;

    public GMSCallBack(int waitTime, Habitat habitat) {
        GMSAdapterService gmsAdapterService = habitat.getComponent(GMSAdapterService.class);
        if (gmsAdapterService != null) {
            GMSAdapter gmsAdapter = gmsAdapterService.getGMSAdapter();
            if (gmsAdapter != null) {
                gmsAdapter.registerFailureRecoveryListener(component, this);

                recoveryListenersRegistry = habitat.getComponent(RecoveryResourceRegistry.class);
                recoveryManager = habitat.getComponent(ResourceRecoveryManager.class);
                servers = habitat.getComponent(Servers.class);

                this.waitTime = waitTime;

                Properties props = TransactionServiceProperties.getJTSProperties(habitat, false);
                String instanceName = props.getProperty(Configuration.INSTANCE_NAME);
                String logdir = props.getProperty(Configuration.LOG_DIRECTORY);
                if (Configuration.getORB() == null) {
                    // IIOP listeners are not setup yet,
                    // Create recoveryfile file so that automatic recovery will find it even 
                    // if no XA transaction is envolved.
                    DefaultTransactionService.setServerName(props);
                }

                GroupManagementService gms = gmsAdapter.getModule();
                try {
                    gms.updateMemberDetails(instanceName, TXLOGLOCATION, logdir);
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "jts.error_updating_gms", e);
                } 
            }
        }
    }

    @Override
    public void processNotification(Signal signal) {
        if (signal instanceof FailureRecoverySignal) {
            if (_logger.isLoggable(Level.INFO)) {
                _logger.log(Level.INFO, "[GMSCallBack] failure recovery signal: " + signal);
            }

            // Waiting for 1 minute (or the user set value) to ensure that indoubt xids are updated into
            // the database, otherwise while doing the recovery an instance may not
            // get all the correct indoubt xids.
            try {
                Thread.sleep(waitTime*1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            String instance = signal.getMemberToken();
            if (isInstanceRunning(instance)) {
                return;
            }

            String logdir = null;
            Map<Serializable, Serializable> failedMemberDetails = signal.getMemberDetails();
            if (failedMemberDetails != null) {
                  logdir = (String)failedMemberDetails.get(TXLOGLOCATION);
            }
            if (logdir == null) {
                    // Could happen if instance fails BEFORE actually getting this info into distributed state cache.
                    // Could also be a gms distributed state cache bug.
                    _logger.log(Level.WARNING, "jts.error_getting_member_details", instance);
                    return;
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Transaction log directory for " + instance + " is " + logdir);
                _logger.log(Level.FINE, "Starting transaction recovery of " + instance);
            }

            Set<RecoveryEventListener> listeners = recoveryListenersRegistry.getEventListeners();
            for (RecoveryEventListener erl : listeners) {
                try {
                    erl.beforeRecovery(instance);
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "", e);
                    _logger.log(Level.WARNING, "jts.before_recovery_excep", erl);
                }
            }

            // TODO
            _logger.log(Level.WARNING, "[GMSCallBack] Automatic delegated transaction recovery is not fully supported");

            boolean result = false;
            try {
                result = recoveryManager.recoverIncompleteTx(true, logdir);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Transaction recovery of " + instance + " is completed");
                }
            } catch (Exception e) {
                _logger.log(Level.WARNING, "jts.recovery_error", e);
            }

            for (RecoveryEventListener erl : listeners) {
                try {
                    erl.afterRecovery(result, instance);
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "", e);
                    _logger.log(Level.WARNING, "jts.after_recovery_excep", erl);
                }
            }
        } else {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "[GMSCallBack] ignoring signal: " + signal);
            }
        }
    }

    private boolean isInstanceRunning(String instance) {
        boolean rs = false;
        for(Server server : servers.getServer()) {
            if(instance.equals(server.getName())) {
                rs = server.isRunning();
                break;
            }
        }

        return rs;
    }
}
