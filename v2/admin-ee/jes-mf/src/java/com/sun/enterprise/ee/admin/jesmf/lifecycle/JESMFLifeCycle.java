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
package com.sun.enterprise.ee.admin.jesmf.lifecycle;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import javax.management.remote.JMXServiceURL;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;

import java.security.KeyStore;
import java.security.cert.Certificate;
import com.sun.enterprise.security.SecuritySupportImpl;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import java.security.KeyStoreException;
import com.sun.enterprise.config.ConfigException;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import java.util.logging.Level; 

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Lifecycle manager for JES MF administration service.
 * 
 * @author Nazrul Islam
 * @since  J2SE 5.0
 */
public class JESMFLifeCycle extends ServerLifecycleImpl {

    /**
     * Constant denoting the status of ws mgmt admin service not started
     */
    public static final byte STATUS_NOT_STARTED = 0;

    /**
     * Constant denoting the status of ws mgmt admin service shutdown started
     */
    public static final byte STATUS_SHUTDOWN = 1;

    /**
     * Constant denoting the status of ws mgmt admin service initialized
     */
    public static final byte STATUS_INITED = 2;

    /**
     * Constant denoting the status of ws mgmt admin service started
     */
    public static final byte STATUS_STARTED = 4;

    /**
     * Constant denoting the status of ws mgmt admin service ready
     */
    public static final byte STATUS_READY = 8;

    /**
     * Constant denoting the status of ws mgmt admin service terminated
     */
    public static final byte STATUS_TERMINATED = 0;

    private byte serverStatus;

    /**
     * Default constructor
     */
    public JESMFLifeCycle() {
        serverStatus = STATUS_NOT_STARTED;
    }

    /**
     * Server is initializing ws mgmt admin service and setting up the runtime 
     * environment.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc)
            throws ServerLifecycleException {

        if ((serverStatus & STATUS_INITED) == STATUS_INITED) {
            throw new IllegalStateException(
                "JES MF is already initialized");
        }
        serverStatus = STATUS_INITED;
    }

    /**
     * Server is starting up applications.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc) throws ServerLifecycleException {
        serverStatus |= STATUS_STARTED;
    }

    /**
     * Server has completed loading the services and is ready to serve requests.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) throws ServerLifecycleException {

        serverStatus |= STATUS_READY;

        try {
            // server name
            String instanceName = sc.getInstanceName();

            // config context
            ConfigContext cCtx = sc.getConfigContext();

            // jmx connector information
            JMXConnectorConfig jcc = 
                ServerHelper.getJMXConnectorInfo(cCtx, instanceName);

            // jmx service url
            JMXServiceURL url = JmxServiceUrlFactory.forRmiWithJndiInAppserver(
                jcc.getHost(), Integer.parseInt(jcc.getPort())); 

            // domain name
            String domainName = 
                ServerHelper.getAdministrativeDomainName(cCtx, instanceName);

            // true when server is DAS
            boolean isDAS = ServerHelper.isDAS(cCtx, instanceName);

            // user data
            Hashtable userData = new Hashtable();
            userData.put(HOST_KEY, jcc.getHost());
            userData.put(PORT_KEY, jcc.getPort());
            userData.put(USER_KEY, jcc.getUser());
            userData.put(PASSWORD_KEY, jcc.getPassword());
            userData.put(PROTOCOL_KEY, jcc.getProtocol());
            userData.put(SystemPropertyConstants.SERVER_NAME, instanceName);
            userData.put(DOMAIN_NAME_KEY, domainName);
            userData.put(IS_DAS_KEY, new Boolean(isDAS));

            // jmx system connector certificate
            Certificate cert = getJmxConnectorCert(cCtx, instanceName);
            userData.put(CERT_KEY, cert);

            // serialized user data
            byte[] data = serializeUserData(userData);

            // MfDiscoveryResponder class
            Class c = Class.forName(
                "com.sun.mfwk.discovery.MfDiscoveryResponder");

            // MfDiscoveryResponder constructor
            Constructor con = c.getConstructor(new Class[] {
                java.lang.String.class,
                java.lang.String.class,
                java.lang.String.class,
                java.lang.String.class,
                java.lang.String.class,
                data.getClass(),
                Boolean.TYPE});

            // MfDiscoveryResponder object
            Object responder = con.newInstance(new Object[] {
                PRODUCT_NAME, PRODUCT_CODE_NAME, PRODUCT_PREFIX, 
                instanceName, url.toString(), data, Boolean.valueOf(true)});

            _logger.log(Level.INFO, "jesmf.discovery.init");

        } catch (ClassNotFoundException cnfe) {
            // MfDiscoveryResponder is not in the classpath
            _logger.log(Level.FINE, "com.sun.mfwk.discovery.MfDiscoveryResponder is not in the server classpath", cnfe);

        } catch (Exception e) {
            // error initializing jes mf responder
            _logger.log(Level.FINE, "Error while initializing MfDiscoveryResponder", e);
        }
    }

    /**
     * Server is shutting down applications.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() throws ServerLifecycleException {
        serverStatus |= STATUS_SHUTDOWN;
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem. This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() throws ServerLifecycleException {
        serverStatus = STATUS_TERMINATED;
    }

    /**
     * Returns the certificate nick name. 
     *
     * @param   ctx  config context
     * @param   instanceName   sever instance name
     * 
     * @return  certificate nick name
     * @throws  ConfigException  if system jmx connector is not defined
     */
    private String getCertNickname(ConfigContext ctx, String instanceName) 
            throws ConfigException {

        String certNickname = null;
        JmxConnector con = 
            ServerHelper.getServerSystemConnector(ctx, instanceName);
        if (con != null) {
            Ssl ssl = con.getSsl();
            if (ssl != null) {
                certNickname = ssl.getCertNickname();
            }
        }
        if (certNickname == null) {
            certNickname = DEF_CERT_ALIAS;
        }

        return certNickname;
    }

    /**
     * Returns certificate used by jmx connector.
     *
     * @param   ctx  config context
     * @param   instanceName   sever instance name
     *
     * @return  jmx connector certificate
     *
     * @throws  KeyStoreException  if keystore has not been initialized
     * @throws  ConfigException  if system jmx connector is not defined
     */
    private Certificate getJmxConnectorCert(ConfigContext ctx, 
            String instanceName) throws KeyStoreException, ConfigException {

        Certificate cert = null;

        // certificate alias name
        String certNickname = getCertNickname(ctx, instanceName);

        // available trust stores
        SecuritySupportImpl ssi = new SecuritySupportImpl();
        KeyStore[] trustStore = ssi.getTrustStores();
        
        for (int i=0; i<trustStore.length; i++) {
            cert = trustStore[i].getCertificate(certNickname);
            if (cert != null) {
                // found target
                break;
            }
        }
        return cert;
    }

    /**
     * Returns the serialized user data. 
     *
     * @param  obj  object to be serialized
     *
     * @return serialized user data
     *
     * @throws NotSerializableException if object is not serializable
     * @throws IOException  if an i/o error while writing the object
     */
    private byte[] serializeUserData(Object obj) 
            throws NotSerializableException, IOException {

        byte[] data = null;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            data = bos.toByteArray();

        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            } catch(Exception ex) {}
        }

        return data;
    }

    // ---- VARIABLES - PRIVATE ------------------------------------
    private static final String PRODUCT_NAME       = "ApplicationServer";
    private static final String PRODUCT_CODE_NAME  = "com.sun.cmm.as";
    private static final String PRODUCT_PREFIX     = "as";
    private static final String HOST_KEY           = "host";
    private static final String PORT_KEY           = "port";
    private static final String USER_KEY           = "user";
    private static final String PASSWORD_KEY       = "password";
    private static final String PROTOCOL_KEY       = "protocol";
    private static final String CERT_KEY           = "certificate";
    private static final String DOMAIN_NAME_KEY    = "domain";
    private static final String IS_DAS_KEY         = "isDAS";
    private static final String DEF_CERT_ALIAS     = "s1as";
    private static Logger _logger = Logger.getLogger(
                            EELogDomains.EE_ADMIN_LOGGER);
}
