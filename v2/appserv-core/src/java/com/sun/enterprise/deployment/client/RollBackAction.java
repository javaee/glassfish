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

package com.sun.enterprise.deployment.client;

import java.util.*;

import com.sun.enterprise.deployapi.SunTarget;

import com.sun.enterprise.deployment.backend.DeploymentStatus;

import com.sun.enterprise.deployment.util.DeploymentProperties;

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.deploy.DeploymentMgr;

import com.sun.appserv.management.deploy.DeploymentSupport;

public class RollBackAction {

    // Stores the moduleID and options required for undeploy
    private String moduleID = null;
    private Map options = null;
    private int currentOperation = 0;
    
    // Keeps track of the targets on which operations are already over and state of each target
    private Map targetState = null;
    private Vector targetList = null;

    // Used to indicate state of the app on a target
    public static final int APP_REF_CREATED = 1;
    public static final int APP_STARTED = 2;
    public static final int APP_REF_DELETED = 3;
    public static final int APP_STOPPED = 4;
    
    // Used to indicate what operation is currently being done
    public static final int DEPLOY_OPERATION = 1;
    public static final int UNDEPLOY_OPERATION = 2;
    public static final int CREATE_APP_REF_OPERATION = 3;
    public static final int DELETE_APP_REF_OPERATION = 4;
    
    public RollBackAction(int operation, String moduleID, Map options) {
        this.currentOperation = operation;
        this.moduleID = moduleID;
        this.options = options;
    }

    // Called after successful completion of each start/create-app-ref on a target
    public void addTarget(SunTarget target, int state) {
        if(targetList == null) {
            targetList = new Vector();
            targetState= new HashMap();
        }
        targetState.put(target.getName(), new Integer(state));
        targetList.add(target);
        return;
    }

    // Called when the client wants to rollback current operation
    public boolean rollback(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        boolean retVal = false;
        
        switch(currentOperation) {
            case DEPLOY_OPERATION :
                if(!stopModules(dasConnection, rollbackStatus)) {
                    return false;
                }
                if(!deleteAppRefs(dasConnection, rollbackStatus)) {
                    return false;
                }
                retVal = undeployModule(dasConnection, rollbackStatus);
                break;
            
            case CREATE_APP_REF_OPERATION :
                if(!stopModules(dasConnection, rollbackStatus)) {
                    return false;
                }
                retVal = deleteAppRefs(dasConnection, rollbackStatus);
                break;
                
            case UNDEPLOY_OPERATION :
                retVal = true; // failures during undeploy from domain will never be rolled back
                break;
                
            case DELETE_APP_REF_OPERATION :
                if(!createAppRefs(dasConnection, rollbackStatus)) {
                    return false;
                }
                retVal = startModules(dasConnection, rollbackStatus);
                break;
                
            default :
                break;
        }
        return retVal;
    }
    
    private boolean undeployModule(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        DeploymentMgr deplMgr = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDeploymentMgr();        
        Map undeployStatus = deplMgr.undeploy(moduleID, options);            
        com.sun.appserv.management.deploy.DeploymentStatus finalStatusFromMBean = 
                            DeploymentSupport.mapToDeploymentStatus(undeployStatus);
        DeploymentStatus tmp = DeploymentClientUtils.getDeploymentStatusFromAdminStatus(finalStatusFromMBean);
        rollbackStatus.addSubStage(tmp);
        if (tmp!=null && tmp.getStatus() < DeploymentStatus.WARNING) {
            return false;
        }
        return true;
    }
    
    private boolean deleteAppRefs(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        if(targetList == null) {
            return true;
        }
        if("true".equals(options.get(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY))) {
            options.put(DeploymentProperties.DEPLOY_OPTION_CASCADE_KEY, "false");
        } else {
            options.put(DeploymentProperties.DEPLOY_OPTION_CASCADE_KEY, "true");
        }
        try {
            SunTarget[] targetObjs = (SunTarget[]) targetList.toArray(new SunTarget[targetList.size()]);
            for(int i=0; i<targetObjs.length; i++) {
                int state = ((Integer)targetState.get(targetObjs[i].getName())).intValue();
                if(state == APP_REF_CREATED) {
                    DeploymentStatus status = 
                        DeploymentClientUtils.deleteApplicationReference(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetObjs[i], options);
                    rollbackStatus.addSubStage(status);
                    if (status!=null && status.getStatus() < DeploymentStatus.WARNING) {
                        return false;
                    }
                    targetState.remove(targetObjs[i].getName());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean stopModules(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        if(targetList == null) {
            return true;
        }
        
        Map tmpOptions = new HashMap();
        tmpOptions.putAll(options);
        tmpOptions.put(DeploymentProperties.DEPLOY_OPTION_CASCADE_KEY, "true");
        tmpOptions.put(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY, "true");
        try {
            SunTarget[] targetObjs = (SunTarget[]) targetList.toArray(new SunTarget[targetList.size()]);
            for(int i=0; i<targetObjs.length; i++) {
                int state = ((Integer)targetState.get(targetObjs[i].getName())).intValue();
                if(state == APP_STARTED) {
                    DeploymentStatus status = 
                        DeploymentClientUtils.stopApplication(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetObjs[i], tmpOptions);
                    rollbackStatus.addSubStage(status);
                    if (status!=null && status.getStatus() < DeploymentStatus.WARNING) {
                        return false;
                    }
                    targetState.put(targetObjs[i].getName(), new Integer(APP_REF_CREATED));
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean createAppRefs(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        if(targetList == null) {
            return true;
        }
        try {
            SunTarget[] targetObjs = (SunTarget[]) targetList.toArray(new SunTarget[targetList.size()]);
            for(int i=0; i<targetObjs.length; i++) {
                int state = ((Integer)targetState.get(targetObjs[i].getName())).intValue();
                if(state == APP_REF_DELETED) {
                    DeploymentStatus status = 
                        DeploymentClientUtils.createApplicationReference(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetObjs[i], options);
                    rollbackStatus.addSubStage(status);
                    if (status!=null && status.getStatus() < DeploymentStatus.WARNING) {
                        return false;
                    }
                    targetState.put(targetObjs[i].getName(), new Integer(APP_STOPPED));
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean startModules(ConnectionSource dasConnection, DeploymentStatus rollbackStatus) {
        if(targetList == null) {
            return true;
        }
        try {
            SunTarget[] targetObjs = (SunTarget[]) targetList.toArray(new SunTarget[targetList.size()]);
            for(int i=0; i<targetObjs.length; i++) {
                int state = ((Integer)targetState.get(targetObjs[i].getName())).intValue();
                if(state == APP_STOPPED) {
                    DeploymentStatus status = 
                        DeploymentClientUtils.startApplication(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetObjs[i], options);
                    rollbackStatus.addSubStage(status);
                    // We don't check the start return because we can't do anything if the instance is down
                    targetState.remove(targetObjs[i].getName());
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
