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

package com.sun.enterprise.admin.server.core;

import com.sun.enterprise.admin.server.core.jmx.ssl.ServerClientEnvSetter;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Ssl;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.MBeanServer;
//import javax.management.MBeanServerFactory;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXAuthenticator;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.server.core.jmx.auth.ASJMXAuthenticator;
import com.sun.enterprise.admin.server.core.jmx.auth.ASLoginDriverImpl;

import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.util.SystemPropertyConstants;
/* Following classes come from /m/jws/jmx-remote which gets built before
 * /m/jws/appserv-core */
/* start */
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxConnectorServerDriver;
import com.sun.enterprise.admin.jmx.remote.server.rmi.RemoteJmxProtocol;
/* end */
import com.sun.enterprise.admin.server.core.jmx.nonssl.RMIMultiHomedServerSocketFactory;
import com.sun.enterprise.admin.server.core.jmx.ssl.AdminSslServerSocketFactory;
import com.sun.enterprise.admin.server.core.jmx.ssl.AsTlsClientEnvSetter;
import com.sun.appserv.management.client.AdminRMISSLClientSocketFactory; /* From MBean API */

/**
 *
 * @author  kedar
 */
public class JmxConnectorLifecycle implements ServerLifecycle {

    public static final Logger sLogger =
    Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager sm = StringManager.getManager(JmxConnectorLifecycle.class);
    private ServerContext initContext = null;
    private JmxConnector connectorConfig = null;
    private JMXConnectorServer cs = null;
    private JMXConnectorServer jconsolecs = null;
    private JmxConnectorServerDriver driver;
    private boolean isEnabled = false;
    /** Creates a new instance of JmxConnectorLifecycle.
     */
    public JmxConnectorLifecycle() {
        sLogger.log(Level.FINE, "rjmx.lc.init");
    }

    public void onInitialization(ServerContext sc) throws ServerLifecycleException {
        try {
            initContext = sc;
            initConnectorConfig();
            handleIsEnabled(connectorConfig.getPort());
            handleSupportedProtocol();
            if (isEnabled) {
                driver = new JmxConnectorServerDriver();
                configureJmxConnectorServerDriver();
            }
        }
        catch(Exception e) {
            throw new ServerLifecycleException(e.getMessage(), e);
        }
    }

    public void onStartup(ServerContext sc) throws ServerLifecycleException {
        try {
            setupClientSide();
            if (isEnabled) {
                this.cs = driver.startConnectorServer();
                // start the connector server for third party jmx clients like
                // JConsole on a thread
                new Thread(
                    new Runnable() {
                        public void run() {
                            try {
                                jconsolecs = driver.startJconsoleConnectorServer();
                            } catch (IOException ex) {
                                sLogger.info("rjmx.connector_server.failed_startup");
                            }
                        }
                } ).start();
            }
            else {
                final String msg = "JmxConnectorLifeCycle.onStartup: Connector Server not enabled at port: " + connectorConfig.getPort();
                sLogger.fine(msg);
            }
        } catch (Exception e) {
            throw new ServerLifecycleException(e.getMessage(), e);
        }
    }

    public void onReady(ServerContext sc) throws ServerLifecycleException {
    }
    public void onShutdown() throws ServerLifecycleException {
        try {
            if (isEnabled) {
                driver.stopConnectorServer(cs);
                driver.stopConnectorServer(jconsolecs);
            }
            else {
                final String msg = "JmxConnectorLifeCycle.onShutdown: Connector Server not enabled at port: " + connectorConfig.getPort() + ", its shutdown is not required";
                sLogger.fine(msg);
            }
        }
        catch (final Exception e) {
            throw new ServerLifecycleException(e.getMessage());
        }
    }

    private MBeanServer getAssociatedMBS() {
        /*
        final String returnAllMBS = null;
        final ArrayList list = MBeanServerFactory.findMBeanServer(returnAllMBS);
        if (list.isEmpty())
                throw new RuntimeException("Initialize the MBeanServers first...");
        return (MBeanServer)list.get(0); //for now
         */
        return ( MBeanServerFactory.getMBeanServer() );
    }

    private Map getEnvironment() {
        final Map env = new HashMap();
        return ( env );
    }

    public void onTermination() throws ServerLifecycleException {
    }

    private void initConnectorConfig() throws ConfigException {
        //This is the AdminService config bean
        AdminService as = ServerBeansFactory.getConfigBean(initContext.getConfigContext()).
                  getAdminService();
        connectorConfig = as.getJmxConnectorByName(as.getSystemJmxConnectorName());
        if (connectorConfig.isEnabled()) {
            this.isEnabled = true;
        }
    }
    
    private JMXAuthenticator createJMXAuthenticator() {
        final ASJMXAuthenticator authenticator = new ASJMXAuthenticator();
        // TODO: If domain.xml is not present, can not configure authentication        
        authenticator.setRealmName(connectorConfig.getAuthRealmName());        
        authenticator.setLoginDriver(new ASLoginDriverImpl());
        return authenticator;
    }

    private void configureJmxConnectorServerDriver() throws ServerLifecycleException {        
        driver.setAuthentication(true);
        driver.setAuthenticator(createJMXAuthenticator());        
        driver.setLogger(this.sLogger);
        driver.setMBeanServer(this.getAssociatedMBS());
        driver.setRmiRegistrySecureFlag(new Boolean(System.getProperty(RmiTweaks.SECURE_RMI_REGISTRY)).booleanValue());
        try {
            driver.setProtocol(RemoteJmxProtocol.instance(connectorConfig.getProtocol()));
            driver.setPort(Integer.parseInt(connectorConfig.getPort()));
            driver.setBindAddress(connectorConfig.getAddress());
            handleSsl();
        }
        catch (final Exception e) {
            throw new ServerLifecycleException(e.getMessage());
        }
    }      
    
    /** Handles the enabled flag on system-jmx-connector. On dev profile, it is okay that this
     *  flag is set to false. But for other profiles, it has to be true as the inter server 
     *  communication depends on it.
     */
    private void handleIsEnabled(final String port) throws ServerLifecycleException {
        /* Implementation note: This could have been handled using the Pluggable
         * Feature Factory, but since there isn't much pluggable behavior, I am going to
         * rely on the system property. In general, this should not be done.
         */
        if (isEE() && !isEnabled) {
            //EE and not enabled is not fine
            final String msg = sm.getString("rjmx.lc.disabled_ee_na", port);
            throw new ServerLifecycleException(msg);
        }
        if (!isEE() && !isEnabled) {
            //PE and not enabled is fine - log and move on
            sLogger.log(Level.INFO, "rjmx.lc.not_enabled", port);
        }
        //other 2 cases are implicitly handled
    }
    
    private boolean isEE() {
        boolean isEE = false;
        final String eepffc = SystemPropertyConstants.CLUSTER_AWARE_FEATURE_FACTORY_CLASS;
        final String pn = PluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME;
        final String pv = System.getProperty(pn);
        if (eepffc.equals(pv)) {
            isEE = true;
        }
        return ( isEE );
    }
    private void handleSupportedProtocol() throws ServerLifecycleException {
        final String pfc = connectorConfig.getProtocol();
        if (RemoteJmxProtocol.RMIJRMP != RemoteJmxProtocol.instance(pfc)) {
            final String port = connectorConfig.getPort();
            final String setP = connectorConfig.getProtocol();
            final String supportedP = RemoteJmxProtocol.RMIJRMP.getName();
            final String msg = sm.getString("rjmx.lc.unsupported_protocol", port, setP, supportedP);
            throw new ServerLifecycleException (msg);
        }
    }
    private void handleSsl() {
        final boolean ssl = connectorConfig.isSecurityEnabled();
        
        RMIServerSocketFactory sf = null;
        if (ssl) {
            driver.setSsl(ssl);
            Ssl sslc = connectorConfig.getSsl();
            if (sslc == null) 
                sslc = initDefaultSslConfiguration();
            sf = new AdminSslServerSocketFactory(sslc, connectorConfig.getAddress());
            RMIClientSocketFactory cf = new AdminRMISSLClientSocketFactory();
            driver.setRmiClientSocketFactory(cf);
        } else sf = new RMIMultiHomedServerSocketFactory(connectorConfig.getAddress());
        driver.setRmiServerSocketFactory(sf);        
    }
    
    /** A method to set up the client side of the TLS connection. Here is the scenario: When
     * the system jmx connector is set up with TLS enabled, all the other server instances
     * need to have the RMIClientSocketFactory related environment. This method ensures that.
     * Since this method is called when the server end is being brought up with the startup, it
     * ensures that the setup happens early. This is also required in case of cascading where
     * the server instances have the TLS setup on the jmx-connectors that are started in their
     * life cycle. Even if the jmx connectors are not set up with TLS, it is okay to 
     * setup the client side. 
     */
    private void setupClientSide() throws ConfigException {
        String serverName = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        String certNickName = ServerHelper.getCertNickname(
            initContext.getConfigContext(), serverName);
        new ServerClientEnvSetter(certNickName).setup();
    }

    private Ssl initDefaultSslConfiguration() {
        Ssl ssl = new Ssl();
        ssl.setCertNickname(ServerHelper.DEFAULT_CERT_NICKNAME);
        ssl.setClientAuthEnabled(false);
        ssl.setSsl2Enabled(false);
        ssl.setSsl3Enabled(true);
        ssl.setTlsEnabled(true);
        ssl.setTlsRollbackEnabled(true);
        return ssl;
    }

    private static class RmiTweaks {
        final static String SECURE_RMI_REGISTRY    = "com.sun.aas.jsr160.SecureRmiRegistry";
    }
}
