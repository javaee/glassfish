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

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amx.loader.AMXStartupService;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Service;

import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
    Command 'amx' initializes AMX and returns a status page. If already initialized it does nothing.
    Unlike most commands, this one is intentionally stateful (instantiated onlly once)
    
 */
@Service(name="amx", metadata="mode=debug")   // must match the value of amx.command in LocalStrings.properties
@I18n("amx.command")
public final class AMXCommand extends AMXCommandBase implements AdminCommand
{
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
        final DomainRoot domainRoot = ProxyFactory.getInstance(  getMBeanServer() ).getDomainRoot();
        
        final ActionReport report = getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        
        report.getTopMessagePart().addChild().setMessage( JMXUtil.getMBeanServerDelegateInfo( getMBeanServer() ) );

        final JMXServiceURL[] serviceURLs = AMXStartupService.getAMXStartupServiceMBean(getMBeanServer()).getJMXServiceURLs();
        report.getTopMessagePart().addChild().setMessage( "JMXServiceURLs[] ===> " + StringUtil.toString( ", ", (Object)serviceURLs ) );
        
        // get a nice sorted list of all AMX MBean ObjectNames
        final ObjectName amxPattern = JMXUtil.newObjectName( "amx:*" );
        final Set<ObjectName> mbeans = JMXUtil.queryNames(getMBeanServer(), amxPattern, null);
        final List<String> mbeanList = JMXUtil.objectNamesToStrings( mbeans );
        Collections.sort(mbeanList);
        
        String msg = "AMX initialized and ready for use." + StringUtil.NEWLINE();
        report.setMessage( msg );
        for( final String on : mbeanList )
        {
            report.getTopMessagePart().addChild().setMessage( on );
        }
    }
}






