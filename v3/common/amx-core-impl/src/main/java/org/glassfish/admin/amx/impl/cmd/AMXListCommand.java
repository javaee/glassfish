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
 
package org.glassfish.admin.amx.impl.cmd;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import javax.management.ObjectName;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
    List all the AMX MBeans currently registered in the MBeanServer
 */

@Service(name="amx-list", metadata="mode=debug")   // must match the value of amx_list.command in LocalStrings.properties
@I18n("amx_list.command")
@Scoped(PerLookup.class)
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
        final List<String> mbeanList = CollectionUtil.toStringList( mbeans );
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






