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

package com.sun.enterprise.ee.server.autotxrecovery;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.io.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.server.autotxrecovery.core.*;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.autotxrecovery.TransactionRecovery;

import javax.management.*;

/**
 *
 * @author <a href="mailto:servesh.singh@sun.com>Servesh Singh</a>"
 * Date: Dec 3, 2004
 * @version $Revision: 1.1.1.1 $
 */
/**
 * This will initialize the Distributed Data Structure with the member info such as
 * log location and shared log flag. After the initialization, it will wait till 
 * fence gets lowered to make sure other instance has finished his recovery.
 */
public class EEAutoTransactionRecoveryServiceImpl implements TransactionRecovery{
    Logger _logger = LogDomains.getLogger(LogDomains.TRANSACTION_LOGGER);
    private ConfigContext configContext_ =null;
    private boolean startRecovery;
    private boolean delegatedRecovery;
    private String instanceName;
    private String clusterName;
    private String waitTime="60";
    private GroupManagementService gms = null;
    private static StringManager sm =
            StringManager.getManager(EEAutoTransactionRecoveryServiceImpl.class);
    private static final String serviceName="TRANSACTION-RECOVERY-SERVICE";
    private static final String TXLOGDIR="TX-LOG-DIR";
    private static final String DELEGATEDRECOVERY = "delegated-recovery";
    private static final String WAITTIMEBEFORERECOVERY = "wait-time-before-recovery-insec";
    private static final String TXLOGLOCATION = "TX_LOG_DIR";
    private static final String ISRECOVERABLE="IS_RECOVERABLE";
    private static final String WAITTIMEBEFORESTARTINGRECOVERY="WAIT_TIME";
    
    private String nfstxLog="";
    private String txLogDir="";

    /**
     * DataStructures are initialized first. Recovery
     * of transaction starts immediatley after the initialization.
     */
    public void start() {
        init(ApplicationServer.getServerContext());
    }
    
   /**
     * It sets the tx-log-dir and isShared flag into DSC, so that
     * other instance can access while doing recovery.
     * @param context ServerContext the server runtime context.
     */
    private void init(ServerContext context) {
        try {
        configContext_ = context.getConfigContext();
        instanceName = context.getInstanceName();
        if(!ServerHelper.isServerClustered(configContext_,instanceName))
            return;
        clusterName = ((Cluster)(ClusterHelper.getClusterForInstance(context.getConfigContext(), 
                        instanceName))).getName(); ;
        if(!GMSFactory.isGMSEnabled(clusterName))
            return;                
        startRecovery = ServerHelper.getConfigForServer(configContext_,instanceName).getTransactionService().isAutomaticRecovery();
        
        TransactionService txnService = null;
        txnService = ServerBeansFactory.getTransactionServiceBean(configContext_);
        //txLogDir = txnService.getTxLogDir();
        txLogDir = getTxLogDir();
        ElementProperty[] eprops = txnService.getElementProperty();
        for (int index = 0; index < eprops.length; index++) {
            if (DELEGATEDRECOVERY.equals(eprops[index].getName())) {
                delegatedRecovery = Boolean.parseBoolean(eprops[index].getValue());
            }
            if (WAITTIMEBEFORERECOVERY.equals(eprops[index].getName())) {
                try{
                    waitTime = eprops[index].getValue();
                    Integer.parseInt(waitTime);
                }catch(Exception e) {
                    waitTime = "60";
                    if (_logger.isLoggable(Level.WARNING))
                        _logger.log(Level.WARNING,"Invalid wait time for the recovery",e);
                }
            }
        }
        
        SystemProperty[] sysArray = ServerHelper.getServerByName(configContext_,
                                        instanceName).getSystemProperty();
        for(int i =0; i<sysArray.length;i++) {
            //System.out.println("sys index "+i+
            //        " name=="+sysArray[i].getName()+" value=="+sysArray[i].getValue());
            if(sysArray[i].getName().equals(TXLOGDIR)) {
                nfstxLog = sysArray[i].getValue();
            }
        }
        
        //If log is shared and user has specified the NFS log location
        //through system property, then this log location will be used
        //for recovery.
        if(delegatedRecovery) {
            if(!nfstxLog.equals("")) {
                txLogDir = nfstxLog;
            }
        }
        
        //txLogDir=txLogDir+File.separator+"tx";
        boolean isRecoverable = delegatedRecovery && startRecovery;
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.tx_log_info",txLogDir,delegatedRecovery));
                      
        gms = (GroupManagementService) GMSFactory.getGMSModule(clusterName);
        if (_logger.isLoggable(Level.INFO))
            _logger.log(Level.INFO,sm.getString("enterprise_autotxrecovery.registering_updating_member_details"));
        gms.addActionFactory(serviceName, new TxnFailureRecoveryActionFactoryImpl());
        gms.updateMemberDetails(instanceName,TXLOGLOCATION,txLogDir);
        gms.updateMemberDetails(instanceName,ISRECOVERABLE,isRecoverable+"");
        gms.updateMemberDetails(instanceName,WAITTIMEBEFORESTARTINGRECOVERY,waitTime);
                               
        checkIfFenceLowered();
        }catch(ConfigException ce){
            if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.config_excep"),ce);
        }catch(GMSException e){
            if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.gms_excep"),e);        
        }
	}
    
    /**
     * Raise the fence so that no other instance can
     * start the recovery at the same time.
     */
    public void raiseFence() {
        try{
            if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"Raising the Fence");
            if(!ServerHelper.isServerClustered(configContext_,instanceName))
            return;
            if(gms == null)
            return;
            final GroupHandle gh = gms.getGroupHandle();
            gh.raiseFence(serviceName,instanceName);
            if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"Raised the Fence");
        }catch(GMSException e) {
            if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.Error_raising_fence"),e);
        }catch(Exception ex) {
           if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.Error_raising_fence"),ex); 
        }
    }
    /**
     * Lower the fence
     */
    public void lowerFence() {
        try{
            if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"Lowering the Fence");
            if(!ServerHelper.isServerClustered(configContext_,instanceName))
            return;
            if(gms == null)
            return;
            final GroupHandle gh = gms.getGroupHandle();
            /*try{
                Thread.sleep(10*1000);
            }catch(Exception ex) {
                
            } */
            gh.lowerFence(serviceName,instanceName);
            if(gh.isFenced(serviceName,instanceName)) {
                if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"There is a fence");
            } else {
                if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"There is no fence");
            }
        }catch(GMSException e) {
            if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.Error_lowering_fence"),e);
        }catch(Exception ex) {
           if (_logger.isLoggable(Level.WARNING))
            _logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.Error_lowering_fence"),ex); 
        }
    }
    
    /**
     * Wait till fence gets lowered i.e let other instance finish
     * your recovery.
     */
    private void checkIfFenceLowered() {
        final GroupHandle gh = gms.getGroupHandle();
        while(gh.isFenced(serviceName,instanceName)) {
            if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.waiting_till_fence_lowered"));
            try {
                Thread.sleep(5*1000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
    
    /**
     * Read the tx-log-dir from domain.xml.
     * log-root will be prefixed if tx-log-dir is relative path.
     */
    private String getTxLogDir() throws ConfigException{
        TransactionService txnService = ServerBeansFactory.
                                        getTransactionServiceBean(configContext_);
        String logdir=txnService.getTxLogDir();
        if(logdir==null){
            Domain svr = null;
	    svr = ServerBeansFactory.getDomainBean(configContext_);
	    logdir = svr.getLogRoot();
	    if(logdir == null){
                logdir = FileUtil.getAbsolutePath(".."+File.separator+"logs");
	    }
	} else if( ! (new File(logdir)).isAbsolute()) {
            //_logger.log(Level.WARNING,"enterprise.relative_tx_log_dir" , logdir);
	    Domain svr = null;
	    svr = ServerBeansFactory.getDomainBean(configContext_);
	    String logroot=svr.getLogRoot();
	    if(logroot != null){
		logdir = logroot + File.separator + logdir;
	    } else {
		logdir = FileUtil.getAbsolutePath(".."+File.separator+"logs"+File.separator+logdir);
	    }
	}
	logdir += File.separator + instanceName + File.separator+ "tx";
        return logdir;
    }
    
}
