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

package com.sun.enterprise.ee.nodeagent.mbeans;

import com.sun.enterprise.ee.diagnostics.EEDiagnosticAgent;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

import com.sun.enterprise.security.store.IdentityManager;

import com.sun.appserv.management.client.AdminRMISSLClientSocketFactoryEnvImpl;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;
import com.sun.appserv.management.client.TrustStoreTrustManager;
import com.sun.appserv.management.client.TLSParams;

import com.sun.enterprise.admin.common.JMXFileTransfer;
import com.sun.enterprise.diagnostics.DiagnosticAgent;
import com.sun.enterprise.diagnostics.DiagnosticException;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.NodeAgentPropertyReader;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.util.zip.ZipFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.X509TrustManager;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;

/**
 * This is called DiagnosticMBean because it is meant to aid the
 * diagnostice services. In future this mBean is expected to provide
 * more diagnostic service specific methods.
 *
 * @author Sreenivas Munnangi
 */

public class NodeAgentDiagnostic implements NodeAgentDiagnosticMBean {
    
    // vars
    protected Logger _logger = null;
    protected static final StringManager _strMgr =
            StringManager.getManager(NodeAgentDiagnostic.class);
    protected DASPropertyReader dasReader = null;
    protected NodeAgentPropertyReader naReader = null;
    
    // constants
    private static final String UPLOAD_DIRECTORY = "reports_temp";
    
    /**
     * default constructor
     */
    public NodeAgentDiagnostic() {
        initLogger();
        initDAS();
        initNodeAgent();
    }
    
    /**
     * uploads the diagnostic zip archive to DAS and returns the zip file name
     */
    public String generateReport(java.util.Map options, List<String> instances,
            String targetType){
        

        try {
            DiagnosticAgent agent = new EEDiagnosticAgent();
            String fileName = agent.generateReport(options, instances,
                    targetType);
            uploadZipFile(fileName);
            return fileName;
            
        } catch (Exception de) {
            _logger.log(Level.WARNING, 
                    "diagnostic-service.error_generating_report_na", 
                    de);
         }
        return null;
    }
    
    /**
     * upload zip file
     */
    private void uploadZipFile(String zipFileName) {
        _logger.log(Level.INFO, "NodeAgentDiagnosticMBean uploadZipFile ...");
        try {
            JMXConnector jmxC = getJMXConnector(
                    new JMXServiceURL(dasReader.getJMXURL()));
            MBeanServerConnection mbsc = jmxC.getMBeanServerConnection();
            // upload zip file using JMXFileTransfer
            JMXFileTransfer jmxFileTfr = new JMXFileTransfer(mbsc);
            jmxFileTfr.uploadFile(zipFileName, UPLOAD_DIRECTORY);
        } catch (java.net.MalformedURLException mfue) {
            _logger.log(Level.WARNING,
                    "nodeAgentDiagnostic.incorrect_jmx_service_url", mfue);
        } catch (java.io.IOException ioe) {
            _logger.log(Level.WARNING,
                    "nodeAgentDiagnostic.no_mbean_server_connection", ioe);
        }
    }
    
    
    /**
     * return jmx connector
     */
    private JMXConnector getJMXConnector(JMXServiceURL jurl) {
        try {
            final Map env = new  HashMap();
            env.put(JMXConnector.CREDENTIALS, new String[] {
                IdentityManager.getUser(),
                        IdentityManager.getPassword()});
                        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME,
                                IdentityManager.getUser());
                        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME,
                                IdentityManager.getPassword());
                        final HandshakeCompletedListenerImpl    hcListener  =
                                new HandshakeCompletedListenerImpl(
                                getSuppliedHandshakeCompletedListener() );
                        final AdminRMISSLClientSocketFactoryEnvImpl rmiEnv  =
                                AdminRMISSLClientSocketFactoryEnvImpl.getInstance();
                        rmiEnv.setHandshakeCompletedListener( hcListener );
                        rmiEnv.setTrustManagers( getTrustManagers() );
                        
                        JMXConnector conn = JMXConnectorFactory.connect(jurl, env);
                        return conn;
        } catch (java.io.IOException ioe) {
            _logger.log(Level.WARNING,
                    "nodeAgentDiagnostic.cannot_connect_to_jmx_connector_factory", ioe);
        }
        return null;
    }
    
    private static final X509TrustManager[] getTrustManagers() {
        return( mTLSParams == null ? null : mTLSParams.getTrustManagers() );
    }
    
    public static TLSParams createTLSParams() {
        File trustStoreFile = getDefaultTrustStore();
        char[] trustStorePassword =  DEFAULT_TRUST_STORE_PASSWORD.toCharArray();
        HandshakeCompletedListener handshakeCompletedListener   =
                new HandshakeCompletedListenerImpl();
        TrustStoreTrustManager trustMgr =
                new TrustStoreTrustManager( trustStoreFile, trustStorePassword);
        trustMgr.setPrompt( false );
        mTLSParams = new TLSParams(trustMgr, handshakeCompletedListener);
        return( mTLSParams );
    }
    
    private static final HandshakeCompletedListener
            getSuppliedHandshakeCompletedListener() {
        return( mTLSParams == null ? null : mTLSParams.getHandshakeCompletedListener() );
    }
    
    
    /**
     * initialze logger
     */
    private void initLogger() {
        _logger = Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER,
                "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
    }
    
    
    /**
     * initialze DAS property reader
     */
    private void initDAS() {
        try {
            dasReader = new DASPropertyReader(new AgentConfig());
            dasReader.read();
        } catch (Exception e) {
            _logger.log(Level.WARNING, "nodeAgent.das_properties_not_found",e);
        }
    }
    
    
    /**
     * initialze Node Agent property reader
     */
    private void initNodeAgent() {
        try {
            naReader = new NodeAgentPropertyReader(new AgentConfig());
            naReader.read();
        } catch (Exception e) {
            _logger.log(Level.WARNING, "nodeAgent.nodeagent_properties_not_found",e);
        }
    }
    
    
    public static File getDefaultTrustStore() {
        final String homeDir = System.getProperty( "user.home" );
        final String sep     = System.getProperty( "file.separator" );
        return new File( homeDir + sep + DEFAULT_TRUST_STORE_NAME );
    }
    
    public static final String DEFAULT_TRUST_STORE_NAME = ".asadmintruststore";
    public static final String  DEFAULT_TRUST_STORE_PASSWORD = "changeit";
    public static TLSParams mTLSParams = createTLSParams();
}
