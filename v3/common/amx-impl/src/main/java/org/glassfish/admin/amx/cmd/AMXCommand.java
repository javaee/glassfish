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
import java.util.List;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

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


/**
    Command 'amx' initializes AMX and returns a status page. If already initialized it does nothing.
    Unlike most commands, this one is intentionally stateful (instantiated onlly once)
 */
@Service(name="amx")   // must match the value of amx.command in LocalStrings.properties
@I18n("amx.command")
public class AMXCommand extends AMXCommandBase implements AdminCommand
{
    private boolean mInitialized;
    private volatile ObjectName mAMXLoaderObjectName;
    
    public AMXCommand()
    {
    }
     
        private void
    initialize()
    {
        final ObjectName loaderObjectName = LoadAMX.loadAMX( getMBeanServer() );
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
        final TimingDelta delta = new TimingDelta();
        
        final Class c = XTypesMapper.class;
        System.out.println( "Reference XTypesMapper: " + delta.elapsedMillis()  + " " + c.getName() );
        XTypesMapper.getInstance();
        System.out.println( "Load XTypesMapper: " + delta.elapsedMillis() );
        J2EETypesMapper.getInstance();
        System.out.println( "Load J2EETypesMapper: " + delta.elapsedMillis() );
        AllTypesMapper.getInstance();
        System.out.println( "Load AllTypesMapper: " + delta.elapsedMillis() );
        
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
        
        // get a nice sorted list of all AMX MBean ObjectNames
        final ObjectName amxPattern = JMXUtil.newObjectName( "amx:*" );
        final Set<ObjectName> mbeans = JMXUtil.queryNames(getMBeanServer(), amxPattern, null);
        final List<String> mbeanList = JMXUtil.objectNamesToStrings( mbeans );
        Collections.sort(mbeanList);
        
        report.setMessage( "AMX initialized and ready for use." + timingMsg );
        for( final String on : mbeanList )
        {
            report.getTopMessagePart().addChild().setMessage( on );
        }
    }
}






