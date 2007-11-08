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
package com.sun.mfwk.agent.appserv.lifecycle;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import com.sun.mfwk.agent.appserv.ASServerManager;
import com.sun.mfwk.agent.appserv.ASServerManagerRegistry;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.discovery.ASMBeanDiscoveryService;
import com.sun.mfwk.agent.appserv.discovery.ASMBeanDiscoveryServiceFactory;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryService;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryServiceFactory;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.mapping.AS_ObjectNameNotFoundException;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryServiceFactory;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.agent.appserv.util.Constants;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Class used to test the ListenerManager funcationality.
 * Its a standard jmx NotificationListener.
 */
public class ListenerImpl implements NotificationListener {
   /**
    * Creates the <code>Listener</code> object for a given server.
    *
    * @param server the server instance name this listener is listening on
    * @param domain the domain name of the application server
    */
    public ListenerImpl(String server, String domain) throws IOException {
        this.server = server;
        this.domain = domain;
        ASServerManagerRegistry serverManagerRegistry =
            ASServerManagerRegistry.getInstance();
        mediator = serverManagerRegistry.getASServerManager(server, domain);
    }


   /**
    * Notification handler.
    *
    * @param notification notification recieved
    * @param handback same object that was passed to mbeanserver during registration of this listener
    */
    public void handleNotification(Notification notification, Object handback) {
        if(notification.getClass().getName().equals("javax.management.MBeanServerNotification")) {    //NOI18N
            MBeanServerNotification mbeanServerNotification = (MBeanServerNotification)notification;
            //printInfo(msn);

            ObjectName asObjectName = (ObjectName)mbeanServerNotification.getUserData();
            String monitoringASMBean = null;

            if((asObjectName != null) && (server != null)) {

                if(REGISTER_EVENT.equals(notification.getType())) {
                    loadCmmMbean(asObjectName);
                }

                if(UNREGISTER_EVENT.equals(notification.getType())) {
                    unloadCmmMbean(asObjectName);
                }
            }
        }
    }


   /**
    * Loads CMM mbeans. This method gets called on registration of AS
    * monitoring mbean. 
    * 
    * @param  ObjectName the newly registered AS monitoring mbean Object Name.
    */
    private void loadCmmMbeans(ObjectName objectName) {
        logger = getLogger();
        if(logger != null){
            logger.log(Level.FINE, "Loading CMM objects for " + objectName);
        }

        ASServerManager mediator = new ASServerManager(server, domain);
        if((mediator == null) || (objectName == null)){
            throw new IllegalArgumentException();
        }

        try {
            //get the connection
            ConnectionRegistry registry = ConnectionRegistry.getInstance();
            MBeanServerConnection connection = registry.getConnection(server, domain);

            ASMBeanDiscoveryServiceFactory dicoveryServiceFactory = 
                ASMBeanDiscoveryServiceFactory.getInstance();

            ASMBeanDiscoveryService discoveryService = 
                    dicoveryServiceFactory.getASMBeanDiscoveryService(connection);
            Set asMbeans = discoveryService.discoverASMBeans(objectName);
            mediator.load(asMbeans);

        } catch(Exception e){
            if(logger != null){
                logger.log(Level.SEVERE, "Error while loading AS mbeans", e);
            }
        }
    }


   /**
    * Loads CMM mbeans. This method gets called on registration of AS
    * monitoring mbean. 
    * 
    * @param  ObjectName the newly registered AS monitoring mbean Object Name.
    */
    private void loadCmmMbean(ObjectName objectName) {
        logger = getLogger(); 
        if(logger != null){
            logger.log(Level.FINE, "Loading CMM object for " + objectName);
        }

        if((mediator == null) || (objectName == null)){
            throw new IllegalArgumentException();
        }

        try {
            mediator.load(objectName);
        } catch(Exception e){
            if(logger != null){
                logger.log(Level.SEVERE, "Error while loading AS mbeans", e);
            }
        }
    }
    

    /**
     * Unloads CMM mbeans. This method gets called on unregistration of 
     * AS monitoring mbean.
     * 
     * @param  ObjectName the unregistered AS monitoring mbean Object Name.
     */
     private void unloadCmmMbeans(ObjectName asObjectName) {
        logger = getLogger();
        if(logger != null){
            logger.log(Level.FINE, "unloading CMM objects for " + asObjectName);
        }

        try {
            ASServerManager mediator = new ASServerManager(server, domain);
            if((mediator == null) || (asObjectName == null)) {
                throw new IllegalArgumentException();
            }

            String cmmObjectNameTemplate = 
                getCMMObjectName(asObjectName.toString(), CMM_OBJECT_TYPE);

            if(cmmObjectNameTemplate != null) {
                Map tokenMap = new Hashtable();
                tokenMap.put(Constants.SERVER_NAME_PROP, server);
                tokenMap.put(Constants.DOMAIN_NAME_PROP, domain);
                String cmmObjectName = ObjectNameHelper.tokenizeON(asObjectName, 
                    cmmObjectNameTemplate, tokenMap);

                CMMMBeanDiscoveryServiceFactory dicoveryServiceFactory = 
                    CMMMBeanDiscoveryServiceFactory.getInstance();

                CMMMBeanDiscoveryService discoveryService = 
                    dicoveryServiceFactory.getCMMMBeanDiscoveryService();
                if(discoveryService != null) {
                    Set cmmMBeans = 
                        discoveryService.discoverCMMMBeans(new ObjectName(cmmObjectName));
                    mediator.unload(cmmMBeans);
                } else {
                    if(logger != null){
                        logger.log(Level.SEVERE, 
                            "Error - Not able to get hold of Discovery Service");
                    }
                }
            } else {
                if(logger != null){
                    logger.log(Level.SEVERE, 
                        "Error - Not able to get hold of core CMM Object");
                }
            }
        } catch(Exception exception){
            if(logger != null){
                logger.log(Level.SEVERE, 
                    "Error while unloading AS mbeans", exception);
            }
        }
     }


    /**
     * Unloads CMM mbeans. This method gets called on unregistration of 
     * AS monitoring mbean.
     * 
     * @param  ObjectName the unregistered AS monitoring mbean Object Name.
     */
     private void unloadCmmMbean(ObjectName asObjectName) {
         logger = getLogger();
        if(logger != null){
            logger.log(Level.FINE, "unloading CMM objects for " + asObjectName);
        }

        try {
            if((mediator == null) || (asObjectName == null)) {
                throw new IllegalArgumentException();
            }

            Set cmmObjectNameTemplates = 
                getCMMObjectNames(asObjectName.toString());

            if(cmmObjectNameTemplates != null) {
                Map tokenMap = new Hashtable();
                tokenMap.put(Constants.SERVER_NAME_PROP, server);
                tokenMap.put(Constants.DOMAIN_NAME_PROP, domain);
                String cmmObjectName = null;
                Set cmmMBeans = new HashSet();
                Iterator iterator = cmmObjectNameTemplates.iterator();
                while(iterator.hasNext()) {
                    cmmObjectName = ObjectNameHelper.tokenizeON(asObjectName, 
                        (String)iterator.next(), tokenMap);

                    cmmMBeans.add(new ObjectName(cmmObjectName));
                }
                mediator.unload(cmmMBeans);
            } else {
                if(logger != null){
                    logger.log(Level.SEVERE, 
                        "Error - Not able to get hold of CMM Object Names");
                }
            }
        } catch(Exception exception){
            if(logger != null){
                logger.log(Level.SEVERE, 
                    "Error while unloading AS mbeans", exception);
            }
        }
     }


    /**
     * Gets the <code>Logger</code> to user.
     * 
     * @returns  Logger the logger to user
     */
     private Logger getLogger(){
         if(logger == null){
            logger = LogDomains.getLogger();
         }
         return logger;
     }


    /**
     * Gets the CMM Object Name template from the descriptor for the given
     * AS Object Name.
     * 
     * @returns  String the CMM Object Name template or null in case of an error
     */
     private String getCMMObjectName(String AS_ObjectName, String type) 
         throws AS_ObjectNameNotFoundException, IOException {
        String cmmObjectNameTemplate = null;

        MappingQueryServiceFactory mqsf = 
             MappingQueryServiceFactory.getInstance();
        MappingQueryService queryService = 
            mqsf.getMappingQueryService(server, domain);

        NodeList nodeList = queryService.getCMM_Mbeans(AS_ObjectName, type);
        Element cmmElement = null;
        if(nodeList.getLength() == 1) {
            cmmElement = (Element)nodeList.item(0);
            if (cmmElement == null)
                throw new AS_ObjectNameNotFoundException(AS_ObjectName);
            cmmObjectNameTemplate = (String)cmmElement.getAttribute(OBJECT_NAME);
        }
        return cmmObjectNameTemplate;
    }


   /**
    * Gets the CMM Object Name templates from the descriptor for the given
    * AS Object Name.
    *
    * @param AS_OjectName the given AS Object Name.
    *
    * @returns  Set the Set containing the CMM Object Name templates.
    */
    private Set getCMMObjectNames(String AS_ObjectName)
        throws AS_ObjectNameNotFoundException, IOException {
       HashSet cmmObjectNameTemplates = new HashSet();

       MappingQueryServiceFactory mqsf =
            MappingQueryServiceFactory.getInstance();
       MappingQueryService queryService = 
           mqsf.getMappingQueryService(server, domain);
       NodeList nodeList = queryService.getCMM_Mbeans(AS_ObjectName);
       Element cmmElement = null;
       for(int i=0; i<nodeList.getLength(); i++) {
           cmmElement = (Element)nodeList.item(i);
           if (cmmElement == null)
               throw new AS_ObjectNameNotFoundException(AS_ObjectName);
           cmmObjectNameTemplates.add(cmmElement.getAttribute(OBJECT_NAME));
       }
       return cmmObjectNameTemplates;
   }


    private String server;
    private String domain;
    private ASServerManager mediator;
    private Logger logger;
    static private final String REGISTER_EVENT = "JMX.mbean.registered";
    static private final String UNREGISTER_EVENT = "JMX.mbean.unregistered";

    private static final String OBJECT_NAME = "objectName";

    private static final String CMM_OBJECT_TYPE = "core";
}
