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
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.base.AMX;
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
import java.util.Map;

import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.base.Pathnames;
import com.sun.appserv.management.util.misc.StringUtil;

/**
    List pathnames.
 */
@Service(name="pndump", metadata="mode=debug")   // must match the value of amx_list.command in LocalStrings.properties
@Scoped(PerLookup.class)
public class PNDumpCommand extends AMXCommandBase implements AdminCommand
{
    // stupid framework won't allow 0 or N arguments
    //@Param(primary=true)    
    //List<String> expr; // NOTE: framework bug eats all but the last operand

    public PNDumpCommand()
    {
    }
    
    protected final String getCmdName() { return getLocalString("pget.command"); }
    
    public void _execute(AdminCommandContext context)
    {
        final Pathnames pathnames = getDomainRoot().getPathnames();
        final java.util.Properties params = context.getCommandParameters();
        
        final ActionReport report = getActionReport();
        report.setMessage( "plist ");
        final ActionReport.MessagePart listHeader = report.getTopMessagePart().addChild();
        
        // framework is buggy; it scrambles a large test message and scrambles lots of 
        // other variants. Ugg..
        // listHeader.setMessage( pathnames.dumpPathnames() );
        
        listHeader.setMessage("Results:" );
        
        // note that the framework has some kind of bug that truncates our messages inexplicably
        // maintain a large buffer for output purposes
        final StringBuffer buf = new StringBuffer();
        
        final String[] all = pathnames.getAllPathnames();
        for( final String pn : all )
        {
            buf.append( "\n" + pn + "\n" );
            final ActionReport.MessagePart part = listHeader.addChild();
            part.setMessage( pn );
            try
            {
                final AMX amx = pathnames.getPathnameTarget(pn);
                final Map<String,String> allValues = pathnames.getPathnameValues(pn);
                for( final String pnAttrName : allValues.keySet() )
                {
                    final ActionReport.MessagePart item = part.addChild();
                    String msg = "\t@" + pnAttrName + " = " + allValues.get(pnAttrName);
                    
                    item.setMessage( msg );
                    buf.append( msg + "\n" );
                }
            }
            catch( Exception e )
            {
                part.setMessage( pn + "FAILURE: " + ExceptionUtil.getRootCause(e) );
            }
        }
    }
}





















