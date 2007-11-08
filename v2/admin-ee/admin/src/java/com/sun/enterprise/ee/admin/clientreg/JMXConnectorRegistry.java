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
 * JMXConnectorRegistry.java
 *
 * Created on September 12, 2003, 3:46 PM
 */
package com.sun.enterprise.ee.admin.clientreg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.ConnectException;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;
import javax.naming.ConfigurationException;
/** 
 * @author  kebbs
 *
 * The JMXConnectorRegistry maintains a list of MBeanServerConnections to either a 
 * named node agent or server instance. In addition to maintaining MBeanServerConnections,
 * it also maintains an arbitrary data Object which can hold state information set
 * up at connection establishment time. The MBean server connection information 
 * (e.g. protocol, host, port) is taken from a <jmx-connector> element in domain.xml.
 * The abstract method findSystemConnector is used to lookup this information.
 */ 
public abstract class JMXConnectorRegistry {               
    
    //This is the minimum amount of time we will wait before re-connecting to 
    //a downed server. In other words, if a connection request to a server fails
    //at time t and another connection request comes in before 
    //t + TIME_BETWEEN_RECONNECTS_IN_MS, the second connection request will be ignored.
    //A value of <= 0 disables this functionality. There is an issue here as this impacts 
    //quicklooke test behavior.
    private static final int TIME_BETWEEN_RECONNECTS_IN_MS = 0; //0 implies disabled.
    
    static final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
   
    //For enabling System.out tracing
    private static final boolean DEBUG = Boolean.getBoolean("Debug");
    
    //Maps either a node agent or instance name to a ConnectionInfo object.
    private HashMap _registry = new HashMap();
    
    private static Logger _logger = null;                
    
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
    
    /** Creates a new instance of NodeAgentRegistry */
    protected JMXConnectorRegistry() {
    }
    
    /**
     * Fetches the <jmx-connector> element for the given name (either a node agent or 
     * server instance name). The connectionInfo is passed in so that its data can 
     * be altered (using setData()). The connectionInfo's MBeanServerConnection will be 
     * null, should not be set, and if set will be overwritten.
     */
    protected abstract MBeanServerConnectionInfo findConnectionInfo(String name) 
        throws AgentException;
    
    /**
     * Utility method to determine whether the MBean server is reachable.
     * @param ex the exception to be examined
     * @return true if the exception indicates that the server is unreachable (i.e.
     * not lisenting for connections).
     */
    public static boolean isMBeanServerUnreachable(Throwable ex) {
        Throwable t = ex;        
        while (t != null) {            
            if (t instanceof ConnectException) {                
                //Cannot connect
                return true;
            } else if (t instanceof InstanceNotFoundException) {
                //connected, but JMX object was not found. This exception occurs when
                //we have two instances on the same machine using the same conflicting port
                getLogger().log(Level.WARNING, "registry.potentialPortConflict", ex.toString());                    
                return true;
            }
            else if(isUnknownHostException(t)) {
                return true;
            }
                
            t = t.getCause();
        }
        return false;
    }
    
    /** WBN Aug 2007 
     * Another possibility for a server being unreachable is that RMI can't find the host
     */
    private static boolean isUnknownHostException(Throwable t) {
        if(t instanceof ConfigurationException) {
            ConfigurationException cex = (ConfigurationException) t;
            Throwable root = cex.getCause();
            
            if(root != null) {
                if(root instanceof java.net.UnknownHostException)
                    return true;
                if(root instanceof java.rmi.UnknownHostException)
                    return true;
            }
        }
        return false;
    }
        
    /**
     * Removes the connection from the cache, used for unbind function and the connection
     * retry mechanism. If the connection does not exist, then no exception will be rasied.
     * @param name The server instance or node agent name
     * @throws IOException
     */
    protected void removeConnectorFromCache(String name) throws IOException {
        disconnectCachedConnector(name);
        _registry.remove(name);
    }         
    
    /**
     * Disconnects a connected entry in the cache.
     * @param name
     * @throws IOException
     */    
    protected void disconnectCachedConnector(String name) throws IOException {
        MBeanServerConnectionInfo connectionInfo = (MBeanServerConnectionInfo)_registry.get(name);
        Object monitor = connectionInfo;
        if (monitor == null) {
            monitor = _registry;
        }
        synchronized (monitor) {
            if (connectionInfo != null && connectionInfo.isConnected()){            
                JMXConnector conn = connectionInfo.getJMXConnector();
                if (conn != null) {
                    conn.close();
                }
                connectionInfo.setJMXConnector(null);
            }
        }
    }
    
    /**
     * Connect to the server instance or node agents mbean server. We avoid connecting too 
     * often as a performance optimization. The cache is update with the new connection 
     * and the new connection is returned.
     * @param connectionInfo connection info parameters
     * @param name server instance or node agent name
     * @throws AgentException
     * @throws IOException
     * @return
     */    
    private MBeanServerConnectionInfo connect(MBeanServerConnectionInfo connectionInfo, 
        String name) throws AgentException, IOException
    {
        long currentTime = 0;
        if (TIME_BETWEEN_RECONNECTS_IN_MS > 0) {
            currentTime = System.currentTimeMillis();
        }
        
        if (connectionInfo == null) {
            //First time we have attempted to connect.
            connectionInfo = findConnectionInfo(name);   
            connectionInfo.setLastConnectTime(currentTime);
            _registry.put(name, connectionInfo);
            connectionInfo.setJMXConnector(connect(connectionInfo));                                           
        } else {
            //If we have already connected once, then we do not want to 
            //attempt a reconnect "too often", instead we only reconnect
            //if at least TIME_BETWEEN_RECONNECTS_IN_MS milliseconds has
            //elapsed between the last connection attempt.
            long lastConnectTime = connectionInfo.getLastConnectTime();            
            if (TIME_BETWEEN_RECONNECTS_IN_MS <= 0 || 
                currentTime - lastConnectTime > TIME_BETWEEN_RECONNECTS_IN_MS) 
            {
                if (DEBUG) {
                    System.out.println("attempting to connect to " + name);
                }
                MBeanServerConnectionInfo newConnectionInfo = findConnectionInfo(name);                  
                connectionInfo.setLastConnectTime(currentTime);
                connectionInfo.setJMXConnector(connect(newConnectionInfo));                                    
            } else {
                if (DEBUG) {
                    System.out.println("do not connect to " + name);
                }
            }
        }
        return connectionInfo;
    }
    
    /**
     * Returns true if the connection to the mbean server is valid. The validation check
     * is implemented by invoking getDefaultDomain() on the mbean server connection.
     * @param connectionInfo
     * @param name server instance or node agent name
     * @return true if the connection is established and valid.
     */    
    protected boolean connectionIsValid(MBeanServerConnectionInfo connectionInfo, String name)
    {
        //Validate the connection. Once we get a connection, that has been hanging 
        //around in the cache for a while there are some validation cases to be considered.
        //This may be appropriate for a background thread, or here depending on usage.
        //1) The endpoint may have restarted
        //2) The endpoint may be down
        //3) The endpoint's configuration may have changed. 
        //Upon failed validation, we will probably want to just delete from the 
        //cache and retry the connection.

        // validate the connection by getting the default domain for the server
        if (DEBUG) {
            System.out.println("JMXConnectorRegistry:Reusing connected connection to " + name);
        }
        if (!connectionInfo.isConnected()) {
            return false;
        }

        // See 6270405 -- it is best to do this in the background
        final long timeout = 15000; //15 seconds
        return connectionIsValidTimeout(connectionInfo, name, timeout);
	/*
        try {
            final MBeanServerConnection conn=connectionInfo.getMBeanServerConnection();
            String defaultDomain=conn.getDefaultDomain();
            if (DEBUG) {
                System.out.println("JMXConnectorRegistry:Verified connection by getting the default domain=" + defaultDomain);
            }
            return true;
        } catch (IOException e) { 
            if (DEBUG) {
                System.out.println("JMXConnectorRegistry:Removing cached connection and trying to read in config info again");
            }
            return false;
        }
	*/
    }

    /**
     *
     * @param name The server instance name or node agent name
     * @throws AgentException Indicates that a connection could not be established. The method
     * isMBeanServerUnreachable(ex) can be called to determine whether the exection thrown
     * indicates that the server is unreachable (i.e. not listening for connections).
     * @return A valid (i.e. connected) connection to the givem instance
     */    
    protected MBeanServerConnection getConnection(String name)
        throws AgentException
    {
        try {
            //Check to see if a connection already exists in the cache
            MBeanServerConnectionInfo connectionInfo = (MBeanServerConnectionInfo)_registry.get(name);
            Object monitor = connectionInfo;
            if (monitor == null) {
                monitor = _registry;
            }
            synchronized (monitor) {
                if (connectionInfo == null) {
                    //This is the first time that we are establishing a connection
                    if (DEBUG) {
                        System.out.println("JMXConnectorRegistry:Establishing new connection to " + name);
                    }                          
                    connectionInfo = connect(connectionInfo, name);
                } else if (!connectionInfo.isConnected()) {
                    //We have attempted to establish a connection, but did not succeed
                    if (DEBUG) {
                        System.out.println("JMXConnectorRegistry:Reusing unconnected connection to " + name);
                    }
                    connectionInfo = connect(connectionInfo, name);
                } else if (!connectionIsValid(connectionInfo, name)) {  
                    //We have a connection which was once estabilised, but is no longer valid 
                    //(e.g. because the server instance or node agent is no longer running, 
                    //was restarted, etc.)
                    if (DEBUG) {
                        System.out.println("JMXConnectorRegistry:Reusing invalid connection to " + name);
                    }
                    connectionInfo.setJMXConnector(null);
                    connectionInfo = connect(connectionInfo, name);                
                }       
                //If we did not connect, we need to raise an exception to indicate this fact; 
                //otherwise connectionInfo will be null.
                if (!connectionInfo.isConnected()) {
                    throw new ConnectException(TIME_BETWEEN_RECONNECTS_IN_MS + 
                        " ms have not elapsed since last connection attempt to " + name);
                }
                if (DEBUG) {
                    System.out.println("Connection to " + name + " succeeded");
                }
                return connectionInfo.getMBeanServerConnection();   
            }
        } catch (AgentException ex) {
            if (DEBUG) {
                System.out.println("Connection to " + name + " failed " + ex);
            }
            throw ex;
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println("Connection to " + name + " failed " + ex);
            }
            throw new AgentException(ex);
        }
    }
            
    
    /**
     * Establishes a connection to the mbean server.
     * @param connectionInfo connection info parameters
     * @throws IOException if connection fails for any reason
     * @return established connection to the mbean server
     */
    protected JMXConnector connect(MBeanServerConnectionInfo connectionInfo) throws IOException 
    {                
        //final JMXServiceURL url = new JMXServiceURL("service:jmx:" + connectionInfo.getProtocol() + 
        //    "://" + connectionInfo.getHost() + ":" + connectionInfo.getPort());
        //hardcoding RMI for now
        JMXServiceURL url = null;
        try {
            // TODO: remove the hardcoding
            url = JmxServiceUrlFactory.forRmiWithJndiInAppserver(connectionInfo.getHost(),
            Integer.parseInt(connectionInfo.getPort()));
        }
        catch (final NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
        if (DEBUG) {
            System.out.println("Attempting to connect to mbean server at " + url);
        }
        return connectToServer(url, connectionInfo.getUser(), connectionInfo.getPassword());         
    }
    
    
    /**
     * Establishes a connection to the mbean server.
     * @param url JMX service url
     * @param user admin user
     * @param password admin password password
     * @throws IOException upon connection failure
     * @return established connection
     */    
    public static JMXConnector connectToServer(JMXServiceURL url, 
        String user, String password) throws IOException
    {
        final Map env = new  HashMap();        
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(JMXConnector.CREDENTIALS, new String[] {user, password});
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user);
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME, DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        JMXConnector conn = JMXConnectorFactory.connect(url, env); 
        return conn;        
    }
    
    
    /**
     * Establishes a connection to the mbean server.
     * @param url JMX service url
     * @param user admin user
     * @param password admin password password
     * @throws IOException upon connection failure
     * @return established connection
     */    
    public static MBeanServerConnection connect(JMXServiceURL url, 
        String user, String password) throws IOException 
    {            
        JMXConnector conn=connectToServer(url, user, password);
        return conn.getMBeanServerConnection();
    }

     /** Determines in a background thread, if the given connection (which is mostly cached)
         valid one or not. The calling thread joins the background thread after given timeout
         in milliseconds. Note: This method is written such that if it were to fail, it will
         fail safely which means it will report that given connection is invalid when the timeout
         is reached. This should result in establishing a fresh connection. It is OK to determine
         rather prematurely that a valid cached connection is invalid rather than the other
         way round. This method uses rather rudimentary way of 2-thread communication, but
         it will work in case of its use in the limited scope.
         @return true if the connection is valid i.e. a remote MBeanServerConnection method
                 could be called without any exception, false otherwise. Never throws an Exception.
     */
     private static boolean connectionIsValidTimeout(final MBeanServerConnectionInfo connectionInfo, final String name, final long tom) {
         boolean valid = false;
         try {
             final RudimentaryThreadState ts = new RudimentaryThreadState();
             final Thread cvt = createTask("ConnectionValidatorTask: " + name, connectionInfo, ts);
             cvt.start();
             cvt.join(tom);
             //now analyze what happened in the thread.
             if (cvt.isAlive()) {
             // thread is alive, valid must be false, leave alone.
               try {
                  logFineValidMessage(connectionInfo, tom);
                  cvt.interrupt();
               } catch(final Exception ee) {
                   //thread could not be interrupted, no worries.

               }
             } else {
                 if (ts.isRunSuccessful()) {
                     valid = true;
                 } // else, valid remains false;
             }
         }
         catch(final Exception e) {
             e.printStackTrace();
             valid = false; //redundant, but not wrong
         }
         return valid;
     }

     private static class RudimentaryThreadState {
         private boolean runMethodRanSuccessfully;
         RudimentaryThreadState() {
             runMethodRanSuccessfully = true;
         }
         void runMethodThrewException() {
           runMethodRanSuccessfully = false;
         }
         boolean isRunSuccessful() {
             return runMethodRanSuccessfully;
         }
     }

     private static class CVTask extends Thread {
            private MBeanServerConnectionInfo ci;
         private final RudimentaryThreadState ts;
            CVTask(final String name, final MBeanServerConnectionInfo ci, RudimentaryThreadState ts) {
                super(name);
                this.ci = ci;
                this.ts = ts;
            }
            public void run() {
                try {
                    //cinfo may not be null
                    ci.getMBeanServerConnection().getDefaultDomain();
                 } catch(final Exception e) {
                    //e.printStackTrace();
                    ts.runMethodThrewException();
                 }
            }
     }

     private static Thread createTask(final String name, final MBeanServerConnectionInfo ci,
         final RudimentaryThreadState ts) {
         return new CVTask(name, ci, ts);
     }

     private static void logFineValidMessage(final MBeanServerConnectionInfo ci, long tom) {
         final Logger lg = Logger.getLogger(com.sun.enterprise.admin.common.constant.AdminConstants.kLoggerName);
         if (lg.isLoggable(Level.FINE)) {
                final String msg = "Cached Connection: " + ci + " times out after: " + tom + 
				   " milliseconds and it is not valid anymore. This should not " + 
				   "be a problem as an attempt will be made to establish a new connection";
                lg.fine(msg);
         }
     }
}
