/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.gms;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.impl.client.*;
import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import com.sun.logging.LogDomains;

import java.util.*;

import static com.sun.enterprise.ee.cms.core.ServiceProviderConfigurationKeys.*;

import com.sun.enterprise.mgmt.transport.grizzly.GrizzlyConfigConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.Properties;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.types.Property;

/**
 * @author Sheetal.Vartak@Sun.COM
 */
@Scoped(PerLookup.class)
@Service()
public class GMSAdapterImpl implements GMSAdapter, PostConstruct, CallBack {

    private static final Logger logger =
        LogDomains.getLogger(GMSAdapterImpl.class, LogDomains.GMS_LOGGER);
    private Level gmsLogLevel = Level.CONFIG;
    private static final String BEGINS_WITH = "^";
    private static final String GMS_PROPERTY_PREFIX = "GMS_";
    private static final String GMS_PROPERTY_PREFIX_REGEXP = BEGINS_WITH + GMS_PROPERTY_PREFIX;

    private GroupManagementService gms;

    private final static String INSTANCE_NAME = "INSTANCE_NAME";
    private final static String CLUSTER_NAME = "CLUSTER_NAME";

    private final static String CORE = "CORE";
    private final static String SPECTATOR = "SPECTATOR";
    private final static String MEMBERTYPE_STRING = "MEMBER_TYPE";

    // all set in postConstruct
    private String instanceName = null;
    private boolean isDas = false;
    private Cluster cluster = null;
    private String clusterName = null;
    private Config clusterConfig = null;

    private ConcurrentHashMap<CallBack, JoinNotificationActionFactory> callbackJoinActionFactoryMapping =
            new ConcurrentHashMap<CallBack, JoinNotificationActionFactory>();
    private ConcurrentHashMap<CallBack, JoinedAndReadyNotificationActionFactory> callbackJoinedAndReadyActionFactoryMapping =
            new ConcurrentHashMap<CallBack, JoinedAndReadyNotificationActionFactory>();
    private ConcurrentHashMap<CallBack, FailureNotificationActionFactory> callbackFailureActionFactoryMapping =
            new ConcurrentHashMap<CallBack, FailureNotificationActionFactory>();
    private ConcurrentHashMap<CallBack, FailureSuspectedActionFactory> callbackFailureSuspectedActionFactoryMapping =
            new ConcurrentHashMap<CallBack, FailureSuspectedActionFactory>();
    private ConcurrentHashMap<CallBack, GroupLeadershipNotificationActionFactory> callbackGroupLeadershipActionFactoryMapping =
            new ConcurrentHashMap<CallBack, GroupLeadershipNotificationActionFactory>();
    private ConcurrentHashMap<CallBack, PlannedShutdownActionFactory> callbackPlannedShutdownActionFactoryMapping =
            new ConcurrentHashMap<CallBack, PlannedShutdownActionFactory>();

    @Inject
    Events events;

    @Inject
    ServerEnvironment env;

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    Habitat habitat;

    @Inject
    Clusters clusters;

    @Override
    public void postConstruct() {
        logger.setLevel(Level.CONFIG);
        logger.log(Level.CONFIG, "gmsservice.postconstruct");
    }

    AtomicBoolean initialized = new AtomicBoolean(false);
    AtomicBoolean initializationComplete = new AtomicBoolean(false);

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public boolean initialize(String clusterName) {
        if (initialized.compareAndSet(false, true)) {
            this.clusterName = clusterName;
            if (clusterName == null) {
                logger.log(Level.SEVERE, "no clustername to lookup");
                return false;
            }
            try {
                gms = GMSFactory.getGMSModule(clusterName);
            } catch (GMSException ge) {
                // ignore
            }
            if (gms != null) {
                logger.log(Level.SEVERE, "multiple gms-adapter service for cluster " + clusterName);
                return false;
            }

            Domain domain = habitat.getComponent(Domain.class);
            instanceName = env.getInstanceName();

            isDas = env.isDas();
            if (isDas) {
                for (Cluster clusterI : clusters.getCluster()) {
                    if (clusterName.compareTo(clusterI.getName()) == 0) {
                        cluster = clusterI;
                        break;
                    }
                }
            } else {
                cluster = server.getCluster();
                assert (clusterName.equals(cluster.getName()));
            }

            if (cluster == null) {
                logger.log(Level.WARNING, "gmsservice.nocluster.warning");
                return false;       //don't enable GMS
            }

            clusterConfig = domain.getConfigNamed(clusterName + "-config");
            if (logger.isLoggable(Level.CONFIG)) {
                logger.config("clusterName=" + clusterName + " clusterConfig=" + clusterConfig);
            }
            try {
                initializeGMS();
            } catch (GMSException e) {
                logger.log(Level.WARNING, "gmsexception.occurred", e);
            }
            initializationComplete.set(true);
        }
        return initialized.get();
    }

    public void complete() {
        initialized.compareAndSet(true, false);
        initializationComplete.compareAndSet(true, false);
        gms = null;
        GMSFactory.removeGMSModule(clusterName);
    }

    private void readGMSConfigProps(Properties configProps) {
        configProps.put(MEMBERTYPE_STRING, isDas ? SPECTATOR : CORE);
        for (ServiceProviderConfigurationKeys key : ServiceProviderConfigurationKeys.values()) {
            String keyName = key.toString();
            try {
            switch (key) {
                case MULTICASTADDRESS:
                    if (cluster != null) {
                        configProps.put(keyName, cluster.getGmsMulticastAddress());
                    }
                    break;

                case MULTICASTPORT:
                    if (cluster != null) {
                        configProps.put(keyName, cluster.getGmsMulticastPort());
                    }
                    break;

                case FAILURE_DETECTION_TIMEOUT:
                    if (clusterConfig != null) {
                        String  value = clusterConfig.getGroupManagementService().getFailureDetection().getHeartbeatFrequencyInMillis();
                        configProps.put(keyName, value);
                    }
                    break;

                case FAILURE_DETECTION_RETRIES:
                    if (clusterConfig != null) {
                        String  value = clusterConfig.getGroupManagementService().getFailureDetection().getMaxMissedHeartbeats();
                        configProps.put(keyName, value);
                    }
                    break;

                case FAILURE_VERIFICATION_TIMEOUT:
                    if (clusterConfig != null) {
                        String  value = clusterConfig.getGroupManagementService().getFailureDetection().getVerifyFailureWaittimeInMillis();
                        configProps.put(keyName, value);
                    }
                    break;

                case DISCOVERY_TIMEOUT:
                    if (clusterConfig != null) {
                        String  value = clusterConfig.getGroupManagementService().getGroupDiscoveryTimeoutInMillis();
                        configProps.put(keyName, value);
                    }
                    break;

                case IS_BOOTSTRAPPING_NODE:
                    configProps.put(keyName, isDas ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
                    break;

                case VIRTUAL_MULTICAST_URI_LIST:
                    // todo
                    break;

                case BIND_INTERFACE_ADDRESS:
                    if (cluster != null) {
                        String value = cluster.getGmsBindInterfaceAddress().trim();
                        if (value != null && value.length() > 1 && value.charAt(0) != '$') {

                            // todo: remove check for value length greater than 1.
                            // this value could be anything from IPv4 address, IPv6 address, hostname, network interface name.
                            // Only supported IPv4 address in gf v2.

                            // todo: handle invalid inputs.  for this case, validate that value can be associated with a network interface on machine.
                            // need to provide admin feedback when this value is not set correctly.
                            configProps.put(keyName, value);
                        }
                    }
                    break;

                case FAILURE_DETECTION_TCP_RETRANSMIT_TIMEOUT:
                    if (clusterConfig != null) {
                        String  value = clusterConfig.getGroupManagementService().getFailureDetection().getVerifyFailureConnectTimeoutInMillis();
                        configProps.put(keyName, value);
                    }
                    break;

                case MULTICAST_POOLSIZE:
                case INCOMING_MESSAGE_QUEUE_SIZE :
                // case MAX_MESSAGE_LENGTH:    todo uncomment with shoal-gms.jar with this defined is promoted.
                case FAILURE_DETECTION_TCP_RETRANSMIT_PORT:

                    if (clusterConfig != null) {
                        Property prop = clusterConfig.getGroupManagementService().getProperty(keyName);
                        if (prop == null) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(String.format(
                                    "No config property found for %s",
                                    keyName));
                            }
                            break;
                        }
                        String value = prop.getValue().trim();
                        configProps.put(keyName, value);
                        /*
                        int positiveint = 0;
                        try {
                            positiveint = Integer.getInteger(value);
                        } catch (Throwable t) {}

                        // todo
                        if (positiveint > 0) {
                            configProps.put(keyName, positiveint);
                        } // todo else log event that invalid value was provided.
                        */
                    }
                    break;

                // These Shoal GMS configuration parameters are not supported to be set.
                // Must place here or they will get flagged as not handled.
                case LOOPBACK:
                    break;

                // end unsupported Shoal GMS configuration parameters.


                default:
//                    logger.log();
                    break;
            }  /* end switch over ServiceProviderConfigurationKeys enum */
            } catch (Throwable t) {
                logger.log(Level.WARNING,
                    "gmsexception.processing.config.props", t);
            }
        } /* end for loop over ServiceProviderConfigurationKeys */

        // check for Grizzly transport specific properties in GroupManagementService property list and then cluster property list.
        // cluster property is more specific than group-mangement-service, so allow cluster property to override group-management-service proeprty
        // if a GrizzlyConfigConstant property is in both list.
        List<Property> props = null;
        if (clusterConfig != null) {
            props = clusterConfig.getGroupManagementService().getProperty();
            for (Property prop : props) {
                String name = prop.getName().trim();
                String value = prop.getValue().trim();
                logger.config("processing group-management-service property name=" + name + " value= " + value);
                if (value.startsWith("${")) {
                    logger.config("skipping group-management-service property name=" + name + " since value is unresolved symbolic token="+ value);
                 } else if (name != null ) {
                    try {
                        logger.config("processing group-management-service property name=" + name + " value= " + value);
                        if (name.startsWith(GMS_PROPERTY_PREFIX)) {
                            name = name.replaceFirst(GMS_PROPERTY_PREFIX_REGEXP, "");
                        }
                        GrizzlyConfigConstants key = GrizzlyConfigConstants.valueOf(name);
                        configProps.put(name, value);
                    } catch (IllegalArgumentException iae) {
                        logger.warning("ignoring group-management-service property " + name + " with value of " + value + " due to " + iae.getLocalizedMessage());
                    }
                 }
            }
        }
        if (cluster != null) {
            props = cluster.getProperty();
            for (Property prop : props) {
                String name = prop.getName().trim();
                String value = prop.getValue().trim();
                logger.config("processing cluster property name=" + name + " value= " + value);
                if (value.startsWith("${")) {
                    logger.config("skipping cluster property name=" + name + " since value is unresolved symbolic token="+ value);
                } else if (name != null ) {
                    try {
                        if (name.startsWith(GMS_PROPERTY_PREFIX)) {
                            name = name.replaceFirst(GMS_PROPERTY_PREFIX_REGEXP, "");
                        }
                        if (name.compareTo("LISTENER_PORT") == 0 ) {

                            // special case mapping.  Glassfish Cluster property GMS_LISTENER_PORT maps to Grizzly Config Constants TCPSTARTPORT and TCPENDPORT.
                            configProps.put(GrizzlyConfigConstants.TCPSTARTPORT.toString(), value);
                            configProps.put(GrizzlyConfigConstants.TCPENDPORT.toString(), value);
                         } else {
                            // handle normal case.  one to one mapping.
                            GrizzlyConfigConstants key = GrizzlyConfigConstants.valueOf(name);
                            configProps.put(name, value);
                        }
                    } catch (IllegalArgumentException iae) {

                    }
                }
            }
        }
    }

    private void initializeGMS() throws GMSException{
        Properties configProps = new Properties();


        // read GMS configuration from domain.xml
        readGMSConfigProps(configProps);

        printProps(configProps);

        String memberType = (String) configProps.get(MEMBERTYPE_STRING);

        gms = (GroupManagementService) GMSFactory.startGMSModule(instanceName, clusterName,
                GroupManagementService.MemberType.valueOf(memberType), configProps);
        //remove GMSLogDomain.getLogger(GMSLogDomain.GMS_LOGGER).setLevel(gmsLogLevel);
        GMSFactory.setGMSEnabledState(clusterName, Boolean.TRUE);
        GMSLogDomain.getLogger(GMSLogDomain.GMS_LOGGER).setLevel(Level.CONFIG);
        if (gms != null) {
            try {

                // todo remove these in future. just verifying that these handlers are getting registered and getting called.
                registerJoinedAndReadyNotificationListener(this);
                registerJoinNotificationListener(this);
                registerFailureNotificationListener(this);
                registerPlannedShutdownListener(this);
                registerFailureSuspectedListener(this);
                
                events.register(new org.glassfish.api.event.EventListener() {
                    @Override
                    public void event(Event event) {
                        if (event.is(EventTypes.SERVER_SHUTDOWN)) {
                            logger.fine("Calling gms.shutdown()...");

                            // todo: remove these when removing the test register ones above.
                            removeJoinedAndReadyNotificationListener(GMSAdapterImpl.this);
                            removeJoinNotificationListener(GMSAdapterImpl.this);
                            removeFailureNotificationListener(GMSAdapterImpl.this);
                            removeFailureSuspectedListener(GMSAdapterImpl.this);
                            gms.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                            removePlannedShutdownListener(GMSAdapterImpl.this);
                        } else if (event.is(EventTypes.SERVER_READY)) {
                            logger.fine("Ready");
                            gms.reportJoinedAndReadyState(clusterName);
                        }
                    }
                });

                gms.join();
            } catch (GMSException e) {
                logger.log(Level.WARNING, "gmsexception.occurred", e.getLocalizedMessage());
            }

            logger.log(Level.CONFIG, "gmsservice.started ", new Object[]{instanceName, clusterName});

        } else throw new GMSException("gms object is null.");

        logger.info(instanceName + " joined group " + clusterName);

    }

    private void printProps(Properties prop) {
        if (!logger.isLoggable(Level.CONFIG)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String key : prop.stringPropertyNames()) {
            sb.append(key).append(" = ").append(prop.get(key)).append("  ");
        }
        logger.log(Level.CONFIG, "gmsservice.print.properties", sb.toString());
    }

    public Startup.Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

    public GroupManagementService getGMS(String groupName) {
        //return the gms instance for that group
        try {
            return GMSFactory.getGMSModule(groupName);
        } catch (GMSException e) {
            logger.log(Level.SEVERE, "Exception in getting GMS module for group " + groupName , e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public void processNotification(Signal signal) {
        logger.log(Level.INFO, "gmsservice.processNotification", signal.getClass().getName());
    }

    // each of the getModule(s) methods are temporary. see class-level comment.
    @Override
    public GroupManagementService getModule() {
        if( ! initialized.get() || ! initializationComplete.get())  {
            throw new IllegalStateException("GMSAdapter not properly initialized.");
        }
        return gms;
    }

    /**
     * Registers a JoinNotification Listener.
     *
     * @param callback processes GMS notification JoinNotificationSignal
     */
    @Override
    public void registerJoinNotificationListener(CallBack callback) {
        if (gms != null  && callback != null) {
            JoinNotificationActionFactory jnaf =  new JoinNotificationActionFactoryImpl(callback);
            gms.addActionFactory(jnaf);
            callbackJoinActionFactoryMapping.put(callback, jnaf);
        }
    }

    /**
     * Registers a JoinAndReadyNotification Listener.
     *
     * @param callback processes GMS notification JoinAndReadyNotificationSignal
     */
    @Override
    public void registerJoinedAndReadyNotificationListener(CallBack callback) {
        if (gms != null && callback != null) {
            JoinedAndReadyNotificationActionFactory jnaf =  new JoinedAndReadyNotificationActionFactoryImpl(callback);
            gms.addActionFactory(jnaf);
            callbackJoinedAndReadyActionFactoryMapping.put(callback, jnaf);
        }
    }

    /**
     * Register a listener for all events that represent a member has left the group.
     *
     * @param callback Signal can be either PlannedShutdownSignal, FailureNotificationSignal or JoinNotificationSignal(subevent Rejoin).
     */
    @Override
    public void registerMemberLeavingListener(CallBack callback) {
        if (gms != null && callback != null) {
            registerFailureNotificationListener(callback);
            registerPlannedShutdownListener(callback);
            registerJoinNotificationListener(callback);
        }
    }

    /**
     * Registers a PlannedShutdown Listener.
     *
     * @param callback processes GMS notification PlannedShutdownSignal
     */
    @Override
    public void registerPlannedShutdownListener(CallBack callback) {
        if (gms != null && callback != null) {
            PlannedShutdownActionFactory psaf = new PlannedShutdownActionFactoryImpl(callback);
            callbackPlannedShutdownActionFactoryMapping.put(callback, psaf);
            gms.addActionFactory(psaf);
        }
    }

    /**
     * Registers a FailureSuspected Listener.
     *
     * @param callback processes GMS notification FailureSuspectedSignal
     */
    @Override
    public void registerFailureSuspectedListener(CallBack callback) {
        if (gms != null) {
            FailureSuspectedActionFactory fsaf = new FailureSuspectedActionFactoryImpl(callback);
            callbackFailureSuspectedActionFactoryMapping.put(callback, fsaf);
            gms.addActionFactory(fsaf);
        }
    }

    /**
     * Registers a FailureNotification Listener.
     *
     * @param callback processes GMS notification FailureNotificationSignal
     */
    @Override
    public void registerFailureNotificationListener(CallBack callback) {
        if (gms != null) {
            FailureNotificationActionFactory fnaf = new FailureNotificationActionFactoryImpl(callback);
            callbackFailureActionFactoryMapping.put(callback, fnaf);
            gms.addActionFactory(fnaf);
        }
    }

    /**
     * Registers a FailureRecovery Listener.
     *
     * @param callback      processes GMS notification FailureRecoverySignal
     * @param componentName The name of the parent application's component that should be notified of selected for
     *                      performing recovery operations. One or more components in the parent application may
     *                      want to be notified of such selection for their respective recovery operations.
     */
    @Override
    public void registerFailureRecoveryListener(String componentName, CallBack callback) {
        if (gms != null) {
            gms.addActionFactory(componentName, new FailureRecoveryActionFactoryImpl(callback));
        }
    }

    /**
     * Registers a Message Listener.
     *
     * @param componentName   Name of the component that would like to consume
     *                        Messages. One or more components in the parent application would want to
     *                        be notified when messages arrive addressed to them. This registration
     *                        allows GMS to deliver messages to specific components.
     * @param messageListener processes GMS MessageSignal
     */
    @Override
    public void registerMessageListener(String componentName, CallBack messageListener) {
        if (gms != null) {
            gms.addActionFactory(new MessageActionFactoryImpl(messageListener), componentName);
        }
    }

    /**
     * Registers a GroupLeadershipNotification Listener.
     *
     * @param callback processes GMS notification GroupLeadershipNotificationSignal. This event occurs when the GMS masters leaves the Group
     *                 and another member of the group takes over leadership. The signal indicates the new leader.
     */
    @Override
    public void registerGroupLeadershipNotificationListener(CallBack callback) {
        if (gms != null) {
            gms.addActionFactory(new GroupLeadershipNotificationActionFactoryImpl(callback));
        }
    }

    @Override
    public void removeFailureRecoveryListener(String componentName) {
        if (gms != null) {
            gms.removeFailureRecoveryActionFactory(componentName);
        }
    }

    @Override
    public void removeMessageListener(String componentName){
        if (gms != null) {
            gms.removeMessageActionFactory(componentName);
        }
    }

    @Override
    public void removeFailureNotificationListener(CallBack callback){
        if (gms != null) {
            FailureNotificationActionFactory fnaf = callbackFailureActionFactoryMapping.remove(callback);
            if (fnaf != null) {
                gms.removeActionFactory(fnaf);
            }
        }
    }

    @Override
    public void removeFailureSuspectedListener(CallBack callback){
         if (gms != null) {
            FailureSuspectedActionFactory fsaf = callbackFailureSuspectedActionFactoryMapping.remove(callback);
            if (fsaf != null) {
                gms.removeFailureSuspectedActionFactory(fsaf);
            }
        }
    }

    @Override
    public void removeJoinNotificationListener(CallBack callback){
        if (gms != null) {
            JoinNotificationActionFactory jaf = callbackJoinActionFactoryMapping.get(callback);
            if (jaf != null)  {
                gms.removeActionFactory(jaf);
            }
        }
    }

    @Override
    public void removeJoinedAndReadyNotificationListener(CallBack callback){
        if (gms != null) {
            JoinedAndReadyNotificationActionFactory jaf = callbackJoinedAndReadyActionFactoryMapping.get(callback);
            if (jaf != null)  {
                gms.removeActionFactory(jaf);
            }
        }
    }

    @Override
    public void removePlannedShutdownListener(CallBack callback){
        if (gms != null) {
            PlannedShutdownActionFactory psaf = callbackPlannedShutdownActionFactoryMapping.remove(callback);
            if (psaf != null) {
                gms.removeActionFactory(psaf);
            }
        }
    }

    @Override
    public void removeGroupLeadershipLNotificationistener(CallBack callback){
         if (gms != null) {
            GroupLeadershipNotificationActionFactory glnf = callbackGroupLeadershipActionFactoryMapping.get(callback);
            if (glnf != null)  {
                gms.removeActionFactory(glnf);
            }
        }
    }

    @Override
    public void removeMemberLeavingListener(CallBack callback){
        removePlannedShutdownListener(callback);
        removeFailureNotificationListener(callback);
        removeJoinNotificationListener(callback);
    }
}
