/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.universal.process.LocalAdminCommand;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.jvnet.hk2.annotations.*;
import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import java.util.logging.Logger;
import java.io.IOException;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;

/**
 * Remote AdminCommand to delete an instance.  This command is run only on DAS.
 *
 * @author Jennifer Chou
 */
@Service(name = "delete-instance")
@I18n("delete.instance")
@Scoped(PerLookup.class)
@ExecuteOn({RuntimeType.DAS})
public class DeleteInstanceCommand implements AdminCommand, PostConstruct {

    private static final String DEFAULT_NODE = "localhost";
    private static final String LOCAL_HOST = "localhost";
    private static final String NL = System.getProperty("line.separator");

    @Inject
    private CommandRunner cr;

    @Inject
    Habitat habitat;

    @Inject
    Node[] nodeList;

    @Inject
    private Nodes nodes;

    @Param(name = "instance_name", primary = true)
    private String instanceName;

    private Server instance;
    private String noderef;
    private String nodedir;
    private String installdir;
    private Logger logger;    
    private AdminCommandContext ctx;
    private RemoteInstanceCommandHelper helper;
    private RemoteConnectHelper rch;
    private String instanceHost;
    private String dasHost;
    private int dasPort;
    private Node theNode = null;
    private StringBuilder humanVersionOfCommand = new StringBuilder();


    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        ctx = context;
        logger = context.logger;
        String msg = "";
        boolean  fsfailure = false;
        boolean  configfailure = false;

        // We are going to delete a server instance. Get the instance
        instance = helper.getServer(instanceName);

        if (instance == null) {
            msg = Strings.get("start.instance.noSuchInstance", instanceName);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        instanceHost = instance.getAdminHost();
        dasPort = helper.getAdminPort(SystemPropertyConstants.DAS_SERVER_NAME);
        dasHost = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);

        // make sure instance is not running.
        if (instance.isRunning()){
            msg = Strings.get("instance.shutdown", instanceName);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // We attempt to delete the instance filesystem first by running
        // _delete-instance-filesystem. We then remove the instance
        // from the config no matter if we could delete the files or not.

        // Get the name of the node from the instance's node-ref field
        noderef = helper.getNode(instance);
        if(!StringUtils.ok(noderef)) {
            msg = Strings.get("missingNodeRef", instanceName);
            fsfailure = true;
        } else {
            theNode = nodes.getNode(noderef);
            if (theNode == null) {
                msg = Strings.get("noSuchNode", noderef);
                fsfailure = true;
            }
        }

        if (!fsfailure) {
            nodedir = theNode.getNodeDir();
            installdir = theNode.getInstallDir();
            try {
                // Delete the instance files
                deleteInstanceFilesystem();
            } catch (IOException ex) {
                msg = ex.getMessage();
                fsfailure = true;
            }
        }

        // Now remove the instance from domain.xml.
        CommandInvocation ci = cr.getCommandInvocation("_unregister-instance", report);
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", instanceName);
        ci.parameters(map);
        ci.execute();

        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            // Failed to delete from domain.xml
            configfailure = true;
            if (fsfailure) {
                // Failed to delete instance from fs too
                msg = msg + NL + report.getMessage();
            } else {
                msg = report.getMessage();
            }
        }

        // OK, try to give a helpful message depending on the failure
        if (configfailure && fsfailure) {
            msg = msg + NL + NL + Strings.get("delete.instance.failed",
                    instanceName, instanceHost);
        } else if (configfailure && !fsfailure) {
            msg = msg + NL + NL + Strings.get("delete.instance.config.failed",
                    instanceName, instanceHost);
        } else if (!configfailure && fsfailure) {
            msg = msg + NL + NL + Strings.get("delete.instance.filesystem.failed",
                    instanceName, noderef, instanceHost,
                    humanVersionOfCommand);
        }

        if (configfailure || fsfailure) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
        }
    }

     private void deleteInstanceRemote() throws IOException {

         StringBuilder output = new StringBuilder();
         ParameterMap map = new ParameterMap();
         map.add("--node", noderef);
         if (nodedir != null) {
            map.add("--nodedir", nodedir);
            humanVersionOfCommand.append(" --nodedir " + nodedir);
         }
         map.add("DEFAULT", instanceName);
         humanVersionOfCommand.append(" " + instanceName);

         // Run the command remotely (over SSH)
         try {
             int status = rch.runCommand(noderef, "_delete-instance-filesystem",
                         map, output);
             if (output.length() > 0) {
                 logger.info(output.toString());
             }
             if (status != 0){
                String msg2 = Strings.get("node.command.failed", noderef,
                        instanceHost, output.toString(), rch.getLastCommandRun());
                logger.warning(msg2);
                throw new IOException(msg2);
             }
         } catch (SSHCommandExecutionException ec)  {
            String msg2 = Strings.get("node.ssh.bad.connect",
                noderef, instanceHost, ec.getMessage());
            // Log some extra info
            String msg1 = Strings.get("node.command.failed.ssh.details",
                    noderef, instanceHost, ec.getCommandRun(), ec.getMessage(),  
                    ec.getSSHSettings());
            logger.warning(msg1);
            throw new IOException(msg2, ec);
         }
     }

    private void deleteInstanceFilesystem() throws IOException {

        rch = new RemoteConnectHelper(habitat, nodeList, logger, dasHost, dasPort);

        String msg;
        humanVersionOfCommand.append("asadmin" +
                " --host "+ dasHost + " --port " + dasPort +
                " delete-local-instance " + " --node " + noderef);

        // Check if the node is local. If so we can just execute
        // the command via ProcessManager.
        if (rch.isLocalhost(nodes.getNode(noderef))) {
            // Run local admin command
            LocalAdminCommand lac = null;
            if (nodedir == null) {
                lac = new LocalAdminCommand("_delete-instance-filesystem",
                        "--node", noderef, instanceName);
            } else {
                lac = new LocalAdminCommand("_delete-instance-filesystem",
                        "--node", noderef, "--nodedir", nodedir, instanceName);
                humanVersionOfCommand.append("--nodedir " + nodedir);
            }
            humanVersionOfCommand.append(" " + instanceName);
            msg = Strings.get("deletingInstance", instanceName, LOCAL_HOST);
            logger.info(msg);
            try {
                int status = lac.execute();
                if (status != 0) {
                    // XXX need the commands output from lac, for now just
                    // display status code
                    msg = Strings.get("nonzero.status",
                            "asadmin _delete-instance-filesystem",
                            "localhost");
                    logger.warning(msg);
                    throw new IOException(msg);
                }
            } catch (ProcessManagerException ex)  {
                String msg2 = Strings.get("node.command.failed",
                        noderef, instanceHost, "_delete-instance-filesystem",
                        ex.getMessage());
                throw new IOException(msg2, ex);
            }
        } else if (rch.isRemoteConnectRequired(noderef)) {
            // Need to go remote.
            msg = Strings.get("deletingInstance", instanceName, noderef);
            logger.info(msg);
            deleteInstanceRemote();
        } else {
            String msg2= Strings.get("node.not.ssh", noderef, instanceHost);
            logger.warning(msg2);
            throw new IOException(msg2);
        }
    }
}
