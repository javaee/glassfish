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

package com.sun.enterprise.server.logging.logviewer.backend;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.jvnet.hk2.component.Habitat;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Naman Mehta
 * Date: 6 Aug, 2010
 * Time: 11:20:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogFilterForInstance {

    public File getInstanceLogFile(Habitat habitat, Server targetServer, Domain domain, Logger logger, String instanceName, String domainRoot) {

        File instanceLogFile = null;

        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in temp directory which is used to get LogFile object.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup

        try {
            SSHLauncher sshL = habitat.getComponent(SSHLauncher.class);
            String sNode = targetServer.getNode();
            Nodes nodes = domain.getNodes();
            Node node = nodes.getNode(sNode);
            sshL.init(node, logger);

            SFTPClient sftpClient = sshL.getSFTPClient();

            File logFileDirectoryOnServer = new File(domainRoot + File.separator + "logs"
                    + File.separator + instanceName);
            if (logFileDirectoryOnServer.exists())
                logFileDirectoryOnServer.delete();

            logFileDirectoryOnServer.mkdirs();

            instanceLogFile = new File(logFileDirectoryOnServer.getAbsolutePath() + File.separator + "server.log");

            InputStream inputStream = sftpClient.read(node.getInstallDir() + File.separator + "nodes" + File.separator
                    + sNode + File.separator + instanceName + File.separator + "logs" + File.separator + "server.log");

            BufferedInputStream in = new BufferedInputStream(inputStream);
            FileOutputStream file = new FileOutputStream(instanceLogFile);
            BufferedOutputStream out = new BufferedOutputStream(file);
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            out.flush();
        }
        catch (IOException ex) {
            logger.log(Level.WARNING, "logging.backend.error.instance", ex);
            throw new RuntimeException(ex);
        }

        return instanceLogFile;

    }
}
