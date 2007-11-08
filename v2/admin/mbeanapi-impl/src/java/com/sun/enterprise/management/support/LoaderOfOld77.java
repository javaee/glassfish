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

import java.util.logging.Logger;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;


import com.sun.enterprise.management.support.oldconfig.OldProps;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.SystemInfo;

import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.DomainConfig;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.StringUtil;

import com.sun.appserv.management.j2ee.J2EETypes;


/**
	Loads MBeans.
 */
final class LoaderOfOld77 extends LoaderOfOld
{
	private final String	mAMXJMXDomain;
	/**
		Whether this process is the DAS.
	 */
	private final boolean	mIsDAS;
	
	LoaderOfOld77( final Loader loader )
	{
		super( loader );
		
		mAMXJMXDomain	= loader.getAMXJMXDomainName();
		
		final SystemInfo	systemInfo	= loader.getDomainRoot().getSystemInfo();
		mIsDAS	= systemInfo.supportsFeature( SystemInfo.RUNNING_IN_DAS_FEATURE );
	}
	
		public Set<ObjectName>
	findAllOldCandidates()
	{
		final ObjectName	pattern	= JMXUtil.newObjectName( "com.sun.appserv", JMXUtil.WILD_ALL );
		final Set<ObjectName>	all	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		
		final Set<ObjectName>	results	= new HashSet<ObjectName>();
		for( final ObjectName objectName : all )
		{
			if ( shouldSync( objectName ) )
			{
				results.add( objectName );
			}
		}
		
		return( results );
	}

	
	

		private boolean
	isOld77ObjectName( final ObjectName	objectName )
	{
		final String	j2eeType	= objectName.getKeyProperty( "j2eeType" );
		
	    boolean ret = ( j2eeType != null &&
			! objectName.getDomain().equals( mAMXJMXDomain ) );

            return ret;
	}
	
	/**
	 */
	public static final Set	SYNC_TYPES	= GSetUtil.newUnmodifiableStringSet(
			J2EETypes.J2EE_DOMAIN,
			J2EETypes.J2EE_SERVER,
			J2EETypes.J2EE_APPLICATION,
			J2EETypes.APP_CLIENT_MODULE,
			J2EETypes.EJB_MODULE,
			J2EETypes.WEB_MODULE,
			J2EETypes.RESOURCE_ADAPTER_MODULE,
			J2EETypes.RESOURCE_ADAPTER,
			J2EETypes.ENTITY_BEAN,
			J2EETypes.STATEFUL_SESSION_BEAN,
			J2EETypes.STATELESS_SESSION_BEAN,
			J2EETypes.MESSAGE_DRIVEN_BEAN,
			J2EETypes.SERVLET,
			J2EETypes.JAVA_MAIL_RESOURCE,
			J2EETypes.JCA_RESOURCE,
			J2EETypes.JCA_CONNECTION_FACTORY,
			J2EETypes.JCA_MANAGED_CONNECTION_FACTORY,
			J2EETypes.JDBC_RESOURCE,
			J2EETypes.JDBC_DATA_SOURCE,
			J2EETypes.JDBC_DRIVER,
			J2EETypes.JMS_RESOURCE,
			J2EETypes.JNDI_RESOURCE,
			J2EETypes.JTA_RESOURCE,
			J2EETypes.RMI_IIOP_RESOURCE,
			J2EETypes.URL_RESOURCE,
			J2EETypes.JVM,
			J2EETypes.WEB_SERVICE_ENDPOINT );
	
	
	
		protected Set
	getNeedsSupport()
	{
		return( Collections.EMPTY_SET );
	}

		protected Set
	getIgnoreTypes()
	{
		return( Collections.EMPTY_SET );
	}
	
		private boolean
	isValidCompositeWebModuleName( final String compositeName )
	{
	    boolean valid   = false;
	    
	    try
	    {
		    final String	webModuleName	= WebModuleSupport.extractWebModuleName( compositeName );
		
		    // An empty name is valid, too, if it's the default web module.
		    valid   = true;
		}
		catch( Exception e )
		{
		}
		return valid;
	}
	
		private boolean
	hasValidWebModuleName( final ObjectName objectName )
	{
		boolean	isValid	= true;
		
		final String	j2eeType	= Util.getJ2EEType( objectName );
			
		// Our WebModule names are of the form //<virtual-server-name>/<web-module-name>.
		// some of them are "defective" and lack a <web-module-name> part
		// no empty web-module names allowed (bug in underlying MBean naming), see bug #6180648
		if (  j2eeType.equals( J2EETypes.SERVLET ) )
		{
            final String	compositeName	= objectName.getKeyProperty( J2EETypes.WEB_MODULE );
            isValid	= isValidCompositeWebModuleName( compositeName );
        }
        else if (j2eeType.equals( J2EETypes.WEB_SERVICE_ENDPOINT))
        {
            // check if this web service endpoint belong to EJB module
            final String ejbModName = objectName.getKeyProperty( J2EETypes.EJB_MODULE);
            if (ejbModName != null)
            {
                isValid = true;
            }
            else
            {
                final String	compositeName	= objectName.getKeyProperty( J2EETypes.WEB_MODULE );
                isValid	= isValidCompositeWebModuleName( compositeName );
            }
		}
		else if ( j2eeType.equals( J2EETypes.WEB_MODULE ) )
		{
			final String	compositeName	= objectName.getKeyProperty( AMX.NAME_KEY );
			isValid	= isValidCompositeWebModuleName( compositeName );
		}
		
		return( isValid  );
	}

		public boolean
	isOldMBean( final ObjectName objectName )
	{
		boolean	shouldSync	= isOld77ObjectName( objectName );
		
		if ( shouldSync )
		{
			final String	j2eeType	= Util.getJ2EEType( objectName );
			shouldSync	= SYNC_TYPES.contains( j2eeType );
		
			if ( shouldSync && ! hasValidWebModuleName( objectName ) )
			{
			    // the MBean will load just fine, but other downstream problems will occur
				shouldSync	= false;
		
				getLogger().warning(
					"Not registering AMX MBean against old MBean " + StringUtil.quote( objectName ) +
					" due to malformed composite WebModule name." );
			}
		}
		
		return( shouldSync );
	}
	
	/**
		Containment keys specified by JSR 77 ObjectNames which we must preserve
		from old to new ObjectName
	 */
	private final static String[]	CONTAINMENT_KEYS_77	= new String[]
	{
		AMX.NAME_KEY,
		AMX.J2EE_TYPE_KEY,
		J2EETypes.J2EE_DOMAIN,
		J2EETypes.J2EE_SERVER,
		J2EETypes.J2EE_APPLICATION,
		J2EETypes.EJB_MODULE,
		J2EETypes.WEB_MODULE,
		J2EETypes.RESOURCE_ADAPTER_MODULE,
		J2EETypes.JDBC_RESOURCE,
		J2EETypes.JCA_RESOURCE,
		J2EETypes.WEB_SERVICE_ENDPOINT,
	};
	
		private Map<String,String>
	getOld77Props( final ObjectName	oldObjectName )
	{
		final Map<String,String>	m	= new HashMap<String,String>();
		
		for( int i = 0; i < CONTAINMENT_KEYS_77.length; ++i )
		{
			final String	key	= CONTAINMENT_KEYS_77[ i ];
			
			final String	value	= oldObjectName.getKeyProperty( key );
			if ( value != null )
			{
				m.put( key, value );
			}
		}
		
		return( m );
	}
	
	/**
		JSR77 has some inconsistent approaches to its containment hierarchy; it omits
		implied parents, which causes headaches for generic navigation.  This routine
		accounts for those issues.
	 */
		protected ObjectName
	oldToNewObjectName( final ObjectName	oldObjectName )
	{
		final String		j2eeType	= Util.getJ2EEType( oldObjectName );
		final String		j2eeDomainName	= getAMXJMXDomainName();
		
		final Map<String,String>	propsMap	= getOld77Props( oldObjectName );
		if ( j2eeType.equals( J2EETypes.J2EE_DOMAIN ) )
		{
			// name must be changed to match domain
			propsMap.put( AMX.NAME_KEY, j2eeDomainName );
		}
		else if ( j2eeType.equals( J2EETypes.J2EE_SERVER ) )
		{
		}
		else
		{
			// everything else has a parent
		}

		
		final String	props	= JMXUtil.mapToProps( propsMap );
		
		final ObjectName	newName	= Util.newObjectName( j2eeDomainName, props );
		
		return( newName );
	}


	
		private final boolean
	serverRunningInDAS( final String serverName )
	{
		boolean runningInDAS	= false;
		
		if ( mIsDAS )
		{
			// Simplifying assumption: only one server runs in DAS
			runningInDAS	= true;
		}
		
		return( runningInDAS );
	}
	
		protected Class
	getImplClass(
		final ObjectName	newObjectName,
		final ObjectName	oldObjectName )
	{
		Class	implClass	= super.getImplClass( newObjectName, oldObjectName );
		
		final boolean	isJ2EEServer	=
			Util.getJ2EEType( newObjectName ).equals( J2EETypes.J2EE_SERVER );
		if ( isJ2EEServer )
		{
			// it's a J2EEServer; determine if it's running in the DAS itself
			// in which case we need a different impl
			final String	serverName	= Util.getName( newObjectName );
			final boolean	isAdminServer	= serverRunningInDAS( serverName );
			if ( isAdminServer )
			{
			
				try
				{
					implClass = 
					ClassUtil.getClassFromName( "com.sun.enterprise.management.j2ee.DASJ2EEServerImpl");
				}
				catch (ClassNotFoundException cnfe)
				{
					try
					{
					implClass	= ClassUtil.getClassFromName(
						"com.sun.enterprise.management.j2ee.J2EEServerImpl" );
					}
					catch( ClassNotFoundException e )
					{
						throw new RuntimeException( e );
					}
				}
			}
			getLogger().fine(
			    "LoaderOfOld77.getImplClass: Using J2EEServer impl of class: " +
			    implClass.getName() +
				" for server " + serverName );
			
		}
		
		return( implClass );
	}

}
