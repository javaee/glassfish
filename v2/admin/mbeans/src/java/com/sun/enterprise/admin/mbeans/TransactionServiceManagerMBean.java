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

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.AdminService;
 
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
//import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import java.util.ArrayList;
import java.util.logging.Level; 
import java.util.logging.Logger;

/**
 * object name for this mbean: 
 * <domainName>:type=transaction-service-manager,category=config
 * Based on the target calls the appropriate mbean
 *
 * @author sridatta
 *
 */
public class TransactionServiceManagerMBean extends BaseConfigMBean
{
    private static final TargetType[] VALID_TARGET_TYPES = new TargetType[] {
        TargetType.UNCLUSTERED_SERVER, TargetType.DAS, TargetType.SERVER};
        
    public TransactionServiceManagerMBean()
    {
        super();
    }	
    
	///////////////////////////////////////////////////////////////////////////
      public void freeze(String serverName) throws Exception {
          finest("freeze called for target: " + serverName);
          validateServerName(serverName);
          try {
                  getMBS().invoke(getTransactionServiceObjectName(serverName), "freeze", null, null);
          } catch(javax.management.InstanceNotFoundException  e) {
                 throw new MBeanException(e, _strMgr.getString("admin.mbeans.server_not_running", new Object[] {serverName})); 
          }
      }
      
      public void unfreeze(String serverName) throws Exception {
          finest("unfreeze called for target: " + serverName);
          validateServerName(serverName);
          try {
              getMBS().invoke(getTransactionServiceObjectName(serverName), "unfreeze", null, null);
          } catch(javax.management.InstanceNotFoundException  e) {
                 throw new MBeanException(e, _strMgr.getString("admin.mbeans.server_not_running", new Object[] {serverName})); 
          }
      }
      public void rollback(String[] txids, String serverName)
                            throws Exception {
          finest("rollback called for target: " + serverName);
          validateServerName(serverName);
          try {
              getMBS().invoke(getTransactionServiceObjectName(serverName), 
                        "rollback", 
                        new Object[] {txids},
                        new String[] {"[Ljava.lang.String;"});
          } catch(javax.management.InstanceNotFoundException  e) {
                 throw new MBeanException(e, _strMgr.getString("admin.mbeans.server_not_running", new Object[] {serverName})); 
          }
      }
      
      
      ///////////////////////////////////////////////////////////////////////////
     
      private MBeanServer getMBS() {
         return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
      }      
      
      private ObjectName getTransactionServiceObjectName(String server) 
                                                throws Exception {
          return new ObjectName(
                getDomainName() + 
                ":type=TransactionService,J2EEServer=" 
                + server 
                + ",category=runtime"); 
      }
     
   private void validateServerName(String server) throws Exception {
       finest("validating the target " + server);
         final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_TARGET_TYPES,  
                server, getConfigContext());
         finest("Target is valid: " + server);
   }
   
   //We must subclass this method, because the base case seems to return a null config context if
    //the mbean does not have a corresponding xpath (or something). The properties MBean seems
    //to have this problem.
    protected ConfigContext getConfigContext()
    {
        ConfigContext result = super.getConfigContext();
        if (result == null) {
            result = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        }
        return result;
    }

   private void finest(String  s) {
       _logger.finest("TransactionServiceManagerMBean: " + s); 
   }
     
	///////////////////////////////////////////////////////////////////////////
	
	private static final	StringManager	_strMgr = 
               StringManager.getManager(TransactionServiceManagerMBean.class);
       public static final Logger _logger = Logger.getLogger(AdminConstants.kLoggerName);

	///////////////////////////////////////////////////////////////////////////
                
}
