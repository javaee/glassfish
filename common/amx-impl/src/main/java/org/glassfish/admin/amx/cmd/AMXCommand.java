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
import org.jvnet.hk2.annotations.Extract;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.TimingDelta;

import org.glassfish.admin.amx.support.LoadAMX;

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
import org.glassfish.admin.amx.loader.StartAMX;


/**
    Command 'amx' initializes AMX and returns a status page. If already initialized it does nothing.
    Unlike most commands, this one is intentionally stateful (instantiated onlly once)
    
 */
@Service(name="amx")   // must match the value of amx.command in LocalStrings.properties
@I18n("amx.command")
public final class AMXCommand extends AMXCommandBase implements AdminCommand
{
    @Inject
    private AMXConfigRegistrar mConfigRegistrar;

    private boolean mInitialized;
    
    public AMXCommand()
    {
        //mConfigRegistrar    = AMXConfigRegistrar.getInstance();
    }
    
                    
    protected final String getCmdName() { return getLocalString("amx.command"); }
    
    /**
        Synchronized because this command initializes only once (singleton), but can be invoked
        repeatedly.
     */
    public final synchronized void _execute(AdminCommandContext context)
    {
        String timingMsg = "";
         final TimingDelta allDelta = new TimingDelta();
            
        if ( ! mInitialized ) {
            StartAMX.startAMX( getMBeanServer(), mConfigRegistrar );
            mInitialized    = true;
            timingMsg = " (" + allDelta.elapsedMillis() + " ms)";
        }
        else
        {
            timingMsg = " (previously initialized)";
        }
        
        final DomainRoot domainRoot = ProxyFactory.getInstance(  getMBeanServer() ).getDomainRoot();
        domainRoot.waitAMXReady();
        
        final ActionReport report = getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        
        report.getTopMessagePart().addChild().setMessage( JMXUtil.getMBeanServerDelegateInfo( getMBeanServer() ) );

        report.getTopMessagePart().addChild().setMessage( "JMXServiceURL ===> " + StartAMX.getInstance().getJMXServiceURL() );
        
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






