/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.deployment.autodeploy;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * Abstract class for operations the AutoDeployer can perform (currently
 * deploy and undeploy).
 * <p>
 * AutoOperation and its subclasses have no-arg constructors so they can be
 * initialized as services and an init method that accepts what might otherwise
 * be constructor arguments.
 * 
 * @author tjquinn
 */
@Service
@Scoped(PerLookup.class)
public abstract class AutoOperation {
    
    final static Logger sLogger=LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);
    final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AutoDeployer.class);

    /**
     * Used in deleting all marker files for a given app.
     */
    final static private String [] autoDeployFileSuffixes = new String[] {
            AutoDeployConstants.DEPLOYED,
            AutoDeployConstants.DEPLOY_FAILED,
            AutoDeployConstants.UNDEPLOYED,
            AutoDeployConstants.UNDEPLOY_FAILED,
            AutoDeployConstants.PENDING
        };

    private File file;
    private Properties props;
    private String commandName;
    private AdminCommand command;
    
    @Inject
    private CommandRunner commandRunner;

    @Inject
    private AutodeployRetryManager retryManager;
    
    /**
     * Initializes the AutoOperation.
     * @param file the File of interest
     * @param props command-line options to be passed to the relevant AdminCommand (deploy or undeploy)
     * @param commandName name of the command to execute
     * @param command the AdminCommand descendant to execute
     * @return this same operation
     */
    AutoOperation init(File file, Properties props, String commandName, AdminCommand command) {
        this.file = file;
        this.props = props;
        this.commandName = commandName;
        this.command = command;
        return this;
    }

    /**
     * Marks the files relevant to the specified file appropriately given the
     * outcome of the command as given in the status.
     * @param ds AutodeploymentStatus indicating the outcome of the operation
     * @param file file of interest
     */
    protected abstract void markFiles(AutodeploymentStatus ds, File file);
    
    /**
     * Returns the appropriate message string for the given operation and the
     * outcome.
     * @param ds AutodeploymentStatus value giving the outcome of the operation
     * @param file file of interest
     * @return message string to be logged
     */
    protected abstract String getMessageString(AutodeploymentStatus ds, File file);

    /**
     * Executes the operation
     * @return true/false depending on the outcome of the operation
     * @throws org.glassfish.deployment.autodeploy.AutoDeploymentException
     */
    final AutodeploymentStatus run() throws AutoDeploymentException {
        try {
            ActionReport report = commandRunner.getActionReport("hk2-agent");
            commandRunner.doCommand(commandName, command, props, report);
            AutodeploymentStatus ds = AutodeploymentStatus.forExitCode(report.getActionExitCode());
            Level messageLevel = (ds.status ? Level.INFO : Level.WARNING);
            sLogger.log(messageLevel, getMessageString(ds, file));
            markFiles(ds, file);
            /*
             * Choose the final status to report, based on the outcome of the
             * deployment as well as whether we are now monitoring this file.
             */
            ds = retryManager.chooseAutodeploymentStatus(report.getActionExitCode(), file);
            return ds;
        } catch (Exception ex) {
            /*
             * Log and continue.
             */
            sLogger.log(Level.SEVERE, "Error occurred: ", ex); 
            return AutodeploymentStatus.FAILURE;
        }
    }
    
    private File getSuffixedFile(File f, String suffix) {
        String absPath = f.getAbsolutePath();
        File ret = new File(absPath + suffix);
        return ret;
    }
    
    /**
     * Returns a File object for the "deployed" marker file for a given file.
     * @param f
     * @return File for the "deployed" marker file
     */
    protected File getDeployedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.DEPLOYED);
    }
    
    /**
     * Returns a File object for the "deploy failed" marker file for a given file.
     * @param f
     * @return File for the "deploy failed" marker file
     */
    protected File getDeployFailedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.DEPLOY_FAILED);
    }
    
    /**
     * Returns a File object for the "undeployed" marker file for a given file.
     * @param f
     * @return File for the "undeployed" marker file
     */
    protected File getUndeployedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.UNDEPLOYED);
    }
    
    /**
     * Returns a File object for the "undeploy failed" marker file for a given file.
     * @param f
     * @return File for the "undeploy failed" marker file
     */
    protected File getUndeployFailedFile(File f) {
        return getSuffixedFile(f, AutoDeployConstants.UNDEPLOY_FAILED);
    }
    
    
    /**
     * Deletes all possible marker files for the file.
     * @param f the File whose markers should be removed
     */
    protected void deleteAllMarks(File f) {
        try {
            for (String suffix : autoDeployFileSuffixes) {
                getSuffixedFile(f, suffix).delete();
            }
        } catch (Exception e) { 
            //ignore 
        }
    }
    
}
