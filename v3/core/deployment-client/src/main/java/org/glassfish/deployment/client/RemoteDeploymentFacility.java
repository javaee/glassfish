/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import com.sun.enterprise.admin.cli.RemoteCommand;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.glassfish.deployapi.ProgressObjectImpl;
import org.glassfish.deployapi.TargetImpl;
import org.glassfish.deployapi.TargetModuleIDImpl;

/**
 * Implements DeploymentFacility, currently using the RemoteCommand to work with the
 * admin back-end.
 * <p>
 * Because RemoteCommand uses the REST interface with the admin back-end it
 * is connectionless.  Clients of RemoteDeploymentFacility must still
 * {@link #connect} before attempting to use it.
 * 
 * @author tjquinn
 */
public class RemoteDeploymentFacility implements DeploymentFacility, TargetOwner {

    private ServerConnectionIdentifier targetDAS;
    private RemoteCommand remoteCommand;
    private File passwordFile;
    private TargetImpl domain;
    
    private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RemoteDeploymentFacility.class);
    private static final String DEFAULT_SERVER_NAME = "server";
    
    public boolean connect(ServerConnectionIdentifier targetDAS) {
        this.targetDAS = targetDAS;
        remoteCommand = RemoteCommand.getInstance();
        passwordFile = preparePasswordFile();
        domain = new TargetImpl(this, "domain", localStrings.getLocalString(
                "enterprise.deployment.client.administrative_domain",
                "administrative-domain"));
        return true;
    }

    public boolean isConnected() {
        return (remoteCommand != null);
    }

    public boolean disconnect() {
        remoteCommand = null;
        passwordFile.delete();
        domain = null;
        targetDAS = null;
        return true;
    }

    public DFProgressObject deploy(Target[] targets, URI source,
                                   URI deploymentPlan, Map deploymentOptions) {
        ensureConnected();
        targets = prepareTargets(targets);
        ProgressObjectImpl po = new ProgressObjectImpl(targets);

        //Make sure the file permission is correct when deploying a file
        if (source == null) {
            po.setupForAbnormalExit(localStrings.getLocalString(
                    "enterprise.deployment.client.archive_not_specified",
                    "Archive to be deployed is not specified at all."),
                domain);
            return po;                            
        }

        File tmpFile = new File(source);
        if(!tmpFile.exists()) {
            po.setupForAbnormalExit(localStrings.getLocalString(
                    "enterprise.deployment.client.archive_not_in_location",
                    "Unable to find the archive to be deployed in specified location."), 
                domain);
            return po;
        }
        if(!tmpFile.canRead()) {
            po.setupForAbnormalExit(localStrings.getLocalString(
                    "enterprise.deployment.client.archive_no_read_permission",
                    "Archive to be deployed does not have read permission."), 
                domain);
            return po;
        }
        
        boolean isDirectoryDeploy = tmpFile.isDirectory();

        try {

            if (deploymentPlan != null) {
                deploymentOptions.put(DFDeploymentProperties.DEPLOYMENT_PLAN, deploymentPlan);
            }

            String[] commandArgs = prepareRemoteCommandArguments(
                    "deploy", 
                    deploymentOptions, 
                    new String[] {tmpFile.getAbsolutePath()}
                    );
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            remoteCommand.handleRemoteCommand(commandArgs, "xml-cli", baos);
            DFDeploymentStatus ds = CommandXMLResultParser.parse(new ByteArrayInputStream(baos.toByteArray()));
            DFDeploymentStatus mainStatus = ds.getMainStatus();
            if (mainStatus.getStatus() != DFDeploymentStatus.Status.FAILURE) {
                /*
                 * Get the module ID that the server used to deploy the app.  It
                 * might have been supplied by the caller, but if the caller did
                 * not specify one then the server will have chosen one.  We
                 * need whatever module ID the server used, however it chose it.
                 */
                String moduleID = mainStatus.getProperty(DFDeploymentProperties.NAME);
                TargetModuleIDImpl[] targetModuleIDs = new TargetModuleIDImpl[targets.length];
                int i = 0;
                for (TargetImpl ti : po.toTargetImpl(targets)) {
                    targetModuleIDs[i++] = new TargetModuleIDImpl(ti,moduleID);
                }
                
                po.setupForNormalExit(
                        localStrings.getLocalString(
                        "enterprise.deployment.client.deploy_application", 
                        "Deployment of application {0}",
                        moduleID), 
                    domain,
                    mainStatus,
                    targetModuleIDs);
            } else {
                po.setupForAbnormalExit(
                        localStrings.getLocalString(
                            "enterprise.deployment.client.deploy_application_failed", 
                            "Deployment of application failed - {0}",
                            mainStatus.getStageStatusMessage()),
                        domain,
                        mainStatus);
            }
            return po;

//            if(!checkStatusAndAddStage(
//                domain, null, 
//                localStrings.getString("enterprise.deployment.client.deploy_in_domain"),
//                dasConnection, tmp)) {
//                return;
//            }
//
//            //Take the one returned from the server
//            if (moduleID == null) {
//                moduleID = tmp.getProperty(DeploymentStatus.MODULE_ID);
//            }
//
//            String key = moduleID + DeploymentStatus.KEY_SEPARATOR + DeploymentStatus.MODULE_TYPE;
//            this.moduleType = ModuleType.getModuleType(
//                                (new Integer(tmp.getProperty(key))).intValue());
//
//            // Start keeping track of actions to be rolled back
//            RollBackAction rollback = new RollBackAction(RollBackAction.DEPLOY_OPERATION, moduleID, deployOptions);
//
//            // Deploy is done; create app ref if target[0] was not a domain
//            if(!(TargetType.DOMAIN.equals(targetList[0].getName()))) {
//                for(int i=0; i<targetList.length; i++) {
//                    
//                    // If this is a redeploy, set enable flag of options as per state of the app before redeploy
//                    if(deployedTargets != null) {
//                        dupOptions.put(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY, deployedTargets.get(targetList[i].getName()).toString());
//                    } else {
//                        dupOptions.put(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY, deployOptions.get(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY));
//                    }
//                    DeploymentStatus stat = 
//                        DeploymentClientUtils.createApplicationReference(
//                            dasConnection.getExistingMBeanServerConnection(),
//                            moduleID, targetList[i], dupOptions);
//                    if(!checkStatusAndAddStage(targetList[i], rollback, 
//                       localStrings.getString("enterprise.deployment.client.deploy_create_ref", targetList[i].getName()), dasConnection, stat)) {
//                        return;
//                    }
//                    rollback.addTarget(targetList[i], rollback.APP_REF_CREATED);
//
//                    // Start the apps only if enable is true; if this is a redeploy, then check what was the
//                    // state before redeploy
//                    /*
//                      XXX Start the application regardless the value of "enable"
//                      Otherwise no DeployEvent would be sent to the listeners on 
//                      a remote instance, which would in turn synchronize the app
//                      bits.  Note that the synchronization is only called during
//                      applicationDeployed, not applicationEnabled.  To make sure
//                      the deployment code can work with both the new and the old
//                      mbeans, we will call the start for now (as the old mbeans
//                      would do).  The backend listeners are already enhanced to
//                      make sure the apps are not actually loaded unless the enable
//                      attributes are true for both the application and 
//                      application-ref elements.
//                    */
//                    if ((deployedTargets != null) && 
//                         (Boolean.FALSE.equals(deployedTargets.get(targetList[i].getName())))) {
//                             continue;
//                    }
//                    
//                    // We dont rollback for start failure because start failure may be because of server being down
//                    // We just add DeploymentStatus of this phase to the complete DeploymentStatus
//                    
//                    if (isRedeploy) {
//                        DeploymentClientUtils.setResourceOptions(
//                            deployOptions, 
//                            DeploymentProperties.RES_REDEPLOYMENT,
//                            targetList[i].getName());
//                    } else {
//                        DeploymentClientUtils.setResourceOptions(
//                            deployOptions, 
//                            DeploymentProperties.RES_DEPLOYMENT,
//                            targetList[i].getName());
//                    }
//
//                    stat = DeploymentClientUtils.startApplication(
//                            dasConnection.getExistingMBeanServerConnection(),
//                            moduleID, targetList[i], deployOptions);
//                    checkStatusAndAddStage(targetList[i], null, 
//                        localStrings.getString("enterprise.deployment.client.deploy_start", targetList[i].getName()), dasConnection, stat, true);
//                }
//            }
//
//            // Do WSDL publishing only if the caller is not GUI
//            if ( !isLocalConnectionSource ) {
//                try {
//                    DeploymentClientUtils.doWsdlFilePublishing(tmp, dasConnection);
//                } catch (Exception wsdlEx) {
//                    DeploymentStatus newStatus = new DeploymentStatus();
//                    newStatus.setStageStatus(DeploymentStatus.FAILURE);
//                    newStatus.setStageStatusMessage(wsdlEx.getMessage());
//                    newStatus.setStageException(wsdlEx);
//                    checkStatusAndAddStage(domain, rollback, 
//                        localStrings.getString("enterprise.deployment.client.deploy_publish_wsdl"), dasConnection, newStatus);
//                    String msg =  localStrings.getString("enterprise.deployment.client.deploy_publish_wsdl_exception", wsdlEx.getMessage());
//                    setupForAbnormalExit(msg, domain);
//                    return;
//                }
//            }
//            
//            initializeTargetModuleIDForAllServers(
//                    tmp, dasConnection.getMBeanServerConnection(false));

        } catch (Throwable ioex) {
            po.setupForAbnormalExit(
                    localStrings.getLocalString(
                    "enterprise.deployment.client.deploy_application_failed", 
                    "Deployment of application failed - {0} ",
                    ioex.toString()),
                domain,
                ioex);
            return po;
        }

    }

    public DFProgressObject undeploy(Target[] targets, String moduleID) {
        return undeploy(targets, moduleID, null);
    }

    public DFProgressObject undeploy(Target[] targets, String moduleID,
                                     Map undeploymentOptions) {
        ensureConnected();
        targets = prepareTargets(targets);
        ProgressObjectImpl po = new ProgressObjectImpl(targets);
        try {

            String[] commandArgs = prepareRemoteCommandArguments(
                    "undeploy", 
                    undeploymentOptions, 
                    new String[] {moduleID}
                    );
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            remoteCommand.handleRemoteCommand(commandArgs, "xml-cli", baos);
            DFDeploymentStatus ds = CommandXMLResultParser.parse(new ByteArrayInputStream(baos.toByteArray()));
            DFDeploymentStatus mainStatus = ds.getMainStatus();
            if (mainStatus.getStatus() != DFDeploymentStatus.Status.FAILURE) {
                TargetModuleIDImpl[] targetModuleIDs = new TargetModuleIDImpl[targets.length];
                int i = 0;
                for (TargetImpl ti : po.toTargetImpl(targets)) {
                    targetModuleIDs[i++] = new TargetModuleIDImpl(ti,moduleID);
                }
                
                po.setupForNormalExit(
                        localStrings.getLocalString(
                        "enterprise.deployment.client.undeploy_application",
                        "Undeployment of application {0}", 
                        moduleID), 
                    domain,
                    mainStatus,
                    targetModuleIDs);
            } else {
                po.setupForAbnormalExit(
                        localStrings.getLocalString(
                            "enterprise.deployment.client.undeploy_application_failed",
                            "Undeployment failed - {0} ",
                            mainStatus.getStageStatusMessage()),
                        domain,
                        mainStatus);
            }
            return po;


        } catch (Throwable ioex) {
            po.setupForAbnormalExit(
                    localStrings.getLocalString(
                        "enterprise.deployment.client.undeploy_application_failed",
                        "Undeployment failed - {0} ",
                    ioex.toString()), 
                domain,
                ioex);
            return po;
        }        
    }

    public DFProgressObject enable(Target[] targets, String moduleID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DFProgressObject disable(Target[] targets, String moduleID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DFProgressObject createAppRef(Target[] targets, String moduleID,
                                         Map options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DFProgressObject deleteAppRef(Target[] targets, String moduleID,
                                         Map options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TargetModuleID[] listAppRefs(String[] targets) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String downloadFile(File location, String moduleID, String moduleURI) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DFDeploymentStatus waitFor(DFProgressObject po) {
        return po.waitFor();
    }

    public Target[] createTargets(String[] targets) {
        TargetImpl[] result = new TargetImpl[targets.length];
        int i = 0;
        for (String name : targets) {
            result[i++] = new TargetImpl(this, name, "");
        }
        return result;
    }

    public Target createTarget(String name) {
        return new TargetImpl(this, name, "");
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWebURL(TargetModuleID tmid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void ensureConnected() {
        if ( ! isConnected()) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "enterprise.deployment.client.disconnected_state",
                    "Not connected to the Domain Admin Server"));
        }
    }
    
    /**
     * Assembles an argument list suitable for use by RemoteCommand from the
     * command, options, and operand.
     * @param commandName the command to execute
     * @param options Map, with each key an option name and each value (optionally) the corresponding option value
     * @param operands the operands to the command
     * @return argument list for RemoteCommand
     */
    protected String[] prepareRemoteCommandArguments(
            String commandName,
            Map<String,String> options,
            String[] operands) {
        
        ArrayList<String> result = new ArrayList<String>();
        result.add(commandName);
        if (options == null) {
            options = new HashMap<String,String>();
        }
        for (Map.Entry<String,String> entry : options.entrySet()) {
            result.add("--" + entry.getKey() + "=" + entry.getValue());
        }
        /*
         * Add the authentication information from the 
         * caller-provided connection identifier.
         */
        if (targetDAS.isSecure()) {
            result.add("--secure");
        }
        result.add("--host=" + targetDAS.getHostName());
        result.add("--port=" + targetDAS.getHostPort());
        result.add("--user=" + targetDAS.getUserName());
        /*
         * If we were typing the passwordfile option we would need to enclose
         * the name in quote marks in case it had embedded spaces.  Because
         * we are essentially doing the shell's command-line parsing ourselves
         * by placing options and their values into the argument list we do not
         * need to do the quoting.  If we did then the quote marks would be
         * treated as part of the file spec and the file would not be found.
         */
        result.add("--passwordfile=" + passwordFile.getAbsolutePath());
        
        for (String o : operands) {
            result.add(o);
        }
        return result.toArray(new String[result.size()]);
    }
    
    private File preparePasswordFile() {
        File pwFile = null;
        try {
            pwFile = File.createTempFile("rdf", ".dat");
            PrintStream ps = new PrintStream(pwFile);
            ps.println("AS_ADMIN_PASSWORD=" + targetDAS.getPassword());
            ps.close();
            return pwFile;
        } catch (IOException ex) {
            if (pwFile != null) {
                pwFile.delete();
            }
            throw new RuntimeException(ex);
        }
    }
    
    private Target[] prepareTargets(Target[] targets) {
        if (targets == null || targets.length == 0) {
            targets = new Target[] {targetForDefaultServer()};
        }
        return targets;
    }
    
    /**
     * Provides a {@link Target} object for the default target.
     * 
     * @return Target for the default server
     */
    private Target targetForDefaultServer() {
        TargetImpl t = new TargetImpl(this, DEFAULT_SERVER_NAME, 
                localStrings.getLocalString(
                    "enterprise.deployment.client.default_server_description",
                    "default server"));
        return t;
    }
    
}
