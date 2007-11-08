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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/valueaccessor/ValueAccessorBase.java,v 1.3 2005/12/25 03:42:08 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:42:08 $
 */
 

package com.sun.enterprise.admin.dottedname.valueaccessor;

import java.lang.reflect.Array;

import javax.management.MBeanServerConnection;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.Attribute;

import com.sun.enterprise.admin.util.ClassUtil;
import com.sun.enterprise.admin.util.Tokenizer;
import com.sun.enterprise.admin.util.TokenizerImpl;


abstract class ValueAccessorBase implements ValueAccessor
{
	final MBeanServerConnection		mConn;
	
	
		MBeanServerConnection
	getMBS()
	{
		return( mConn );
	}

		public
	ValueAccessorBase( MBeanServerConnection conn )
	{
		mConn	= conn;
	}
	
	final char	ARRAY_ELEMENT_SEPARATOR	= ',';
	final char	ESCAPE_CHAR				= '\\';
	
		String []
	stringToStringArray( final String s )
		throws com.sun.enterprise.admin.util.TokenizerException
	{
		final String	delimiters		= "" + ARRAY_ELEMENT_SEPARATOR;
		final String	escapableChars	= "" + ARRAY_ELEMENT_SEPARATOR + ESCAPE_CHAR;
		
		final Tokenizer	tok	= new TokenizerImpl( s, delimiters, false, ESCAPE_CHAR, escapableChars );
		
		final String []	values	= tok.getTokens();
		
		return( values );
	}
	
		Object []
	convert( final String [] stringValues, final Class elementClass )
		throws Exception
	{
		if ( elementClass == String.class )
		{
			return( stringValues );
		}
		
		if ( ClassUtil.classnameIsPrimitiveArray( elementClass.getName() ) )
		{
			throw new IllegalArgumentException(  );
		}
		
		final Object []	values	= (Object [])Array.newInstance( elementClass, stringValues.length );
		
		for( int i = 0; i < values.length; ++i )
		{
			values[ i ]	= ClassUtil.InstantiateFromString( elementClass, stringValues[ i ] );
		}
		
		return( values );
	}
	
		Object []
	stringToArray( final String s, final Class elementClass )
		throws Exception
	{
		final String []	stringValues	= stringToStringArray( s );
		
		final Object []	values	= convert( stringValues, elementClass );
		
		return( values );
	}
	
		
		Object
	coerceToClass( final Class theClass, String value )
		throws Exception
	{
		Object	result	= value;
		
		if ( theClass != String.class )
		{
			if ( ClassUtil.classIsArray( theClass ) )
			{
				final String	theClassName		= theClass.getName();
				final String	elementClassName	= ClassUtil.getArrayMemberClassName( theClassName );
				
				// convert the string to an array of strings
				result	= stringToArray( value, ClassUtil.getClassFromName( elementClassName ) );
			}
			else
			{
				boolean 	canCoerce	= true;
				
				final Class resultClass	= ClassUtil.PrimitiveClassToObjectClass( theClass );
				
				if ( resultClass == Boolean.class  )
				{
					// insist on "true" or "false" (case insensitive)
					canCoerce	= value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" );
				}
				
				if ( canCoerce )
				{
					result	= ClassUtil.InstantiateFromString( resultClass, value );
				}
			}
		}
		return( result );
	}

	public abstract Attribute	getValue( ObjectName objectName, String valueName ) throws Exception;
	public abstract Attribute	setValue( ObjectName objectName, Attribute attr ) throws Exception;
}






