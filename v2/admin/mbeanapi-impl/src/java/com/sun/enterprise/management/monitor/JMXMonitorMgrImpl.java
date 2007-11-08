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
import javax.management.JMException;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.monitor.JMXMonitorMgr;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.AMXImplBase;
import com.sun.enterprise.management.support.Delegate;


/**
*/
public final class JMXMonitorMgrImpl extends AMXImplBase
	// implements JMXMonitorMgr
{
		public
	JMXMonitorMgrImpl()
	{
	}
	
	
	private static final Set<String> NOT_SUPERFLUOUS =
	    GSetUtil.newUnmodifiableStringSet(
    	    "getCounterMonitorObjectNameMap",
            "getGaugeMonitorObjectNameMap",
            "getStringMonitorObjectNameMap"
        );
            
            
	    protected final Set<String>
	getNotSuperfluousMethods()
	{
	    return GSetUtil.newSet( super.getNotSuperfluousMethods(), NOT_SUPERFLUOUS );
	}
	
		public String
	getGroup()
	{
		return( AMX.GROUP_UTILITY );
	}
	
		private ObjectName
	registerMonitor(
		final Object	impl,
		final String	j2eeType,
		final String	name )
	{
		final ObjectName	self	= getObjectName();
		
		final String	domain			= self.getDomain();
		final String	requiredProps	= Util.makeRequiredProps( j2eeType, name );
		
		ObjectName	objectName	= Util.newObjectName( domain, requiredProps );
		try
		{
			objectName	= registerMBean( impl, objectName );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		
		return( objectName );
	}
	
    	public ObjectName
    createStringMonitor( final String name )
    {
    	final AMXStringMonitorImpl	monitor	= new AMXStringMonitorImpl();
    	return( registerMonitor( monitor, XTypes.JMX_STRING_MONITOR, name ) );
    }
    
    	public ObjectName
    createCounterMonitor( final String name )
    {
    	final AMXCounterMonitorImpl	monitor	= new AMXCounterMonitorImpl();
    	return( registerMonitor( monitor, XTypes.JMX_COUNTER_MONITOR, name ) );
    }
    
    	public ObjectName
    createGaugeMonitor( final String name )
    {
    	final AMXGaugeMonitorImpl	monitor	= new AMXGaugeMonitorImpl();
    	return( registerMonitor( monitor, XTypes.JMX_GAUGE_MONITOR, name ) );
    }
    
    
    	private Map<String,ObjectName>
    getMap( final String j2eeType )
    {
    	final Map<String,AMX> m	= getDomainRoot().getContaineeMap( j2eeType );
    	
    	return( Util.toObjectNames( m ) );
    }
    
    	public Map<String,ObjectName>
    getStringMonitorObjectNameMap()
    {
    	return( getMap( XTypes.JMX_STRING_MONITOR ) );
    }
    
		public Map<String,ObjectName>
    getCounterMonitorObjectNameMap()
    {
    	return( getMap( XTypes.JMX_COUNTER_MONITOR ) );
    }
    
    	public Map<String,ObjectName>
    getGaugeMonitorObjectNameMap()
    {
    	return( getMap( XTypes.JMX_GAUGE_MONITOR ) );
    }
    
    static private final Set<String>	TYPES	=
    	GSetUtil.newUnmodifiableStringSet(
			//XTypes.JMX_GAUGE_MONITOR,
			//XTypes.JMX_COUNTER_MONITOR,
			XTypes.JMX_STRING_MONITOR
		);
			
    
    	private ObjectName
    getMonitor( final String name )
    {
    	final Set<AMX>	s	= getDomainRoot().getByNameContaineeSet( TYPES, name );
    	
    	final AMX	mon	= Util.asAMX( GSetUtil.getSingleton( s ));
    	
    	return( Util.getObjectName( mon ) );
    }
    
    	public void
    remove( final String name )
    {
    	final ObjectName	objectName	= getMonitor( name );
    	if ( objectName == null )
    	{
    		throw new IllegalArgumentException( name );
    	}
    	
    	try
    	{
    		getMBeanServer().unregisterMBean( objectName );
    	}
    	catch( JMException e )
    	{
    		throw new RuntimeException( e );
    	}
    }
}












