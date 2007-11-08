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
package com.sun.enterprise.management.support;

import java.util.Properties;

import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.MapUtil;


import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import static com.sun.appserv.server.LifecycleEvent.*;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.ServerLifecycleException;


/**
 */
public final class AMXLifecycleModule implements LifecycleListener
{
    private final String    NEWLINE;
    
		public
	AMXLifecycleModule()
	{
	    NEWLINE = System.getProperty( "line.separator" );
	}
	
	    private void
	init(
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    context.log( "AMXLifecycleModule: init" );
	}
	
	    private void
	startup(
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    context.log( "AMXLifecycleModule: startup" );
	}
	
	    private void
	ready(
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    context.log( "AMXLifecycleModule: ready" );
	}
	
	    private void
	shutdown(
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    context.log( "AMXLifecycleModule: shutdown" );
	}
	
	    private void
	terminate(
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    context.log( "AMXLifecycleModule: terminate" );
	}
	
	    private void
	dumpInfo(
	    final int   eventType,
	    final LifecycleEventContext context,
	    final Properties props )
	{
	    final String msg    = "AMXLifecycleModule: " + eventType + NEWLINE +
	        "InstallRoot: " + context.getInstallRoot() + NEWLINE +
	        "InstanceName: " + context.getInstanceName() + NEWLINE +
	        "CmdLineArgs: " + StringUtil.toString( " ", (Object[])context.getCmdLineArgs() ) + NEWLINE +
	        "Properties: " + NEWLINE +
	        MapUtil.toString( props, NEWLINE ) + NEWLINE;
	    
	    context.log( msg );
	        
	}
	
	
        public void
    handleEvent(LifecycleEvent event)
        throws ServerLifecycleException
    {
        final int  type  = event.getEventType();
        
        final Properties    props   = (Properties)event.getData();
        final LifecycleEventContext context = event.getLifecycleEventContext();
        
        dumpInfo( type, context, props );
        
        if ( type == INIT_EVENT )
        {
            init( context, props );
        }
        else if ( type == STARTUP_EVENT )
        {
            startup( context, props );
        }
        else if ( type == READY_EVENT )
        {
            ready( context, props );
        }
        else if ( type == SHUTDOWN_EVENT )
        {
            shutdown( context, props );
        }
        else if ( type == TERMINATION_EVENT )
        {
            terminate( context, props );
        }
        else
        {
            throw new IllegalArgumentException( "eventType: " + type );
        }
    }
}








