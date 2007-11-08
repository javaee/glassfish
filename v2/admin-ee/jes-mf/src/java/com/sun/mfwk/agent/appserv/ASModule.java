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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.cacao.DeploymentDescriptor;
import com.sun.mfwk.MfAbstractCpModule;
import com.sun.mfwk.MfMonitoringState;
import com.sun.mfwk.MfStatesManager;
import com.sun.mfwk.discovery.MfDiscoveryInfo;
import com.sun.mfwk.discovery.MfDiscoveryParameters;
import com.sun.mfwk.discovery.MfDiscoveryService;
import com.sun.mfwk.util.log.MfLogService;

import javax.management.remote.JMXConnector;
import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.NotificationFilterSupport;
import javax.management.MBeanServerConnection;

import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.MfAgentNode;
import com.sun.mfwk.MfDelegate;
import com.sun.mfwk.MfDelegateFactory;
import com.sun.mfwk.MfInstanceID;
import com.sun.mfwk.MfObjectFactory;

import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.connection.TrustAnyConnectionFactory;

import com.sun.mfwk.agent.appserv.modeler.MBeanModeler;

import java.security.cert.Certificate;

import java.io.IOException;

/**
 * AsExampleModule is a class provided as an example of a AS Module.
 * Extends the MfAbstractCpModule.
 */
public class ASModule extends MfAbstractCpModule 
        implements NotificationListener {

    /**
     * Creates a new instance of AsExampleModule
     *
     * @param moduleName module name : com.sun.cmm.as
     * @param productName name of the product
     * @param installedLocation installation directory of 
     *        Java ES Application Server Product
     * @param supportedMIBs names of MIBS that should be supported by 
     *        Java ES Monitoring framework
     * @param buildNumber of the installed product
     * @param patchId of the installed product
     * @param revisionNumber of the installed product
     * @param installDate installed date of the installed product
     */
    public ASModule(DeploymentDescriptor descriptor) {
        super(descriptor);
        logger.entering(sourceClass, "Constructor", new Object[] {descriptor});
        logger.exiting(sourceClass, "Constructor finish");
        
    }
    
    /**
     * Implements the required initialize method as specified by the 
     * MfAbstractCpModule class
     *
     * @throws java.lang.Exception if the module could not be initialized
     */
    public void initialize() throws Exception {
        logger.entering(sourceClass, "initialize");

        try {
            Properties properties = descriptor.getParameters();
        } catch(Exception e) {
        }

        // add discovery listener
        addDiscoveryListener();
        
        logger.exiting(sourceClass, "initialize");
    }

    /**
     * Starts the module and call the initialize abstract method that should be implemented by each
     * by each Component Product Module.
     * A Component Product that does not use the Java ES MF instrumentation toolkit to instrument
     * their source code, has to provide a Module class that extends the MfAbstractCpModule.
     * A default implementation of MfAbsractCpModule is provided : MfCpModule class.
     * This implementation is dedicated to Component Product using the Java ES MF instrumentstion
     * toolkit.
     * @throws java.lang.Exception if a problem occurred
     */
    public void start()  {
        logger.entering(sourceClass,"start", this.moduleName);
   
        // create my factory for the module
        try {
            // delegate factory
            Map delegateMap = MBeanModeler.getDelegateMap();
            delegateMap.put("CMM_InstalledProduct", com.sun.mfwk.MfInstalledProductDelegateSupport.class);

            logger.fine(sourceClass + " Creates the ObjectFactory for " + this.moduleName);
            objectFactory = MfObjectFactory.getObjectFactory(this.moduleName);

            // Create the delegateFactory for the module and the list of supported interfaces
            logger.fine(sourceClass+" Creates the delegateFactory for " + moduleName);
            delegateFactory = objectFactory.getDelegateFactory(delegateMap);

            logger.fine(sourceClass + " Creates the CMM_InstalledProduct");

            String installedProductInstanceId = MfInstanceID.getInstanceID(installedProductInstanceId = 
                moduleName + ":" + "type=CMM_InstalledProduct," + "name=" + this.productName +
                   ",collectionID=" + ObjectName.quote(this.installedLocation)).getCanonicalName();

            installedProduct=objectFactory.createObject(installedProductInstanceId);
            this.delegateFactory.createDelegate(super.descriptor, installedProduct);

            // Create the relation to the Node Agent objects
            MfAgentNode.createRelationsWithAgent(this.moduleName, installedProduct);

            logger.fine(sourceClass+" Initialize the module " + this.moduleName);

            this.initialize();

            logger.exiting(sourceClass,"start",this.moduleName);

        } catch (Exception ex) {
            logger.throwing(sourceClass, "Start failed for module = " + this.moduleName, ex);
            return;
        }
    }

    
    /**
     * Implements the required finalize method as specified by 
     * the MfAbstractCpModule class
     *
     * @throws java.lang.Exception if the module could not be finalized
     */
    public void finalize() throws Exception {
        logger.entering(sourceClass, "finalize");
        logger.exiting(sourceClass, "finalize");
    }

    /**
     * Registers the discovery listener.
     */
    private void addDiscoveryListener() {

        try {
            logger.log(Level.FINE, 
                "Adding Discovery Listener for Application Server");

            NotificationFilterSupport filter = new NotificationFilterSupport();
            filter.enableType(Constants.PRODUCT_NAME);

            ObjectName dson = new ObjectName(Constants.DIS_OBJ_NAME);
            getMbs().addNotificationListener(dson, this, filter, null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, 
                "Unable to register listener for discovery.", e);
        }
    }

    /**
     * Handles discovery events.
     *
     * @param  notification  discovery notification
     * @param  handback  opaque handback object
     */
    public void handleNotification(Notification notification, Object handback) {

        try {
            logger.info("**Discovered new Application Server Instance**");
            LogDomains.setLogger(logger);

            String type = notification.getType();
            // Only handle notification from discovery service
            if (!MfDiscoveryService.objectName.equals(notification.getSource()) ) {
               //this is a cacao operational status change; ignore
               return;
            }

            MfDiscoveryInfo msg = null; 
            if (notification.getUserData() != null) {
                msg = (com.sun.mfwk.discovery.MfDiscoveryInfo) notification.getUserData();

                // user data
                Map map = deserializeUserData(msg.getUserData());
                map.put(Constants.URI_KEY, msg.getUri());

                String domainName = (String) map.get(Constants.DOMAIN_NAME_KEY);
                Boolean isDAS = (Boolean) map.get(Constants.IS_DAS_KEY);

                // server instance name
                String instanceName = (String) map.get(Constants.SERVER_KEY);

                // call modeler to instrument the mbeans
                ASServerManagerRegistry serverManagerRegistry = 
                    ASServerManagerRegistry.getInstance();
                serverManagerRegistry.addASServerManager(moduleName,
                    instanceName, domainName);

                ASServerManager sManager = 
                    serverManagerRegistry.getASServerManager(instanceName, domainName); 

                // handle HELLO messages 
                if (msg.getMessageType() == MfDiscoveryParameters.MSG_TYPE_HELLO) {
                    logger.finest("DISCOVERY HELLO message");
                    // set up connection credentials in registry 
                    ConnectionRegistry reg = ConnectionRegistry.getInstance();
                    reg.setConnectionCredentials(instanceName, domainName, map);
                    
                    sManager.start();

                   // Set the appropriated states
                   // Send the notifications to SNMP mediation
                   logger.log(Level.INFO,
                       "Set states of all objects to " + MfStatesManager.STATE_INITIALIZED);
                   MfStatesManager.setState(MfStatesManager.STATE_INITIALIZED);
                   delegateFactory.setMonitoringStates(MfMonitoringState.STATE_INITIALIZED);
                   MfStatesManager.setState(MfStatesManager.STATE_STEADY);
                }

                // handle LEAVE messages 
                if (msg.getMessageType() == MfDiscoveryParameters.MSG_TYPE_LEAVE) {
                    logger.finest("DISCOVERY LEAVE message");
                    sManager.stop();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in discovery event handling", e);
        }
    }

    /**
     * Converts the user data to a java.util.Map.
     *
     * @param  data  user data as byte array
     * @return  user data converted as java.util.Map
     * 
     * @throws  ClassNotFoundException  if class not found
     * @throws  IOExceptoin  if an i/o error
     */
    private Map deserializeUserData(byte[] data) 
            throws ClassNotFoundException, IOException {

        Map map = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);

            map = (Map) ois.readObject();
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) { }
        }

        return map;
    }

    // ---- VARIABLES - PRIVATE ---------------------------------------
    private String sourceClass                = ASModule.class.getName();
   //"com.sun.mfwk.mfwk_module:type=mfDiscoveryService,name=mfDiscoveryService";
}
