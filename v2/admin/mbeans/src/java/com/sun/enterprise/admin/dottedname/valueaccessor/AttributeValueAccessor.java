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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/valueaccessor/AttributeValueAccessor.java,v 1.3 2005/12/25 03:42:06 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:42:06 $
 */
 

package com.sun.enterprise.admin.dottedname.valueaccessor;

import java.util.Set;
import java.util.HashSet;

import javax.management.MBeanServerConnection;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.Attribute;


import com.sun.enterprise.admin.dottedname.DottedNameStrings;

import com.sun.enterprise.admin.util.ClassUtil;

public class AttributeValueAccessor extends ValueAccessorBase
{
		public
	AttributeValueAccessor( final MBeanServerConnection conn )
	{
		super( conn );
	}
		
	/*
		Return a Set of String of the names of all attributes within the MBean
	 */
		public static Set
	getAllAttributeNames( final MBeanServerConnection conn, final ObjectName objectName )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException, IntrospectionException
	{
		final Set		allNames	= new HashSet();
	
		// add the Attribute names
		final MBeanInfo					info		= conn.getMBeanInfo( objectName );
		final MBeanAttributeInfo []		attrsInfo	= info.getAttributes();
		if ( attrsInfo != null )
		{
			for( int i = 0; i < attrsInfo.length; ++i )
			{
				allNames.add( attrsInfo[ i ].getName() );
			}
		}
		
		return( allNames );
	}
	
	
		public Attribute
	getValue( final ObjectName objectName, final String valueName )
		throws MBeanException, AttributeNotFoundException, InstanceNotFoundException,
		ReflectionException, java.io.IOException
	{
		final Object value	= getMBS().getAttribute( objectName, valueName);
		
		return( new Attribute( valueName, value ) );
	}
	
		Class
	getAttributeClass( final ObjectName objectName, final String attributeName )
		throws IntrospectionException, java.io.IOException, ReflectionException,
				InstanceNotFoundException, ClassNotFoundException
	{
		final MBeanInfo				info	= getMBS().getMBeanInfo( objectName );
		final MBeanAttributeInfo []	attrsInfo	= info.getAttributes();
		Class	theClass	= null;
		
		for( int i = 0; i < attrsInfo.length; ++i )
		{
			final String	testName	= attrsInfo[ i ].getName();
			
			if ( testName.equals( attributeName ) )
			{
				theClass	= ClassUtil.getClassFromName( attrsInfo[ i ].getType() );
				break;
			}
		}
		return( theClass );
	}
	
		public Attribute
	setValue( final ObjectName objectName, final Attribute attr ) 
		throws Exception
	{
		if ( attr.getValue() == null )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.ILLEGAL_TO_SET_NULL_KEY,
					attr.getName( ) );
					
			throw new IllegalArgumentException( msg );
		}

		Attribute	actualAttr	= null;
		
		Object	value	= attr.getValue();
		if ( value instanceof String )
		{
			final Class	attrClass	= getAttributeClass( objectName, attr.getName() );
			if ( attrClass == null )
			{
				final String	msg	= DottedNameStrings.getString(
						DottedNameStrings.ATTRIBUTE_NOT_FOUND_KEY,
						attr.getName( ) );
					
				throw new AttributeNotFoundException( attr.getName() );
			}
			
			value	= coerceToClass( attrClass, (String)value );
			
			actualAttr	= new Attribute( attr.getName(), value );
		}
		else
		{
			// if not a String, then pass on through
			actualAttr	= attr;
		}
		
		getMBS().setAttribute( objectName, actualAttr);
		
		return( actualAttr );
	}
}

