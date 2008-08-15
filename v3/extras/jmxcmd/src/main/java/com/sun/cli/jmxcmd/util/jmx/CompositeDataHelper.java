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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/CompositeDataHelper.java,v 1.3 2005/11/15 20:21:47 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:47 $
 */
 

package com.sun.cli.jmxcmd.util.jmx;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;

import java.math.BigInteger;
import java.math.BigDecimal;

import java.lang.reflect.Array;

import javax.management.ObjectName;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.InvalidOpenTypeException;

import com.sun.cli.jcmd.util.misc.ArrayConversion;
import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;

public class CompositeDataHelper
{
		public
	CompositeDataHelper()
	{
	}

	
		protected CompositeType
	mapToCompositeType(
		final String	typeName,
		final String	description,
		final Map<String,Object>		map)
		throws OpenDataException
	{
	    return mapToCompositeType( typeName, description, map, null );
	}
	
	/**
		Create a CompositeType from a Map.  Each key in the map must be a String,
		and each value must be a type consistent with OpenTypes.
		
		@param typeName	the arbitrary name of the OpenType to be used
		@param description	the arbitrary description of the OpenType to be used
		@param map	a Map keyed by String, whose values may not be null
	 */
		protected CompositeType
	mapToCompositeType(
		final String	typeName,
		final String	description,
		final Map<String,Object>		map,
		CompositeTypeFromNameCallback   callback)
		throws OpenDataException
	{
	    return OpenMBeanUtil.mapToCompositeType( typeName, description, map, callback );
	}
	
	/**
		Create a CompositeData from a Map.  Each key in the map must be a String,
		and each value must be a type consistent with OpenTypes.
		
		@param typeName	the arbitrary name of the OpenType to be used
		@param description	the arbitrary description of the OpenType to be used
		@param map	a Map keyed by String, whose values may not be null
	 */
		protected CompositeData
	mapToCompositeData(
		final String	typeName,
		final String	description,
		final Map<String,Object>		map )
		throws OpenDataException
	{
		final CompositeType	type	= mapToCompositeType( typeName, description, map );
		
		return( new CompositeDataSupport( type, map ) );
	}
	
		public CompositeData
	listToCompositeData(
		final String	typeName,
		final String	description,
		final List<?>		list )
	{
		return( null );
	}
	
		public CompositeData
	setToCompositeData(
		final String	typeName,
		final String	description,
		final Set<?>		set )
	{
		return( null );
	}
	
		public Object
	asData( Object o )
		throws OpenDataException
	{
		Object	result	= null;
		
		if ( o instanceof StackTraceElement )
		{
			result	= stackTraceElementCompositeData( (StackTraceElement)o );
		}
		else if ( o instanceof Throwable )
		{
			result	= throwableToCompositeData( (Throwable)o );
		}
		else if ( o instanceof Map )
		{
            @SuppressWarnings("unchecked")
            final Map<String,Object> m = (Map)o;
			result	= mapToCompositeData( Map.class.getName(), "", m);
		}
		else if ( o instanceof List )
		{
			result	= listToCompositeData( List.class.getName(), "", (List)o );
		}
		else if ( o instanceof Set )
		{
			result	= setToCompositeData( Set.class.getName(), "", Set.class.cast(o) );
		}
		else
		{
			final OpenType	type	= OpenMBeanUtil.getOpenType( o );
			
			if ( type instanceof SimpleType )
			{
				result	= o;
			}
			else if ( type instanceof ArrayType )
			{
				result	= o;
			}
			else
			{
				throw new IllegalArgumentException( "" + o );
			}
		}
		
		return( result );
	}
	
		
	/**
		Get a CompositeType describing a CompositeData which has no elements.
	 */
		public CompositeType
	getStackTraceElementCompositeType()
		throws OpenDataException
	{
		final String[]	itemNames	= new String[]
		{
			"ClassName",
			"FileName",
			"LineNumber",
			"isNativeMethod",
		};
		
		final String[]	descriptions	= new String[  ]
		{
			"ClassName",
			"FileName",
			"LineNumber",
			"IsNativeMethod",
		};
		
		final OpenType[]	openTypes	= new OpenType[ itemNames.length ];
		openTypes[ 0 ]	= SimpleType.STRING;
		openTypes[ 1 ]	= SimpleType.STRING;
		openTypes[ 2 ]	= SimpleType.INTEGER;
		openTypes[ 3 ]	= SimpleType.BOOLEAN;
		
		final CompositeType	type = new CompositeType(
			StackTraceElement.class.getName(), 
			"StackTraceElement composite type",
			itemNames,
			descriptions,
			openTypes
			);
		return( type );
	}
	
	
	/**
		Get a CompositeType describing a CompositeData which has no elements.
	 */
		public CompositeData
	stackTraceElementCompositeData( StackTraceElement elem )
		throws OpenDataException
	{
		final Map<String,Object>	m	= new HashMap<String,Object>();
		m.put( "ClassName", elem.getClassName() );
		m.put( "FileName", elem.getFileName() );
		m.put( "LineNumber", new Integer( elem.getLineNumber() ) );
		m.put( "isNativeMethod", Boolean.valueOf( elem.isNativeMethod() ) );
		
		return( new CompositeDataSupport( getStackTraceElementCompositeType(), m ) );
	}
	
	

	/**
		Get a CompositeType describing a CompositeData which has no elements.
	 */
		public CompositeData
	throwableToCompositeData( final Throwable t)
		throws OpenDataException
	{
		final Throwable	cause	= t.getCause();
		
		final String[]	itemNames	= new String[]
		{
			"Message",
			"Cause",
			"StackTrace",
		};
		
		final String[]	descriptions	= new String[  ]
		{
			"The message from the Throwable",
			"The cause (if any) from the Throwable",
			"The stack trace from the Throwable",
		};
		
		final OpenType<?>[]	openTypes	= new OpenType[ itemNames.length ];
		
		openTypes[ 0 ]	= SimpleType.STRING;
		openTypes[ 1 ]	= cause == null ?
			getEmptyCompositeType() : throwableToCompositeData( cause ).getCompositeType();
        final CompositeType ct = getStackTraceElementCompositeType();
        
        @SuppressWarnings("unchecked")
        final ArrayType at = new ArrayType( t.getStackTrace().length, ct );
		openTypes[ 2 ]	= at;
		
		
		final CompositeType	type	= new CompositeType(
			t.getClass().getName(), 
			"Throwable composite type",
			itemNames,
			descriptions,
			openTypes
			);
		
		
		final Map<String,Object>	m	= new HashMap<String,Object>();
		m.put( "Message", t.getMessage() );
		m.put( "Cause", cause == null ? null : throwableToCompositeData( cause ) );
		m.put( "StackTrace", t.getStackTrace() );
		
		return( new CompositeDataSupport( type, m ) );
	}
	
	private final static String[]	EMPTY_STRING_ARRAY	= new String[0];
	private final static OpenType[]	EMPTY_OPENTYPES		= new OpenType[0];
	
	/**
		Get a CompositeType describing a CompositeData which has no elements.
	 */
		public static CompositeType
	getEmptyCompositeType()
		throws OpenDataException
	{
		return( new CompositeType(
			CompositeType.class.getName() + ".Empty", 
			"Empty composite type",
			EMPTY_STRING_ARRAY,
			EMPTY_STRING_ARRAY,
			EMPTY_OPENTYPES
			) );
	}
}






