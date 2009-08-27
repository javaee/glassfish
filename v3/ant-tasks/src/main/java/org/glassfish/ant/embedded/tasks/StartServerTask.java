/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ant.embedded.tasks;

import java.util.*;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import java.io.File;


public class StartServerTask extends Task {

    String serverID = Constants.DEFAULT_SERVER_ID;
    int port = Constants.DEFAULT_HTTP_PORT;
    String installRoot = null, instanceRoot = null, configFile = null;
    ContainerBuilder.Type type = ContainerBuilder.Type.all;

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setInstallRoot(String installRoot) {
        this.installRoot = installRoot;
    }

    public void setInstanceRoot(String instanceRoot) {
        this.instanceRoot = instanceRoot;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void execute() throws BuildException {
        log ("Starting server");
        Server server = getServer();
        try {
            server.createPort(port);
            server.addContainer(type);
            ArrayList<Sniffer> sniffers = new ArrayList<Sniffer>();
            for (EmbeddedContainer c : server.getContainers()) {
                sniffers.addAll(c.getSniffers());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Server getServer() {
        Server.Builder builder = new Server.Builder(serverID);
        Server server;
        EmbeddedFileSystem efs = getFileSystem();
        if (efs != null) {
            log("Using the install root : " + efs.installRoot);
            server = builder.setEmbeddedFileSystem(getFileSystem()).build();
        }
        else {
            server = builder.build();
        }
        return server;
    }

    EmbeddedFileSystem getFileSystem() {
        if (installRoot == null)
            return null;
        File instance;
        if (instanceRoot == null) {
            instance = new File(installRoot, "domains");
            instance = new File(instance, "domain1");
        }
        else
            instance = new File(instanceRoot);
        File config;
        if (configFile == null)  {
            config = new File(instance,"config");
            config = new File(config, "domain.xml");
        }
        else
            config = new File(configFile);

        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.setInstallRoot(new File(installRoot), true);
        efsb.setInstanceRoot(instance);
        return efsb.build();
    }

    void setContainerType(ContainerBuilder.Type type) {
        this.type = type;
    }

}
