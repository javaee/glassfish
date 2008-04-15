/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 */
 

package com.sun.cli.jmxcmd.util.jmx;

import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;

import java.lang.reflect.Method;

import javax.management.*;

import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.misc.ArrayUtil;
import com.sun.cli.jcmd.util.misc.SetUtil;
import com.sun.cli.jcmd.util.misc.GSetUtil;
import com.sun.cli.jcmd.util.misc.MapUtil;
import com.sun.cli.jcmd.util.misc.RegexUtil;
import com.sun.cli.jcmd.util.misc.ArrayConversion;

import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanOperationInfoStringifier;
import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanFeatureInfoStringifierOptions;
import com.sun.cli.jmxcmd.util.jmx.stringifier.MBeanAttributeInfoStringifier;
import com.sun.cli.jmxcmd.util.jmx.stringifier.ObjectNameStringifier;

import com.sun.cli.jcmd.util.stringifier.SmartStringifier;



/**
 */
public final class JMXUtil
{
	
	public final static String	MBEAN_SERVER_DELEGATE	=
							"JMImplementation:type=MBeanServerDelegate";
	
	public final static String	MBEAN_SERVER_ID_ATTRIBUTE_NAME	=
							"MBeanServerId";
							
	/**
		The wilcard property at the end of an ObjectName which indicates
		that it's an ObjectName pattern.
	 */
	public final static String WILD_PROP		= ",*";
	
	/**
		The wilcard property at the end of an ObjectName which indicates
		that all properties should be matched.
	 */
	public final static String WILD_ALL			= "*";
	
	
		public static ObjectName
	getMBeanServerDelegateObjectName()
	{
		return( newObjectName( "JMImplementation:type=MBeanServerDelegate" ) );
	}
	
		public static void
	listenToMBeanServerDelegate(
		final MBeanServerConnection	conn,
		final NotificationListener	listener,
		final NotificationFilter	filter,
		final Object				handback)
		throws IOException, InstanceNotFoundException
	{
		conn.addNotificationListener(
			getMBeanServerDelegateObjectName(), listener, filter, handback );
	}
	
		public static String
	getMBeanServerID( final MBeanServerConnection conn )
		throws IOException,
		ReflectionException, InstanceNotFoundException, AttributeNotFoundException,
		MBeanException
	{
		return( (String)conn.getAttribute( getMBeanServerDelegateObjectName(),
					MBEAN_SERVER_ID_ATTRIBUTE_NAME ) );
	}
	
	
	/**
		Create a new ObjectName, caller is guaranteeing that the name is
		well-formed (a RuntimeException will be thrown if not). This avoids
		having to catch all sorts of JMX exceptions.
		<p>
		<b>Do not call this method if there is not 100% certainty of a well-formed name.</b>
	 */
		public static ObjectName
	newObjectName( final String name )
	{
		try
		{
			return( new ObjectName( name ) ); 
		}
		catch( Exception e )
		{
			throw new RuntimeException( e.getMessage(), e );
		}
	}
	
		public static ObjectName
	newObjectName(
		final ObjectName	objectName,
		final String 		props )
	{
		final String	domain	= objectName.getDomain();
		final String	existingProps	= objectName.getKeyPropertyListString();
		final String	allProps	= concatenateProps( existingProps, props );
		return( newObjectName( domain, allProps ) ); 
	}
	
		public static ObjectName
	newObjectName(
		final String	domain,
		final String	props )
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
		String	actualProps	= null;
		
		if ( props.endsWith( JMXUtil.WILD_PROP ) ||
			props.equals( JMXUtil.WILD_ALL ) )
		{
			actualProps	= props;
		}
		else if ( props.length() == 0 )
		{
			actualProps	= "*";
		}
		else
		{
			actualProps	= props + WILD_PROP;
		}
		
		return( newObjectName( domain + ":" + actualProps ) );
	}
	
	
	/**
		Build an ObjectName pattern.
		
		@param domain	the JMX domain
		@param props	properties of the ObjectName
	 */
		public static ObjectName
	newObjectNamePattern(
		final String domain,
		final Map	props )
	{
		final String	propsString	= mapToProps( props );
		
		return( JMXUtil.newObjectNamePattern( domain, propsString ) );
	}
	
		public static String
	mapToProps( final Map	propsMap )
	{
		return( MapUtil.toString( propsMap, "," ) );
	}
	
		public static ObjectName
	removeProperty(
		final ObjectName	objectName,
		final String		key )
	{
		ObjectName	nameWithoutKey	= objectName;
		
		if ( objectName.getKeyProperty( key ) != null )
		{
			final String				domain	= objectName.getDomain();
			final java.util.Hashtable	props	= objectName.getKeyPropertyList();
			
			props.remove( key );
			
			if ( objectName.isPropertyPattern() )
			{
				final String	propsString	= mapToProps( props );
			
				nameWithoutKey	= newObjectNamePattern( domain,
						nameWithoutKey.getKeyPropertyListString() );
			}
			else
			{
				try
				{
				    nameWithoutKey	= new ObjectName( domain, props );
				}
				catch( Exception e )
				{
				    throw new RuntimeException( e );
				}
			}
		}
		
		return( nameWithoutKey );
	}
	
	
		private 
	JMXUtil()
	{
		// disallow;
	}
	
	public static final String	GET	= "get";
	public static final String	SET	= "set";
	public static final String	IS	= "is";
	
		public static String
	makeProp( String name, String value )
	{
		return( name + "=" + value );
	}
	
		public static String
	concatenateProps( String props1, String props2 )
	{
		String	result	= null;
		
		if ( props1.length() == 0 )
		{
			result	= props2;
		}
		else if ( props2.length() == 0 )
		{
			result	= props1;
		}
		else
		{
			result	= props1 + "," + props2;
		}
		
		return( result );
	}
	
		public static String
	concatenateProps( String props1, String props2, String props3 )
	{
		return( concatenateProps( concatenateProps( props1, props2), props3) );
	}
	
	
	
	/**
		Convert a Set of ObjectName into an array
		
		@param objectNameSet	a Set of ObjectName
		@return an ObjectName[]
	 */
		public static ObjectName[]
	objectNameSetToArray( final Set objectNameSet )
	{
		final ObjectName[]	objectNames	= new ObjectName[ objectNameSet.size() ];
		objectNameSet.toArray( objectNames );
		
		return( objectNames );
	}
	
	
	/**
		@param key	the property name, within the ObjectName
		@param objectNames	
		@return values from each ObjectName
	 */
		public static String[]
	getKeyProperty( String key, ObjectName[] objectNames )
	{
		final String[]	values	= new String[ objectNames.length ];
		
		for( int i = 0; i < objectNames.length; ++i )
		{
			values[ i ]	= objectNames[ i ].getKeyProperty( key );
		}
		
		return( values );
	}
	
	/**
		@param objectName
		@param key	
		@return an ObjectName property with the specified key
	 */
		public static String
	getProp(
		final ObjectName	objectName,
		final String		key )
	{
		final String	value	= objectName.getKeyProperty( key );
		if ( value == null )
		{
			return( null );
		}
		
		return( makeProp( key, value ) );
	}
	
		public static String
	getProps(
		final ObjectName	objectName,
		final Set<String>   propKeys )
	{
		return( getProps( objectName, propKeys, false ) );
	}

		public static String
	getProps(
		final ObjectName	objectName,
		final Set<String>	propKeys,
		final boolean		ignoreMissing )
	{
		String	props	= "";
		
		final Iterator	iter	= propKeys.iterator();
		while( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			final String	pair	= getProp( objectName, key );
			if ( pair != null )
			{
				props	= concatenateProps( props, pair );
			}
			else if ( ! ignoreMissing )
			{
				throw new IllegalArgumentException(
					"key not found: " + key + " in " + objectName );
			}
		}
		return( props );
	}
	
	/**
		@param key	the property name, within the ObjectName
		@param objectNameSet
		@return values from each ObjectName
	 */
		public static String[]
	getKeyProperty( String key, Set<ObjectName> objectNameSet )
	{
		final ObjectName[]	objectNames	=
			JMXUtil.objectNameSetToArray( objectNameSet );
		
		return( getKeyProperty( key, objectNames ) );
	}
	

	/**
		@param key	the property name, within the ObjectName
		@param objectNameSet
		@return values from each ObjectName
	 */
		public static Set<String>
	getKeyPropertySet( String key, Set<ObjectName> objectNameSet )
	{
		final ObjectName[]	objectNames	=
			JMXUtil.objectNameSetToArray( objectNameSet );
		
		final String[]	values	= getKeyProperty( key, objectNames );
		
		return( GSetUtil.newStringSet( values ) );
	}
	
	/**
		Find the first key that is present in the ObjectName
		
		@param candidateKeys
		@param objectName
		@return first key present in the ObjectName
	 */
		public static String
	findKey(
		final Set<String>   candidateKeys,
		final ObjectName	objectName )
	{
		final Iterator	iter	= candidateKeys.iterator();
		
		String	match	= null;
		
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			if ( objectName.getKeyProperty( key ) != null )
			{
				match	= key;
				break;
			}
		}
		
		return( match );
	}
	
	
	/**
		Find all ObjectName(s) that contains the associated key and value
		
		@param objectNames
		@param propertyKey
		@param propertyValue
		@return Set of all ObjectName that match
	 */
		public static Set<ObjectName>
	findByProperty(
		final Set<ObjectName>   objectNames,
		final String	propertyKey,
		final String	propertyValue )
	{
		final Set<ObjectName>	result	= new HashSet<ObjectName>();
		
		final Iterator	iter	= objectNames.iterator();
		while ( iter.hasNext() )
		{
			final ObjectName	objectName	= (ObjectName)iter.next();
			
			final String	value	= objectName.getKeyProperty( propertyKey );
			if ( propertyValue.equals( value ) )
			{
				result.add( objectName );
			}
		}
		
		return( result );
	}
	
	/**
		Change or add a key property in an ObjectName.
	 */
		public static ObjectName
	setKeyProperty( final ObjectName objectName, final String key, final String value )
	{
		final String	domain	= objectName.getDomain();
		final java.util.Hashtable	props	= objectName.getKeyPropertyList();
		
		props.put( key, value );
		
		ObjectName	newObjectName	= null;
		try
		{
			newObjectName	= new ObjectName( domain, props );
		}
		catch( MalformedObjectNameException e )
		{
			throw new RuntimeException( e );
		}
		
		return( newObjectName  );
	}
	
		private static String
	toString( Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	
		public static void
	unregisterAll( final MBeanServerConnection conn, final Set<ObjectName> allNames)
		throws IOException, MalformedObjectNameException, MBeanRegistrationException
	{
		final Iterator	iter	= allNames.iterator();
		
		while ( iter.hasNext() )
		{
			final ObjectName	name	= (ObjectName)iter.next();
			
			try
			{
				conn.unregisterMBean( name );
			}
			catch( Exception e )
			{
				// OK, gone, it objects, etc
			}
		}
	}
	
		public static void
	unregisterAll( final MBeanServerConnection conn )
		throws IOException, MalformedObjectNameException, MBeanRegistrationException
	{
		unregisterAll( conn, conn.queryNames( new ObjectName( "*:*" ), null ) );
	}
	
		public static String[]
	getAllAttributeNames(
		final MBeanServerConnection	conn,
		final ObjectName			objectName )
		throws IOException,
			ReflectionException, IntrospectionException, InstanceNotFoundException
	{
		return( getAttributeNames( getAttributeInfos( conn, objectName ) ) );
	}
	
		public static MBeanAttributeInfo[]
	filterAttributeInfos(
		final MBeanAttributeInfo[]	infos,
		final AttributeFilter		filter )
	{
		final ArrayList	matches	= new ArrayList();
		for( int i = 0; i < infos.length; ++i )
		{
			if ( filter.filterAttribute( infos[ i ] ) )
			{
				matches.add( infos[ i ] );
			}
		}
		
		final MBeanAttributeInfo[]	results	= new MBeanAttributeInfo[ matches.size() ];
		matches.toArray( results );
		
		return( results );
	}
	
	/**
		Get a String[] of Attribute names.
		
		@param infos	array of infos
	 */
		public static String []
	getAttributeNames( final MBeanAttributeInfo[]	infos  )
	{
		final String[]	names	= new String[ infos.length ];
		
		for( int i = 0; i < infos.length; ++i )
		{
			names[ i ]	= infos[ i ].getName();
		}
		
		return( names );
	}
	
	/**
		@param infos	array of infos
		@param attrName
	 */
		public static MBeanAttributeInfo
	getMBeanAttributeInfo(
		final MBeanAttributeInfo[]	infos,
		final String				attrName )
	{
		MBeanAttributeInfo	info	= null;
		
		for( int i = 0; i < infos.length; ++i )
		{
			if ( infos[ i ].getName().equals( attrName ) )
			{
				info	= infos[ i ];
				break;
			}
		}
		
		return( info );
	}
	
	/**
		@param mbeanInfo
		@param attrName
	 */
		public static MBeanAttributeInfo
	getMBeanAttributeInfo(
		final MBeanInfo	mbeanInfo,
		final String	attrName )
	{
		return( getMBeanAttributeInfo( mbeanInfo.getAttributes(), attrName ) );
	}
	
	
	/**
		@param conn
		@param objectName
	 */
		public static MBeanAttributeInfo []
	getAttributeInfos( 
		final MBeanServerConnection	conn,
		final ObjectName			objectName )
		throws IOException,
			ReflectionException, IntrospectionException, InstanceNotFoundException
	{
		final MBeanAttributeInfo []	infos	= conn.getMBeanInfo( objectName ).getAttributes();
		
		return( infos );
	}
	
	
	/**
		Convert an AttributeList to a Map where the keys are the Attribute names,
		and the values are Attribute.
		
		@param attrs	the AttributeList
	 */
		public static Map<String,Attribute>
	attributeListToAttributeMap( final AttributeList attrs )
	{
		final HashMap<String,Attribute>	map	= new HashMap<String,Attribute>();
		
		for( int i = 0; i < attrs.size(); ++i )
		{
			final Attribute	attr	= (Attribute)attrs.get( i );
			
			map.put( attr.getName(), attr );
		}
		
		return( map );
	}
	
	/**
		Convert an AttributeList to a Map where the keys are the Attribute names,
		and the values are the Attribute values.
		
		@param attrs	the AttributeList
	 */
		public static <T> Map<String,T>
	attributeListToValueMap( final AttributeList attrs )
	{
		final Map<String,T>	map	= new HashMap<String,T>();
		
		for( int i = 0; i < attrs.size(); ++i )
		{
			final Attribute	attr	= (Attribute)attrs.get( i );
			
		    final T     value   = (T)attr.getValue();
		    
			map.put( attr.getName(), value );
		}
		
		return( map );
	}
	
	/**
		Convert an AttributeList to a Map where the keys are the Attribute names,
		and the values are the Attribute values.
		
		@param attrs	the AttributeList
	 */
		public static Map<String,String>
	attributeListToStringMap( final AttributeList attrs )
	{
		final Map<String,String>	map	= new HashMap<String,String>();
		
		for( int i = 0; i < attrs.size(); ++i )
		{
			final Attribute	attr	= (Attribute)attrs.get( i );
			
			map.put( attr.getName(), attr.getValue().toString() );
		}
		
		return( map );
	}
	
	
		
	/**
		Convert an MBeanAttributeInfo[] to a Map where the keys are the Attribute names,
		and the values are MBeanAttributeInfo.
		
		@param attrInfos	the AttributeList
	 */
		public static Map<String,MBeanAttributeInfo>
	attributeInfosToMap( final MBeanAttributeInfo[] attrInfos )
	{
		final Map<String,MBeanAttributeInfo>	map	= new HashMap();
		
		for( int i = 0; i < attrInfos.length; ++i )
		{
			final MBeanAttributeInfo	attrInfo	= attrInfos[ i ];
			
			map.put( attrInfo.getName(), attrInfo );
		}
		
		return( map );
	}
	
	
		public static MBeanInfo
	removeAttributes(
		final MBeanInfo origInfo,
		final String[]	attributeNames )
	{
		MBeanInfo	result	= origInfo;
		
		if ( attributeNames.length != 0 )
		{
			final Map	infos	= JMXUtil.attributeInfosToMap( origInfo.getAttributes() );
			
			for( int i = 0; i < attributeNames.length; ++i )
			{
				infos.remove( attributeNames[ i ] );
			}
			
			final MBeanAttributeInfo[]	newInfos = new MBeanAttributeInfo[ infos.keySet().size() ];
			infos.values().toArray( newInfos );
			
			result	= new MBeanInfo(
					origInfo.getClassName(),
					origInfo.getDescription(),
					newInfos,
					origInfo.getConstructors(),
					origInfo.getOperations(),
					origInfo.getNotifications() );
		}
	
		return( result );
	}
	
	/**
		Find a feature by name (attribute name, operation name, etc) and return
		all matches.  The feature is matched by calling MBeanFeatureInfo.getName().
		
		@param infos	infos
		@param name	name
		@return Set of the matching items
	 */
		public static Set<MBeanFeatureInfo>
	findInfoByName(
		final MBeanFeatureInfo[]	infos,
		final String				name )
	{
		final Set<MBeanFeatureInfo>	s	= new HashSet<MBeanFeatureInfo>();
		
		for( int i = 0; i < infos.length; ++i )
		{
			final MBeanFeatureInfo	info	= infos[ i ];
			
			if ( info.getName().equals( name ) )
			{
				s.add( info );
			}
		}
		
		return( s );
	}
	
	
	
	/**
		Convert an Map to an Attribute list where the keys are the Attribute names,
		and the values are objects.
		
		@param m
	 */
		public static AttributeList
	mapToAttributeList( final Map<String,Object> m )
	{
		final AttributeList	attrList	= new AttributeList();
		
		for( final String key : m.keySet() )
		{
			final Object	value	= m.get( key );
			
			final Attribute	attr	= new Attribute( key, value );
			
			attrList.add( attr);
		}
		
		return( attrList );
	}
	
	
	/**
		Convert ObjectName into a Set of String.  The resulting
		strings are more readable than just a simple toString() on the ObjectName;
		they are sorted and output in preferential order.
	 */
		public static List<String>
	objectNamesToStrings( final Collection<ObjectName> objectNames )
	{
		// sorting doesn't work on returned array, so convert to Strings first,then sort
		final List<String>	result	= new ArrayList<String>();
		
		for( final ObjectName objectName : objectNames )
		{
			result.add( ObjectNameStringifier.DEFAULT.stringify( objectName ) );
		}
		
		return( result );
	}
	
		public static Set<String>
	objectNamesToStringSet( final Collection<ObjectName> objectNames )
	{
	    final Set<String> s = new HashSet<String>();
	    
	    s.addAll( objectNamesToStrings( objectNames ) );
	    
	    return s;
	}
	
	
	
	/**
		Convert a Set of ObjectName into a Set of String
	 */
		public static String[]
	objectNamesToStrings( final ObjectName[] objectNames )
	{
		final String[]	strings	= new String[ objectNames.length ];
		
		for( int i = 0; i < strings.length; ++i )
		{
			strings[ i ]	= objectNames[ i ].toString();
		}
		
		return( strings );
	}
	
		private static boolean
	connectionIsDead( final MBeanServerConnection conn )
	{
		boolean	isDead	= false;
		
		// see if the connection is really dead by calling something innocuous
		try
		{
			conn.isRegistered( new ObjectName( MBEAN_SERVER_DELEGATE )  );
		}
		catch( MalformedObjectNameException e )
		{
			assert( false );
		}
		catch( IOException e )
		{
			isDead	= true;
		}
		
		return( isDead );
	}
	
		private static AttributeList
	getAttributesSingly(
		MBeanServerConnection	conn,
		ObjectName				objectName,
		String[]				attrNames,
		Set<String>				problemNames )
		throws InstanceNotFoundException
	{
		AttributeList	attrs	= new AttributeList();
		
		for( int i = 0; i < attrNames.length; ++i )
		{
			final String	name	= attrNames[ i ];
			
			try
			{
				final Object	value	= conn.getAttribute( objectName, name );
				
				attrs.add( new Attribute( name, value ) );
			}
			catch( Exception e )
			{
				// if the MBean disappeared while processing, just consider it gone
				// from the start, even if we got some Attributes
				if ( e instanceof InstanceNotFoundException )
				{
					throw (InstanceNotFoundException)e;
				}
				
				if ( problemNames != null )
				{
					problemNames.add( name );
				}
			}
		}
		
		return( attrs );
	}
	
	
	/**
		Get the Attributes using getAttributes() if possible, but if exceptions
		are encountered, attempt to get them one-by-one.
		
		@param conn			the conneciton
		@param objectName	name of the object to access
		@param attrNames	attribute names
		@param problemNames	optional Set to which problem names will be added.
		@return AttributeList
	 */
		public static AttributeList
	getAttributesRobust(
		MBeanServerConnection	conn,
		ObjectName				objectName,
		String[]				attrNames,
		Set<String>				problemNames )
		throws InstanceNotFoundException, IOException
	{
		AttributeList	attrs	= null;
		
		if ( problemNames != null )
		{
			problemNames.clear();
		}
		
		try
		{
			attrs	= conn.getAttributes( objectName, attrNames );
			if ( attrs == null )
			{
				attrs	= new AttributeList();
			}
		}
		catch( InstanceNotFoundException e )
		{
			// if it's not found, we can't do anything about it.
			throw e;
		}
		catch( IOException e )
		{
			if ( connectionIsDead( conn ) )
			{
				throw e;
			}
			
			// connection is still good
			
			attrs	= getAttributesSingly( conn, objectName, attrNames, problemNames );
		}
		catch( Exception e )
		{
			attrs	= getAttributesSingly( conn, objectName, attrNames, problemNames );
		}
		
		return( attrs );
	}
	
	/**
		Caution: this Comparator may be inconsistent with equals() because it ignores the description.
	 */
	public static final class MBeanOperationInfoComparator implements java.util.Comparator
	{
		private static final MBeanOperationInfoStringifier		OPERATION_INFO_STRINGIFIER	=
			new MBeanOperationInfoStringifier( new MBeanFeatureInfoStringifierOptions( false, ",") );
			
			
		public static final MBeanOperationInfoComparator	INSTANCE	= new MBeanOperationInfoComparator();
		
		private	MBeanOperationInfoComparator()	{}
		
			public int
		compare( Object o1, Object o2 )
		{
			final MBeanOperationInfoStringifier	sf	= OPERATION_INFO_STRINGIFIER;
			
			final MBeanOperationInfo	info1	= (MBeanOperationInfo)o1;
			final MBeanOperationInfo	info2	= (MBeanOperationInfo)o2;
			
			// we just want to sort based on name and signature; there can't be two operations with the
			// same name and same signature, so as long as we include the name and signature the
			// sorting will always be consistent.
			int	c	= info1.getName().compareTo( info2.getName() );
			if ( c == 0 )
			{
				// names the same, subsort on signature, first by number of params
				c	= info1.getSignature().length - info2.getSignature().length;
				if ( c == 0 )
				{
					// names the same, subsort on signature, first by number of params
					c	= sf.getSignature( info1 ).compareTo( sf.getSignature( info2 ) );
				}
				
			}
			
			return( c );
		}
		
			public boolean
		equals( Object other )
		{
			return( other instanceof MBeanOperationInfoComparator );
		}
	}
	
	/**
		Caution: this Comparator may be inconsistent with equals() because it ignores the description.
	 */
	public static final class MBeanAttributeInfoComparator implements java.util.Comparator
	{
		private static final MBeanAttributeInfoStringifier		ATTRIBUTE_INFO_STRINGIFIER	=
			new MBeanAttributeInfoStringifier( new MBeanFeatureInfoStringifierOptions( false, ",") );
		
		public static final MBeanAttributeInfoComparator		INSTANCE	= new MBeanAttributeInfoComparator();
		
		private	MBeanAttributeInfoComparator()	{}
		
			public int
		compare( Object o1, Object o2 )
		{
			final String	s1	= ATTRIBUTE_INFO_STRINGIFIER.stringify( (MBeanAttributeInfo)o1 );
			final String	s2	= ATTRIBUTE_INFO_STRINGIFIER.stringify( (MBeanAttributeInfo)o2 );
			
			return( s1.compareTo( s2 ) );
		}
		
			public boolean
		equals( Object other )
		{
			return( other instanceof MBeanAttributeInfoComparator );
		}
	}
	
	
	
	/**
		Return true if the two MBeanAttributeInfo[] contain the same attributes
		WARNING: arrays will be sorted to perform the comparison if they are the same length.
	 */
		boolean
	sameAttributes( MBeanAttributeInfo[] infos1, MBeanAttributeInfo[] infos2 )
	{
		boolean	equal	= false;
		
		if( infos1.length == infos2.length )
		{
			equal	= ArrayUtil.arraysEqual( infos1, infos2 );
			if ( ! equal )
			{
				// could still be equal, just in different order
				Arrays.sort( infos1, MBeanAttributeInfoComparator.INSTANCE );
				Arrays.sort( infos2, MBeanAttributeInfoComparator.INSTANCE );
				
				equal	= true;	// reset to false upon failure
				for( int i = 0; i < infos1.length; ++i )
				{
					if ( ! infos1[ i ].equals( infos2[ i ] ) )
					{
						equal	= false;
						break;
					}
				}
			}
			else
			{
				equal	= true;
			}
		}
		return( equal );
	}
	
	
	/**
		Return true if the two MBeanAttributeInfo[] contain the same operations
		WARNING: arrays will be sorted to perform the comparison if they are the same length.
	 */
		boolean
	sameOperations( MBeanOperationInfo[] infos1, MBeanOperationInfo[] infos2 )
	{
		boolean	equal	= false;
		
		if ( infos1.length == infos2.length )
		{
			// if they're in identical order, this is the quickest test if they ultimately succeed
			equal	= ArrayUtil.arraysEqual( infos1, infos2 );
			if ( ! equal )
			{
				// could still be equal, just in different order
				Arrays.sort( infos1, MBeanOperationInfoComparator.INSTANCE );
				Arrays.sort( infos2, MBeanOperationInfoComparator.INSTANCE );
				
				equal	= true;	// reset to false upon failure
				for( int i = 0; i < infos1.length; ++i )
				{
					if ( ! infos1[ i ].equals( infos2[ i ] ) )
					{
						equal	= false;
						break;
					}
				}
			}
		}
		return( equal );
	}
	
	/**
		Return true if the MBeanInfos have the same interface (for Attributes and
		operations).  MBeanInfo.equals() is not sufficient as it will fail if the
		infos are in different order, but are actually the same.
	 */
		boolean
	sameInterface( MBeanInfo info1, MBeanInfo info2 )
	{
		return( sameAttributes( info1.getAttributes(), info2.getAttributes() ) &&
			sameOperations( info1.getOperations(), info2.getOperations() ) );
	}
	
		public static boolean
	isIs( final Method method )
	{
		return( method.getName().startsWith( IS ) && method.getParameterTypes().length == 0 );
	}
	
	/**
		Return true if the method is of the form isXyz() or getXyz()
		(no parameters)
	 */
		public static boolean
	isGetter( Method method )
	{
		return( method.getName().startsWith( GET ) && method.getParameterTypes().length == 0 );
	}
	
	
		public static boolean
	isGetter( final MBeanOperationInfo info )
	{
		return ( info.getName().startsWith( GET ) &&
				info.getSignature().length == 0 &&
				! info.getReturnType().equals( "void" ) );
	}


		public static MBeanOperationInfo[]
	findOperations(
		final MBeanOperationInfo[]	operations,
		final String				operationName)
	{
		final Set<MBeanOperationInfo>	items	= new HashSet<MBeanOperationInfo>();
		for( int i = 0; i < operations.length; ++i )
		{
			if ( operations[ i ].getName().equals( operationName ) )
			{
				items.add( operations[ i ] );
			}
		}
		
		final MBeanOperationInfo[]	itemsArray	= new MBeanOperationInfo[ items.size() ];
		items.toArray( itemsArray );
		return itemsArray;
	}
	
		public static MBeanOperationInfo
	findOperation(
		final MBeanOperationInfo[]	operations,
		final String				operationName,
		final String[]				types )
	{
		MBeanOperationInfo	result	= null;
		
		for( int i = 0; i < operations.length; ++i )
		{
			final MBeanOperationInfo	info	= operations[ i ];
			
			if ( info.getName().equals( operationName ) )
			{
				final MBeanParameterInfo[]	sig	= info.getSignature();
				
				if ( sig.length == types.length )
				{
					result	= info;	// assume match...
					for( int j = 0; j < sig.length; ++j )
					{
						if ( ! types[ j ].equals( sig[ j ].getType() ) )
						{	
							result	= null;	// no match
							break;
						}
					}
				}
			}
		}
		
		return( result );
	}

	
	/**
		Return true if the method is of the form isXyz() or getXyz()
		(no parameters)
	 */
		public static boolean
	isIsOrGetter( Method method )
	{
		return( isGetter( method ) || isIs( method ) );
	}
	
		public static String
	getAttributeName( final Method method )
	{
		final String	methodName	= method.getName();
		String			attrName	= null;
		
		int prefixLength	= 0;
		
		if ( methodName.startsWith( GET ) || methodName.startsWith( SET ) )
		{
			prefixLength	= 3;
		}
		else
		{
			prefixLength	= 2;
		}
		
		return( methodName.substring( prefixLength, methodName.length() ) );
	}
	
	
		public static boolean
	isSetter( Method method )
	{
		return( method.getName().startsWith( SET ) &&
			method.getParameterTypes().length == 1 &&
			method.getParameterTypes()[ 0 ] != Attribute.class &&
			method.getReturnType().getName().equals( "void" ) );
	}
	
		public static boolean
	isGetAttribute( Method m )
	{
		return( m.getName().equals( "getAttribute" ) &&
			m.getParameterTypes().length == 1 && m.getParameterTypes()[ 0 ] == String.class );
			
	}
	
		public static boolean
	isGetAttributes( Method m )
	{
		return( m.getName().equals( "getAttributes" ) &&
			m.getParameterTypes().length == 1 && m.getParameterTypes()[ 0 ] == String[].class );
			
	}
	
		public static boolean
	isSetAttribute( Method m )
	{
		return( m.getName().equals( "setAttribute" ) &&
			m.getParameterTypes().length == 1 && m.getParameterTypes()[ 0 ] == Attribute.class );
			
	}
	
		public static boolean
	isSetAttributes( Method m )
	{
		return( m.getName().equals( "setAttributes" ) &&
			m.getParameterTypes().length == 1 && m.getParameterTypes()[ 0 ] == AttributeList.class );
			
	}
	
	
		public static ArrayList
	generateAttributeInfos(
		final Collection<Method> methodSet,
		final boolean	read,
		final boolean	write)
	{
		final ArrayList	infos	= new ArrayList();
		
		assert( methodSet != null );
		final Iterator	iter	= methodSet.iterator();
	
		while ( iter.hasNext() )
		{
			final Method	m	= (Method)iter.next();
			final String	methodName	= m.getName();
			
			assert( read || ( write && methodName.startsWith( SET )) );
			final MBeanAttributeInfo	info = new MBeanAttributeInfo(
				getAttributeName( m ),
				m.getReturnType().getName(),
				methodName,
				read,
				write,
				methodName.startsWith( "is" )
			);
			
			infos.add( info );
			
		}
		
		return( infos );
	}
	
		public static MBeanAttributeInfo[]
	generateMBeanAttributeInfos(
		final Collection<Method> getterSetters,
		final Collection<Method> getters,
		final Collection<Method> setters  )
	{
		final ArrayList	attrsList	= new ArrayList();
		
		attrsList.addAll( generateAttributeInfos( getterSetters, true, true ) );
		attrsList.addAll( generateAttributeInfos( getters, true, false ) );
		attrsList.addAll( generateAttributeInfos( setters, false, true ) );
		
		final MBeanAttributeInfo[]	attrs	= new MBeanAttributeInfo[ attrsList.size() ];
		attrsList.toArray( attrs );
		
		return( attrs );
	}
	
	
	    public static String[]
    getSignature( final MBeanParameterInfo[]infos )
    {
        final String[]  sig = new String[ infos.length ];
        
        int i = 0;
        for( final MBeanParameterInfo info : infos )
        {
            sig[ i ]    = info.getType();
            ++i;
        }
        return sig;
    }
    
		public static MBeanParameterInfo[]
	generateSignature( final Class[] sig )
	{
		final MBeanParameterInfo[]	infos	= new MBeanParameterInfo[ sig.length ];
		
		for( int i = 0; i < sig.length; ++i )
		{
			final Class	paramClass	= sig[ i ];
			
			final String	name		= "p" + i;
			final String	type		= paramClass.getName();
			final String	description	= paramClass.getName();
			
			final MBeanParameterInfo	info	=
				new MBeanParameterInfo( name, type, description );
			infos[ i ]	= info;
		}
		
		return( infos );
	}

		public static MBeanOperationInfo[]
	generateMBeanOperationInfos(
		final Collection<Method> 		methodSet )
	{
		final MBeanOperationInfo[]	infos	= new MBeanOperationInfo[ methodSet.size() ];
		
		final Iterator	iter	= methodSet.iterator();
	
		int	i = 0;
		while ( iter.hasNext() )
		{
			final Method	m	= (Method)iter.next();
			final String	methodName	= m.getName();
			
			final MBeanOperationInfo	info = new MBeanOperationInfo(
				methodName,
				methodName,
				generateSignature( m.getParameterTypes() ),
				m.getReturnType().getName(),
				MBeanOperationInfo.UNKNOWN
			);
			
			infos[ i ]	= info;
			++i;
			
		}
		
		return( infos );
	}
	

		public static MBeanInfo
	interfaceToMBeanInfo( final Class theInterface )
	{
		final Method[]	methods	= theInterface.getMethods();
		
		final Map<String,Method>	getters			= new HashMap<String,Method>();
		final Map<String,Method>	setters			= new HashMap<String,Method>();
		final Map<String,Method>	getterSetters	= new HashMap<String,Method>();
		final Set<Method>	operations		= new HashSet<Method>();
		
		for( int i = 0; i < methods.length; ++i )
		{
			final Method	method	= methods[ i ];
			
			final String	methodName	= method.getName();
			
			String	attrName	= null;
			if ( isIsOrGetter( method ) )
			{
				attrName	= getAttributeName( method );
				getters.put( attrName, method );
			}
			else if ( isSetter( method ) )
			{
				attrName	= getAttributeName( method );
				setters.put( attrName, method );
			}
			else
			{
				operations.add( method );
			}
			
			if ( (attrName != null) &&
					getters.containsKey( attrName ) &&
					setters.containsKey( attrName ) )
			{
				final Method	getter	= (Method)getters.get( attrName );
					
				final Class	getterType	= getter.getReturnType();
				final Class	setterType	= ((Method)setters.get( attrName )).getParameterTypes()[ 0 ];
				
				if ( getterType == setterType )
				{
					getters.remove( attrName );
					setters.remove( attrName );
					getterSetters.put( attrName, getter );
				}
				else
				{
					throw new IllegalArgumentException( "Attribute " + attrName + 
						"has type " + getterType.getName() + " as getter but type " +
						setterType.getName() + " as setter" );
				}
			}
		}
		
		/*
		java.util.Iterator	iter	= null;
		trace( "-------------------- getterSetters -------------------" );
		iter	= getterSetters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getName() + ", " );
		}
		trace( "-------------------- getters -------------------" );
		iter	= getters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getName() + ", " );
		}
		trace( "-------------------- setters -------------------" );
		iter	= setters.values().iterator();
		while ( iter.hasNext() )
		{
			trace( ((Method)iter.next()).getName() + ", " );
		}
		*/
		
		final MBeanAttributeInfo[]	attrInfos	=
			generateMBeanAttributeInfos( getterSetters.values(),
				getters.values(), setters.values() );
			
		final MBeanOperationInfo[]	operationInfos	=
			generateMBeanOperationInfos( operations );
		
		final MBeanConstructorInfo[]	constructorInfos	= null;
		final MBeanNotificationInfo[]	notificationInfos	= null;
		
		final MBeanInfo	mbeanInfo	= new MBeanInfo(
				theInterface.getName(),
				theInterface.getName(),
				attrInfos,
				constructorInfos,
				operationInfos,
				notificationInfos );
		
		return( mbeanInfo );
	}
	
	/**
		Merge two MBeanAttributeInfo[].  info1 overrides any duplication in info2.
		
		@param infos1
		@param infos2
	 */
		public static MBeanAttributeInfo[]
	mergeMBeanAttributeInfos(
		final MBeanAttributeInfo[]	infos1,
		final MBeanAttributeInfo[]	infos2 )
	{
	    // first make a Set of all names in infos1
	    final Set<String>   names   = new HashSet<String>();
	    for( final MBeanAttributeInfo info : infos1 )
	    {
	        names.add( info.getName() );
	    }

		final Set<MBeanAttributeInfo>	merged	= SetUtil.newSet( infos1 );
		
		for( final MBeanAttributeInfo info2 : infos2 )
		{
			final String	info2Name	= info2.getName();
			
			if ( ! names.contains( info2Name ) )
			{
				merged.add( info2 );
			}
		}

		final MBeanAttributeInfo[]	infosArray	=
			new MBeanAttributeInfo[ merged.size() ];
		merged.toArray( infosArray );

		return( infosArray );
	}
	
	/**
		Merge two MBeanNotificationInfo[].
		
		@param infos1
		@param infos2
	 */
		public static MBeanNotificationInfo[]
	mergeMBeanNotificationInfos(
		final MBeanNotificationInfo[] infos1,
		final MBeanNotificationInfo[] infos2 )
	{
	    if ( infos1 == null )
	    {
	        return infos2;
	    }
	    else if ( infos2 == null )
	    {
	        return( infos1 );
	    }

	    final Set<MBeanNotificationInfo>    all = GSetUtil.newSet( infos1 );
	    all.addAll( GSetUtil.newSet( infos2 ) );
		
		final MBeanNotificationInfo[]   merged  = new MBeanNotificationInfo[ all.size() ];
		return all.toArray( merged );
	}
	
	/**
		Add MBeanNotificationInfo into the MBeanInfo.
		
		@param origInfo
		@param notifs
	 */
	    public static MBeanInfo
	addNotificationInfos(
	    final MBeanInfo origInfo,
	    final MBeanNotificationInfo[]    notifs )
	{
	    MBeanInfo   result  = origInfo;
	    
	    if ( notifs != null && notifs.length != 0 )
	    {
    	    result  = new MBeanInfo(
    	        origInfo.getClassName(),
    	        origInfo.getDescription(),
    	        origInfo.getAttributes(),
    	        origInfo.getConstructors(),
    	        origInfo.getOperations(),
    	        mergeMBeanNotificationInfos( origInfo.getNotifications(), notifs )
    	        );
	    }
	    return result;
	}

	
	/**
		Merge two MBeanOperationInfo[].
		
		@param infos1
		@param infos2
	 */
		public static MBeanOperationInfo[]
	mergeMBeanOperationInfos(
		final MBeanOperationInfo[] infos1,
		final MBeanOperationInfo[] infos2 )
	{
	    if ( infos1 == null )
	    {
	        return infos2;
	    }
	    else if ( infos2 == null )
	    {
	        return( infos1 );
	    }
	    
	    final Set<MBeanOperationInfo>    all = GSetUtil.newSet( infos1 );
	    all.addAll( GSetUtil.newSet( infos2 ) );
		
		final MBeanOperationInfo[]   merged  = new MBeanOperationInfo[ all.size() ];
		return all.toArray( merged );
	}
	
	/**
		Merge two MBeanOperationInfo[].
		
		@param infos1
		@param infos2
	 */
		public static MBeanConstructorInfo[]
	mergeMBeanConstructorInfos(
		final MBeanConstructorInfo[] infos1,
		final MBeanConstructorInfo[] infos2 )
	{
	    if ( infos1 == null )
	    {
	        return infos2;
	    }
	    else if ( infos2 == null )
	    {
	        return( infos1 );
	    }
	    
	    final Set<MBeanConstructorInfo>    all = GSetUtil.newSet( infos1 );
	    all.addAll( GSetUtil.newSet( infos2 ) );
		
		final MBeanConstructorInfo[]   merged  = new MBeanConstructorInfo[ all.size() ];
		return all.toArray( merged );
	}
	
	
	/**
		Merge two MBeanInfo.  'info1' takes priority in conflicts, name, etc.
		
		@param info1
		@param info2
	 */
	    public static MBeanInfo
	mergeMBeanInfos(
	    final MBeanInfo info1,
	    final MBeanInfo info2 )
	{
	    if ( info1 == null )
	    {
	        return info2;
	    }
	    else if ( info2 == null )
	    {
	        return( info1 );
	    }
	    
	    return( new MBeanInfo(
	        info1.getClassName(),
	        info1.getDescription(),
	        mergeMBeanAttributeInfos( info1.getAttributes(), info2.getAttributes() ),
	        mergeMBeanConstructorInfos( info1.getConstructors(), info2.getConstructors() ),
	        mergeMBeanOperationInfos( info1.getOperations(), info2.getOperations() ),
	        mergeMBeanNotificationInfos( info1.getNotifications(), info2.getNotifications() )
	        ) );
	        
	}
	
	
	/**
		Make a new MBeanInfo from an existing one, substituting MBeanAttributeInfo
		
		@param origMBeanInfo
		@param newAttrInfos
	 */
		public static MBeanInfo
	newMBeanInfo(
		final MBeanInfo	origMBeanInfo,
		final MBeanAttributeInfo[]	newAttrInfos )
	{
		final MBeanInfo	info	= new MBeanInfo( origMBeanInfo.getClassName(),
									origMBeanInfo.getDescription(),
									newAttrInfos,
									origMBeanInfo.getConstructors(),
									origMBeanInfo.getOperations(),
									origMBeanInfo.getNotifications() );
		return( info );
	}
	
	
		public static boolean
	domainMatches(
		final String		defaultDomain,
		final ObjectName	pattern,
		final ObjectName	candidate )
	{
		boolean	matches	= false;
		
		final String	candidateDomain	= candidate.getDomain();
		if ( pattern.isDomainPattern() )
		{
			final String	regex	=
				RegexUtil.wildcardToJavaRegex( pattern.getDomain() );
			
			matches	= Pattern.matches( regex, candidateDomain);
		}
		else
		{	
			// domain is not a pattern
			
			String	patternDomain	= pattern.getDomain();
			if ( patternDomain.length() == 0 )
			{
				patternDomain	= defaultDomain;
			}
			
			matches	= patternDomain.equals( candidateDomain );
		}
		
		//dm( "MBeanProxyMgrImpl.domainMatches: " + matches + " " + pattern + " vs " + candidate );
		
		return( matches );
	}
	
		public static boolean
	matchesPattern(
		final String		defaultDomain,
		final ObjectName	pattern,
		final ObjectName	candidate )
	{
		boolean	matches	= false;
		
		if ( domainMatches( defaultDomain, pattern, candidate ) )
		{
			final String	patternProps	= pattern.getCanonicalKeyPropertyListString();
			final String	candidateProps	= candidate.getCanonicalKeyPropertyListString();
			assert(  patternProps.indexOf( "*" ) < 0 );
			assert(  candidateProps.indexOf( "*" ) < 0 );
			
			// Since we used canonical form any match means the pattern props String
			// must be a substring of candidateProps
			if ( candidateProps.indexOf( patternProps ) >= 0 )
			{
				matches	= true;
			}
		}
		
		return( matches );
	}
	
	    public static String
	toString( final ObjectName objectName )
	{
	    return ObjectNameStringifier.DEFAULT.stringify( objectName );
	}
	
	
	    public static Notification
	cloneNotification(
	    final Notification  in,
	    final Object        source )
	{
	    Notification out = null;
	    
	    if ( in.getClass() == AttributeChangeNotification.class )
	    {
	        final AttributeChangeNotification a = (AttributeChangeNotification)in;
	        
	        out = new AttributeChangeNotification(
	            source,
	            a.getSequenceNumber(),
	            a.getTimeStamp(),
	            a.getMessage(),
	            a.getAttributeName(),
	            a.getAttributeType(),
	            a.getOldValue(),
	            a.getNewValue() );
	    }
	    else if ( in.getClass() == Notification.class )
	    {
	        out = new Notification(
	            in.getType(),
	            source,
	            in.getSequenceNumber(),
	            in.getTimeStamp(),
	            in.getMessage() );
	    }
	    else
	    {
	        throw new IllegalArgumentException( "Not supporting cloning of: " + in.getClass() );
	    }
	    
	    return out;
	}
}






