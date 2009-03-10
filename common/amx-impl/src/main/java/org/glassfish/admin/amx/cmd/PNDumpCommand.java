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





















