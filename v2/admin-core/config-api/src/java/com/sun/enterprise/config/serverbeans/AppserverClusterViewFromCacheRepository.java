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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * AppserverClusterViewFromCacheRepository.java
 *
 * Created on August 2, 2005, 2:46 PM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AppserverClusterViewFromCacheRepository {
    
    private final String myName;
    private final ConfigContext domainCC;
    public AppserverClusterViewFromCacheRepository(final String domainXmlUrl) throws ConfigException {
        if (domainXmlUrl == null) {
            throw new IllegalArgumentException("null_arg");
        }
        myName              = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        final String url    = domainXmlUrl;
        // enable caching - with caching domain.xml gets read/parsed one less
        domainCC            = ConfigFactory.createConfigContext(url, true, true, false, false);
    }

    public ConfigContext getUnResolvedConfigContext(){
        return domainCC;
    }

     public Map<String, JmsHost> getResolvedLocalJmsHostsInCluster(final String clusterName) throws ConfigException {
         final Map<String, JmsHost> map = new HashMap<String, JmsHost> ();
         final String myCluster      = ClusterHelper.getClusterByName(domainCC, clusterName).getName();
         final Server[] buddies      = ServerHelper.getServersInCluster(domainCC, myCluster);
         for (final Server as : buddies) {
	 try {
             final JmsHost copy   = getResolvedJmsHost(as);
             map.put(as.getName(), copy);
	 } catch (Exception e) {
		// we dont add the host if we cannot get it
		;
	 }
         }
         return map;
    }


    public Map<String, JmsHost> getResolvedLocalJmsHostsInMyCluster(final boolean includeMe) throws ConfigException {
        final Map<String, JmsHost> map = new HashMap<String, JmsHost> ();
        if (ServerHelper.isServerClustered(domainCC, myName)) {
            final String myCluster      = ClusterHelper.getClusterForInstance(domainCC, myName).getName();
            final Server[] buddies      = ServerHelper.getServersInCluster(domainCC, myCluster);
            for (final Server as : buddies) {
                if (!includeMe && myName.equals(as.getName()))
                    continue;
		try {
                	final JmsHost copy      = getResolvedJmsHost(as);
                	map.put(as.getName(), copy);
		} catch (Exception e) {
			;
		}
            }
        }
        return ( map );
    }
    
    public JmsHost getResolvedJmsHost(final Server as) throws ConfigException{
        final JmsHost jmsHost   = getResolvedLocalJmsHostInServer(as);
        final JmsHost copy      = new JmsHost();
        final String hostName = getNodeAgentHostName(as);
        if (jmsHost != null) {
             copy.setHost(hostName);
             copy.setName(jmsHost.getName());
             final String resolved = this.resolve(as.getName(), SystemPropertyConstants.unSystemProperty(jmsHost.getPort()));
             copy.setPort(resolved);
             copy.setAdminPassword(jmsHost.getAdminPassword());
             copy.setAdminUserName(jmsHost.getAdminUserName());
         }
        
        return copy;
    }

    public Map<String, JmsHost> getResolvedLocalJmsHostsInMyCluster() throws ConfigException {
        return ( this.getResolvedLocalJmsHostsInMyCluster(false) );
    }
    
    public JmsHost getMasterJmsHostInCluster(String clusterName) throws ConfigException {
        final String myCluster      = ClusterHelper.getClusterByName(domainCC, clusterName).getName();
	final Server[] buddies      = ServerHelper.getServersInCluster(domainCC, myCluster);
	 for (final Server as : buddies) {
	 	try {
             		final JmsHost copy	  = getResolvedJmsHost(as);
			// return the first valid host
			// there may be hosts attached to an NA that is down
             		return copy;
	 	} catch (Exception e) {
		// we dont add the host if we cannot get it
			;
	 	}
	}
	throw new RuntimeException("No JMS hosts available to select as Master");
		
        // final JmsHost copy   = getResolvedJmsHost(buddies[0]);
    }

    public JmsService getJmsServiceForMasterBroker(String clusterName)  throws ConfigException {
        final String myCluster      = ClusterHelper.getClusterByName(domainCC, clusterName).getName();
	final Server[] buddies      = ServerHelper.getServersInCluster(domainCC, myCluster);
        final Config cfg             =  ServerHelper.getConfigForServer(domainCC, buddies[0].getName());
        return cfg.getJmsService();
	}
	
    private JmsHost getResolvedLocalJmsHostInServer(final Server server) throws ConfigException {
        final Config cfg                = ServerHelper.getConfigForServer(domainCC, server.getName());
        final JmsService jmsService     = cfg.getJmsService();
        JmsHost jmsHost                 = null;
        if (JMSServiceType.LOCAL.toString().equals(jmsService.getType())	|| JMSServiceType.EMBEDDED.toString().equals(jmsService.getType())) {
            jmsHost = getDefaultJmsHost(jmsService);
        }
        return ( jmsHost );
    }
    
    private JmsHost getDefaultJmsHost(final JmsService jmsService) {
        final JmsHost[] jmsHosts        = jmsService.getJmsHost();
        final String defaultHostName    = jmsService.getDefaultJmsHost();
        JmsHost defaultJmsHost          = null;
        for (final JmsHost h : jmsHosts) {
            final String name = h.getName();
            if (name.equals(defaultHostName)) {
                defaultJmsHost = h;
                break;
            }
        }
        return ( defaultJmsHost );
    }
      public String getNodeAgentHostName(final Server as) throws ConfigException {
        final NodeAgent na = NodeAgentHelper.getNodeAgentForServer(domainCC, as.getName());
        final boolean dasShookHandsWithNodeAgent = NodeAgentHelper.hasNodeAgentRendezvousd(domainCC, na);
        if (! dasShookHandsWithNodeAgent)
            throw new RuntimeException("Error: NA: " + na.getName() + " has not rendezvous'ed with DAS");
        final String naHost = NodeAgentHelper.getNodeAgentSystemConnector(domainCC, na.getName()).getElementPropertyByName(IAdminConstants.HOST_PROPERTY_NAME).getValue();
        return ( naHost );
    }

    private String resolve(final String server, final String value) throws ConfigException {
        final PropertyResolver pr = new PropertyResolver(domainCC, server);
        String resolved = pr.getPropertyValue(value, true);
        if (resolved == null) // the property could not be resolved, returned what was passed
            resolved = value;
        return ( resolved );
    }
    
}
enum JMSServiceType {
    LOCAL,
    REMOTE,
    EMBEDDED
}
