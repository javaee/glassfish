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
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/j2ee/WebModuleImpl.java,v 1.9 2006/03/09 20:30:45 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2006/03/09 20:30:45 $
 */
 
package com.sun.enterprise.management.j2ee;

import java.util.Map;

import javax.management.ObjectName;

import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.jmx.AttributeNameMapper;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.WebModuleSupport;

import com.sun.appserv.management.j2ee.WebModule;
import com.sun.appserv.management.j2ee.Servlet;

/**
 */
public final class WebModuleImpl extends J2EEModuleImplBase
	// implements WebModule
{
		public
	WebModuleImpl( final Delegate delegate )
	{
		super( delegate );
	}
	
	/**
		MBeans of type j2eeType=WebModule have their virtual server prepended
		to their name. For example, if the virtual server is "__asadmin", the
		name will start with "//__asadmin/".
	 */
		protected String
	getConfigPeerName()
	{
		final String compositeName	= getName();
		
		final String webModuleName	= WebModuleSupport.extractWebModuleName( compositeName );

		return( webModuleName );
	}
	
		public boolean
	isConfigProvider()
	{
		boolean	isConfigProvider	= super.isConfigProvider();
		if ( super.isConfigProvider() && getObjectName() != null )
		{
			final String name	= getConfigPeerName();
			
			/*
				Ugly hack for our System WebModules.
				// context root is being used instead of module name
			 */
			if ( name.length() == 0 || name.equals( "asadmin" ) ||
				name.equals( "web1" ) )
			{
				isConfigProvider	= false;
			}
			
		}
		
		return( isConfigProvider );
	}
	
	    protected WebModule
	getSelfProxy()
	{
	    return (WebModule)getSelf();
	}
	
		public String[]
	getservlets()
	{
	    final Map<String,Servlet>   servlets    = getSelfProxy().getServletMap();
	    
		return( CollectionUtil.toStringArray( Util.toObjectNames( servlets ).values() ) );
	}
	
		public Map
	getServletObjectNameMap()
	{
		return( getContaineeObjectNameMap( J2EETypes.SERVLET ) );
	}
	
		protected String
	getMonitoringPeerJ2EEType()
	{
		return( XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR );
	}


		private final String
	deduceModuleName()
	{
		// get the last part of the workDir; it should be the actual module name
		final String	workDir	= getSelfProxy().getWorkDir();
		final int index1	= workDir.lastIndexOf( "/" );
		final int index2	= workDir.lastIndexOf( "\\" );
		final int index	= index1 > index2 ? index1 : index2;
		
		String	result	= null;
		
		if (index > 0 )
		{
			result	= workDir.substring( index + 1, workDir.length() );
		}
		
		return( result );
	}
	 
		protected ObjectName
	queryConfigPeerFailed( final Map<String,String> propsMap )
	{
		final String potentialName	= deduceModuleName();
		propsMap.put( AMX.NAME_KEY, potentialName );
		
		final ObjectName	result	= queryProps( propsMap );
		
		return( result );
	}
	
		protected ObjectName
	queryMonitoringPeerFailed( final Map<String,String> propsMap )
	{
		final String	selfName	= getName();
		
		final String virtualServerName	= WebModuleSupport.extractVirtualServerName( selfName );
		
		ObjectName	result	= null;
		
		final String potentialName	= deduceModuleName();
		if ( potentialName != null )
		{
			final String	name	= WebModuleSupport.formCompositeName( virtualServerName, potentialName );

			propsMap.put( AMX.NAME_KEY, name );
			result	= queryProps( propsMap );
		}
		
		return result;
	}
}
