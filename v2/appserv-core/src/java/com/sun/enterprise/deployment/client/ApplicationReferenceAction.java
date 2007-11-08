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
import java.io.IOException;

import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.util.i18n.StringManager;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;

import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployapi.ProgressObjectImpl;
import com.sun.enterprise.deployapi.SunTargetModuleID;

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.AppserverConnectionSource;

public class ApplicationReferenceAction extends ProgressObjectImpl {
    
    private static StringManager localStrings = StringManager.getManager(ApplicationReferenceAction.class);

    public ApplicationReferenceAction(SunTarget[] targets) {
        super(targets);
    }

    private void handleAppRefActionForLifeCycleModules(ConnectionSource dasConnection, SunTarget[] targetList, 
                                            String id, CommandType cmd, Map options) {
        String action = (CommandType.DISTRIBUTE.equals(cmd)) ? "Creation" : "Removal";


        try {
            for(int i=0; i<targetList.length; i++) {
                DeploymentStatus stat = null;
                if(CommandType.DISTRIBUTE.equals(cmd)) {
                    stat = DeploymentClientUtils.createLifecycleModuleReference(
                        dasConnection.getExistingMBeanServerConnection(), id, targetList[i].getName(), options);
                } else {
                    stat = DeploymentClientUtils.removeLifecycleModuleReference(
                        dasConnection.getExistingMBeanServerConnection(), id, targetList[i].getName());
                }
                if(!checkStatusAndAddStage(targetList[i], null, localStrings.getString("enterprise.deployment.client.change_reference", action, targetList[i].getName()), dasConnection, stat)) {
                    return;
                }
            }
            setupForNormalExit(localStrings.getString("enterprise.deployment.client.change_reference_lifemodule", action), targetList[0]);
        } catch(Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.change_reference_lifemodule_failed", action, ioex.getMessage()), targetList[0]);
        }
        return;
    }
    
    public void run() {
        ConnectionSource dasConnection= (ConnectionSource) args[0];
        SunTarget[] targetList = (SunTarget[]) args[1];
        String moduleID = (String) args[2];
        CommandType cmd = (CommandType) args[3];
        Map deployOptions = (Map) args[4];
            
        String action = (CommandType.DISTRIBUTE.equals(cmd)) ? "Creation" : "Removal";
        try {
            // Handle app-ref-creation/app-ref-removal for life-cycle-module as a special case
            if(DeploymentClientUtils.isLifecycleModule(dasConnection.getExistingMBeanServerConnection(), moduleID)) {
                handleAppRefActionForLifeCycleModules(dasConnection, targetList, moduleID, cmd, deployOptions);
                return;
            }
        
            // the target module ids in which the operation was successful
            ArrayList resultTargetModuleIDs = new ArrayList();             

            RollBackAction rollback;
            if(CommandType.DISTRIBUTE.equals(cmd)) {
                rollback = new RollBackAction(RollBackAction.CREATE_APP_REF_OPERATION, moduleID, deployOptions);
            } else{
                rollback = new RollBackAction(RollBackAction.DELETE_APP_REF_OPERATION, moduleID, deployOptions);
            }

            for(int i=0; i<targetList.length; i++) {
                if(CommandType.DISTRIBUTE.equals(cmd)) {
                    DeploymentStatus stat =  
                        DeploymentClientUtils.createApplicationReference(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], deployOptions);
                    if(!checkStatusAndAddStage(targetList[i], rollback, 
                                    localStrings.getString("enterprise.deployment.client.create_reference", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    rollback.addTarget(targetList[i], RollBackAction.APP_REF_CREATED);
                    
                    /*
                      XXX Start the application regardless the value of "enable"
                      Otherwise no DeployEvent would be sent to the listeners on 
                      a remote instance, which would in turn synchronize the app
                      bits.  Note that the synchronization is only called during
                      applicationDeployed, not applicationEnabled.  To make sure
                      the deployment code can work with both the new and the old
                      mbeans, we will call the start for now (as the old mbeans
                      would do).  The backend listeners are already enhanced to
                      make sure the apps are not actually loaded unless the enable
                      attributes are true for both the application and 
                      application-ref elements.
                    */
                    DeploymentClientUtils.setResourceOptions(
                        deployOptions,
                        DeploymentProperties.RES_CREATE_REF,
                        targetList[i].getName());
                    stat = DeploymentClientUtils.startApplication(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], deployOptions);
                    
                    // We dont rollback for start failure because start failure may be because of server being down
                    // We just add DeploymentStatus of this phase to the complete DeploymentStatus
                    
                    checkStatusAndAddStage(targetList[i], null, 
                        localStrings.getString("enterprise.deployment.client.reference_start", targetList[i].getName()), dasConnection, stat, true);

                } else {
                    deployOptions.put(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY, "false");
                    
                    // We dont rollback for stop failure because the failure may be because of server being down
                    // We just add DeploymentStatus of this phase to the complete DeploymentStatus
                    
                    DeploymentClientUtils.setResourceOptions(
                        deployOptions,
                        DeploymentProperties.RES_DELETE_REF,
                        targetList[i].getName());
                    DeploymentStatus stat = 
                        DeploymentClientUtils.stopApplication(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], deployOptions);
                    if (!checkStatusAndAddStage(targetList[i], null, 
                                  localStrings.getString("enterprise.deployment.client.reference_stop", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    
                    stat = DeploymentClientUtils.deleteApplicationReference(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], deployOptions);
                    if(!checkStatusAndAddStage(targetList[i], rollback, 
                       localStrings.getString("enterprise.deployment.client.remove_reference", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    rollback.addTarget(targetList[i], RollBackAction.APP_REF_DELETED);
                }
                resultTargetModuleIDs.add(new SunTargetModuleID(moduleID, targetList[i]));
            }
            
            // initialize the instance variable targetModuleIDs using 
            // the successful module ids
            this.targetModuleIDs = new TargetModuleID[resultTargetModuleIDs.size()];
            this.targetModuleIDs = 
                (TargetModuleID[])resultTargetModuleIDs.toArray(this.targetModuleIDs);
            
            setupForNormalExit(localStrings.getString("enterprise.deployment.client.change_reference_application", action), targetList[0]);
        } catch (Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.change_reference_application_failed", action, ioex.getMessage()), targetList[0]);
            return;
        }
    }
}
