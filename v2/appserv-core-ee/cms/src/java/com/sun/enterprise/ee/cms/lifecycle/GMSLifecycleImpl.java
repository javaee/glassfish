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

package com.sun.enterprise.ee.cms.lifecycle;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.ext.IiopInfo;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.util.IAdminConstants;

import javax.management.*;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of ServerLifecycle interface conforming to application server
 * lifecycle programming model.
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 * Date: Mar 1, 2004
 * @version $Revision: 1.11 $
 */
public class GMSLifecycleImpl extends ServerLifecycleImpl {
    private com.sun.enterprise.ee.cms.core.GroupManagementService gms;
    private String instanceName;
    private ServerContext serverContext;
    private final Logger logger = Logger.getLogger("javax.ee.enterprise.system.gms");
    private StandardMBean mbean;
    private static final String GmsClientName =
            new StringBuffer().append(AdminService.PRIVATE_MBEAN_DOMAIN_NAME)
                .append(":type=GMSClientMBean,category=monitor")
                .toString();
    private boolean isDASInstance = false;
    private boolean isGMSEnabled = true;
    private MBeanServer mbs = null;
    private final GMSConstants.shutdownType shutdownType =
            GMSConstants.shutdownType.INSTANCE_SHUTDOWN;

    public void onInitialization(final ServerContext serverContext)
            throws ServerLifecycleException {
        instanceName = serverContext.getInstanceName();
        this.serverContext = serverContext;
        final String gmsClientMBeanName =
                "com.sun.enterprise.ee.admin.mbeans.GMSClientMBean";
        final String gmsClientIntfName =
                "com.sun.enterprise.ee.admin.mbeans.GMSClient";
        final String gmsClientListenerName =
                "com.sun.enterprise.ee.admin.clientreg.RegistrySynchronizer";
        final String gmsClientStdMBeanName =
                "com.sun.enterprise.ee.admin.mbeans.GMSClientStandardMBean";

        try {
            if(!AdminService.getAdminService().isDas()){
                initializeGMSForThisInstance( serverContext );
            }
            else {
                isDASInstance = true;
                final Object gcm =
                    Class.forName(gmsClientMBeanName).newInstance();
                final Object[] initargs = {gcm, Class.forName(gmsClientIntfName)};

                final Class gcsmClass = Class.forName(gmsClientStdMBeanName);
                final Constructor[] gcsmConstructors = gcsmClass.getConstructors();
                final Constructor gcsmConstructor = gcsmConstructors[0];
                mbean = (StandardMBean)gcsmConstructor.newInstance(initargs);

                mbs = MBeanServerFactory.getMBeanServer();
                final ObjectName gmsClientMBeanObjName = new ObjectName(GmsClientName);
                mbs.registerMBean(mbean, gmsClientMBeanObjName );
                final NotificationListener gmsClientListener =  (NotificationListener)
                    Class.forName(gmsClientListenerName).newInstance();
                mbs.addNotificationListener(gmsClientMBeanObjName,
                    gmsClientListener, null, null);
                mbs.invoke( new ObjectName(GmsClientName),
                            "initGMSGroupForAllClusters",
                            new Object[]{}, new String[]{});
            }
            // Self management hook
            final String GMS_EVENT_FACTORY_CLASS =
                "com.sun.enterprise.ee.selfmanagement.events.GMSEventFactory";
            try {
                final Class c = Class.forName(GMS_EVENT_FACTORY_CLASS);
            } catch (Exception ex) {
                logger.log(Level.FINE, "No class found: " +
                     GMS_EVENT_FACTORY_CLASS , ex);
            }

        } catch (ConfigException e) {
            logger.log(Level.FINEST, e.getClass().getName()+':'
                              +"GroupManagementService will initialize only for"
                             +"clustered instances:"+e.getLocalizedMessage());
        }
        catch ( IllegalAccessException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( ClassNotFoundException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( InstantiationException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( NotCompliantMBeanException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( MBeanRegistrationException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( MalformedObjectNameException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( InstanceAlreadyExistsException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( ReflectionException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( InstanceNotFoundException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
        catch ( MBeanException e ) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        } catch (java.lang.reflect.InvocationTargetException e) {
            logger.log(Level.WARNING, e.getClass().getName()+':'
                                      +e.getLocalizedMessage());
        }
    }

    public void onStartup(final ServerContext serverContext)
            throws ServerLifecycleException {
    }

    private void initializeGMSForThisInstance (
            final ServerContext serverContext ) throws ConfigException {
        final Cluster cluster;
        final String clusterName;
        cluster = ClusterHelper
                        .getClusterForInstance(
                                serverContext.getConfigContext(),
                                instanceName) ;
        if(cluster != null && cluster.isHeartbeatEnabled()) {
            try {
                final Class gmsFactory = Class.forName(
                        "com.sun.enterprise.ee.cms.core.GMSFactory");

                logger.log(Level.INFO,
                   "Initializing and Starting GroupManagementService" );
                clusterName = cluster.getName();

                GMSFactory.setGMSEnabledState( clusterName, Boolean.TRUE);
                final Properties props  = getGMSConfigProps(cluster,
                                              serverContext.getConfigContext());
                gms = (com.sun.enterprise.ee.cms.core.GroupManagementService) GMSFactory.startGMSModule(
                        instanceName,
                        clusterName,
                        //assume a core member if not DAS
                        com.sun.enterprise.ee.cms.core.GroupManagementService.MemberType.CORE,
                        props );

                gms.join();
                //only add IIOP endpoints here.
                addMemberDetails(instanceName, serverContext.getConfigContext());
            }
            catch (ClassNotFoundException e){
                isGMSEnabled = false;
                logger.log(Level.WARNING,
                            "GroupManagementService classes are not available "+
                            "in the classpath."+ e.getLocalizedMessage() +
                            ". Continuing startup without GroupManagementServices enabled.");
            }
            catch (GMSException e ){
                logger.log(Level.WARNING, "Exception Occured in GMS Initialization:"+e);
            }

        }
    }

    private Properties getGMSConfigProps ( final Cluster cluster,
                                           final ConfigContext configContext ) {
        final Properties props = new Properties();
        final Config config;
        try {
            final String configRef = cluster.getConfigRef();
            config = ConfigAPIHelper.getConfigByName( configContext,
                                                     configRef);
            final com.sun.enterprise.config.serverbeans.GroupManagementService
                    gmsConfig = config.getGroupManagementService();
            props.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_RETRIES.toString(),
                      gmsConfig.getFdProtocolMaxTries());
            props.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TIMEOUT.toString(),
                      gmsConfig.getFdProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.DISCOVERY_TIMEOUT.toString(),
                      gmsConfig.getPingProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.FAILURE_VERIFICATION_TIMEOUT.toString(),
                      gmsConfig.getVsProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.MULTICASTADDRESS.toString(),
                      cluster.getHeartbeatAddress());
            props.put(ServiceProviderConfigurationKeys.MULTICASTPORT.toString(),
                      cluster.getHeartbeatPort());
        }
        catch ( ConfigException e ) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
        return props;
    }

    private void addMemberDetails (final String instanceName,
                                   final ConfigContext configContext)
    {
        try {
            addIIOPEndPoints( instanceName, configContext);
        }
        catch ( GMSException e ) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
        catch ( ConfigException e ) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    private void addIIOPEndPoints(final String instanceName,
                                  final ConfigContext configContext)
            throws ConfigException, GMSException
    {

      IiopInfo iiopInfo;
      final Map<String, IiopListener[]> lsnrMap ;
      lsnrMap = getResolvedIiopListenersInCluster( true, configContext, instanceName );

        if(!lsnrMap.isEmpty()){

            for(String instance : lsnrMap.keySet()){
                final List<IiopInfo> infoList = new ArrayList<IiopInfo>();
                final IiopListener[] listeners = lsnrMap.get(instance);
                final Server server= ServerHelper.getServerByName(configContext, instance);
                for(IiopListener listener : listeners){
                    iiopInfo = getIiopInfo( listener);
                    iiopInfo.setWeight( Integer.parseInt( server.getLbWeight()));
		    try {
		        String nodeAgentHostName = getNodeAgentHostName(configContext, server);
			iiopInfo.setHostName(nodeAgentHostName);

		    } catch (ConfigException configEx) {
		        logger.log(Level.WARNING, configEx.getMessage());
		    }
		    infoList.add(iiopInfo);
                }
                gms.updateMemberDetails(instance,
                                        IiopInfo.IIOP_MEMBER_DETAILS_KEY,
                                        (Serializable)infoList);
            }
        }

    }

    private Map<String, IiopListener[]> getResolvedIiopListenersInCluster(final boolean includeMe,
									 ConfigContext configContext,
									 final String instanceName)
      throws ConfigException {
        final Map<String, IiopListener[]> map = new HashMap<String, IiopListener[]> ();
        if (ServerHelper.isServerClustered(configContext, instanceName)) {
            final String myCluster      = ClusterHelper.getClusterForInstance(configContext, instanceName).getName();
            final Server[] buddies      = ServerHelper.getServersInCluster(configContext, myCluster);
            for (final Server as : buddies) {
                if (!includeMe && instanceName.equals(as.getName()))
                    continue;
                final IiopListener[] ls = getResolvedIiopListenersInServer(as, configContext);
                map.put(as.getName(), ls);
            }
        }
        return ( map );
    }


    private IiopListener[] getResolvedIiopListenersInServer(final Server server,
							     ConfigContext configContext)
      throws ConfigException {
	  final Config cfg                = ServerHelper.getConfigForServer(configContext, server.getName());
	  final IiopService iiop          = cfg.getIiopService();
	  final IiopListener[] lsnrs      = iiop.getIiopListener();
	  final IiopListener[] values     = new IiopListener[lsnrs.length];
	  int i = 0;
	  for (final IiopListener lsnr : lsnrs) {
	      final IiopListener copy = new IiopListener(); //avoid side effecting, hence send only copy!
	      String resolved = resolve(server.getName(), SystemPropertyConstants.unSystemProperty(lsnr.getRawAttributeValue(ServerTags.ID)), configContext);
	      copy.setId(resolved);
	      resolved = resolve(server.getName(), SystemPropertyConstants.unSystemProperty(lsnr.getRawAttributeValue(ServerTags.PORT)), configContext);
	      copy.setPort(resolved);
	      resolved = resolve(server.getName(), SystemPropertyConstants.unSystemProperty(lsnr.getRawAttributeValue(ServerTags.ADDRESS)), configContext);
	      copy.setAddress(resolved);
	      logger.fine("Iiop Service Endpoint for Server: " + server.getName() + ", iiop-lsnr-id = " + copy.getId() +
			  ", iiop-lsnr-port = " + copy.getPort() + ", iiop-lsnr-address = " + copy.getAddress());
	      values[i++] = copy;
	  }
	  return ( values );
    }

    private String resolve(final String server, final String value, ConfigContext configContext ) throws ConfigException {
        final PropertyResolver pr = new PropertyResolver(configContext, server);
        String resolved = pr.getPropertyValue(value, true);
        if (resolved == null) // the property could not be resolved, returned what was passed
	    resolved = value;
        return ( resolved );
    }

    private String getNodeAgentHostName(ConfigContext configContext, Server server)   throws ConfigException {

        final NodeAgent na = NodeAgentHelper.getNodeAgentForServer(configContext, server.getName());
        final boolean dasShookHandsWithNodeAgent = NodeAgentHelper.hasNodeAgentRendezvousd(configContext, na);
        if (! dasShookHandsWithNodeAgent)
	  throw new ConfigException("Error: NA: " + na.getName() + " has not rendezvous'ed with DAS");
        final String naHost = NodeAgentHelper.getNodeAgentSystemConnector(configContext, na.getName()).getElementPropertyByName(IAdminConstants.HOST_PROPERTY_NAME).getValue();
        return ( naHost );
      }

    /**
     * Fetch the AdminListener for the given server instance. The server
     * instance must exist or an exception is thrown.
     */
    private IiopInfo getIiopInfo(final IiopListener il )
    {
	    final IiopInfo info = new IiopInfo();
	    final String address = il.getAddress();
	    final String port = il.getPort();
        final String id = il.getId();

	    info.setAddress(address);
	    info.setPort(port);
        info.setID( id );
	    return info;
    }

    public void onReady(final ServerContext serverContext)
            throws ServerLifecycleException {
    }

    public void onShutdown() throws ServerLifecycleException {
        if(isDASInstance){
           if(mbs != null ){
               try {
                   mbs.invoke(new ObjectName(GmsClientName),
                                "leaveGMSGroupForAllClusters",
                    new Object[]{ shutdownType },
                    new String[]{ GMSConstants.shutdownType.class.getName() });
               }
               catch ( InstanceNotFoundException e ) {
                   logger.log( Level.WARNING, e.getClass().getName()+':'
                                      + e.getLocalizedMessage());
               }
               catch ( MBeanException e ) {
                   logger.log( Level.WARNING, e.getClass().getName()+':'
                                      + e.getLocalizedMessage());
               }
               catch ( ReflectionException e ) {
                   logger.log( Level.WARNING, e.getClass().getName()+':'
                                      + e.getLocalizedMessage());
               }
               catch ( MalformedObjectNameException e ) {
                   logger.log( Level.WARNING, e.getClass().getName()+':'
                                      + e.getLocalizedMessage());
               }
           }
        }
        else if(isGMSEnabled && gms != null) {
            gms.shutdown(shutdownType);
		}
    }

    public void onTermination() throws ServerLifecycleException {
    }
}
