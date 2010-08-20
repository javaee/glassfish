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

import java.io.*;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.web.EmbeddedWebContainer;
import org.glassfish.api.embedded.Port;

import java.io.File;


public  class Util {

    public static Server getServer(String serverID, String installRoot, String instanceRoot, String configFile, 
            Boolean autoDelete) throws IOException {

        Server server = Server.getServer(serverID);
        if (server != null)
            return server;

        Server.Builder builder = new Server.Builder(serverID);

        EmbeddedFileSystem efs = getFileSystem(installRoot, instanceRoot, configFile, autoDelete);
        server = builder.embeddedFileSystem(efs).build();
        return server;
    }

    public static EmbeddedFileSystem getFileSystem(String installRoot, String instanceRoot, String configFile, Boolean autoDelete) {

        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        if (installRoot != null)
            efsb.installRoot(new File(installRoot), true);
        if (instanceRoot != null) {
            // this property is normally used as a token in a regular glassfish domain.xml
            System.setProperty("com.sun.aas.instanceRootURI", "file:" + instanceRoot);
            efsb.instanceRoot(new File(instanceRoot));
        }
        
        if (configFile != null)
            efsb.configurationFile(new File(configFile));
        if (autoDelete != null)
            efsb.autoDelete(autoDelete.booleanValue());

        return efsb.build();
    }

    public static void createPort(Server server, String configFile, int port)
        throws java.io.IOException {
        Port http = null;

        if (configFile == null && port == -1) {
            http = server.createPort(Constants.DEFAULT_HTTP_PORT);
        }
        else if (port != -1) {
            http = server.createPort(port);
        }
        if (http != null) {
            ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
            server.addContainer(b);
            EmbeddedWebContainer embedded = (EmbeddedWebContainer) b.create(server);
            embedded.bind(http, "http");
        }
    }

}
