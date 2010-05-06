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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.logging.LogDomains;


/**
 *  This is a local command that creates a local instance.
 *  Create the local directory structure
 *  nodeagents/<host-name>/
 *   || ---- agent
 *             || ---- config
 *                     | ---- das.properties
 *   || ---- <server-instance-1>
 *             ||---- config
 *             ||---- applications
 *             ||---- java-web-start
 *             ||---- generated
 *             ||---- lib
 *             ||---- docroot
 *   || ---- <server-instance-2>
 *
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
    private File agentConfigDir;
    private File applicationsDir;
    private File configDir;
    private File generatedDir;
    private File libDir;
    private File docrootDir;

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

        // nodeagents
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

        //if (!agentsDir.isDirectory()) {
        //    throw new CommandException(
        //            strings.get("Instance.badAgentDir", agentsDir));
        //}

        // nodeagents\<hostname>
        if (nodeAgent != null) {
            nodeAgentDir = new File(agentsDir, nodeAgent);
        } else {
            // XXX - find the existing agent, if it exists,
            // or create a default agent if none exists
            //nodeAgentDir = getTheOneAndOnlyAgent(agentsDir);
            String hostName = "hostname";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                Logger.getLogger(CreateLocalInstanceCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            nodeAgentDir = new File(agentsDir, hostName);
           
            nodeAgent = nodeAgentDir.getName();
        }

        // nodeagents\<host name>\agent\config
        String agentPath = "agent" + File.separator + "config";
        agentConfigDir = new File(nodeAgentDir, agentPath);

        // nodeagents\<host name>\<server instance>
        instanceDir = new File(nodeAgentDir, instanceName);
        applicationsDir = new File(instanceDir, "applications");
        configDir = new File(instanceDir, "config");
        generatedDir = new File(instanceDir, "generated");
        libDir = new File(instanceDir, "lib");
        docrootDir = new File(instanceDir, "docroot");


        agentsDir = SmartFile.sanitize(agentsDir);
        nodeAgentDir = SmartFile.sanitize(nodeAgentDir);
        instanceDir = SmartFile.sanitize(instanceDir);
        applicationsDir = SmartFile.sanitize(applicationsDir);
        configDir = SmartFile.sanitize(configDir);
        generatedDir = SmartFile.sanitize(generatedDir);
        libDir = SmartFile.sanitize(libDir);
        docrootDir = SmartFile.sanitize(docrootDir);

        // XXX - validate lots more...
        
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        
            if (!this.filesystemOnly) {
               registerToDAS();
            }

            return createDirectories();
    }

    private int registerToDAS() throws CommandException {
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add(0, "create-instance");
        if (clusterName != null) {
            argsList.add("--cluster");
            argsList.add(clusterName);
        }
        if (configName != null) {
            argsList.add("--config");
            argsList.add(configName);
        }
        if (nodeAgent != null) {
            argsList.add("--nodeagent");
            argsList.add(nodeAgent);
        }
        if (systemProperties != null) {
            argsList.add("--systemproperties");
            argsList.add(systemProperties);
        }
        argsList.add(this.instanceName);

        String[] argsArray = new String[argsList.size()];
        argsArray = argsList.toArray(argsArray);

        Environment currEnv = new Environment();
        ProgramOptions po = new ProgramOptions(currEnv);
        RemoteCommand rc = new RemoteCommand("create-instance", po, currEnv);
        return rc.execute(argsArray);
    }

    private int createDirectories() throws CommandException {
        boolean createDirsComplete = false;
        File badfile = null;
        while (badfile == null && !createDirsComplete) {
            if (!agentsDir.isDirectory()) {
                if (!agentsDir.mkdir()) {
                    badfile = agentsDir;
                }
            }
            if (!nodeAgentDir.isDirectory()) {
                if (!nodeAgentDir.mkdir()) {
                    badfile = nodeAgentDir;
                }
            }
            if (!agentConfigDir.isDirectory()) {
                if (!agentConfigDir.mkdirs()) {
                    badfile = agentConfigDir;
                }
            }
            if (!instanceDir.isDirectory()) {
                if (!instanceDir.mkdir())
                    badfile = instanceDir;
                if (!applicationsDir.mkdir())
                    badfile = applicationsDir;
                if (!configDir.mkdir())
                    badfile = configDir;
                if (!generatedDir.mkdir())
                    badfile = generatedDir;
                if (!libDir.mkdir())
                    badfile = libDir;
                if (!docrootDir.mkdir())
                    badfile = docrootDir;
            } else {
                if (!instanceDir.isDirectory()) {
                    if (!instanceDir.mkdir())
                        badfile = instanceDir;
                }
                if (!applicationsDir.isDirectory()) {
                    if (!applicationsDir.mkdir())
                        badfile = applicationsDir;
                }
                if (!configDir.isDirectory()) {
                    if (!configDir.mkdir())
                        badfile = configDir;
                }
                if (!generatedDir.isDirectory()) {
                    if (!generatedDir.mkdir())
                        badfile = generatedDir;
                }
                if (!libDir.isDirectory()) {
                    if (!libDir.mkdir())
                        badfile = libDir;
                }
                if (!docrootDir.isDirectory()) {
                    if (!docrootDir.mkdir())
                        badfile = docrootDir;
                }
            }
            createDirsComplete = true;
        }
        if (badfile != null) {
            throw new CommandException(strings.get("Instance.cannotMkDir", badfile));
        }
        return SUCCESS;
    }
}
