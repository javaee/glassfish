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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/MappedDelegate.java,v 1.8 2007/05/05 05:23:42 tcfujii Exp $
 * $Revision: 1.8 $
 * $Date: 2007/05/05 05:23:42 $
 */

package com.sun.enterprise.management.support;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import com.sun.enterprise.management.support.AMXAttributeNameMapper;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ArrayConversion;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.StringUtil;


/**
	Delegate class which wraps another, and maps its Attributes
	to different names.
 */
public class MappedDelegate extends DelegateBase
{
	/**
		MBeanInfo in which names have not been mapped (original).
	 */
	private final Delegate		mDelegate;
	
	/**
		MBeanInfo in which names have not been mapped (original).
	 */
	private final MBeanInfo		mUnmappedMBeanInfo;
	
	/**
		MBeanInfo in which names have been mapped.
	 */
	private final MBeanInfo		mMappedMBeanInfo;
	
	/**
		the Attribute names, before mapping
	 */
	private final Set<String>			mUnmappedAttributeNames;
	
	/**
		the mapper
	 */
	private final AMXAttributeNameMapper	mAttributeNameMapper;
	
	
	/**
		New instance which wraps the supplied delegate using the specified mapper.
	 */
		public
	MappedDelegate(
		final Delegate 				    delegate,
		final AMXAttributeNameMapper	mapper )
	{
		super( "MappedDelegate." + (delegate == null ? "null" : delegate.getID()), null);
        
        if ( delegate == null )
        {
            throw new IllegalArgumentException();
        }
		
		mDelegate	= delegate;
		
		mUnmappedMBeanInfo	= getUnmappedMBeanInfoFresh();
		
		final String[]	unmappedAttributeNames	=
			JMXUtil.getAttributeNames( mUnmappedMBeanInfo.getAttributes() );
		mUnmappedAttributeNames	= ArrayConversion.arrayToSet( unmappedAttributeNames );
		
		mAttributeNameMapper	= mapper;
		//mAttributeNameMapper.deriveAll( unmappedAttributeNames );
		
		mMappedMBeanInfo	= mapMBeanInfo( mDelegate.getMBeanInfo() );
	}
    
	/**
		Get the unmapped name for a mapped Attribute name.  If the name has
		not been mapped, it is returned unchanged.
	 */
		protected String
	derivedToOriginal( final String mappedName )
	{
		return( mAttributeNameMapper.derivedToOriginal( mappedName ) );
	}
	
	/**
		Return the unmapped version of an mapped Attribute.
	 */
		private Attribute
	derivedToOriginal( final Attribute mappedAttr )
	{
		final String	unmappedName	= derivedToOriginal( mappedAttr.getName() );
		
		return( new Attribute( unmappedName, mappedAttr.getValue() ) );
	}
	
	/**
		Call derivedToOriginal( name ) for each mapped name.
	 */
		protected String[]
	derivedToOriginal( final String[] mappedNames )
	{
		final String[]	unmappedNames	= new String[ mappedNames.length ];
		
		for( int i = 0; i < unmappedNames.length; ++i )
		{
			unmappedNames[ i ]	= derivedToOriginal( mappedNames[ i ] );
		}
		
		return( unmappedNames );
	}
	
	
	/**
		Get the mapped name for an unmapped name.  If the name has
		not been mapped, it is returned unchanged.
	 */
		private String
	originalToDerived( final String unmappedName )
	{
		return( mAttributeNameMapper.originalToDerived( unmappedName ) );
	}
	
	
	/**
	 */
		private Attribute
	originalToDerived( final Attribute unmappedAttr )
	{
		final String	mappedName	= originalToDerived( unmappedAttr.getName() );
		return( new Attribute( mappedName, unmappedAttr.getValue() ) );
	}
	
	
		protected AttributeList
	derivedToOriginal( final AttributeList mappedAttrs )
	{
		// first, create a new list which uses the unmapped names
		final int			numAttrs		= mappedAttrs.size();
		final AttributeList	unmappedAttrs	= new AttributeList();
		for( int i = 0; i < numAttrs; ++i )
		{
			unmappedAttrs.add( derivedToOriginal( (Attribute)mappedAttrs.get( i ) ) );
		}
		
		return( unmappedAttrs );
	}
	
		protected AttributeList
	originalToDerived( final AttributeList unmappedAttrs )
	{
		// first, create a new list which uses the unmapped names
		final int			numAttrs	= unmappedAttrs.size();
		final AttributeList	mappedAttrs	= new AttributeList();
		for( int i = 0; i < numAttrs; ++i )
		{
			mappedAttrs.add( originalToDerived( (Attribute)unmappedAttrs.get( i ) ) );
		}
		
		
		return( unmappedAttrs );
	}
	
		public boolean
	supportsAttribute( final String mappedName )
	{
		return( mAttributeNameMapper.getDerivedNames().contains( mappedName ) );
	}
	
		private void
	checkSupported( final String mappedName )
		throws AttributeNotFoundException
	{
		if ( ! supportsAttribute( mappedName ) )
		{
		    debug( "Attribute " + mappedName + " is not supported, have: " +
		        CollectionUtil.toString( mAttributeNameMapper.getDerivedNames(), ", ") );
			throw new AttributeNotFoundException( mappedName );
		}
	}
	
		public Object
	getAttribute( final String mappedName )
		throws AttributeNotFoundException
	{
		checkSupported( mappedName );
		
		final String unmappedName	= mAttributeNameMapper.derivedToOriginal( mappedName );
		
		return( mDelegate.getAttribute( unmappedName ) );
	}

		public AttributeList
	getAttributes( final String[] mappedNames )
	{
		final AttributeList	unmappedResults	=
			mDelegate.getAttributes( derivedToOriginal( mappedNames ) );
		
		return( originalToDerived( unmappedResults ) );
	}
	
		public void
	setAttribute( final Attribute mappedAttr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
		checkSupported( mappedAttr.getName() );
		
		final Attribute unmappedAttr	= derivedToOriginal( mappedAttr );
		
		mDelegate.setAttribute( unmappedAttr );
	}
	
		public AttributeList
	setAttributes( final AttributeList mappedAttrs )
	{
		// first, create a new list which uses the unmapped names
		final AttributeList	unmappedAttrs	= derivedToOriginal( mappedAttrs );
		
		// get the Attributes using the unmapped names
		final AttributeList	unmappedResults	=
						mDelegate.setAttributes( unmappedAttrs );
		
		return( originalToDerived( unmappedResults ) );
	}
	
		protected MBeanInfo
	getUnmappedMBeanInfoFresh()
	{
		return( mDelegate.getMBeanInfo( ) );
	}
	
		protected MBeanInfo
	getUnmappedMBeanInfo()
	{
		return( mUnmappedMBeanInfo );
	}
	
		private MBeanAttributeInfo
	mapAttributeInfo( final MBeanAttributeInfo	unmappedInfo )
	{
		String	mappedName	= originalToDerived( unmappedInfo.getName() );
		
		if ( mappedName == null )
		{
            mappedName  = unmappedInfo.getName();
		}
		
		final MBeanAttributeInfo	mappedInfo	=
							new MBeanAttributeInfo(
								mappedName, 
								unmappedInfo.getType(), 
								unmappedInfo.getDescription(), 
								unmappedInfo.isReadable(), 
								unmappedInfo.isWritable(), 
								unmappedInfo.isIs()
							);
		
		return( mappedInfo );
	}
	
		private MBeanAttributeInfo[]
	mapAttributeInfos( final MBeanAttributeInfo[]	unmappedInfos )
	{
		final MBeanAttributeInfo[]	mappedInfos	=
				new MBeanAttributeInfo[ unmappedInfos.length ];
		
		for ( int i = 0; i < unmappedInfos.length; ++i )
		{
		    final String    attrName    = unmappedInfos[ i ].getName();
			assert( attrName != null );
		    
			mappedInfos[ i ]	= mapAttributeInfo( unmappedInfos[ i ] );
			assert( mappedInfos[ i ].getName() != null );
			debug( "Mapped " + unmappedInfos[ i ].getName() + " to " + mappedInfos[i].getName() );
		}
		return( mappedInfos );
	}

	/**
		Create an MBeanInfo which contains the mapped names.
	 */
		private MBeanInfo
	mapMBeanInfo( final MBeanInfo	unmappedMBeanInfo )
	{
		final MBeanAttributeInfo[]	mappedAttributeInfos	=
				mapAttributeInfos( unmappedMBeanInfo.getAttributes() );
				
		final MBeanInfo	mappedInfo	= new MBeanInfo(
			unmappedMBeanInfo.getClassName(),
			unmappedMBeanInfo.getDescription(),
			mappedAttributeInfos,
			unmappedMBeanInfo.getConstructors(),
			unmappedMBeanInfo.getOperations(),
			unmappedMBeanInfo.getNotifications()
			);
		
		return( mappedInfo );
	}
	
	
		protected MBeanInfo
	getMappedMBeanInfo()
	{
		return( mMappedMBeanInfo );
	}
	
		public MBeanInfo
	getMBeanInfo()
	{
		return( getMappedMBeanInfo() );
	}
	
	/**
	 */
		public String[]
	getAttributeNames( )
	{
		final String[]	names	=
			JMXUtil.getAttributeNames( getMBeanInfo().getAttributes() );
			
		return( names );
	}
	

	
	/**
		Just pass it along to the Delegate
	 */
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
		final Object	result	= mDelegate.invoke( operationName, args, types );
		
		return( result );
	}
    
    	protected final String
	_getDefaultValue( final String mappedName )
		throws AttributeNotFoundException
	{
		checkSupported( mappedName );
		
		final String unmappedName	= mAttributeNameMapper.derivedToOriginal( mappedName );
		
		return( mDelegate.getDefaultValue( unmappedName ) );
	}
}








