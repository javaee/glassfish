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
package com.sun.appserv.management.base;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.io.Serializable;

import java.lang.reflect.Proxy;

import javax.management.ObjectName;
import javax.management.Notification;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.TypeCast;

/**
	Utility routines pertinent to the MBean API.
 */
public final class Util
{
	private	Util()	{}
	
	/**
		Create a new ObjectName, caller is guaranteeing that the name is
		well-formed (a RuntimeException will be thrown if not). This avoids
		having to catch all sorts of JMX exceptions.<br>
		<b>NOTE:</b> Do not call this method if there is not certainty of a well-formed name.
		
		@param name
	 */
		public static ObjectName
	newObjectName( String name )
	{
		return( JMXUtil.newObjectName( name ) );
	}
	
	
	/**
		Build an ObjectName.  Calls newObjectName( domain + ":" + props )
		
		@param domain	the JMX domain
		@param props	properties of the ObjectName
	 */
		public static ObjectName
	newObjectName( String domain, String props )
	{
		return( newObjectName( domain + ":" + props ) );
	}
	
	/**
		Build an ObjectName pattern.  
		
		@param domain	the JMX domain
		@param props	properties of the ObjectName
	 */
		public static ObjectName
	newObjectNamePattern(
		final String domain,
		final String props )
	{
		return( JMXUtil.newObjectNamePattern( domain, props ) );
	}
	
	/**
		Build an ObjectName pattern.
		
		@param objectName
	 */
		public static ObjectName
	newObjectNamePattern( ObjectName objectName )
	{
		final String	props	= objectName.getKeyPropertyListString();
		
		return( newObjectNamePattern( objectName.getDomain(), props) );
	}
	
	
	/**
		Make an ObjectName property of the form <i>name</i>=<i>value</i>.
		
		@param name
		@param value
	 */
		public static String
	makeProp(
		final String name,
		final String value )
	{
		return( JMXUtil.makeProp( name, value ) );
	}
	
	/**
		Make an ObjectName property of the form j2eeType=<i>value</i>.
		
		@param value
	 */
		public static String
	makeJ2EETypeProp( final String value )
	{
		return( makeProp( AMX.J2EE_TYPE_KEY, value ) );
	}
	
	/**
		Make an ObjectName property of the form name=<i>value</i>.
		
		@param value
	 */
		public static String
	makeNameProp( final String value )
	{
		return( makeProp( AMX.NAME_KEY, value ) );
	}

	/**
		@param j2eeType
		@param name
	 */
		public static String
	makeRequiredProps(
		final String j2eeType,
		final String name )
	{
		final String	j2eeTypeProp	= Util.makeJ2EETypeProp( j2eeType );
		final String	nameProp		= Util.makeNameProp( name );
		
		final String	props	= Util.concatenateProps( j2eeTypeProp, nameProp );
		
		return( props );
	}
	
	/**
		Extract the j2eeType and name properties and return it as a single property
		<i>j2eeType</i>=<i>name</i>
		
		@param objectName
	 */
		public static String
	getSelfProp( final ObjectName objectName )
	{
		final String	j2eeType	= objectName.getKeyProperty( AMX.J2EE_TYPE_KEY );
		final String	name		= objectName.getKeyProperty( AMX.NAME_KEY );
		
		return( Util.makeProp( j2eeType, name ) );
	}
    
    /**
		Extract all properties other than j2eeType=<type>,name=<name>.
		
		@param objectName
	 */
		public static String
	getAdditionalProps( final ObjectName objectName )
	{
        final java.util.Hashtable allProps = objectName.getKeyPropertyList();
        allProps.remove( AMX.J2EE_TYPE_KEY );
        allProps.remove( AMX.NAME_KEY );
        
        String props = "";
        for( final Object key : allProps.keySet() )
        {
            final String prop = makeProp( (String)key, (String)allProps.get(key) );
            props = concatenateProps( props, prop );
        }
		
		return props;
	}
	
	
	/**
		Get properties corresponding to the FullType of this ObjectName.
		Its j2eeType/name are included as <i>j2eeType</i>=<i>name</i>, <b>not</b>
		as j2eeType=<i>j2eeType</i>,name=<i>name</i>.
		
		@param objectName
		@param fullType
		@return String of relevant ObjectName properties
	 */
		public static String
	getFullTypeProps(
		final ObjectName	objectName,
		final String		fullType )
	{
		final String selfProp	= Util.getSelfProp( objectName );
			
		// now add properties for ancestors; skip the last type; it's
		// present in the j2eeType/name properties (selfProp)
		String	ancestorProps	= "";
		final String[]	types	= Util.getTypeArray( fullType );
		for( int i = 0; i < types.length - 1; ++i )
		{
			final String	key		= types[ i ];
			final String	value	= objectName.getKeyProperty( key );
			final String	prop	= Util.makeProp( key, value );
			
			ancestorProps	= Util.concatenateProps( ancestorProps, prop );
		}
		
		final String	props	= Util.concatenateProps( selfProp, ancestorProps );
		
		return( props );
	}
	
	/**
		Get the value of the AMX.NAME_KEY property within the ObjectName
		or AMX.NO_NAME if not present.
		
		@return the name
	 */
		public static String
	getName( final ObjectName	objectName )
	{
		String	name	= objectName.getKeyProperty( AMX.NAME_KEY );
		
		if ( name == null )
		{
			name	= AMX.NO_NAME;
		}

		return( name  );
	}
	
		public static String
	getJ2EEType( final ObjectName	objectName )
	{
		return( objectName.getKeyProperty( AMX.J2EE_TYPE_KEY )  );
	}
    
		public static String
	getJ2EEType( final Class<? extends AMX> amxInterface )
	{
        final String fieldName = "J2EE_TYPE";
        
        try {
            final java.lang.reflect.Field field = amxInterface.getField( fieldName );
            return String.class.cast( field.get(null) );
        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( "Missing J2EE_TYPE field in interface " + amxInterface.getName() );
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Can't access J2EE_TYPE field in " + amxInterface.getName() );
        }
	}
	
	/**
		Get the FullType as a String[], last element being the j2eeType.
		
		@param fullType as returned from {@link AMX#getFullType}
	 */
		public static String[]
	getTypeArray( final String fullType )
	{
		if ( fullType == null )
		{
			throw new IllegalArgumentException();
		}
		
		assert( AMX.FULL_TYPE_DELIM.equals( "." ) );
		return( fullType.split( "\\." )  );
	}
	
	
	/**
		Minimal ObjectName properties required for an ObjectName pattern
		to uniquely identify an MBean.  See {@link #getObjectNamePattern}.
	 */
	private static final Set<String> PATTERN_PROPS	=
	    GSetUtil.newUnmodifiableStringSet(
		AMX.J2EE_TYPE_KEY,
		AMX.NAME_KEY );


	/**
		Get all keys required for an ObjectName pattern which uniquely
		identifies the MBean.
	 */
		public static Set<String>
	getPatternKeys(
		final String		fullType )
	{
		final Set<String> requiredKeys	= GSetUtil.copySet( PATTERN_PROPS );
		
		// omit the last one, it is the simple type of this MBean, which we've
		// already included
		final String[]	types	= Util.getTypeArray( fullType );
		for( int i = 0; i < types.length - 1; ++i )
		{
			requiredKeys.add( types[ i ] );
		}

		return TypeCast.checkedStringSet( requiredKeys  );
	}
	
		public static String
	concatenateProps(
		final String props1,
		final String props2 )
	{
		return( JMXUtil.concatenateProps( props1, props2 ) );
	}
	
		public static String
	concatenateProps(
		final String props1,
		final String props2,
		final String props3 )
	{
		return( concatenateProps( concatenateProps( props1, props2), props3) );
	}
	
	/**
		@return a Set of ObjectNames from a Set of AMX.
	 */
		public static Set<ObjectName>
	toObjectNames( final Set<? extends AMX> amxs )
	{
		final Set<ObjectName>	objectNames	= new HashSet<ObjectName>();
		for( final AMX next : amxs )
		{
			objectNames.add( getObjectName( next ) );
		}
		return( Collections.checkedSet(objectNames, ObjectName.class) );
	}
	
	/**
		@return a Map of ObjectNames from a Map whose values are AMX.
	 */
		public static Map<String,ObjectName>
	toObjectNames( final Map<String,? extends AMX> amxMap )
	{
		final Map<String,ObjectName>	m	= new HashMap<String,ObjectName>();
		
		for( final String key : amxMap.keySet() )
		{
			final AMX	value	= amxMap.get( key );
			m.put( key, getExtra( value ).getObjectName() );
		}
		return( Collections.checkedMap( m, String.class, ObjectName.class) );
	}
	
	/**
		@return an ObjectName[] from an AMX[]
	 */
		public static ObjectName[]
	toObjectNames( final AMX[] amx )
	{
		final ObjectName[]	objectNames	= new ObjectName[ amx.length ];
		for( int i = 0; i < objectNames.length; ++i )
		{
			objectNames[ i ]	= amx[ i ] == null ? null : getExtra( amx[ i ] ).getObjectName();
		}
		
		return( objectNames );
	}
	
	/**
		Extract the names from all ObjectNames.  The name is the value of the
		property NAME_KEY (See {@link AMX}).  Note that if two or more ObjectNames
		share the same name, the resulting Set will be of smaller size() than
		the original.
		
		@return Set
	 */
		public static Set<String>
	getNames( final Set<? extends AMX>	amxs )
	{
		return getNamesSet( Util.toObjectNames( amxs ) );
	}
	
	/**
		Extract the names from all ObjectNames.  The name is the value of the
		property NAME_KEY (See {@link AMX}).  Note that if two or more ObjectNames
		share the same name, the resulting Set will be of smaller size() than
		the original.
		
		@return Set
	 */
		public static Set<String>
	getNamesSet( final Set<ObjectName>	objectNames )
	{
		return TypeCast.checkedStringSet(
		        JMXUtil.getKeyPropertySet( AMX.NAME_KEY, objectNames ) );
	}
	
	/**
		Extract the names from all ObjectNames.
		
		@return String[] of names from the ObjectNames
	 */
		public static String[]
	getNamesArray( final Set<ObjectName>	objectNames )
	{
		return( JMXUtil.getKeyProperty( AMX.NAME_KEY, objectNames ) );
	}
	
	/**
		Create a Map keyed by the value of the AMX.NAME_KEY with
		value the ObjectName. Note that if two or more ObjectNames
		share the same name, the resulting Map will contain only
		one of the original ObjectNames.
		
		@param objectNames Set of ObjectName
	 */
		public static final Map<String,ObjectName>
	createObjectNameMap( final Set<ObjectName> objectNames )
	{
		final Map<String,ObjectName>	m	= new HashMap<String,ObjectName>();
		
		for( final ObjectName objectName : objectNames )
		{
			final String		name	= getName( objectName );
			
			assert( ! m.containsKey( name ) ) :
				"createObjectNameMap: key already present: " + name + " in " + objectName;
			m.put( name, objectName );
		}
		
		assert( m.keySet().size() == objectNames.size() );
		
		return( Collections.checkedMap(m, String.class, ObjectName.class) );
	}
	
	
	/**
		Create a Map keyed by the value of the AMX.NAME_KEY with
		value the AMX item.
		
		@param amxs Set of AMX
	 */
		public static <T extends AMX> Map<String,T>
	createNameMap( final Set<T> amxs )
	{
		final Map<String,T>	m	= new HashMap<String,T>();
		
		for( final T amx : amxs )
		{
			final String	name	= amx.getName();
			m.put( name, amx );
		}
		
		return( m );
	}
	
	/**
		Get extra information about this {@link AMX}.
		'Extra' is not an MBean Attribute; it exists only in the {@link AMX}.
		
		@param proxy an AMX
	 */
		public static Extra
	getExtra( final AMX proxy )
	{
		return( (Extra)Proxy.getInvocationHandler( proxy ) );
	}
	
	
	/**
		Get the ObjectName targeted by this {@link AMX}.
		
		@param proxy an AMX
	 */
		public static <T extends AMX> ObjectName
	getObjectName( final T proxy )
	{
		return( getExtra( proxy ).getObjectName() );
	}
	
	
	/**
		Get an ObjectName from a Map of ObjectName where the
		values are keyed by name.
		
		@param candidates
		@param name
		@return ObjectName
		@throws IllegalArgumentException if not found
	 */
		public static ObjectName
	getObjectName(
		final Map<String,ObjectName> candidates,
		final String	name )
	{
		final Object	item	= candidates.get( name );
		if ( item == null )
		{
			throw new IllegalArgumentException( "Not found: " + name );
		}
		
		return (ObjectName)item;
	}
	
	/**
		All Notifications emitted by AMX MBeans which are not
		standard types defined by JMX place a Map
		into the userData field of the Notification.  This call
		retrieves that Map, which may be null if no additional
		data is included.
	 */
		public static Map<String,Serializable>
	getAMXNotificationData( final Notification notif )
	{
	    return Collections.unmodifiableMap(
	            JMXUtil.getUserDataMapString_Serializable( notif ) );
	}
	
	/**
	    Use of generic type form taking Class<T> is preferred.
	 */
	    public static Serializable
	getAMXNotificationValue( final Notification notif, final String key )
	{
		final Map<String,Serializable> data	=
		    getAMXNotificationData( notif );
		
		if ( data == null )
		{
			throw new IllegalArgumentException( notif.toString() );
		}
		
		if ( ! data.containsKey( key ) )
		{
			throw new IllegalArgumentException( "Value not found for " + key +
				" in " + notif );
		}
		
		return data.get( key );
	}
	
	/**
		Retrieve a particular value associated with the specified
		key from an AMX Notification.
		@see #getAMXNotificationData
	 */
		public static <T extends Serializable> T
	getAMXNotificationValue(
	    final Notification  notif,
	    final String        key,
	    final Class<T>      theClass)
	{
	    final Serializable value    = getAMXNotificationValue( notif, key );
		
		return theClass.cast( value );
	}
	
	    public static void
	sleep( final long millis )
	{
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
		}
	}
	
	
    /**
        A safe way to cast to AMX.
     */
        public static AMX
    asAMX( final Object o)
    {
        return AMX.class.cast( o );
    }
}



















