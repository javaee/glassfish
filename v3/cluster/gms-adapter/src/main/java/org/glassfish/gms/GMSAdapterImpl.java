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

import java.util.List;
import java.util.Properties;

import static com.sun.enterprise.ee.cms.core.ServiceProviderConfigurationKeys.*;

import com.sun.enterprise.mgmt.transport.grizzly.GrizzlyConfigConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.Properties;
import java.util.Enumeration;
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
import org.glassfish.gms.logging.LogDomain;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.types.Property;

/**
 * @author Sheetal.Vartak@Sun.COM
 */
@Service()
public class GMSAdapterImpl implements GMSAdapter, PostConstruct, CallBack {

    private static final Logger logger = LogDomain.getLogger(LogDomain.GMS_LOGGER);

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

    @Inject
    Events events;

    @Inject
    ServerEnvironment env;

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    Habitat habitat;

    @Override
    public void postConstruct() {
        logger.setLevel(Level.CONFIG);
        Domain domain = habitat.getComponent(Domain.class);
        instanceName = env.getInstanceName();

        Cluster cluster = null;

        if (env.isDas()) {
            // hack: only supporting one cluster for M2
            cluster = domain.getClusters().getCluster().get(0);
        } else {
            cluster = server.getCluster();
        }
        isDas = env.isDas();
        clusterName = (cluster == null ? null : cluster.getName());
        clusterConfig = domain.getConfigNamed(clusterName + "-config");
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("clusterName=" + clusterName);
            logger.config("clusterConfig=" + clusterConfig);
            logger.config("domaing.getConfigs()=" + domain.getConfigs());
        }
        try {
            logger.log(Level.CONFIG, "gmsservice.postconstruct");
            initializeGMS();
        } catch (GMSException e) {
            logger.log(Level.WARNING, "gmsexception.occurred", e.getLocalizedMessage());
        }
    }

    private void initStopGapGMSConfiguration(Properties configProps) {
        getSystemProps(configProps);   //setting up Shoal defaults
        readFromPropsFile(configProps);

        // trim white space from keys and values.  Was needed for java property files, unsure whether needed when getting from domain.xml
        for (Enumeration property = configProps.propertyNames(); property.hasMoreElements();) {
            String key = (String) property.nextElement();
            String value = configProps.getProperty(key);
            if (value != null) {
                configProps.setProperty(key.trim(), value.trim());
            }
        }
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
                        String value = prop.getValue().trim();
                        int positiveint = 0;
                        try {
                            positiveint = Integer.getInteger(value);
                        } catch (Throwable t) {}

                        // todo
                        if (positiveint > 0) {
                            configProps.put(keyName, positiveint);
                        } // todo else log event that invalid value was provided.
                    }
                    break;

                // These Shoal GMS configuration parameters are not supported to be set.
                // Must place here or they will get flagged as not handled.
                case LOOPBACK:
                    break;

                // end unsupported Shoal GMS configuration parameters.


                default:
                    // todo: log message that a Shoal GMS property is not being handled.
                    break;
            }  /* end switch over ServiceProviderConfigurationKeys enum */
            } catch (Throwable t) {
                
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
                if (name != null ) {
                    try {
                        GrizzlyConfigConstants key = GrizzlyConfigConstants.valueOf(name);
                        configProps.put(name, value);
                    } catch (IllegalArgumentException iae) {
                        //
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
                if (name != null ) {
                    try {
                        GrizzlyConfigConstants key = GrizzlyConfigConstants.valueOf(name);
                        configProps.put(name, value);
                    } catch (IllegalArgumentException iae) {
                        //
                    }
                }
            }
        }
    }

    private void initializeGMS() throws GMSException{

        if (clusterName == null) {
            // todo: log something fine here
            return;       //don't enable GMS
        }

        Properties configProps = new Properties();
        initStopGapGMSConfiguration(configProps);
        //readGMSConfigProps(configProps);


        
        printProps(configProps);

        String memberType = (String) configProps.get(MEMBERTYPE_STRING);   

        gms = (GroupManagementService) GMSFactory.startGMSModule(instanceName, clusterName,
                GroupManagementService.MemberType.valueOf(memberType), configProps);

        if (gms != null) {
            try {

                gms.addActionFactory(new JoinedAndReadyNotificationActionFactoryImpl(this));
                gms.addActionFactory(new JoinNotificationActionFactoryImpl(this));
                gms.addActionFactory(new FailureNotificationActionFactoryImpl(this));
                gms.addActionFactory(new PlannedShutdownActionFactoryImpl(this));
                gms.addActionFactory(new FailureSuspectedActionFactoryImpl(this));

                events.register(new org.glassfish.api.event.EventListener() {
                    @Override
                    public void event(Event event) {
                        if (event.is(EventTypes.SERVER_SHUTDOWN)) {
                            logger.fine("Calling gms.shutdown()...");
                            gms.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
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

    private Properties readFromPropsFile(Properties configProps) {
        final String INSTANCE_PROPS_FILE_LOCATION = "/clusters/" + clusterName + "/" + instanceName + ".properties";
        final String CLUSTER_PROPS_FILE_LOCATION = "/clusters/" + clusterName + "/cluster.properties";

        //load default properties

        String install_location= System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        String fileName = install_location + INSTANCE_PROPS_FILE_LOCATION;
        logger.config("configProps file location ..." + install_location + INSTANCE_PROPS_FILE_LOCATION + "and " +
                install_location + CLUSTER_PROPS_FILE_LOCATION);
        FileInputStream in;
        try {

            in = new FileInputStream(fileName);
            configProps.load(in);
            in.close();

        } catch (FileNotFoundException fe) {
            logger.log(Level.WARNING, "Cannot find the properties file : " + fileName, fe.getLocalizedMessage() + "Using Shoal Defaults..." );
            return configProps;

        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Exception while trying to load the properties file.Using GMS default properties", ioe.getLocalizedMessage());
            return configProps;
        }

        try {
            fileName = install_location + CLUSTER_PROPS_FILE_LOCATION;

            in = new FileInputStream(fileName);
            configProps.load(in);
            in.close();
            return configProps;
        } catch (FileNotFoundException fe) {
            logger.log(Level.WARNING, "Cannot find the properties file : " + fileName + "Using GMS default properties", fe.getLocalizedMessage());
            return configProps;

        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Exception while trying to load the properties file.Using GMS default properties", ioe.getLocalizedMessage());
            return configProps;
        }
    }

    private Properties getSystemProps(Properties configProps) {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Is initial host=" + System.getProperty("IS_INITIAL_HOST"));
        }
        configProps.put(INSTANCE_NAME, instanceName);
        configProps.put(CLUSTER_NAME, clusterName);

        // bobby: get the other names into here
        if (instanceName.equals("server")) {   //instance is DAS
            configProps.put(MEMBERTYPE_STRING, SPECTATOR);

            configProps.put(ServiceProviderConfigurationKeys.IS_BOOTSTRAPPING_NODE.toString(), true);
        } else {
            configProps.put(MEMBERTYPE_STRING, System.getProperty(MEMBERTYPE_STRING, CORE).toUpperCase());
            configProps.put(ServiceProviderConfigurationKeys.IS_BOOTSTRAPPING_NODE.toString(),
                    System.getProperty("IS_INITIAL_HOST", "false"));
        }

        configProps.put(ServiceProviderConfigurationKeys.MULTICASTADDRESS.toString(),
                System.getProperty("MULTICASTADDRESS", "229.9.1.1"));

        configProps.put(ServiceProviderConfigurationKeys.MULTICASTPORT.toString(), 2299);

        if (System.getProperty("INITIAL_HOST_LIST") != null) {
            configProps.put(ServiceProviderConfigurationKeys.VIRTUAL_MULTICAST_URI_LIST.toString(),
                    System.getProperty("INITIAL_HOST_LIST"));
        }

        configProps.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_RETRIES.toString(),
                System.getProperty(ServiceProviderConfigurationKeys.FAILURE_DETECTION_RETRIES.toString(), "3"));

        configProps.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TIMEOUT.toString(),
                System.getProperty(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TIMEOUT.toString(), "2000"));

        configProps.put(ServiceProviderConfigurationKeys.DISCOVERY_TIMEOUT.toString(),
                System.getProperty(ServiceProviderConfigurationKeys.DISCOVERY_TIMEOUT.toString(), "5000"));

        configProps.put(ServiceProviderConfigurationKeys.FAILURE_VERIFICATION_TIMEOUT.toString(),
                System.getProperty(ServiceProviderConfigurationKeys.FAILURE_VERIFICATION_TIMEOUT.toString(), "1500"));

        String timeout = System.getProperty(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TCP_RETRANSMIT_TIMEOUT.toString());
        if (timeout != null)
            configProps.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TCP_RETRANSMIT_TIMEOUT.toString(),
                    timeout);


        //Uncomment this to receive loop back messages
        //configProps.put(ServiceProviderConfigurationKeys.LOOPBACK.toString(), "true");
        final String bindInterfaceAddress = System.getProperty("BIND_INTERFACE_ADDRESS");
        if (bindInterfaceAddress != null) {
            configProps.put(ServiceProviderConfigurationKeys.BIND_INTERFACE_ADDRESS.toString(), bindInterfaceAddress);
        }
        configProps.put(GrizzlyConfigConstants.TCPSTARTPORT.toString(), System.getProperty("TCPSTARTPORT", "9090"));
        configProps.put(GrizzlyConfigConstants.TCPENDPORT.toString(), System.getProperty("TCPENDPORT", "9120"));

        return configProps;
    }

    private void printProps(Properties prop) {
        if (!logger.isLoggable(Level.CONFIG)) {
            return;
        }

        StringBuilder sbul = new StringBuilder();
        logger.config("Printing all the properties : ");

        for (Enumeration en = prop.propertyNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            sbul.append(key + " = " + prop.get(key) + "  ");
        }

        logger.config(sbul.toString());
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
        return gms;
    }

}
