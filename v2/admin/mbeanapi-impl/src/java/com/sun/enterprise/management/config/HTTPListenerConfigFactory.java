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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/HTTPListenerConfigFactory.java,v 1.4 2006/03/09 20:30:38 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2006/03/09 20:30:38 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.config.HTTPListenerConfigKeys;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.VirtualServerConfig;

import com.sun.appserv.management.util.misc.MapUtil;

import com.sun.enterprise.management.support.oldconfig.OldHTTPServiceMBean;

import com.sun.appserv.management.util.misc.GSetUtil;

public final class HTTPListenerConfigFactory extends ConfigFactory
{
	private final OldHTTPServiceMBean	mOldHTTPServiceMBean;
	
		public
	HTTPListenerConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
		
		mOldHTTPServiceMBean =
			getOldConfigProxies().getOldHTTPServiceMBean( getConfigName() );
	}
	
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet( 
		HTTPListenerConfigKeys.ADDRESS_KEY,
		HTTPListenerConfigKeys.PORT_KEY,
		HTTPListenerConfigKeys.ENABLED_KEY,
		HTTPListenerConfigKeys.SECURITY_ENABLED_KEY,
		HTTPListenerConfigKeys.DEFAULT_VIRTUAL_SERVER_KEY,
		HTTPListenerConfigKeys.ACCEPTOR_THREADS_KEY,
		HTTPListenerConfigKeys.XPOWERED_BY_KEY,
		HTTPListenerConfigKeys.XPOWERED_BY_KEY,
		HTTPListenerConfigKeys.IGNORE_MISSING_REFERENCES_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
		protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "id" ) );
	}
	
		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		return mOldHTTPServiceMBean.createHttpListener( translatedAttrs );
	}
	
	
		private void
	checkVirtualServerExists( final String	virtualServerName )
	{
		final HTTPServiceConfig		httpService	= (HTTPServiceConfig)getFactoryContainer();
		
		final Map<String,VirtualServerConfig>	virtualServers	=
		    httpService.getVirtualServerConfigMap();
		
		if( ! virtualServers.containsKey( virtualServerName ) )
		{
			throw new IllegalArgumentException( "No virtual server exists with the name: " + virtualServerName );
		}
	}
	
	
		public ObjectName
	create(
		final String	name,
		final String	address,
		final int		port,
		final String	defaultVirtualServer,
		final String	serverName,
		final Map<String,String>		options )
	{
		final String[] requiredParams =
		{
		    HTTPListenerConfigKeys.ADDRESS_KEY,				address,
		    HTTPListenerConfigKeys.PORT_KEY,					"" + port,
			HTTPListenerConfigKeys.DEFAULT_VIRTUAL_SERVER_KEY, defaultVirtualServer,
			HTTPListenerConfigKeys.SERVER_NAME_KEY,			serverName,
		};
		
		if ( requireValidReferences( options )  )
		{
			checkVirtualServerExists( defaultVirtualServer );
		}

		final Map<String,String> params	= initParams( name, requiredParams, options );

		final ObjectName	amxName	= createNamedChild( name, params );
		
		return( amxName );
	}
	
		protected final void
	removeByName( String name )
	{
		mOldHTTPServiceMBean.removeHttpListenerById( name );
	}
}






