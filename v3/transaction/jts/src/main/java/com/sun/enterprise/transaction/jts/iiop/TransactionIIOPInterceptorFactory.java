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

package com.sun.enterprise.transaction.jts.iiop;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.jts.pi.InterceptorImpl;
import com.sun.jts.jta.TransactionManagerImpl;
import com.sun.jts.CosTransactions.Configuration;

import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.admin.config.Property;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import org.omg.CORBA.*;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import com.sun.corba.ee.spi.orbutil.ORBConstants;
import com.sun.corba.ee.spi.orbutil.closure.ClosureFactory;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt;
import com.sun.corba.ee.impl.logging.POASystemException;
import com.sun.corba.ee.impl.txpoa.TSIdentificationImpl;

/**
 *
 * @author mvatkina
 */
@Service(name="TransactionIIOPInterceptorFactory")
public class TransactionIIOPInterceptorFactory implements IIOPInterceptorFactory{

    // The log message bundle is in com.sun.jts package
    private static Logger _logger =
            LogDomains.getLogger(InterceptorImpl.class, LogDomains.TRANSACTION_LOGGER);

    private static StringManager localStrings =
            StringManager.getManager(InterceptorImpl.class);

    private static final String JTS_XA_SERVER_NAME = "com.sun.jts.xa-servername";
    private static final String J2EE_SERVER_ID_PROP = "com.sun.enterprise.J2EEServerId";
    private static final String JTS_SERVER_ID = "com.sun.jts.persistentServerId";
    private static final int DEFAULT_SERVER_ID = 100 ;

    private static String jtsClassName = "com.sun.jts.CosTransactions.DefaultTransactionService";

    private static Properties jtsProperties = new Properties();
    private static TSIdentificationImpl tsIdent = new TSIdentificationImpl();
    private static boolean txServiceInitialized = false;
    private InterceptorImpl interceptor = null;

    @Inject private Habitat habitat;
    @Inject private ServerContext ctx;
    @Inject private ProcessEnvironment processEnv;

    public ClientRequestInterceptor createClientRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (!txServiceInitialized) {
            createInterceptor(info, codec);
        }

        return interceptor;
    }

    public ServerRequestInterceptor createServerRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (!txServiceInitialized) {
            createInterceptor(info, codec);
        }

        return interceptor;
    }

    private void createInterceptor(ORBInitInfo info, Codec codec) {
        if( processEnv.getProcessType().isServer()) {
            try {
                System.setProperty(
                        InterceptorImpl.CLIENT_POLICY_CHECKING, String.valueOf(false));
            } catch ( Exception ex ) {
                _logger.log(Level.WARNING,"iiop.readproperty_exception",ex);
            }

            initJTSProperties(true);
        }

        try {
            // register JTS interceptors
            // first get hold of PICurrent to allocate a slot for JTS service.
            Current pic = (Current)info.resolve_initial_references("PICurrent");

            // allocate a PICurrent slotId for the transaction service.
            int[] slotIds = new int[2];
            slotIds[0] = info.allocate_slot_id();
            slotIds[1] = info.allocate_slot_id();

            interceptor = new InterceptorImpl(pic, codec, slotIds, null);
            // Get the ORB instance on which this interceptor is being
            // initialized
            com.sun.corba.ee.spi.orb.ORB theORB = ((ORBInitInfoExt)info).getORB();

            // Set ORB and TSIdentification: needed for app clients,
            // standalone clients.
            interceptor.setOrb(theORB);
            try {
                Class theJTSClass = Class.forName(jtsClassName);

                if (theJTSClass != null) {
                        try {
                        com.sun.corba.ee.spi.costransactions.TransactionService jts = 
                                (com.sun.corba.ee.spi.costransactions.TransactionService)theJTSClass.newInstance();
                        jts.identify_ORB(theORB, tsIdent, jtsProperties ) ;
                        interceptor.setTSIdentification(tsIdent);

                        // V2-XXX should jts.get_current() be called everytime
                        // resolve_initial_references is called ??
                        org.omg.CosTransactions.Current transactionCurrent =
                                jts.get_current();

                        theORB.getLocalResolver().register(
                                ORBConstants.TRANSACTION_CURRENT_NAME,
                                ClosureFactory.makeConstant(transactionCurrent));

                        // the JTS PI use this to call the proprietary hooks
                        theORB.getLocalResolver().register(
                                "TSIdentification", ClosureFactory.makeConstant(tsIdent));
                        txServiceInitialized = true;
                    } catch (Exception ex) {
                        throw new INITIALIZE("JTS Exception: " + ex,
                                POASystemException.JTS_INIT_ERROR,
                                CompletionStatus.COMPLETED_MAYBE);
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                _logger.log(Level.SEVERE,"iiop.inittransactionservice_exception",cnfe);
            }


            // Add IOR Interceptor only for OTS tagged components
            TxIORInterceptor iorInterceptor = new TxIORInterceptor(codec, habitat);
            info.add_ior_interceptor(iorInterceptor);

        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE,"Exception registering JTS interceptors",e);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    private void initJTSProperties(boolean lateRegistration) {
        if (habitat != null) {
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
                int jtsServerId = habitat.getComponent(GlassFishORBHelper.class).getORBInitialPort();
                if (jtsServerId == 0) {
                    // XXX Can this ever happen?
                    jtsServerId = DEFAULT_SERVER_ID; // default value
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
    
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                                "++++ Server id: "
                                + jtsProperties.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY));
                }
    
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
            Configuration.setProperties(jtsProperties);
        }
    }

    private boolean isValueSet(String value) {
        return (value != null && !value.equals("") && !value.equals(" "));
    }
}
