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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ClusterConfigImpl.java,v 1.13 2006/03/17 03:34:15 llc Exp $
 * $Revision: 1.13 $
 * $Date: 2006/03/17 03:34:15 $
 */


package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import javax.management.ObjectName;
import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.Delegate;
	
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;

import com.sun.enterprise.management.support.AMXAttributeNameMapper;

/**
	Configuration for the &lt;cluster&gt; element.
 */
public final class ClusterConfigImpl extends AMXConfigImplBase
	// implements ClusterConfig
	implements ConfigFactoryCallback
{
		public
	ClusterConfigImpl( final Delegate delegate )
	{
		super( delegate );
	}
	
	    private ClusterConfig
	self()
	{
	    return (ClusterConfig)getSelf();
	}
	
		protected void
	addCustomMappings( final AMXAttributeNameMapper mapper )
	{
	    super.addCustomMappings( mapper );
		mapper.matchName( "ReferencedConfigName", "config-ref" );
	}
	
	
                
		public Map<String,ObjectName>
	getClusteredServerConfigObjectNameMap()
	{
		final Map<String,ObjectName> serverRefConfigMap =
		    Util.toObjectNames( self().getServerRefConfigMap() );
		if ( serverRefConfigMap.isEmpty() )
		{
			return new HashMap<String,ObjectName>();
		}
		
		final Set<ObjectName> allClusteredServerConfigObjectNames = 
			getQueryMgr().queryJ2EETypeObjectNameSet( XTypes.CLUSTERED_SERVER_CONFIG );
		final Map<String,ObjectName> objectNameMap =
		    Util.createObjectNameMap( allClusteredServerConfigObjectNames );

		final Map<String,ObjectName> clusteredServerConfigObjectNameMap = 
			new HashMap<String,ObjectName>( allClusteredServerConfigObjectNames.size() );

		for( final String key : serverRefConfigMap.keySet() )
		{
			clusteredServerConfigObjectNameMap.put( key, objectNameMap.get( key ) );
		}
		return clusteredServerConfigObjectNameMap;
	}
	
	
}



