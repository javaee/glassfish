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
package com.sun.enterprise.management.support.oldconfig;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;

import javax.management.ObjectName;


import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.XTypes;
import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.TypeInfo;
import com.sun.enterprise.management.support.TypeInfos;
import com.sun.enterprise.management.support.OldTypeToJ2EETypeMapper;

import com.sun.appserv.management.util.stringifier.SmartStringifier;

/**
	Extracts relevant properties from "old" ObjectNames.
 */
public final class OldProps
{
	private final ObjectName	        mObjectName;
	private final Map<String,String>	mValues;
	
	private static final String	SERVER_CONFIG_KEY	= "ServerConfig";
	
	    private static void
	debug( final Object o )
	{
	    AMXDebug.getInstance().getOutput( "OldProps" ).println( o );
	}
	
	/**
		These are the possible key values in an old ObjectName that
		contain the name of the config element.
	 */
	static private final List<String>	POSSIBLE_NAME_KEYS	= Collections.unmodifiableList(
        ListUtil.newListFromArray( new String[]
	{
		"name",
		"id",
		"ref",
		"thread-pool-id",
		"jndi-name",
		"resource-adapter-name",
		"provider-id",
		"connector-resource-jndi-name",
		"auth-layer",
	} ));
	
	    public static List<String>
	getPossibleNameKeys()
	{
	    return POSSIBLE_NAME_KEYS;
	}

	
		public ObjectName
	getOldObjectName()
	{
		return( mObjectName );
	}
	
	/**
		Get a String consisting of all new properties suitable for use
		in creating a new ObjectName.
	 */
		public String
	getNewProps()
	{
		return( JMXUtil.mapToProps( mValues ) );
	}
	
	/**
		Determine the name of an item from the old ObjectName.
	 */
		private String
	getOldName( final ObjectName oldObjectName )
	{
		String	oldName	= null;
		
		for( final String key : getPossibleNameKeys() )
		{
			oldName	= oldObjectName.getKeyProperty( key );
			if ( oldName != null )
			{
				break;
			}
		}
		
		// special case for 'root' mbean, which is monitoring root
		if ( oldName == null && oldObjectName.getKeyProperty( "type" ).equals( "root" ) )
		{
			oldName	= oldObjectName.getKeyProperty( "server" );
		}
		
		return( oldName );
	}
	
		private static String
	toString( final Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	
	/**
		Find the first key that is present in the ObjectName
		
		@return first key present in the ObjectName
	 */
		public static Set
	j2eeTypesToOldTypes(
		final Set<String>			j2eeTypes,
		final OldTypeToJ2EETypeMapper	mapper)
	{
		final Set<String>		old		= new HashSet<String>();
		
		String	match	= null;
		for ( final String j2eeType : j2eeTypes )
		{
			final String	oldType	= mapper.j2eeTypeToOldType( j2eeType );
			if ( oldType != null )
			{
				old.add( oldType );
			}
		}
		
		return( old );
	}
	
	

		public
	OldProps(
		final ObjectName oldObjectName,
		final OldTypeToJ2EETypeMapper	mapper)
	{
		mObjectName	= oldObjectName;
		mValues	= new HashMap<String,String>();
		
		// determine the new type from the old
		final String	j2eeType	= mapper.oldObjectNameToJ2EEType( oldObjectName );
		if ( j2eeType == null )
		{
		    throw new IllegalArgumentException( "" + oldObjectName );
		}
		mValues.put( AMX.J2EE_TYPE_KEY, j2eeType );
		
		// determine the name
		final String	oldName	= getOldName( oldObjectName );
		final String	name	= oldName != null ?
			oldName : ObjectNames.getSingletonName( j2eeType );
		mValues.put( AMX.NAME_KEY, name );
		
		final Set<String>   keys    = TypeCast.asSet(oldObjectName.getKeyPropertyList().keySet());
		for( final String prop : keys )
		{
			final String	newType	= mapper.oldTypeToJ2EEType( prop, oldObjectName );
			
			if ( newType != null )
			{
				mValues.put( newType, oldObjectName.getKeyProperty( prop ) );
			}
		}
		
		// special-cases
		
		if ( j2eeType.equals( XTypes.JMX_CONNECTOR_CONFIG ) )
		{
			/*
				JMX_CONNECTOR_CONFIG MBean has two possible parents,
				but neither is present in the old "jmx-connector" mbean, 
				which screws things up for itself and its children (SSL_CONFIG).
			 */
			if ( mValues.containsKey( XTypes.CONFIG_CONFIG )  )
			{
				mValues.put( XTypes.ADMIN_SERVICE_CONFIG,
					ObjectNames.getSingletonName( XTypes.ADMIN_SERVICE_CONFIG ) );
			}
			else if ( mValues.containsKey( XTypes.NODE_AGENT_CONFIG ) )
			{
				// OK
			}
			else
			{
				throw new IllegalArgumentException( "unrecognized ObjectName for jmx-connector" );
			}
		}
		else if ( j2eeType.equals( XTypes.SSL_CONFIG ) )
		{
			if ( mValues.containsKey( XTypes.NODE_AGENT_CONFIG ) )
			{
				/**
					Old object name for "node-agent/jmx-connector/ssl" doesnot 
					have jmx-connector property.
				 */
				mValues.put( XTypes.JMX_CONNECTOR_CONFIG,
					ObjectNames.getSingletonName( XTypes.JMX_CONNECTOR_CONFIG ) );
			}
			else if ( mValues.containsKey( XTypes.JMX_CONNECTOR_CONFIG )  )
			{
				mValues.put( XTypes.ADMIN_SERVICE_CONFIG,
					ObjectNames.getSingletonName( XTypes.ADMIN_SERVICE_CONFIG ) );
			}
			else if (	mValues.containsKey( XTypes.CONFIG_CONFIG )  && 
						!mValues.containsKey( XTypes.HTTP_LISTENER_CONFIG ) && 
						!mValues.containsKey( XTypes.IIOP_LISTENER_CONFIG ) )
			{
				mValues.put( XTypes.IIOP_SERVICE_CONFIG,
					ObjectNames.getSingletonName( XTypes.IIOP_SERVICE_CONFIG ) );
			}
		}
		else if ( j2eeType.equals( XTypes.AUTH_REALM_CONFIG ) )
		{
			/*
				AUTH_REALM_CONFIG MBean has two possible parents,
				but neither is present in the old "auth-realm" mbean when it
				is a child of security service.
			 */
			if ( mValues.containsKey( XTypes.CONFIG_CONFIG )  )
			{
				mValues.put( XTypes.SECURITY_SERVICE_CONFIG,
					ObjectNames.getSingletonName( XTypes.SECURITY_SERVICE_CONFIG ) );
			}
			else if ( mValues.containsKey( XTypes.NODE_AGENT_CONFIG ) )
			{
				// OK
			}
			else
			{
				throw new IllegalArgumentException( "unrecognized ObjectName for ssl or jmx-connector" );
			}
		 }
	}
	
	
	
}
	






