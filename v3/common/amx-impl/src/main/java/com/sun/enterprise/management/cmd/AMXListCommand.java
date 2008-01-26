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
package com.sun.enterprise.management.cmd;

import java.util.Set;
import java.util.List;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;

@Service(name="amx-list")   // must match the value of amx_list.command in LocalStrings.properties
@I18n("amx_list.command")
@Scoped(PerLookup.class)

/**
    List all the AMX MBeans currently registered in the MBeanServer
 */
public class AMXListCommand extends AMXCommandBase implements AdminCommand
{
    public AMXListCommand()
    {
    }
    
    protected final String getCmdName() { return getLocalString("amx_list.command"); }
    
    /**
     */
    public void _execute(AdminCommandContext context)
    {
        final ObjectName allPattern = JMXUtil.newObjectName( "*:*" );
        final Set<ObjectName> mbeans = JMXUtil.queryNames(getMBeanServer(), allPattern, null);
        final List<String> mbeanList = JMXUtil.objectNamesToStrings( mbeans );
        Collections.sort(mbeanList);
        
        final ActionReport report = getActionReport();
        report.setMessage( "Appserver MBeanServer contents" );
        
        ActionReport.MessagePart part = report.getTopMessagePart().addChild();
        part.setMessage( "MBeanServer domains: " + StringUtil.toString( getMBeanServer().getDomains() ) );
        
        part = report.getTopMessagePart().addChild();
        part.setMessage( "MBeanServer mbeans: " + getMBeanServer().getMBeanCount() );
    
        final ActionReport.MessagePart listHeader = report.getTopMessagePart().addChild();
        listHeader.setMessage( mbeans.size() + " MBeans matching " +
            JMXUtil.toString(allPattern) + " registered in the appserver MBeanServer");
        for( final String on : mbeanList )
        {
            part = listHeader.addChild();
            part.setMessage( on );
        }
    }
}






