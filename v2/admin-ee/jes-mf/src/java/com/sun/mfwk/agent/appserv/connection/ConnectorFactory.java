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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.connection;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HandshakeCompletedListener;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.appserv.management.client.AdminRMISSLClientSocketFactoryEnvImpl;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Factory class to create mbean server connection.
 */
class ConnectorFactory {

    /**
     * Constructor.
     */
    ConnectorFactory() { }

    /**
     * Returns a jmx connector for the given information.
     *
     * @param  info  map containing connection information
     * Mandatory params are the following:
     *   user - user id,
     *   password - password, 
     *   uri - jmx service url
     *
     * @return a mbean server connection
     */
    JMXConnector getConnector(Map info) 
            throws MalformedURLException, IOException {

        // service url
        String uri = (String) info.get(Constants.URI_KEY);

        // user 
        String user = (String) info.get(Constants.USER_KEY);

        // password
        String password = (String) info.get(Constants.PASSWORD_KEY);

        // validate arguments
        if ( (uri==null) || (user==null) || (password==null) ) {
            throw new IllegalArgumentException();
        }

        JMXServiceURL url = new JMXServiceURL(uri);
        Map env = new HashMap(); 
        env.put(JMXConnector.CREDENTIALS, new String[] {user, password});

        // setup TLS environment
        String serverName = (String) info.get(Constants.SERVER_KEY);
        Certificate cert = (Certificate) info.get(Constants.CERT_KEY);
        setupForTls(serverName, (X509Certificate) cert);

        LogDomains.getLogger().fine(
            "Creating connector for server: " + serverName);

        JMXConnector connector = JMXConnectorFactory.connect(url, env);

        return connector;
    }

    /**
     * Sets up environment for TLS connection.
     * 
     * @param  serverName  application server name
     * @param  cert  certificate from discovery msg
     */
    private static void setupForTls(String serverName, X509Certificate cert) {
        
        if ( (serverName == null) || (cert == null) ) {
            return;
        }

        LogDomains.getLogger().fine(
            "Setting up TLS for server: " + serverName);

        AdminRMISSLClientSocketFactoryEnvImpl env = 
            AdminRMISSLClientSocketFactoryEnvImpl.getInstance();

        // adds the the new certificate to the in-memory cert database
        TrustManager[] newTrustMgrs = 
              new TrustManager[] {new X509TrustManagerImpl(serverName, cert)};
        env.setTrustManagers(newTrustMgrs);

        HandshakeCompletedListener listener = 
            new HandshakeCompletedListenerImpl();
        env.setHandshakeCompletedListener(listener);
    }
}
