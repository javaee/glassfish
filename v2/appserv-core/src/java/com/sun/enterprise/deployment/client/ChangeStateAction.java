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
import com.sun.enterprise.deployapi.ProgressObjectImpl;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;

public class ChangeStateAction extends ProgressObjectImpl {

    private static final Set<ModuleType> unchangeableStateModuleTypes = initUnchangeableStateModuleTypes();

    private static Set<ModuleType> initUnchangeableStateModuleTypes() {
        Set<ModuleType> result = new HashSet<ModuleType>();
        result.add(ModuleType.CAR);
        return result;
    }
    
    public ChangeStateAction(SunTarget[] targets) {
        super(targets);
    }

    public void run() {
        ConnectionSource dasConnection= (ConnectionSource) args[0];
        SunTarget[] targets = (SunTarget[]) args[1];
        moduleID = (String) args[2];
        CommandType newState = (CommandType) args[3];
        SunTarget domain = (SunTarget) args[4];
        StringManager localStrings = StringManager.getManager(getClass());

        ModuleType moduleType;
        try {
            moduleType = DeploymentClientUtils.getModuleType(
                dasConnection.getExistingMBeanServerConnection(), moduleID);
        } catch (Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.unrecognized_module_type", moduleID ,ioex.getMessage()),
                        domain);
            return;
        }
        
        boolean state = false;
        String action = "Disable";
        if (CommandType.START.equals(newState)) {
            state = true;
            action = "Enable";
        }

        // the target module ids in which the operation was successful
        ArrayList resultTargetModuleIDs = new ArrayList();             

        for(int i=0; i<targets.length; i++) {
            DeploymentStatus stat = new DeploymentStatus();
            stat.setStageDescription(
                localStrings.getString("enterprise.deployment.client.state_change_desc", action, moduleID));
            try {
                /*
                 *If the module type supports state changes, change the state.
                 *Otherwise prepare a warning status with a "no-op" message.
                 */
                String messageKey;
                int deplStatus;
                if (! unchangeableStateModuleTypes.contains(moduleType)) {
                    messageKey = "enterprise.deployment.client.state_change_success";
                    deplStatus = DeploymentStatus.SUCCESS;
                    
                    DeploymentClientUtils.changeStateOfModule(dasConnection.getExistingMBeanServerConnection(), moduleID,
                                    ((moduleType == null) ? null : moduleType.toString()), targets[i], state);
                } else {
                    messageKey = "enterprise.deployment.client.state_change_noop";
                    deplStatus = DeploymentStatus.SUCCESS;
                }
                stat.setStageStatus(deplStatus);
                stat.setStageStatusMessage(localStrings.getString(messageKey, action, moduleID));
            } catch (Throwable ex) {
                String msg;
                if (CommandType.START.equals(newState)) {
                    msg = localStrings.getString(
                        "enterprise.deployment.client.start.failed");
                } else {
                    msg = localStrings.getString(
                        "enterprise.deployment.client.stop.failed");
                }
                stat.setStageException(ex);
                stat.setStageStatus(DeploymentStatus.FAILURE);
                stat.setStageStatusMessage(msg + ex.getMessage());
            }
            if(!checkStatusAndAddStage(targets[i], null, 
                            localStrings.getString("enterprise.deployment.client.change_state", action, moduleID, targets[i].getName()), dasConnection, stat, state)) {
                return;
            }
            resultTargetModuleIDs.add(new SunTargetModuleID(moduleID, targets[i]));
        }

        // initialize the instance variable targetModuleIDs using 
        // the successful module ids
        this.targetModuleIDs = new TargetModuleID[resultTargetModuleIDs.size()];
        this.targetModuleIDs = 
            (TargetModuleID[])resultTargetModuleIDs.toArray(this.targetModuleIDs);

        setupForNormalExit(localStrings.getString("enterprise.deployment.client.change_state_all", action), domain);
    }
}
