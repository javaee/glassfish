package org.glassfish.config.support;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.ThreadPools;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.grizzly.config.dom.FileCache;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import org.jvnet.hk2.config.types.Property;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@SuppressWarnings({"deprecation"})
@Service
public class GrizzlyConfigSchemaMigrator implements ConfigurationUpgrade, PostConstruct {
    private final static String SSL_CONFIGURATION_WANTAUTH = "com.sun.grizzly.ssl.auth";

    private final static String SSL_CONFIGURATION_SSLIMPL = "com.sun.grizzly.ssl.sslImplementation";

    @Inject
    private Domain domain;

    @Inject
    private Habitat habitat;

    public void postConstruct() {
        try {
            final Config config = domain.getConfigs().getConfig().get(0);
            processHttpListeners(config);
            promoteHttpServiceProperties(config.getHttpService());
            promoteVirtualServerProperties(config.getHttpService());
            promoteSystemProperties();
            moveThreadPools(config);
        } catch (TransactionFailure tf) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading domain.xml.  Please redeploy", tf);
            throw new RuntimeException(tf);
        } catch (PropertyVetoException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading domain.xml.  Please redeploy", e);
            throw new RuntimeException(e);
        }
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
                        if (SSL_CONFIGURATION_WANTAUTH.equals(name)
                            || SSL_CONFIGURATION_SSLIMPL.equals(name)) {
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
                    public Object run(Ssl param) throws PropertyVetoException, TransactionFailure {
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

    private void moveThreadPools(Config config) throws TransactionFailure, PropertyVetoException {
        if (config.getNetworkConfig().getNetworkListeners().getThreadPool() != null) {
            ThreadPools threadPools = config.getThreadPools();
            if (threadPools == null) {
                threadPools = createThreadPools();
            }
            ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                public Object run(ThreadPools param) throws TransactionFailure {
                    migrateThreadPools(param);
                    return null;
                }
            }, threadPools);
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

    private void processHttpListeners(Config config) throws TransactionFailure {
        if (!config.getHttpService().getHttpListener().isEmpty()) {
            ConfigSupport.apply(new SingleConfigCode<Domain>() {
                public Object run(Domain param) throws TransactionFailure {
                    migrateSettings(param);
                    return null;
                }
            }, domain);
        }
    }

    private void migrateThreadPools(final ThreadPools threadPools) throws TransactionFailure {
        final Config config = threadPools.getParent(Config.class);
        final NetworkListeners networkListeners = config.getNetworkConfig().getNetworkListeners();
        threadPools.getThreadPool().addAll(networkListeners.getThreadPool());
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            public Object run(NetworkListeners param) throws PropertyVetoException {
                param.getThreadPool().clear();
                return null;
            }
        }, networkListeners);
    }

    private ThreadPools createThreadPools() throws TransactionFailure, PropertyVetoException {
        return (ThreadPools) ConfigSupport.apply(new SingleConfigCode<Config>() {
            public Object run(Config param) throws PropertyVetoException, TransactionFailure {
                final ThreadPools threadPools = param.createChild(ThreadPools.class);
                param.setThreadPools(threadPools);
                return threadPools;
            }
        }, domain.getConfigs().getConfig().get(0));
    }

    private void migrateSettings(Domain domain) throws TransactionFailure {
        final Configs configs = domain.getConfigs();
        Config baseConfig = configs.getConfig().get(0);
        final HttpService service = baseConfig.getHttpService();
        NetworkConfig config = getNetworkConfig(baseConfig);
        migrateHttpListeners(baseConfig, config);
        migrateHttpProtocol(config, service);
        migrateHttpFileCache(config, service);
        migrateRequestProcessing(config, service);
        migrateKeepAlive(config, service);
        migrateConnectionPool(config, service);
    }

    private void migrateConnectionPool(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final ConnectionPool pool = httpService.getConnectionPool();
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
        for (ThreadPool threadPool : config.getNetworkListeners().getThreadPool()) {
            ConfigSupport.apply(new SingleConfigCode<ThreadPool>() {
                @Override
                public Object run(ThreadPool param) {
                    param.setMaxQueueSize(pool.getQueueSizeInBytes());
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
        Transports listeners = config.getTransports();
        if (listeners == null) {
            listeners = (Transports) ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
                public Object run(NetworkConfig param) throws TransactionFailure {
                    final Transports child = param.createChild(Transports.class);
                    param.setTransports(child);
                    return child;
                }
            }, config);
        }
        return listeners;

    }

    private void migrateKeepAlive(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final KeepAlive keepAlive = httpService.getKeepAlive();
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
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            @Override
            public Object run(NetworkListeners listeners) throws TransactionFailure {
                final ThreadPool pool = listeners.createChild(ThreadPool.class);
                listeners.getThreadPool().add(pool);
                pool.setName("http-thread-pool");
                pool.setMaxThreadPoolSize(request.getThreadCount());
                pool.setMinThreadPoolSize(request.getInitialThreadCount());
                for (Protocol protocol : config.getProtocols().getProtocol()) {
                    ConfigSupport.apply(new SingleConfigCode<Http>() {
                        @Override
                        public Object run(Http http) {
                            http.setHeaderBufferLengthBytes(request.getHeaderBufferLengthInBytes());
                            return null;
                        }
                    }, protocol.getHttp());
                }
                for (NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
                    ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                        @Override
                        public Object run(NetworkListener param) {
                            param.setThreadPool(pool.getName());
                            return null;
                        }
                    }, listener);
                }
                return null;
            }
        }, config.getNetworkListeners());
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

    private void migrateHttpProtocol(NetworkConfig config, HttpService httpService) throws TransactionFailure {
        final HttpProtocol httpProtocol = httpService.getHttpProtocol();
        ConfigSupport.apply(new SingleConfigCode<NetworkConfig>() {
            @Override
            public Object run(NetworkConfig param) throws TransactionFailure {
                for (Protocol protocol : param.getProtocols().getProtocol()) {
                    ConfigSupport.apply(new SingleConfigCode<Http>() {
                        @Override
                        public Object run(Http http) {
                            http.setVersion(httpProtocol.getVersion());
                            http.setDnsLookupEnabled(httpProtocol.getDnsLookupEnabled());
                            http.setForcedResponseType(httpProtocol.getForcedResponseType());
                            http.setDefaultResponseType(httpProtocol.getDefaultResponseType());
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
                    final HttpListener listener1 = param.getHttpListenerById(listener.getId());
                    param.getHttpListener().remove(listener1);
                    return null;
                }
            }, baseConfig.getHttpService());
        }
    }

    private NetworkConfig getNetworkConfig(Config baseConfig) throws TransactionFailure {
        NetworkConfig config = baseConfig.getNetworkConfig();
        if (config == null) {
            config = (NetworkConfig) ConfigSupport.apply(new SingleConfigCode<Config>() {
                public Object run(Config param) throws PropertyVetoException, TransactionFailure {
                    final NetworkConfig child = param.createChild(NetworkConfig.class);
                    param.setNetworkConfig(child);
                    return child;
                }
            }, baseConfig);
        }
        return config;
    }

    private void createNetworkListener(Config baseConfig, final HttpListener listener, final Protocol protocol)
        throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            @Override
            public Object run(NetworkListeners param) throws TransactionFailure {
                NetworkListener netListener = param.createChild(NetworkListener.class);
                netListener.setName(listener.getId());
                netListener.setEnabled(listener.getEnabled());
                netListener.setAddress(listener.getAddress());
                netListener.setPort(listener.getPort());
                netListener.setProtocol(protocol.getName());
                param.getNetworkListener().add(netListener);
                return null;
            }
        }, getNetworkListeners(baseConfig.getNetworkConfig()));
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
        protocol.setHttp(http);
        http.setDefaultVirtualServer(listener.getDefaultVirtualServer());
        http.setServerName(listener.getServerName());
        http.setRedirectPort(listener.getRedirectPort());
        http.setXpoweredBy(listener.getXpoweredBy());
    }
}
