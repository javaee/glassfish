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

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.tx.TransactionsRecoveryEvent;

import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.MBeanConfigException;

import com.sun.enterprise.util.i18n.StringManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level; 

/**
 * object name for this mbean: <domainName>:type=transactions-recovery,category=config
 * TransactionsRecoveryMBean sends request to transactions recovery
 *
 * @author alexkrav
 *
 */
public class TransactionsRecoveryMBean extends BaseConfigMBean
{
   
    public TransactionsRecoveryMBean()
    {
        super();
    }	
    
	///////////////////////////////////////////////////////////////////////////
    /**
     * Recovers transaction for given server instance
     *
     * @throws MBeanConfigException if not SUCCESS result of event handling
     *
     */
    public void recoverTransactions() throws MBeanConfigException
    {
        
        fine("TransactionsRecoverytMBean.recoveryTransactions for server");
	 
        sendTransactionsRecoveryEvent(getServerName(), getServerName(), null);
        
    }
	
    ///////////////////////////////////////////////////////////////////////////

 
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
            Throwable exc = null;
            exc = res.getFirstThrowable();
            if (exc != null) {
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
