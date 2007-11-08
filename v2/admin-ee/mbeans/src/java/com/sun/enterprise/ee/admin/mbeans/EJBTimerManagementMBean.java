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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.EjbTimerEvent;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;  

import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.Properties;

/**
 * object name for this mbean: <domainName>:type=ejb-timer-management,category=config
 * EJBTimerManagementMBean provide functionality to list timers and migrate timers.
 *
 * @author sridatta
 *
 */
public class EJBTimerManagementMBean 
        extends EEBaseConfigMBean 
        // extends com.sun.enterprise.admin.mbeans.EJBTimerManagementMBean 
        implements com.sun.enterprise.ee.admin.mbeanapi.EJBTimerManagementMBean
{
   
	public EJBTimerManagementMBean()
	{
	    super();
	}		
    
    private Logger getLogger()
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

	///////////////////////////////////////////////////////////////////////////
	
	/**
         * migrateTimers method is used to migrate ejb timers associated with 
         * a server (that has probably stopped or failed abnormally) to another server
         *
         * @param sourceServerName The name of the server from where ejbTimers
         *                         need to be migrated from. sourceServerName has 
         *                         to be a server which is part of a cluster. 
         * 			   Otherwise an ConfigException is thrown. 
         *
         * @ param destServerName The name of server to which the ejbTimers
         *                        need to be migrated to. destServerName can
         *                        be null. If destServerName is null, one alive 
         *                        server is picked randomly from the cluster 
         *                        that it belongs to. If the migration fails for 
         *                        that server, error is returned to users and 
         *                        migration is NOT initialized on other servers. 
         *
         * @exception ConfigException is thrown when there are no servers to
         *                            migrate to or the sourceServer is not part 
         *                            of a cluster or if sourceServer is alive
         *
         * @exception InstanceException is thrown when there is an error during
         *                              migration.
         */
    public void migrateTimers(String sourceServerName, String destServerName)
            throws InstanceException, ConfigException {

	fine("Entering migrate Timers"); 
        validateSourceServer(sourceServerName); //throws exception if invalid
        
        if(destServerName == null || "".equals(destServerName)) {
            destServerName = getOneAliveSiblingServer(sourceServerName);
        } else {
            validateDestServer(sourceServerName, destServerName);
        }
        
	fine("Migrate Timers: Sending Notification for " + sourceServerName + " to " + destServerName); 
        sendTimerMigrateEvent(sourceServerName, destServerName);
    }
    
    /**
     * Lists ALL the ejb timers owned by the target--a standalone 
     * server instance or an instance in a cluster or a cluster.
     *
     * @param target. If target is a server instance then the all timers
     *                owned by that instance will be listed.
     *
     *                If the target is a cluster, then all timers owned by 
     *                each instance in the cluster will be listed.
     *
     * exception ConfigException 
     * exception InstanceException 
     *
     * @return returns the list of timers as a string array.
     * 
     */
    public String[] listTimers(String target) 
		throws ConfigException, InstanceException {
       
	fine("Entering list Timers"); 
	String server = null; 
	String[] allServers = null;
        if(isCluster(target)) {
            server = getOneAliveServer(target);
	    allServers = getServersInCluster(target);
        } else if (isServer(target)) {
            assertServerAliveForListTimer(target);
	    server = target;
	    allServers = new String[] { target};
        } else {
            throw new ConfigException(
                _strMgr.getString("notAValidTargetForListTimers",
                                target));
        }
        
	fine("List Timers: Sending Notification to " + server + " for :" + arrayToString(allServers));
	 
        String[] resStr = sendListTimerEvent(server, allServers);
	fine("RESULT: " + resStr);

	String[] ret = null;
	if(resStr == null || resStr.length == 0) {
	   ret = new String[] {"There are no Ejb Timers."};
	}

	if(resStr.length == allServers.length) {
	    ret = new String[resStr.length];

	    for(int i=0; i< resStr.length; i++) {
		ret[i] = allServers[i] + ": " + resStr[i];
	    }
	}
	return ret;
    }
    
    	///////////////////////////////////////////////////////////////////////////

    private boolean isCluster(String target) throws ConfigException {
        return ClusterHelper.isACluster(getConfigContext(), target);
    }
    
    private boolean isServer(String target) throws ConfigException {
        return ServerHelper.isAServer(getConfigContext(), target);
    }
    
    private void assertServerAliveForListTimer(String server) throws ConfigException, InstanceException {
                if (!isServerRunning(server)) {
                    throw new ConfigException(
                        _strMgr.getString("serverNotAliveForTimerList", server));
                }                
    }
   
    /**
     * This method will call a utility method instead of coding the logic here. TBD
     */
    private boolean isServerRunning(String server) throws InstanceException {
	try {
            ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(server);
                Status status = serverMBean.getRuntimeStatus().getStatus();
                if (status.getStatusCode() == Status.kInstanceRunningCode) {
                    return true;
                }
	} catch(Exception e) {
		//return false; //FIXME: what else to do??
	}
                return false;
    }
   
 
    /**
     * This method will call a utility method instead of coding the logic here. TBD
     */
    private boolean isServerDead(String server) throws InstanceException {
	try {
           ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(server);
                Status status = serverMBean.getRuntimeStatus().getStatus();
                if (status.getStatusCode()  == Status.kInstanceNotRunningCode) {
                    return true;
                }
	} catch(Exception e) {
		return true; //is this true?? //FIXME
	}
                return false;
    }

   
    private void assertSourceDead(String server) throws ConfigException, InstanceException {
        if (!isServerDead(server)) {
            throw new ConfigException(
                        _strMgr.getString("sourceServerAliveForMigrateTimers",
                                            server));
        }
    }
     
    private void assertDestServerAlive(String server) throws ConfigException, InstanceException {
        if (!isServerRunning(server)) {
            throw new ConfigException(
                        _strMgr.getString("destServerNotAliveForMigrateTimers",
                                            server));
        }
    }
   
    private void validateSourceServer(String server) 
                        throws ConfigException, InstanceException {
        assertSourceServerNotNull(server);
        assertSourceDead(server); //throws ConfigException if Alive
    }
    
    private void validateDestServer(String source, String dest) 
                        throws ConfigException, InstanceException {
        assertClusterMembership(source, dest);
        assertDestServerAlive(dest); //throws ConfigException if Alive
    }
    
    private String getOneAliveServer(String cluster) 
                throws ConfigException, InstanceException {
                    
         String[] servers = getServersInCluster(cluster);
          
            if (servers != null && servers.length > 0) {
                for(int i = 0; i < servers.length; i++) {
                    String name = servers[i];
                    if(isServerRunning(name)) {
                        return name;
                    }
                }
            }     
          
          throw new ConfigException (
                            _strMgr.getString("noAliveServerInCluster", 
                                              cluster));
    }
     
    private String[] getServersInCluster(String cluster) 
				throws ConfigException {
         final ConfigContext configContext = getConfigContext();
        Server[] servers = 
		ServerHelper.getServersInCluster(configContext, cluster);

	if(servers == null) return null;

	String[] serverNames = new String[servers.length];
        for(int i = 0 ; i < servers.length ; i ++ ) { 
	    serverNames[i] = servers[i].getName();
 	}
	return serverNames;
    }

 
     private void assertSourceServerNotNull(String server)
					throws ConfigException {
	if(server == null || "".equals(server)) {
             throw new ConfigException (
                            _strMgr.getString("sourceServerNull", 
                                              server));
	}
     }

     private void assertNotSame(String server1, String server2) 
							throws ConfigException {

	if(server1 !=null && server1.equals(server2)) {
             throw new ConfigException (
                            _strMgr.getString("sameSourceDest", 
                                              server1, server2));
	}     
     } 

     private void assertClusterMembership(String server1, String server2) 
							throws ConfigException {
         
	 assertNotSame(server1, server2);
	 final ConfigContext configContext = getConfigContext();
        
	
         Cluster cluster1 = 
            ClusterHelper.getClusterForInstance(configContext, server1);
         Cluster cluster2 = 
            ClusterHelper.getClusterForInstance(configContext, server2);
         
         if (cluster1 == null || cluster2 == null ||
                !cluster1.getName().equals(cluster2.getName()) ) {
                    
             throw new ConfigException (
                            _strMgr.getString("serversNotSiblings", 
                                              server1, server2));
         }
     }
     
     private String getOneAliveSiblingServer(String sourceServerName) 
				throws ConfigException, InstanceException {
         Cluster cluster1 = getClusterForInstance(sourceServerName);
         String[] servers = getServersInCluster(cluster1.getName());
          
         if (servers!= null && servers.length > 0) {
             for(int i = 0; i < servers.length; i++) {
                 String name = servers[i];
                 if(!name.equals(sourceServerName) && 
                             isServerRunning(name)) {
                     return name;
                 }
             }
         }     
          
          throw new ConfigException (
                            _strMgr.getString("noAliveSibling", 
                                              cluster1.getName(), 
                                              sourceServerName));         
     }

    private Cluster getClusterForInstance(String server)
				     throws ConfigException {
         final ConfigContext configContext = getConfigContext();
         return ClusterHelper.getClusterForInstance(configContext, server);
    }
   
     private void sendTimerMigrateEvent(String sourceServerName, 
                                        String destServerName)
                                                throws InstanceException {
            
         AdminEvent event = new EjbTimerEvent(destServerName, 
                        EjbTimerEvent.ACTION_MIGRATETIMER, 
                        sourceServerName, 
                        new String[] {destServerName});
        
	 event.setTargetDestination(destServerName); 
         forwardEvent(event);
     }
     
     private String[] sendListTimerEvent(String server, String[] allServers) {
         AdminEvent event = new EjbTimerEvent(server, 
                        EjbTimerEvent.ACTION_LISTTIMERS, 
                        server, 
                        allServers);
         
	 event.setTargetDestination(server); 
         AdminEventResult res = forwardEvent(event);

	return (String[]) res.getAttribute(server,
			EjbTimerEvent.EJB_TIMER_CALL_RESULT_ATTRNAME);
     }
      

     private AdminEventResult forwardEvent(AdminEvent e) {
        AdminEventResult result = null;
        result = AdminEventMulticaster.multicastEvent(e);
        return result;
    }
    
     private void fine(String s) {
         getLogger().log(Level.FINE, s); 
     }

    private String arrayToString(String[] s) {
	if(s==null) return null;
	String res = "";
	for(int i = 0; i < s.length; i++) {
	    if(res != "") res += ",";
	    res += s[i];
	}
	return res;
    }
	    
     
	///////////////////////////////////////////////////////////////////////////
	
	private static final	StringManager	_strMgr = 
                StringManager.getManager(EJBTimerManagementMBean.class);
	private static 	        Logger			_logger;
	///////////////////////////////////////////////////////////////////////////
}
