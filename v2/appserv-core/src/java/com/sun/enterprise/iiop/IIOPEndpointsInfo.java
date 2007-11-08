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

package com.sun.enterprise.iiop;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;


// BEGIN imports for cluster info.
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
// END imports for cluster info.

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;

import com.sun.corba.ee.impl.orbutil.ORBConstants;

import com.sun.enterprise.util.ORBManager;
/**
 * This class is responsible for reading the domain.xml via Config API
 * and producing a list of instances in the form of ClusterInstanceInfo 
 * objects.
 * This class is designed for use by both FailoverIORInterceptor
 * and Java Web Start.
 * @author Sheetal Vartak
 * @date 1/12/05
 */

public class IIOPEndpointsInfo {  

    private static ServerRef[] serverRefs;
    private static IiopListener[][] listeners;
    private static Cluster cluster = null;

    private static Logger _logger = LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    
    private static final String baseMsg	= IIOPEndpointsInfo.class.getName();
    private static ConfigContext configCtx;

    static {
        if (ApplicationServer.getServerContext() != null) {
	    configCtx = ApplicationServer.getServerContext().getConfigContext();
	    String instanceName =
	      ApplicationServer.getServerContext().getInstanceName();	    
	    try {
	        cluster = 
		  ClusterHelper.getClusterForInstance(configCtx, instanceName);
		serverRefs = cluster.getServerRef();
		listeners = getIIOPEndPointsForCurrentCluster();
	    } catch (ConfigException e) {
	        _logger.log(Level.FINE, 
			    baseMsg + ".<static block>:" + e);	    
	    }
	}
    }

    public static ServerRef[] getServersInCluster() {
    
        if (serverRefs != null)
	    return serverRefs;
	else {
	    _logger.log(Level.FINE,
			baseMsg + ".getServersInCluster:"
			+ "no servers available. Server Context is null");
	    return null;
	}
    }

    public static IiopListener[][] getListenersInCluster() {
 
        if (listeners != null) 
	    return listeners;
	else {
	    _logger.log(Level.FINE,
			baseMsg + ".getServersInCluster:"
			+ "no listeners available. Server Context is null");
	    return null;
	}
    }

    /**
     * This method returns a list of SocketInfo objects for a particular 
     * server. This method is the common code called by 
     * getIIOPEndpoints() and getClusterInstanceInfo()
     */
    public static List<SocketInfo> getSocketInfoForServer(ServerRef serverRef, 
							  IiopListener[] listen) 
    throws ConfigException {
      
        List<SocketInfo> listOfSocketInfo =
		    new LinkedList<SocketInfo>();
	String serverName = serverRef.getRef();
	String hostName =
	  getHostNameForServerInstance(serverName);
	if (hostName == null) {
	    hostName = listen[0].getAddress();
	}
	for (int j = 0; j < listen.length; j++) { 
	    String id = listen[j].getId();
	    String port = 
	      getResolvedPort(listen[j], serverName);
	    if (_logger.isLoggable(Level.FINE)) {
	        _logger.log(Level.FINE, 
			    baseMsg + ".getSocketInfoForServer:" +
			    " adding address for "+ 
			    serverName + "/" + id +
			    "/" + hostName + "/" + port);
	    }
	    listOfSocketInfo.add(new SocketInfo(id, hostName, Integer.valueOf(port)));
	}
	return listOfSocketInfo;
    }


    /**
     * Determines if the AS process is running in EE. 
     * XXX: to refactor this to use the common implementation.
     * from PELaunch.java
     */
    private static boolean isEE() {
        boolean isEE = false;
        final String eepffc = "com.sun.enterprise.ee.server.pluggable.EEPluggableFeatureImpl";
        final String pn = "com.sun.appserv.pluggable.features";
        final String pv = System.getProperty(pn);
        if (eepffc.equals(pv)) {
            isEE = true;
        }
        return ( isEE );
    }

    /**
     * This method returns the endpoints in host:port,host1:port1,host2:port2,...
     * format. This is called by Java Web Start
     */
    public static String getIIOPEndpoints() {
        String endpoints = null;
	GroupInfoService gis;

        try {
	    _logger.log(Level.FINE,
			baseMsg + ".getIIOPEndpoints->:");
	    if (isEE()) {
	        gis = 
		  (GroupInfoService) ((ORBManager.getORB()).resolve_initial_references(
				       ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE));	    
		if (gis == null) {
		    _logger.fine(baseMsg + ".getIIOPEndpoints->:" +
				 "GroupInfoService not available.Is this PE?");
		    return null;
		} 
	    } else {
	        _logger.fine(baseMsg + ".getIIOPEndpoints->:" +
				 "This is PE");
		return null;
	    }

	    /*Cluster[] clusterInfo = ClusterHelper.getClustersInDomain(configCtx);
	      if (clusterInfo.length == 0) {
	      _logger.fine(baseMsg + ".getIIOPEndpoints->:" +
	      "This is EE. But there is either no cluster or" +
	      " the cluster has only one server instance");
	      return null;
	      
	      }
	      for (Cluster clus : clusterInfo) {
	      ServerRef[] serverRef = clus.getServerRef();		
	      IiopListener[][] listenersForServerRef = getIIOPEndPointsForCurrentCluster(serverRef);
	      // For EE and a server participating in a cluster...
	      for (int i = 0; i <serverRef.length; i++) {   
	      
	      
	      List<SocketInfo> listOfSocketInfo = 
	      getSocketInfoForServer(i, serverRef, listenersForServerRef);
	      */
	    if (serverRefs != null) {
	        for (int i = 0; i < serverRefs.length; i++) {
		    List<SocketInfo> listOfSocketInfo = 
		      getSocketInfoForServer(serverRefs[i], listeners[i]);
		    
		    for (SocketInfo si : listOfSocketInfo) {
		        if (!si.type.equals("SSL_MUTUALAUTH") &&
			    !si.type.equals("SSL")) {
			    if (endpoints == null) {
			        endpoints = si.host + ":" + si.port;
			    } else {
			        endpoints = endpoints + "," + si.host + ":" + si.port;
			    }
			}
		    }
		    
		    if (_logger.isLoggable(Level.FINE)) {
		        _logger.log(Level.FINE, 
				    baseMsg + ".getIIOPEndpoints: " +
				    endpoints);
		    }
		}
	    }
	    return endpoints;
	} catch (ConfigException e) {
	    _logger.log(Level.FINE, baseMsg + ".getIIOPEndpoints: " + "ConfigException occurred => " + e);
	    return null;
	} catch (NullPointerException e) {
	    _logger.log(Level.FINE, baseMsg + ".getIIOPEndpoints: " + "NPE occurred => " + e);
	    return null;
	} catch (org.omg.CORBA.ORBPackage.InvalidName in) {
	    _logger.log(Level.FINE,
			baseMsg + ".getIIOPEndpoints<-: " +
			ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE + 
			"doesnot exist. This is PE");
	    return null;
	} finally {
	    _logger.log(Level.FINE,
			baseMsg + ".getIIOPEndpoints<-: " + endpoints);
	}
    }

    /**
     * This method returns a ClusterInstanceInfo list.
     */
    public static List<ClusterInstanceInfo> getClusterInstanceInfo()
    {
	List<ClusterInstanceInfo> result = 
	    new LinkedList<ClusterInstanceInfo>();

	try {
	    _logger.log(Level.FINE,
			baseMsg + ".getClusterForInstanceInfo->:");

	    Cluster[] clusterInfo = ClusterHelper.getClustersInDomain(configCtx);
	    if (clusterInfo.length == 0) {
	        _logger.fine(baseMsg + ".getClusterInstanceInfo->:" +
			     "This is EE. But there is either no cluster or" +
			     " the cluster has only one server instance");
		return null;
		
	    }
	    /*for (Cluster clus : clusterInfo) {
	      ServerRef[] serverRef = clus.getServerRef();
	      IiopListener[][] listenersForServerRef = getIIOPEndPointsForCurrentCluster(serverRef);
	      
	      // For EE and a server participating in a cluster...
	      for (int i = 0; i <serverRef.length; i++) {    
	      String serverName = serverRef[i].getRef();
	      List<SocketInfo> listOfSocketInfo = 
	      getSocketInfoForServer(i, serverRef, listenersForServerRef);
	      */
	    //Server[] servers = ServerHelper.getServersInCluster(configCtx, cluster);

	    if (serverRefs != null) {
	        for (int i = 0; i < serverRefs.length; i++) {
		    String serverName = serverRefs[i].getRef();
		    Server server = ServerHelper.getServerByName(configCtx, serverName);
		    List<SocketInfo> listOfSocketInfo = 
		      getSocketInfoForServer(serverRefs[i], listeners[i]);
		    		    
		    // REVISIT - make orbutil utility
		    // and use in IiopFolbGmsClient and here.
		    SocketInfo[] arrayOfSocketInfo =
		      new SocketInfo[listOfSocketInfo.size()];
		    int x = 0;
		    for (SocketInfo si : listOfSocketInfo) {
		      arrayOfSocketInfo[x++] = si;
		    }
		    _logger.fine("server.getLbWeight() = "+ server.getLbWeight());
		    // REVISIT - default 100 weight
		    // Not used by JNLP but should put in the actual weight.
		    ClusterInstanceInfo clusterInstanceInfo = 
		      new ClusterInstanceInfo(serverName, (Integer.valueOf(server.getLbWeight())).intValue(), arrayOfSocketInfo);
		    result.add(clusterInstanceInfo);
		    if (_logger.isLoggable(Level.FINE)) {
		      _logger.log(Level.INFO, 
				  baseMsg + ".getClusterForInstance: " +
				  ASORBUtilities.toString(clusterInstanceInfo));
		       }
		}	
	    }
	    return result;
	} catch (ConfigException e) {
	    _logger.log(Level.FINE, baseMsg + ".getClusterForInstance: " + "ConfigException occurred => " + e);
	    return null;
	} catch (NullPointerException e) {
	    _logger.log(Level.FINE, baseMsg + ".getClusterForInstance: " + "NPE occurred => " + e);
	    return null;
	} finally {
	    _logger.log(Level.FINE,
			baseMsg + ".getClusterForInstanceInfo<-: " + result);
	}
    }

    /**
      * The following returns the IIOP listener(s) for all the
      * servers belonging to the current cluster.
      *
      * @author  satish.viswanatham@sun.com
      *
      * @param   configCtx             Current server's config context
      *
      * @return  IiopListener[i][j]    Two dimension array of iiop listener info
      *                                i = index of server 
      *                                j = index of iiop listner (j <= 3)
      * @throws  ConfigException       In any configuration error cases, 
      *                                ConfigException is thrown
      */
    public static IiopListener[][] getIIOPEndPointsForCurrentCluster()
	throws ConfigException
    {
	// For each server instance in a cluster, there are 3 iiop listeners:
	// one for non ssl, one for ssl and third for ssl mutual auth
	
        IiopListener[][] listeners = new IiopListener[serverRefs.length][3];  //SHEETAL can there be multiple SSL or 
                                                                         //SSL_MUTH_AUTH ports? bug 6321813
	for (int i = 0; i < serverRefs.length; i++) {
	    Server server = 
		ServerHelper.getServerByName(configCtx, serverRefs[i].getRef());
	    String configRef = server.getConfigRef();
	    Config config =
		ConfigAPIHelper.getConfigByName(configCtx, configRef);
	    IiopService iiopService = config.getIiopService();
	    listeners[i] = iiopService.getIiopListener();
	}
	return listeners;
    }

    /**
     * Returns ip address from node agent refered from instance
     * or null if Exception
     *
     * @author  sridatta.viswanath@sun.com
     */
    public static String getHostNameForServerInstance(String serverName) 
    {
        try {
            JMXConnectorConfig info = 
		ServerHelper.getJMXConnectorInfo(configCtx, serverName);
            _logger.log(Level.FINE, 
			baseMsg + ".getHostNameForServerInstance: " +
			"found info: " + info.toString());
	    String host = info.getHost();
            _logger.log(Level.FINE, 
			baseMsg + ".getHostNameForServerInstance: " +
			"found host: " + host);
            return host;
        } catch (Throwable e){
            _logger.log(Level.FINE, 
			baseMsg + ".getHostNameForServerInstance: " +
			"gotException: " + e + " " + e.getMessage() +
			"; returning null");
            return null;
        }
    }

    /**
     * Gets the correct resolved value for the specific instance
     * Without this routine, config context resolves the value
     * to the current running instance
     *
     * @author  sridatta.viswanath@sun.com
     */
    public static String getResolvedPort(IiopListener l, 
					  String server) 
	throws ConfigException 
    {
	String rawPort = l.getRawAttributeValue("port");
	PropertyResolver pr = new PropertyResolver(configCtx, server);
	return pr.resolve(rawPort);
    }
}
