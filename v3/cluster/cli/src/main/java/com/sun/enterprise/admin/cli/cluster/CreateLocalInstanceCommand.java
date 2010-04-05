/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import java.io.File;
import java.io.Console;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *  This is a local command that creates a local instance.
 */
@Service(name = "create-local-instance")
@Scoped(PerLookup.class)
public final class CreateLocalInstanceCommand extends CLICommand {

    @Param(name = "nodeagent", optional = true)
    private String nodeAgent;

    @Param(name = "agentdir", optional = true)
    private String agentDir;

    @Param(name = "agentport", optional = true)
    private String agentPort;

    @Param(name = "agentproperties", optional = true, separator = ':')
    private String agentProperties;     // XXX - should it be a Properties?

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    private boolean saveMasterPassword = false;

    @Param(name = "filesystemonly", optional = true, defaultValue = "false")
    private boolean filesystemOnly = false;

    @Param(name = "config", optional = true)
    private String configName;

    @Param(name = "cluster", optional = true)
    private String clusterName;

    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;     // XXX - should it be a Properties?

    @Param(name = "instance_name", primary = true)
    private String instanceName;

    private File agentsDir;           // the parent dir of all node agents
    private File nodeAgentDir;        // the specific node agent dir
    private File instanceDir;         // the specific instance dir

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateLocalInstanceCommand.class);

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {
        if (configName != null && clusterName != null)
            throw new CommandValidationException(
                                        strings.get("ConfigClusterConflict"));
        // XXX - is there a default for config_name?

        if (ok(agentDir)) {
            agentsDir = new File(agentDir);
        } else {
            String agentRoot = getSystemProperty(
                                SystemPropertyConstants.AGENT_ROOT_PROPERTY);
            // AS_DEF_NODEAGENTS_PATH might not be set on upgraded domains
            if (agentRoot != null)
                agentsDir = new File(agentRoot);
            else
                agentsDir = new File(new File(getSystemProperty(
                                SystemPropertyConstants.INSTALL_ROOT_PROPERTY)),
                                "nodeagents");
        }

        if (!agentsDir.isDirectory()) {
            throw new CommandException(
                    strings.get("Instance.badAgentDir", agentsDir));
        }

        if (nodeAgent != null) {
            nodeAgentDir = new File(agentsDir, nodeAgent);
        } else {
            // XXX - find the existing agent, if it exists,
            // or create a default agent if none exists
            //nodeAgentDir = getTheOneAndOnlyAgent(agentsDir);
            nodeAgent = nodeAgentDir.getName();
        }

        instanceDir = new File(nodeAgentDir, instanceName);

        nodeAgentDir = SmartFile.sanitize(nodeAgentDir);
        instanceDir = SmartFile.sanitize(instanceDir);

        // XXX - validate lots more...
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        throw new CommandException("Not implemented");
    }
}
