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

/*
 * InstanceStatus.java
 *
 * Created on August 27, 2004, 3:10 PM
 */

package com.sun.enterprise.ee.nodeagent;

import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.logging.ee.EELogDomains;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * This class is used to hold a RuntimeStatus containing the transient state of each
 * server instance managed by the Node Agent / ProcessManager. This is used to prevent concurrent
 * access to a particular server (e.g. starting server1 twice, stopping a server 
 * which is in the process of starting, etc. Currently we use only the state component
 * of the RuntimeStatus, but the full RuntimeStatus object may prove more useful going forward. 
 *
 * Unfortunately both the NodeAgent and ProcessManager are consumers of this class and the two
 * classes are now co-dependent because of it. In other words the ProcessManager cannot be 
 * used in the absence of the NodeAgent since it requires that the Node Agent setup state. 
 * It would be possible to move some of the status checking done by the Node Agent into 
 * the ProcessManager but due to the synchronization done by the Node Agent, all the state 
 * checking cannot be moved there, and as such, it is not clear what is the best approach.
 */
class InstanceStatus extends HashMap
{
    private static InstanceStatus _instanceStatus = null;
   
    private InstanceStatus() {
        super();
    }      
    
    private static Logger getLogger() {
        return Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER);
    }
    /**
     * 
     * @return the singleton InstanceStatus object.
     */    
    public synchronized static InstanceStatus getInstance() {
        if (_instanceStatus == null) {
            _instanceStatus = new InstanceStatus();
        }
        return _instanceStatus;
    }
    
    /**
     * Fetch the status for the given server instance. 
     * @param instanceName the server instance name. If the server instance does not exist
     * a newly constructed RuntimeStatus (in a not running state) will be returned.
     * @return The RuntimeStatus of the given server instance.
     */    
    public synchronized RuntimeStatus getStatus(String instanceName) {
        RuntimeStatus result = (RuntimeStatus)get(instanceName);
        if (result == null) {
            result = new RuntimeStatus(instanceName);
            result.setStatus(new Status(Status.kInstanceNotRunningCode));
            put(result.getName(), result);
        } 
        //If we are in the state of running or not running, then we need to ensure 
        //that our internal state is in sync with that of the admin channel. We need to 
        //avoid our internal state getting out of sync.
        if (result.isRunning() || result.isStopped()) {
            updateStatusFromAdminChannel(instanceName, result.getStatus().getStatusCode());
        }
        return result;
    }

    /**
     * Update the status of a server instance
     * @param instanceName the server instance name
     * @param statusCode the status code, should be one of the constants defined
     * in com.sun.enterprise.admin.common.Status.
     * @return newly udated RuntimeStatus
     */    
    public synchronized RuntimeStatus updateStatus(String instanceName,
        int statusCode) 
    {
        RuntimeStatus result = getStatus(instanceName);
        result.setStatus(new Status(statusCode));
        return result;
    }
 
    private synchronized boolean updateStatusConditionally(String instanceName, 
        int requiredStatusCode, int newStatusCode)         
    {               
        RuntimeStatus result = getStatus(instanceName);
        if (result.getStatus().getStatusCode() != requiredStatusCode) {        
            return false;
        } else {
            result.setStatus(new Status(newStatusCode));         
            return true;
        }
    }
       
    /**
     * Update the internal status of the server instance to match that provided by the admin channel
     * of the server instance itelef
     * @param instanceName server instance name
     * @param statusCode desired status code
     */    
    public synchronized RuntimeStatus updateStatusFromAdminChannel(String instanceName, int statusCode)
    {
        int newStatusCode = statusCode;
        try {
            InstanceConfig instanceConfig=new InstanceConfig(
                InstanceDirs.getRepositoryName(), InstanceDirs.getRepositoryRoot(), instanceName);
            EEInstancesManager eeInstancesManager=new EEInstancesManager(instanceConfig);        
            newStatusCode = eeInstancesManager.getInstanceStatus();              
            if (newStatusCode != statusCode) {
                getLogger().log(Level.INFO, "server " + instanceName + " has admin channel status of " + 
                    Status.getStatusString(newStatusCode) + " and desired status of " +
                    Status.getStatusString(statusCode));
            }
        } catch (InstanceException ex) {
            ex.printStackTrace();            
        }        
        RuntimeStatus result = (RuntimeStatus)get(instanceName);
        if (result == null) {
            result = new RuntimeStatus(instanceName);            
            put(result.getName(), result);
        }  
        result.setStatus(new Status(newStatusCode));
        return result;
    }
        
    /**
     * Update the instances status if it is in the stopped state. This is a synchronized 
     * call to getStatus and updateStatus and is necessary so that the status can be 
     * checked and updated as an atomic operation.
     * @param instanceName the name of the instance to check and update
     * @param newStatusCode the new status
     * @return true if the instance was in the stopped state and its status was successfully
     * updated
     */    
    public synchronized boolean updateStatusIfStopped(String instanceName, int newStatusCode)         
    {               
        return updateStatusConditionally(instanceName, Status.kInstanceNotRunningCode,
            newStatusCode);
    }
    
    /**
     * Update the instances status if it is in the started state. This is a synchronized 
     * call to getStatus and updateStatus and is necessary so that the status can be 
     * checked and updated as an atomic operation.
     * @param instanceName the name of the instance to check and update
     * @param newStatusCode the new status
     * @return true if the instance was in the started state and its status was successfully
     * updated
     */     
    public synchronized boolean updateStatusIfRunning(String instanceName, int newStatusCode) 
    {       
        return updateStatusConditionally(instanceName, Status.kInstanceRunningCode,
            newStatusCode);
    }
    
    
}