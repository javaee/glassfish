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
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import java.util.logging.Logger;
import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;


/**
 * Remote AdminCommand to create a config node.  This command is run only on DAS.
 *  Register the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "delete-node-config")
@I18n("delete.node.config")
@Scoped(PerLookup.class)
@ExecuteOn({RuntimeType.DAS})
public class DeleteNodeConfigCommand implements AdminCommand, PostConstruct {
    @Inject
    Habitat habitat;

    @Inject
    Node[] nodeList;

    @Inject
    Nodes nodes;

    @Inject
    private CommandRunner cr;

    @Param(name="name", primary = true)
    String name;
    private RemoteInstanceCommandHelper helper;

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }        

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        RemoteConnectHelper rch;
        Logger logger = context.logger;

        if (nodes.getNode(name) == null) {
            //no node to delete  nothing to do here
            String msg = Strings.get("noSuchNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        int dasPort = helper.getAdminPort(SystemPropertyConstants.DAS_SERVER_NAME);
        String dasHost = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        rch = new RemoteConnectHelper(habitat, nodeList, logger, dasHost, dasPort);

        /*
         *
         * We don't have a robust way to differentiate between a "config"
         * node and an ssh node -- see bug 12694. So for now we don't
         * check which means delete-node-config can delete a node
         * of any type.
        if (!isConfigNode(name)) {
            String msg = Strings.get("notConfigNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
         */
        
        // for now delete-node-ssh deletes all types of nodes so can call it.  that needs to be fixed.
        CommandInvocation ci = cr.getCommandInvocation("delete-node-ssh", report);
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        ci.parameters(map);
        ci.execute();
    }

    private boolean isConfigNode(String nodeName) {

        if (! StringUtils.ok(nodeName)) {
            return false;
        }

        Node node = nodes.getNode(name);
        if (node == null) {
            return false;
        }

        // If the node has no node-host and no install-dir then consider
        // it a config node
        if (! StringUtils.ok(node.getNodeHost()) &&
            ! StringUtils.ok(node.getInstallDir())) {
            return true;
        } else {
            return false;
        }
    }

}
