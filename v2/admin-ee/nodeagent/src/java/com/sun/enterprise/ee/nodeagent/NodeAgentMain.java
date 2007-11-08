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

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import com.sun.enterprise.util.JvmInfoUtil;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
//import javax.management.remote.JMXServiceURL;
//import javax.management.remote.jmxmp.JMXMPConnectorServer;

import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.NodeAgentPropertyReader;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.admin.event.ShutdownEvent;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ShutdownEventListener;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.security.store.IdentityManager;


import com.sun.appserv.management.util.misc.RunnableBase;
import com.sun.enterprise.util.FeatureAvailability;
import static com.sun.enterprise.util.FeatureAvailability.MBEAN_SERVER_FEATURE;



class Shutdown implements ShutdownEventListener {
    private final BaseNodeAgent bna;
    
    public Shutdown(BaseNodeAgent bna) {
        this.bna=bna;
    }

    public void startShutdown(ShutdownEvent event)
            throws AdminEventListenerException 
    {
        NodeAgentMain.getLogger().log(Level.INFO, "nodeAgent.stopping.agent");
        try {
            AdminChannel.setRMIChannelStopping();
            if (bna != null) bna.shutdownProcessing();
            AdminChannel.destroyRMIChannel();
        } catch (Exception ex) {
            NodeAgentMain.getLogger().log(Level.WARNING, "nodeAgent.exception", ex);            
        }
        System.exit(0);
    }
}

public class NodeAgentMain {

    private static final StringManager _strMgr=StringManager.getManager(NodeAgentMain.class);
    private static AgentConfig _config = null;
    private static Logger _logger = null;
    private static volatile boolean bDebug=Boolean.getBoolean("Debug");

    private static void usage()
    {
        System.out.println("usage: NodeAgentMain start|stop");
    }
    
    private static synchronized AgentConfig getConfig() {
        if (_config == null) {
            _config = new AgentConfig();
        }
        return _config;
    }
    
    static synchronized Logger getLogger() {
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
    
    private static void shutdown() {        
        try {
            getLogger().log(Level.INFO, "nodeAgent.sending-stop");
            String agentName = getConfig().getRepositoryName();
            RMIClient rmiClient = AdminChannel.getRMIClient(agentName);                
            ShutdownEvent shutdownEvent = new ShutdownEvent(agentName);	   
            AdminEventResult result = rmiClient.sendNotification(shutdownEvent);            
        } catch(Exception ex) {         
            getLogger().log(Level.WARNING, "nodeAgent.exception", ex);
            System.exit(1);
        }	
    }       
    
    private static void startup(String startInstancesOverride, 
        boolean syncInstancesOverride) throws ServerLifecycleException {                

        // read in parameters off the stdin if they exist
        try {
            IdentityManager.populateFromInputStreamQuietly();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "nodeagent.failureOnReadingSecurityIdentity", e);
        }
        getLogger().log(Level.INFO, IdentityManager.getFormatedContents());

        // Initialize admin channel and set our status to running
        AdminChannel.createRMIChannel();
        AdminChannel.createSharedSecret();
        // Publish PID for this VM in a secure place
        publishPID();
        
        // tell user to go to log 
        // FIXME: this needs to tell the user the proper place which is dictated by its config ???
        System.out.println(_strMgr.getString("nodeAgent.redirecting.output", System.getProperty("com.sun.aas.instanceRoot") + "/logs/server.log"));
        getLogger().log(Level.INFO, "nodeagent.starting.agent");

        // Read in the DAS configuration (it may not be present).
        DASPropertyReader dasReader = new DASPropertyReader(new AgentConfig());
        try {             
            dasReader.read();
            if (dasReader.getPort() != null) {
                getLogger().log(Level.CONFIG, "DAS url = " + dasReader.getJMXURL());             
            }
        } catch (Exception e) {
            getLogger().log(Level.INFO, "nodeAgent.das_properties_not_found",e);
        }

        // Read in the nodeagent configuration (it must be present).
        NodeAgentPropertyReader nodeAgentReader = new NodeAgentPropertyReader(new AgentConfig());
        try {             
            nodeAgentReader.read();
            if (nodeAgentReader.isBound() == false)
                getLogger().log(Level.CONFIG, "NodeAgent url  = " + nodeAgentReader.getJMXURL());             
                
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "nodeAgent.nodeagent_properties_not_found",e);
            System.exit(1);
        }

        try {
            // instantiate an agent which will register the jmx connector and the mbean
            // get prop
            NodeAgent nas = new NodeAgent(dasReader, nodeAgentReader);
            AdminEventListenerRegistry.addShutdownEventListener(new Shutdown(nas));        
            nas.run(startInstancesOverride, syncInstancesOverride);
            // Put here so asadmin command waits until nodeagent is reachable
            // Set our status to running in the admin RMI channel
            AdminChannel.setRMIChannelReady();
        } catch (Exception e) {
            throw new ServerLifecycleException(e);
        }
        
    }
    
    
    /**
        Load the MBeanServer.
     */
    private static final class LoadMBeanServer extends RunnableBase {
        LoadMBeanServer() {
            super( "NodeAgentMain-LoadMBeanServer" );
        }
        protected void doRun() {
            final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            FeatureAvailability.getInstance().registerFeature(
                MBEAN_SERVER_FEATURE, mbeanServer);
            if (bDebug)
            System.out.println( "class of platform MBeanServer is " +
                mbeanServer.getClass().getName() );
        }
    };


    
    public static void main (String[] args) {
        new LoadMBeanServer().submit( RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD );
        
        try {
            // look for Debug system property
            if (System.getProperty("Debug") != null) {
                // turn on debug, this option was added to help developers
                // debug the their code what adding/modifying tasks that are executed via
                // the ProcessLauncher
                bDebug=true;
            }        

            // Use asenv.conf/bat to set up necessary system properties
            ASenvPropertyReader propertyReader = new ASenvPropertyReader(
                System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
            propertyReader.setSystemProperties();
        
            if (args == null || args.length < 1) {
                usage();
                System.exit(1);
            } else if (args[0].equals("stop")) {
                shutdown();
                System.exit(0);
            } else if (args[0].equals("start")) {
                // need string to be 3 states, null means domain.xml will take presidence
                String startInstancesOverride = null;
                boolean syncInstances = false;
                // look for argument that flags and override in the domain.xml properties
                for(int ii=0; ii < args.length; ii++) {
                    if (args[ii].startsWith(IAdminConstants.NODEAGENT_STARTINSTANCES_OVERRIDE)) {
                        // don't check for values, because will only override domain.xml value if true or false
                        startInstancesOverride=args[ii].substring(args[ii].indexOf("=") + 1);
                    }
                    if (args[ii].startsWith(
                        IAdminConstants.NODEAGENT_SYNCINSTANCES_OVERRIDE)) {
                        String syncInstancesOverride  = 
                            args[ii].substring(args[ii].indexOf("=") + 1);
                        if ("true".equalsIgnoreCase(syncInstancesOverride)) 
                            syncInstances = true;
                    }
                }
                startup(startInstancesOverride, syncInstances);
            } else {
                usage();
                System.exit(1);
            }
        } catch (Throwable ex) {
            // need to catch throwable, because some components through RuntimeExceptions
            getLogger().log(Level.SEVERE, "nodeAgent.exception", ex);
            System.exit(1);
        }
        
    }
    
    private static void publishPID() {
        JvmInfoUtil jvminfo = new JvmInfoUtil();
        String pidFileName = System.getProperty("com.sun.aas.instanceRoot") + "/config/";
        pidFileName += SystemPropertyConstants.PID_FILE;
        jvminfo.logPID(pidFileName);
    }        
}