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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.admin.cli.*;
import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.net.NetUtils;

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
@Service(name = "_create-instance-filesystem")
@Scoped(PerLookup.class)
public class CreateLocalInstanceFilesystemCommand extends LocalInstanceCommand {
    
    @Param(name = "agentport", optional = true)
    private String agentPort;  //nodeagent.properties agent.adminPort

    @Param(name = "agentproperties", optional = true, separator = ':')
    private Properties agentProperties;  //TODO Properties error handling

    //@Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    //private boolean saveMasterPassword = false;

    @Param(name = "instance_name", primary = true)
    private String instanceName0;

    String DASHost;
    int DASPort = -1;
    String DASProtocol;
    boolean dasIsSecure;

    private File agentConfigDir;
    private File applicationsDir;
    private File configDir;
    private File generatedDir;
    private File libDir;
    private File docrootDir;
    private File dasPropsFile;
    private Properties dasProperties;
    private File nodeagentPropsFile;
    private File loggingPropsFile;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateLocalInstanceFilesystemCommand.class);

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(ok(instanceName0))
            instanceName = instanceName0;
        else
            throw new CommandException(strings.get("Instance.badInstanceName"));

        //ProgramOptions should takes care of default values?
        if (!ok(DASHost)) {
             try {
                DASHost = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                logger.getLogger().warning(strings.get("Instance.unknownHost"));
                logger.getLogger().warning(ex.getLocalizedMessage());
                DASHost = CLIConstants.DEFAULT_HOSTNAME;
            }
        }

        if (agentPort != null) {
            if (!NetUtils.isPortStringValid(agentPort)) {
                throw new CommandException(strings.get("Instance.invalidAgentPort", agentPort));
            }
        }

        super.validate();

        String agentPath = "agent" + File.separator + "config";
        agentConfigDir = new File(nodeAgentDir, agentPath);
        dasPropsFile = new File(agentConfigDir, "das.properties");
        nodeagentPropsFile = new File(agentConfigDir, "nodeagent.properties");

        applicationsDir = new File(instanceDir, "applications");
        configDir = new File(instanceDir, "config");
        generatedDir = new File(instanceDir, "generated");
        libDir = new File(instanceDir, "lib");
        docrootDir = new File(instanceDir, "docroot");
        loggingPropsFile = new File(configDir, ServerEnvironmentImpl.kLoggingPropertiesFileName);

        DASHost = programOpts.getHost();
        DASPort = programOpts.getPort();
        dasIsSecure = programOpts.isSecure();
        
        if (DASPort == -1) {
            DASPort = dasIsSecure ? 4849 : 4848;
        }
        DASProtocol = dasIsSecure ? "https" : "http";

    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {
        //if (saveMasterPassword) {
        //    String masterPassword = getPassword(null, "changeit", true);
        //    createMasterPasswordFile(masterPassword);
        //}
        return createDirectories();
    }

    private int createDirectories() throws CommandException {
        boolean createDirsComplete = false;
        File badfile = null;
        while (badfile == null && !createDirsComplete) {
            if (!agentConfigDir.isDirectory()) {
                if (!agentConfigDir.mkdirs()) {
                    badfile = agentConfigDir;
                }
            }
            if (!applicationsDir.isDirectory()) {
                if (!applicationsDir.mkdir()) {
                    badfile = applicationsDir;
                }
            }
            if (!configDir.isDirectory()) {
                if (!configDir.mkdir()) {
                    badfile = configDir;
                }
            }
            if (!generatedDir.isDirectory()) {
                if (!generatedDir.mkdir()) {
                    badfile = generatedDir;
                }
            }
            if (!libDir.isDirectory()) {
                if (!libDir.mkdir()) {
                    badfile = libDir;
                }
            }
            if (!docrootDir.isDirectory()) {
                if (!docrootDir.mkdir()) {
                    badfile = docrootDir;
                }
            }
            createDirsComplete = true;
        }
        if (badfile != null) {
            throw new CommandException(strings.get("Instance.cannotMkDir", badfile));
        }
        writeProperties();
        return SUCCESS;
    }

    private void writeProperties() throws CommandException {
        String filename = "";
        try {
            filename = dasPropsFile.getName();
            writeDasProperties();
            filename = nodeagentPropsFile.getName();
            writeNodeagentProperties();
            filename = loggingPropsFile.getName();
            writeLoggingProperties();
        } catch (IOException ex) {
            throw new CommandException(strings.get("Instance.cantWriteProperties", filename), ex);
        }
    }

    private void writeDasProperties() throws IOException {
        if (!dasPropsFile.isFile()) {
            dasPropsFile.createNewFile();
        }

        dasProperties = new Properties();
        
        dasProperties.setProperty(K_DAS_HOST, DASHost);
        dasProperties.setProperty(K_DAS_PORT, String.valueOf(DASPort));
        dasProperties.setProperty(K_DAS_IS_SECURE, String.valueOf(dasIsSecure));
        dasProperties.setProperty(K_DAS_PROTOCOL, DASProtocol);

        FileOutputStream fos = new FileOutputStream(dasPropsFile);
        dasProperties.store(fos, strings.get("Instance.dasPropertyComment"));
        fos.close();
    }

    private void writeNodeagentProperties() throws IOException, CommandException {
        //TODO allow any properties?
        if (!nodeagentPropsFile.isFile()) {
            nodeagentPropsFile.createNewFile();
        }

        String remoteClientAddress = getSystemProperty(HOST_NAME_PROPERTY);
        String listenAddress = NODEAGENT_DEFAULT_HOST_ADDRESS;
        String agentProtocol = NODEAGENT_JMX_DEFAULT_PROTOCOL; //??
        String dasProtocol = DASProtocol;
        String isDasSecure = String.valueOf(dasIsSecure);

        if (agentProperties != null && !agentProperties.isEmpty()) {
            remoteClientAddress = agentProperties.getProperty(REMOTE_CLIENT_ADDRESS_NAME, getSystemProperty(HOST_NAME_PROPERTY));
            listenAddress = agentProperties.getProperty(AGENT_LISTEN_ADDRESS_NAME, NODEAGENT_DEFAULT_HOST_ADDRESS);
            agentProtocol = agentProperties.getProperty(AGENT_JMX_PROTOCOL_NAME, NODEAGENT_JMX_DEFAULT_PROTOCOL);
            dasProtocol = agentProperties.getProperty(DAS_JMX_PROTOCOL_NAME, DASProtocol);
            isDasSecure = agentProperties.getProperty(NODEAGENT_DEFAULT_DAS_IS_SECURE, String.valueOf(dasIsSecure));
        }

        Properties _nodeagentProps = new Properties();
        _nodeagentProps.setProperty(K_CLIENT_HOST, remoteClientAddress);
        _nodeagentProps.setProperty(K_ADMIN_HOST, listenAddress);
        _nodeagentProps.setProperty(K_ADMIN_PORT, String.valueOf(getAgentPort()));
        _nodeagentProps.setProperty(K_AGENT_PROTOCOL, agentProtocol);
        _nodeagentProps.setProperty(K_DAS_PROTOCOL, dasProtocol);
        _nodeagentProps.setProperty(K_DAS_IS_SECURE, isDasSecure);

        FileOutputStream fos = new FileOutputStream(nodeagentPropsFile);
        _nodeagentProps.store(fos, strings.get("Instance.nodeagentPropertiesComment"));
        fos.close();
    }

    /**
     * Returns the agent port specified by the --agentport option. If unspecified a
     * random free port is chosen. We ensure that the agent port is not in use or
     * a CommandException is thrown.
     */
    protected int getAgentPort() throws CommandException
    {
        //verify admin port is not in use
        int port;
        if (agentPort == null) {
            port = NetUtils.getFreePort();
        } else {
            port = Integer.parseInt(agentPort);
        }
        if (!NetUtils.isPortFree(port)) {
            throw new CommandException(strings.get("AgentPortInUse",
                agentPort));
        }
        logger.printDebugMessage("agentPort = " + port);
        return port;
    }

    private void writeLoggingProperties() throws IOException {
        String rootFolder = getSystemProperty(com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        String templateDir = rootFolder + File.separator + "lib" + File.separator + "templates";
        File src = new File(templateDir, ServerEnvironmentImpl.kLoggingPropertiesFileName);
        FileUtils.copy(src, loggingPropsFile);
    }

}
