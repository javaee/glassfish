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

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.admin.amx.impl.loader.AMXStartupServiceNew;
import org.glassfish.admin.mbeanserver.AppserverMBeanServerFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;

import javax.management.MBeanServer;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.proxy.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;

/**
    Base class for AMX commands.
 */
abstract class AMXCommandBase implements AdminCommand {
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AMXCommandBase.class);
    
    @Inject
    private MBeanServer mMBeanServer;
    protected final MBeanServer getMBeanServer() { return mMBeanServer; }
    
    // use 'volatile'; don't assume stateless subclasses
    private volatile AdminCommandContext mAdminCommandContext;
    
    public AMXCommandBase() {
        //debug( "AMXCommandBase.AMXCommandBase: " + this.getClass().getName() );
    }
    
    protected static void debug( final String s ) { System.out.println(s); }
    
    protected ActionReport getActionReport() { return mAdminCommandContext.getActionReport(); }
    
        protected final String
    getLocalString( final String key, final String def )
    {
        return localStrings.getLocalString( key, def );
    }
    
    protected final String getLocalString( final String key ) { return getLocalString( key, key ); }

    protected abstract String getCmdName();
    protected final String getCmdDescription() { return getLocalString( getCmdName() + ".description" ); }
        
        protected void
    preExecute(final AdminCommandContext context) {
        //debug( "AMXCommandBase.preExecute: " + this.getClass().getName() + ", MBeanServer = " + getMBeanServer() );
        // presume success
        getActionReport().setActionExitCode(ExitCode.SUCCESS);
        
        AMXBooter.bootAMX( mMBeanServer );
    }
    
    protected DomainRoot
    getDomainRoot()
    {
        return ProxyFactory.getInstance(getMBeanServer()).getDomainRootProxy();
    }
    
    protected abstract void _execute(final AdminCommandContext context);
    
        protected void
    postExecute(final AdminCommandContext context, final boolean success )
    {
        final String cmdName = getCmdName();
        final ActionReport report = getActionReport();
        
        final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            
        if ( success )
        {
            part.setMessage( "AMXCommand \"" + cmdName +
                "\" executed successfully at: " + new java.util.Date() );
        }
        else
        {
            part.setMessage( "AMXCommand " + cmdName + " FAILED: ");
        }
    }

    
    /**
        Synchronized because this command initializes only once.
     */
    public final void execute(final AdminCommandContext context) {
        mAdminCommandContext = context;
        boolean success = false;
        try {
            preExecute( context );
            _execute( context );
            success = true;
        }
        finally {
            postExecute( context, success );
        }
    }
}






