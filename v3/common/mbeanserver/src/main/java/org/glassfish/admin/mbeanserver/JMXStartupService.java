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

import org.jvnet.hk2.annotations.Service;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.ListenerNotFoundException;

import org.glassfish.api.Startup;
import org.glassfish.api.Async;

import org.glassfish.api.amx.BootAMXMBean;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.lang.management.ManagementFactory;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectionNotification;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import java.io.IOException;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.internal.api.AdminAccessController;

/**
    Responsible for creating the {@link BootAMXMBean}, and starting JMXConnectors,
    which will initialize (boot) AMX when a connection arrives.
 */
@Service
@Async
public final class JMXStartupService implements Startup, PostConstruct
{
    private static void debug(final String s)
    {
        System.out.println("### " + s);
    }

    @Inject
    private MBeanServer mMBeanServer;

    @Inject
    private AdminService mAdminService;

    @Inject
    private Habitat mHabitat;

    @Inject Events mEvents;

    @Inject
    volatile static AdminAccessController sAuthenticator;
    
    private volatile BootAMX mBootAMX;
    
    private volatile JMXConnectorsStarterThread mConnectorsStarterThread;

    public JMXStartupService()
    {
        mMBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    private final class ShutdownListener implements EventListener
    {
        public void event(EventListener.Event event)
        {
            if ( event.is(EventTypes.SERVER_SHUTDOWN) )
            {
                shutdown();
            }
        }
    }
    
    public void postConstruct()
    {
        mBootAMX = BootAMX.create(mHabitat, mMBeanServer);

        final List<JmxConnector> configuredConnectors = mAdminService.getJmxConnector();

        mConnectorsStarterThread = new JMXConnectorsStarterThread(mMBeanServer, configuredConnectors, mBootAMX);
        mConnectorsStarterThread.start();
        
        mEvents.register( new ShutdownListener() );
    }
    
    private void shutdown()
    {
       Util.getLogger().info("ConnectorStartupService: shutting down AMX and JMX");
       
       mConnectorsStarterThread.shutdown();
    }

    /*
    KEEP: this was a problem at one point, could be again
    private static boolean verifyBugFix( final int port)
    {
    boolean jndiWorking = false;

    try
    {
    final javax.naming.InitialContext ctx = new javax.naming.InitialContext();
    final Registry reg = LocateRegistry.getRegistry(port);
    ctx.bind("rmi://" + localhost() + ":" + port + "/test", reg);
    reg.lookup("test");
    jndiWorking = true;
    }
    catch( final java.rmi.NotBoundException e )
    {
    jndiWorking = false;
    }
    catch( Exception e )
    {
    // some other problem
    throw new RuntimeException(e);
    }
    return jndiWorking;
    }
     */

    /**
     * Listens for a connection on the connector server, and when made,
     * ensures that AMX has been started.
     */
    private static final class BootAMXListener implements NotificationListener
    {
        private final JMXConnectorServer mServer;

        private final BootAMXMBean mBooter;

        public BootAMXListener(final JMXConnectorServer server, final BootAMXMBean booter)
        {
            mServer = server;
            mBooter = booter;
        }

        public void handleNotification(final Notification notif, final Object handback)
        {
            if (notif instanceof JMXConnectionNotification)
            {
                final JMXConnectionNotification n = (JMXConnectionNotification) notif;
                if (n.getType().equals(JMXConnectionNotification.OPENED))
                {
                    Util.getLogger().info("ConnectorStartupService.BootAMXListener: connection made, booting AMX MBeans");
                    mBooter.bootAMX();

                    // job is done, stop listening
                    try
                    {
                        mServer.removeNotificationListener(this);
                        Util.getLogger().fine("ConnectorStartupService.BootAMXListener: AMX is booted, stopped listening");
                    }
                    catch (final ListenerNotFoundException e)
                    {
                        // should be impossible.
                        e.printStackTrace();
                    }
                }
            }
        }

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

    /**
        Thread that starts the configured JMXConnectors.
     */
    private static final class JMXConnectorsStarterThread extends Thread
    {
        private final List<JmxConnector> mConfiguredConnectors;

        private final MBeanServer mMBeanServer;

        private final BootAMX mAMXBooterNew;

        public JMXConnectorsStarterThread(
                final MBeanServer mbs,
                final List<JmxConnector> configuredConnectors,
                final BootAMX amxBooter)
        {
            mMBeanServer = mbs;
            mConfiguredConnectors = configuredConnectors;
            mAMXBooterNew = amxBooter;
        }
        
        void shutdown()
        {
            for( final JMXConnectorServer connector : mConnectorServers )
            {
                try
                {
                    final JMXServiceURL address = connector.getAddress();
                    connector.stop();
                    Util.getLogger().info("Stopped JMXConnectorServer: " + address);
                }
                catch( final Exception e )
                {
                    e.printStackTrace();
                }
            }
            mConnectorServers.clear();
        }

        private static String toString(final JmxConnector c)
        {
            return "JmxConnector config: { name = " + c.getName() +
                   ", Protocol = " + c.getProtocol() +
                   ", Address = " + c.getAddress() +
                   ", Port = " + c.getPort() +
                   ", AcceptAll = " + c.getAcceptAll() +
                   ", AuthRealmName = " + c.getAuthRealmName() +
                   ", SecurityEnabled = " + c.getSecurityEnabled() +
                   "}";
        }

        /**
        Start the JMX RMI connector server using rmi_jrmp protocol.
         */
        private final class RMIConnectorStarter
        {
            private final MBeanServer mMBeanServer;

            private final String mAddress;

            private final int mPort;

            private final String mProtocol;

            private final String mAuthRealmName;

            private final boolean mSecurityEnabled;

            private final Registry mRegistry;

            public RMIConnectorStarter(
                    final MBeanServer mbeanServer,
                    final String address,
                    final int port,
                    final String protocol,
                    final String authRealmName,
                    final boolean securityEnabled)
            {
                mMBeanServer = mbeanServer;
                mAddress = address;
                mPort = port;
                mProtocol = protocol;
                mAuthRealmName = authRealmName;
                mSecurityEnabled = securityEnabled;
                if (securityEnabled)
                {
                    throw new IllegalArgumentException("JMXConnectorServer not yet supporting security");
                }
                if (!"rmi_jrmp".equals(protocol))
                {
                    throw new IllegalArgumentException("JMXConnectorServer not yet supporting protocol: " + protocol);
                }

                mRegistry = startRegistry(mPort);
                /*
                if ( ! verifyBugFix(mPort) )
                {
                final String msg = "JNDI provider for JNDI URLs that look like rmi://host:port/name is not working. " +
                "The JMX RMI connector will not be accessible to clients, even if it appears to load correctly.  See issues #6025, 5637";
                Util.getLogger().warning( msg );
                }
                 */
            }

            public JMXConnectorServer startRMIConnector(final String name) throws IOException
            {
                final String hostname = Util.localhost();
                final Map<String, Object> env = new HashMap<String, Object>();
                
                //env.put( "jmx.remote.jndi.rebind", "true" );
                //env.put( "jmx.remote.credentials", null );
                if (sAuthenticator != null)
                {
                    env.put( "jmx.remote.authenticator", sAuthenticator );
                }
                // env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
                //env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());
                
                final boolean useSamePortForClients = false;
                String jmxServiceURL = null;
                if ( useSamePortForClients )
                {
                    // extended variant uses the same port for both the RMIRegistry and the client port
                    // see: http://blogs.sun.com/jmxetc/entry/connecting_through_firewall_using_jmx
                    jmxServiceURL = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + mPort + "/" + 
                        "/jndi/rmi://" + hostname + ":" + mPort + "/" + name;

                }
                else
                {
                    jmxServiceURL = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + mPort + "/" + name;
                }
                        
                final JMXServiceURL url = new JMXServiceURL(jmxServiceURL);

                final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mMBeanServer);
                final BootAMXListener listener = new BootAMXListener(cs, mAMXBooterNew);
                cs.addNotificationListener(listener, null, null);
                cs.start();

                return cs;
            }

        }

        private JMXConnectorServer startConnector(final JmxConnector connConfig)
                throws IOException
        {
            Util.getLogger().fine("Starting JMXConnector: " + toString(connConfig));

            final String protocol = connConfig.getProtocol();
            if (!protocol.equals("rmi_jrmp"))
            {
                throw new IllegalArgumentException("The only protocol supported is rmi_jrmp");
            }

            final String address = connConfig.getAddress();
            final int port = Integer.parseInt(connConfig.getPort());
            final String authRealmName = connConfig.getAuthRealmName();
            final boolean securityEnabled = Boolean.parseBoolean(connConfig.getSecurityEnabled());

            final RMIConnectorStarter starter = new RMIConnectorStarter(mMBeanServer, address, port, protocol, authRealmName, securityEnabled);

            final JMXConnectorServer server = starter.startRMIConnector("jmxrmi");
            final JMXServiceURL url = server.getAddress();

            Util.getLogger().info("Started JMXConnector, JMXService URL = " + url);

            try
            {
                ObjectName objectName = new ObjectName("jmxremote:type=jmx-connector,name=jmxrmi");
                objectName = mMBeanServer.registerMBean(server, objectName).getObjectName();
            }
            catch (final Exception e)
            {
                // it's not critical to have it registered as an MBean
                e.printStackTrace();
            }

            // test that it works
            /*
            final JMXConnector conn = JMXConnectorFactory.connect(url);
            final MBeanServerConnection mbsc = conn.getMBeanServerConnection();
            mbsc.getDomains();
             */

            return server;
        }

        private final List<JMXConnectorServer> mConnectorServers = new ArrayList<JMXConnectorServer>();

        public void run()
        {
            // JmxConnectorServerDriver.testStart(8686, "rmi_jrmp", true );

            Util.getLogger().fine("Number of JMX connectors: " + mConfiguredConnectors.size());

            for (final JmxConnector c : mConfiguredConnectors)
            {
                if (!Boolean.parseBoolean(c.getEnabled()))
                {
                    Util.getLogger().info("JmxConnector " + c.getName() + " is disabled, skipping.");
                    continue;
                }

                try
                {
                    final JMXConnectorServer server = startConnector(c);
                    mConnectorServers.add(server);
                }
                catch (final Throwable t)
                {
                    Util.getLogger().warning("Cannot start JMX connector: " + toString(c) + ": " + t);
                    //t.printStackTrace();
                }
            }

            if (Boolean.valueOf(System.getProperty("START_JMXMP")))
            {
                startJMXMPConnector();
            }
        }

        /**
        Retain this code, it is used for testing/verification.
         */
        private void startJMXMPConnector()
        {
            // this is for JMXMP, remove soon, use the config mechanism above
            final JMXMPConnectorStarter jmxmpStarter = new JMXMPConnectorStarter(mMBeanServer);
            try
            {
                final JMXConnectorServer server = jmxmpStarter.start();
                mConnectorServers.add( server );
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

    }

    public Startup.Lifecycle getLifecycle()
    {
        return Startup.Lifecycle.SERVER;
    }
}











