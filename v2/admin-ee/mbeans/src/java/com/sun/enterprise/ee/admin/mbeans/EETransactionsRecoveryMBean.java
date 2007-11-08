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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.tx.TransactionsRecoveryEvent;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;  

import com.sun.enterprise.admin.config.MBeanConfigException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level; 

/**
 * object name for this mbean: <domainName>:type=transactions-recovery,category=config
 * TransactionsRecoveryMBean sends request to transactions recovery
 *
 * @author alexkrav
 *
 */

public class EETransactionsRecoveryMBean extends EEBaseConfigMBean
{
   
	public EETransactionsRecoveryMBean()
	{
	    super();
	}		
    

	///////////////////////////////////////////////////////////////////////////
    /**
     * Recovers transaction for given server instance
     *
     *   1) if the recovery_server_name (the server instance to be recovered) is running, and 
     * destination is null ot it equals recovery_server_name;
     *   2) if the recovery_server_name is running and the different --destination is specified, 
     * then its an error.
     *   3) if the recover_server_name is running and the --txlogdir is specified, then error
     *   4) if the recovery_server_name is not running, and the destination_server_name 
     * (instance which will recover the transactions for recovery_server) is not running 
     * (or if it doesnt exist), then its an error
     *   5) if the recovery_server_name is not running, and the --txlogdir is null or 
     * if it contains a path which cannot be resolved, then its an error.
     *   6) If recovery_server_name is not running and destination is not specified 
     * then it's an error.
     *
     * @param serverToRecover   - name of the server instance to be recovered
     * @param destinationServer - server name to run the recovery process
     * @param transactionLogDir - path to transaction log files
     *
     * @throws MBeanConfigException
     *
     */
    public void recoverTransactions(String serverToRecover, String destinationServer, 
                    String transactionLogDir) throws MBeanConfigException
    {
        
        fine("TrasactionsRecoverytMBean.recoveryTransactions for server: "+serverToRecover+
             " destinationServer"+destinationServer+" transactionLogDir"+transactionLogDir);
        

        if(serverToRecover==null)
           throw new MBeanConfigException(_strMgr.getString("tx.noServerToRecover"));                 

        if(!isServer(serverToRecover))
            throw new MBeanConfigException(_strMgr.getString("tx.ServerBeRecoveredIsNotKnown", serverToRecover));                
        if(isServerRunning(serverToRecover))
        {
            if(destinationServer!=null && !serverToRecover.equals(destinationServer))
                //server running + destination not null & not equal
                throw new MBeanConfigException(_strMgr.getString("tx.runningServerBeRecoveredFromAnotherServer", serverToRecover, destinationServer));                

            if(transactionLogDir!=null)
                //server running + logDir notnot
                throw new MBeanConfigException(_strMgr.getString("tx.logDirShouldNotBeSpecifiedForSelfRecovering", serverToRecover));
        }
        else if(destinationServer==null)
        {
              // server not running + no destination
              throw new MBeanConfigException(_strMgr.getString("tx.noDestinationServer", serverToRecover));

        }
        else if(!isServerRunning(destinationServer))
        {
             // server not running + destination not running
             throw new MBeanConfigException(_strMgr.getString("tx.destinationServerIsNotAlive", serverToRecover));
        }
        else if(transactionLogDir==null)
        {
             // server not running + destination not running
             throw new MBeanConfigException(_strMgr.getString("tx.logDirNotSpecified", serverToRecover));
        }
            
        if(destinationServer!=null && !isServer(destinationServer))
            throw new MBeanConfigException(_strMgr.getString("tx.DestinationServerIsNotKnown", destinationServer));                

        //here we are only if parameters consistent
        if(destinationServer==null)
            destinationServer = serverToRecover;

    	fine("TrasactionsRecoverytMBean.recoveryTransactions: Sending Notification to server" + destinationServer);
	 
        sendTransactionsRecoveryEvent(destinationServer, serverToRecover, transactionLogDir);
        
    }
	
    ///////////////////////////////////////////////////////////////////////////

    private boolean isServer(String target) {
        try {
            return ServerHelper.isAServer(getConfigContext(), target);
        } catch(Exception e) {
        }
        return false;
    }

    /**
     * This method will call a utility method instead of coding the logic here. TBD
     */
    private boolean isServerRunning(String server)
    {
        try {
            ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(server);
                Status status = serverMBean.getRuntimeStatus().getStatus();
                if (status.getStatusCode() == Status.kInstanceRunningCode) {
                    return true;
                }
        } catch(Exception e) {
		//return false; //FIXME: what else to do??
        }
        return false;
    }

   
 
   private void sendTransactionsRecoveryEvent(String destinationServer, String serverToRecover, 
                    String transactionLogDir) throws MBeanConfigException
   {
        AdminEvent event = new TransactionsRecoveryEvent(getServerName(), 
                    serverToRecover, 
                    transactionLogDir);
        event.setTargetDestination(destinationServer); 
        AdminEventResult res = forwardEvent(event);
        if(!AdminEventResult.SUCCESS.equals(res.getResultCode()))
        {
            Throwable exc = res.getFirstThrowable();
            if(exc!=null) { 
                throw new MBeanConfigException(_strMgr.getString("tx.exceptionInTargetServer", 
                    exc.getMessage()));
            }
            throw new MBeanConfigException(_strMgr.getString("tx.notSuccessInSendReturn", 
                    res.getResultCode()));
        }
    }
      

     private AdminEventResult forwardEvent(AdminEvent e) {
        AdminEventResult result = null;
        result = AdminEventMulticaster.multicastEvent(e);
        return result;
    }
    
     private void fine(String s) {
         _sLogger.log(Level.FINE, s); 
     }

    private String getServerName() 
    {
        return ApplicationServer.getServerContext().getInstanceName();
    } 
     
	///////////////////////////////////////////////////////////////////////////
	
	private static final	StringManager	_strMgr = 
                StringManager.getManager(EJBTimerManagementMBean.class);
	///////////////////////////////////////////////////////////////////////////
}
