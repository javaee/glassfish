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
package org.glassfish.admin.amx.cmd;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;

import java.lang.management.ManagementFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.TimingDelta;

import com.sun.enterprise.management.support.LoadAMX;
import com.sun.enterprise.management.support.XTypesMapper;
import com.sun.enterprise.management.support.J2EETypesMapper;
import com.sun.enterprise.management.support.AllTypesMapper;

import java.net.MalformedURLException;
import java.io.IOException;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnector;


//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;

import javax.management.remote.jmxmp.JMXMPConnectorServer;

import org.glassfish.admin.amx.AMXConfigRegistrar;


/**
    Command 'amx' initializes AMX and returns a status page. If already initialized it does nothing.
    Unlike most commands, this one is intentionally stateful (instantiated onlly once)
    
    Temporary RMI support,add this to domain.xml ^lt;jvm-options>
    <pre>
    <jvm-options>-Dcom.sun.management.jmxremote=true</jvm-options>
    <jvm-options>-Dcom.sun.management.jmxremote.port=8686</jvm-options>
    <jvm-options>-Dcom.sun.management.jmxremote.ssl=false</jvm-options>
    <jvm-options>-Dcom.sun.management.jmxremote.authenticate=false</jvm-options>
    </pre>
    
 */
@Service(name="amx")   // must match the value of amx.command in LocalStrings.properties
@I18n("amx.command")
public class AMXCommand extends AMXCommandBase implements AdminCommand
{
    @Inject
    private AMXConfigRegistrar mConfigRegistrar;

    private boolean mInitialized;
    private volatile ObjectName mAMXLoaderObjectName;
   // private volatile Registry mRmiRegistry= null;
   
    // these 3 fields are initialized later
    volatile JMXConnectorServer mJMXMP = null;
    volatile JMXServiceURL  mJMXMPServiceURL = null;
    volatile ObjectName     mJMXMPObjectName = null;

    public static final int RMI_REGISTRY_PORT = 8686;
    
    public static final int JMXMP_PORT = 8888;
    
    public AMXCommand()
    {
    }
     
        private void
    initialize()
    {
        final ObjectName loaderObjectName = LoadAMX.loadAMX( getMBeanServer() );
        
        mConfigRegistrar.getAMXConfigLoader().start( getMBeanServer() );
    }
    
        private synchronized JMXConnectorServer
    startJMXMPConnectorServer( final MBeanServer mbeanServer, int port)
        throws MalformedURLException, IOException
    {
        if ( mJMXMP == null )
        {
            final Map<String,Object> env = new HashMap<String,Object>();
        
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
    
/*
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


    protected final String getCmdName() { return getLocalString("amx.command"); }
    
        private static String
    getMBeanServerDelegateInfo( final MBeanServer server )
    {
        final MBeanServerDelegateMBean delegate = JMXUtil.getMBeanServerDelegateMBean(server);
        final String mbeanServerInfo = "MBeanServerDelegate: {" +
            "MBeanServerId = " + delegate.getMBeanServerId() +
            ", ImplementationMame = " + delegate.getImplementationName() +
            ", ImplementationVendor = " + delegate.getImplementationVendor() +
            ", ImplementationVersion = " + delegate.getImplementationVersion() +
            ", SpecificationName = " + delegate.getSpecificationName() +
            ", SpecificationVendor = " + delegate.getSpecificationVendor() +
            ", SpecificationVersion = " + delegate.getSpecificationVersion() +
            " }";
        return mbeanServerInfo;
    }
    
    /**
        Synchronized because this command initializes only once (singleton), but can be invoked
        repeatedly.
     */
    public final synchronized void _execute(AdminCommandContext context)
    {
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
        
        if ( ! mInitialized ) {
            initialize();
            mInitialized    = true;
            timingMsg = " (" + allDelta.elapsedMillis() + " ms)";
        }
        else
        {
            timingMsg = " (previously initialized)";
        }
        
        final ActionReport report = getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        
        report.getTopMessagePart().addChild().setMessage( getMBeanServerDelegateInfo( getMBeanServer() ) );

        JMXConnectorServer connectorServer    = null;
        try
        {
            connectorServer = startJMXMPConnectorServer( getMBeanServer(), JMXMP_PORT );
            report.getTopMessagePart().addChild().setMessage( "JMXServiceURL ===> " + connectorServer.getAddress() );
        }
        catch ( final Exception e )
        {
            debug( "failed to start JMXMPConnectorServer" );
            e.printStackTrace();
            report.getTopMessagePart().addChild().setMessage( "ERROR: could not start JMXConnectorServer: " + e );
        }
        
        // get a nice sorted list of all AMX MBean ObjectNames
        final ObjectName amxPattern = JMXUtil.newObjectName( "amx:*" );
        final Set<ObjectName> mbeans = JMXUtil.queryNames(getMBeanServer(), amxPattern, null);
        final List<String> mbeanList = JMXUtil.objectNamesToStrings( mbeans );
        Collections.sort(mbeanList);
        
        String msg = "AMX initialized and ready for use." + timingMsg + StringUtil.NEWLINE();
        report.setMessage( msg );
        for( final String on : mbeanList )
        {
            report.getTopMessagePart().addChild().setMessage( on );
        }
    }
}






