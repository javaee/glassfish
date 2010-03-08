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

import com.sun.logging.LogDomains;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.api.Startup;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.gms.logging.LogDomain;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sheetal.Vartak@Sun.COM
 */
@Service
public class GMSService implements Startup, PostConstruct, CallBack {

//    @Inject (fail = false)
//    GMSConfig gmsCfg;
    private static final Logger logger = LogDomain.getLogger(LogDomain.GMS_LOGGER);
    private GroupManagementService gms;

    @Inject
    Events events;

    public void usage() {
        logger.log(Level.CONFIG, "The following properties need to be set : /n" +
        "INSTANCE_NAME(server for DAS), CLUSTER_NAME, <MEMBER_TYPE>, <TCPSTARTPORT>, <TCPENDPORT>");
    }
    public void postConstruct() {
        try {
            logger.log(Level.CONFIG, "gmsservice.postconstruct");
            initializeGMS();
        } catch (GMSException e) {
            logger.log(Level.WARNING, "gmsexception.occurred", new Object[] {e});
        }
    }

    private void initializeGMS() throws GMSException{

        final String instanceName = System.getProperty("INSTANCE_NAME");
        final String groupName = System.getProperty("CLUSTER_NAME");

        if (instanceName == null && groupName == null) {
            usage();
            return;       //don't enable GMS
        }
        if (instanceName == null || groupName == null) {
            usage();
            throw new GMSException("Either instanceName or cluster name is null. " +
                    "Please set the appropriate system property INSTANCE_NAME or CLUSTER_NAME");
        }

        Properties configProps = new Properties();
        final String MEMBERTYPE_STRING;

        logger.log(Level.FINE, "Is initial host=" + System.getProperty("IS_INITIAL_HOST"));

        if (instanceName.equals("server")) {   //instance is DAS
            MEMBERTYPE_STRING = "SPECTATOR";
            configProps.put(ServiceProviderConfigurationKeys.IS_BOOTSTRAPPING_NODE.toString(), true);
        } else {
            MEMBERTYPE_STRING = System.getProperty("MEMBERTYPE", "CORE").toUpperCase();
            configProps.put(ServiceProviderConfigurationKeys.IS_BOOTSTRAPPING_NODE.toString(),
                System.getProperty("IS_INITIAL_HOST", "false"));
        }

        final GroupManagementService.MemberType memberType = GroupManagementService.MemberType.valueOf(MEMBERTYPE_STRING);

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

        gms = (GroupManagementService) GMSFactory.startGMSModule(instanceName, groupName, memberType, configProps);
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
                logger.log(Level.WARNING, "gmsexception.occurred", new Object[]{e});
            }
            logger.log(Level.CONFIG, "gmsservice.started ", new Object[]{instanceName, groupName});
        }
        else throw new GMSException("gms object is null.");
    }

    public Startup.Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

    public GroupManagementService getGMS() {
        return gms;
    }

    @Override
    public void processNotification(Signal signal) {
         logger.log(Level.CONFIG, "gmsservice.processNotification", new Object[] {signal.getClass().getName()});
    }
}
