/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.ant.embedded.tasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.ContainerBuilder;

public class StartServerTask extends TaskBase {

    String serverID = Constants.DEFAULT_SERVER_ID;
    int port = -1;
    String installRoot = null, instanceRoot = null, configFile = null;
    Boolean autoDelete;
    ContainerBuilder.Type containerType = ContainerBuilder.Type.all;

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

    public void setAutoDelete(Boolean autoDelete) {
        this.autoDelete = autoDelete;
    }


    public void execute() throws BuildException {

        try {
            Server server = Server.getServer(serverID);
            if (server != null) {
                return;
            }
            log ("Starting server");
            server = Util.getServer(serverID, installRoot, instanceRoot, configFile, autoDelete);

            Util.createPort(server, configFile, port);

            server.addContainer(getContainerType());
            server.start();

        } catch (Exception ex) {
            error(ex);
        }
    }

    ContainerBuilder.Type getContainerType() {
        return containerType;
    }

    void setContainerType(ContainerBuilder.Type type) {
        this.containerType = type;
    }

}
