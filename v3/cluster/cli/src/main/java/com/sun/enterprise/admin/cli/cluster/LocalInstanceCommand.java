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

import com.sun.enterprise.util.io.InstanceDirs;
import java.io.*;
import java.util.*;

import org.glassfish.api.Param;
import org.glassfish.api.admin.*;

import com.sun.enterprise.admin.cli.LocalServerCommand;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;

/**
 * A base class for local commands that manage a local server instance.
 */
public abstract class LocalInstanceCommand extends LocalServerCommand{

    @Param(name = "nodeagent", optional = true)
    protected String nodeAgent;

    @Param(name = "agentdir", optional = true)
    protected String agentDir;

    // subclasses decide whether it's optional, required, or not allowed
    //@Param(name = "instance_name", primary = true, optional = true)
    protected String instanceName;

    protected File agentsDir;           // the parent dir of all node agents
    protected File nodeAgentDir;        // the specific node agent dir
    protected File instanceDir;         // the specific instance dir
    protected File dasProperties;       // the das.properties file

    private InstanceDirs instanceDirs;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(LocalInstanceCommand.class);

    @Override
    protected void validate()
                        throws CommandException, CommandValidationException {
        initInstance();
    }

    protected void initInstance() throws CommandException {
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

            // TODO -- agentDir might be set to null
        }

        agentsDir.mkdirs();

        if (!agentsDir.isDirectory()) {
            throw new CommandException(
                    strings.get("Instance.badAgentDir", agentsDir));
        }

        if (nodeAgent != null) {
            nodeAgentDir = new File(agentsDir, nodeAgent);
        } else {
            nodeAgentDir = getTheOneAndOnlyAgent(agentsDir);
            nodeAgent = nodeAgentDir.getName();
        }

        if (instanceName != null) {
            instanceDir = new File(nodeAgentDir, instanceName);
        } else {
            instanceDir = getTheOneAndOnlyInstance(nodeAgentDir);
            instanceName = instanceDir.getName();
        }

        if (!instanceDir.isDirectory()) {
            throw new CommandException(
                    strings.get("Instance.badInstanceDir", instanceDir));
        }
        nodeAgentDir = SmartFile.sanitize(nodeAgentDir);
        instanceDir = SmartFile.sanitize(instanceDir);

        try {
           instanceDirs = new InstanceDirs(instanceDir);
           setServerDirs(instanceDirs.getServerDirs());
           //setServerDirs(instanceDirs.getServerDirs(), checkForSpecialFiles());
        }
        catch(IOException e) {
            throw new CommandException(e);
        }

        setDasDefaults();
        logger.printDebugMessage("nodeAgentDir: " + nodeAgentDir);
        logger.printDebugMessage("instanceDir: " + instanceDir);
        logger.printDebugMessage("dasProperties: " + dasProperties);
    }

    protected final InstanceDirs getInstanceDirs() {
        return instanceDirs;
    }

    private File getTheOneAndOnlyAgent(File parent) throws CommandException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new CommandException(
                    strings.get("Agent.noAgentDirs", parent));
        }

        if (files.length > 1) {
            throw new CommandException(
                    strings.get("Agent.tooManyAgentDirs", parent));
        }

        return files[0];
    }

    private File getTheOneAndOnlyInstance(File parent) throws CommandException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new CommandException(
                    strings.get("Instance.noInstanceDirs", parent));
        }

        // expect two - the "agent" directory and the instance directory
        if (files.length > 2) {
            throw new CommandException(
                    strings.get("Instance.tooManyInstanceDirs", parent));
        }

        for (File f : files) {
            if (!f.getName().equals("agent"))
                return f;
        }
        throw new CommandException(
                strings.get("Instance.noInstanceDirs", parent));
    }

    protected void setDasDefaults() throws CommandException {
        // TODO
        // TODO
        // make sure this works!
        dasProperties = new File(new File(new File(instanceDirs.getNodeAgentDir(), "agent"),
                                                "config"), "das.properties");

        if (!dasProperties.exists())
            return;

        Properties dasprops = new Properties();
        FileInputStream fis = null;
        try {
            // read properties and set them in programOptions
            // properties are:
            // agent.das.port
            // agent.das.host
            // agent.das.isSecure
            // agent.das.user           XXX - not in v2?
            fis = new FileInputStream(dasProperties);
            dasprops.load(fis);
            String p;
            p = dasprops.getProperty("agent.das.host");
            if (p != null)
                programOpts.setHost(p);
            p = dasprops.getProperty("agent.das.port");
            if (p != null)
                programOpts.setPort(Integer.parseInt(p));
            p = dasprops.getProperty("agent.das.isSecure");
            if (p != null)
                programOpts.setSecure(Boolean.parseBoolean(p));
            p = dasprops.getProperty("agent.das.user");
            if (p != null)
                programOpts.setUser(p);
            // XXX - what about the DAS admin password?
        } catch (IOException ioex) {
            throw new CommandException(
                        strings.get("Instance.cantReadDasProperties",
                                    dasProperties.getPath()));
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException cex) {
                    // ignore it
                }
            }
        }
    }
}
