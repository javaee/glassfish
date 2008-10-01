/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.deployment.client;

import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import org.glassfish.deployapi.ProgressObjectImpl;
import org.glassfish.deployapi.TargetImpl;
import org.glassfish.deployapi.TargetModuleIDImpl;

/**
 * Provides common behavior for the local and remote deployment facilities.
 * <p>
 * Code that needs an instance of a remote deployment facility use the
 * {@link DeploymentFacilityFactory}.
 * <p>
 * Note that for GlassFish v3 prelude there is only a single target, the
 * default server, so the target arguments are not typically used.  This will
 * change after the prelude release.
 *
 * @author tjquinn
 */
public abstract class AbstractDeploymentFacility implements DeploymentFacility, TargetOwner {
    private static final String DEFAULT_SERVER_NAME = "server";
    protected static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RemoteDeploymentFacility.class);
    private boolean connected;
    private TargetImpl domain;
    private ServerConnectionIdentifier targetDAS;

    /**
     * Defines behavior implemented in the local or remote deployment facility
     * for actually executing the requested command.
     */
    public interface DFCommandRunner {

        /**
         * Runs the command.
         *
         * @return the DF deployment status reflecting the outcome of the operation
         * @throws com.sun.enterprise.cli.framework.CommandException
         */
        DFDeploymentStatus run() throws CommandException;
    }

    /**
     * Returns a command runner for the concrete implementation.
     * 
     * @param commandName
     * @param commandOptions
     * @param operands
     * @return
     * @throws com.sun.enterprise.cli.framework.CommandException
     */
    protected abstract DFCommandRunner getDFCommandRunner(
            String commandName,
            Map<String,Object> commandOptions,
            String[] operands) throws CommandException;

    /**
     * Changes the state of an application.
     * <p>
     * Used for enable and disable.
     * @param targets targets on which the change should occur
     * @param moduleID name of the module affected
     * @param commandName enable or disable
     * @param successStatusKey message key for successful result (one for enable, one for disable)
     * @param successStatusDefaultMessage default message value for success
     * @param failureStatusKey message key for failed result
     * @param failureStatusDefaultMessage default message value for failed result
     * @return DFProgressObject the caller can use to monitor progress and query final status
     */
    protected DFProgressObject changeState(Target[] targets, String moduleID, String commandName, String successStatusKey, String successStatusDefaultMessage, String failureStatusKey, String failureStatusDefaultMessage) {
        ensureConnected();
        targets = prepareTargets(targets);
        ProgressObjectImpl po = new ProgressObjectImpl(targets);
        try {
            DFCommandRunner commandRunner = getDFCommandRunner(commandName, null, new String[]{moduleID});
            DFDeploymentStatus ds = commandRunner.run();
            DFDeploymentStatus mainStatus = ds.getMainStatus();
            if (mainStatus.getStatus() != DFDeploymentStatus.Status.FAILURE) {
                TargetModuleIDImpl[] targetModuleIDs = new TargetModuleIDImpl[targets.length];
                int i = 0;
                for (TargetImpl ti : po.toTargetImpl(targets)) {
                    targetModuleIDs[i++] = new TargetModuleIDImpl(ti, moduleID);
                }
                po.setupForNormalExit(localStrings.getLocalString(successStatusKey, successStatusDefaultMessage, moduleID), domain, mainStatus, targetModuleIDs);
            } else {
                po.setupForAbnormalExit(localStrings.getLocalString(failureStatusKey, failureStatusDefaultMessage, mainStatus.getStageStatusMessage()), domain, mainStatus);
            }
            return po;
        } catch (Throwable ioex) {
            po.setupForAbnormalExit(localStrings.getLocalString(failureStatusKey, failureStatusDefaultMessage, ioex.toString()), domain, ioex);
            return po;
        }
    }

    /**
     * Performs any local- or remote-specific work related to connecting to the DAS.
     * @return true if the connection was made successfully; false otherwise
     */
    protected abstract boolean doConnect();

    /**
     * Connects the deployment facility to the DAS.
     * @param targetDAS the DAS to contact
     * @return true if the connection was made successfully; false otherwise
     */
    public boolean connect(ServerConnectionIdentifier targetDAS) {
        connected = true;
        this.targetDAS = targetDAS;
        domain = new TargetImpl(this, "domain", localStrings.getLocalString(
                "enterprise.deployment.client.administrative_domain",
                "administrative-domain"));
        return doConnect();
    }

    /**
     * Performs any local- or remote-specific work to end the connection to the DAS.
     * @return true if the disconnection succeeded; false otherwise
     */
    protected abstract boolean doDisconnect();

    /**
     * Disconnects the deployment facility from the DAS.
     * @return true if the disconnection was successful; false otherwise
     */
    public boolean disconnect() {
        connected = false;
        domain = null;
        targetDAS = null;
        return doDisconnect();
    }

    public DFProgressObject createAppRef(Target[] targets, String moduleID, Map options) {
        throw new UnsupportedOperationException("Not supported in v3 prelude");
    }

    public Target createTarget(String name) {
        return new TargetImpl(this, name, "");
    }

    public Target[] createTargets(String[] targets) {
        TargetImpl[] result = new TargetImpl[targets.length];
        int i = 0;
        for (String name : targets) {
            result[i++] = new TargetImpl(this, name, "");
        }
        return result;
    }

    public DFProgressObject deleteAppRef(Target[] targets, String moduleID, Map options) {
        throw new UnsupportedOperationException("Not supported in v3 prelude");
    }

    /**
     * Deploys the application (with optional deployment plan) to the specified
     * targets with the indicated options.
     * @param targets targets to which to deploy the application
     * @param source the app
     * @param deploymentPlan the deployment plan (null if not specified)
     * @param deploymentOptions options to be applied to the deployment
     * @return DFProgressObject the caller can use to monitor progress and query status
     */
    public DFProgressObject deploy(Target[] targets, URI source, URI deploymentPlan, Map deploymentOptions) {
        ensureConnected();
        targets = prepareTargets(targets);
        ProgressObjectImpl po = new ProgressObjectImpl(targets);
        //Make sure the file permission is correct when deploying a file
        if (source == null) {
            po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.archive_not_specified", "Archive to be deployed is not specified at all."), domain);
            return po;
        }
        File tmpFile = new File(source);
        if (!tmpFile.exists()) {
            po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.archive_not_in_location", "Unable to find the archive to be deployed in specified location."), domain);
            return po;
        }
        if (!tmpFile.canRead()) {
            po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.archive_no_read_permission", "Archive to be deployed does not have read permission."), domain);
            return po;
        }
        boolean isDirectoryDeploy = tmpFile.isDirectory();
        try {
            if (deploymentPlan != null) {
                File dp = new File(deploymentPlan);
                if (!dp.exists()) {
                    po.setupForAbnormalExit(localStrings.getLocalString(
                            "enterprise.deployment.client.plan_not_in_location",
                            "Unable to find the deployment plan in specified location."), domain);
                    return po;
                }
                if (!dp.canRead()) {
                    po.setupForAbnormalExit(localStrings.getLocalString(
                            "enterprise.deployment.client.plan_no_read_permission",
                            "Deployment plan does not have read permission."), domain);
                    return po;
                }
                deploymentOptions.put(DFDeploymentProperties.DEPLOYMENT_PLAN, dp.getAbsolutePath());
            }
            DFCommandRunner commandRunner = getDFCommandRunner(
                    "deploy", deploymentOptions, new String[]{tmpFile.getAbsolutePath()});
            DFDeploymentStatus ds = commandRunner.run();
            DFDeploymentStatus mainStatus = ds.getMainStatus();
            if (mainStatus.getStatus() != DFDeploymentStatus.Status.FAILURE) {
                String moduleID = mainStatus.getProperty(DFDeploymentProperties.NAME);
                TargetModuleIDImpl[] targetModuleIDs = new TargetModuleIDImpl[targets.length];
                int i = 0;
                for (TargetImpl ti : po.toTargetImpl(targets)) {
                    targetModuleIDs[i++] = new TargetModuleIDImpl(ti, moduleID);
                }
                po.setupForNormalExit(localStrings.getLocalString("enterprise.deployment.client.deploy_application", "Deployment of application {0}", moduleID), domain, mainStatus, targetModuleIDs);
            } else {
                po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.deploy_application_failed", "Deployment of application failed - {0}", mainStatus.getStageStatusMessage()), domain, mainStatus);
            }
            return po;
        } catch (Throwable ioex) {
            po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.deploy_application_failed", "Deployment of application failed - {0} ", ioex.toString()), domain, ioex);
            return po;
        }
    }

    /**
     * Disables an app on the specified targets.
     * @param targets the targets on which to disable the app
     * @param moduleID the app
     * @return DFProgressObject for monitoring progress and querying status
     */
    public DFProgressObject disable(Target[] targets, String moduleID) {
        return changeState(targets, moduleID, "disable", "enterprise.deployment.client.disable_application", "Application {0} disabled successfully", "enterprise.deployment.client.disable_application_failed", "Attempt to disable application {0} failed");
    }

    public String downloadFile(File location, String moduleID, String moduleURI) throws IOException {
        throw new UnsupportedOperationException("Not supported in v3 prelude");
    }

    /**
     * Enables an app on the specified targets.
     * @param targets the targets on which to enable the app
     * @param moduleID the app
     * @return DFProgressObject for monitoring progress and querying status
     */
    public DFProgressObject enable(Target[] targets, String moduleID) {
        return changeState(targets, moduleID, "enable", "enterprise.deployment.client.enable_application", "Application {0} enabled successfully", "enterprise.deployment.client.enable_application_failed", "Attempt to enable application {0} failed");
    }

    private void ensureConnected() {
        if (!isConnected()) {
            throw new IllegalStateException(localStrings.getLocalString("enterprise.deployment.client.disconnected_state", "Not connected to the Domain Admin Server"));
        }
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Reports whether the deployment facility is connected.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    public TargetModuleID[] listAppRefs(String[] targets) throws IOException {
        throw new UnsupportedOperationException("Not supported in v3 prelude");
    }

    private Target[] prepareTargets(Target[] targets) {
        if (targets == null || targets.length == 0) {
            targets = new Target[]{targetForDefaultServer()};
        }
        return targets;
    }

    /**
     * Provides a {@link Target} object for the default target.
     *
     * @return Target for the default server
     */
    private Target targetForDefaultServer() {
        TargetImpl t = new TargetImpl(this, DEFAULT_SERVER_NAME, localStrings.getLocalString("enterprise.deployment.client.default_server_description", "default server"));
        return t;
    }

    /**
     * Undeploys an application from specified targets.
     * @param targets the targets from which to undeploy the app
     * @param moduleID the app
     * @return DFProgressObject for monitoring progress and querying status
     */
    public DFProgressObject undeploy(Target[] targets, String moduleID) {
        return undeploy(targets, moduleID, null);
    }

    /**
     * Undeploys an application from specified targets.
     * @param targets the targets from which to undeploy the app
     * @param moduleID the app
     * @param undeploymentOptions options to control the undeployment
     * @return DFProgressObject for monitoring progress and querying status
     */
    public DFProgressObject undeploy(Target[] targets, String moduleID, Map undeploymentOptions) {
        ensureConnected();
        targets = prepareTargets(targets);
        ProgressObjectImpl po = new ProgressObjectImpl(targets);
        try {
            DFCommandRunner commandRunner = getDFCommandRunner(
                    "undeploy",
                    undeploymentOptions,
                    new String[]{moduleID});
            DFDeploymentStatus ds = commandRunner.run();
            DFDeploymentStatus mainStatus = ds.getMainStatus();
            if (mainStatus.getStatus() != DFDeploymentStatus.Status.FAILURE) {
                TargetModuleIDImpl[] targetModuleIDs = new TargetModuleIDImpl[targets.length];
                int i = 0;
                for (TargetImpl ti : po.toTargetImpl(targets)) {
                    targetModuleIDs[i++] = new TargetModuleIDImpl(ti, moduleID);
                }
                po.setupForNormalExit(localStrings.getLocalString("enterprise.deployment.client.undeploy_application", "Undeployment of application {0}", moduleID), domain, mainStatus, targetModuleIDs);
            } else {
                po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.undeploy_application_failed", "Undeployment failed - {0} ", mainStatus.getStageStatusMessage()), domain, mainStatus);
            }
            return po;
        } catch (Throwable ioex) {
            po.setupForAbnormalExit(localStrings.getLocalString("enterprise.deployment.client.undeploy_application_failed", "Undeployment failed - {0} ", ioex.toString()), domain, ioex);
            return po;
        }
    }

    /**
     * Convenient method to wait for the operation monitored by the progress
     * object to complete, returning the final operation status.
     * @param po DFProgressObject for the operation of interestt
     * @return DFDeploymentStatus final status for the operation
     */
    public DFDeploymentStatus waitFor(DFProgressObject po) {
        return po.waitFor();
    }


    public String getWebURL(TargetModuleID tmid) {
        throw new UnsupportedOperationException("Not supported for v3 prelude");
    }

    protected ServerConnectionIdentifier getTargetDAS() {
        return targetDAS;
    }
}
