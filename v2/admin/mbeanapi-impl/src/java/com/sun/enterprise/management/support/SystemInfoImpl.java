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
 
/*
 */

package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
 */
public final class SystemInfoImpl extends AMXImplBase
	implements SystemInfo
{
	private final MBeanServer	mServer;
	
	private BootUtil	mBootUtil;
	
	public static final String	NAME_PROP_VALUE	= "system-info";
	
	private final Map<String,Boolean>	mFeatures;
	
		private final boolean
	supportsClusters( )
	{
		final ObjectName	serversObjectName	= getOldServersMBeanObjectName();
		
		boolean	supportsClusters	= false;
		if ( serversObjectName != null )
		{
			// see if the 'servers' MBean supports listing unclustered instances
			try
			{
				final MBeanInfo				info	= mServer.getMBeanInfo( serversObjectName );
				
				final String	operationName	= "listUnclusteredServerInstancesAsString";
				final Set	operations	= JMXUtil.findInfoByName( info.getOperations(), operationName );
				supportsClusters	= operations.size() != 0;
				
			}
			catch( JMException e )
			{
				// should never happen...
				throw new RuntimeException( "problem with 'servers' MBean: " + serversObjectName, e );
			}
		}
		else
		{
			// presumably, we're in another instance, which implies multiple instances.
			// assume this also means clustering is possible
			supportsClusters	= true;
		}
		
		return( supportsClusters );
	}
    
    
    /*
        <mbean name="hadb-config" group="config" className="com.sun.enterprise.ee.admin.mbeans.HadbConfigMBean">
            <descriptor>
                <field name="ObjectName" value="{0}:type=hadb-config,category=config"/>
            </descriptor>
        </mbean>           
    */
    private void checkHADBAvailable()
    {
        final boolean   available   = mServer.isRegistered(
            com.sun.enterprise.admin.common.ObjectNames.getHADBConfigObjectName() );
        mFeatures.put( HADB_CONFIG_FEATURE, Boolean.valueOf( available ) );
    }
    
         private  void
    _refresh()
    {
        checkHADBAvailable();
    }
    
    private static long LAST_REFRESH    = 0;
        private  void
    refresh()
    {
        final long REFRESH_MILLIS   = 5 * 1000; // 5 seconds
        final long elapsed   = System.currentTimeMillis() - LAST_REFRESH;
        if ( elapsed > REFRESH_MILLIS )
        {
            _refresh();
        }
    }
	
	/**
		Get the ObjectName of the "type=servers" MBean, which only exists in the DAS.
	 */
		private ObjectName
	getOldServersMBeanObjectName()
	{
		// if we find the old "servers" MBean, it should only be running in the DAS.
		final ObjectName	pattern		=
				Util.newObjectName( "com.sun.appserv", "category=config,type=servers" );
		final Set<ObjectName> serversSet	= JMXUtil.queryNames( mServer, pattern, null );
		
		final ObjectName	objectName	= serversSet.size() == 0 ? 
					null : (ObjectName)GSetUtil.getSingleton( serversSet );
					
		return( objectName );
	}
	
		private boolean
	isRunningInDomainAdminServer()
	{
		return( getOldServersMBeanObjectName() != null );
	}
	
		public
	SystemInfoImpl(
		final MBeanServer	server,
		final BootUtil		bootUtil )
	{
		super( );
		
		mServer			= server;
		mBootUtil		= bootUtil;
		
		mFeatures	= new HashMap<String,Boolean>();
		
		final boolean	supportsClusters	= supportsClusters( );
		
		mFeatures.put( CLUSTERS_FEATURE, Boolean.valueOf( supportsClusters ));
		mFeatures.put( MULTIPLE_SERVERS_FEATURE, Boolean.valueOf( supportsClusters ));
		mFeatures.put( RUNNING_IN_DAS_FEATURE, Boolean.valueOf( isRunningInDomainAdminServer() ) );
	}
	
		public final String
	getGroup()
	{
		return( AMX.GROUP_UTILITY );
	}
	
	private static final String[] FEATURE_NAMES	=
		new String[]
		{
			CLUSTERS_FEATURE,
			MULTIPLE_SERVERS_FEATURE,
			RUNNING_IN_DAS_FEATURE,
		};
	
		public String[]
	getFeatureNames()
	{
		return( (String[])FEATURE_NAMES.clone() );
	}
	
		public boolean
	supportsFeature( final String key )
	{
		boolean	supports	= false;
		
		Boolean	result	= mFeatures.get( key );
		if ( result == null )
		{
			result	= Boolean.FALSE;
		}
		
		return( result.booleanValue() );
	}
    
    
    /**
        Return a Map keyed by an arbitrary String denoting some feature.  The value
        is the time in milliseconds.  Code should not rely on the keys as they are subject to 
        changes, additions, or removal at any time, except as otherwise documented.
        Even documented items should be used only for informational purposes,
        such as assessing performance.
        
         @return Map<String,Long>
     */
        public Map<String,Long>
    getPerformanceMillis()
    {
        // ensure that we return a copy which is a HashMap, not some other variant of Map
        final HashMap<String,Long>  result = new HashMap<String,Long>();
        
        result.putAll( SystemInfoData.getInstance().getPerformanceMillis() );
        
        return result;
    }
}








