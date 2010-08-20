/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;

import com.sun.enterprise.universal.process.LocalAdminCommand;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;


/**
 * AdminCommand to start the instance
 * server.
 * If this is DAS -- we call the instance
 * If this is an instance we start it
 *
 * @author Carla Mott
 */
@Service(name = "start-instance")
@Scoped(PerLookup.class)
@I18n("start.instance.command")
public class StartInstanceCommand implements AdminCommand, PostConstruct {
    @Inject
    Habitat habitat;

    @Inject
    Node[] nodeList;

    @Inject
    private Nodes nodes;

    @Inject
    private CommandRunner cr;

    @Inject
    private ServerEnvironment env;
    
    @Param(optional = true, primary = true)
    private String instanceName;

    @Param(optional = true)
    private boolean fullsync;

    @Param(optional = true, defaultValue = "false")
    private boolean nosync;

    @Param(optional = true, defaultValue = "false")
    private boolean debug;

    @Param(optional = true, obsolete = true)
    private String setenv;

    private Logger logger;
    private RemoteInstanceCommandHelper helper;

    private AdminCommandContext ctx;
    private String noderef;
    private String nodedir;
    private String nodeHost;
    private Server instance;
    private String installDir = null;

    private static final String NL = System.getProperty("line.separator");

    @Override
    public void execute(AdminCommandContext context) {
        logger = context.getLogger();
        this.ctx=context;
        ActionReport report = ctx.getActionReport();
        String msg = "";
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

        if(!StringUtils.ok(instanceName)) {
            msg = Strings.get("start.instance.noInstanceName");
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }
        instance = helper.getServer(instanceName);
        if(instance == null) {
            msg = Strings.get("start.instance.noSuchInstance", instanceName);
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }
        noderef = helper.getNode(instance);
        if(!StringUtils.ok(noderef)) {
            msg = Strings.get("missingNodeRef", instanceName);
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }
        if (nodes != null) {
            Node n = nodes.getNode(noderef);
            if (n != null) {
                nodedir = n.getNodeDir();
                nodeHost = n.getNodeHost();
                installDir = n.getInstallDir();
            } else {
                msg = Strings.get("missingNode", noderef);
                logger.severe(msg);
                report.setMessage(msg);
                return;
            }
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        if(env.isDas()) {
            startInstance();
        } else {
            msg = Strings.get("start.instance.notAnInstanceOrDas",
                    env.getRuntimeType().toString());
            logger.severe(msg);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if (report.getActionExitCode() == ActionReport.ExitCode.SUCCESS) {
            // Make sure instance is really up
            String s = pollForLife();
            if (s != null) {
                report.setMessage(s);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
        }
    }

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    private void startLocalInstance()  {
        LocalAdminCommand lac = null;
        ArrayList<String> command = new ArrayList<String>();
        ActionReport report = ctx.getActionReport();

        command.add("--node");
        command.add(noderef);

        if (StringUtils.ok(nodedir)) {
            command.add("--nodedir");
            command.add(nodedir);
        }
        if (fullsync) {
            command.add("--fullsync");
        }
        if (nosync) {
            command.add("--nosync");
        }
        if (debug) {
            command.add("--debug");
        }

        command.add(instanceName);

        StringBuilder humanVersionOfCommand = new StringBuilder();
        for (String s : command) {
            humanVersionOfCommand.append(s);
            humanVersionOfCommand.append(" ");
        }

        String[] commandArray = new String[command.size()];
        lac = new LocalAdminCommand("start-local-instance",
                                    command.toArray(commandArray));

        try {
            lac.waitForReaderThreads(false);
            int status = lac.execute();
            if (status != 0) {
                String msg = Strings.get("nonzero.status",
                            humanVersionOfCommand,
                            "localhost");
                logger.warning(msg);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
        } catch (ProcessManagerException ex) {
            String msg = Strings.get("start.instance.failed",
                        instanceName, noderef, nodeHost );
            logger.warning(msg);
            String msg2 = Strings.get("node.command.failed",
                    noderef, nodeHost, humanVersionOfCommand,
                    ex.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg + NL + msg2);
        }
    }

    private void startInstance() {
        int dasPort = helper.getAdminPort(SystemPropertyConstants.DAS_SERVER_NAME);
        String dasHost = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        RemoteConnectHelper rch = new RemoteConnectHelper(habitat, nodeList,
                logger, dasHost, dasPort);
        ActionReport report = ctx.getActionReport();

        StringBuilder humanVersionOfCommand = new StringBuilder(
            "asadmin start-local-instance");

        if (rch.isLocalhost(nodes.getNode(noderef))) {
            startLocalInstance();
        } else if (rch.isRemoteConnectRequired(noderef)) {  // check if needs a remote connection
            // this command will run over ssh
            StringBuilder output = new StringBuilder();
            ParameterMap map = new ParameterMap();
            map.set("DEFAULT", instanceName);
            map.set("--node", noderef);
            humanVersionOfCommand.append(" --node " + noderef);
            if (nodedir != null) {
                map.set("--nodedir", nodedir);
                humanVersionOfCommand.append(" --nodedir ");
            }
            if (fullsync) {
                map.set("--fullsync", "true");
                humanVersionOfCommand.append(" --fullsync ");
            }
            if (nosync) {
                map.set("--nosync", "true");
                humanVersionOfCommand.append(" --nosync ");
            }
            if (debug) {
                map.set("--debug", "true");
                humanVersionOfCommand.append(" --debug ");
            }
            humanVersionOfCommand.append(" " + instanceName);
            try {
                int status = rch.runCommand(noderef, "start-local-instance",
                     map, output);
                if (output.length() > 0) {
                     logger.info(output.toString());
                }
                if (status != 0){
                    String msg1 = Strings.get("node.command.failed", noderef,
                        nodeHost, output.toString(), rch.getLastCommandRun());
                    logger.warning(msg1);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg1);
                }
            } catch (SSHCommandExecutionException ec) {
                String msg1 = Strings.get("start.instance.failed",
                        instanceName, noderef, nodeHost );
                String msg2 = Strings.get("node.ssh.bad.connect",
                        noderef, nodeHost, ec.getMessage());
                String msg3 = Strings.get("node.ssh.tocomplete",
                        nodeHost, installDir, humanVersionOfCommand);
                report.setMessage(msg1 + " " + msg2 + NL + NL + msg3);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                // Log some extra info
                msg1 = Strings.get("node.command.failed.ssh.details",
                    noderef, nodeHost, ec.getCommandRun(), ec.getMessage(),
                    ec.getSSHSettings());
                logger.warning(msg1);
            }
        } else {
            String msg1 = Strings.get("start.instance.failed",
                        instanceName, noderef, nodeHost );
            logger.warning(msg1);
            String msg2= Strings.get("node.not.ssh", noderef, nodeHost);
            logger.warning(msg2);
            String msg3 = Strings.get("node.ssh.tocomplete",
                        nodeHost, installDir, humanVersionOfCommand);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg1 + " " + msg2 + NL + NL + msg3);
        }
    }

    // return null means A-OK
    private String pollForLife() {
        int counter = 0;  // 120 seconds

        while (++counter < 240) {
            if (instance.isRunning())
                return null;

            try {
                Thread.sleep(500);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return Strings.get("start.instance.timeout", instanceName);
    }
}
