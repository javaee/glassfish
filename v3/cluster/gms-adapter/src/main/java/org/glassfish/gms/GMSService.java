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

import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.impl.client.*;

import com.sun.enterprise.mgmt.transport.grizzly.GrizzlyConfigConstants;
import com.sun.enterprise.util.SystemPropertyConstants;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.api.Startup;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.gms.logging.LogDomain;

import java.util.Properties;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * @author Sheetal.Vartak@Sun.COM
 */

@Service(name="GMSService")

public class GMSService implements Startup, PostConstruct, CallBack {

    private static final Logger logger = LogDomain.getLogger(LogDomain.GMS_LOGGER);
    private GroupManagementService gms;

    private final static String INSTANCE_NAME = "INSTANCE_NAME";
    private final static String CLUSTER_NAME = "CLUSTER_NAME";

    private final static String CORE = "CORE";
    private final static String SPECTATOR = "SPECTATOR";
    private final static String MEMBERTYPE_STRING = "MEMBER_TYPE";

    private String instanceName = System.getProperty(INSTANCE_NAME);
    private String clusterName = System.getProperty(CLUSTER_NAME);
    
    private String INSTANCE_PROPS_FILE_LOCATION = "/clusters/" + clusterName + "/" + instanceName + ".properties";
    private String CLUSTER_PROPS_FILE_LOCATION = "/clusters/" + clusterName + "/cluster.properties";

    @Inject
    Events events;

    public void usage() {
        logger.log(Level.CONFIG, "The following properties need to be set in order to enable GMS : \n" +
                "INSTANCE_NAME(server for DAS), CLUSTER_NAME, <MEMBER_TYPE>, <TCPSTARTPORT>, <TCPENDPORT>");
    }
    public void postConstruct() {
        try {
            logger.log(Level.CONFIG, "gmsservice.postconstruct");
            initializeGMS();
        } catch (GMSException e) {
            logger.log(Level.WARNING, "gmsexception.occurred", e.getLocalizedMessage());
        }
    }

    private void initializeGMS() throws GMSException{

        logger.setLevel(Level.CONFIG);

        if (instanceName == null && clusterName == null) {
            return;       //don't enable GMS
        }
        if (instanceName == null || clusterName == null) {
            usage();
            throw new GMSException("instanceName =  " + instanceName + " clusterName = " + clusterName +
                    " Please set the appropriate system property INSTANCE_NAME or CLUSTER_NAME to a valid value");
        }

        Properties configProps = getSystemProps();   //setting up Shoal defaults

        configProps = readFromPropsFile(configProps);

        for (Enumeration property = configProps.propertyNames(); property.hasMoreElements();) {
            String key = (String) property.nextElement();
            String value = configProps.getProperty(key);
            if (value != null) {
                configProps.setProperty(key.trim(), value.trim());
            }
        }
        
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
                    public void event(Event event) {
                        if (event.is(EventTypes.SERVER_SHUTDOWN)) {
                            logger.fine("Calling gms.shutdown()...");
                            gms.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
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
        //trim the spaces from the name-value pairs


    }

    private Properties getSystemProps() {
        
        Properties configProps = new Properties();
        

        logger.log(Level.FINE, "Is initial host=" + System.getProperty("IS_INITIAL_HOST"));
        configProps.put(INSTANCE_NAME, instanceName);
        configProps.put(CLUSTER_NAME, clusterName);

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

        StringBuffer sbuf = new StringBuffer();
        logger.config("Printing all the properties : ");

        for (Enumeration en = prop.propertyNames(); en.hasMoreElements();) {
            String key = (String)en.nextElement();
            sbuf.append(key + " = " + prop.get(key) + "  ");
        }

        logger.config(sbuf.toString());
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
}
