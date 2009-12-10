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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;



import java.util.Map;
import java.util.HashMap;

import javax.management.remote.*;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import javax.security.auth.Subject;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import org.jvnet.hk2.component.*;

/**
Start the JMX RMI connector server using rmi_jrmp protocol.
 */
final class RMIConnectorStarter extends ConnectorStarter
{
    private final Registry mRegistry;
    private final boolean   mBindToSingleIP;
    private volatile MyRMIJRMPServerImpl  mMyServer;
    
    /** will be null if we don't need it */
    private final MyRMIServerSocketFactory  mServerSocketFactory;
    
    public RMIConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String protocol,
        final String authRealmName,
        final boolean securityEnabled,
        final Habitat habitat,
        final BootAMXListener bootListener) throws UnknownHostException
    {
        super(mbeanServer, address, port, authRealmName, securityEnabled, habitat, bootListener);

        if (!"rmi_jrmp".equals(protocol))
        {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting protocol: " + protocol);
        }
        
        final boolean ENABLED = true;
        mBindToSingleIP = ENABLED && ! ( address.equals("0.0.0.0") || address.equals("") );

        final InetAddress inetAddr = getAddress(address);
        mServerSocketFactory = mBindToSingleIP ? new MyRMIServerSocketFactory( inetAddr ) : null;
        mRegistry = startRegistry( address, mPort);
    }
    
    private static InetAddress getAddress(final String addrSpec) throws UnknownHostException
    {
        String actual = addrSpec;
        if ( addrSpec.equals("localhost" ) )
        {
            actual = "127.0.0.1";
        }
        
        final InetAddress addr = InetAddress.getByName(actual);
        return addr;
    }
    
    public static final String RMI_HOSTNAME_PROP = "java.rmi.server.hostname";
    
    
    static String setupRMIHostname(final String host) {
        return System.setProperty( RMI_HOSTNAME_PROP, host );
    }
        
    private static void restoreRMIHostname(final String saved, final String expectedValue)
    {
        if ( saved == null ) {
            System.clearProperty(RMI_HOSTNAME_PROP);
        }
        else {
            final String temp = System.setProperty( RMI_HOSTNAME_PROP, saved);
            // check that it didn't change since the last setup
            if ( ! temp.equals(expectedValue) ) {
                throw new IllegalStateException( "Something changed " + RMI_HOSTNAME_PROP + " to " + temp );
            }
        }
    }
    
    public static final class MyRMIServerSocketFactory extends RMISocketFactory
    {
        private final InetAddress mAddress;
        
        public MyRMIServerSocketFactory(final InetAddress addr)
        {
            mAddress = addr;
        }
        
        public ServerSocket	createServerSocket(int port) throws IOException
        {
            //debug( "MyRMIServerSocketFactory.createServerSocket(): " + mAddress + " : " + port );
            final int backlog = 5;  // plenty
            final ServerSocket s = new ServerSocket(port, backlog, mAddress );
            //debug( "MyRMIServerSocketFactory.createServerSocket(): " + mAddress + " : " + port );
            return s;
        }
        
        /** shouldn't be called */
        public Socket	createSocket(String host, int port) throws IOException
        {
            //debug( "MyRMIServerSocketFactory.createSocket(): " + host + " : " + port );
            throw new IllegalStateException("MyRMIServerSocketFactory.createSocket");
        }
    }
    
    /**
        Purpose: to ensure binding to a specific IP address instead fo all IP addresses.
     */
    private static final class MyRMIJRMPServerImpl extends RMIJRMPServerImpl {
        private final String mBindToAddr;
        
        public MyRMIJRMPServerImpl(
            final int port,
            final Map<String,?> env,
            final RMIServerSocketFactory serverSocketFactory,
            final String bindToAddr ) throws IOException
        {
            super( port, null, serverSocketFactory, env);
            
            mBindToAddr = bindToAddr;
        }
        
        /** must be 'synchronized': threads can't save/restore the same system property concurrently */
            protected synchronized void
        export(final String host) throws IOException {
            final String saved = setupRMIHostname( mBindToAddr );
            try {
                super.export();
                System.out.println( "MyRMIJRMPServerImpl: exported on address " + mBindToAddr);
            }
            finally {
                restoreRMIHostname(saved, mBindToAddr);
            }
        }
        
        /** must be 'synchronized': threads can't save/restore the same system property concurrently */
            protected synchronized RMIConnection
        makeClient(final String connectionId, final Subject subject)  throws IOException {
            final String saved = setupRMIHostname( mBindToAddr );
            try {
                Util.getLogger().fine( "MyRMIJRMPServerImpl: makeClient on address = " + System.getProperty(RMI_HOSTNAME_PROP) );
                return super.makeClient( connectionId, subject);
            }
            finally {
                restoreRMIHostname(saved, mBindToAddr);
            }
        }
    }
    
    private static void debug( final Object o )
    {
        System.out.println( "" + o );
    }

    private Registry startRegistry(final String addr, final int port) {
        Registry registry = null;
        
        if ( mBindToSingleIP ) {
            //System.out.println( RMI_HOSTNAME_PROP + " before: " + System.getProperty(RMI_HOSTNAME_PROP) );
            final String saved = setupRMIHostname( addr );
            try {
                Util.getLogger().info( "Binding RMI port to single IP address = " + System.getProperty(RMI_HOSTNAME_PROP) + ", port " + port);
                registry = _startRegistry(port);
            }
            finally {
                restoreRMIHostname(saved, addr);
            }
        }
        else {
             Util.getLogger().info( "Binding RMI port to *:" + port );
            registry = _startRegistry(port);
        }
        return registry;
    }

    private Registry _startRegistry(final int port)
    {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");
        try
        {
            return LocateRegistry.createRegistry(port, null, mServerSocketFactory );
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
        
        final String hostname = hostname();
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
        if ( mBindToSingleIP )
        {
            final RMIClientSocketFactory csf = null;
            final RMIServerSocketFactory ssf = null;
            
            mMyServer = new MyRMIJRMPServerImpl( mPort, env, mServerSocketFactory, mAddress);

            mConnectorServer = new RMIConnectorServer( mJMXServiceURL, env, mMyServer, mMBeanServer);
        }
        else
        {
            mConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(mJMXServiceURL, env, mMBeanServer);
        }
        
        if ( mBootListener != null )
        {
            mConnectorServer.addNotificationListener(mBootListener, null, mJMXServiceURL.toString() );
        }
        mConnectorServer.start();

        return mConnectorServer;
    }
}












