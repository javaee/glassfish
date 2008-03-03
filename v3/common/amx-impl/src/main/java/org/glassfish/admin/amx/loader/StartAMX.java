/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.amx.loader;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.TimingDelta;

import org.glassfish.admin.amx.support.LoadAMX;
//import org.glassfish.admin.amx.types.XTypesMapper;
//import org.glassfish.admin.amx.types.J2EETypesMapper;
//import org.glassfish.admin.amx.types.AllTypesMapper;

import java.net.MalformedURLException;
import java.io.IOException;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnector;


//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;

import javax.management.remote.jmxmp.JMXMPConnectorServer;

import org.glassfish.admin.amx.loader.AMXConfigRegistrar;


/**
    Initialize AMX:<br/>
    <ul>
    <li>Start the MBeanServer if not already started/li>
    <li>Start the AMX loader which causes AMX MBeans to be registered</li>
    <li>Start the JMXMP connector</li>
    </ul>
 */
public final class StartAMX
{
    protected static void debug( final String s ) { System.out.println(s); }
    
    private static StartAMX INSTANCE;
    
    private volatile ObjectName mAMXLoaderObjectName;
    private final AMXConfigRegistrar mConfigRegistrar;
    private final MBeanServer   mMBeanServer;
    
   // private volatile Registry mRmiRegistry= null;
   
    // these 3 fields are initialized later
    private volatile JMXConnectorServer mJMXMP = null;
    private volatile JMXServiceURL  mJMXMPServiceURL = null;
    private volatile ObjectName     mJMXMPObjectName = null;
    public static final int JMXMP_PORT = 8888;

    public JMXServiceURL    getJMXServiceURL() { return mJMXMPServiceURL; }
    
    //public static final int RMI_REGISTRY_PORT = 8686;
    
    private StartAMX( final MBeanServer mbs, final AMXConfigRegistrar registrar )
    {
        mMBeanServer = mbs;
        mConfigRegistrar= registrar;
    }
    
        public static boolean
    isStarted()
    {
        return getInstance() != null;
    }
    
        private synchronized void
    start()
    {
        /*
        String timingMsg = "";
        final TimingDelta allDelta = new TimingDelta();
        final TimingDelta delta = new TimingDelta();
        
        final Class c = XTypesMapper.class;
        debug( "Reference XTypesMapper: " + delta.elapsedMillis()  + " " + c.getName() );
        XTypesMapper.getInstance();
        debug( "Load XTypesMapper: " + delta.elapsedMillis() );
        J2EETypesMapper.getInstance();
        debug( "Load J2EETypesMapper: " + delta.elapsedMillis() );
        AllTypesMapper.getInstance();
        debug( "Load AllTypesMapper: " + delta.elapsedMillis() );
        */
        
        
        // loads the high-level AMX MBeans, like DomainRoot, QueryMgr, etc
        mAMXLoaderObjectName = LoadAMX.loadAMX( mMBeanServer );
        
        // load config MBeans
        mConfigRegistrar.getAMXConfigLoader().start( mMBeanServer );
        
        try
        {
            startJMXMPConnectorServer( mMBeanServer, JMXMP_PORT );
        }
        catch( Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    // @ return the instance or null if AMX has not yet been started
        public static synchronized StartAMX
    getInstance()
    {
        return INSTANCE;
    }
    
        public static synchronized void
    startAMX(final MBeanServer mbs, final AMXConfigRegistrar registrar)
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new StartAMX( mbs, registrar );
            INSTANCE.start();
        }
    }
    
        private synchronized JMXConnectorServer
    startJMXMPConnectorServer( final MBeanServer mbeanServer, int port)
        throws MalformedURLException, IOException
    {
        if ( mJMXMP == null )
        {
            final Map<String,Object> env = new HashMap<String,Object>();
            env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol"); 
            env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());

            final JMXServiceURL url =
                mJMXMPServiceURL = new JMXServiceURL("service:jmx:jmxmp://localhost:" + port );
                mJMXMP = JMXConnectorServerFactory.newJMXConnectorServer(mJMXMPServiceURL, env, null);
                final ObjectName objectName = JMXUtil.newObjectName( "jmxremote:type=jmx-connector,name=jmxmp" );
                try
                {
                    mJMXMPObjectName = mbeanServer.registerMBean( mJMXMP, objectName).getObjectName();
                    mJMXMP.start();
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }

                // test
               // JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
               // MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        }
        return mJMXMP;
    }
}



/*

   // private volatile Registry mRmiRegistry= null;


        private synchronized JMXConnectorServer
    startRMIConnectorServer( final MBeanServer mbeanServer, int port)
        throws MalformedURLException, IOException
    {
        final String JMXMPServer = "javax.management.remote.jmxmp.JMXMPConnectorServer";
        try {
            Class.forName( JMXMPServer );
            debug( "!!! JMXMPConnectorServer: OK" );
        }
        catch( final Throwable t )
        {
            debug( "!!! JMXMPConnectorServer: FAILED" );
            t.printStackTrace();
        }
        
        if ( mRmiRegistry == null )
        {
            debug( "CREATING REGISTRY" );
            mRmiRegistry = LocateRegistry.createRegistry( RMI_REGISTRY_PORT );
            debug( "CREATED REGISTRY: " + mRmiRegistry);
        }
        
        final Map<String,Object> env = new HashMap<String,Object>();
        
        JMXServiceURL serviceURL = new JMXServiceURL(
            "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server"); 
        
        debug( "Attempting to start JMXConnector using URL: " + serviceURL );
        final JMXConnectorServer connectorServer = 
           JMXConnectorServerFactory.newJMXConnectorServer( serviceURL,  env, mbeanServer); 
        connectorServer.start(); 
        
        return connectorServer;
    }
*/






