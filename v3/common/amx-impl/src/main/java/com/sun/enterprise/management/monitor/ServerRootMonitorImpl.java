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

package com.sun.enterprise.management.monitor;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.JMException;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.monitor.ServerRootMonitor;
//import com.sun.appserv.management.monitor.CallFlowMonitor;

import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.BootUtil;

import org.glassfish.admin.amx.util.Issues;


/**
*/
public final class ServerRootMonitorImpl
	extends MonitoringImplBase
	// implements ServerRootMonitor
{
	public ServerRootMonitorImpl()
	{
		super( XTypes.SERVER_ROOT_MONITOR );
	}

	
		public Map<String,ObjectName>
	getORBConnectionManagerMonitorObjectNameMap()
	{
		return( getContaineeObjectNameMap( XTypes.CONNECTION_MANAGER_MONITOR ) );
	}
	
	
	private static final Set<String> NOT_SUPERFLUOUS =
	    GSetUtil.newUnmodifiableStringSet(
    	    "getCallFlowMonitorObjectName",
    	    "getORBConnectionManagerMonitorObjectNameMap"
        );
	    protected final Set<String>
	getNotSuperfluousMethods()
	{
	    return GSetUtil.newSet( super.getNotSuperfluousMethods(), NOT_SUPERFLUOUS );
	}
	
	
		protected void
	registerMisc()
	{
		super.registerMisc();
		
		if ( getLoader().isDAS() )
		{
		    if ( ( BootUtil.getInstance().getServerName().equals( getName() ) ) )
		    {
		        // this is the ServerRootMonitor for the DAS.
		        //registerCallFlow();
                Issues.getAMXIssues().notDone( "ServerRootMonitorImpl.registerCallFlow()" );
		    }
		}
		else
		{	
		    // they should be cascaded from other instances
		    // and ServerRootMonitorImpl does not exist there.
	         throw new RuntimeException( "ServerRootMonitorImpl expected in DAS only" );
		}

	}
	
		public final ObjectName
	getCallFlowMonitorObjectName()
	{
	    return getContaineeObjectName( XTypes.CALL_FLOW_MONITOR );
	}
	
		protected void
	unregisterMisc()
	{
	    if ( getLoader().isDAS() )
	    {
            final MBeanServer mbeanServer       = getMBeanServer();
            final ObjectName callFlowObjectName = getCallFlowMonitorObjectName();
            // ObjectName might or might not be available
            if ( callFlowObjectName != null )
            {
                try
                {
                    mbeanServer.unregisterMBean( callFlowObjectName );
                }
                catch( JMException e )
                {
                    logWarning( "ServerRootMonitorImpl: exception unregistering CallFlowMonitor: " +
                        ExceptionUtil.getRootCause(e));
                }
            }
		}
	}
	
    /*
		protected final void
	registerCallFlow()
	{
		final ObjectNames	objectNames	= ObjectNames.getInstance( getJMXDomain() );
		final ObjectName	childObjectName	=
			objectNames.buildContaineeObjectName( getObjectName(),
				getFullType(), XTypes.CALL_FLOW_MONITOR,
					getName() );
		
		final CallFlowMonitorImpl	callFlow	= new CallFlowMonitorImpl();
		try
		{
            debug( "Loading CallFlowMonitor for DAS: " + childObjectName );
			getMBeanServer().registerMBean( callFlow, childObjectName );
		}
		catch( JMException e )
		{
			logWarning( "ServerRootMonitor: Can't load CallFlow" );
		}
	}
    */
	
	final Set<String>	FAUX_CONTAINEE_TYPES	=
		GSetUtil.newUnmodifiableStringSet(
			XTypes.EJB_MODULE_MONITOR,
			XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR
			);
		
		protected Set<String>
	getFauxChildTypes()
	{
		return( FAUX_CONTAINEE_TYPES );
	}
	
	
	
	/*
		Override default behavior to find modules that belong to ServerRootMonitor directly
		because X-ApplicationMonitor=null.
	 */
		public final Set<ObjectName>
	getContaineeObjectNameSet( final String childJ2EEType )
	{
		final Set<ObjectName>	result	= super.getContaineeObjectNameSet( childJ2EEType );
		
		if ( getFauxChildTypes().contains( childJ2EEType ) )
		{
			final String	nullAppMonitorProp	= Util.makeProp( XTypes.APPLICATION_MONITOR, AMX.NULL_NAME );

			final Set<ObjectName>	fauxContainees	= getFauxContaineeObjectNameSet( childJ2EEType, nullAppMonitorProp);
			result.addAll( fauxContainees );
		}
		
		return( result );
	}
}
