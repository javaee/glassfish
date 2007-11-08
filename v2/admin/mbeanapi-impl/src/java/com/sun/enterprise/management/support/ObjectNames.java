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

/**
*/

package com.sun.enterprise.management.support;

import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;

import java.lang.reflect.Field;

import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.base.AMXDebug;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.*;
import com.sun.appserv.management.monitor.*;
import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.base.AllTypesMapper;

import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

/**
	Class used to build ObjectNames for AMX MBeans.  <b>This class does not assume
	that any MBeans are loaded</b>--and no code should be added which does; such code
	belongs in the {@link QueryMgr}.
	<p>
	Note that most of the time, using the
	{@link QueryMgr} is a better choice.
	In particular, the ObjectNames returned from here are the minimal ones required to
	uniquely identify the ; the actual registered MBean may well have
	additional properties.
*/
public final class  ObjectNames
{
	private final String	mJMXDomain;
	
		private
	ObjectNames( final String jmxDomain )
	{
		mJMXDomain	= jmxDomain;
	}
	
	    private void
	debug( final Object o )
	{
	    AMXDebug.getInstance().getOutput(
	        "com.sun.enterprise.management.support.ObjectNames" ).println( o );
	}
	
		public static ObjectNames
	getInstance( final String jmxDomain )
	{
		return( new ObjectNames( jmxDomain ) );
	}
	
	
		public String
	getJMXDomain()
	{
		return( mJMXDomain );
	}
	
	private static final String[]	EMPTY_STRING_ARRAY	= new String[ 0 ];
	
	
	/**
	 */
		public static String
	getJ2EEType( Class theInterface )
	{
		return( (String)ClassUtil.getFieldValue( theInterface, "J2EE_TYPE" ) );
	}
	

	/**
		Append the formatted props to the JMX domain and return the ObjectName
	 */
		private ObjectName
	newObjectName( String props )
	{
		return( Util.newObjectName( getJMXDomain(), props ) );
	}
	
		public static String
	makeWild( String props )
	{
		return( Util.concatenateProps( props, JMXUtil.WILD_PROP ) );
	}
	
	/**
		Build an ObjectName for an MBean logically contained within the parent MBean.
		The child may be a true child (a subtype), or simply logically contained
		within the parent.
		
		@param parentObjectName
		@param parentFullType
		@param childJ2EEType
		@param childName
		@return ObjectName
	 */
		public ObjectName
	buildContaineeObjectName(
		final ObjectName	parentObjectName,
		final String		parentFullType,
		final String		childJ2EEType,
		final String		childName )
	{
		final String	domain	= parentObjectName.getDomain();
		
		String	props	= "";
		
		final TypeInfo	info	= TypeInfos.getInstance().getInfo( childJ2EEType );
		if ( info.isSubType() )
		{
			// extract j2eeType and name
			final String 	parentProp		= Util.getSelfProp( parentObjectName );
			
			// extract the remaining ancestors
			final String[]	parentFullTypes	= Util.getTypeArray( parentFullType );
			final Set<String> ancestorKeys	= GSetUtil.newSet( parentFullTypes, 0, parentFullTypes.length - 1 );
			final String	ancestorProps	= JMXUtil.getProps( parentObjectName, ancestorKeys, true );
			props	= Util.concatenateProps( parentProp, ancestorProps );
		}
		else
		{
			// it's logically contained within the parent, nothing more to do
		}
		
		
		final String	requiredProps	= Util.makeRequiredProps( childJ2EEType, childName );
		final String	allProps		= Util.concatenateProps( requiredProps, props );
		
		return( Util.newObjectName( domain, allProps  ) );
	}
	
		public ObjectName
	buildContaineeObjectName(
		final ObjectName	parentObjectName,
		final String		parentFullType,
		final String		childJ2EEType )
	{
		final String	name	= getSingletonName( childJ2EEType );
		
		return( buildContaineeObjectName( parentObjectName, parentFullType, childJ2EEType, name ) );
	}
	
		public ObjectName
	getDomainRootObjectName()
	{
		return( newObjectName( Util.makeRequiredProps( XTypes.DOMAIN_ROOT, getJMXDomain() ) ) );
	}
	
	/**
		Get the ObjectName for a singleton object.
	 */
		public ObjectName
	getSingletonObjectName( final String j2eeType )
	{
		final TypeInfo info	= TypeInfos.getInstance().getInfo( j2eeType );
		if ( info.isSubType() )
		{
			throw new IllegalArgumentException( "singletons may not be sub-types: " + j2eeType );
		}
		
		final String	props	= Util.makeRequiredProps( j2eeType, getSingletonName( j2eeType ) );
		
		return( newObjectName( props ) );
	}
	

	
	/**
		In the ObjectName, what should the "name" property contain if the object
		is a singleton or otherwise not named? The name property is always required.
		Possible choices include:
		<ul>
		<li>"none" (or similar idea)</li>
		<li>the same as its j2eeType"</li>
		<li>empty</li>
		<li>random</li>
		</ul>
	 */
		public static String
	getSingletonName( String j2eeType )
	{
		if ( TypeInfos.getInstance().getInfo( j2eeType ) == null )
		{
			throw new IllegalArgumentException( j2eeType );
		}
		
		assert( ! AMX.NO_NAME.equals( MISSING_PARENT_NAME ) );
		return( AMX.NO_NAME );
	}
	
	/**
		The name that should be used for a missing parent such as J2EEApplication=null,
		as defined by JSR 77 specification.
		Must be distinct from the name returned by getSingletonName( j2eeType ).
	 */
	public static final String	MISSING_PARENT_NAME	= AMX.NULL_NAME;

	
	
	/**
		Get a parent ObjectName pattern using a child's ObjectName. The "parent" in 
		this context means the MBean that logically contains the child.  The caller
		must query for the actual ObjectName.
		
		@param childObjectName	the child ObjectName
		@return an ObjectName pattern for the parent.
	*/
		public ObjectName
	getContainerObjectNamePattern(
		final ObjectName childObjectName,
		final String	 childFullType )
	{
		ObjectName	parentPattern	= null;
		
		final String	domain	= childObjectName.getDomain();
		
		final String[]	fullType	= Util.getTypeArray( childFullType );
		assert( fullType.length >= 1 );
		
		final String	childType	= fullType[ fullType.length - 1 ];
		
		if ( fullType.length == 1 )
		{
			final TypeInfo	info		= TypeInfos.getInstance().getInfo( childType );
			
			final String	containedByJ2EEType	= info.getContainedByJ2EEType();
			if ( containedByJ2EEType != null )
			{
				String	parentProps	= "";
				
				// special case--DomainRoot and J2EEDomain must have a name
				// equal to the domain name
				if ( containedByJ2EEType.equals( XTypes.DOMAIN_ROOT ) ||
					containedByJ2EEType.equals( J2EETypes.J2EE_DOMAIN ) )
				{
					parentProps	= Util.makeRequiredProps( containedByJ2EEType, domain );
				}
				else
				{
					parentProps	= Util.makeRequiredProps( containedByJ2EEType,
						getSingletonName( containedByJ2EEType ) );
				}
				
				parentPattern	= Util.newObjectNamePattern( domain, parentProps );
			}
			else
			{
				parentPattern	= null;	// no parent
			}
		}
		else
		{
			/*
				 It's a subType.  A subtype may have one or more "missing" ancestors.  For example,
				 the FullType of EJBModule is J2EEServer.J2EEApplication.EJBModule.  Since an EJBModule
				 may be standalone, the J2EEApplication may not actually exist.  We need to account for
				 this, and we do so by using the convention that a name of "null" indicates such a missing
				 ancestor.
				 
				 The parent is handled differently than the other properties because we have to extract
				 j2eeType=<type>,name=<name> whereas the other properties are of the form
				 <type>=<name>.  So first, we must find the first real parent.
			 */
			String	parentJ2EEType	= null;
			String	parentName		= null;
			Set<String>		remainingKeys		= Collections.emptySet();
			for( int i = fullType.length - 2; i >= 0; --i )
			{
				final String	tempType	= fullType[ i ];
				final String	tempName	= childObjectName.getKeyProperty( tempType );
				assert( tempName != null ) : "missing ObjectName property: " + tempType;
				if ( ! MISSING_PARENT_NAME.equals( tempName ) )
				{
					parentJ2EEType	= tempType;
					parentName		= tempName;
					final int	numItems	= i;
					remainingKeys	= GSetUtil.newSet( fullType, 0, numItems );
					break;
				}
			}
			/*
			trace( "\n---------------------- getContainerObjectNamePattern ---------------------------" );
			trace( childFullType + " = " + childObjectName );
			trace( "parentJ2EEType = " + parentJ2EEType );
			trace( "parentName = " + parentName );
			trace( "remainingKeys = " + toString( remainingKeys ) );
			trace( "-------------------------------------------------" );
			*/
			final String parentProps = Util.makeRequiredProps( parentJ2EEType, parentName );
			final String ancestorProps	= JMXUtil.getProps( childObjectName, remainingKeys );
			
			final String props	= Util.concatenateProps( parentProps, ancestorProps );
			
			parentPattern	= Util.newObjectNamePattern( domain, props );
		}
		
		return( parentPattern );
	}
	

		private static String
	getFullType( final MBeanServer server, final ObjectName objectName )
	{
		try
		{
			final String fullType	= (String)
				server.getAttribute( objectName, AMXAttributes.ATTR_FULL_TYPE );
				
			return( fullType );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	/**
		Get the ObjectName of the MBean logically containing the specified MBean.
		
		@param server
		@param containedObjectName
		@return the ObjectName of the MBean logically containing the child
	 */
		public ObjectName
	getContainerObjectName(
		final MBeanServer	server,
		final ObjectName	containedObjectName )
		throws InstanceNotFoundException
	{
		debug( "getContainerObjectName: containedObjectName = " + containedObjectName );
		    
		final String containedFullType	= getFullType( server, containedObjectName );
		
		debug( "getContainerObjectName: containedFullType = " + containedFullType );
		
		ObjectName	parentPattern	= getContainerObjectNamePattern( containedObjectName, containedFullType );
		ObjectName	containingObjectName	= null;
		
		if ( parentPattern != null )
		{
		    debug( "getContainerObjectName: parentPattern = " + parentPattern );
			final Set<ObjectName>	names	= JMXUtil.queryNames( server, parentPattern, null );
			
			if ( names.size() == 0 )
			{
				throw new InstanceNotFoundException( parentPattern.toString() );
			}
			
			containingObjectName	= (ObjectName)GSetUtil.getSingleton( names );
		}
		else
		{
			final String	j2eeType	= Util.getJ2EEType( containedObjectName );
			
			if ( ! j2eeType.equals( XTypes.DOMAIN_ROOT ) )
			{
				throw new IllegalArgumentException( containedObjectName.toString() );
			}
		}
		
		return( containingObjectName );
	}
	
	
	
	
		private static String
	toString( final Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
}









