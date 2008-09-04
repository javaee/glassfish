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

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Extract;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

import org.glassfish.api.Startup;
import org.glassfish.api.Async;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;

import java.util.List;

import java.lang.management.ManagementFactory;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import org.glassfish.admin.connector.rmi.JmxConnectorServerDriver;
import org.glassfish.admin.connector.rmi.RemoteJmxProtocol;

import javax.management.remote.JMXConnectorServer;
import java.io.IOException;
/**
    Responsible for starting JMXConnectors as configured.
 */
@Service
@Async
public final class ConnectorStartupService implements Startup, PostConstruct {
    private static void debug( final String s ) { System.out.println( "### " + s); }
    
    @Inject
    private MBeanServer     mMBeanServer;

    @Inject
    AdminService    mAdminService;
    
    public ConnectorStartupService()
    {
    }
            

    public void postConstruct()
    {
        if ( mMBeanServer != ManagementFactory.getPlatformMBeanServer() )
        {
            throw new IllegalStateException( "MBeanServer must be the Platform one" );
        }        
       final List<JmxConnector> configuredConnectors = mAdminService.getJmxConnector();
       //debug( "SystemJmxConnectorName: " + mAdminService.getSystemJmxConnectorName() );
       new ConnectorsStarterThread( mMBeanServer, configuredConnectors).start();
    }
    
    private static final class ConnectorsStarterThread extends Thread
    {
        private final List<JmxConnector> mConfiguredConnectors;
        private final MBeanServer mMBeanServer;
        
        public ConnectorsStarterThread(
            final MBeanServer mbs,
            final List<JmxConnector> configuredConnectors )
        {
            mMBeanServer = mbs;
            mConfiguredConnectors = configuredConnectors;
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

        private JMXConnectorServer startConnector( final JmxConnector conn )
            throws IOException
        {
            final String protocol = conn.getProtocol();
            final String address  = conn.getAddress();
            final int port        = Integer.parseInt(conn.getPort());
            final String authRealmName = conn.getAuthRealmName();
            final boolean securityEnabled = Boolean.parseBoolean(conn.getSecurityEnabled());
            
            Util.getLogger().fine( toString(conn) );
            
            final JmxConnectorServerDriver dr = new JmxConnectorServerDriver();
            dr.setMBeanServer(mMBeanServer);
            dr.setProtocol( RemoteJmxProtocol.instance(protocol) );
            dr.setPort( port );
            //dr.setddress( address );
            dr.setSsl( securityEnabled );
            dr.setAuthentication( false );
            dr.setRmiRegistrySecureFlag( false );
            final JMXConnectorServer  server = dr.startConnectorServer();
            
            return server;
        }

        public void run()
        {
            for( final JmxConnector c : mConfiguredConnectors )
            {
                if ( ! Boolean.parseBoolean(c.getEnabled()) )
                {
                     Util.getLogger().info( "JmxConnector " + c.getName() + " is disabled, skipping." );
                     continue;
                }

                try
                {
                    startConnector(c);
                }
                catch( final Throwable t )
                {
                    Util.getLogger().severe( "ERROR starting JMX connector: " + toString(c) + ": " + t);
                    //t.printStackTrace();
                }
            }

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











