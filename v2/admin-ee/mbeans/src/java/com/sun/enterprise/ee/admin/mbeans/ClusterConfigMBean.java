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


import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import javax.management.ObjectName;
import javax.management.MBeanException;


public class ClusterConfigMBean extends ServerAndClusterBaseMBean implements com.sun.enterprise.ee.admin.mbeanapi.ClusterConfigMBean
{               
    private static final StringManager _strMgr = 
        StringManager.getManager(ClusterConfigMBean.class);

    private static Logger _logger = null;            
    
    //The logger is used to log to server log file
    protected static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
    
    private static ExceptionHandler _handler = null;
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }
    
    public ClusterConfigMBean() {
        super();
    }      
    
    protected String getLogMessageId()
    {
        return "eeadmin.ClusterConfigMBean.Exception";
    }         
        
    public boolean isStandAlone() throws InstanceException
    {
        String clusterName = getName();
        try {            
            return ClusterHelper.isClusterStandAlone(getConfigContext(), 
                clusterName);
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), clusterName); //not JMX compliant
        }
    }       
        
    public RuntimeStatusList getRuntimeStatus() throws InstanceException
    {              
        return getClustersConfigBean().getRuntimeStatus(
            getName());
    }       
    
    public boolean anyRunning() throws InstanceException
    {              
        return getClustersConfigBean().getRuntimeStatus(
            getName()).anyRunning();
    }       
    
    public void clearRuntimeStatus() throws InstanceException
    {               
        getClustersConfigBean().clearRuntimeStatus(
            getName());        
    }
                
    public RuntimeStatusList start() throws InstanceException
    {           
        return getClustersConfigBean().startCluster(
            getName());        
    }        
        
    public RuntimeStatusList stop() throws InstanceException
    {   
        return getClustersConfigBean().stopCluster(
            getName());
    }      
    
    public void delete() throws InstanceException
    {   
        getClustersConfigBean().deleteCluster(
            getName());
    }         
    
    public String[] listServerInstancesAsString(boolean andStatus) throws InstanceException
    {
        return getServersConfigBean().listServerInstancesAsString(getName(), andStatus);
    }
    
    public ObjectName[] listServerInstances() throws InstanceException, MBeanException
    {
        return toServerONArray(listServerInstancesAsString(false));
    }
}
