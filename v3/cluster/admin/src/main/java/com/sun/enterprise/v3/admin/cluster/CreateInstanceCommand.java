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

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.universal.process.ProcessManagerException;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.universal.process.LocalAdminCommand;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
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
public class CreateInstanceCommand implements AdminCommand {

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

    @Param(name="node", optional=true, defaultValue=DEFAULT_NODE)
    String node;

    @Param(name="nodeagent", optional=true)
    String nodeAgent;

    @Param(name = "config", optional=true)
    String configRef;

    @Param(name="cluster", optional=true)
    String clusterName;

    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;

    @Param(name = "instance_name", primary = true)
    private String instance;

    private Logger logger;
    private AdminCommandContext ctx;


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        ctx = context;
        logger = context.logger;

        if (nodes.getNode(node) == null) {
            String msg = Strings.get("noSuchNode", node);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // XXX dipol
        // We are transitioning from nodeAgent to node. Some parts of the code
        // still assume the server's node-agent-ref to be the hostname for the
        // host the server is on. If --nodeagent was not specified we use the
        // hostname from the node.
        // At some point we need to come back and remove all the nodeagent stuf
        if (nodeAgent == null || nodeAgent.length() == 0) {
            nodeAgent = getHostFromNodeName(node);
        }
        
        CommandInvocation ci = cr.getCommandInvocation("_register-instance", report);
        ParameterMap map = new ParameterMap();
        map.add("node", node);        
        map.add("nodeagent", nodeAgent);
        map.add("config", configRef);
        map.add("cluster", clusterName);
        map.add("systemproperties", systemProperties);
        map.add("DEFAULT", instance);
        ci.parameters(map);
        ci.execute();

        String msg;

        if (node == null || node.equals(LOCAL_HOST)) {
            LocalAdminCommand lac = new LocalAdminCommand("_create-instance-filesystem",instance);
            msg = Strings.get("creatingInstance", instance, LOCAL_HOST);
            logger.info(msg);
            try {
                 int status = lac.execute();
            }   catch (ProcessManagerException ex)  {
                msg = Strings.get("create.instance.failed", instance);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);                
            }

        } else {
            msg = Strings.get("creatingInstance", instance, node);
            logger.info(msg);
            createInstanceRemote();
        }
        
    }

    /**
     * Given a node name return the node's host name
     * @param nodeName  name of node
     * @return  node's host name, or "localhost" if can't find the host name
     */
    private String getHostFromNodeName(String nodeName) {

        Node theNode = nodes.getNode(nodeName);

        if (theNode == null) {
            return LOCAL_HOST;
        }
        String hostName = theNode.getNodeHost();
        if (hostName == null || hostName.length() == 0) {
            return LOCAL_HOST;
        }
        return hostName;
    }

    private void createInstanceRemote() {
            RemoteConnectHelper rch = new RemoteConnectHelper(habitat, nodeList, logger);
            ActionReport report = ctx.getActionReport();
            // check if needs a remote connection
            if (rch.isRemoteConnectRequired(node)) {
                // this command will run over ssh
                StringBuilder output = new StringBuilder();
                int status = rch.runCommand(node, "_create-instance-filesystem",
                        instance, output);
                if (output.length() > 0) {
                    logger.info(output.toString());
                }
                if (status != 1){
                    String msg = Strings.get("create.instance.failed", instance);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(output.toString() + NL + msg);
                }
            } else {
                // Could not reach the node via SSH
                String msg = Strings.get("mustRunLocal",
                        getHostFromNodeName(node), node,
                        "asadmin create-local-instance --filesystemonly " + instance);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
                report.setMessage(msg);
            }
    }

}
