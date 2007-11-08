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

package com.sun.enterprise.ee.server.autotxrecovery.core;

import com.sun.enterprise.ee.cms.core.*;

import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.resource.ResourceInstaller;
import com.sun.enterprise.Switch;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.ejb.spi.distributed.DistributedEJBServiceFactory;
import com.sun.ejb.spi.distributed.DistributedEJBService;

/**
 * This recoveryAction class does the recovery of transactions
 * after a particular instance has been chosen for recovery.
 * @author <a href=mailto:servesh.singh@sun.com>Servesh Singh</a>
  * Date: June 1, 2005
 * @version $Revision: 1.2 $
 */
public class TxnFailureRecoveryActionImpl implements FailureRecoveryAction {
    private Logger logger = LogDomains.getLogger(LogDomains.TRANSACTION_LOGGER);
    private static StringManager sm =
            StringManager.getManager(TxnFailureRecoveryActionImpl.class);
    private static final String TXLOGDIR="TX_LOG_DIR";
    private static final String ISRECOVERABLE = "IS_RECOVERABLE";
    private static final String WAITTIMEBEFORESTARTINGRECOVERY="WAIT_TIME";
    private static final String serviceName="TRANSACTION-RECOVERY-SERVICE";

    public TxnFailureRecoveryActionImpl () {
    }

    /**
     * processes the recovery signal. typically involves getting information
     * from the signal, acquiring the signal and after processing, releasing
     * the signal
     * @param signal
     */
    public void consumeSignal(final Signal signal) {
        String failedInstance ="";
        String component = "";
        GroupHandle gh =null;
        try {
            GroupManagementService gms = GMSFactory.getGMSModule();
            gh = gms.getGroupHandle();
            component =
                    ((FailureRecoverySignal)signal).getComponentName();
            failedInstance = ((FailureRecoverySignal)signal).getFailedMemberToken();
            //Raising the fence to prevent other to come and do the recovery
            //of the same instance
            //gh.raiseFence(component,failedInstance);
            
            //Waiting for 1 minute to ensure that indoubt xids are updated into 
            //the database, otherwise while doing the recovery an instance may not
            //get all the correct indoubt xids.
            int waitTime = 60;
            //if(signal.getMemberDetails() != null)
            waitTime = Integer.parseInt((String)(signal.getMemberDetails().get(WAITTIMEBEFORESTARTINGRECOVERY)));
            if (logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.wait_before_recovery",waitTime));
            try {
                    Thread.sleep(waitTime*1000);
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            
            while(gh.isFenced(serviceName,failedInstance)) {
                if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.waiting_till_fence_lowered"));
                try {
                    Thread.sleep(2*1000);
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
            if(gh.isMemberAlive(failedInstance))// Failed Member is alive, don't do recovery, he must have done self recovery
                return;
            signal.acquire();
            if (logger.isLoggable(Level.INFO))
                    logger.log(Level.INFO,
                    sm.getString("enterprise_autotxrecovery.fence_raised",
                    component,failedInstance));
            //boolean flag = ServerHelper.getConfigForServer(txnInfo.getConfigContext(),failedInstance).getTransactionService().isAutomaticRecovery();
            boolean result = false;
            boolean flag = Boolean.parseBoolean((String)(signal.getMemberDetails().get(ISRECOVERABLE)));
            if(flag) {
                ResourceInstaller resInstaller = Switch.getSwitch().getResourceInstaller();
                //String txLogDir= ServerHelper.getConfigForServer(txnInfo.getConfigContext(),
                //                        failedInstance).getTransactionService().getTxLogDir()+File.separator+"tx";
                String txLogDir=(String)(signal.getMemberDetails().get(TXLOGDIR));
                if (logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.tx_logdir",txLogDir,failedInstance));
                if (logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.starting_recovery",failedInstance));
                result = resInstaller.recoverIncompleteTx(true, txLogDir);
                if (logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,sm.getString("enterprise_autotxrecovery.recovery_completed",failedInstance));   
                if (result) {
                    // If we have successfully recovered transactions for
                    // the failed instance, initiate EJB Timer migration
                    // within this server instance.
                    DistributedEJBService distribEjbService =
                       DistributedEJBServiceFactory.getDistributedEJBService();
                    distribEjbService.migrateTimers(failedInstance);
                } else {
                    throw new IllegalStateException();
                }
            }
            
        }catch(ConfigException ce){
            if (logger.isLoggable(Level.WARNING))
            logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.config_error"),ce);
        }catch(GMSException e) {
            if (logger.isLoggable(Level.WARNING))
            logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.gms_error"),e);
        }catch(Exception e){
            if (logger.isLoggable(Level.WARNING))
            logger.log(Level.WARNING,sm.getString("enterprise_autotxrecovery.recovery_error"),e);
        }finally {
            try { 
                if(!failedInstance.equals(""))  {
                    //gh.lowerFence(component,failedInstance);
                    signal.release();
                    if (logger.isLoggable(Level.INFO))
                    logger.log(Level.INFO,sm.getString("enterprise_autotxrecovery.fence_lowered"));
                }
            }catch(Exception e){}
        }
    }
  
}
