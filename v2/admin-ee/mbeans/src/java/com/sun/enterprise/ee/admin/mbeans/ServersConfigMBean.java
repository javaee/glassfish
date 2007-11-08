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

import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;

import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;

import javax.management.ObjectName;
import javax.management.MBeanException;

import java.util.logging.Logger;
import java.util.logging.Level;        
import java.util.Properties;

import com.sun.enterprise.ee.admin.PortReplacedException;

import com.sun.enterprise.ManagementObjectManager;
import com.sun.enterprise.admin.mbeans.DomainStatusHelper;
import com.sun.appserv.management.j2ee.StateManageable;

//ISSUE: Do we really want to throws an InstanceException here as this will clients 
//using this mbean to have our runtime; however we seem to be throwing our own
//exceptions everywhere else in the mbeans. The problem with MBeanException 
//currently is that it masks the real exception (due to the fact that MBeanHelper
//does some bogus formatting on the exception.

public class ServersConfigMBean extends EEBaseConfigMBean 
    implements IAdminConstants, com.sun.enterprise.ee.admin.mbeanapi.ServersConfigMBean 
{        
    
    private DomainStatusHelper dsh = null;
    private static final StringManager sm = StringManager.getManager(ServersConfigMBean.class);

    public ServersConfigMBean() {
        super();        
	dsh = new DomainStatusHelper();
    }
    
    public void clearRuntimeStatus(String serverName) throws InstanceException
    {        
        getServersConfigBean().clearRuntimeStatus(serverName);
    }
      
    public RuntimeStatus getRuntimeStatus(String serverName) throws InstanceException
    {        
        return getServersConfigBean().getRuntimeStatus(serverName);        
    }              
           
    public ObjectName[] listServerInstances(String targetName) 
        throws InstanceException, MBeanException
    {                
        return toServerONArray(listServerInstancesAsString(targetName, false));        
    }    
    
    /**
     * Lists server instances.
     */
    public String[] listServerInstancesAsString(
        String targetName, boolean andStatus) throws InstanceException
    {
        return getServersConfigBean().listServerInstancesAsString(targetName, andStatus);
    }
    
    public RuntimeStatusList getServerInstanceRuntimeStatus(String targetName) 
        throws InstanceException
    {
        return getServersConfigBean().getServerInstanceRuntimeStatus(targetName);
    }
    
    public String listDASServerInstanceAsString(boolean andStatus) throws InstanceException
    {
        return getServersConfigBean().listDASServerInstanceAsString(andStatus);
    }
    
    public ObjectName listDASServerInstance() throws InstanceException, MBeanException
    {                
        return getServerObjectName(listDASServerInstanceAsString(false));            
    }
        
    public String[] listUnclusteredServerInstancesAsString(boolean andStatus)
        throws InstanceException
    {
        return getServersConfigBean().listUnclusteredServerInstancesAsString(
            andStatus, true);
    }
    
    public ObjectName[] listUnclusteredServerInstances() 
        throws InstanceException, MBeanException
    {
        return toServerONArray(listUnclusteredServerInstancesAsString(false));        
    }    

    public ObjectName[] listUnclusteredServerInstances(boolean excludeDASInstance) 
        throws InstanceException, MBeanException
    {
        return toServerONArray(getServersConfigBean().
            listUnclusteredServerInstancesAsString(false, excludeDASInstance));
    }

    /**
     * Starts the specified server instance. This operation is invoked by the asadmin start-instance
     * command.
     */
    public RuntimeStatus startServerInstance(
        String serverName) throws InstanceException
    {
	try {
		dsh.setstate(serverName, StateManageable.STATE_STARTING);
        	return getServersConfigBean().startServerInstance(serverName);
	} catch (InstanceException ie) {
		dsh.setstate(serverName, StateManageable.STATE_FAILED);
		throw ie;
	}
    }
    
    /**
     * Stops the specified server instance. This operation is invoked by the asadmin stop-instance
     * command.
     */
    public void stopServerInstance(
        String serverName) throws InstanceException
    {
	try {
		dsh.setstate(serverName, StateManageable.STATE_STOPPING);
        	getServersConfigBean().stopServerInstance(serverName);
	} catch (InstanceException ie) {
		dsh.setstate(serverName, StateManageable.STATE_FAILED);
		throw ie;
	}
    }
    /**
     * Stops the specified server instance. This operation is invoked by 
     * the asadmin stop-instance command.
     */
    public void stopServerInstance(String serverName, String timeoutString) 
    throws InstanceException
    {
        // null timeoutString -- means do not force
        // timeoutString that isn't a valid positive integer or zero means ERROR
        
        if(timeoutString == null)
        {
            stopServerInstance(serverName);
        }
        else
        {
            int timeout;
            try {
                timeout = Integer.parseInt(timeoutString);
            }catch(Exception e) {
                // handle this the same as the test for negative integer below...
                timeout = -1;  
            }

            if(timeout < 0)
                throw new InstanceException(sm.getString("StopInstance.BadTimeout", timeoutString));
            
            try {
                dsh.setstate(serverName, StateManageable.STATE_STOPPING);
                getServersConfigBean().stopServerInstance(serverName, timeout);
            } catch (InstanceException ie) {
                dsh.setstate(serverName, StateManageable.STATE_FAILED);
                throw ie;
            }
        }
    }    
   
    /**
     * Deletes the specified server instance. This operation is invoked by the asadmin delete-instance
     * command.
     */    
    public void deleteServerInstance(String serverName) throws InstanceException
    {
        getServersConfigBean().deleteServerInstance(serverName);
	com.sun.enterprise.ManagementObjectManager mgmtObjManager =
		com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
	mgmtObjManager.unregisterDasJ2EEServer(serverName);
    }    
    
    /**
     * Creates a new server instance. This operation is invoked by the asadmin create-instance
     * command.
     */
    public ObjectName createServerInstance(
        String nodeAgentName, String serverName, 
        String configName, String clusterName, Properties props) 
            throws InstanceException, MBeanException, PortReplacedException
        
    {
        PortReplacedException e = null;
        try {
        getServersConfigBean().createServerInstance(nodeAgentName, serverName,
            configName, clusterName, props);
        } catch (PortReplacedException pre) {
            e = pre;
        }
	com.sun.enterprise.ManagementObjectManager mgmtObjManager =
		com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
	mgmtObjManager.registerDasJ2EEServer(serverName);
	dsh.setstate(serverName, StateManageable.STATE_STOPPED);
        if (e != null) {
            throw e;
        }
        return getServerObjectName(serverName);        
    }          
}
