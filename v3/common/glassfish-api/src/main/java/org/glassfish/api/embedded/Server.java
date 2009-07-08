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

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Contract;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;

import com.sun.enterprise.module.bootstrap.PlatformMain;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * Defines a embedded Server, capable of attaching containers (entities running
 * users applications).
 *
 * @author Jerome Dochez
 */
@Contract
public class Server {

    public static class Builder {
        final String serverName;
        boolean loggerEnabled;
        boolean verbose;
        File loggerFile;
        EmbeddedFileSystem fileSystem;

        /**
         * Creates an unconfigured instance. The habitat will be obtained
         * by scanning the inhabitants files using this class classloader
         * @param id the server name
         */
        public Builder(String id) {
            this.serverName = id;
        }                              

        /**
         * Enables or disables the logger for this server
         *
         * @param enabled true to enable, false to disable
         * @return this instance
         */
        public Builder setLogger(boolean enabled) {
            loggerEnabled = enabled;
            return this;
        }

        /**
         * Sets the log file location
         *
         * @param f a valid file location
         * @return this instance
         */
        public Builder setLogFile(File f) {
            loggerFile = f;
            return this;
        }

        /**
         * Turns on of off the verbose flag.
         *
         * @param b true to turn on, false to turn off
         * @return this instance
         */
        public Builder setVerbose(boolean b) {
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
        public Builder setEmbeddedFileSystem(EmbeddedFileSystem fileSystem) {
            this.fileSystem = fileSystem;
            return this;
        }

        public Server build() {
            synchronized(servers) {
                if (!servers.containsKey(serverName)) {
                    servers.put(serverName, new Server(this));
                }
                return servers.get(serverName);
            }
        }

        public static List<String> getServerNames() {
            List<String> names = new ArrayList<String>();
            names.addAll(servers.keySet());
            return names;
        }
    }

    private class Container {
        private final EmbeddedContainer container;
        boolean started;

        private Container(EmbeddedContainer container) {
            this.container = container;
        }
        
    }

    private final static Map<String, Server> servers = new HashMap<String, Server>();

    public final String serverName;
    public final boolean loggerEnabled;
    public final boolean verbose;
    public final File loggerFile;
    public final Inhabitant<EmbeddedFileSystem> fileSystem;
    private final Habitat habitat;
    private final List<Container> containers = new ArrayList<Container>();



    private Server(Builder builder) {
        serverName = builder.serverName;
        loggerEnabled = builder.loggerEnabled;
        verbose = builder.verbose;
        loggerFile = builder.loggerFile;
        final PlatformMain embedded = getMain();
        if (embedded==null) {
            throw new RuntimeException("Embedded startup not found");
        }
        EmbeddedFileSystem fs;
        if (builder.fileSystem==null) {
            // there is no instance root directory, let's create one.
            EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
            try {
                File f = File.createTempFile("gfembed", "tmp", new File(System.getProperty("user.dir")));
                f.delete();
                fsBuilder.setInstanceRoot(new File(f.getParent(), f.getName()));
                fsBuilder.setAutoDelete(true);
            } catch (IOException e) {

            }
            fs = fsBuilder.build();
        } else {
            fs = builder.fileSystem;
        }
        if (!fs.instanceRoot.exists()) {
            fs.instanceRoot.mkdirs();
            // todo : dochez : temporary fix for docroot
            File f = new File(fs.instanceRoot, "docroot");
            f.mkdirs();
        }
        embedded.setContext(new StartupContext(fs.instanceRoot, new String[0]));
        embedded.setContext(this);
        try {
            embedded.start(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
        }
        habitat = embedded.getStartedService(Habitat.class);


        fileSystem = new ExistingSingletonInhabitant<EmbeddedFileSystem>(fs);
        habitat.add(fileSystem);


        for (EmbeddedLifecycle lifecycle : habitat.getAllByContract(EmbeddedLifecycle.class)) {
            try {
                lifecycle.creation(this);
            } catch(Exception e) {
                Logger.getAnonymousLogger().log(Level.WARNING,"Exception while notifying of embedded server startup",e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ContainerBuilder<EmbeddedContainer> createConfig(ContainerBuilder.Type type) {
        return habitat.getComponent(ContainerBuilder.class, type.toString());
    }

    /**
     * Creates a new embedded container configuration of a type.
     *
     * @param configType the type of the embedded container configuration
     * @param <T> type of the embedded container
     * @return the configuration to configure a container of type <T>
     */
    public <T extends ContainerBuilder<?>> T createConfig(Class<T> configType) {
        return habitat.getComponent(configType);        
    }

    /**
     * Adds a container to this server.
     *
     * Uing the configuration instance for the container of type <T>,
     * creating the container from that configuration and finally adding the
     * container instance to the list of managed containers
     *
     * @param info the configuration for the container
     * @param <T> type of the container
     * @return instance of the container <T>
     */
    public <T extends EmbeddedContainer> T addContainer(ContainerBuilder<T> info) {
        T container = info.create(this);
        if (container!=null && containers.add(new Container(container))) {
            return container;
        }
        return null;

    }


    /**
     * Returns a list of the currently managed containers
     *
     * @return the containers list
     */
    public Collection<EmbeddedContainer> getContainers() {
        ArrayList<EmbeddedContainer> copy = new ArrayList<EmbeddedContainer>();
        for (Container c : containers) {
            copy.add(c.container);
        }
        return copy;        
    }

    /**
     * Creates a port to attach to embedded containers. Ports can be attached to many
     * embedded containers and containers may accept more than one port.
     *
     * @param portNumber port number for this port
     * @return a new port abstraction.
     */
    public Port createPort(int portNumber) {
        Port port = habitat.getComponent(Port.class);
        port.bind(portNumber);
        return port;
    }

    /**
     * Returns the configured habitat for this server.
     *
     * @return the habitat
     */
    public Habitat getHabitat() {
        return habitat;
    }

    /**
     * Returns the container name, as specified in {@link org.glassfish.api.embedded.Server.Builder#Builder(String)}
     *
     * @return container name
     */
    public String getName(){
        return serverName;
    }

    /**
     * Starts the server
     */
    public void start() throws LifecycleException {
        for (Container c : containers) {
            try {
                c.container.start();
                c.started=true;
            } catch (LifecycleException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                c.started=false;
            }
        }
    }

    /**
     * Stops the container
     */
    public void stop() throws LifecycleException {
        System.out.println("Received Stop");
        for (Container c : containers) {
            try {
                if (c.started) {
                    c.container.stop();
                }
            } catch (LifecycleException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            c.started=false;
        }
        ModuleStartup ms = habitat.getComponent(ModuleStartup.class);
        if (ms!=null) {
            System.out.println("Sending stop event to ms");
            ms.stop();
        }
        synchronized(servers) {
            servers.remove(serverName);
        }
        for (EmbeddedLifecycle lifecycle : habitat.getAllByContract(EmbeddedLifecycle.class)) {
            try {
                lifecycle.destruction(this);
            } catch(Exception e) {
                Logger.getAnonymousLogger().log(Level.WARNING,"Exception while notifying of embedded server destruction",e);
            }
        }
        fileSystem.get().preDestroy();
        
    }

    /**
     * Returns the embedded deployer implementation, can be used to
     * generically deploy applications to the embedded server.
     *
     * @return embedded deployer
     */
    public EmbeddedDeployer getDeployer() {
        return habitat.getByContract(EmbeddedDeployer.class);
    }

    private PlatformMain getMain() {
        ServiceLoader<PlatformMain> mains = ServiceLoader.load(PlatformMain.class, Server.class.getClassLoader());
        for (PlatformMain main : mains) {
            if (main.getName().equals("Embedded")) {
                return main;
            }
        }
        return null;
    }




}
