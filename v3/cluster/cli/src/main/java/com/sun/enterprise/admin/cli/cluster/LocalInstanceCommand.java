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

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.InstanceDirs;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public abstract class LocalInstanceCommand extends LocalServerCommand {
    @Param(name = "nodeagent", optional = true)
    protected String nodeAgent;
    @Param(name = "nodehome", optional = true)
    protected String agentDir;
    @Param(name = "node", optional=true)
    protected String node;
    // subclasses decide whether it's optional, required, or not allowed
    //@Param(name = "instance_name", primary = true, optional = true)
    protected String instanceName;
    protected File agentsDir;           // the parent dir of all node agents
    protected File nodeAgentDir;        // the specific node agent dir
    protected File instanceDir;         // the specific instance dir
    private InstanceDirs instanceDirs;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(LocalInstanceCommand.class);

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        initInstance();
    }

    /** 
     * override this method if your class does NOT want to create directories
     * @param f the directory to create
     */
    protected boolean mkdirs(File f) {
        return f.mkdirs();
    }
    
    protected void initInstance() throws CommandException {
        String agentsDirPath = null;  // normally <install-root>/nodeagents

        if(ok(agentDir))
            agentsDirPath = agentDir;
        else
            agentsDirPath = getAgentsDirPath();

        agentsDir = new File(agentsDirPath);
        mkdirs(agentsDir);

        if(!agentsDir.isDirectory()) {
            throw new CommandException(
                    strings.get("Instance.badAgentDir", agentsDir));
        }

        if(nodeAgent != null) {
            nodeAgentDir = new File(agentsDir, nodeAgent);
        }
        else {
            nodeAgentDir = getTheOneAndOnlyAgent(agentsDir);
            nodeAgent = nodeAgentDir.getName();
        }

        if(instanceName != null) {
            instanceDir = new File(nodeAgentDir, instanceName);
            mkdirs(instanceDir);
        }
        else {
            instanceDir = getTheOneAndOnlyInstance(nodeAgentDir);
            instanceName = instanceDir.getName();
        }

        if(!instanceDir.isDirectory()) {
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
        catch (IOException e) {
            throw new CommandException(e);
        }

        logger.printDebugMessage("nodeAgentDir: " + nodeAgentDir);
        logger.printDebugMessage("instanceDir: " + instanceDir);
    }

    protected final InstanceDirs getInstanceDirs() {
        return instanceDirs;
    }

    /**
     * Set the programOpts based on the das.properties file.
     */
    protected final void setDasDefaults(File propfile) throws CommandException {
        Properties dasprops = new Properties();
        FileInputStream fis = null;
        try {
            // read properties and set them in programOpts
            // properties are:
            // agent.das.port
            // agent.das.host
            // agent.das.isSecure
            // agent.das.user           XXX - not in v2?
            fis = new FileInputStream(propfile);
            dasprops.load(fis);
            fis.close();
            fis = null;
            String p;
            p = dasprops.getProperty("agent.das.host");
            if (p != null)
                programOpts.setHost(p);
            p = dasprops.getProperty("agent.das.port");
            int port = -1;
            if (p != null)
                port = Integer.parseInt(p);
            p = dasprops.getProperty("agent.das.protocol");
            if (p != null && p.equals("rmi_jrmp")) {
                programOpts.setPort(updateDasPort(dasprops, port, propfile));
            } else if (p == null || p.equals("http"))
                programOpts.setPort(port);
            else
                throw new CommandException(strings.get("Instance.badProtocol",
                                                    propfile.toString(), p));
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
                                    propfile.getPath()));
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

    /**
     * Update DAS port from an old V2 das.properties file.
     * If the old port is the standard jrmp port, just use the new
     * standard http port.  Otherwise, prompt for the new port number
     * if possible.  In any event, try to rewrite the das.properties
     * file with the new values.
     */
    private int updateDasPort(Properties dasprops, int port, File propfile) {
        Console cons;
        if (port == 8686) {     // the old JRMP port
            logger.printMessage(
                strings.get("Instance.oldDasProperties",
                    propfile.toString(), Integer.toString(port),
                    Integer.toString(programOpts.getPort())));
            port = programOpts.getPort();
        } else if ((cons = System.console()) != null) {
            String line = cons.readLine("%s",
                strings.get("Instance.oldDasPropertiesPrompt",
                    propfile.toString(), Integer.toString(port),
                    Integer.toString(programOpts.getPort())));
            while (line != null && line.length() > 0) {
                try {
                    port = Integer.parseInt(line);
                    if (port > 0 && port <= 65535)
                        break;
                } catch (NumberFormatException nfex) {
                }
                line = cons.readLine(strings.get("Instance.reenterPort"),
                    Integer.toString(programOpts.getPort()));
            }
        } else {
            logger.printMessage(
                strings.get("Instance.oldDasPropertiesWrong",
                    propfile.toString(), Integer.toString(port),
                    Integer.toString(programOpts.getPort())));
            port = programOpts.getPort();
        }
        dasprops.setProperty("agent.das.protocol", "http");
        dasprops.setProperty("agent.das.port", Integer.toString(port));
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(propfile));
            dasprops.store(bos,
                "Domain Administration Server Connection Properties");
            bos.close();
            bos = null;
        } catch (IOException ex2) {
            logger.printMessage(
                strings.get("Instance.dasPropertiesUpdateFailed"));
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException cex) {
                    // ignore it
                }
            }
        }
        // whether we were able to update the file or not, keep going
        logger.printDebugMessage("New DAS port number: " + port);
        return port;
    }

    private File getTheOneAndOnlyAgent(File parent) throws CommandException {
        // look for subdirs in the parent dir -- there must be one and only one
        // or there can be zero in which case we create one-and-only

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        // ERROR:  more than one nodeagent directory
        if(files.length > 1) {
            throw new CommandException(
                    strings.get("Agent.tooManyAgentDirs", parent));
        }

        // the usual case -- one agent dir
        if(files.length == 1)
            return files[0];

        /*
         * If there is no existing nodeagent directory -- create one!
         */
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            File f = new File(parent, hostname);

            if(!f.mkdirs() || !f.isDirectory()) // for instance there is a regular file with that name
                throw new CommandException(strings.get("Agent.cantCreateAgentDir", f));

            return f;
        }
        catch (UnknownHostException ex) {
            throw new CommandException(strings.get("Agent.cantGetHostName", ex));
        }
    }

    private File getTheOneAndOnlyInstance(File parent) throws CommandException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if(files == null || files.length == 0) {
            throw new CommandException(
                    strings.get("Instance.noInstanceDirs", parent));
        }

        // expect two - the "agent" directory and the instance directory
        if(files.length > 2) {
            throw new CommandException(
                    strings.get("Instance.tooManyInstanceDirs", parent));
        }

        for(File f : files) {
            if(!f.getName().equals("agent"))
                return f;
        }
        throw new CommandException(
                strings.get("Instance.noInstanceDirs", parent));
    }

    private String getAgentsDirPath() throws CommandException {
        String agentsDirPath = getSystemProperty(
                SystemPropertyConstants.AGENT_ROOT_PROPERTY);

        if(StringUtils.ok(agentsDirPath))
            return agentsDirPath;

        String installRootPath = getSystemProperty(
                SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        if(!StringUtils.ok(installRootPath))
            installRootPath = System.getProperty(
                    SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        if(!StringUtils.ok(installRootPath))
            throw new CommandException("Agent.noInstallDirPath");

        return installRootPath + "/" + "nodeagents";
    }
}
