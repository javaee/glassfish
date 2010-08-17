/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.universal.process.LocalAdminCommand;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import java.util.logging.Logger;

/**
 * Remote AdminCommand to create an instance.  This command is run only on DAS.
 *  1. Register the instance on DAS
 *  2. Create the file system on the instance node via ssh, node agent, or other
 *
 * @author Jennifer Chou
 */
@Service(name = "create-instance")
@I18n("create.instance")
@Scoped(PerLookup.class)
@Cluster({RuntimeType.DAS})
public class CreateInstanceCommand implements AdminCommand, PostConstruct  {

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

    @Param(name="node", alias="nodeagent")
    String node;

    @Param(name = "config", optional=true)
    String configRef;

    @Param(name="cluster", optional=true)
    String clusterName;

    @Param(name="lbenabled", optional=true, defaultValue=ServerRef.LBENABLED_DEFAULT_VALUE)
    private Boolean lbEnabled;

    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;

    @Param(name = "instance_name", primary = true)
    private String instance;

    private Logger logger;
    private AdminCommandContext ctx;
    private RemoteInstanceCommandHelper helper;
    private RemoteConnectHelper rch;
    private String nodeHost = null;
    private String nodeDir = null;
    private int dasPort;
    private String dasHost;


    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        ctx = context;
        logger = context.logger;

        // Make sure Node is valid
        if (nodes.getNode(node) == null) {
            String msg = Strings.get("noSuchNode", node);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        nodeHost = getHostFromNodeName(node);
        nodeDir = getNodeDir(node);

        // First, update domain.xml by calling _register-instance

        CommandInvocation ci = cr.getCommandInvocation("_register-instance", report);
        ParameterMap map = new ParameterMap();
        map.add("node", node);
        map.add("config", configRef);
        map.add("cluster", clusterName);
        if(lbEnabled != null){
            map.add("lbenabled", lbEnabled.toString());
        }
        map.add("systemproperties", systemProperties);
        map.add("DEFAULT", instance);
        ci.parameters(map);
        ci.execute();

        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            // If we couldn't update domain.xml then stop!
            return;
        }

        dasPort = helper.getAdminPort(SystemPropertyConstants.DAS_SERVER_NAME);
        dasHost = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        rch = new RemoteConnectHelper(habitat, nodeList, logger, dasHost, dasPort);
        String msg;
        // What we tell humans to run if we fail. Local version
        String humanVersionOfCommand = "asadmin " + " create-local-instance " +
                    "--host "+ dasHost + " --node " + node + " " + instance;

//        if (node == null || !rch.isRemoteConnectRequired(node)) {
        if (rch.isLocalhost(nodes.getNode(node))) {
            LocalAdminCommand lac = null;
            if (nodeDir == null) {
                lac = new LocalAdminCommand("_create-instance-filesystem", "--node", node, instance);
            } else {
                lac = new LocalAdminCommand("_create-instance-filesystem", "--node", node, "--nodedir", nodeDir, instance);
            }
            msg = Strings.get("creatingInstance", instance, LOCAL_HOST);
            logger.info(msg);
            try {
                 int status = lac.execute();
            }   catch (ProcessManagerException ex)  {
                msg = Strings.get("create.instance.remote.failed",
                        instance, node, nodeHost, humanVersionOfCommand );
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        } else if (rch.isRemoteConnectRequired(node)) {
            msg = Strings.get("creatingInstance", instance, node);
            logger.info(msg);
            int status =createInstanceRemote();

        } else {
            msg= Strings.get("create.instance.remote.notssh", instance, node, nodeHost, humanVersionOfCommand);
            logger.warning(msg);
            msg = Strings.get("create.instance.remote.notssh",
                    instance, node, nodeHost, humanVersionOfCommand );
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;            
        }

    }

    /**
     * Given a node name return the node's host name
     * @param nodeName  name of node
     * @return  node's host name, or "localhost" if can't find the host name
     */
    private String getHostFromNodeName(String nodeName) {
        Node theNode = nodes.getNode(nodeName);
        String hostName = null;

        if (theNode != null) {
            hostName = theNode.getNodeHost();
        }

        if (hostName == null && nodeName.equals(LOCAL_HOST)) {
            return LOCAL_HOST;
        } else {
            return hostName;
        }
    }

     /**
     * Given a node name return the node's node-dir
     * @param nodeName  name of node
     * @return  node's node-dir, or null if not specified
     */
    private String getNodeDir(String nodeName) {
        Node theNode = nodes.getNode(nodeName);
        String dir = null;
        if (theNode != null) {
            dir = theNode.getNodeDir();
        }
        return dir;
    }

    private int createInstanceRemote() {

        ActionReport report = ctx.getActionReport();
            StringBuilder output = new StringBuilder();
            ParameterMap map = new ParameterMap();
            map.set("--node", node);
            if (nodeDir != null) {
                map.set("--nodedir", nodeDir);
            }
            map.set("DEFAULT", instance);

            // What we tell humans to run if we fail
            String humanVersionOfCommand = "asadmin --host " + dasHost +
                    " --port " + dasPort + " create-local-instance" +
                    " --node " + node + " " + instance;
        try {
                
            int status = rch.runCommand(node, "_create-instance-filesystem",
                        map, output);
            if (output.length() > 0) {
                logger.info(output.toString());
            }
            if (status != 0){
                String msg = Strings.get("create.instance.remote.failed",
                        instance, node, nodeHost, humanVersionOfCommand);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(output.toString() + NL + msg);
                return 1;
            }
        } catch (SSHCommandExecutionException ec )  {
            String msg = Strings.get("create.instance.ssh.failed", instance, ec.getSSHSettings(), ec.getMessage(), nodeHost, humanVersionOfCommand);
            logger.severe(msg);
            msg = Strings.get("create.instance.ssh.failed",
                        instance, ec.getSSHSettings(), ec.getMessage(), nodeHost, humanVersionOfCommand);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return 1;
        }
        return 0;
    }

}
