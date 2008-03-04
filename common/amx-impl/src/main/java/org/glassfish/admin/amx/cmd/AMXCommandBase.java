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

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;

import com.sun.enterprise.util.LocalStringManagerImpl;

/**
    Base class for AMX commands.
 */
abstract class AMXCommandBase implements AdminCommand {
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AMXCommandBase.class);
    
    @Inject //(name=AppserverMBeanServerFactory.OFFICIAL_MBEANSERVER)
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






