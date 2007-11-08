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

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentProgressImpl;
import com.sun.appserv.management.deploy.DeploymentSource;
import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.enterprise.deployapi.ProgressObjectImpl;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.util.i18n.StringManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

public class UndeployAction extends ProgressObjectImpl {

    private DeploymentMgr deplMgr = null;
    private static StringManager localStrings = StringManager.getManager(UndeployAction.class);
    
    public UndeployAction(SunTarget[] targets) {
        super(targets);
    }

    public void run() {
        ConnectionSource dasConnection= (ConnectionSource) args[0];
        String moduleID = (String) args[1];
        Map deployOptions = (Map) args[2];
        SunTarget[] targetList = (SunTarget[]) args[3];
        SunTarget domain = (SunTarget) args[4];
        boolean isLocalConnectionSource = ((Boolean) args[5]).booleanValue();

        try {
            // First check if this module is a web module and if so, it is a default web module
            if((DeploymentClientUtils.getModuleType(dasConnection.getExistingMBeanServerConnection(), moduleID) 
                == ModuleType.WAR) &&
               (isDefaultWebModule(domain, dasConnection, moduleID))) {
                return;
            }
            deplMgr = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDeploymentMgr();
            // Get list of all targets on which this module is already deployed
            Map deployedTargets = DeploymentClientUtils.getDeployedTargetList(dasConnection, moduleID);

            // the target module ids in which the operation was successful
            ArrayList resultTargetModuleIDs = new ArrayList(); 

            // if there is already app ref associated with this app
            if (deployedTargets.size() > 0) {
                // if it's undeploy from domain, then it's equivalent
                // to undeploy from all targets
                if ((TargetType.DOMAIN.equals(targetList[0].getName()))) {
                    DeploymentFacility deploymentFacility;
                    if(isLocalConnectionSource) {
                        deploymentFacility = DeploymentFacilityFactory.getLocalDeploymentFacility();
                    } else {
                        deploymentFacility = DeploymentFacilityFactory.getDeploymentFacility();
                    }
                    deploymentFacility.connect(
                        targetList[0].getConnectionInfo());
                    Set nameSet = deployedTargets.keySet();
                    String[] targetNames = (String[])nameSet.toArray(
                        new String[nameSet.size()]);
                    Target[] targetList2 =
                        deploymentFacility.createTargets(targetNames);
                    if (targetList2 == null) {
                        setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.createTargetsFailed"), domain);
                        return;
                    }
                    targetList = new SunTarget[targetList2.length];
                    for (int ii = 0; ii < targetList2.length; ii++) {
                        targetList[ii] = (SunTarget)targetList2[ii];
                    }
                }

                // if all targets on which the app is deployed is not given, 
                // return error
                else if (!DeploymentClientUtils.isTargetListComplete(deployedTargets, targetList)) {
                    setupForAbnormalExit(
                        localStrings.getString("enterprise.deployment.client.specifyAllTargets", moduleID, "undeploy"),
                        domain);
                    return;
                }

                // First stop all apps and remove all app references
                RollBackAction rollback = new RollBackAction(RollBackAction.DELETE_APP_REF_OPERATION, 
                                                                moduleID, deployOptions);
                deployOptions.put(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY, "false");
                for(int i=0; i<targetList.length; i++) {

                    // We dont rollback for stop failure because the failure may be because of server being down
                    // We just add DeploymentStatus of this phase to the complete DeploymentStatus
                    
                    DeploymentClientUtils.setResourceOptions(
                        deployOptions,
                        DeploymentProperties.RES_UNDEPLOYMENT,
                        targetList[i].getName());
                    DeploymentStatus stat =  DeploymentClientUtils.stopApplication(
                        dasConnection.getExistingMBeanServerConnection(),
                        moduleID, targetList[i], deployOptions);
                    if (!checkStatusAndAddStage(targetList[i], null, 
                                localStrings.getString("enterprise.deployment.client.undeploy_stop", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    
                    stat = DeploymentClientUtils.deleteApplicationReference(
                        dasConnection.getExistingMBeanServerConnection(),
                        moduleID, targetList[i], deployOptions);
                    if(!checkStatusAndAddStage(targetList[i], rollback, 
                                localStrings.getString("enterprise.deployment.client.undeploy_remove_ref", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    rollback.addTarget(targetList[i], RollBackAction.APP_REF_DELETED);
                    resultTargetModuleIDs.add(new SunTargetModuleID(moduleID, targetList[i]));
                }
            }
            
            // if undeploy from "domain" with no existing application-ref
            if ((TargetType.DOMAIN.equals(targetList[0].getName()))) {
                DeploymentClientUtils.setResourceOptions(
                    deployOptions,
                    DeploymentProperties.RES_UNDEPLOYMENT,
                    targetList);
            } else {
                DeploymentClientUtils.setResourceOptions(
                    deployOptions,
                    DeploymentProperties.RES_NO_OP,
                    targetList);
            }
 
            // Call DeploymentMgr to start undeploy
            fireProgressEvent(StateType.RUNNING, localStrings.getString("enterprise.deployment.client.undeploying"), domain);
            Map undeployStatus = deplMgr.undeploy(moduleID, deployOptions);            
            
            com.sun.appserv.management.deploy.DeploymentStatus finalStatusFromMBean = 
                            DeploymentSupport.mapToDeploymentStatus(undeployStatus);
            DeploymentStatus tmp = DeploymentClientUtils.getDeploymentStatusFromAdminStatus(finalStatusFromMBean);
            if(!checkStatusAndAddStage(domain, null, localStrings.getString("enterprise.deployment.client.undeploy_from_domain"), dasConnection, tmp)) {
                return;
            }
            
            // undeploy over - add this to the result target module ID
            resultTargetModuleIDs.add(new SunTargetModuleID(moduleID, domain));
                
            // initialize the instance variable targetModuleIDs using 
            // the successful module ids
            this.targetModuleIDs = new TargetModuleID[resultTargetModuleIDs.size()];
            this.targetModuleIDs = 
                (TargetModuleID[])resultTargetModuleIDs.toArray(this.targetModuleIDs);
            
            setupForNormalExit(localStrings.getString("enterprise.deployment.client.undeploy_application", moduleID), domain);
        } catch (Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.undeploy_application_failed", ioex.getMessage()), domain);
            return;
        }
    }
    
    private boolean isDefaultWebModule(SunTarget domain, ConnectionSource dasConnection, String moduleID) {
        try {
            DomainConfig cfg = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDomainConfig();
            Map cfgcfg = cfg.getConfigConfigMap();
            for(Iterator it1 = cfgcfg.keySet().iterator(); it1.hasNext(); ) {
                ConfigConfig cf1 = (ConfigConfig) cfgcfg.get(it1.next());
                HTTPServiceConfig httpSvc = cf1.getHTTPServiceConfig();
                Map vsMap = httpSvc.getVirtualServerConfigMap();
                for(Iterator it2 = vsMap.keySet().iterator(); it2.hasNext(); ) {
                    VirtualServerConfig vs = (VirtualServerConfig) vsMap.get(it2.next());
                    if(moduleID.equals(vs.getDefaultWebModule())) {
                        setupForAbnormalExit(
                            localStrings.getString("enterprise.deployment.client.def_web_module_refs_exist", moduleID), domain);
                        return true;
                    }
                }
            }
        } catch (Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.undeploy_application_failed", ioex.getMessage()), domain);
            return true;
        }
        return false;
    }
}
