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



package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.admin.ConfigBean;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.sun.enterprise.config.serverbeans package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
public class ObjectFactory extends ConfigBean {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.enterprise.config.serverbeans
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AuditModule }
     */
    public AuditModule createAuditModule() {
        return new AuditModule();
    }

    /**
     * Create an instance of {@link EjbModule }
     */
    public EjbModule createEjbModule() {
        return new EjbModule();
    }

    /**
     * Create an instance of {@link HttpFileCache }
     */
    public HttpFileCache createHttpFileCache() {
        return new HttpFileCache();
    }

    /**
     * Create an instance of {@link JmsAvailability }
     */
    public JmsAvailability createJmsAvailability() {
        return new JmsAvailability();
    }

    /**
     * Create an instance of {@link LoadBalancer }
     */
    public LoadBalancer createLoadBalancer() {
        return new LoadBalancer();
    }

    /**
     * Create an instance of {@link LoadBalancers }
     */
    public LoadBalancers createLoadBalancers() {
        return new LoadBalancers();
    }

    /**
     * Create an instance of {@link AppclientModule }
     */
    public AppclientModule createAppclientModule() {
        return new AppclientModule();
    }

    /**
     * Create an instance of {@link Servers }
     */
    public Servers createServers() {
        return new Servers();
    }

    /**
     * Create an instance of {@link JaccProvider }
     */
    public JaccProvider createJaccProvider() {
        return new JaccProvider();
    }

    /**
     * Create an instance of {@link WebContainer }
     */
    public WebContainer createWebContainer() {
        return new WebContainer();
    }

    /**
     * Create an instance of {@link MailResource }
     */
    public MailResource createMailResource() {
        return new MailResource();
    }

    /**
     * Create an instance of {@link HttpListener }
     */
    public HttpListener createHttpListener() {
        return new HttpListener();
    }

    /**
     * Create an instance of {@link AdminService }
     */
    public AdminService createAdminService() {
        return new AdminService();
    }

    /**
     * Create an instance of {@link PersistenceManagerFactoryResource }
     */
    public PersistenceManagerFactoryResource createPersistenceManagerFactoryResource() {
        return new PersistenceManagerFactoryResource();
    }

    /**
     * Create an instance of {@link SystemProperty }
     */
    public SystemProperty createSystemProperty() {
        return new SystemProperty();
    }

    /**
     * Create an instance of {@link Server }
     */
    public Server createServer() {
        return new Server();
    }

    /**
     * Create an instance of {@link KeepAlive }
     */
    public KeepAlive createKeepAlive() {
        return new KeepAlive();
    }

    /**
     * Create an instance of {@link Orb }
     */
    public Orb createOrb() {
        return new Orb();
    }

    /**
     * Create an instance of {@link Action }
     */
    public Action createAction() {
        return new Action();
    }

    /**
     * Create an instance of {@link ExternalJndiResource }
     */
    public ExternalJndiResource createExternalJndiResource() {
        return new ExternalJndiResource();
    }

    /**
     * Create an instance of {@link RequestProcessing }
     */
    public RequestProcessing createRequestProcessing() {
        return new RequestProcessing();
    }

    /**
     * Create an instance of {@link LogService }
     */
    public LogService createLogService() {
        return new LogService();
    }

    /**
     * Create an instance of {@link JmxConnector }
     */
    public JmxConnector createJmxConnector() {
        return new JmxConnector();
    }

    /**
     * Create an instance of {@link HttpService }
     */
    public HttpService createHttpService() {
        return new HttpService();
    }

    /**
     * Create an instance of {@link J2EeApplication }
     */
    public J2EeApplication createJ2EeApplication() {
        return new J2EeApplication();
    }

    /**
     * Create an instance of {@link GroupManagementService }
     */
    public GroupManagementService createGroupManagementService() {
        return new GroupManagementService();
    }

    /**
     * Create an instance of {@link WebContainerAvailability }
     */
    public WebContainerAvailability createWebContainerAvailability() {
        return new WebContainerAvailability();
    }

    /**
     * Create an instance of {@link Property }
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link ConnectorModule }
     */
    public ConnectorModule createConnectorModule() {
        return new ConnectorModule();
    }

    /**
     * Create an instance of {@link ManagerProperties }
     */
    public ManagerProperties createManagerProperties() {
        return new ManagerProperties();
    }

    /**
     * Create an instance of {@link SessionConfig }
     */
    public SessionConfig createSessionConfig() {
        return new SessionConfig();
    }

    /**
     * Create an instance of {@link SecurityMap }
     */
    public SecurityMap createSecurityMap() {
        return new SecurityMap();
    }

    /**
     * Create an instance of {@link ModuleMonitoringLevels }
     */
    public ModuleMonitoringLevels createModuleMonitoringLevels() {
        return new ModuleMonitoringLevels();
    }

    /**
     * Create an instance of {@link JmsHost }
     */
    public JmsHost createJmsHost() {
        return new JmsHost();
    }

    /**
     * Create an instance of {@link ApplicationRef }
     */
    public ApplicationRef createApplicationRef() {
        return new ApplicationRef();
    }

    /**
     * Create an instance of {@link AlertSubscription }
     */
    public AlertSubscription createAlertSubscription() {
        return new AlertSubscription();
    }

    /**
     * Create an instance of {@link ManagementRule }
     */
    public ManagementRule createManagementRule() {
        return new ManagementRule();
    }

    /**
     * Create an instance of {@link MessageSecurityConfig }
     */
    public MessageSecurityConfig createMessageSecurityConfig() {
        return new MessageSecurityConfig();
    }

    /**
     * Create an instance of {@link EjbTimerService }
     */
    public EjbTimerService createEjbTimerService() {
        return new EjbTimerService();
    }

    /**
     * Create an instance of {@link ListenerConfig }
     */
    public ListenerConfig createListenerConfig() {
        return new ListenerConfig();
    }

    /**
     * Create an instance of {@link AuthRealm }
     */
    public AuthRealm createAuthRealm() {
        return new AuthRealm();
    }

    /**
     * Create an instance of {@link SessionManager }
     */
    public SessionManager createSessionManager() {
        return new SessionManager();
    }

    /**
     * Create an instance of {@link RegistryLocation }
     */
    public RegistryLocation createRegistryLocation() {
        return new RegistryLocation();
    }

    /**
     * Create an instance of {@link AvailabilityService }
     */
    public AvailabilityService createAvailabilityService() {
        return new AvailabilityService();
    }

    /**
     * Create an instance of {@link Event }
     */
    public Event createEvent() {
        return new Event();
    }

    /**
     * Create an instance of {@link ConnectorConnectionPool }
     */
    public ConnectorConnectionPool createConnectorConnectionPool() {
        return new ConnectorConnectionPool();
    }

    /**
     * Create an instance of {@link BackendPrincipal }
     */
    public BackendPrincipal createBackendPrincipal() {
        return new BackendPrincipal();
    }

    /**
     * Create an instance of {@link SessionProperties }
     */
    public SessionProperties createSessionProperties() {
        return new SessionProperties();
    }

    /**
     * Create an instance of {@link WebModule }
     */
    public WebModule createWebModule() {
        return new WebModule();
    }

    /**
     * Create an instance of {@link ManagementRules }
     */
    public ManagementRules createManagementRules() {
        return new ManagementRules();
    }

    /**
     * Create an instance of {@link Resources }
     */
    public Resources createResources() {
        return new Resources();
    }

    /**
     * Create an instance of {@link JdbcResource }
     */
    public JdbcResource createJdbcResource() {
        return new JdbcResource();
    }

    /**
     * Create an instance of {@link ModuleLogLevels }
     */
    public ModuleLogLevels createModuleLogLevels() {
        return new ModuleLogLevels();
    }

    /**
     * Create an instance of {@link AccessLog }
     */
    public AccessLog createAccessLog() {
        return new AccessLog();
    }

    /**
     * Create an instance of {@link JdbcConnectionPool }
     */
    public JdbcConnectionPool createJdbcConnectionPool() {
        return new JdbcConnectionPool();
    }

    /**
     * Create an instance of {@link TransactionService }
     */
    public TransactionService createTransactionService() {
        return new TransactionService();
    }

    /**
     * Create an instance of {@link ExtensionModule }
     */
    public ExtensionModule createExtensionModule() {
        return new ExtensionModule();
    }

    /**
     * Create an instance of {@link NodeAgents }
     */
    public NodeAgents createNodeAgents() {
        return new NodeAgents();
    }

    /**
     * Create an instance of {@link WebServiceEndpoint }
     */
    public WebServiceEndpoint createWebServiceEndpoint() {
        return new WebServiceEndpoint();
    }

    /**
     * Create an instance of {@link ResponsePolicy }
     */
    public ResponsePolicy createResponsePolicy() {
        return new ResponsePolicy();
    }

    /**
     * Create an instance of {@link ConnectorService }
     */
    public ConnectorService createConnectorService() {
        return new ConnectorService();
    }

    /**
     * Create an instance of {@link JavaConfig }
     */
    public JavaConfig createJavaConfig() {
        return new JavaConfig();
    }

    /**
     * Create an instance of {@link AdminObjectResource }
     */
    public AdminObjectResource createAdminObjectResource() {
        return new AdminObjectResource();
    }

    /**
     * Create an instance of {@link ConnectionPool }
     */
    public ConnectionPool createConnectionPool() {
        return new ConnectionPool();
    }

    /**
     * Create an instance of {@link StoreProperties }
     */
    public StoreProperties createStoreProperties() {
        return new StoreProperties();
    }

    /**
     * Create an instance of {@link SecurityService }
     */
    public SecurityService createSecurityService() {
        return new SecurityService();
    }

    /**
     * Create an instance of {@link EjbContainerAvailability }
     */
    public EjbContainerAvailability createEjbContainerAvailability() {
        return new EjbContainerAvailability();
    }

    /**
     * Create an instance of {@link ThreadPool }
     */
    public ThreadPool createThreadPool() {
        return new ThreadPool();
    }

    /**
     * Create an instance of {@link Mbean }
     */
    public Mbean createMbean() {
        return new Mbean();
    }

    /**
     * Create an instance of {@link Ssl }
     */
    public Ssl createSsl() {
        return new Ssl();
    }

    /**
     * Create an instance of {@link Domain }
     */
    public Domain createDomain() {
        return new Domain();
    }

    /**
     * Create an instance of {@link ClusterRef }
     */
    public ClusterRef createClusterRef() {
        return new ClusterRef();
    }

    /**
     * Create an instance of {@link JmsService }
     */
    public JmsService createJmsService() {
        return new JmsService();
    }

    /**
     * Create an instance of {@link HealthChecker }
     */
    public HealthChecker createHealthChecker() {
        return new HealthChecker();
    }

    /**
     * Create an instance of {@link ConnectorResource }
     */
    public ConnectorResource createConnectorResource() {
        return new ConnectorResource();
    }

    /**
     * Create an instance of {@link Profiler }
     */
    public Profiler createProfiler() {
        return new Profiler();
    }

    /**
     * Create an instance of {@link ServerRef }
     */
    public ServerRef createServerRef() {
        return new ServerRef();
    }

    /**
     * Create an instance of {@link TransformationRule }
     */
    public TransformationRule createTransformationRule() {
        return new TransformationRule();
    }

    /**
     * Create an instance of {@link Cluster }
     */
    public Cluster createCluster() {
        return new Cluster();
    }

    /**
     * Create an instance of {@link DasConfig }
     */
    public DasConfig createDasConfig() {
        return new DasConfig();
    }

    /**
     * Create an instance of {@link MonitoringService }
     */
    public MonitoringService createMonitoringService() {
        return new MonitoringService();
    }

    /**
     * Create an instance of {@link HttpProtocol }
     */
    public HttpProtocol createHttpProtocol() {
        return new HttpProtocol();
    }

    /**
     * Create an instance of {@link IiopListener }
     */
    public IiopListener createIiopListener() {
        return new IiopListener();
    }

    /**
     * Create an instance of {@link NodeAgent }
     */
    public NodeAgent createNodeAgent() {
        return new NodeAgent();
    }

    /**
     * Create an instance of {@link RequestPolicy }
     */
    public RequestPolicy createRequestPolicy() {
        return new RequestPolicy();
    }

    /**
     * Create an instance of {@link EjbContainer }
     */
    public EjbContainer createEjbContainer() {
        return new EjbContainer();
    }

    /**
     * Create an instance of {@link VirtualServer }
     */
    public VirtualServer createVirtualServer() {
        return new VirtualServer();
    }

    /**
     * Create an instance of {@link ThreadPools }
     */
    public ThreadPools createThreadPools() {
        return new ThreadPools();
    }

    /**
     * Create an instance of {@link HttpAccessLog }
     */
    public HttpAccessLog createHttpAccessLog() {
        return new HttpAccessLog();
    }

    /**
     * Create an instance of {@link IiopService }
     */
    public IiopService createIiopService() {
        return new IiopService();
    }

    /**
     * Create an instance of {@link Config }
     */
    public Config createConfig() {
        return new Config();
    }

    /**
     * Create an instance of {@link AlertService }
     */
    public AlertService createAlertService() {
        return new AlertService();
    }

    /**
     * Create an instance of {@link Configs }
     */
    public Configs createConfigs() {
        return new Configs();
    }

    /**
     * Create an instance of {@link MdbContainer }
     */
    public MdbContainer createMdbContainer() {
        return new MdbContainer();
    }

    /**
     * Create an instance of {@link Clusters }
     */
    public Clusters createClusters() {
        return new Clusters();
    }

    /**
     * Create an instance of {@link DiagnosticService }
     */
    public DiagnosticService createDiagnosticService() {
        return new DiagnosticService();
    }

    /**
     * Create an instance of {@link LbConfig }
     */
    public LbConfig createLbConfig() {
        return new LbConfig();
    }

    /**
     * Create an instance of {@link SslClientConfig }
     */
    public SslClientConfig createSslClientConfig() {
        return new SslClientConfig();
    }

    /**
     * Create an instance of {@link LbConfigs }
     */
    public LbConfigs createLbConfigs() {
        return new LbConfigs();
    }

    /**
     * Create an instance of {@link ResourceRef }
     */
    public ResourceRef createResourceRef() {
        return new ResourceRef();
    }

    /**
     * Create an instance of {@link Applications }
     */
    public Applications createApplications() {
        return new Applications();
    }

    /**
     * Create an instance of {@link ResourceAdapterConfig }
     */
    public ResourceAdapterConfig createResourceAdapterConfig() {
        return new ResourceAdapterConfig();
    }

    /**
     * Create an instance of {@link FilterConfig }
     */
    public FilterConfig createFilterConfig() {
        return new FilterConfig();
    }

    /**
     * Create an instance of {@link ProviderConfig }
     */
    public ProviderConfig createProviderConfig() {
        return new ProviderConfig();
    }

    /**
     * Create an instance of {@link LifecycleModule }
     */
    public LifecycleModule createLifecycleModule() {
        return new LifecycleModule();
    }

    /**
     * Create an instance of {@link CustomResource }
     */
    public CustomResource createCustomResource() {
        return new CustomResource();
    }

}
