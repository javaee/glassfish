/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.api.embedded;

import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.util.*;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 *
 * Configuration for an embedded server.
 *
 * Configuration data can be set on this builder which can then return
 * a configured {@link Server} instance with the {@link #create()} method.
 *
 * @author Jerome Dochez
 *
 */
@Service
public class ServerInfo  {


    @Inject
    private Habitat habitat;

    final String serverName;
    boolean loggerEnabled;
    boolean verbose;
    File loggerFile;
    EmbeddedFileSystem fileSystem;

    /**
     * Creates an unconfigured instance. The habitat will be obtained
     * by scanning the inhabitants files using this class classloader
     */
    public ServerInfo(String id) {
        this.serverName = id;
    }

    /**
     * Enables or disables the logger for this server
     *
     * @param enabled true to enable, false to disable
     * @return this instance
     */
    public ServerInfo setLogger(boolean enabled) {
        loggerEnabled = enabled;
        return this;
    }

    /**
     * Sets the log file location
     *
     * @param f a valid file location
     * @return this instance
     */
    public ServerInfo setLogFile(File f) {
        loggerFile = f;
        return this;
    }

    /**
     * Turns on of off the verbose flag.
     *
     * @param b true to turn on, false to turn off
     * @return this instance
     */
    public ServerInfo setVerbose(boolean b) {
        this.verbose = b;
        return this;
    }

    /**
     * Sets the embedded file system for the application server, used to locate
     * important files or directories used through the server lifetime.
     *
     * @param fileSystem a virtual filesystem
     * @return this instance
     */
    public ServerInfo setEmbeddedFileSystem(EmbeddedFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        return this;
    }

    private ServerInfo setHabitat(Habitat h) {
        habitat = h;
        return this;
    }

    /**
     * Creates a server configuration using the passed id as an identifier
     * or return an managed server info created with that name.
     *
     * @param id the server identification
     * @return an unconfigured server info
     */
    public static ServerInfo getServerInfo(String id) {
        synchronized (servers) {
            if (servers.containsKey(id)) {
                return servers.get(id);
            }
            // Bootstrap a hk2 environment.

            ModulesRegistry registry = new StaticModulesRegistry(ServerInfo.class.getClassLoader());
            Habitat habitat = registry.createHabitat("default");

            StartupContext startupContext = new StartupContext();
            habitat.add(new ExistingSingletonInhabitant(startupContext));

            habitat.addComponent(null, new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));
            ServerInfo info = new ServerInfo(id);
            info.setHabitat(habitat);
            servers.put(id, info);
            return info;
        }
    }

    private static final HashMap<String, ServerInfo> servers = new HashMap<String, ServerInfo>();
    private Server server = null;

    /**
     * Creates or return the already created Server instance corresponding to this
     * ServerInfo instance
     *
     * @return server instance
     */
    public synchronized Server create() {
        final boolean loggerEnabled = this.loggerEnabled;
        final boolean verbose = this.verbose;
        final File loggerFile = this.loggerFile;
        final EmbeddedFileSystem fileSystem = this.fileSystem;

        if (server==null) {
            server = new Server() {

                public <T extends EmbeddedContainerInfo> T createConfig(Class<T> configType) {
                    return habitat.getComponent(configType);
                }

                public Port createPort(int portNumber) {
                    Port port = habitat.getComponent(Port.class);
                    port.bind(portNumber);
                    return port;
                }

                public Habitat getHabitat() {
                    return habitat;
                }

                public String getName() {
                    return serverName;
                }

                List<EmbeddedContainer> containers = new ArrayList<EmbeddedContainer>();
                public <T extends EmbeddedContainer> T addContainer(EmbeddedContainerInfo<T> info) {
                    T container = info.create(this);
                    if (container!=null && containers.add(container)) {
                        return container;
                    }
                    return null;
                }

                public void start() {
                    for (EmbeddedContainer container : containers) {
                        container.start();
                    }
                }

                public Collection<EmbeddedContainer> getContainers() {
                    ArrayList<EmbeddedContainer> copy = new ArrayList<EmbeddedContainer>();
                    copy.addAll(containers);
                    return copy;
                }

                public void stop() {
                    for (EmbeddedContainer container : containers) {
                        container.stop();
                    }
                }
            };
        }
        return server;
    }

}
