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

package com.sun.enterprise.ee.nodeagent;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.Vector;
import java.io.IOException;
import java.io.File;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.NodeAgentPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.server.logging.FileandSyslogHandler;
import com.sun.enterprise.config.ConfigContext;

public abstract class BaseNodeAgent  {
    
    
    protected static final String MBEAN_SERVER_DOMAIN="com.sun.appserver.nodeagent";
    protected static final int DEFAULT_CONNECTOR_PORT=9900;
    protected int _port=DEFAULT_CONNECTOR_PORT;
    protected static final String JMX_SERVER_MBEAN_PREFIX=":name=JmxConnectorServer,port=";
    protected static final String JMX_SERVER_MBEAN_SEARCH=":name=JmxConnectorServer,*";
    protected static final String RELATIVE_LOCATION_DOMAIN_XML=IAdminConstants.NODEAGENT_DOMAIN_XML_LOCATION;
    protected static final String[] LOCAL_DAS_SYNC_FILES=new String []{RELATIVE_LOCATION_DOMAIN_XML, "/config/.domain.xml.timestamp",
    "/config/domain.xml_save"};
    protected static final String INSTANCE_SYNC_JVM_OPTIONS="INSTANCE-SYNC-JVM-OPTIONS";
    protected Logger _logger=null;
    protected MBeanServer _server=null;
    protected DASPropertyReader _dasReader=null;
    protected NodeAgentPropertyReader _naReader=null;
    
    private static ConfigContext _configCtxt=null;
    protected static boolean _isNodeAgent=false;
    protected static final StringManager _strMgr=StringManager.getManager(NodeAgent.class);
    protected boolean bDebug=Boolean.getBoolean("Debug");
    
    
    //************************************************************************************
    // ABSTRACT METHOD CONTRACT
    //************************************************************************************
    protected abstract void rendezvousWithDAS() throws Exception;
    protected abstract Vector synchronizeWithDASInternal() throws AgentException;
    protected abstract void configureLogger() throws Exception;
    protected abstract void configureAgent() throws Exception;
    
    /**
     * postStartupProcessing - This method is call by the BaseNodeagent after intialization has been completed.
     */
    protected abstract void postStartupProcessing(String startInstancesOverride, boolean syncInstancesOverride) throws Exception;
    
    /**
     * shutdownProcessing - This method is called by the NodeagentMain before the nodeagent is shutdown
     */
    protected abstract void shutdownProcessing() throws Exception;
    
    
    
    /**
     * \ - This method is the intial method that is called to startup the node agent and get it intialized.
     * This method is currently only called from the NodeAgentMain class.
     */
    public void run(String startInstancesOverride, boolean syncInstancesOverride) throws Exception {
        // look for Debug system property
        if (System.getProperty("Debug") != null) {
            // turn on debug, this option was added to help developers
            // debug the their code what adding/modifying tasks that are executed via
            // the ProcessLauncher
            bDebug=true;
        }
        try {
            
            // need to get local log levels, to log rendezvous messages
            configureLogger();
            // Check to see if the NodeAgent deleted, is do log message and don't start
            if (getNodeAgentPropertyReader().isDeleted()) {
                // log message
                getLogger().log(Level.WARNING, "nodeagent.isDeleted");
                stopAgentWithPause();
            }
            
            // if unbound rendesvous with DAS
            if (getDASPropertyReader().getHost() != null && !getNodeAgentPropertyReader().isBound()) {
                try {
                    rendezvousWithDAS();
                } catch (Exception e) {
                    // rendezvous error already logged
                    stopAgentWithPause();
                }
            }
            
            Vector createdInstances=null;
            // try to synchronize with das at startup if bound
            if (getNodeAgentPropertyReader().isBound()) {
                // need to stop on error if the nodeagent just rendezvoused and a
                // domain.xml doesn't exist ???
                createdInstances=synchronizeWithDASInternal();
            } else {
                // should not be at this point and not bound, error already printed
                stopAgentWithPause();
            }
            
            // call post processing method for tasks like starting up all managed servers on nodeagent startup
            postStartupProcessing(startInstancesOverride, syncInstancesOverride);
        } catch (Exception ex) {
            // should present a properly localized message ???
            //ex.printStackTrace();
            throw(ex);
        }
    }
    
    /**
     * init - This Method initializes both the agent's Logger and the agent's jmx connectors
     */
    protected void init() throws AgentException {
        getLogger().log(Level.FINE, "Initializing NodeAgent...");
        try {
            
            // if in unbound state and waiting for das to rendezvous then must create new log and log level and remove old one ???
            //configureLogger();
            
            // if in unbound state and waiting for das to rendezvous then must create new JMXConnector and remove old one ???
            // create MBeanServer & JMX connectors for secure and non-secure ports in necessary also register mbeans
            configureAgent();
            
        } catch (AgentException ae) {
            throw ae;
            
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "nodeagent.initialization.Exception", e);
            throw new AgentException(e);
        }
        
    }
    
    /**
     * registerLogger - performs the actual registration of the file to be used as a logger
     * @param level - Level to log at
     * @param file - relative path of the log file which will be concatenated with com.sun.ass.instanceRoot
     * @throws IOException
     */
    protected void registerLogger(Level level, String file) throws IOException {
        // register default logger
        if (bDebug) {
            getLogger().setLevel(Level.FINEST);
        } else {
            getLogger().setLevel(level);
        }
        
        /* currently the FileandSyslogHandle has a bug that need to be fix so the log can be moved and rename
         * when this is fix this code will be changes ???
         */
        
        //FileHandler fh = new FileHandler(System.getProperty("com.sun.aas.instanceRoot") + file, true);
        // set to system formatter
        //fh.setFormatter( new UniformLogFormatter( ) );
        
        // Send logger output to our FileHandler.
        //FileandSyslogHandler fh=new FileandSyslogHandler(System.getProperty("com.sun.aas.instanceRoot") + file);
        //getLogger().addHandler(fh);
    }
    
    
    /**
     * isPortRegistered - checks to see if the port in registered in the MBean server which is represened
     * in the vector of ObjectNames
     * @param vtCurrent - The Vector of ObjectNames that are registered in the MBeanServer
     * @param port - The port that is being checked to see if it exists
     * @return boolean - Returns true if the port exists in the vector
     */
    protected boolean isPortRegistered(Vector vtCurrent, String port) throws Exception {
        boolean bRet=false;
        // see if it connector's port is already exposed
        if (bDebug) {
            System.out.println("Looking for :" + _server.getDefaultDomain() + JMX_SERVER_MBEAN_PREFIX + port);
            for(int ii=0; ii < vtCurrent.size(); ii++) {
                System.out.println("\t" + (ObjectName)vtCurrent.get(ii));
            }
        }
        ObjectName nameSearch=new ObjectName(_server.getDefaultDomain() + JMX_SERVER_MBEAN_PREFIX + port);
        if (vtCurrent.contains(nameSearch)) {
            bRet=true;
        }
        // remove it each time, because calling this method means that the port should exist
        // and will either be created or left alone
        vtCurrent.remove(nameSearch);
        return bRet;
    }
    
    
    protected void query() {
        try {
            final ObjectName nameSearch=new ObjectName(_server.getDefaultDomain() + ":*");
            final java.util.Set ls=_server.queryNames(nameSearch, null);
            final java.util.Iterator it=ls.iterator();
            ObjectName objectName=null;
            String currPort=null;
            while(it.hasNext()) {
                objectName=(ObjectName)it.next();
                System.out.println("*** currently registered connectors: " + objectName);
            }
        } 
        catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * registerMBean - This method facilitates the registering of the mbeans in the MBeanServer
     *
     * @param mbean - The MBean to reigster
     * @param type - The object name of the MBean without the domain portion
     * @throws Exception
     */
    protected void registerMBean(Object mbean, String type) throws Exception {
        ObjectName objname = new ObjectName(_server.getDefaultDomain() +  type);
        _server.registerMBean(mbean, objname);
        getLogger().log(Level.CONFIG, "registered object: " + objname);
    }
    
    /**
     * setNodeAgentBindStatus - This method sets and persists the nodeagent status
     * @param status - Statuses are: "UNBOUND" - Nodeagent has yet to rendevous with DAS,
     * "BOUND" - Nodeagent has rendezvous with DAS & "DELETED" - After rendezvous the nodeagent
     * has been deleted from the configuration and can be restarted.
     *
     * @throws IOException
     */
    protected void setNodeAgentBindStatus(String status) throws IOException {
        // set property and persist it
        if (bDebug) System.out.println("Senting Node Agent Status to " + status);
        getLogger().log(Level.FINE, "Setting NodeAgent Status to:" + status);
        getNodeAgentPropertyReader().setBindStatus(status);
        getNodeAgentPropertyReader().write();
    }
    
    
    /**
     * Method getLogger
     *
     * @return Logger - logger for the NodeAgent
     */
    public Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER, "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
            if (bDebug) {
                _logger.setLevel(Level.FINEST);
            } else {
                _logger.setLevel(Level.CONFIG);
            }
        }
        return _logger;
    }
    
    
    /**
     * setDASPropertyReader - Set current DASPropertiesReader
     *
     * @param dasReader
     */
    protected void setDASPropertyReader(DASPropertyReader dasReader) {
        _dasReader=dasReader;
    }
    
    /**
     * getDASPropertyReader
     * @return  DASPropertyReader
     */
    protected DASPropertyReader getDASPropertyReader() {
        return _dasReader;
    }
    
    /**
     * @param naReader  */
    protected void setNodeAgentPropertyReader(NodeAgentPropertyReader naReader) {
        _naReader=naReader;
    }
    
    /**
     * NodeAgentPropertyReader - Set current NodeAgentPropertyReader
     *
     * @param NodeAgentPropertyReader
     */
    protected NodeAgentPropertyReader getNodeAgentPropertyReader() {
        return _naReader;
    }
    
    
    protected void setConfigContext(ConfigContext configContext) {
        _configCtxt=configContext;
    }
    
    /**
     * getConfigContext - return config context
     * @return configContext to domain.xml, used in security initialization, could be null
     */
    protected static ConfigContext getConfigContext() {
        return _configCtxt;
    }
    
    
    protected void stopAgentWithPause() {
        try {
            // need to wait so the PEInstancesManager can see the change in status from
            // starting to notrunning. If you don't sleep, the asadmin command holds for the full
            // time out of the waitUntilStarting method (3 minutes)
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {}
        
        getLogger().log(Level.WARNING, "nodeAgent.stopping.agent");
        // stop process from continuing, but return a 1, so start infratructure
        // doesn't look for starting state
        System.exit(0);
    }
    
}
