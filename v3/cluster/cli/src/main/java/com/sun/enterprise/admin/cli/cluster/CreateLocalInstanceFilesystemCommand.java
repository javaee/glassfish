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
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

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
    //TODO --agentport, --agentproperties not needed until we have node agent.
    //@Param(name = "agentport", optional = true)
    //private String agentPort;  --> nodeagent.properties agent.adminPort

    //@Param(name = "agentproperties", optional = true, separator = ':')
    //private Properties agentProperties;  --> nodeagent.properties

    //TODO where to get masterpassword?
    //@Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    //private boolean saveMasterPassword = false;

    @Param(name = "instance_name", primary = true)
    private String instanceName0;

    String DASHost;
    int DASPort = -1;
    String DASProtocol;
    boolean dasIsSecure;

    public static final String K_DAS_HOST = "agent.das.host";
    public static final String K_DAS_PROTOCOL = "agent.das.protocol";
    public static final String K_DAS_PORT = "agent.das.port";
    public static final String K_DAS_IS_SECURE = "agent.das.isSecure";

    public static final String K_MASTER_PASSWORD = "agent.masterpassword";
    public static final String K_SAVE_MASTER_PASSWORD = "agent.saveMasterPassword";

    public static final String NODEAGENT_DEFAULT_DAS_IS_SECURE = "false";
    public static final String NODEAGENT_DEFAULT_DAS_PORT = String.valueOf(CLIConstants.DEFAULT_ADMIN_PORT);

    private File agentConfigDir;
    private File applicationsDir;
    private File configDir;
    private File generatedDir;
    private File libDir;
    private File docrootDir;
    private File dasPropsFile;
    private Properties dasProperties;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateLocalInstanceFilesystemCommand.class);

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {

        if(ok(instanceName0))
            instanceName = instanceName0;
        else
            throw new CommandValidationException(strings.get("Instance.badInstanceName"));

        super.validate();

        String agentPath = "agent" + File.separator + "config";
        agentConfigDir = new File(nodeAgentDir, agentPath);
        dasPropsFile = new File(agentConfigDir, "das.properties");

        applicationsDir = new File(instanceDir, "applications");
        configDir = new File(instanceDir, "config");
        generatedDir = new File(instanceDir, "generated");
        libDir = new File(instanceDir, "lib");
        docrootDir = new File(instanceDir, "docroot");

        DASHost = programOpts.getHost();
        DASPort = programOpts.getPort();
        dasIsSecure = programOpts.isSecure();

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
        
        if (DASPort == -1) {
            DASPort = dasIsSecure ? 4849 : 4848;
        }
        DASProtocol = dasIsSecure ? "https" : "http";

    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        
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
        try {
            writeDasProperties();
        } catch (IOException ex) {
            throw new CommandException(strings.get("Instance.cantWriteDasProperties", dasPropsFile.getName()), ex);
        }
        if (badfile != null) {
            throw new CommandException(strings.get("Instance.cannotMkDir", badfile));
        }
        return SUCCESS;
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
}
