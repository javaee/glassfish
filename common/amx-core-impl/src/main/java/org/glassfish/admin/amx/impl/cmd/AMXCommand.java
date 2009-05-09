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

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.impl.loader.AMXStartupServiceNew;
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
@Service(name="amx", metadata="mode=debug")   // name must match the value of amx.command in LocalStrings.properties
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
        final DomainRoot domainRoot = ProxyFactory.getInstance(  getMBeanServer() ).getDomainRootProxy();
        
        final ActionReport report = getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        
        report.getTopMessagePart().addChild().setMessage( JMXUtil.getMBeanServerDelegateInfo( getMBeanServer() ) );

        final JMXServiceURL[] serviceURLs = AMXStartupServiceNew.getAMXStartupServiceMBeanProxy(getMBeanServer()).getJMXServiceURLs();
        report.getTopMessagePart().addChild().setMessage( "JMXServiceURLs[] ===> " + StringUtil.toString( ", ", (Object)serviceURLs ) );
        
        // get a nice sorted list of all AMX MBean ObjectNames
        final ObjectName amxPattern = JMXUtil.newObjectName( "amx:*" );
        final Set<ObjectName> mbeans = JMXUtil.queryNames(getMBeanServer(), amxPattern, null);
        final List<String> mbeanList = CollectionUtil.toStringList( mbeans );
        Collections.sort(mbeanList);
        
        String msg = "AMX initialized and ready for use." + StringUtil.NEWLINE();
        report.setMessage( msg );
        for( final String on : mbeanList )
        {
            report.getTopMessagePart().addChild().setMessage( on );
        }
    }
}






