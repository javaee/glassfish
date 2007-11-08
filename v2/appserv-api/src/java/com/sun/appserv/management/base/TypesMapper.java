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

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;
import java.lang.reflect.Field;


/**
	Map all types from XTypes to their respective MBean interfaces.
	
	@see AllTypesMapper
	@see com.sun.appserv.management.j2ee.J2EETypesMapper
 */
public class TypesMapper
{
	private final Map<String,Class>	mMap;
	
		public
	TypesMapper( final Class[] interfaces )
	{
		mMap	= init( interfaces );
	}
	
	/**
		Map all types to interfaces.
	 */
		private Map<String,Class>
	init( final Class[] interfaces )
	{
		final Map<String,Class>	m	= new HashMap<String,Class>();

		for( int i = 0; i < interfaces.length; ++i )
		{
			final Class	theInterface	= interfaces[ i ];
			
			try
			{
				final Field	field	= theInterface.getField( "J2EE_TYPE" );
				final String value	= (String)field.get( theInterface );
				if ( m.containsKey( value ) )
				{
					final String	msg	=
						"TypesMapper: key already present: " +
						value + " for " + theInterface.getName();
					
					assert(  false ): msg;
					throw new RuntimeException( msg );
				}
				m.put( value, theInterface );
			}
			catch( Exception e )
			{
				e.printStackTrace();
				assert( false );
				throw new IllegalArgumentException( theInterface.getName() );
			}
		}
		
		return( m );
	}
	
	/**
		Return the Class associated with a given type.
	 */
		public Class
	getInterfaceForType( final String type )
	{
		final Class theClass	= mMap.get( type );
		
		return( theClass );
	}
	
	    public Set<String>
	getJ2EETypes()
	{
	    return Collections.unmodifiableSet( mMap.keySet() );
	}
	
	    public Set<Class>
	getClasses()
	{
	    final Set<Class>    classes = new HashSet<Class>();
	    classes.addAll( mMap.values() );
	    return Collections.unmodifiableSet( classes );
	}
}











