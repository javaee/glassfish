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

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;

import java.util.List;
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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;


/**
    Responsible for starting AMXBooter initialization, and starting JMXConnectors as configured.
 */
@Service
@Async
public final class ConnectorStartupService implements Startup, PostConstruct {
    private static void debug( final String s ) { System.out.println( "### " + s); }
    
    @Inject
    private MBeanServer     mMBeanServer;

    @Inject
    AdminService    mAdminService;
    
    @Inject
    Habitat mHabitat;
    
    private volatile BooterOld mOldBooter;
    private volatile BooterNew mNewBooter;

    public ConnectorStartupService()
    {
    }
            
    public void postConstruct()
    {
        if ( mMBeanServer != ManagementFactory.getPlatformMBeanServer() )
        {
            throw new IllegalStateException( "MBeanServer must be ManagementFactory.getPlatformMBeanServer()" );
        }
        
        mOldBooter = BooterOld.create( mHabitat, mMBeanServer );
        mNewBooter = BooterNew.create( mHabitat, mMBeanServer );
    
        final List<JmxConnector> configuredConnectors = mAdminService.getJmxConnector();
        
        final ConnectorsStarterThread starter = new ConnectorsStarterThread( mMBeanServer, configuredConnectors, mOldBooter, mNewBooter);
        starter.start();
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
    
    private static final class BootAMXListener implements NotificationListener
    {
        private final JMXConnectorServer mServer;
        private final Booter             mBooter;
        
        public BootAMXListener(final JMXConnectorServer server, final Booter booter)
        {
            mServer = server;
            mBooter = booter;
        }
        
        public void handleNotification( final Notification notif, final Object handback )
        {
            if ( notif instanceof JMXConnectionNotification )
            {
                final JMXConnectionNotification n = (JMXConnectionNotification)notif;
                if ( n.getType().equals(JMXConnectionNotification.OPENED) )
                {
                    Util.getLogger().info( "ConnectorStartupService.BootAMXListener: connection made, booting AMX MBeans" );
                    mBooter.bootAMX();
                    
                    // job is done, stop listening
                    try
                    {
                        mServer.removeNotificationListener(this);
                        Util.getLogger().fine( "ConnectorStartupService.BootAMXListener: AMX is booted, stopped listening" );
                    }
                    catch( final ListenerNotFoundException e )
                    {
                        // should be impossible.
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private static Registry startRegistry(final int port) {
        try {
            return LocateRegistry.createRegistry(port);
        }
        catch (final Exception e) {
            throw new RuntimeException("Port " + port + " is not available for the internal rmi registry. " + 
                "This means that a call was made with the same port, without closing earlier " +
                "registry instance. This has to do with the system jmx connector configuration " +
                "in admin-service element of the configuration associated with this instance" );
        }
    }
    
    
    private static final class ConnectorsStarterThread extends Thread
    {
        private final List<JmxConnector> mConfiguredConnectors;
        private final MBeanServer mMBeanServer;
        private final BooterOld      mAMXBooterOld;
        private final BooterNew      mAMXBooterNew;
        
        public ConnectorsStarterThread(
            final MBeanServer mbs,
            final List<JmxConnector> configuredConnectors,
            final BooterOld amxBooterOld,
            final BooterNew amxBooterNew)
        {
            mMBeanServer = mbs;
            mConfiguredConnectors = configuredConnectors;
            mAMXBooterOld = amxBooterOld;
            mAMXBooterNew = amxBooterNew;
        }
        
        private static String toString( final JmxConnector c )
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
            private final int    mPort;
            private final String mProtocol;
            private final String mAuthRealmName;
            private final boolean mSecurityEnabled;
            private final Registry mRegistry;
            
            public RMIConnectorStarter(
                final MBeanServer mbeanServer,
                final String address,
                final int    port,
                final String protocol,
                final String authRealmName,
                final boolean securityEnabled
                )
            {
                mMBeanServer  = mbeanServer;
                mAddress      = address;
                mPort          = port;
                mProtocol      = protocol;
                mAuthRealmName = authRealmName;
                mSecurityEnabled = securityEnabled;
                if ( securityEnabled )
                {
                    throw new IllegalArgumentException( "JMXConnectorServer not yet supporting security" );
                }
                if ( ! "rmi_jrmp".equals(protocol) )
                {
                    throw new IllegalArgumentException( "JMXConnectorServer not yet supporting protocol: "  + protocol );
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

            public JMXConnectorServer startRMIConnector( final String name) throws IOException
            {
                final Map<String,Object> env = new HashMap<String,Object>();
                //env.put( "jmx.remote.jndi.rebind", "true" );
                //env.put( "jmx.remote.credentials", null );
                //env.put( "jmx.remote.authenticator", null );
               // env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol"); 
                //env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());
                
                final String s = "service:jmx:rmi:///jndi/rmi://" + Util.localhost() + ":" + mPort  + "/" + name;
                final JMXServiceURL url = new JMXServiceURL( s );
                
                final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer( url, env, mMBeanServer);
                final BootAMXListener listener1 = new BootAMXListener(cs, mAMXBooterOld);
                final BootAMXListener listener2 = new BootAMXListener(cs, mAMXBooterNew);
                cs.addNotificationListener( listener1, null, null);
                cs.addNotificationListener( listener2, null, null);
                cs.start();
                
                return cs;
            }
        }

        private JMXConnectorServer startConnector( final JmxConnector connConfig )
            throws IOException
        {
            Util.getLogger().fine( "Starting JMXConnector: " + toString(connConfig) );
            
            final String protocol = connConfig.getProtocol();
            if ( ! protocol.equals("rmi_jrmp" ) )
            {
                throw new IllegalArgumentException( "The only protocol supported is rmi_jrmp" );
            }
            
            final String address  = connConfig.getAddress();
            final int port        = Integer.parseInt(connConfig.getPort());
            final String authRealmName = connConfig.getAuthRealmName();
            final boolean securityEnabled = Boolean.parseBoolean(connConfig.getSecurityEnabled());
            
            final RMIConnectorStarter starter = new RMIConnectorStarter( mMBeanServer, address, port, protocol, authRealmName, securityEnabled );
            
            final JMXConnectorServer server = starter.startRMIConnector("jmxrmi");
            final JMXServiceURL url = server.getAddress();
           
            Util.getLogger().info( "Started JMXConnector, JMXService URL = " + url );
            
            try
            {
                ObjectName objectName =  new ObjectName( "jmxremote:type=jmx-connector,name=jmxrmi" );
                objectName = mMBeanServer.registerMBean( server, objectName).getObjectName();
            }
            catch ( final Exception e )
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
        
        public void run()
        {
           // JmxConnectorServerDriver.testStart(8686, "rmi_jrmp", true );

            Util.getLogger().fine( "Number of JMX connectors: " + mConfiguredConnectors.size() );
            
            for( final JmxConnector c : mConfiguredConnectors )
            {
                if ( ! Boolean.parseBoolean(c.getEnabled()) )
                {
                     Util.getLogger().info( "JmxConnector " + c.getName() + " is disabled, skipping." );
                     continue;
                }
                
                try
                {
                    final JMXConnectorServer server = startConnector(c);
                }
                catch( final Throwable t )
                {
                    Util.getLogger().warning( "Cannot start JMX connector: " + toString(c) + ": " + t);
                    //t.printStackTrace();
                }
            }

            if ( Boolean.valueOf( System.getProperty("START_JMXMP") ))
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
            final JMXMPConnectorStarter jmxmpStarter = new JMXMPConnectorStarter( mMBeanServer);
            try
            {
                jmxmpStarter.start();
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
        }
    }
    
    public Startup.Lifecycle getLifecycle() { return Startup.Lifecycle.SERVER; }
}











