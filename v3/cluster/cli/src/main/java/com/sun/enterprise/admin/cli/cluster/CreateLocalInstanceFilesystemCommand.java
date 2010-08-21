/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import static com.sun.enterprise.admin.cli.CLIConstants.*;

/**
 *  This is a local command that creates a local instance.
 *  Create the local directory structure
 *  nodes/<host-name>/
 *   || ---- agent
 *             || ---- config
 *                     | ---- das.properties
 *   || ---- <server-instance-1>
 *   || ---- <server-instance-2>
 *
 */
@Service(name = "_create-instance-filesystem")
@Scoped(PerLookup.class)
public class CreateLocalInstanceFilesystemCommand extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true)
    private String instanceName0;

    String DASHost;
    int DASPort = -1;
    String DASProtocol;
    boolean dasIsSecure;

    private File agentConfigDir;
    private File dasPropsFile;
    private Properties dasProperties;
    protected boolean setDasDefaultsOnly = false;

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(ok(instanceName0))
            instanceName = instanceName0;
        else
            throw new CommandException(Strings.get("Instance.badInstanceName"));

        super.validate();

        String agentPath = "agent" + File.separator + "config";
        agentConfigDir = new File(nodeDirChild, agentPath);
        dasPropsFile = new File(agentConfigDir, "das.properties");

        if (dasPropsFile.isFile()) {
            setDasDefaults(dasPropsFile);
            if (!setDasDefaultsOnly) {
                String nodeDirChildName = nodeDirChild != null ? nodeDirChild.getName() : "";
                String nodeName = node != null ? node : nodeDirChildName;
                logger.printMessage(Strings.get("Instance.existingDasPropertiesWarning",
                    programOpts.getHost(), "" + programOpts.getPort(), nodeName));
            }
        }

        DASHost = programOpts.getHost();
        DASPort = programOpts.getPort();
        dasIsSecure = programOpts.isSecure();
        DASProtocol = "http";
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {
        
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
            createDirsComplete = true;
        }
        if (badfile != null) {
            throw new CommandException(Strings.get("Instance.cannotMkDir", badfile));
        }
        writeProperties();
        return SUCCESS;
    }

    private void writeProperties() throws CommandException {
        String filename = "";
        try {
            filename = dasPropsFile.getName();
            if (!dasPropsFile.isFile()) {
                writeDasProperties();
            }
        } catch (IOException ex) {
            throw new CommandException(Strings.get("Instance.cantWriteProperties", filename), ex);
        }
    }

    private void writeDasProperties() throws IOException {
        dasPropsFile.createNewFile();
        dasProperties = new Properties();
        dasProperties.setProperty(K_DAS_HOST, DASHost);
        dasProperties.setProperty(K_DAS_PORT, String.valueOf(DASPort));
        dasProperties.setProperty(K_DAS_IS_SECURE, String.valueOf(dasIsSecure));
        dasProperties.setProperty(K_DAS_PROTOCOL, DASProtocol);

        FileOutputStream fos = new FileOutputStream(dasPropsFile);
        dasProperties.store(fos, Strings.get("Instance.dasPropertyComment"));
        fos.close();
    }
}
