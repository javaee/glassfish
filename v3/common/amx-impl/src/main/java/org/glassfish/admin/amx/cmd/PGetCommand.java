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

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.Param;

import javax.management.ObjectName;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.base.Pathnames;
import com.sun.appserv.management.util.misc.StringUtil;

/**
    List all the AMX MBeans currently registered in the MBeanServer
 */

@Service(name="pget", metadata="mode=debug")   // must match the value of amx_list.command in LocalStrings.properties
@I18n("pget.command")
@Scoped(PerLookup.class)
public class PGetCommand extends AMXCommandBase implements AdminCommand
{
    @Param(primary=true)    
    List<String> expr; // NOTE: framework bug eats all but the last operand

    public PGetCommand()
    {
    }
    
    protected final String getCmdName() { return getLocalString("pget.command"); }
    
    public void _execute(AdminCommandContext context)
    {
        final Pathnames pathnames = getDomainRoot().getPathnames();
        final java.util.Properties params = context.getCommandParameters();
        
        final ActionReport report = getActionReport();
        report.setMessage( "pget " + StringUtil.quote(expr));
        
        final ActionReport.MessagePart listHeader = report.getTopMessagePart().addChild();
        listHeader.setMessage("Results:" );
        /*
        for( final Object name : params.keySet() )
        {
            final ActionReport.MessagePart part = listHeader.addChild();
            part.setMessage( name + " = " + params.get(name) );
        }
        ActionReport.MessagePart part = listHeader.addChild();
        part.setMessage( expr );
        */
        
        System.out.println( "Operands: " + expr.size() );
        
        for( final String e : expr )
        {
            ActionReport.MessagePart part = listHeader.addChild();
            part.setMessage( e + " = " + pathnames.pathnameGetSingleValue(e) );
        }
    }
}






