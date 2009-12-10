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

package com.sun.jts.jta;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.RecoveryManager;

import org.glassfish.internal.api.ServerContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author mvatkina
 */
public class TransactionServiceProperties {

    private static Logger _logger =
            LogDomains.getLogger(TransactionServiceProperties.class, LogDomains.TRANSACTION_LOGGER);

    private static StringManager localStrings =
            StringManager.getManager(TransactionServiceProperties.class);

    private static final String JTS_XA_SERVER_NAME = "com.sun.jts.xa-servername";
    private static final String J2EE_SERVER_ID_PROP = "com.sun.enterprise.J2EEServerId";
    private static final String JTS_SERVER_ID = "com.sun.jts.persistentServerId";
    private static final int DEFAULT_SERVER_ID = 100 ;

    public static Properties getJTSProperties (Habitat habitat, boolean isORBAvailable) {
        Properties jtsProperties = new Properties();
        if (habitat != null) {
            ProcessEnvironment processEnv = habitat.getComponent(ProcessEnvironment.class);
            if( processEnv.getProcessType().isServer()) {
                TransactionService txnService = habitat.getComponent(TransactionService.class);

                if (txnService != null) {
                    jtsProperties.put(Configuration.HEURISTIC_DIRECTION, txnService.getHeuristicDecision());
                    jtsProperties.put(Configuration.KEYPOINT_COUNT, txnService.getKeypointInterval());

                    String automaticRecovery = txnService.getAutomaticRecovery();
                    boolean isAutomaticRecovery = 
                            (isValueSet(automaticRecovery) && "true".equals(automaticRecovery));
                    if (isAutomaticRecovery) {
                        _logger.log(Level.FINE,"Recoverable J2EE Server");
                        jtsProperties.put(Configuration.MANUAL_RECOVERY, "true");
                    }
    
                    boolean disable_distributed_transaction_logging = false;
                    String dbLoggingResource = null;
                    for (Property prop : txnService.getProperty()) {
                        String name = prop.getName();
                        String value = prop.getValue();

                        if (name.equals("disable-distributed-transaction-logging")) {
                            if (isValueSet(value) && "true".equals(value)) {
                                disable_distributed_transaction_logging = true;
                            } 
        
                        } else if (name.equals("xaresource-txn-timeout")) {
                            if (isValueSet(value)) {
                                _logger.log(Level.FINE,"XAResource transaction timeout is"+value);
                                TransactionManagerImpl.setXAResourceTimeOut(Integer.parseInt(value));
                            }
        
                        } else if (name.equals("db-logging-resource")) {
                            dbLoggingResource = value;
                            _logger.log(Level.FINE,
                                    "Transaction DB Logging Resource Name" + dbLoggingResource);
                            if (dbLoggingResource != null 
                                    && (" ".equals(dbLoggingResource) || "".equals(dbLoggingResource))) {
                                dbLoggingResource = "jdbc/TxnDS";
                            }
        
                        } else if (name.equals("xa-servername")) {
                            if (isValueSet(value)) {
                                jtsProperties.put(JTS_XA_SERVER_NAME, value);
                            }
        
                        } else if (name.equals("pending-txn-cleanup-interval")) {
                            if (isValueSet(value)) {
                                int interval = Integer.parseInt(value);
                                new RecoveryHelperThread(habitat, interval).start();
                                if (_logger.isLoggable(Level.FINE))
                                   _logger.log(Level.FINE,"Asynchronous thread for incomplete "
                                           + "tx is enabled with interval " + interval);
                            }
                        }
                    }

                    if (dbLoggingResource != null) {
                        disable_distributed_transaction_logging = true;
                        jtsProperties.put(Configuration.DB_LOG_RESOURCE, dbLoggingResource);
                    }
    
                    /**
                       JTS_SERVER_ID needs to be unique for each for server instance.
                       This will be used as recovery identifier along with the hostname
                       for example: if the hostname is 'tulsa' and iiop-listener-port is 3700
                       recovery identifier will be tulsa,P3700
                    **/
                    int jtsServerId = DEFAULT_SERVER_ID; // default value

                    if (isORBAvailable) {
                        jtsServerId = habitat.getComponent(GlassFishORBHelper.class).getORBInitialPort();
                        if (jtsServerId == 0) {
                            // XXX Can this ever happen?
                            jtsServerId = DEFAULT_SERVER_ID; // default value
                        }
                    }
                    jtsProperties.put(JTS_SERVER_ID, String.valueOf(jtsServerId));
    
                    /* ServerId is an J2SE persistent server activation
                       API.  ServerId is scoped at the ORBD.  Since
                       There is no ORBD present in J2EE the value of
                       ServerId is meaningless - except it must have
                       SOME value if persistent POAs are created. 
                     */
        
                    // For clusters - all servers in the cluster MUST
                    // have the same ServerId so when failover happens
                    // and requests are delivered to a new server, the
                    // ServerId in the request will match the new server.
        
                    String serverId = String.valueOf(DEFAULT_SERVER_ID);
                    System.setProperty(J2EE_SERVER_ID_PROP, serverId);
    
                    /**
                     * if the auto recovery is true, always transaction logs will be written irrespective of
                     * disable_distributed_transaction_logging.
                     * if the auto recovery is false, then disable_distributed_transaction_logging will be used
                     * to write transaction logs are not.If disable_distributed_transaction_logging is set to
                     * false(by default false) logs will be written, set to true logs won't be written.
                     **/
                    if (!isAutomaticRecovery && disable_distributed_transaction_logging) {
                        Configuration.disableFileLogging();
                    }

                    ServerContext ctx = habitat.getByContract(ServerContext.class);
                    String instanceName = ctx.getInstanceName();
                    if (dbLoggingResource == null) {
                        String logdir = txnService.getTxLogDir();
                        if(logdir == null){
                            Domain domain = habitat.getComponent(Domain.class);
                            logdir = domain.getLogRoot();
                            if(logdir == null){
                                // logdir = FileUtil.getAbsolutePath(".." + File.separator + "logs");
                                logdir = ".." + File.separator + "logs";
                            }
                        } else if( ! (new File(logdir)).isAbsolute()) {
                            if(_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, 
                                    "Relative pathname specified for transaction log directory : " 
                                    + logdir);
                            }
                            Domain domain = habitat.getComponent(Domain.class);;
                            String logroot = domain.getLogRoot();
                            if(logroot != null){
                                logdir = logroot + File.separator + logdir;
                            } else {
                                // logdir = FileUtil.getAbsolutePath(".." + File.separator + "logs"
                                // + File.separator + logdir);
                                logdir = ".." + File.separator + "logs" + File.separator + logdir;
                            }
                        }
                        logdir += File.separator + instanceName + File.separator + "tx";
    
                        _logger.log(Level.FINE,"JTS log directory: " + logdir);
                        _logger.log(Level.FINE,"JTS Server id " + jtsServerId);

                        (new File(logdir)).mkdirs();
                        jtsProperties.put(Configuration.LOG_DIRECTORY, logdir);
                    }
                    jtsProperties.put(Configuration.COMMIT_RETRY, txnService.getRetryTimeoutInSeconds());
                    jtsProperties.put(Configuration.INSTANCE_NAME, instanceName);

                }
            }
        }

        return jtsProperties;
    }

    private static boolean isValueSet(String value) {
        return (value != null && !value.equals("") && !value.equals(" "));
    }

    private static class RecoveryHelperThread extends Thread {
        private int interval;
        private ResourceRecoveryManager recoveryManager;

        RecoveryHelperThread(Habitat habitat, int interval) {
            setName("Recovery Helper Thread");
            setDaemon(true);
            this.interval = interval;
            recoveryManager = habitat.getByContract(ResourceRecoveryManager.class);
        }

        public void run() {
            int prevSize = 0;
            try {
                while(true) {
                    Thread.sleep(interval*1000L);
                    if (!RecoveryManager.isIncompleteTxRecoveryRequired()) {
                        if (_logger.isLoggable(Level.FINE))
                            _logger.log(Level.FINE, "Incomplete transaction recovery is "
                                    + "not requeired,  waiting for the next interval");
                        continue;
                    }
                    if (RecoveryManager.sizeOfInCompleTx() <= prevSize) {
                        if (_logger.isLoggable(Level.FINE))
                            _logger.log(Level.FINE, "Incomplete transaction recovery is "
                                    + "not required,  waiting for the next interval SIZE");
                       continue;
                    }
                    prevSize = RecoveryManager.sizeOfInCompleTx();
                    recoveryManager.recoverIncompleteTx(false, null);
                }
            } catch (Exception ex) {
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE, " Exception occurred in recoverInCompleteTx ");
            }
        }
    }
}
