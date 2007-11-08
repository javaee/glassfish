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
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Main driver class to get a (secure) mbean server connection 
 * to a sever instance.
 */
public class ConnectionRegistry {

    /**
     * Returns the singleton instance of this class.
     *
     * @return  singleton instance
     */
    public static ConnectionRegistry getInstance() {
        return _instance;
    }

    /**
     * Private constructor.
     */
    private ConnectionRegistry() {
        _connectors    = new Hashtable();
        _connectorInfo = new Hashtable();
    }

    /**
     * Returns a mbean server connection for the given server instance. 
     * Connection credentials must be set for this server before 
     * this method is called.
     *
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     * @return  mbean server connection
     * 
     * @throws  IOException if an i/o error
     */
    public MBeanServerConnection getConnection(String serverName, 
            String domainName) throws IOException {

        String instanceName = domainName + serverName;

        MBeanServerConnection connection = null;

        // checking with connector cache
        JMXConnector connector = (JMXConnector) _connectors.get(instanceName);

        // connection credential is not available for given server
        if (connector == null) {
            throw new IllegalArgumentException();
        }

        try {
            connection = connector.getMBeanServerConnection();

            // stale connection
            if (TrustAnyConnectionFactory.isStaleConnection(connection)) {
                // get rid of stale connection
                connection = refreshConnection(instanceName);
            }
        } catch (Exception e) {
            // try forcing a new connection
            connection = refreshConnection(instanceName);
        }

        return connection;
    }

    /**
     * Creates a new connection.
     *
     * @param  instanceName  fully qualified application server instance name. 
     *                       instanceName = domainName + serverName
     *
     * @return  a new mbean server connection
     *
     * @throws MalformedURLException if service url is not correct
     * @throws  IOException if an i/o error
     */
    private MBeanServerConnection refreshConnection(String instanceName) 
            throws MalformedURLException, IOException {

        LogDomains.getLogger().fine("Refreshing connection for server: " 
                                    + instanceName);

        Map info = (Map) _connectorInfo.get(instanceName);
        if (info == null) {
            throw new IllegalArgumentException();
        }

        ConnectorFactory factory = new ConnectorFactory();
        JMXConnector connector = factory.getConnector(info);

        // replace the old connector
        synchronized (this) {
            _connectors.put(instanceName, connector);
        }

        return connector.getMBeanServerConnection();
    }

    /**
     * Sets the connection credentials for a server instance.
     *
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     * @param  info  connection credentials
     * Mandatory params are the following:
     *   user - user id,
     *   password - password, 
     *   uri - jmx service url
     * 
     * @throws MalformedURLException if service url is not correct
     * @throws  IOException if an i/o error
     */
    public void setConnectionCredentials(String serverName, String domainName,
            Map info) throws MalformedURLException, IOException {

        String instanceName = domainName + serverName;

        LogDomains.getLogger().fine(
            "Setting connection credentials for server: " + instanceName);

        ConnectorFactory factory = new ConnectorFactory();
        JMXConnector connector = factory.getConnector(info);

        synchronized (this) {
            _connectors.put(instanceName, connector);
            _connectorInfo.put(instanceName, info);
        }
    }

    /**
     * Cleans up connection credentials for the given server instance. 
     * This method is called when a server instance is detected to 
     * be going down from the plugin.
     *
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     */
    public synchronized void removeConnection(String serverName, 
            String domainName) {

        String instanceName = domainName + serverName;

        LogDomains.getLogger().fine(
            "Removing connection for server: " + instanceName);

        _connectors.remove(instanceName);
        _connectorInfo.remove(instanceName);

        // remove certificate from certificate database
        X509TrustManagerImpl.removeCertificate(instanceName);
    }

    // ---- PRIVATE - VARIABLES -------------------------------
    private static Map _connectors    = null;
    private static Map _connectorInfo = null;
    private static final ConnectionRegistry _instance = new ConnectionRegistry();
}
