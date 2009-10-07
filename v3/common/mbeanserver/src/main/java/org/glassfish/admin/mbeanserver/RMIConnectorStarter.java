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
package org.glassfish.admin.mbeanserver;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.MBeanServer;


import java.util.Map;
import java.util.HashMap;

import javax.management.remote.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import org.jvnet.hk2.component.*;

/**
Start the JMX RMI connector server using rmi_jrmp protocol.
 */
final class RMIConnectorStarter extends ConnectorStarter
{
    private final Registry mRegistry;

    public RMIConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String protocol,
        final String authRealmName,
        final boolean securityEnabled,
        final Habitat habitat,
        final BootAMXListener bootListener)
    {
        super(mbeanServer, address, port, authRealmName, securityEnabled, habitat, bootListener);

        if (!"rmi_jrmp".equals(protocol))
        {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting protocol: " + protocol);
        }

        mRegistry = startRegistry(mPort);
    }


    private static Registry startRegistry(final int port)
    {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");

        try
        {
            return LocateRegistry.createRegistry(port);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Port " + port + " is not available for the internal rmi registry. " +
                "This means that a call was made with the same port, without closing earlier " +
                "registry instance. This has to do with the system jmx connector configuration " +
                "in admin-service element of the configuration associated with this instance");
        }
    }


    public JMXConnectorServer start() throws MalformedURLException, IOException
    {
        final String name = "jmxrmi";
        
        final String hostname = Util.localhost();
        final Map<String, Object> env = new HashMap<String, Object>();

        //env.put( "jmx.remote.jndi.rebind", "true" );
        //env.put( "jmx.remote.credentials", null );
        JMXAuthenticator authenticator = getAccessController();
        if (authenticator != null)        
        {
            env.put("jmx.remote.authenticator", authenticator);
        }
        // env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
        //env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());
        final String hostPort = hostname + ":" + mPort;

        // !!!
        //  extended JMXServiceURL  uses the same port for both the RMIRegistry and the client port
        // see: http://blogs.sun.com/jmxetc/entry/connecting_through_firewall_using_jmx
        //
        // the first hostPort value is the host/port to be used for the client connections; this makes it a fixed
        // port number and we're making it the same as the RMI registry port.
        final String urlStr = "service:jmx:rmi://" + hostPort + "/jndi/rmi://" + hostPort + "/" + name;
        //final String urlStr = "service:jmx:rmi:///jndi/rmi://" + hostPort + "/" + name;  <== KEEP for reference, this is the basic form

        mJMXServiceURL = new JMXServiceURL(urlStr);
        mConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(mJMXServiceURL, env, mMBeanServer);
        if ( mBootListener != null )
        {
            mConnectorServer.addNotificationListener(mBootListener, null, mJMXServiceURL.toString() );
        }
        mConnectorServer.start();

        return mConnectorServer;
    }
}












