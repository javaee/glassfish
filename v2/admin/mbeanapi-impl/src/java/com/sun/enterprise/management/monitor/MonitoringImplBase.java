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

import javax.management.ObjectName;
import javax.management.MBeanServer;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.MonitoringRoot;

import com.sun.enterprise.management.support.AMXImplBase;
import com.sun.enterprise.management.support.Delegate;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
	Base implementation class for Monitoring MBeans of all stripes; both
	those that have Stats and those that do not.
*/
public class MonitoringImplBase extends AMXImplBase
{
		public
	MonitoringImplBase( final String j2eeType, final Delegate delegate )
	{
		super( j2eeType, delegate );
	}

		public
	MonitoringImplBase( final String j2eeType )
	{
		this( j2eeType, null );
	}

		public String
	getGroup()
	{
		return( AMX.GROUP_MONITORING );
	}

		public ObjectName
	getServerRootMonitorObjectName()
	{
	    ObjectName  objectName  = null;
	    
		final String	name	= getObjectName().getKeyProperty( XTypes.SERVER_ROOT_MONITOR );
		if ( name != null )
		{
    		final MonitoringRoot	root	= getDomainRoot().getMonitoringRoot();
    		final ServerRootMonitor	mon	=
    		    (ServerRootMonitor)root.getServerRootMonitorMap().get( name );
		
		    objectName  = Util.getObjectName( mon );
		}
		
		return( objectName );
	}
	
	/**
		For whatever reason the com.sun.appserv MBeans use:
		<key>=foo.jar,categor=runtime (JSR 77)
		<key>=foo_jar,category=monitor (monitoring)
		
		We want both to be named "foo.jar" for two reasons:
		(1) for consistency, (2) so that the JSR 77 MBeans can find
		their corresponding monitors by name.
		
	 */
		protected final String
	fixEJBModuleName( final String moduleName )
	{
		final String	JAR	= "jar";
		
		String	result	= moduleName;
		
		if ( moduleName.endsWith( "_" + JAR ) )
		{
			result	= StringUtil.stripSuffix( moduleName, "_" + JAR ) + "." +JAR;
		}
		
		return( result );
	}
	
		protected final ObjectName
	fixEJBModuleName(
		final ObjectName 	objectNameIn,
		final String		key )
	{
		ObjectName	objectNameOut	= objectNameIn;
		
		final String	origName	= objectNameIn.getKeyProperty( key );
		if ( origName != null )
		{
			final String	fixedName	= fixEJBModuleName( origName );
			
			if ( ! fixedName.equals( origName ) )
			{
				objectNameOut	= JMXUtil.setKeyProperty( objectNameIn, key, fixedName);
			}
		}
		
		if ( ! objectNameOut.equals( objectNameIn ) )
		{
			logFiner( "MonitoringImplBase.fixEJBModuleName: modified " + 
				quote( objectNameIn ) + " => " + quote( objectNameOut ) );
		}
		return( objectNameOut );
	}
	
	/**
		See {@link #fixEJBModuleName} for info on why this is being done.
	 */
		protected ObjectName
	preRegisterModifyName(
		final MBeanServer	server,
		final ObjectName	nameIn )
	{
		ObjectName	objectNameOut	= super.preRegisterModifyName( server, nameIn );
		
		if ( nameIn.getKeyProperty( XTypes.EJB_MODULE_MONITOR ) != null )
		{
			objectNameOut	= fixEJBModuleName( objectNameOut, XTypes.EJB_MODULE_MONITOR );
		}
		
		return objectNameOut;
	}
}
























