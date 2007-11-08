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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.oldconfig.OldThreadPoolsConfigMBean;


import com.sun.appserv.management.config.ThreadPoolConfigKeys;



/**
	Configuration for the &lt;thread-pools&gt; element.
 */
public final class ThreadPoolConfigFactory  extends ConfigFactory
{
		public
	ThreadPoolConfigFactory( final ConfigFactoryCallback	callbacks )
	{
		super( callbacks );
	}
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		ThreadPoolConfigKeys.MIN_THREAD_POOL_SIZE_KEY,
		ThreadPoolConfigKeys.MAX_THREAD_POOL_SIZE_KEY,
		ThreadPoolConfigKeys.IDLE_THREAD_TIMEOUT_IN_SECONDS_KEY,
		ThreadPoolConfigKeys.NUM_WORK_QUEUES_KEY
	);
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	
		private OldThreadPoolsConfigMBean
	getOldThreadPoolsConfigMBean( )
	{
		return getOldConfigProxies().getOldThreadPoolsConfigMBean( getConfigName() );
	}
	
	/**
		Create a new &lt;thread-pool>
		
		@param name			name of the &lt;thread-pool> (thread-pool-id)
		@param optional
		@return ObjectName of the newly-created ThreadPoolConfig
	 */
		public ObjectName
	create( String name, Map<String,String> optional )
	{
		final Map<String,String> params	= initParams( name, null, optional );
		
		final ObjectName	amxName	= createNamedChild( name, params );
		
		return( amxName );
	}

		public void
	internalRemove( final ObjectName objectName)
	{
		final String	name	= Util.getName( objectName );
		
		getOldThreadPoolsConfigMBean().removeThreadPoolByThreadPoolId( name );
	}

		protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "thread-pool-id" ) );
	}

		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		final ObjectName	objectName	=
				getOldThreadPoolsConfigMBean().createThreadPool( translatedAttrs );
		
		return( objectName );
	}

}

