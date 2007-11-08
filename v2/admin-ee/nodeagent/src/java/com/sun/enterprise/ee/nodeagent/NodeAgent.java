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

import com.sun.enterprise.util.system.GFSystem;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.Vector;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.File;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.security.cert.CertificateNotYetValidException;

import javax.management.MBeanException;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.AttributeList;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;

import com.sun.appserv.management.client.RedirectException;
import com.sun.appserv.management.client.AdminRMISSLClientSocketFactory;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxConnectorServerDriver;
import com.sun.enterprise.admin.jmx.remote.server.rmi.RemoteJmxProtocol;
import com.sun.enterprise.admin.server.core.jmx.auth.ASJMXAuthenticator;
import com.sun.enterprise.admin.server.core.jmx.auth.ASLoginDriverImpl;
import com.sun.enterprise.admin.pluggable.ClientPluggableFeatureFactory;
import com.sun.enterprise.admin.pluggable.ClientPluggableFeatureFactoryImpl;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.server.core.jmx.ssl.AdminSslServerSocketFactory;
import com.sun.enterprise.admin.server.core.jmx.nonssl.RMIMultiHomedServerSocketFactory;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.admin.jmx.remote.https.NoCertCheckX509TrustManager;
import com.sun.enterprise.admin.server.core.jmx.ssl.NodeAgentSyncWithDAS_TlsClientEnvSetter;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.ee.admin.concurrent.Task;
import com.sun.enterprise.ee.admin.concurrent.Executor;    
import com.sun.enterprise.ee.admin.configbeans.PortConflictCheckerConfigBean;
import com.sun.enterprise.ee.admin.configbeans.NodeAgentsConfigBean;
import com.sun.enterprise.ee.admin.servermgmt.*;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.NodeAgentPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.ee.admin.servermgmt.AgentManager;
import com.sun.enterprise.ee.admin.servermgmt.EEScriptsTokens;
import com.sun.enterprise.ee.synchronization.DASCommunicationException;
import com.sun.enterprise.ee.synchronization.NonMatchingDASContactedException;
import com.sun.enterprise.ee.synchronization.SynchronizationDriver;
import com.sun.enterprise.ee.synchronization.SynchronizationDriverFactory;
import com.sun.enterprise.ee.synchronization.http.HttpUtils;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.naming.SerialInitContextFactory;
import com.sun.enterprise.server.logging.LogMBean;
import com.sun.enterprise.server.logging.SystemOutandErrHandler;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.RealmConfig;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.io.StreamFlusher;
import com.sun.enterprise.util.SystemPropertyConstants;

import static com.sun.enterprise.util.SystemPropertyConstants.AGENT_CERT_NICKNAME;
import static com.sun.enterprise.util.SystemPropertyConstants.AGENT_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.AGENT_NAME_PROPERTY; 
import static com.sun.enterprise.util.SystemPropertyConstants.CLUSTER_NAME;
import static com.sun.enterprise.util.SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JKS_KEYSTORE;
import static com.sun.enterprise.util.SystemPropertyConstants.JKS_TRUSTSTORE;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.NSS_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.NSS_DB_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.NSS_DB_PASSWORD_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.SERVER_NAME;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_PROPERTY;

/**
 * This class is run as part of a standalone application that is instantiated by 
 * NodeAgentMain. Its primary purpose is to keep the instances that belong to 
 * the NodeAgent, and the NodeAgent itself, in-sync with the Domain Admin Server 
 * (DAS). The NodeAgent also facilites creating, deleting and the starting of 
 * instances that belongs to it. The NodeAgent uses JMX Connectors for 
 * communication to and from the DAS and uses the internal RMI Admin Channel to 
 * check the state of the instances' that currently exist. 
 */
public class NodeAgent extends BaseNodeAgent implements 
    NodeAgentMBean, IAdminConstants {
    
    private static final String 
        DOMAIN_XML_DOMAIN_NAME_PROPERTY_NAME = "administrative.domain.name";
                
    protected static final String 
        MBEAN_SERVER_DOMAIN = "com.sun.appserver.nodeagent";       
        
    private AgentConfig _agentConfig;      
        
    public NodeAgent() throws Exception {        
        this(new DASPropertyReader(new AgentConfig()),
             new NodeAgentPropertyReader(new AgentConfig()), new AgentConfig());
    }

    public NodeAgent(AgentConfig agentConfig) throws Exception {
        this(new DASPropertyReader(agentConfig), 
             new NodeAgentPropertyReader(agentConfig), agentConfig);
        // now reset InstanceDirs properly.
        // Note this is a create-node-agent call.  We are running in DAS, not NA
        InstanceDirs.initialize(agentConfig);
    }          

    public NodeAgent(
        DASPropertyReader dasReader, NodeAgentPropertyReader naReader)
    throws Exception {
        this(dasReader, naReader, new AgentConfig());
    }
    
    /** 
     * @param dasReader Reads from das.properties 
     * @param naReader  Reads from nodeagent.properties
     * @throws Exception  
     */
    public NodeAgent(DASPropertyReader dasReader, 
        NodeAgentPropertyReader naReader, AgentConfig agentConfig) 
    throws Exception {
        // we are running inside NA, not DAS, so use the System Properties
        InstanceDirs.initialize( System.getProperty(INSTANCE_ROOT_PROPERTY)); 
        setDASPropertyReader(dasReader);
        setNodeAgentPropertyReader(naReader);
        _agentConfig = agentConfig;
        
        // read in domain.xml from the nodeagent config directory
        String domainXMLLocation = System.getProperty(INSTANCE_ROOT_PROPERTY) 
                                 + RELATIVE_LOCATION_DOMAIN_XML;
        setConfigContext(ConfigFactory.createConfigContext(domainXMLLocation));
    }

    // ********************************************************************
    // ********************* NodeAgentMBean interface *********************
    // ********************************************************************       
        
    /** 
     * This method is used to remotely start an instance
     *
     * @param instanceName      instance to start
     * @throws AgentException   Exception thrown if anything goes wrong 
     *                          with starting the instance
     */    
    public void startInstance(String instanceName) throws AgentException {
   
        // call startInstance with sync option
        startInstance(instanceName, true);

    }
    
    /** 
     * This method is used to remotly stop an instance
     *
     * @param instanceName    instance to stop
     * @throws AgentException Exception thrown if anything goes wrong with 
     *                        stopping the instance
     */    
    public void stopInstance(String instanceName) throws AgentException {
        getLogger().log(Level.INFO, "nodeagent.stoppingInstance", instanceName);                        
            stopInstance(instanceName, false, -1);    
    }

    /** 
     * This method is used to remotly stop an instance forcible
     *
     * @param instanceName    instance to stop
     * @param timeout         timeout before process killed forcibly
     * @throws AgentException Exception thrown if anything goes wrong with 
     *                        stopping the instance
     */    
    public void stopInstance(String instanceName, int timeout) 
    throws AgentException {
        stopInstance(instanceName, true, timeout);
    }

    /** 
     * This method is used to remotly stop an instance forcibly
     *
     * @param instanceName    instance to stop
     * @param force           flag for force kill or normal kill
     * @param timeout         timeout before process killed forcibly
     * @throws AgentException Exception thrown if anything goes wrong with 
     *                        stopping the instance
     */    
    private void stopInstance(String instanceName, boolean force, int timeout)
    throws AgentException {
        getLogger().log(Level.INFO, "nodeagent.stoppingInstance", instanceName);                        
        
        // make sure instance exists to start it
        if (!instanceExists(instanceName)) {
            // instance doesn't exist, this could be caused by a sync error 
            // with the nodeagent so it doesn't know to create the instance 
            // and then someone tries to start or stop it
            throw new AgentException(_strMgr.getString(
                "nodeAgent.instanceDoesNotExist", instanceName));
        }

        checkOnInstanceIsStarted(instanceName);
        
        try {           
            //beginning instance stopping
            InstanceStatus.getInstance().updateStatus(
                instanceName, Status.kInstanceStoppingCode);
            // stop instance and remove it from the ProcessManager
            if (!ProcessManager.getInstance().processExists(instanceName)){
                // create the process so it can be stopped this solves the 
                // case of an instance being started outside the NA, like call 
                // startserv script directly or NA reconvery from failure.
                // watchdogging can not be done, because associated process 
                // can not be recaptured
                ProcessManager.getInstance().addProcessInstance(instanceName, 
                                    new ProcessInstanceInternal(instanceName));
            }

            if (!force)  {
                ProcessManager.getInstance().stopProcess(instanceName);        
            } else {
                ProcessManager.getInstance().stopProcess(instanceName, timeout);
            }
            ProcessManager.getInstance().removeProcessInstance(instanceName);
            //instance successfully stopped
            InstanceStatus.getInstance().updateStatusFromAdminChannel(
                instanceName, Status.kInstanceNotRunningCode);
        } catch (Exception e) {
            //stop failed. We mark the instance running so that the user can 
            //attempt to issue the stop a second time..
            InstanceStatus.getInstance().updateStatusFromAdminChannel(
                instanceName, Status.kInstanceRunningCode);
            ProcessInstance pInstance = 
                ProcessManager.getInstance().getProcess(instanceName);
            if (pInstance !=null) pInstance.setStopping(false);
            StringManagerBase sm = StringManagerBase.getStringManager(
                                   getLogger().getResourceBundleName());  
            getLogger().log(Level.WARNING, sm.getString(
                "nodeagent.stop.instance.exception", instanceName), e);             
            throw new AgentException(e);
        }    
    }
   
    /**
     * Notifies this NodeAgent that a sync of domain.xml is required.  
     * It also initiates a reconcilation with the configuration for the node 
     * described in domain.xml and the current node's configuration. This method 
     * is usually call via a notification from the DAS, but it also can be 
     * called by a third party.
     *
     * @throws AgentException Exception thrown if anything goes wrong with sync 
     *                        and reconciliation
     */    
    public void synchronizeWithDAS() throws AgentException {
        // this methos returns the created instances which is 
        // not needed as part of the exposed api
        synchronizeWithDASInternal(
            SynchronizationDriverFactory.NODE_AGENT_CONFIG_URL);
    }
    
    public void clearRuntimeStatus() {
        RuntimeStatus.clearRuntimeStatus();
    }
            
    public RuntimeStatus getRuntimeStatus() throws AgentException {        
        try {            
            final AgentManager manager = new AgentManager(getNodeAgentConfig());
            return RuntimeStatus.getRuntimeStatus(getNodeAgentName(),
                manager);
        } catch (Exception ex) {
            StringManagerBase sm = StringManagerBase.getStringManager(
                getLogger().getResourceBundleName());  
            getLogger().log(Level.INFO, sm.getString(
                "nodeagent.getStatus.exception", getNodeAgentName()), ex);             
            throw new AgentException(ex); 
        }
    }            

    public void rotateLog() {
        LogMBean.getInstance().rotateNow();
        getLogger().log(Level.INFO, "nodeagent.rotateLog");                        
    }
    
    public String getLogFilesDirectory(String componentName) 
    throws AgentException {
        String logDir="";
        try {
            logDir=getLogFileForConponent(componentName, null);
            // remove default log file from path
            logDir=logDir.substring(0, logDir.lastIndexOf("/"));
            getLogger().log(Level.FINE, "Log directory of component " + 
                                        componentName + " - " + logDir);
        } catch (Exception e) {
            throw new AgentException(e);
        }
        return logDir;
    }
    
    public AttributeList getLogRecordsUsingQuery(String componentName, 
        String logFile, Long fromRecord, Boolean next, Boolean forward,
        Integer requestedCount, Date fromDate, Date toDate, String logLevel, 
        Boolean onlyLevel, List listOfModules, Properties nameValueMap) 
    throws AgentException {
        try {
            logFile=getLogFileForConponent(componentName, logFile);
            getLogger().log(Level.FINE, "Querying logfile  - " + logFile);

            return LogMBean.getInstance().getLogRecordsUsingQuery(logFile, 
                    fromRecord, next, forward, requestedCount, fromDate, toDate, 
                    logLevel, onlyLevel, listOfModules, nameValueMap);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }
    
    public void dasHasMoved(String newHost, String newPort) 
    throws AgentException, MBeanException {
        try {
            if (haveDASCoordinatesChanged(newHost, newPort) == false) 
                return; 
            StringManagerBase sm = StringManagerBase.getStringManager(
                                        getLogger().getResourceBundleName());              
            _logger.log(Level.INFO, "nodeagent.das_has_moved.info", 
                        new Object[] {newHost, newPort});
            writeToDASPropertyReader(newHost, newPort);
            setJMXConnectorInfo(newHost, newPort);
            cleanCachedConnectionToDAS();
        } catch (IOException ex) {
            throw new AgentException(ex);
        } catch (ConfigException ex) {
            throw new AgentException(ex);
        } catch (InstanceException ex) {
            throw new AgentException(ex);
        } 
    }
    
    private void writeToDASPropertyReader(String newHost, String newPort) 
    throws IOException {
        // get appropriate das properties
        DASPropertyReader dasProperty = getDASPropertyReader();
        // something changed for the das connection, update das properties
        dasProperty.setPort(newPort);
        dasProperty.setHost(newHost);
        // logmessage so the user will know to change say /etc/hosts file 
        // so it becomes pingable.
        dasProperty.write();

        getLogger().log(Level.CONFIG, "nodeagent.changeOfDASValues", 
                        new Object[] {newHost + ":" + newPort, newHost} );    
    }

    private void setJMXConnectorInfo(String newHost, String newPort)  
    throws ConfigException {     
        AdminService as = ServerHelper.getAdminServiceForServer(
                            getConfigContext(), "server");
        JmxConnector jmxConn = as.getJmxConnectorByName(
                                as.getSystemJmxConnectorName());
        jmxConn.setPort(newPort);

        ElementProperty clientHostname = 
            jmxConn.getElementPropertyByName(HOST_PROPERTY_NAME);
        clientHostname.setValue(HOST_PROPERTY_NAME, newHost);

        getConfigContext().flush(true);
       
    } 
    
    private void cleanCachedConnectionToDAS() 
    throws ConfigException, InstanceException {
    
        Server das = ServerHelper.getDAS(getConfigContext());
        InstanceRegistry.removeInstanceConnection(das.getName());
        try {
            InstanceRegistry.getDASConnection(getConfigContext());
        } catch (InstanceException ex) {
            // nothing more can be done, next attempt to connect to DAS 
            // hopefully works      
        }
    }
    
    private AgentConfig getNodeAgentConfig() {
        return _agentConfig;
    }

    private void checkOnInstanceIsStarted(String instanceName) 
    throws AgentException {
        // The instance must be stoppped before we proceed. This check is here
        // to prevent the same server being started multiple times.  We must 
        // synchronize on status to guard agains the case when the same instance 
        // is stopped at exactly the same time (and they both attempt to update 
        // the status).
        
        // WBN June 12, 2007
        // this method also ensures that we can never stop an instance
        // that is stuck in the starting state (Zombie).
        // Instead we just go ahead and stop it anyways.
        // Change -- do not throw an Exception if it is not "running"

        boolean wasRunning = InstanceStatus.getInstance().updateStatusIfRunning(
                                instanceName, Status.kInstanceStoppingCode);
        if (!wasRunning) {
            StringManagerBase sm = StringManagerBase.getStringManager(
                getLogger().getResourceBundleName()); 
            String msg = sm.getString(
                "nodeagent.couldNotStopInstance", instanceName, 
                InstanceStatus.getInstance().getStatus(
                    instanceName).getStatus().getStatusString());
            getLogger().log(Level.WARNING, msg);                                       
            //throw new AgentException(msg);
        }      
    }

    private String getNodeAgentName() {
        return getNodeAgentConfig().getRepositoryName();
    }
    
    // ********************* NodeAgent supporting methods******************

    protected String getLogFileForConponent(String componentName, String logFile) 
    throws Exception {
        // see if nodeagent or server that nodeagent manages.
        if (componentName == null || 
            componentName.equals(getNodeAgentConfig().getRepositoryName())) {
            // nodeagent log, should be able to fall through and the 
            // LogMBean work correctly
        } else {
            // make sure instance exists
            if (!instanceExists(componentName)) {
                // instance doesn't exist, this could be caused by a sync error 
                // with the nodeagent so it doesn't know to create the instance 
                // and then someone tries to start or stop it
                throw new AgentException(_strMgr.getString(
                    "nodeAgent.instanceDoesNotExist", componentName));
            }
            // create log prefix
            String logFileX = "";
            String logPrefix = getNodeAgentConfig().getRepositoryRoot() + "/" + 
                               getNodeAgentConfig().getRepositoryName() + "/" + 
                               componentName + "/" ;
            boolean bFound = false;
            Config config = ServerHelper.getConfigForServer(
                    getConfigContext(), componentName);
            if (config != null) {
                LogService logservice=config.getLogService();
                if (logservice != null) {
                    logFileX = logservice.getFile();
                    if (logFileX != null && !logFileX.equals("")) {
                        // set that is was found
                        bFound=true;
                        // see if log file set on the way in
                        if (logFile == null) {
                            // no log file set, use log-service default
                            logFile=logFileX;
                        } else {
                            // log file specified, remove last file, so can 
                            // concatinate logFile that is passed in
                            logFile = logFileX.substring(0, 
                                    logFileX.lastIndexOf("/") + 1) + logFile;
                        }

                        // see if instanceroot used in logfile location 
                        // (currently it is)
                        String instanceRoot = 
                            System.getProperty(INSTANCE_ROOT_PROPERTY);
                        int iPos = logFile.indexOf(instanceRoot);
                        if (iPos == 0) {
                            // remove instance root and build location of log
                            logFile = logPrefix + logFile.substring(
                                                     instanceRoot.length() + 1);
                        }
                    }
                }
            }
            // see if have found a logfile
            if (!bFound) {
                // no log set in domain.xml, but must set a fully qualified 
                // path to instance use defaults
                if (logFile == null) {
                    // use all defaults
                    logFile= logPrefix + "logs/server.log";
                } else {
                    // specific log file set
                    logFile= logPrefix + "logs/" + logFile;
                }
            }
        }
        return logFile;
    }

    /**
     * This method is called to notify NodeAgent that a sync of domain.xml is 
     * required. It also initiates a reconcilation with the configuration for 
     * the node described in domain.xml and the current node's configuration. 
     * This method is usually call via a notification from the DAS, but it 
     * also can be called by a third party.
     *
     * @throws AgentException Exception thrown if anything goes wrong with 
     *                        sync and reconciliation
     */    
    protected Vector synchronizeWithDASInternal() throws AgentException {        
        return synchronizeWithDASInternal(
                SynchronizationDriverFactory.NODE_AGENT_STARTUP_CONFIG_URL);
    }
    
    private Vector synchronizeWithDASInternal(String url) throws AgentException {
        getLogger().log(Level.INFO, "nodeagent.ExecutingSync");
        final DASPropertyReader dasEnv = getDASPropertyReader();
        
        // We want to control initialization ourselves. The issue is that we do 
        // not want initialization (i.e.loading of NSS) to happen implicitly 
        // here or we will not able to synchronize the NSS database on Windows 
        // platforms since it will be in use. For this reason, we do not want 
        // orb initialization in our InitialContextFactory to occur.
        SerialInitContextFactory.setInitializeOrbManager(false);
        
        // Setup as an SSL client
        if (Boolean.valueOf(dasEnv.isDASSecure()).booleanValue()) {
            String certNickname = "s1as";
            /*try {
                if (getConfigContext() != null)
                    certNickname = ServerHelper.getCertNickname(
                        getConfigContext(), DAS_SERVER_NAME);

            } catch(Exception ex) {
                getLogger().log(
                    Level.WARNING, "nodeagent.synchronization.Exception",ex);
                throw new AgentException(ex);
            }*/
            new NodeAgentSyncWithDAS_TlsClientEnvSetter(certNickname).setup();
        }
        Vector createdInstances=null;
        // check to see if bound, check here because this method is in 
        // mbean interface
        if (getNodeAgentPropertyReader().isBound()) {
            // Synchronise with DAS, which will put domain.xml in 
            // config directory
            long startSync=System.currentTimeMillis();
            try {
                try {
                    SynchronizationDriver sd = SynchronizationDriverFactory.
                        getSynchronizationDriver(
                            System.getProperty(INSTANCE_ROOT_PROPERTY), 
                        url, null);
                    sd.synchronize();
		} catch(final DASCommunicationException de) {
                    new CommunicationExceptionLogUtils(getLogger(),
                      Level.SEVERE, dasEnv).handleDASCommunicationException(de);
                } catch (NonMatchingDASContactedException ex) { 
                    stopAgentWithPause();
                } catch (Exception se) {
                    // An exception is now thrown when the das can't be reached
                    // this is an acceptable case for the nodeagent, so just 
                    // log at fine level
                    getLogger().log(Level.FINE,"Synchronization Exception", se);                    
                }

                // check to make sure there is a domain.xml to use, this could 
                // happen as a result of a rendezvous with the das during create 
                // time and a sync never being successfully completed. Like if 
                // the nodeagent is initially started when the das is down
                File domain_xml_file = new File( 
                                    System.getProperty(INSTANCE_ROOT_PROPERTY) + 
                                    RELATIVE_LOCATION_DOMAIN_XML);
                if (!domain_xml_file.exists()) {
                    // domain.xml doesn't exists, can't continue
                    getLogger().log(Level.SEVERE, "nodeagent.initialSyncFailed");
                    stopAgentWithPause();
                }

                // Now validate the admin-password against admin-keyfile which
                // might be in synch.
                if (domain_xml_file.exists()) {
                    try {
                        validateAdminUserAndPassword();
                    } catch (final Exception ae) {
                        this.stopAgentWithPause();
                    }
                }
 
                // Resync our config context in case domain.xml 
                // has changed on disk.
                ConfigContext configContext = getConfigContext();
                if (configContext.isFileChangedExternally()) {
                    configContext.refresh();
                }

                String instanceRoot = 
                    (String) System.getProperty(INSTANCE_ROOT_PROPERTY);
                GFSystem.setProperty( KEYSTORE_PROPERTY, 
                                    instanceRoot + JKS_KEYSTORE);
                GFSystem.setProperty( TRUSTSTORE_PROPERTY, 
                                    instanceRoot + JKS_TRUSTSTORE);

                // check to see if the nodeagent still exists
                if(getNodeAgentConfigBean(getConfigContext()) == null) {
                    // nodeagent has been removed
                    getLogger().log(Level.WARNING, "nodeagent.hasBeenDeleted");    
                    // finished synchonizing now reconcile configuration 
                    // and remove instances
                    reconcileNodeConfiguration();
                    // clear das.properties
                    getDASPropertyReader().clearProperties();
                    getDASPropertyReader().write();
                    // set nodeAgent to deleted, as per spec it will never be 
                    // able to start again
                    setNodeAgentBindStatus(AgentConfig.NODEAGENT_DELETED_STATUS);
                    // remove local domain.xml and .domain.xml.timestamp 
                    // files that are used in sync
                    File file = null;
                    for (int ii = 0; ii < LOCAL_DAS_SYNC_FILES.length; ii++) {
                        file = new File(System.getProperty(INSTANCE_ROOT_PROPERTY)
                                      + LOCAL_DAS_SYNC_FILES[ii]);
                        file.delete();
                    }
                    // stop nodeagent after delete
                    System.exit(0);                    
                }  else {
                    // check to see if config for communication with DAS 
                    // has changes
                    updateDASConnectionInfo();                    
                    // reinitialize NodeAgent with updated domain.xml
                    init();
                    // finished synchonizing now reconcile configuration
                    createdInstances=reconcileNodeConfiguration();                    
                }
                
            } catch (Exception e) {
                getLogger().log(Level.WARNING, 
                                "nodeagent.synchronization.Exception",e);
                throw new AgentException(e);
            }        
        } else {
            // not bound
            getLogger().log(Level.FINE, 
                            "Error on Synchronization, NodeAgent is not bound");
            throw new AgentException(
                    _strMgr.getString("nodeAgent.unboundSyncError"));
        }           
        return createdInstances;
    }            
    

    private void ensureInstanceIsStopped(
        String instanceName, boolean synchronizeInstance) 
    throws AgentException {
        // The instance must be stoppped before we proceed. This check is here 
        // to prevent the same server being started multiple times. We must 
        // synchronize on status to guard agains the case when the same instance 
        // is started at exactly the same time (and they both attempt to update 
        // the status).
        int newStatus = Status.kInstanceStartingCode;
        if (synchronizeInstance)  {
            newStatus = Status.kInstanceSynchronizingCode;
        } 
        boolean wasStopped = InstanceStatus.getInstance().updateStatusIfStopped(
                                instanceName, newStatus);

        if (!wasStopped) {
            StringManagerBase sm = StringManagerBase.getStringManager(
                getLogger().getResourceBundleName());  
            String msg = 
                sm.getString("nodeagent.couldNotStartInstance", instanceName, 
                             InstanceStatus.getInstance().getStatus(
                                instanceName).getStatus().getStatusString());
            getLogger().log(Level.WARNING, msg);                
            throw new AgentException(msg);
        }
    }
  
    
    /** 
     * This method is used to remotely start an instance
     *
     * @param instanceName      instance to start
     * @throws AgentException   Exception thrown if anything goes wrong with 
     *                          starting the instance
     */    
    protected void startInstance(String instanceName, 
        boolean synchronizeInstance) throws AgentException {
       
        getLogger().log(Level.INFO, "nodeagent.startingInstance", instanceName);
        if (bDebug) System.out.println("Starting instance " + 
            instanceName + " with synch flag set to " + synchronizeInstance);
        
        // make sure instance exists to start it
        if (!instanceExists(instanceName)) {
            // instance doesn't exist, this could be caused by a sync error
            // with the nodeagent so it doesn't know to create the instance 
            // and then someone tries to start or stop it
            throw new AgentException(_strMgr.getString(
                    "nodeAgent.instanceDoesNotExist", instanceName));
        }        
        //Ensure that the instance is in a stopped state before starting it.
        ensureInstanceIsStopped(instanceName, synchronizeInstance);        
        if (synchronizeInstance)  {            
            try {
                //beginning instance synchronization
                InstanceStatus.getInstance().updateStatus(
                    instanceName, Status.kInstanceSynchronizingCode);
                // synchronise of behalf of the instance, should only be done 
                // on direct start invocation
                getLogger().log(Level.FINE, 
                    "NodeAgent synchronizing for instance", instanceName);
                synchronizeInstanceWithDAS(instanceName);
            } catch (AgentException ae) {
                //synchronization failed
                InstanceStatus.getInstance().updateStatusFromAdminChannel(
                    instanceName, Status.kInstanceNotRunningCode);
                // need to throw exception so start-instance command show 
                // proper error and fails
                throw ae;
            }
        }

        // there was no sync or it succeeded
        try {                
            //beginning instance startup
            InstanceStatus.getInstance().updateStatus(
                instanceName, Status.kInstanceStartingCode);                           
            // always start when asked (not checking for now)
            // add instance to ProcessManager & start it
            ProcessManager.getInstance().addProcessInstance(instanceName, 
                new ProcessInstanceInternal(instanceName));
            ProcessManager.getInstance().startProcess(instanceName);
            //startup succeeded
            InstanceStatus.getInstance().updateStatusFromAdminChannel(
                instanceName, Status.kInstanceRunningCode);   
        } catch (Exception e) {
            //startup failed. We put the instance in a not running state so that 
            // the user can attempt to start it again.
            InstanceStatus.getInstance().updateStatusFromAdminChannel(
                instanceName, Status.kInstanceNotRunningCode);                  
            StringManagerBase sm = StringManagerBase.getStringManager(
                getLogger().getResourceBundleName());  
            getLogger().log(Level.WARNING, 
              sm.getString("nodeagent.start.instance.exception", instanceName), e);                  
            throw new AgentException(e);
        }
    }
    
    private com.sun.enterprise.config.serverbeans.NodeAgent 
        getNodeAgentConfigBean(ConfigContext configCtxt) 
        throws ConfigException {                        
        // get the servers that currently exist in domain xml
        Domain domain = ConfigAPIHelper.getDomainConfigBean(configCtxt);  
        // get node controller config bean        
        return domain.getNodeAgents().getNodeAgentByName(getNodeAgentName());
    }

    Vector reconcileNodeConfiguration() throws AgentException {        
        getLogger().log(Level.FINE,
                        "Reconciling existing Node Configuration with " +
                        "configuration that is in domain.xml");
        Vector createdInstances=new Vector();
        try {            
            // get the servers that currently exist in domain xml
            Domain domain = ConfigAPIHelper.getDomainConfigBean(
                                getConfigContext());  
            Server[] servers=domain.getServers().getServer();
            
            // get EEInstancesManager and get instances that currently 
            // physically exist in repository
            InstanceConfig instanceConfig = new InstanceConfig(
                                            InstanceDirs.getInstanceRoot());
            EEInstancesManager eeInstancesManager = new EEInstancesManager(
                                                    instanceConfig);
            HashMap hmInstances = getInstancesAndStatus(
                                  instanceConfig, eeInstancesManager);
            // loop through for creatation of instances boolean match = false;
            Iterator serverIt = null;
            String instanceName = null;
            int status = 0;
            
            // get node controller config bean from domain
            com.sun.enterprise.config.serverbeans.NodeAgent nodeController
				= getNodeAgentConfigBean(getConfigContext());

            if (nodeController != null) {
                Properties props = null;
                ElementProperty elementProperty = null;
                Cluster cluster = null;
                // need a default, can't have %%%DOMAIN_NAME%%% go into
                // running server
                String domainName="DomainPropertyNotFound";
                // set default to "" because this string will actual be placed 
                // in the startserv's java command for starting the instance 
                // via the ProcessLauncher. This is the easiest way to have the 
                // com.sun.aas.clusterName set to null for a standalone Instance
                String clusterArg = "";
                
                for (int ii = 0; ii < servers.length; ii++) {
                    // check to see if the server belongs to this 
                    // node agent (repository)
                    if (instanceConfig.getRepositoryName().equals(
                            servers[ii].getNodeAgentRef())) {
                        // check if instance already exists or 
                        // needs to be created.
                        if (instanceExists(servers[ii].getName())) {
                            getLogger().log(Level.INFO, 
                            "nodeagent.instance.exists", servers[ii].getName());
                        } else {
                            // instance does not exist so create it
                            getLogger().log(Level.INFO, 
                            "nodeagent.create.instance", servers[ii].getName());
                            instanceConfig.setInstanceName(servers[ii].getName());
                            // set properties for token replacing startserv scripts
                            props = new Properties();
                            // add proper domain
                            elementProperty = domain.getElementPropertyByName(
                                           DOMAIN_XML_DOMAIN_NAME_PROPERTY_NAME);
                            if (elementProperty != null) 
                               domainName=elementProperty.getValue();
                            
                            getLogger().log(Level.INFO, 
                                        "domain for instance - " + domainName);
                            props.setProperty(EEScriptsTokens.DOMAIN_NAME, 
                                              domainName);

                            if (ServerHelper.isServerClustered(
                                  getConfigContext(), servers[ii].getName())) {
                                try {
                                    // see if part of cluster
                                    cluster = ClusterHelper.getClusterForInstance(
                                      getConfigContext(), servers[ii].getName());
                                    if (cluster != null)
                                        clusterArg = "-D" + CLUSTER_NAME + "=\"" 
                                                   + cluster.getName() + "\"";                                    
                                } catch (ConfigException ce) {
                                    // show at finest, because the server may 
                                    // not be part of cluster
                                    getLogger().log(Level.FINEST, 
                                        "Server isn't part of cluster", ce);
                                }
                            }
                            props.setProperty(EEScriptsTokens.CLUSTER_NAME, clusterArg);                            
                            // set the overriding properties used in token replacement
                            eeInstancesManager.setOverridingProperties(props);
                            // create instance
                            eeInstancesManager.createInstance();                            
                            // make list of created server instances to know which ones to 
                            // sync on nodeagent startup.
                            createdInstances.add(servers[ii].getName());
                        }
                        // remove it from delete instances array
                        hmInstances.remove(servers[ii].getName());
                    }
                }
            }
            
            // loop through and delete instances if nessesary
            serverIt = hmInstances.keySet().iterator();
            while (serverIt.hasNext()) {
                instanceName = (String)serverIt.next();
                status = ((Status)hmInstances.get(instanceName)).getStatusCode();
                
                // delete instances
                if (bDebug) System.out.println("Delete Server =" + instanceName 
                                             + " with status:" + status);
                getLogger().log(Level.INFO, "nodeagent.delete.instance", 
                                instanceName);
                instanceConfig.setInstanceName(instanceName);
                
                // check to see if instances is 
                if(status != Status.kInstanceNotRunningCode) {
                    try {
                        stopInstance(instanceName);
                    } catch (AgentException e) {
                        // only log exception, and continue deleting
                        StringManagerBase sm = StringManagerBase.getStringManager(
                            getLogger().getResourceBundleName());  
                        getLogger().log(
                          Level.INFO, 
                          sm.getString(
                             "nodeagent.stop.instance.exception", instanceName), 
                          e);                          
                    }
                }
                // delete instance, without checking for JMS
                // instances (RepositoryManager)
                eeInstancesManager.deleteInstance(false);
            }
            
            // after all has been completed successfully
            // synchonize newly created instances to make sure that the new 
            // synchroniztion strategy doesn't leave an instance without a 
            // config, which could be potentially started by a nodeagent restart
            // loop through and delete instances if nessesary
            serverIt = createdInstances.iterator();
            while (serverIt.hasNext()) {
                instanceName = (String)serverIt.next();
                // sync instance, if failed it should have been logged
                try {
                    synchronizeInstanceWithDAS(instanceName);
                } catch (AgentException ae) {
                    // exception should already be logged
                    // don't stop creation loop, so just keep looping
                    // this was only a fail safe on restart
                }
            }
        } catch (Exception e) {
            throw new AgentException(e);
        }
        return createdInstances;
    }
    
    protected boolean instanceExists(String iName) throws AgentException {
        boolean match=false;
        Iterator serverIt=null;
        String instanceName=null;

        // get instance and status' in repository
        try {
            HashMap hmInstances=getInstancesAndStatus();
            serverIt=hmInstances.keySet().iterator();

            while(serverIt.hasNext()) {
                instanceName=(String)serverIt.next();
                if(instanceName.equals(iName)) {
                    match=true;
                    break;
                }
            }
        } catch (RepositoryException e) {
            throw new AgentException(e);
        }
        return match;
    }
    
    /**
     * This method gets the reposority instances and their status
     *
     * @return HashMap of instance names as the key and the status as the value
     */
    protected HashMap getInstancesAndStatus() throws RepositoryException {
        InstanceConfig instanceConfig = 
            new InstanceConfig(InstanceDirs.getInstanceRoot());
        EEInstancesManager eeInstancesManager = 
            new EEInstancesManager(instanceConfig);
        return getInstancesAndStatus(instanceConfig, eeInstancesManager);
    }
    
    
    /**
     * This method gets the reposority instances and their status
     *
     * @param instanceConfig
     * @param eeInstancesManager
     * @return HashMap of instance names as the key and the status as value
     */
    protected HashMap getInstancesAndStatus(InstanceConfig instanceConfig, 
        EEInstancesManager eeInstancesManager) throws RepositoryException {
        
        // get EEInstancesManager and get instances that currently physically 
        // exist in repository
        HashMap hmInstances = new HashMap();

        RuntimeStatusList rtstatusList = 
            eeInstancesManager.getRuntimeStatus(instanceConfig);
        RuntimeStatus rtstatus = null;
        for (int i = 0; i < rtstatusList.size(); i++) {
            rtstatus = rtstatusList.getStatus(i);
            hmInstances.put(rtstatus.getName(), rtstatus.getStatus());
        }
        return hmInstances;
    }
          
    /**
     * This method attempts a "local" rendezvous. We look for all the domains 
     * at the same level (i.e. same parent directory) as this node agent, and we 
     * register ourselves with the first domain without checking host and port. 
     * This code is meant to be called by the installer only and is potentially 
     * dangerous since it is common to create multiple domains with the same 
     * port, etc in testing scenerios. Furthermore, it is important that the DAS 
     * not be running when this is invoked. This writes domain.xml directly, and 
     * could result in issues if the DAS is running since the DAS config context 
     * will not be refreshed with the new domain.xml.
     * @throws Exception
     */    
    public void localRendezvousWithDAS() throws Exception
    {
        getLogger().log(Level.FINE, "Attempting to Rendezvous with local DAS...");
        AgentConfig agentConfig = getNodeAgentConfig();
        File root = new File( 
                      new File(agentConfig.getRepositoryRoot()).getParentFile(),          
                      "domains");        
        NodeAgentPropertyReader naReader = getNodeAgentPropertyReader();  
        DASPropertyReader dasReader = getDASPropertyReader();        
        ConfigContext goodConfigContext = null;        
        boolean tooMany = false;
        String dasHost2 = dasReader.getHost();
        String dasPort2 = dasReader.getPort();
        if (root.exists()) {
            //Find all the domains with the same parent as this node agent
            File[] domains = root.listFiles();            
            if (domains != null) {                                
                for (int i = 0; i < domains.length; i++) {                                        
                    File domainXml = new File(new File(domains[i], "config"),  
                        PEFileLayout.DOMAIN_XML_FILE);                    
                    getLogger().log(Level.FINE, "Looking for " + 
                                                domainXml.getAbsolutePath());
                    if (domainXml.exists()) {                        
                        ConfigContext configContext = 
                            ConfigFactory.createConfigContext(
                                domainXml.getAbsolutePath());                        
                        String dasName = 
                            ServerHelper.getDAS(configContext).getName();            
                        Config dasConfig = ServerHelper.getConfigForServer(
                                                configContext, dasName);
                        JMXConnectorConfig dasInfo = 
                            ServerHelper.getJMXConnectorInfo(configContext,  
                                                             dasName);
                        String dasPort = 
                            dasConfig.getHttpService().getHttpListenerById(
                            "admin-listener").getPort();
                        String dasHost = dasInfo.getHost();                        
                        
                        getLogger().log(Level.FINE, "dasHost: " + dasHost + 
                              " dasPort: " + dasPort + " dasHost2: " + dasHost2 
                            + " dasPort2: " + dasPort2);
                        
                        // Look for the first domain matching the specified host 
                        // and port. There must be exactly one or we error out.
                        if (dasPort.equals(dasPort2) && (dasHost.equals(dasHost2) || 
                            PortConflictCheckerConfigBean.isLocalHost(dasHost2))) {
                            if (goodConfigContext != null) {
                                tooMany = true;
                                break; //we have found more than 1 matching domain
                            }                                           
                            goodConfigContext = configContext;
                            getLogger().log(Level.INFO, 
                                "nodeagent.attemptingRendezvousedWithDAS",
                                domainXml.getAbsolutePath()); 
                        }
                    }
                }
            }
        }
        if (tooMany) { 
            // There were more than one domain matching the specified host & port
            throw new AgentException(_strMgr.getString(
                "nodeAgent.attemptLocalRendezvous.TooManyLocalDomains", 
                dasHost2 + ":" + dasPort2, root.getAbsolutePath()));
        } else if (goodConfigContext == null) {
            // There were no domains matching the specified host 
            // and port NoLocalDomains
            throw new AgentException(_strMgr.getString(
                "nodeAgent.attemptLocalRendezvous.NoLocalDomains", 
                dasHost2 + ":" + dasPort2, root.getAbsolutePath()));
        } else {
            // There was exactly one domain, flush out its domain.xml
            NodeAgentsConfigBean ncb = 
                new NodeAgentsConfigBean(goodConfigContext);                            
            String retDasString = ncb.rendezvousWithDAS(
                naReader.getHost(), naReader.getPort(), getNodeAgentName(), 
                naReader.getProtocol(), naReader.getClientHost());     
            goodConfigContext.flush();            
            markAsBound(retDasString);                         
        }        
    }
    
    /**
     * @throws Exception  */    
    public void rendezvousWithDAS() throws Exception {
        getLogger().log(Level.FINE, "Attempting to Rendezvous with DAS...");
        DASPropertyReader dasReader=null;
        String reportingUrl=null;
        try {                                      
            // create JMXConnector to DAS using user and password as necessary
            // call MBean to add this NodeAgent
            NodeAgentPropertyReader naReader = getNodeAgentPropertyReader();            
            String agentName = getNodeAgentName();
            dasReader = getDASPropertyReader();
            ObjectName objname  = new ObjectName(DAS_NODECONTROLLER_MBEAN_NAME);
            
            int port=Integer.parseInt(dasReader.getPort());
            String jmxProtocol = null;
            
            //The secure property indicates whether we should initiate a 
            // secure connection to the DAS. 
            Boolean isDASSecure = new Boolean(dasReader.isDASSecure());
            if (isDASSecure.booleanValue()) {            
                jmxProtocol = DefaultConfiguration.S1_HTTPS_PROTOCOL;
            } else {
                jmxProtocol = DefaultConfiguration.S1_HTTP_PROTOCOL;
            }
            final JMXServiceURL url = 
                new JMXServiceURL(jmxProtocol, dasReader.getHost(), port);
            reportingUrl=url.toString();
            getLogger().log(Level.INFO, 
                "nodeagent.attemptingRendezvousedWithDAS", url);

            final JMXConnector conn = 
                JMXConnectorFactory.connect(url, initEnvironment(url));
            MBeanServerConnection serverConn=conn.getMBeanServerConnection();
            
            Object[] params = { naReader.getHost(), naReader.getPort(), 
                agentName, naReader.getProtocol(), naReader.getClientHost() };
            String[] signature = {"java.lang.String", "java.lang.String", 
                    "java.lang.String", "java.lang.String", "java.lang.String"};
            Object oret = serverConn.invoke(objname, "rendezvousWithDAS", 
                                            params,  signature);
            if (oret == null) {
                // problem with return from rendezvous, can't continue
                getLogger().log(Level.SEVERE, "nodeagent.invalidRendezvous");
                getLogger().log(Level.WARNING, "nodeAgent.stopping.agent");
                System.exit(1);
            }                  

            reportingUrl=dasReader.getJMXURL();
            if (bDebug) 
                System.err.println("BACK FROM HTTP CONNECT " + ((String)oret));
            markAsBound((String)oret);
            
            getLogger().log(Level.INFO, "nodeagent.rendezvousedWithDAS", 
                            dasReader.getJMXURL());
            
        } catch (RedirectException e) {
            getLogger().log(Level.SEVERE, 
                _strMgr.getString("nodeAgent.invalidRedirectAttempted")); 
            throw e;
        } catch (Exception e) {
            // proxy throws throwable, can't tightem up exceptions
            getLogger().log(Level.SEVERE, "nodeagent.failedRendezvousWithDAS", 
                reportingUrl  + " == " + e.toString());
            // set full exception to fine
            getLogger().log(
                Level.FINE, "nodeagent.rendezvous.with.das.Exception", e);
            
            // get cause to see it if is because CertificateNotYetValidException
            Throwable cause = e.getCause();
            if (cause instanceof CertificateNotYetValidException) {
                // this could be caused by miss-match is systems times, 
                // log detailed message of resolution
              getLogger().log(Level.WARNING, "nodeagent.CertificateNotYetValid", 
                  reportingUrl  + " == " + e.toString());
            }
            throw e;
        }
    }

    private void markAsBound(String dasRetString) throws IOException {
        
        // the das return string is expected in the form port|securityEnabled
        int iPos=dasRetString.indexOf("|");
        String port=dasRetString.substring(0, iPos);
        String securityFlag=dasRetString.substring(iPos + 1);
        // set and write out das properties
        DASPropertyReader dasReader = getDASPropertyReader();
        dasReader.setPort(port);
        dasReader.setIsDASSecure(securityFlag);
        dasReader.write();

        // nodeagent bound
        setNodeAgentBindStatus(AgentConfig.NODEAGENT_BOUND_STATUS);
    }
    /**
     *  This method initialize the environment for creating the JMXConnector.
     *  @return Map - HashMap of environemtn
     */
    private Map initEnvironment(JMXServiceURL serviceUrl) {
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";

        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, 
                IdentityManager.getUser());
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, 
                IdentityManager.getPassword());
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME, 
		DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        // If the do not confirm server cert flag is set, we must instantiate 
        // a new trustmanager and register it with the connector. The 
        // NoCertCheckX509TrustManager implicitly accepts the certificate of DAS
        // (without user confirmation) and enters it into the asadmintruststore.
        Boolean doNotConfirmCert = new Boolean(
            getNodeAgentPropertyReader().doNotConfirmServerCert());
        if (doNotConfirmCert.booleanValue()) {
            if (bDebug) System.out.println("do not confirm server certificate");            
            NoCertCheckX509TrustManager tm = 
                new NoCertCheckX509TrustManager(serviceUrl);
            env.put(DefaultConfiguration.TRUST_MANAGER_PROPERTY_NAME, tm);
        } else if (bDebug) {
            System.out.println("confirm server certificate");
        }
        return env;
    }
    
    protected void configureLogger() { 
        try {
            getLogger().log(Level.FINE, "Configuring Logger...");
            String logFileDefault = System.getProperty(INSTANCE_ROOT_PROPERTY) 
                                  + File.separator + "logs/server.log";
            String logFile = logFileDefault;
            if (getNodeAgentPropertyReader().isBound()) {
                // go through domain.xm
                com.sun.enterprise.config.serverbeans.NodeAgent nodeController =
                    getNodeAgentConfigBean(getConfigContext());
                if (nodeController != null) {
                    LogService logService=nodeController.getLogService();
                    if (logService != null) {
                        // get logservice info from config beans            
                        logFile=logService.getFile();
                        if (logFile == null) logFile = logFileDefault;
                        
                        logFile = System.getProperty(logFile);
                        String logHandler = logService.getLogHandler();
                        String logLevel = 
                            logService.getModuleLogLevels().getNodeAgent();
                        Handler[] handlers = getLogger().getHandlers();

                        if (bDebug) System.out.println("Log config number of " +
                            "handlers that currently exist:" + handlers.length +
                            " - " + logFile + " - " + logHandler  + " - " + 
                            logLevel);
                        
                        // register default logger
                        registerLogger(Level.parse(logLevel), logFile);

			// fix for CR# 6443219
                        configureSynchronizationLogger(
                           logService.getModuleLogLevels().getSynchronization());
                    }
                }
            } else {
                // register default logger from nodeagent properties
                registerLogger(Level.INFO, logFile);
            }
            
            // Redirect stdout and stderr to appropriate location
            // NOTE: In verbose more the stderr only goes to the console, 
            // this is exactly the way the apache commons-launcher functioned.
            String verboseMode = System.getProperty(
                                    "com.sun.aas.verboseMode", "false");
            if (bDebug) System.out.println(
                    "******* Redirecting stdout & stderr to : " + logFile);
            if (logFile != null && !verboseMode.equals("true")) {
                // If applicable, redirect output and error streams
                // PrintStream printStream = 
                //   new PrintStream(new FileOutputStream(logFile, true), true);
                // System.setOut(printStream);
                // System.setErr(printStream);
                new SystemOutandErrHandler();
            } 
        } catch (Exception e) {
	    getLogger().info("nodeagent.noLogger");
        }
    }

    private void initializeSecurity(JmxConnector jmxConnector, 
        com.sun.enterprise.config.serverbeans.NodeAgent nodeController) 
    throws Exception {                        
        // Force static initialization of NSS store. This causes the NSS 
        // database to be read into an in memory java keystore and will be
        // used NSS.        
        SecurityUtil.getSecuritySupport();
        
        //Realm initializtion
        String realmName = jmxConnector.getAuthRealmName();                            
        RealmConfig.createRealms(realmName, 
            new AuthRealm[] {nodeController.getAuthRealm()});                             
    }    

    private String getObjectName() {
       return ":type=NodeAgent,name=" + getNodeAgentName() + ",category=config";
    }
    
    /**
     * Registers a JMXConnector in the MBeanServer based on the parameters
     *
     * @param protocol   protocol of JmxConnector
     * @param host       hostname for jmxConnector to listen on
     * @param port       port for the jmxConnector to listen on
     * @throws           Exception  
     *
     */    
    protected void registerJmxConnector(
        String protocol, String host, String port) throws Exception {
        if (bDebug) 
            System.out.println(this.getClass().getName() + 
                ": Trying to start jmx connector:" + "service:jmx:" + protocol 
              + "://" + host + ":"  + port);

        /* TODO - changes required here ... */        
        ConfigContext configContext = getConfigContext();
        JmxConnector connector = NodeAgentHelper.getNodeAgentSystemConnector(
            configContext, getNodeAgentName());        
        final int iPort = Integer.parseInt(port);        
        final JmxConnectorServerDriver dr = new JmxConnectorServerDriver();
        dr.setAuthentication(true);
        dr.setAuthenticator(createJMXAuthenticator(connector));
        dr.setMBeanServer(_server);
        dr.setBindAddress(connector.getAddress());
        dr.setPort(iPort);
        dr.setProtocol(RemoteJmxProtocol.instance(protocol)); 
        handleSsl(dr, connector);
        dr.setLogger(_logger);
        dr.setRmiRegistrySecureFlag(false); //TODO
        final JMXConnectorServer serverEnd = dr.startConnectorServer();
        getLogger().log(Level.INFO, "NodeAgent url  = " + 
                                      "service:jmx:" + connector.getProtocol() + 
                                      "://" + connector.getAddress() + 
                                      ":"  + connector.getPort());

        registerMBean(serverEnd, JMX_SERVER_MBEAN_PREFIX + port);
        if (bDebug) query();
    }
    
    private JMXAuthenticator createJMXAuthenticator(JmxConnector connector) 
        throws ConfigException {
        final ASJMXAuthenticator authenticator = new ASJMXAuthenticator();                
        authenticator.setRealmName(connector.getAuthRealmName());        
        authenticator.setLoginDriver(new ASLoginDriverImpl());
        return authenticator;
    }
    
    private void handleSsl(JmxConnectorServerDriver dr, JmxConnector connector) {
        final boolean ssl = connector.isSecurityEnabled();
        if (bDebug) System.out.println(
                "DEBUG MESSAGE: SSL Status for System Jmx Connector: " + ssl);
        dr.setSsl(ssl);        
        RMIServerSocketFactory sf = null;
        if (ssl) {                          
            sf = new AdminSslServerSocketFactory(connector.getSsl(), 
                                                 connector.getAddress());
            RMIClientSocketFactory cf = new AdminRMISSLClientSocketFactory(); 
            dr.setRmiClientSocketFactory(cf);
        } else sf = new RMIMultiHomedServerSocketFactory(connector.getAddress());
        dr.setRmiServerSocketFactory(sf);
    }
    
    protected void configureAgent() throws Exception {
        try {
            getLogger().log(Level.FINEST, "Configuring Agent...");

            if (_server == null) {
                // create mbean server only once
                _server = 
                    MBeanServerFactory.createMBeanServer(MBEAN_SERVER_DOMAIN);
                // register the nodeagent mbean
                registerMBean(this, getObjectName());
            }

            // get JmxConnectors that already exist in MBeanServer
            ObjectName nameSearch = new ObjectName(
                _server.getDefaultDomain() + JMX_SERVER_MBEAN_SEARCH);

            // query on all ports to find the JMXConnectors that exist
            Set ls = _server.queryNames(nameSearch, null);

            Iterator it = ls.iterator();
            ObjectName objectName = null;
            String currPort = null;
            Vector vtCurrent = new Vector();
            while (it.hasNext()) {
                objectName = (ObjectName)it.next();
                if (bDebug) System.out.println(
                    "*** currently registered connectors: " + objectName);
               // break down list an get actual port numbers
                currPort = objectName.getKeyProperty("port");
                vtCurrent.add(objectName);
            }

            if (getNodeAgentPropertyReader().isBound()) {
                // go through domain.xml and register connector
                com.sun.enterprise.config.serverbeans.NodeAgent nodeController
                    = getNodeAgentConfigBean(getConfigContext());
                if (nodeController != null) {
                    final JmxConnector jmxConnector = 
                        nodeController.getJmxConnector();
                    if (jmxConnector != null) {
                        initializeSecurity(jmxConnector, nodeController); 
                        if (!isPortRegistered(
                                vtCurrent, jmxConnector.getPort())) {
                            // if not registered in mbean server, 
                            // create and register it
                            registerJmxConnector(jmxConnector.getProtocol(), 
                                                 jmxConnector.getAddress(), 
                                                 jmxConnector.getPort());
                        }                                                           
                    }
                }
            } else {
                // register default jmx connector
                if (!isPortRegistered(vtCurrent, 
                        getNodeAgentPropertyReader().getPort())) {
                    // if not registered in mbean server, create and register it
                    registerJmxConnector(
                        getNodeAgentPropertyReader().getProtocol(), 
                        getNodeAgentPropertyReader().getHost(), 
                        getNodeAgentPropertyReader().getPort());
                }
            }

            // now remove any JmxConnectors from the mbean server that should 
            // no longer exist
            it = vtCurrent.iterator();
            while (it.hasNext()) {
                objectName = (ObjectName)it.next();
                if (bDebug) System.out.println(
                    "Removing JmxConnectorServer - " + objectName);

                // invoke stop on the jmxConnectors mbean and unregister it
                _server.invoke(objectName, "stop", 
                               new Object[]{}, new String[]{});                
                _server.unregisterMBean(objectName);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Invoked by the BaseNodeagent after intialization has been completed.
     *
     * @param startInstancesOverride  Whether to override the attribute 
     *                                in node-agent element in domain.xml
     */
    protected void postStartupProcessing(String startInstancesOverride, 
        boolean syncInstancesOverride) throws Exception {

        // currently only startup all servers managed by nodeagent if BOUND 
        // andset in domain.xml get node controller config bean from domain
        if (getNodeAgentPropertyReader().isBound()) {
            com.sun.enterprise.config.serverbeans.NodeAgent nodeController = 
                getNodeAgentConfigBean(getConfigContext());

            if (bDebug) {
                String defaultStartup=null;
                if (nodeController != null) 
                  defaultStartup = 
                    String.valueOf(nodeController.isStartServersInStartup());
                System.out.println(
                    "Node Agent PostStartUpProcessing: domain.xml " +
                    "startup attibute = " + defaultStartup +
                    " startup override = " + startInstancesOverride);
            }

            // protect against null from all source, 
            // including command line invoke
            if (startInstancesOverride == null) startInstancesOverride="NOTSET";
            
            // check to see if should start from domain.xml attribute or override
            if (  (startInstancesOverride.equals("NOTSET") && 
                   nodeController != null && 
                   nodeController.isStartServersInStartup())
                || startInstancesOverride.toLowerCase().equals("true") ) {

                // get servers that are associated with nodeagent
                // since this is post processing, all servers should be already
                // created it is possible that a race condition could be 
                // encountered so startup errors are just logged                
                Server[] servers = ServerHelper.getServersOfANodeAgent(
                                        getConfigContext(), getNodeAgentName());
                                
                //Start all the server instances in parallel
                StartServerTask tasks[] = new StartServerTask[servers.length];
                for(int ii=0; ii < servers.length; ii++) {
                    tasks[ii] = new StartServerTask(
                            servers[ii].getName(), syncInstancesOverride);
                }
                Executor e = new Executor(tasks);
                e.run();                
            }
        }
    }
    
    /**
     * Invoked by the NodeagentMain before the nodeagent is shutdown
     */
    protected void shutdownProcessing() throws Exception {
        // currently only shutdown all servers managed by nodeagent (not just 
        // the ones you specifically started) if BOUND and set in domain.xml
        // get node controller config bean from domain
        if (getNodeAgentPropertyReader().isBound()) {
            com.sun.enterprise.config.serverbeans.NodeAgent nodeController = 
                getNodeAgentConfigBean(getConfigContext());

            // stop all instances that belong to nodeagent
            if (nodeController != null) {
                // get instances that currently reside in repository
                HashMap hmInstances=getInstancesAndStatus();
                Iterator serverIt=hmInstances.keySet().iterator();
                String instanceName=null;
                Status instanceStatus=null;
                ArrayList runningInstances = new ArrayList();
                //Get a list of the running server instances
                while(serverIt.hasNext()) {
                    // get instance name of server
                    instanceName=(String)serverIt.next();
                    instanceStatus=(Status)hmInstances.get(instanceName);
                    if (bDebug) System.out.println("Shutdown processing, " +
                      "seeing if need to shutdown - " + instanceName  +
                      " with status " + instanceStatus);
                   
                    if (instanceStatus != null && 
                        instanceStatus.getStatusCode() == 
                            Status.kInstanceRunningCode) {
                        // stop instance
                        if (bDebug) System.out.println("Shutdown processing, " 
                                           + "shutting down - " + instanceName);
                        getLogger().log(Level.FINE, 
                            "NodeAgent Shutdown processing, Shutting " +
                            "down instance - " + instanceName);                        
                        runningInstances.add(instanceName);                            
                    }
                }
                       
                //Stop the server instances
                String[] servers = (String[])runningInstances.toArray(
                    new String[runningInstances.size()]);
                //Start all the server instances in parallel
                StopServerTask tasks[] = new StopServerTask[servers.length];
                for(int ii=0; ii < servers.length; ii++) {
                    tasks[ii] = new StopServerTask(servers[ii]);
                }
                Executor e = new Executor(tasks);
                e.run();                
            }
        }
    }

    /**
     * Performs synchronization on behalf of the instance
     * @param instance - name of instance to synchronize
     */
    protected int synchronizeInstanceWithDAS(String instance) 
    throws AgentException {
        // synchronize instance before starting in same jvm as ProcessLauncher
        // Synchronise with DAS, which will put domain.xml in config directory
        // need to use runtime exec until Nazrul finishes removing env 
        // dependency so it can be called in process
        int iret = 1;
        String sxRet = "FAILED!";
       
        if(isInstanceStarted(instance)) {
	    getLogger().log(Level.FINE, 
                            "Returning from synchronizeInstanceWithDAS as " +
                            "instance is already running");
	    // FIXME: This return value causes the stopInstance method to 
            // return silently instead of throwing exception saying that the 
            // instance is already running. This is a TBD.
	    return 90; // 90 indicates that the server instance is running 
	}
 
        // get node controller config bean from domain to see if there 
        // is a jvm are for synchronization
        String instanceSyncJvmOptions = "";
        try {
            com.sun.enterprise.config.serverbeans.NodeAgent nodeagent
                = getNodeAgentConfigBean(getConfigContext());
            ElementProperty instanceSyncJvmOptionsProperty =
                nodeagent.getElementPropertyByName(INSTANCE_SYNC_JVM_OPTIONS);
            if (instanceSyncJvmOptionsProperty != null) 
               instanceSyncJvmOptions=instanceSyncJvmOptionsProperty.getValue();
        } catch (ConfigException ce) {
            getLogger().log(Level.WARNING, 
               "nodeagent.synchronization.Exception", ce);
        }
        
        long startSync = System.currentTimeMillis();
        try {
            InstanceConfig instanceConfig = new InstanceConfig(
                InstanceDirs.getRepositoryName(), 
                InstanceDirs.getRepositoryRoot(), instance);
            String iRoot = instanceConfig.getRepositoryRoot() + File.separator + 
                           instanceConfig.getRepositoryName() + File.separator + 
                           instance;
            if (bDebug) System.out.println("*** SYNCHRONIZE INSTANCE: "+ iRoot);

            //SynchronizationDriver sd = 
            // new SynchronizationDriverFactory.getSynchronizationDriver(
            // iRoot, SynchronizationDriverFactory.INSTANCE_CONFIG_URL);
            //sd.synchronize();
            String installRoot = System.getProperty(INSTALL_ROOT_PROPERTY);
            ArrayList ar = new ArrayList();
            ar.add(System.getProperty(JAVA_ROOT_PROPERTY) + File.separator + 
                                      "bin" + File.separator + "java");
            // add jvm options from domain.xml
            if ( instanceSyncJvmOptions != null && 
                !instanceSyncJvmOptions.equals("")) {
                
                ar.add(instanceSyncJvmOptions);
            }

            String logFileDefault = System.getProperty(INSTANCE_ROOT_PROPERTY) + 
                                            File.separator + "logs/server.log";
            /*String logFile=logFileDefault;
            com.sun.enterprise.config.serverbeans.NodeAgent nodeController = 
             getNodeAgentConfigBean(getConfigContext());
            LogService logService=nodeController.getLogService();
            if(logService != null) {
                // get logservice info from config beans            
                logFile=logService.getFile();
                if (logFile == null) {
                    logFile=logFileDefault;
                }
            }
            logFile=System.getProperty(logFile);*/
            
            //ar.add("-Xdebug");
            //ar.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9119");
            
            ar.add("-Djava.util.logging.manager" + 
                    "=" + "com.sun.enterprise.server.logging.ServerLogManager");
            ar.add("-D" + INSTALL_ROOT_PROPERTY + "=" + installRoot);
            ar.add("-D" + AGENT_ROOT_PROPERTY + 
                    "=" + System.getProperty(INSTANCE_ROOT_PROPERTY));
            ar.add("-D" + AGENT_NAME_PROPERTY + 
                    "=" + System.getProperty(SERVER_NAME));
            ar.add("-D" + AGENT_CERT_NICKNAME + 
                    "=" + ServerHelper.getCertNickname(getConfigContext(), 
                          IAdminConstants.DAS_SERVER_NAME));
            setKeyAndTrustStoresForInstanceSync(ar);
            ar.add("-D" + "com.sun.aas.defaultLogFile" +
                    "=" + logFileDefault);           
                      
            ar.add("-D" + INSTANCE_ROOT_PROPERTY + "=" + iRoot);
            ar.add("-D" + CONFIG_ROOT_PROPERTY + 
                    "=" + System.getProperty(CONFIG_ROOT_PROPERTY));
            ar.add("-D" + SystemPropertyConstants.SERVER_NAME + "=" + instance);
            ar.add("-D" + "com.sun.appserv.pluggable.features" + 
                   "=" + "com.sun.enterprise.ee.server." +
                         "pluggable.EEPluggableFeatureImpl");
            ar.add("-D" +  "com.sun.aas.promptForIdentity" + "=" + "true");
            ar.add("-cp");
            String fs = File.separator;
            String fps = File.pathSeparator;
            ar.add(installRoot + fs + "lib" + fs + "appserv-admin.jar" + fps + 
                   installRoot + fs + "lib" + fs + "appserv-rt.jar" + fps + 
                   installRoot + fs + "lib" + fs + "javaee.jar" + fps + 
                   installRoot + fs + "lib" + fs + "appserv-se.jar" + fps + 
                   installRoot + fs + "lib" + fs + "appserv-ee.jar");

            ar.add("com.sun.enterprise.ee.synchronization.SynchronizationMain");
            String synUrl = HttpUtils.getSynchronizationURL(getConfigContext());
            if (synUrl != null) ar.add(synUrl);
            
            String[] command=new String[ar.size()];
            command = (String[])ar.toArray(command);

            if (getLogger().isLoggable(Level.FINE)) {
                StringBuffer sb=new StringBuffer();
                for(int ii=0; ii < command.length; ii++) {
                    sb.append(command[ii] + " ");
                } 
                getLogger().log(Level.FINE, sb.toString());
            }
            
            Process process = Runtime.getRuntime().exec(command);
            IdentityManager.writeToOutputStream(process.getOutputStream());
            StreamFlusher sfOut = 
                new StreamFlusher(process.getInputStream(), System.err);
            StreamFlusher sfErr = 
                new StreamFlusher(process.getErrorStream(), System.err);
            sfOut.start();
            sfErr.start();
            iret=process.waitFor();

            // logs a severe msg when synchronization fails
            Level sLoglevel = Level.SEVERE;
            if (iret == 0) {
                sxRet="SUCCEEDED!";
                sLoglevel = Level.INFO;
            }
            getLogger().log(sLoglevel, "nodeagent.syncStatus", 
                new Object[]{ instance, sxRet, 
                    String.valueOf(System.currentTimeMillis() - startSync)});
            
        } catch (Exception e) {
            // need to catch specific exception for sync
            getLogger().log(Level.WARNING, 
                "nodeagent.synchronization.Exception",e);
            throw new AgentException(e);
        }
        return iret;
    }
    
   /**
    * @param instance name of the instance
    */
    private boolean isInstanceStarted(String instance) {
	try {
	    HashMap h = getInstancesAndStatus();
	    Status s = (Status) h.get(instance);
	    if (s.getStatusCode() == Status.kInstanceRunningCode ||
	        s.getStatusCode() == Status.kInstanceStoppingCode ||
	        s.getStatusCode() == Status.kInstanceStartingCode ) {
	        return true;
	    }
	} catch(Exception e){
	    //ignore
        }
	return false;
    }

    /**
     * Updates the das properties from domain.xml if they have changed
     */
    protected void updateDASConnectionInfo() throws Exception {

        MBeanServerConnectionInfo dasInfo = 
            InstanceRegistry.getDASConnectionInfo(getConfigContext());
        boolean bWrite = false;

        // get appropriate das properties
        DASPropertyReader dasProperty = getDASPropertyReader();
        if (!dasProperty.getProtocol().equals(dasInfo.getProtocol()) ||
            !dasProperty.getPort().equals(dasInfo.getPort()) ||
            !dasProperty.getHost().equals(dasInfo.getHost())) {

            // something changed for the das connection, update das properties
            dasProperty.setProtocol(dasInfo.getProtocol());
            dasProperty.setPort(dasInfo.getPort());
            dasProperty.setHost(dasInfo.getHost());
            bWrite = true;
            
            // log message so the user will know to change hosts file 
            // so it is pingable.
            getLogger().log(Level.CONFIG, "nodeagent.changeOfDASValues", 
                new Object[] {dasInfo.getHost() + ":" + dasInfo.getPort(), 
                              dasInfo.getHost()});
            
        }
        
        // check to see if jmx secure flas has changed
        Server das = ServerHelper.getDAS(getConfigContext());
        if (das != null) {
            String dasName=das.getName();
            JmxConnector dasConnector = ServerHelper.getServerSystemConnector(
                                            getConfigContext(), dasName);
            if (dasConnector != null) {
                String jmxConnSecure = 
                    String.valueOf(dasConnector.isSecurityEnabled());
                if (!String.valueOf(
                        dasProperty.isDASSecure()).equals(jmxConnSecure)) {
                    dasProperty.setIsDASSecure(jmxConnSecure);
                    bWrite = true;
                }
            }
        }
        // only write if you have to
        if (bWrite) dasProperty.write();
    }    
    
    protected void validateAdminUserAndPassword() throws AgentException {
        final AgentConfig ac = getAgentConfig();
        setPasswords(ac);
        final AgentManager am = getAgentManager(ac);
        am.validateAdminUserAndPassword(ac);
    }
    
    private AgentConfig getAgentConfig() {
        final String myName = System.getProperty(SERVER_NAME);
        final String myPath = getMyPath();
        final AgentConfig ac = new AgentConfig(myName, myPath);
        return ac;
    }
    
    private String getMyPath() {
        final String path = System.getProperty(INSTANCE_ROOT_PROPERTY);
        final File f = new File(path);
        final String parent = new File(f.getParent()).getParent();
        return parent;
    }

    private AgentManager getAgentManager(AgentConfig agentConfig) {
        EEDomainsManager domainsManager = 
            (EEDomainsManager)getFeatureFactory().getDomainsManager();
        return domainsManager.getAgentManager(agentConfig);
    }

     private void setPasswords(final AgentConfig ac) {
         String mp = IdentityManager.getMasterPassword();
         ac.put(AgentConfig.K_MASTER_PASSWORD, mp);
         // Also make sure to have the system properties new JKS loading will
         // require in the NodeAgent VM. This was missed during JKS changes
         GFSystem.setProperty("javax.net.ssl.keyStorePassword", mp);
         GFSystem.setProperty("javax.net.ssl.trustStorePassword", mp);
         ac.put(AgentConfig.K_DAS_USER, IdentityManager.getUser());
         ac.put(AgentConfig.K_DAS_PASSWORD, IdentityManager.getPassword());
     }

     private ClientPluggableFeatureFactory getFeatureFactory() {
        final String featurePropClass = System.getProperty(
            ClientPluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME,
            null);
         ClientPluggableFeatureFactoryImpl featureFactoryImpl =
            new ClientPluggableFeatureFactoryImpl(getLogger());
         ClientPluggableFeatureFactory featureFactory =
            (ClientPluggableFeatureFactory)featureFactoryImpl.getInstance(
            featurePropClass);
         return featureFactory;
     }

    // This is a quick solution to fix the synchronization logging domain's 
    // log level since its broken in ServerLogManager within the 
    // node agent process
    private void configureSynchronizationLogger(String level) {
        Level ll = Level.INFO;
        try {
            ll = Level.parse(level);
        } catch(Exception e) {
            getLogger().log(Level.FINE, "Exception: " + e.getMessage());
        }
        Logger syncLogger = Logger.getLogger(
            com.sun.logging.ee.EELogDomains.SYNCHRONIZATION_LOGGER);

        getLogger().log(Level.FINE, "Setting synchronization loglevel to "+ ll);
        syncLogger.setLevel(ll);
    }
 
    //XXX temp, need to change after profile is done
    private void setKeyAndTrustStoresForInstanceSync(ArrayList ar) 
    throws AgentException {
        String configDir = System.getProperty("com.sun.aas.instanceRoot") +
                           File.separator + "config";
        java.io.File nssFile = new File(configDir, "key3.db");
        if (!nssFile.exists()) {
            if (System.getProperty("javax.net.ssl.keyStore") == null) {
                StringManagerBase sm = StringManagerBase.getStringManager(
                                        getLogger().getResourceBundleName()); 
                String msg = sm.getString(
                    "nodeagent.noKeyStoreFoundForInstanceSync");
                getLogger().log(Level.SEVERE, msg);                                       
                throw new AgentException(msg);
            } else {
                ar.add("-D" + "javax.net.ssl.keyStore" + "=" + 
                    System.getProperty("javax.net.ssl.keyStore"));
                ar.add("-D" + "javax.net.ssl.trustStore" + "=" + 
                    System.getProperty("javax.net.ssl.trustStore"));
                ar.add("-D" + "javax.net.ssl.keyStorePassword" + "=" + 
                    System.getProperty("javax.net.ssl.keyStorePassword"));
                ar.add("-D" + "javax.net.ssl.trustStorePassword" + "=" + 
                    System.getProperty("javax.net.ssl.trustStorePassword"));
            }
        } else {
            ar.add("-D" + NSS_ROOT_PROPERTY + "=" + 
                    System.getProperty(NSS_ROOT_PROPERTY));
            ar.add("-D" + NSS_DB_PROPERTY + "=" + 
                    System.getProperty(NSS_DB_PROPERTY));
            ar.add("-D" + NSS_DB_PASSWORD_PROPERTY + "=" + 
                    System.getProperty(NSS_DB_PASSWORD_PROPERTY));
        }
    }

    public static void main(String args[]) {
        try {
            final int numArgs = args.length;
            int port = DEFAULT_CONNECTOR_PORT;

            if (numArgs == 1) {
                port = Integer.parseInt(args[0]);
            } else if (numArgs != 0) {
                System.out.println("USAGE: Server <port");            
            }
            NodeAgent nas = new NodeAgent();
            nas.run(null, false);
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }   

    private boolean haveDASCoordinatesChanged(String newHost, String newPort) 
    throws ConfigException {
        
        AdminService as = 
            ServerHelper.getAdminServiceForServer(getConfigContext(), "server");
        JmxConnector jmxConn = 
            as.getJmxConnectorByName(as.getSystemJmxConnectorName());
        
        if (!jmxConn.getPort().equals(newPort)) return true;

        ElementProperty clientHostname = 
            jmxConn.getElementPropertyByName(HOST_PROPERTY_NAME);
        if (!newHost.equals((String)clientHostname.getValue())) return true;
        
        return false;
    }

    class StartServerTask extends Task {
        private static final long TIMEOUT_IN_MILLIS = 600000; //5 minutes       
        private String _serverName;        
        private boolean _syncInstance;
        
        public StartServerTask(String serverName, boolean syncInstance) {
            super(TIMEOUT_IN_MILLIS);
            _serverName = serverName;            
            _syncInstance = syncInstance;
        }
                
        public void run() {            
            try {
                startInstance(_serverName, _syncInstance);
            } catch (AgentException ex) {
                // Eat the exception. It has already been logged by startInstance.                 
            }            
        }
    }

    class StopServerTask extends Task {
        private static final long TIMEOUT_IN_MILLIS = 120000; //2 minutes     
        private String _serverName;        
        
        public StopServerTask(String serverName) {            
            super(TIMEOUT_IN_MILLIS);
            _serverName = serverName;            
        }
                
        public void run() {            
            try {
                stopInstance(_serverName);
            } catch (AgentException ex) {
                // Eat the exception. It has already been logged by stopInstance.
            }            
        }
    }
}
