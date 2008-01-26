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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.management.support;


import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
 */
public final class WebModuleSupport
{
	private 	WebModuleSupport()	{}
	
	/**
		Used to prefix a virtual server name for a j2eeType=WebModule
	 */
	public static final String	VIRTUAL_SERVER_PREFIX	= "//";
	
	/**
		Used to terminate a virtual server name for a j2eeType=WebModule
	 */
	public static final String	VIRTUAL_SERVER_DELIM	= "/";
	
		public static boolean
	isLegalVirtualServerName( final String candidate )
	{
		return (! candidate.startsWith( VIRTUAL_SERVER_PREFIX ) ) &&
			( candidate.indexOf( VIRTUAL_SERVER_DELIM ) < 0 );
	}
	
		public static String
	formCompositeName(
		final String	virtualServerName,
		final String	webModuleName )
	{
		return VIRTUAL_SERVER_PREFIX + virtualServerName + VIRTUAL_SERVER_DELIM + webModuleName;
	}
	
	
	/**
		WebModule names are of the form //<virtual-server-name>/<web-module-name>.
		Extract the virtual-server-name portion
		@see #extractWebModuleName
	 */
		public static String
	extractVirtualServerName( final String compositeName )
	{
		if ( ! compositeName.startsWith( VIRTUAL_SERVER_PREFIX ) )
		{
			throw new IllegalArgumentException( compositeName );
		}
		
		final String temp	=
			StringUtil.stripPrefix( compositeName, VIRTUAL_SERVER_PREFIX );
		final int	delimIdx	= temp.indexOf( VIRTUAL_SERVER_DELIM );
		if ( delimIdx < 0 )
		{
		    throw new IllegalArgumentException( compositeName );
		}
		
		final String virtualServerName	= temp.substring( 0, delimIdx );
		
		return virtualServerName;
	}
	
	/**
		WebModule names are of the form //<virtual-server-name>/<web-module-name>.
		Extract the web-module-name portion
		@see #extractVirtualServerName
	 */
		public static String
	extractWebModuleName( final String compositeName )
	{
	    final String virtualServerName  = extractVirtualServerName( compositeName );
	    
		final String prefix	=
			VIRTUAL_SERVER_PREFIX + virtualServerName + VIRTUAL_SERVER_DELIM;
	
	    String  name    = compositeName.substring( prefix.length(), compositeName.length() );
		if ( name.length() == 0 )
		{
		    name    = virtualServerName + ".default";
		}
		
		return name;
	}

		public static String
	getWebModuleName( final ObjectName	oldObjectName )
	{
		final String webModule			    = oldObjectName.getKeyProperty( "web-module" );
		final String standaloneWebModule	= oldObjectName.getKeyProperty( "standalone-web-module" );
		
		String	webModuleName   = null;
		if ( standaloneWebModule != null )
		{
		    webModuleName   = extractWebModuleName( standaloneWebModule );
		}
		else if ( webModule != null )
		{
		    webModuleName   = extractWebModuleName( webModule );
		}
		else
		{
		    throw new IllegalArgumentException( JMXUtil.toString( oldObjectName ) );
		}
		
		return( webModuleName );
	}
	
	/** the category=monitor,type=web-module name is {@link #JWS_APP_CLIENTS_WEB_MODULE_MONITOR_NAME} */
	public static final String JWS_APP_CLIENTS_WEB_MODULE_NAME  = "//server/__JWSappclients";
	
	/** the category=runtime,type=WebModule name is {@link #JWS_APP_CLIENTS_WEB_MODULE_NAME} */
	public static final String JWS_APP_CLIENTS_WEB_MODULE_MONITOR_NAME  = "//server/sys";
	
	//
}

