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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.ThreadPools;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.FileCache;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.Protocols;
import com.sun.grizzly.config.dom.Ssl;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import com.sun.grizzly.config.dom.Transports;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"deprecation"})
@Service
public class GrizzlyConfigSchemaMigrator implements ConfigurationUpgrade, PostConstruct {

    private final static String SSL_CONFIGURATION_WANTAUTH = "com.sun.grizzly.ssl.auth";
    private final static String SSL_CONFIGURATION_SSLIMPL = "com.sun.grizzly.ssl.sslImplementation";

    @Inject
    private Configs configs;

    @Inject
    private Habitat habitat;

    private static final String HTTP_THREAD_POOL = "http-thread-pool";
    private static final String ASADMIN_LISTENER = "admin-listener";
    private static final String ASADMIN_VIRTUAL_SERVER = "__asadmin";

    public void postConstruct() {
        for (Config config : configs.getConfig()) {
            try {
                final NetworkConfig networkConfig = config.getNetworkConfig();
                if (networkConfig == null) {
                    createFromScratch(config);
                }
                normalizeThreadPools(config);
                processHttpListeners(config);
                promoteHttpServiceProperties(config.getHttpService());
                promoteVirtualServerProperties(config.getHttpService());
                promoteSystemProperties();
                addAsadminProtocol(config.getNetworkConfig());
            } catch (PropertyVetoException pve) {
                Logger.getAnonymousLogger().log(Level.SEVERE,
                        "Failure while upgrading domain.xml.", pve);
                throw new RuntimeException(pve);
            } catch (TransactionFailure tf) {
                Logger.getAnonymousLogger().log(Level.SEVERE,
                        "Failure while upgrading domain.xml.", tf);
                throw new RuntimeException(tf);
            }
        }
    }

    private void createFromScratch(Config config) throws TransactionFailure {
        normalizeThreadPools(config);
        getNetworkConfig(config);
    }


    private void promoteSystemProperties() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {
            @Override
            public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                final List<String> props = new ArrayList<String>(param.getJvmOptions());
                final Iterator<String> iterator = props.iterator();
                while (iterator.hasNext()) {
                    String prop = iterator.next();
                    if (prop.startsWith("-D")) {
                        final String[] parts = prop.split("=");
                        String name = parts[0].substring(2);
                        if (SSL_CONFIGURATION_WANTAUTH.equals(name) || SSL_CONFIGURATION_SSLIMPL.equals(name)) {
                            iterator.remove();
                            updateSsl(name, parts[1]);
                        }
                    }
                }
                param.setJvmOptions(props);
                return param;
            }
        }, habitat.getByType(JavaConfig.class));
    }

    private void updateSsl(final String propName, final String value) throws TransactionFailure {
        final Collection<Protocol> protocols = habitat.getAllByContract(Protocol.class);
        for (Protocol protocol : protocols) {
            final Ssl ssl = protocol.getSsl();
            if (ssl != null) {
                ConfigSupport.apply(new SingleConfigCode<Ssl>() {
                    @Override
                    public Object run(Ssl param) {
                        if (SSL_CONFIGURATION_WANTAUTH.equals(propName)) {
                            param.setClientAuth(value);
                        } else if (SSL_CONFIGURATION_SSLIMPL.equals(propName)) {
                            param.setClassname(value);
                        }
                        return param;
                    }
                }, ssl);
            }
        }
    }

    private void normalizeThreadPools(Config config) throws TransactionFailure {
        ThreadPools threadPools = config.getThreadPools();
        if (threadPools == null) {
            threadPools = createThreadPools(config);
        } else {
            final List<ThreadPool> list = threadPools.getThreadPool();
            boolean httpListenerFound = false;
            for (ThreadPool pool : list) {
                httpListenerFound |= HTTP_THREAD_POOL.equals(pool.getThreadPoolId()) || HTTP_THREAD_POOL.equals(pool.getName());
                if (pool.getName() == null) {
                    ConfigSupport.apply(new SingleConfigCode<ThreadPool>() {
                        public Object run(ThreadPool param) {
                            param.setName(param.getThreadPoolId());
                            param.setThreadPoolId(null);
                            if (param.getMinThreadPoolSize() == null || Integer.parseInt(param.getMinThreadPoolSize()) < 2) {
                                param.setMinThreadPoolSize("2");
                            }
                            return null;
                        }
                    }, pool);
                }
            }
            if (!httpListenerFound) {
                ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                    public Object run(ThreadPools param) throws TransactionFailure {
                        final ThreadPool pool = param.createChild(ThreadPool.class);
                        pool.setName(HTTP_THREAD_POOL);
                        param.getThreadPool().add(pool);
                        return null;
                    }
                }, threadPools);
            }
        }
        final NetworkConfig networkConfig = config.getNetworkConfig();
        if (networkConfig != null) {
            final NetworkListeners networkListeners = networkConfig.getNetworkListeners();
            if (networkListeners != null) {
                if (networkListeners.getThreadPool() != null && !networkListeners.getThreadPool().isEmpty()) {
                    ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                        public Object run(ThreadPools param) throws TransactionFailure {
                            migrateThreadPools(param);
                            return null;
                        }
                    }, threadPools);
                }
                final ConnectionPool pool = config.getHttpService().getConnectionPool();
//                for (ThreadPool threadPool : threadPools.getThreadPool()) {
//                    ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
//                        @Override
//                        public Object run(ConfigBeanProxy configBeanProxy) throws PropertyVetoException, TransactionFailure {
//                            return null;
//                        }
//                    }, threadPool);
//                }
            }
        }
    }

    private void promoteVirtualServerProperties(final HttpService service) throws TransactionFailure {
        for (VirtualServer virtualServer : service.getVirtualServer()) {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
                @Override
                public Object run(VirtualServer param) throws PropertyVetoException {
                    if (param.getHttpListeners() != null && !"".equals(param.getHttpListeners())) {
                        param.setNetworkListeners(param.getHttpListeners());
                    }
                    param.setHttpListeners(null);
                    final List<Property> propertyList = new ArrayList<Property>(param.getProperty());
                    final Iterator<Property> it = propertyList.iterator();
                    while (it.hasNext()) {
                        final Property property = it.next();
                        if ("docroot".equals(property.getName())) {
                            param.setDocroot(property.getValue());
                            it.remove();
                        } else if ("accesslog".equals(property.getName())) {
                            param.setAccessLog(property.getValue());
                            it.remove();
                        } else if ("sso-enabled".equals(property.getName())) {
                            param.setSsoEnabled(property.getValue());
                            it.remove();
                        }
                    }
                    param.getProperty().clear();
                    param.getProperty().addAll(propertyList);
                    return null;
                }
            }, virtualServer);
        }
    }

    private void promoteHttpServiceProperties(final HttpService service) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) {
                final List<Property> propertyList = new ArrayList<Property>(param.getProperty());
                final Iterator<Property> it = propertyList.iterator();
                while (it.hasNext()) {
                    final Property property = it.next();
                    if ("accessLoggingEnabled".equals(property.getName())) {
                        param.setAccessLoggingEnabled(property.getValue());
                        it.remove();
                    } else if ("accessLogBufferSize".equals(property.getName())) {
                        param.getAccessLog().setBufferSizeBytes(property.getValue());
                        it.remove();
                    } else if ("accessLogWriterInterval".equals(property.getName())) {
                        param.getAccessLog().setWriteIntervalSeconds(property.getValue());
                        it.remove();
                    } else if ("sso-enabled".equals(property.getName())) {
                        param.setSsoEnabled(property.getValue());
                        it.remove();
                    }
                }
                param.getProperty().clear();
                param.getProperty().addAll(propertyList);
                return null;
            }
        }, service);

    }

    private void processHttpListeners(Config config)
            throws PropertyVetoException, TransactionFailure {
        if (!config.getHttpService().getHttpListener().isEmpty()) {
            // all changes in this method must be in their own transactions
            migrateSettings(config);
        }
    }

    private void migrateThreadPools(final ThreadPools threadPools) throws TransactionFailure {
        final Config config = threadPools.getParent(Config.class);
        final NetworkListeners networkListeners = config.getNetworkConfig().getNetworkListeners();
        threadPools.getThreadPool().addAll(networkListeners.getThreadPool());
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            public Object run(NetworkListeners param) {
                param.getThreadPool().clear();
                return null;
            }
        }, networkListeners);
    }

    private ThreadPools createThreadPools(Config config) throws TransactionFailure {
        return (ThreadPools) ConfigSupport.apply(new SingleConfigCode<Config>() {
            public Object run(Config param) throws PropertyVetoException, TransactionFailure {
                final ThreadPools threadPools = param.createChild(ThreadPools.class);
                param.setThreadPools(threadPools);
                return threadPools;
            }
        }, config);
    }

    private void migrateSettings(Config config) throws PropertyVetoException, TransactionFailure {
        final HttpService service = config.getHttpService();
        NetworkConfig networkConfig = getNetworkConfig(config);
        migrateHttpListeners(config, networkConfig);
        migrateHttpProtocol(networkConfig, service);
        migrateHttpFileCache(networkConfig, service);
        migrateRequestProcessing(networkConfig, service);
        migrateKeepAlive(networkConfig, service);
        migrateConnectionPool(networkConfig, service);
    }

    private void migrateConnectionPool(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final ConnectionPool pool = httpService.getConnectionPool();
        if (pool == null) {
            return;
        }
/*
        final Transport transport = (Transport) ConfigSupport.apply(new SingleConfigCode<Transports>() {
            @Override
            public Object run(Transports param) throws TransactionFailure {
                final Transport transport = param.createChild(Transport.class);
                param.getTransport().add(transport);
                transport.setMaxConnectionsCount(pool.getMaxPendingCount());
                transport.setName("tcp");
                return transport;
            }
        }, getTransports(config));
        updateNetworkListener(config, transport);
*/
        updateHttp(config, pool);
        updateThreadPool(config, pool);
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) throws PropertyVetoException {
                param.setConnectionPool(null);
                return null;
            }
        }, httpService);
    }

    private void updateThreadPool(NetworkConfig config, final ConnectionPool pool) throws TransactionFailure {
        final Config parent = config.getParent(Config.class);
        for (ThreadPool threadPool : parent.getThreadPools().getThreadPool()) {
            ConfigSupport.apply(new SingleConfigCode<ThreadPool>() {
                @Override
                public Object run(ThreadPool param) {
                    param.setMaxQueueSize(pool.getQueueSizeInBytes());
                    if (param.getMinThreadPoolSize() == null || Integer.parseInt(param.getMinThreadPoolSize()) < 2) {
                        param.setMinThreadPoolSize("2");
                    }
                    return null;
                }
            }, threadPool);
        }
    }

    private void updateHttp(NetworkConfig config, final ConnectionPool pool) throws TransactionFailure {
        for (Protocol protocol : config.getProtocols().getProtocol()) {
            ConfigSupport.apply(new SingleConfigCode<Http>() {
                @Override
                public Object run(Http http) {
                    http.setSendBufferSizeBytes(pool.getSendBufferSizeInBytes());
                    return null;
                }
            }, protocol.getHttp());
        }
    }

    private void updateNetworkListener(NetworkConfig config, final Transport transport) throws TransactionFailure {
        for (NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
            ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                @Override
                public Object run(NetworkListener param) {
                    param.setTransport(transport.getName());
                    return null;
                }
            }, listener);
        }
    }

    private Transports getTransports(NetworkConfig config) throws TransactionFailure {
        Transports transports = config.getTransports();
        if (transports == null) {
            transports = (Transports) ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
                public Object run(NetworkConfig param) throws TransactionFailure {
                    final Transports child = param.createChild(Transports.class);
                    param.setTransports(child);
                    return child;
                }
            }, config);
        }
        return transports;

    }

    private void migrateKeepAlive(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final KeepAlive keepAlive = httpService.getKeepAlive();
        if (keepAlive == null) {
            return;
        }
        for (Protocol protocol : config.getProtocols().getProtocol()) {
            ConfigSupport.apply(new SingleConfigCode<Http>() {
                @Override
                public Object run(Http http) {
                    http.setMaxConnections(keepAlive.getMaxConnections());
                    http.setTimeoutSeconds(keepAlive.getTimeoutInSeconds());
                    return null;
                }
            }, protocol.getHttp());
        }
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) throws PropertyVetoException {
                param.setKeepAlive(null);
                return null;
            }
        }, httpService);
    }

    private void migrateRequestProcessing(final NetworkConfig config, HttpService httpService)
            throws TransactionFailure {
        final RequestProcessing request = httpService.getRequestProcessing();
        if (request == null) {
            return;
        }
        ConfigSupport.apply(new SingleConfigCode<ThreadPool>() {
            @Override
            public Object run(final ThreadPool pool) throws PropertyVetoException, TransactionFailure {
                pool.setMaxThreadPoolSize(request.getThreadCount());
                pool.setMinThreadPoolSize(request.getInitialThreadCount());
                if (pool.getMinThreadPoolSize() == null || Integer.parseInt(pool.getMinThreadPoolSize()) < 2) {
                    pool.setMinThreadPoolSize("2");
                }
                return null;
            }
        }, habitat.getComponent(ThreadPool.class, HTTP_THREAD_POOL));
        for (NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
            ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                @Override
                public Object run(NetworkListener param) {
                    param.setThreadPool(HTTP_THREAD_POOL);
                    return null;
                }
            }, listener);
        }
        for (Protocol protocol : config.getProtocols().getProtocol()) {
            ConfigSupport.apply(new SingleConfigCode<Http>() {
                @Override
                public Object run(Http http) {
                    http.setHeaderBufferLengthBytes(request.getHeaderBufferLengthInBytes());
                    return null;
                }
            }, protocol.getHttp());
        }
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) throws PropertyVetoException {
                param.setRequestProcessing(null);
                return null;
            }
        }, httpService);
    }

    private void migrateHttpFileCache(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final HttpFileCache httpFileCache = httpService.getHttpFileCache();
        if (httpFileCache == null) {
            return;
        }
        ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
            @Override
            public Object run(NetworkConfig param) throws TransactionFailure {
                for (Protocol protocol : param.getProtocols().getProtocol()) {
                    ConfigSupport.apply(new SingleConfigCode<Http>() {
                        @Override
                        public Object run(Http http) throws TransactionFailure {
                            final FileCache cache = http.createChild(FileCache.class);
                            http.setFileCache(cache);
                            cache.setEnabled(httpFileCache.getFileCachingEnabled());
                            cache.setMaxAgeSeconds(httpFileCache.getMaxAgeInSeconds());
                            cache.setMaxCacheSizeBytes(httpFileCache.getMediumFileSpaceInBytes());
                            cache.setMaxFilesCount(httpFileCache.getMaxFilesCount());
                            return null;
                        }
                    }, protocol.getHttp());
                }
                return null;
            }
        }, config);
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) throws PropertyVetoException {
                param.setHttpFileCache(null);
                return null;
            }
        }, httpService);
    }

    private void migrateHttpProtocol(NetworkConfig config, final HttpService httpService) throws TransactionFailure {
        final HttpProtocol httpProtocol = httpService.getHttpProtocol();
        if (httpProtocol == null) {
            return;
        }
        ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
            @Override
            public Object run(NetworkConfig networkConfig) throws TransactionFailure {
                final Protocols protocols = networkConfig.getProtocols();
                if(protocols.getProtocol().isEmpty()) {
                    createNewProtocols(httpService, protocols);
                }
                for (Protocol protocol : protocols.getProtocol()) {
                    ConfigSupport.apply(new SingleConfigCode<Http>() {
                        @Override
                        public Object run(Http http) {
                            http.setVersion(httpProtocol.getVersion());
                            http.setDnsLookupEnabled(httpProtocol.getDnsLookupEnabled());

                            // these are both deprecated and end up with the
                            // value 'AttributeDeprecated' if they exist
                            http.setForcedResponseType(null);
                            http.setDefaultResponseType(null);
                            return null;
                        }
                    }, protocol.getHttp());
                }
                return null;
            }
        }, config);
        ConfigSupport.apply(new SingleConfigCode<HttpService>() {
            @Override
            public Object run(HttpService param) throws PropertyVetoException {
                param.setHttpProtocol(null);
                return null;
            }
        }, httpService);
    }

    private void migrateHttpListeners(Config baseConfig, NetworkConfig config) throws TransactionFailure {
        for (final HttpListener listener : baseConfig.getHttpService().getHttpListener()) {
            final Protocol protocol = migrateToProtocols(config, listener);
            createNetworkListener(baseConfig, listener, protocol);
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {
                @Override
                public Object run(HttpService param) {
                    param.getHttpListener().remove(param.getHttpListenerById(listener.getId()));
                    return null;
                }
            }, baseConfig.getHttpService());
        }
    }

    private NetworkConfig getNetworkConfig(final Config baseConfig) throws TransactionFailure {
        NetworkConfig config = baseConfig.getNetworkConfig();
        if (config == null) {
            config = (NetworkConfig) ConfigSupport.apply(new SingleConfigCode<Config>() {
                public Object run(Config param) throws PropertyVetoException, TransactionFailure {
                    final NetworkConfig netConfig = param.createChild(NetworkConfig.class);
                    netConfig.setProtocols(netConfig.createChild(Protocols.class));
                    netConfig.setNetworkListeners(netConfig.createChild(NetworkListeners.class));
                    netConfig.setTransports(netConfig.createChild(Transports.class));
                    param.setNetworkConfig(netConfig);
                    return netConfig;
                }
            }, baseConfig);
        }
        return config;
    }

    private void createNewProtocols(final HttpService httpService, final Protocols protocols) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Protocols>() {
            @Override
            public Object run(Protocols param) throws TransactionFailure {
                for (final HttpListener httpListener : httpService.getHttpListener()) {
                    final Protocol protocol = param.createChild(Protocol.class);
                    param.getProtocol().add(protocol);
                    protocol.setName(httpListener.getId());
                    final Http http = protocol.createChild(Http.class);
                    http.setFileCache(http.createChild(FileCache.class));
                    http.setDefaultVirtualServer(
                            httpListener.getId().equals(ASADMIN_LISTENER) ? ASADMIN_VIRTUAL_SERVER : "server");
                    protocol.setHttp(http);
                }
                return null;
            }
        }, protocols);
    }

    private void createNetworkListener(Config baseConfig, final HttpListener listener, final Protocol protocol)
            throws TransactionFailure {
        final NetworkListener networkListener = (NetworkListener) ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            @Override
            public Object run(NetworkListeners param) throws TransactionFailure {
                final Iterator<NetworkListener> it = param.getNetworkListener().iterator();
                NetworkListener netListener = null;
                while (it.hasNext() && netListener == null) {
                    final NetworkListener next = it.next();
                    if (next.getName().equals(listener.getId())) {
                        netListener = next;
                        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                            @Override
                            public Object run(NetworkListener netParam) throws PropertyVetoException, TransactionFailure {
                                updateListener(netParam, listener, protocol);
                                return null;
                            }
                        }, netListener);
                    }
                }
                if (netListener == null) {
                    netListener = param.createChild(NetworkListener.class);
                    netListener.setName(listener.getId());
                    updateListener(netListener, listener, protocol);
                    param.getNetworkListener().add(netListener);
                }
                return netListener;
            }
        }, getNetworkListeners(baseConfig.getNetworkConfig()));

        Transport transport = networkListener.findTransport();
        if(transport == null) {
            transport = (Transport) ConfigSupport.apply(new SingleConfigCode<Transports>() {
                @Override
                public Object run(Transports param) throws PropertyVetoException, TransactionFailure {
                    final Transport child = param.createChild(Transport.class);
                    child.setName(networkListener.getTransport());
                    param.getTransport().add(child);
                    return child;
                }
            }, getTransports(networkListener.getParent().getParent()));
        }
        ConfigSupport.apply(new SingleConfigCode<Transport>() {
            @Override
            public Object run(Transport param) throws PropertyVetoException, TransactionFailure {
                param.setAcceptorThreads(listener.getAcceptorThreads());
                return null;
            }
        }, transport);
    }

    private void updateListener(NetworkListener netListener, HttpListener listener, Protocol protocol) {
        netListener.setEnabled(listener.getEnabled());
        netListener.setAddress(listener.getAddress());
        netListener.setPort(listener.getPort());
        netListener.setProtocol(protocol.getName());
        netListener.setTransport("tcp");
    }

    private NetworkListeners getNetworkListeners(NetworkConfig config) throws TransactionFailure {
        NetworkListeners listeners = config.getNetworkListeners();
        if (listeners == null) {
            listeners = (NetworkListeners) ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
                public Object run(NetworkConfig param) throws TransactionFailure {
                    final NetworkListeners child = param.createChild(NetworkListeners.class);
                    param.setNetworkListeners(child);
                    return child;
                }
            }, config);
        }
        return listeners;
    }

    private Protocol migrateToProtocols(NetworkConfig config, final HttpListener listener)
            throws TransactionFailure {
        final Protocols protocols = getProtocols(config);
        return (Protocol) ConfigSupport.apply(new SingleConfigCode<Protocols>() {
            public Object run(Protocols param) throws TransactionFailure {
                final Protocol protocol = param.createChild(Protocol.class);
                final Ssl ssl = listener.getSsl();
                param.getProtocol().add(protocol);
                protocol.setName(listener.getId());
                protocol.setSsl(ssl);
                protocol.setSecurityEnabled(listener.getSecurityEnabled());
                createHttp(protocol, listener);
                return protocol;
            }
        }, protocols);
    }

    public static Protocols getProtocols(NetworkConfig config) throws TransactionFailure {
        Protocols protocols = config.getProtocols();
        if (protocols == null) {
            protocols = (Protocols) ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
                public Object run(NetworkConfig param) throws TransactionFailure {
                    final Protocols child = param.createChild(Protocols.class);
                    param.setProtocols(child);
                    return child;
                }
            }, config);
        }
        return protocols;
    }

    private void createHttp(Protocol protocol, HttpListener listener) throws TransactionFailure {
        Http http = protocol.createChild(Http.class);
        http.setFileCache(http.createChild(FileCache.class));
        protocol.setHttp(http);
        http.setDefaultVirtualServer(listener.getDefaultVirtualServer());
        http.setServerName(listener.getServerName());
        http.setRedirectPort(listener.getRedirectPort());
        http.setXpoweredBy(listener.getXpoweredBy());
    }

    private Protocol addAsadminProtocol(NetworkConfig config)
            throws TransactionFailure {
        final Protocols protocols = getProtocols(config);
        Protocol adminProtocol = protocols.findProtocol(ASADMIN_LISTENER);
        if (adminProtocol == null) {
            return (Protocol) ConfigSupport.apply(new SingleConfigCode<Protocols>() {
                public Object run(Protocols param) throws TransactionFailure {
                    final Protocol protocol = param.createChild(Protocol.class);
                    param.getProtocol().add(protocol);
                    protocol.setName(ASADMIN_LISTENER);
                    Http http = protocol.createChild(Http.class);
                    http.setFileCache(http.createChild(FileCache.class));
                    protocol.setHttp(http);
                    http.setDefaultVirtualServer(ASADMIN_VIRTUAL_SERVER);
                    http.setMaxConnections("250");
                    return protocol;
                }
            }, protocols);
        } else {
            return null;
        }
    }
}
