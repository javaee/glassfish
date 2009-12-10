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
import org.glassfish.api.container.Sniffer;

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
 * Instances of server are embedded application servers, capable of attaching various containers
 * (entities running users applications).
 *
 * @author Jerome Dochez
 */
@Contract
public class Server {

    /**
     * Builder for creating embedded server instance. Builder can be used to configure
     * the logger, the verbosity and the embedded file system which acts as a
     * virtual file system to the embedded server instance.
     */
    public static class Builder {
        final String serverName;
        boolean loggerEnabled;
        boolean verbose;
        File loggerFile;
        EmbeddedFileSystem fileSystem;

        /**
         * Creates an unconfigured instance. The habitat will be obtained
         * by scanning the inhabitants files using this class's classloader
         *
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
        public Builder logger(boolean enabled) {
            loggerEnabled = enabled;
            return this;
        }

        /**
         * Sets the log file location
         *
         * @param f a valid file location
         * @return this instance
         */
        public Builder logFile(File f) {
            loggerFile = f;
            return this;
        }

        /**
         * Turns on of off the verbose flag.
         *
         * @param b true to turn on, false to turn off
         * @return this instance
         */
        public Builder verbose(boolean b) {
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
        public Builder embeddedFileSystem(EmbeddedFileSystem fileSystem) {
            this.fileSystem = fileSystem;
            return this;
        }
        /**
         * Uses this builder's name to create or return an existing embedded
         * server instance.
         * The embedded server will be using the configured parameters
         * of this builder. If no embedded file system is used, the embedded instance will use
         * a temporary instance root with a default basic configuration. That temporary instance
         * root will be deleted once the server is shutdown.
         *
         * @return the configured server instance
         */
        public Server build() {
            return build(null);
        }

        /**
         * Uses this builder's name to create or return an existing embedded
         * server instance.
         * The embedded server will be using the configured parameters
         * of this builder. If no embedded file system is used, the embedded instance will use
         * a temporary instance root with a default basic configuration. That temporary instance
         * root will be deleted once the server is shutdown.
         *
         * @param properties extra creation properties
         *
         * @return the configured server instance
         */
        public Server build(Properties properties) {
            synchronized(servers) {
                if (!servers.containsKey(serverName)) {
                    Server s = new Server(this, properties);
                    servers.put(serverName, s);
                    return s;
                }
                throw new IllegalStateException("An embedded server of this name already exists");
            }
        }
    }

    private final static class ContainerStatus {

        int status=0;

        private void started() { status=1; }
        private void stopped() { status=0; }
        private boolean isStopped() {
            return status==0;
        }
        private boolean isStarted() {
            return status==1;
        }
    }

    private final static class Container {
        private final EmbeddedContainer container;
        boolean started;

        private Container(EmbeddedContainer container) {
            this.container = container;
        }
        
    }

    private final static Map<String, Server> servers = new HashMap<String, Server>();

    private final String serverName;
    private final boolean loggerEnabled;
    private final boolean verbose;
    private final File loggerFile;
    private final ContainerStatus status = new ContainerStatus();
    private final Inhabitant<EmbeddedFileSystem> fileSystem;
    private final Habitat habitat;
    private final List<Container> containers = new ArrayList<Container>();



    private Server(Builder builder, Properties properties) {
        serverName = builder.serverName;
        loggerEnabled = builder.loggerEnabled;
        verbose = builder.verbose;
        loggerFile = builder.loggerFile;

        EmbeddedFileSystem fs;
        File instanceRoot=null;

        if (builder.fileSystem==null || builder.fileSystem.instanceRoot==null) {
            File f;
            try {
                String tmpDir = System.getProperty("glassfish.embedded.tmpdir");
                if (tmpDir==null) {
                    tmpDir = System.getProperty("user.dir");
                }
                f = File.createTempFile("gfembed", "tmp", new File(tmpDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            f.delete();
            instanceRoot = new File(f.getParent(), f.getName());
            EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();

            // not pretty, revisit when more time is available.
            if (builder.fileSystem!=null) {
                fsBuilder.installRoot(builder.fileSystem.installRoot, builder.fileSystem.cookedMode);
                fsBuilder.configurationFile(builder.fileSystem.configFile);
                fsBuilder.readOnly=builder.fileSystem.readOnlyConfigFile;
            }

            fsBuilder.instanceRoot(instanceRoot);
            fsBuilder.autoDelete(true);
            fs = fsBuilder.build();
        } else {
            fs = builder.fileSystem;
        }

        if (!fs.instanceRoot.exists()) {
            fs.instanceRoot.mkdirs();
        }
        // todo : dochez : temporary fix for docroot
        File f = new File(fs.instanceRoot, "docroot");
        if (!f.mkdirs()) {
            if (Logger.getAnonymousLogger().isLoggable(Level.FINE)) {
                Logger.getAnonymousLogger().fine("Cannot create docroot embedded directory at "
                    + f.getAbsolutePath());
            }
        }
        f = new File(fs.instanceRoot, "config");
        if (!f.mkdirs()) {
            if (Logger.getAnonymousLogger().isLoggable(Level.FINE)) {
                Logger.getAnonymousLogger().fine("Cannot create config embedded directory at "
                    + f.getAbsolutePath());
            }
        }
        
        fileSystem = new ExistingSingletonInhabitant<EmbeddedFileSystem>(fs);

        final PlatformMain embedded = getMain();
        if (embedded==null) {
            throw new RuntimeException("Embedded startup not found, classpath is probably incomplete");
        }

        String[] args = new String[0];
        if (properties!=null) {
            if (properties.containsKey(StartupContext.STARTUP_MODULESTARTUP_NAME)) {
                args = new String[2];
                args[0] = "-" + StartupContext.STARTUP_MODULESTARTUP_NAME; 
                args[1] = properties.getProperty(StartupContext.STARTUP_MODULESTARTUP_NAME);
            }
        }
        if (fs.installRoot==null) {
            embedded.setContext(new StartupContext(fs.instanceRoot, fs.instanceRoot, args));
            System.setProperty("com.sun.aas.installRoot", fs.instanceRoot.getAbsolutePath());
        } else {
            embedded.setContext(new StartupContext(fs.installRoot, fs.instanceRoot, args));
            System.setProperty("com.sun.aas.installRoot", fs.installRoot.getAbsolutePath());            
        }
        System.setProperty("com.sun.aas.instanceRoot", fs.instanceRoot.getAbsolutePath());

        embedded.setContext(this);
        embedded.setLogger(Logger.getAnonymousLogger());
        try {
            embedded.start(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
        }

        habitat = embedded.getStartedService(Habitat.class);
        habitat.addIndex(fileSystem, EmbeddedFileSystem.class.getName(), null);


        for (EmbeddedLifecycle lifecycle : habitat.getAllByContract(EmbeddedLifecycle.class)) {
            try {
                lifecycle.creation(this);
            } catch(Exception e) {
                Logger.getAnonymousLogger().log(Level.WARNING,"Exception while notifying of embedded server startup",e);
            }
        }
    }

    /**
     * Returns the list of existing embedded instances
     *
     * @return list of the instanciated embedded instances.
     */
    public static List<String> getServerNames() {
        List<String> names = new ArrayList<String>();
        names.addAll(servers.keySet());
        return names;
    }

    /**
     * Returns the embedded server instance of a particular name
     *
     * @param name requested server name
     * @return a server instance if it exists, null otherwise
     */
    public static Server getServer(String name) {
        return servers.get(name);
    }

    // todo : have the same name, and make it clear we use the type string().

    /**
     * Get the embedded container configuration for a container type.
     * @param type the container type (e.g. Type.ejb)
     * @return the embedded configuration for this container
     */
    public ContainerBuilder<EmbeddedContainer> createConfig(ContainerBuilder.Type type) {
        return createConfig(type.toString());
    }

    /**
     * Get the embedded container builder for a container type identified by its
     * name.
     * @param name the container name, which is the name used on the @Service annotation
     * @return the embedded builder for this container
     */
    @SuppressWarnings("unchecked")
    public ContainerBuilder<EmbeddedContainer> createConfig(String name) {
        return habitat.getComponent(ContainerBuilder.class, name);
    }

    /**
     * Get an embedded container configuration. The type of the expected
     * configuration is passed to the method and is not necessarily known to
     * the glassfish embedded API. This type of configuration is used for
     * extensions which are not defined by the core glassfish project.
     *
     * The API stability of the interfaces returned by this method is outside the
     * scope of the glassfish-api stability contract, it's a private contract
     * between the provider of that configuration and the user.
     *
     * @param configType the type of the embedded container configuration
     * @param <T> type of the embedded container
     * @return the configuration to configure a container of type <T>
     */
    public <T extends ContainerBuilder<?>> T createConfig(Class<T> configType) {
        return habitat.getComponent(configType);        
    }

    /**
     * Adds a container of a particular type using the default operating
     * configuration for the container.
     *
     * @param type type of the container to be added (like web, ejb).
     * @throws IllegalStateException if the container is already started.
     */
    public synchronized void addContainer(final ContainerBuilder.Type type) {

        if (status.isStarted()) {
            throw new IllegalStateException("Cannot add container to a started embedded instance");
        }
        containers.add(new Container(new EmbeddedContainer() {

            final List<Container> delegates = new ArrayList<Container>();
            final ArrayList<Sniffer> sniffers = new ArrayList<Sniffer>();

            public List<Sniffer> getSniffers() {
                synchronized(sniffers) {
                    if (sniffers.isEmpty()) {
                        if (type == ContainerBuilder.Type.all) {
                            for (final ContainerBuilder.Type t : ContainerBuilder.Type.values()) {
                                if (t!=ContainerBuilder.Type.all) {
                                    delegates.add(getContainerFor(t));
                                }
                            }
                        } else {
                            delegates.add(getContainerFor(type));
                        }
                    }
                    for (Container c : delegates) {
                        sniffers.addAll(c.container.getSniffers());
                    }
                }
                return sniffers;
            }

            public void bind(Port port, String protocol) {
                for (Container delegate : delegates) {
                    delegate.container.bind(port, protocol);
                }
            }

            private Container getContainerFor(final ContainerBuilder.Type type) {
                ContainerBuilder b = createConfig(type);
                if (b!=null) {
                    return new Container(b.create(Server.this));
                } else {
                    return new Container(new EmbeddedContainer() {

                        public List<Sniffer> getSniffers() {
                            List<Sniffer> sniffers = new ArrayList<Sniffer>();
                            Sniffer s = habitat.getComponent(Sniffer.class, type.toString());
                            if (s!=null) {
                                sniffers.add(s);
                            }
                            return sniffers;
                        }

                        public void bind(Port port, String protocol) {

                        }

                        public void start() throws LifecycleException {

                        }

                        public void stop() throws LifecycleException {

                        }
                    });
                }
            }

            public void start() throws LifecycleException {
                for (Container c : delegates) {
                    if (!c.started) {
                        c.container.start();
                        c.started=true;
                    }
                }
            }

            public void stop() throws LifecycleException {
                for (Container c : delegates) {
                    if (c.started) {
                        c.container.stop();
                        c.started=false;
                    }
                }

            }
        }));
    }



    // todo : clarify that adding containers after the server is created is illegal
    // todo : makes the return of those APIs return void.

    /**
     * Adds a container to this server.
     *
     * Using the configuration instance for the container of type <T>,
     * creating the container from that configuration and finally adding the
     * container instance to the list of managed containers
     *
     * @param info the configuration for the container
     * @param <T> type of the container
     * @return instance of the container <T>
     * @throws IllegalStateException if the container is already started.
     */
    public synchronized <T extends EmbeddedContainer> T addContainer(ContainerBuilder<T> info) {
        if (status.isStarted()) {
            throw new IllegalStateException("Cannot add containers to an already started embedded instance");
        }
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
     * embedded containers and some containers may accept more than one port.
     *
     * @param portNumber port number for this port
     * @return a new port abstraction.
     * @throws IOException if the port cannot be opened.
     */
    public Port createPort(int portNumber) throws IOException {
        Ports ports = habitat.getComponent(Ports.class);
        return ports.createPort(portNumber);
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
     * Returns the server name, as specified in {@link org.glassfish.api.embedded.Server.Builder#Builder(String)}
     *
     * @return container name
     */
    public String getName(){
        return serverName;
    }

    /**
     * Returns the embedded file system used to run this embedded instance.
     *
     * @return embedded file system used by this instance
     */
    public EmbeddedFileSystem getFileSystem() {
        return fileSystem.get();
    }

    /**
     * Starts the embedded server, opening ports, and running the startup
     * services.
     *
     * @throws LifecycleException if the server cannot be started propertly
     */
    public synchronized void start() throws LifecycleException {
        if (status.isStarted()) {
            return;
        }

        for (Container c : containers) {
            try {
                c.container.start();
                c.started=true;
            } catch (LifecycleException e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot start embedded container", e);
                c.started=false;
            }
        }
        status.started();
    }

    /**
     * stops the embedded server instance, any deployed application will be stopped
     * ports will be closed and shutdown services will be ran.
     * EmbeddedFileSystem will be released, meaning that any managed directory will
     * be deleted rendering the EmbeddedFileSystem unusable.
     *
     * @throws LifecycleException if the server cannot shuts down properly
     */
    public synchronized void stop() throws LifecycleException {

        try {
            if (status.isStopped()) {
                return;
            }
            for (Container c : containers) {
                try {
                    if (c.started) {
                            c.container.stop();
                    }
                } catch(Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING,"Exception while closing a embedded container",e);
                } finally {
                    c.started=false;
                }
            }

            Ports ports = habitat.getComponent(Ports.class);
            if (ports != null) {
                Collection<Port> coll = ports.getPorts();
                for (Port port:coll) {
                    port.close();
                }
            }
            ModuleStartup ms = habitat.getComponent(ModuleStartup.class, habitat.DEFAULT_NAME);
            if (ms!=null) {
                ms.stop();
            }
            synchronized(servers) {
                servers.remove(serverName);
            }
            //todo : change to DEAD
            status.stopped();

            for (EmbeddedLifecycle lifecycle : habitat.getAllByContract(EmbeddedLifecycle.class)) {
                try {
                    lifecycle.destruction(this);
                } catch(Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING,"Exception while notifying of embedded server destruction",e);
                }
            }
        } finally {
            fileSystem.get().preDestroy();
        }
        
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

        String platformName = "Embedded";
        if (fileSystem.get().installRoot!=null && fileSystem.get().installRoot.exists()) {
            if (!fileSystem.get().cookedMode) {
                platformName = "Static";
            }
        }

        ServiceLoader<PlatformMain> mains = ServiceLoader.load(PlatformMain.class, Server.class.getClassLoader());
        for (PlatformMain main : mains) {
            if (platformName.equals(main.getName())) {
                return main;
            }
        }
        return null;
    }




}
