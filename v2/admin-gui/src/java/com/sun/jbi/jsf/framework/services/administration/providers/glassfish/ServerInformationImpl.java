/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * ServerInformationImpl 
 */
 
package com.sun.jbi.jsf.framework.services.administration.providers.glassfish;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.StandardMBean;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.jbi.jsf.framework.services.administration.providers.ServerEventBroadcaster;
import com.sun.jbi.jsf.framework.services.administration.providers.ServerEventListener;


import com.sun.jbi.jsf.framework.common.GenericConstants;

/**
 * This is a helper class used to get Application Server domain configuration
 * and runtime context information.
 * 
 * @author graj
 * 
 */
public class ServerInformationImpl implements ServerInformation,
        NotificationListener, ServerEventBroadcaster, Serializable {

    private boolean mSupportsMultipleServers;

    transient private Logger mLog;

    transient private MBeanServer mMBeanServer;

    transient private MBeanServerConnection mbeanServerConnection;

    transient private InstanceRegistry mInstanceRegistry;

    transient private List<ServerEventListener> mListeners;

    private static final String SERVER = "server";

    private static final String INSTANCE_ROOT_KEY = "com.sun.aas.instanceRoot";

    private static final String INSTALL_ROOT_KEY = "com.sun.aas.installRoot";

    static final long serialVersionUID = -7234860345825372291L;

    /**
     * No argument constructor
     */
    public ServerInformationImpl() {
    }

    /**
     * Constructor
     * 
     * @param connection
     * @throws InstanceNotFoundException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws IOException
     */
    public ServerInformationImpl(MBeanServerConnection connection)
            throws InstanceNotFoundException, MalformedObjectNameException,
            NullPointerException, IOException {
        this.mbeanServerConnection = connection;
        this.initialize();
    }

    /**
     * Constructor
     * 
     * @param server
     * @throws InstanceNotFoundException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws IOException
     */
    public ServerInformationImpl(MBeanServer server)
            throws InstanceNotFoundException, MalformedObjectNameException,
            NullPointerException, IOException {
        this.mMBeanServer = server;
        this.initialize();
    }

    /**
     * Initializes the class
     * 
     * @throws InstanceNotFoundException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws IOException
     */
    private void initialize() throws InstanceNotFoundException,
            MalformedObjectNameException, NullPointerException, IOException {
        mLog = java.util.logging.Logger
                .getLogger("com.sun.jbi.jsf.framework.services.administration.providers.glassfish");

        if (true == this.isDAS()) {
            MBeanServer mbeanServer = ManagementFactory
                    .getPlatformMBeanServer();
            DomainRoot domainRoot = ProxyFactory.getInstance(mbeanServer)
                    .getDomainRoot();

            mSupportsMultipleServers = domainRoot.getSystemInfo()
                    .supportsFeature(SystemInfo.MULTIPLE_SERVERS_FEATURE);

            if (supportsMultipleServers()) {
                mInstanceRegistry = InstanceRegistry.getInstanceRegistry();
                /*
                 * This is not working MBeanServerNotificationFilter myFilter =
                 * new MBeanServerNotificationFilter();
                 * //myFilter.enableObjectName(new
                 * ObjectName("amx:j2eeType=J2EEServer,*"));
                 * myFilter.enableObjectName(new
                 * ObjectName("amx:j2eeType=J2EEServer,*,"));
                 * 
                 * java.util.Vector<ObjectName> names =
                 * myFilter.getEnabledObjectNames();
                 * 
                 * for ( ObjectName name : names ) { System.out.println("Enabled
                 * Object Name : " + name); } myFilter.enableType("*");
                 */

                if (this.mMBeanServer != null) {
                    this.mMBeanServer.addNotificationListener(new ObjectName(
                            "JMImplementation:type=MBeanServerDelegate"), this,
                            null, null);
                } else {
                    this.mbeanServerConnection
                            .addNotificationListener(
                                    new ObjectName(
                                            "JMImplementation:type=MBeanServerDelegate"),
                                    this, null, null);
                }
            }
        }
    }

    /**
     * 
     * 
     * @return a set of all clusters in the domain
     */
    public Set<String> getClusterNames() throws ConfigException {
        ConfigContext context = getConfigContext();
        Cluster[] clusters = null;
        if (context != null) {
            clusters = ClusterHelper.getClustersInDomain(context);
        }
        if (clusters != null) {
            return getClusterNames(clusters);
        }
        return Collections.synchronizedSet(new HashSet<String>());
    }

    /**
     * 
     * 
     * @return a set of names of all clustered servers in the domain.
     */
    public Set<String> getClusteredServerNames() throws ConfigException {
        Set<String> clusteredServers = Collections
                .synchronizedSet(new HashSet<String>());

        Set<String> allClusters = getClusterNames();

        for (String clusterName : allClusters) {
            clusteredServers.addAll(getServersInCluster(clusterName));
        }

        mLog.fine("Clustered servers in the domain are "
                + clusteredServers.toString());
        return clusteredServers;
    }

    /**
     * 
     * 
     * @return the instance root property
     */
    public String getInstallRootKey() {
        return INSTALL_ROOT_KEY;
    }

    /**
     * Get the name of this instance. This would be called by both the DAS
     * server instances and a stand alone / clustered instance.
     * 
     * 
     * @return the name of this server instance
     */
    public String getInstanceName() {
        return System.getProperty("com.sun.aas.instanceName");
    }

    /**
     * 
     * 
     * @return the instance root property
     */
    public String getInstanceRootKey() {
        return INSTANCE_ROOT_KEY;
    }

    /**
     * 
     * 
     * @param forceNew
     *            if set to true a new connection is created
     * @return the MBeanServerConnection for a Server instance
     *      boolean)
     */
    public MBeanServerConnection getMBeanServerConnection(String instanceName,
			boolean forceNew) throws IOException {
    	
    	MBeanServerConnection connection = null;
		try {
			if ( instanceName!=null ) {
				if (SERVER.equals(instanceName)) {
					if (this.mMBeanServer != null) {
						connection = this.mMBeanServer;
					} else {
						connection = this.mbeanServerConnection;
					}
				} else if (GenericConstants.DOMAIN_SERVER.equals(instanceName)) {
					// no support for Domain server
				} else {
					if (isInstanceClustered(instanceName)) {
                                            // get the cluster name
                                            String clusterName = getTargetName(instanceName);
						Set<String> serverNames = getServersInCluster(clusterName);
						if (serverNames != null) {
                                                    for (Iterator iter=serverNames.iterator(); iter.hasNext(); ) {
                                                        String serverName = (String)iter.next();
                                                        if ( instanceName.equalsIgnoreCase(serverName) ) {
                                                            connection = mInstanceRegistry.getInstanceConnection(serverName);
                                                            break;
                                                        }
                                                    }
						} 
	
					} else {
                                             connection =  mInstanceRegistry.getInstanceConnection(instanceName);
					}
				}
			}
		} catch (Exception e) {
                    e.printStackTrace();
		}
		
		return connection;
	}

    /**
	 * 
	 * 
	 * @return a set of servers in a cluster
	 */
    public Set<String> getServersInCluster(String clusterName)
            throws ConfigException {

        Server[] servers;
        servers = ServerHelper.getServersInCluster(getConfigContext(),
                clusterName);
        Set<String> serverNames = getServerNames(servers);
        mLog.fine("Servers in cluster " + clusterName + " are "
                + serverNames.toString());
        return serverNames;
    }

    /**
     * 
     * 
     * @return a set of names of all stand alone servers in the domain.
     */
    public Set<String> getStandaloneServerNames() throws ConfigException {
        ConfigContext context = getConfigContext();

        Server[] servers = null;
        Set<String> serverNames = null;
        if (context != null) {
            servers = ServerHelper.getUnclusteredServers(context, false);
            if (servers != null) {
                serverNames = getServerNames(servers);
                if (serverNames != null) {
                    mLog.fine("Unclustered servers in the domain are "
                            + serverNames.toString());
                    return serverNames;
                }
            }
        }
        return Collections.synchronizedSet(new HashSet<String>());
    }

    /**
     * Get the Target Name. If the instance is a clustered instance then the
     * target is the instance name. If it is a part of a cluster then it is the
     * cluster name.
     * 
     * 
     * @return the target name.
     */
    public String getTargetName() throws ConfigException {
        String instanceName = getInstanceName();
        return getTargetName(instanceName);
    }

    /**
     * Get the Target Name for a specified instance. If the instance is not
     * clustered the instance name is returned. This operation is invoked by the
     * JBI instance MBeans only.
     * 
     * 
     * @return the target name.
     */
    public String getTargetName(String instanceName) throws ConfigException {
        String targetName = instanceName;
        if (isInstanceClustered(instanceName)) {
            // -- Target is the cluster name
            targetName = getClusterForInstance(instanceName);
        }
        return targetName;
    }

    /**
     * 
     * 
     * @return true if the target is a cluster
     */
    public boolean isCluster(String targetName) throws ConfigException {
        return getClusterNames().contains(targetName);
    }

    /**
     * 
     * 
     * @return true if the target is a standalone server
     */
    public boolean isClusteredServer(String targetName) throws ConfigException {
        return getClusteredServerNames().contains(targetName);
    }

    /**
     * 
     * 
     * @return true if this instance is the DAS
     */
    public boolean isDAS() {
        AdminService adminService = AdminService.getAdminService();
        boolean isDAS = false;
        if (adminService != null) {
            isDAS = adminService.isDas();
        }
        return isDAS;
    }

    /**
     * 
     * 
     * @return true if the instance is up and running, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean isInstanceUp(String instanceName)
            throws MalformedObjectNameException, NullPointerException,
            IOException {
        boolean isRunning = false;
        if (SERVER.equals(instanceName)) {
            // -- If DAS is running, so is the "server" instance.
            isRunning = true;
        } else {
            String instanceObjName = "amx:J2EEServer=" + instanceName
                    + ",j2eeType=JVM,*";

            ObjectName objName = null;
            objName = new ObjectName(instanceObjName);
            Set<ObjectName> nameSet = null;

            if (this.mMBeanServer != null) {
                nameSet = (Set<ObjectName>)this.mMBeanServer.queryNames(objName, null);
            } else {
                nameSet = (Set<ObjectName>)this.mbeanServerConnection.queryNames(objName, null);
            }

            if ((nameSet != null) && (false == nameSet.isEmpty())) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    /**
     * 
     * 
     * @return true if the target is a standalone server
     */
    public boolean isStandaloneServer(String targetName) throws ConfigException {
        return getStandaloneServerNames().contains(targetName);
    }

    /**
     * 
     * 
     * @return true if the targetName is a valid standalone server name or a
     *         cluster name
     */
    public boolean isValidTarget(String targetName) throws ConfigException {
        boolean isValid = false;
        if (isCluster(targetName) || isStandaloneServer(targetName)) {
            isValid = true;
        }

        return isValid;
    }

    /**
     * Returns true is multiple servers are permitted within the app server
     * installation.
     * 
     * 
     */
    public boolean supportsMultipleServers() {
        return mSupportsMultipleServers;
    }

    // -- NotificationListener ops

    /**
     * Handles Notifications
     * 
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification,
     *      java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        if (notification instanceof javax.management.MBeanServerNotification) {
            javax.management.MBeanServerNotification mbnEvent = (javax.management.MBeanServerNotification) notification;
            ObjectName objectName = mbnEvent.getMBeanName();
            if (isInstanceMBean(objectName)) {
                String instanceName = objectName.getKeyProperty("name");
                try {
                    if (isStandaloneServer(instanceName)) {
                        // -- only interested in non clustered instances
                        broadcastInstanceEvent(mbnEvent, instanceName);
                    }
                } catch (ConfigException e) {
                    e.printStackTrace();
                }
            } else if (isClusterMBean(objectName)) {
                String clusterName = objectName.getKeyProperty("name");
                broadcastClusterEvent(mbnEvent, clusterName);
            }
        }
    }

    // -- AdminBroadcaster ops

    /**
     * Register a AdminEventListener
     * 
     * @param instanceName -
     *            name of the standalone instance.
     * 
     */
    public void addListener(ServerEventListener listener) {
        getServerListeners().add(listener);

    }

    /**
     * Remove a AdminEventListener
     * 
     * @param instanceName -
     *            name of the standalone instance.
     * 
     */
    public void removeListener(ServerEventListener listener) {
        List<ServerEventListener> listeners = getServerListeners();

        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }

    }

    /*----------------------------------------------------------------------------------*\
     *                          Private Helpers                                         *
     \*----------------------------------------------------------------------------------*/
    /**
     * @return list of ServerEventListeners
     */
    private List<ServerEventListener> getServerListeners() {
        if (mListeners == null) {
            mListeners = new ArrayList<ServerEventListener>();
        }
        return mListeners;
    }

    /**
     * @return true if this is a instance Mbean object name
     */
    private boolean isInstanceMBean(ObjectName objectName) {
        return objectName.toString()
                .startsWith("amx:j2eeType=J2EEServer,name=");
    }

    /**
     * @return true if this is a Cluster MBean
     */
    private boolean isClusterMBean(ObjectName objectName) {
        return objectName.toString().startsWith(
                "amx:j2eeType=X-J2EECluster,name=");
    }

    /**
     * Broadcast the MBean registration / unregistration event for an instance
     * MBean.
     * 
     * @param notification -
     *            MBeanServerNotification
     * @param instanceName -
     *            instance name
     */
    private void broadcastInstanceEvent(MBeanServerNotification notification,
            String instanceName) {
        List<ServerEventListener> listeners = getServerListeners();
        if (notification.getType().equalsIgnoreCase("JMX.mbean.registered")) {
            for (ServerEventListener listener : listeners) {
                try {
                    listener.createdStandaloneInstance(instanceName);
                } catch (Exception ex) {
                    mLog.warning(ex.getMessage());
                    continue;
                }
            }
        } else if (notification.getType().equalsIgnoreCase(
                "JMX.mbean.unregistered")) {
            for (ServerEventListener listener : listeners) {
                try {
                    listener.deletedStandaloneInstance(instanceName);
                } catch (Exception ex) {
                    mLog.warning(ex.getMessage());
                    continue;
                }
            }

        }
    }

    /**
     * Broadcast the MBean registration / unregistration event for a cluster
     * MBean.
     * 
     * @param notification -
     *            MBeanServerNotification
     * @param clusterName -
     *            cluster name
     */
    private void broadcastClusterEvent(MBeanServerNotification notification,
            String clusterName) {
        List<ServerEventListener> listeners = getServerListeners();
        if (notification.getType().equalsIgnoreCase("JMX.mbean.registered")) {
            for (ServerEventListener listener : listeners) {
                try {
                    listener.createdCluster(clusterName);
                } catch (Exception ex) {
                    mLog.warning(ex.getMessage());
                    continue;
                }
            }
        } else if (notification.getType().equalsIgnoreCase(
                "JMX.mbean.unregistered")) {
            for (ServerEventListener listener : listeners) {
                try {
                    listener.deletedCluster(clusterName);
                } catch (Exception ex) {
                    mLog.warning(ex.getMessage());
                    continue;
                }
            }

        }
    }

    /**
     * This operation uses Glassfish internal interfaces to get the required
     * cluster information
     * 
     * @return true if the instance is clustered
     */
    private boolean isInstanceClustered(String instanceName)
            throws ConfigException

    {
        boolean isClustered = false;
        ConfigContext context = getConfigContext();
        if (context != null) {
            isClustered = ServerHelper.isServerClustered(context, instanceName);
        }
        return isClustered;
    }

    /**
     * @return the name of the cluster the instance belongs to. If the instance
     *         does not belong to a cluster a null is returned.
     */
    private String getClusterForInstance(String instanceName)
            throws ConfigException {
        String clusterName = null;
        if (isInstanceClustered(instanceName)) {
            Cluster cluster = ClusterHelper.getClusterForInstance(
                    getConfigContext(), instanceName);
            clusterName = cluster.getName();
        }
        return clusterName;
    }

    /**
     * This ConfigContext is available only on the DAS
     * 
     * @return the ConfigContext
     */
    private ConfigContext getConfigContext() {
        ConfigContext context = null;
        if (isDAS()) {
            if (AdminService.getAdminService() != null) {
                context = AdminService.getAdminService().getAdminContext()
                        .getAdminConfigContext();
            }
        } else {
            if (ApplicationServer.getServerContext() != null) {
                context = ApplicationServer.getServerContext()
                        .getConfigContext();
            }
        }
        return context;
    }

    /**
     * 
     * @return a Set containing the server names.
     */
    private Set<String> getServerNames(Server[] servers) {
        Set<String> unclusteredServers = Collections
                .synchronizedSet(new HashSet<String>());
        for (int index = 0; index < servers.length; index++) {
            unclusteredServers.add(servers[index].getName());
        }
        return unclusteredServers;
    }

    /**
     * Get cluster names as a set
     * 
     * @param clusters
     * @return a Set containing the cluster names.
     */
    private Set<String> getClusterNames(Cluster[] clusters) {
        Set<String> domainClusters = Collections
                .synchronizedSet(new HashSet<String>());
        for (int index = 0; index < clusters.length; index++) {
            domainClusters.add(clusters[index].getName());
        }
        return domainClusters;
    }

    /**
     * Registers the MBean with the MBeanServer
     * 
     * @param connection
     * @return true if successful, false if not
     */
    public static boolean registerMBean(MBeanServerConnection connection) {
        boolean result = false;
        ObjectName objectName = null;

        try {
            objectName = new ObjectName(ServerInformation.OBJECT_NAME);
            if (false == connection.isRegistered(objectName)) {
                Object[] params = { connection };
                String[] signature = { MBeanServerConnection.class.getName() };
                connection.createMBean(ServerInformationImpl.class.getName(),
                        objectName, params, signature);
            }
            result = true;
        } catch (MalformedObjectNameException e) {
            
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            
            e.printStackTrace();
        } catch (NullPointerException e) {
            
            e.printStackTrace();
        } catch (ReflectionException e) {
            
            e.printStackTrace();
        } catch (MBeanException e) {
            
            e.printStackTrace();
        } catch (IOException e) {
            
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Registers the MBean with the MBeanServer
     * 
     * @param connection
     * @return true if successful, false if not
     */
    public static boolean registerMBean(MBeanServer connection) {
        boolean result = false;
        StandardMBean mbean = null;
        ServerInformation serverInfo = null;
        ObjectName objectName = null;

        try {
            objectName = new ObjectName(ServerInformation.OBJECT_NAME);
            if (false == connection.isRegistered(objectName)) {
                serverInfo = new ServerInformationImpl(connection);
                mbean = new StandardMBean(serverInfo,
                        ServerInformation.class);
                connection.registerMBean(mbean, objectName);
            }
            result = true;
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Unregister the MBean from the MBeanServer
     * 
     * @param connection
     * @return true if unregistered, false if not
     */
    public static boolean unregisterMBean(MBeanServerConnection connection) {
        boolean result = false;
        ObjectName objectName = null;

        try {
            objectName = new ObjectName(ServerInformation.OBJECT_NAME);
            if (true == connection.isRegistered(objectName)) {
                connection.unregisterMBean(objectName);
            }
            result = true;
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Unregister the MBean from the MBeanServer
     * 
     * @param connection
     * @return true if unregistered, false if not
     */
    public static boolean unregisterMBean(MBeanServer connection) {
        boolean result = false;
        ObjectName objectName = null;

        try {
            objectName = new ObjectName(ServerInformation.OBJECT_NAME);
            if (true == connection.isRegistered(objectName)) {
                connection.unregisterMBean(objectName);
            }
            result = true;
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return result;
    }
    
    /**
     * 
     * @return MBeanServer
     */
    static MBeanServer getMBeanServer() {
        MBeanServer server = null;
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(servers.size() > 0) {
            server = (MBeanServer)servers.get(0);
        }
        return server;
    }    

}
