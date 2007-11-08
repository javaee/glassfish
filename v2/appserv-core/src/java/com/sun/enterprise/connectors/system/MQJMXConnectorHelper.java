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

package com.sun.enterprise.connectors.system;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.ResourceAdapter;
import com.sun.enterprise.Switch;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AppserverClusterViewFromCacheRepository;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.server.ManagerFactory;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigContext;

//import javax.management.*;
//import javax.management.remote.*;
//import com.sun.messaging.AdminConnectionFactory;
//import com.sun.messaging.jms.management.*;

/**
 * A Utility class to obtain JMXConnectorInfo for MQ broker instances  
 * @author Sivakumar Thyagarajan sivakumar.thyagarajan@sun.com
 */
public class MQJMXConnectorHelper {
    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    private static final String JMXSERVICEURLLIST = "JMXServiceURLList";
    private static final String JMXCONNECTORENV = "JMXConnectorEnv";
    
    private static StringManager sm = StringManager.getManager(
                                                   MQJMXConnectorHelper.class);
    

    public static MQJMXConnectorInfo[] getMQJMXConnectorInfo(final String target) 
                                throws ConnectorRuntimeException {
        try {
            final Target tgt = getTarget(target);
            final Config config = tgt.getConfigs()[0];
            final JmsService jmsService = config.getJmsService();
            
            ActiveJmsResourceAdapter air = getMQAdapter();
            final Class mqRAClassName = air.getResourceAdapter().getClass();
            
            MQJMXConnectorInfo mqjmxForServer = (MQJMXConnectorInfo)
            java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction() {
                 public java.lang.Object run() throws Exception {
                     if(!isClustered(tgt)) {
                         _logger.log(Level.FINE, "Getting JMX connector for" +
                                               " standalone target " + target);
                         return _getMQJMXConnectorInfo(tgt.getName(),
                                                      jmsService, mqRAClassName);
                     } else {
                         _logger.log(Level.FINE, "Getting JMX connector for"  +
                                                    " cluster target " + target);
                         return _getMQJMXConnectorInfoForCluster(tgt.getName(), 
                                                      jmsService, mqRAClassName);
                     }
                 }
            });
          
            return new MQJMXConnectorInfo[]{mqjmxForServer};
        } catch (Exception e) {
            //e.printStackTrace();
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }
    }

    /*
     *  Starts the MQ RA in the DAS, as all MQ related operations are 
     *  performed in DAS.
     */
    private static ActiveJmsResourceAdapter getMQAdapter() throws Exception {
        //Start ActiveJMSResourceAdapter.
        final String mqraModuleName = ConnectorRuntime.DEFAULT_JMS_ADAPTER;

        ActiveJmsResourceAdapter air = (ActiveJmsResourceAdapter)
        java.security.AccessController.doPrivileged
            (new java.security.PrivilegedExceptionAction() {
                 public java.lang.Object run() throws Exception {
                     ManagerFactory.getSAConnectorModulesManager().
                     loadOneSystemApp(mqraModuleName, true);
                     return (ActiveJmsResourceAdapter) ConnectorRegistry.getInstance().
                           getActiveResourceAdapter(mqraModuleName);
             }
        });
        return air;
    }

    /**
     *  Gets the <code>MQJMXConnector</code> object for a cluster. Since this code is 
     *  executed in DAS, an admin API is used to resolve hostnames and ports of 
     *  cluster instances for LOCAL type brokers while creating the connectionURL.
     */
    private static MQJMXConnectorInfo _getMQJMXConnectorInfoForCluster(
                    String target, JmsService jmsService, Class mqRAClassName) 
                    throws ConnectorRuntimeException {
        // Create a new RA instance.
        ResourceAdapter raInstance = null;
        // Set the ConnectionURL.
        try {
            MQAddressList list = null;

            if (jmsService.getType().equalsIgnoreCase(ActiveJmsResourceAdapter.REMOTE)) {
                list = getDefaultAddressList(jmsService);
            } else {
                String domainurl  =
                    ApplicationServer.getServerContext().getServerConfigURL();
                AppserverClusterViewFromCacheRepository rep
                    = new AppserverClusterViewFromCacheRepository(domainurl);

                java.util.Map<String,JmsHost> hostMap =
                    rep.getResolvedLocalJmsHostsInCluster(target);

                if ( hostMap.size() == 0 ) {
                    String msg = sm.getString("mqjmx.no_jms_hosts");
                    throw new ConnectorRuntimeException(msg);
                }
                
                list = new MQAddressList();
                for (JmsHost host : hostMap.values()) {
                    list.addMQUrl(host);
                }
            }

            String connectionUrl = list.toString();
            raInstance = getConfiguredRA(mqRAClassName, connectionUrl);
        } catch (Exception e) {
            e.printStackTrace();
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        } 
        
        try {
            String jmxServiceURL = null, jmxServiceURLList = null;
            Map<String, ?> jmxConnectorEnv = null; 
            Method[] methds = raInstance.getClass().getMethods();
            for (int i = 0; i < methds.length; i++) {
                Method m = methds[i];
                if (m.getName().equalsIgnoreCase("get" + JMXSERVICEURLLIST)){ 
                    jmxServiceURLList = (String)m.invoke(raInstance, new Object[]{});
                    if (jmxServiceURLList != null && !jmxServiceURLList.trim().equals("")){
                        jmxServiceURL = getFirstJMXServiceURL(jmxServiceURLList);
                    }
                } else if (m.getName().equalsIgnoreCase("get" + JMXCONNECTORENV)){
                    jmxConnectorEnv = (Map<String,?>)m.invoke(raInstance, new Object[]{});
                }
            }
            MQJMXConnectorInfo mqInfo = new MQJMXConnectorInfo(target,
                            ActiveJmsResourceAdapter.getBrokerInstanceName(jmsService) ,
                            jmsService.getType(), jmxServiceURL, jmxConnectorEnv);
            return mqInfo;
        } catch (Exception e) {
            e.printStackTrace();
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        } 
    }

    /**
     *  Gets the <code>MQJMXConnector</code> object for a standalone server instances 
     *  and destinations created in DAS/PE instance. The default address list is used 
     *  while setting the ConnectionURL in MQ RA. 
     */
    private static MQJMXConnectorInfo _getMQJMXConnectorInfo(
                    String targetName, JmsService jmsService, Class mqRAClassName) 
                                        throws ConnectorRuntimeException {
        try {
            //If DAS, use the default address list, else obtain 
            
            String connectionURL = null;
            
            boolean isDAS = isDAS(targetName);
            
            if (isDAS) {
                logFine(" _getMQJMXConnectorInfo - in DAS");
                _logger.log(Level.FINE,"In DAS");
                connectionURL = getDefaultAddressList(jmsService).toString();
            } else {
                //Standalone server instance
                _logger.log(Level.FINE,"not in DAS");
                logFine(" _getMQJMXConnectorInfo - NOT in DAS");
                String domainurl  =
                ApplicationServer.getServerContext().getServerConfigURL();
                JmsService serverJmsService= getJmsServiceOfStandaloneServerInstance(targetName);
                MQAddressList mqadList = new MQAddressList(serverJmsService, targetName);
                mqadList.setup();
                connectionURL = mqadList.toString();                
            }
            logFine(" _getMQJMXConnectorInfo - connection URL " + connectionURL);
            
            ResourceAdapter raInstance = getConfiguredRA(mqRAClassName, connectionURL); 
            String jmxServiceURL = null, jmxServiceURLList = null;
            Map<String, ?> jmxConnectorEnv = null; 
            Method[] methds = raInstance.getClass().getMethods();
            for (int i = 0; i < methds.length; i++) {
                Method m = methds[i];
		if (m.getName().equalsIgnoreCase("get" + JMXSERVICEURLLIST)){ 
                    jmxServiceURLList = (String)m.invoke(raInstance, new Object[]{});
                } else if (m.getName().equalsIgnoreCase("get" + JMXCONNECTORENV)){
                    jmxConnectorEnv = (Map<String,?>)m.invoke(raInstance, new Object[]{});
                }
            }
            logFine(" _getMQJMXConnectorInfo - jmxServiceURLList " +  jmxServiceURLList);
            logFine(" _getMQJMXConnectorInfo - jmxConnectorEnv " + jmxConnectorEnv);
            jmxServiceURL = getFirstJMXServiceURL(jmxServiceURLList);
            
            MQJMXConnectorInfo mqInfo = new MQJMXConnectorInfo(targetName,
                            ActiveJmsResourceAdapter.getBrokerInstanceName(jmsService) ,
                            jmsService.getType(), jmxServiceURL, jmxConnectorEnv);
            return mqInfo;
        } catch (Exception e) {
            e.printStackTrace();
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        } 
    }
    
    private static boolean isDAS(String targetName) throws ConfigException {
        ConfigContext con = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        if (isAConfig(targetName)) {
            return false;   
        }
        return ServerHelper.isDAS(con, targetName);
    }
    
    private static boolean isAConfig(String targetName) throws ConfigException {
        ConfigContext con = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        return ServerHelper.isAConfig(con, targetName);
    }
    

    private static JmsService getJmsServiceOfStandaloneServerInstance(String targetName) throws ConfigException {
        logFine("getJMSServiceOfSI LL " + targetName);
        ConfigContext con = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        
        Config cfg = null;
        if (isAConfig(targetName)) {
            cfg = ServerHelper.getConfigByName(con, targetName);
        } else {
            cfg = ServerHelper.getConfigForServer(con, targetName);
        }
        
        logFine("cfg " + cfg);
        JmsService jmsService     = cfg.getJmsService();
        logFine("jmsservice " + jmsService);
        return jmsService;
    }

    /*
     *  Configures an instance of MQ-RA with the connection URL passed in.
     *  This configured RA is then used to obtain the JMXServiceURL/JMXServiceURLList
     */
    private static ResourceAdapter getConfiguredRA(Class mqRAclassname, 
                                       String connectionURL) throws Exception {
        ResourceAdapter raInstance = (ResourceAdapter) mqRAclassname.newInstance();
        Method setConnectionURL = mqRAclassname.getMethod(
                       "set" + ActiveJmsResourceAdapter.CONNECTION_URL,
                        new Class[] { String.class});
        setConnectionURL.invoke(raInstance, new Object[] {connectionURL});
        logFine(" getConfiguredRA - set connectionURL as " + connectionURL);
        return raInstance;
    }
    
    private static MQAddressList getDefaultAddressList(JmsService jmsService) 
                                                       throws ConfigException {
        MQAddressList list = new MQAddressList(jmsService);
        list.setup();
        return list;
    }
    
    private static String getFirstJMXServiceURL(String jmxServiceURLList) {
        //If type is REMOTE, MQ RA returns a null jmxServiceURL and a non-null
        //jmxServiceURLList for PE also.
        if ((jmxServiceURLList == null) || ("".equals(jmxServiceURLList))) {
            return jmxServiceURLList;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(jmxServiceURLList, " ");
            return  tokenizer.nextToken();
        }
    }

    private static Target getTarget(String target) throws ConfigException {
        final TargetType[] vaildTargetTypes = new TargetType[] {
                        TargetType.CONFIG,
                        TargetType.SERVER,
                        TargetType.DOMAIN,
                        TargetType.CLUSTER,
                        TargetType.STANDALONE_SERVER,
                        TargetType.UNCLUSTERED_SERVER,
                        TargetType.STANDALONE_CLUSTER,
                        TargetType.DAS };
        final Target tgt = TargetBuilder.INSTANCE.createTarget(
                        vaildTargetTypes, target, 
                        com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext());
        assert tgt != null;
        return tgt;
    }

    private static boolean isClustered(Target tgt) throws ConfigException {
        return tgt.getType() == TargetType.CLUSTER;
    }

    private static void logFine(String s) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "MQJMXConnectorHelper :: " + s);
        }
    }
    
    /*    
    //Simple method for testing
    private static void test(MQJMXConnectorInfo mqjmxForServer) {
        try {
            MBeanServerConnection mbsc = mqjmxForServer.getMQMBeanServerConnection();
            ObjectName objName = new ObjectName(
                            MQObjectName.DESTINATION_MONITOR_MANAGER_MBEAN_NAME);
            String destinationObjNames[] = (String[]) mbsc.invoke(objName,
                            DestinationOperations.GET_DESTINATIONS, null, null);
            _logger.log(Level.FINE,"Listing destinations:");
            for (int i = 0; i < destinationObjNames.length; ++i) {
                ObjectName oneDestObjName = new ObjectName(
                                destinationObjNames[i]);
                _logger.log(Level.FINE,"\tName: "
                                + mbsc.getAttribute(oneDestObjName,
                                                DestinationAttributes.NAME));
                _logger.log(Level.FINE,"\tType: "+ mbsc.getAttribute(oneDestObjName,
                                DestinationAttributes.TYPE_LABEL));
                _logger.log(Level.FINE,"\tState: " + mbsc.getAttribute(oneDestObjName,
                                DestinationAttributes.STATE_LABEL));
                _logger.log(Level.FINE,"\tMessages: " + mbsc.getAttribute(oneDestObjName,
                                DestinationAttributes.AVG_NUM_MSGS));
                _logger.log(Level.FINE,"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
*/  
}
