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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/j2ee/J2EEClusterImpl.java,v 1.6 2006/03/17 03:34:18 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2006/03/17 03:34:18 $
 */
 
package com.sun.enterprise.management.j2ee;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.j2ee.J2EECluster;

import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.j2ee.StateManageable;


import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.oldconfig.OldClusterMBean;
import com.sun.enterprise.management.support.oldconfig.OldClustersMBean;

import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

/**
	JSR 77 extension representing an Appserver Cluster
 */
public final class J2EEClusterImpl
	extends J2EELogicalServerImplBase 
{
		public
	J2EEClusterImpl( final Delegate delegate )
	{
		super( J2EETypes.J2EE_CLUSTER, delegate );
	}
	
		public String[]
	getServerNames()
	{
		/*
			J2EEServer is not a subtype of J2EECluster in the containment hierarchy. 
			So the following doesnot work.			
			return getContaineeNamesOfType( J2EETypes.J2EE_SERVER );
		 */
		return getOldClusterMBean().listServerInstancesAsString( false );
    }
	
		public Map<String,ObjectName>
	getServerObjectNameMap()
	{
		/*
			J2EEServer is not a subtype of J2EECluster in the containment hierarchy. 
			So the following does not work.
            return getContaineeObjectNameMap( J2EETypes.J2EE_SERVER );
         */
		final Set<String> serverNamesInCluster	= GSetUtil.newStringSet( getServerNames() );
		if ( serverNamesInCluster.size() == 0 )
		{
			return Collections.emptyMap();
		}
		final Set<ObjectName> allJ2EEServerObjectNames = 
			getQueryMgr().queryJ2EETypeObjectNameSet( J2EETypes.J2EE_SERVER );
		final Map<String,ObjectName> objectNameMap = Util.createObjectNameMap( allJ2EEServerObjectNames );

		final Map<String,ObjectName> serverObjectNameMap =
		    new HashMap<String,ObjectName>( serverNamesInCluster.size() );

		for( final String nameKey : serverNamesInCluster )
		{
			serverObjectNameMap.put( nameKey, objectNameMap.get( nameKey ) );
		}
		return serverObjectNameMap;
	}

		public boolean
	isstateManageable()
	{
		return true;
	}

		public void
	start()
	{
		trace( "J2EEClusterImpl.start" );
		getOldClusterMBean().start();
		setstartTime( System.currentTimeMillis() );
	}

		public void
	startRecursive()
	{
		start();
	}

		public void
	stop()
	{
		trace( "J2EEClusterImpl.start" );
		getOldClusterMBean().stop();
		setstartTime( 0 );
	}

		public int
	getstate()
	{
		final RuntimeStatusList rsl = getRuntimeStatus();
		int state = rsl.anyRunning() ? 
			StateManageable.STATE_RUNNING : StateManageable.STATE_STOPPED;
		return state;
	}
		private OldClusterMBean
	getOldClusterMBean()
	{
		return( getOldConfigProxies().getOldClusterMBean( getSelfName() ) );
	}

		private RuntimeStatusList
	getRuntimeStatus()
	{
		/**
			Should have called OldClusterMBean.getRuntimeStatus() instead. 
			But was getting back AttributeNotFoundException. Maybe bacause
			RuntimeStatus is not exposed as an attribute of the ClusterConfigMBean.
		 */
		final OldClustersMBean oldMBean = 
			getOldConfigProxies().getOldClustersMBean();
		return( ( RuntimeStatusList )oldMBean.getRuntimeStatus( getSelfName() ) );
	}
}
