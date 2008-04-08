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
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.io.File;
import java.io.Serializable;


import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.misc.ObjectUtil;
import com.sun.appserv.management.util.jmx.OpenMBeanUtil;


/**
	Base impl class.
 */

public abstract class MapCapableBase implements MapCapable
{
	private Map<String,Serializable>	mFields;
	private boolean		    mWasMadeImmutable;
	private String		    mClassName;
 	
 	    public int
 	hashCode()
 	{
 	    return ObjectUtil.hashCode( mFields, mClassName) ^
 	                (mWasMadeImmutable ? 1 : 0);
 	}
 	
		protected
	MapCapableBase( final String className )
	{
		mFields				= new HashMap<String,Serializable>();
		mWasMadeImmutable	= false;
		mClassName			= className;
	}
	
		protected <T extends Serializable>
	MapCapableBase(
		final Map<String,T>		m,
		final String	        className)
	{
		this( className );
		
		mClassName	= className;
		
		if ( m != null )
		{
			putAll( m );
			
			// if present, remove the type
			getFields().remove( MAP_CAPABLE_CLASS_NAME_KEY );
		}
	}
	
		protected <T extends Serializable> void 
	checkValidType( final Map<String,T> m, final String requiredType )
	{
		final String	type	= (String)m.get( MAP_CAPABLE_CLASS_NAME_KEY );
		
		if ( ! requiredType.equals( type ) )
		{
			throw new IllegalArgumentException( "Illegal MAP_CAPABLE_CLASS_NAME_KEY: " + type );
		}
	}
	
	/**
		Return true if internal state is valid, false otherwise.
	 */
	protected abstract	boolean	validate();
	
	/**
		Return the type name which will be paired with MAP_CAPABLE_CLASS_NAME_KEY.
	 */
		public String
	getMapClassName()
	{
		return( mClassName );
	}
	
	
		protected void
	illegalObject( final Object o )
	{
		throw new IllegalArgumentException(
					"Object is of illegal class and/or non-Serializable class " +
					o.getClass().getName() );
	}
	
		protected void
	checkInJavaUtil( final Object o)
	{
		if ( ! o.getClass().getName().startsWith( "java.util." ) )
		{
			illegalObject( o );
		}
	}
	
	/**
		We restrict the set of legal types to:
		<ul>
		<li>OpenTypes</li>
		<li>Collection whose values meet these restrictions</li>
		<li>arrays whose values meet these restrictions</li>
		<li>Map whose keys and values meet these restrictions</li>
		<li>Throwable which are part of java or javax</li>
		<li>MapCapable whose Map meets these restrictions (assuming allowMapCapable is true)</li>
		</ul>
	 */
		protected final void
	checkLegalObject( 
		final Object    o,
		final boolean	allowMapCapable )
	{
		if ( o != null )
		{
		    // we can't check for "instanceof Serializable" because items such as
		    // HashMap use a non-Serializable key set; it apparently serializes
		    // the key set when it itself is serialized.
			if ( o instanceof Collection )
			{
				checkInJavaUtil( o );

                final Collection<?> oc  = TypeCast.asCollection( o );
				for( final Object next : oc )
				{
					checkLegalObject( next );
				}
			}
			else if ( o.getClass().getComponentType() != null )
			{
				// it's an array
				final Object[]   ta  = (Object[])o;
				for( int i = 0; i < ta.length; ++i )
				{
					checkLegalObject( ta[i] );
				}
			}
			else if ( o instanceof MapCapable )
			{
				if ( allowMapCapable )
				{
					checkLegalObject( ((MapCapable)o).asMap() );
				}
				else
				{
					illegalObject( o );
				}
			}
			else if ( o instanceof Map )
			{
				checkInJavaUtil( o );
				
				checkLegalObject( ((Map)o).keySet() );
				checkLegalObject( ((Map)o).values() );
			}
			else if ( o instanceof Throwable )
			{
				final String	classname	= o.getClass().getName();
				
				if ( !	(classname.startsWith( "java." ) ||
						classname.startsWith( "javax." )) )
				{
					illegalObject( o );
				}
				checkLegalObject( asT( ((Throwable)o).getCause() ) );
			}
			else if ( OpenMBeanUtil.getSimpleType( o.getClass() ) == null )
			{
				illegalObject( o );
			}
		}
	}
	
	    private Serializable
	asT( final Object o )
	{
	    return Serializable.class.cast( o );
	}
	
		protected final void
	checkLegalObject(  final Object	o )
	{
		checkLegalObject( o, true );
	}
	
		protected boolean
	validateNullOrOfType(
		final String	key,
		final Class<?>	theClass )
	{
		final Object o	= getField( key );
		
		return( o == null || theClass.isAssignableFrom( o.getClass() ) );
	}
	
	
		protected final void
	validateThrow()
	{
		if ( ! validate() )
		{
			throw new IllegalArgumentException( toString() );
		}
	}
	
	    protected Serializable
	asMapHook( final String key, final Serializable value)
	{
		Serializable  result	= value;
		
		if ( result instanceof MapCapable )
		{
		    final Map<String,Serializable> m = ((MapCapable)result).asMap();
		    
		    TypeCast.checkSerializable( m );
		    
			result	= Serializable.class.cast( m );
		}
		
		return result;
	}

		public final Map<String,Serializable>
	asMap()
	{
		final Map<String,Serializable>	result	= new HashMap<String,Serializable>();
		
		// convert all MapCapable into Map
		for( final String key : getFields().keySet() )
		{
		    final Serializable value    = getField( key );
		    
			Serializable  s	= asMapHook( key, value );
			result.put( key, s );
		}
		
		result.put( MAP_CAPABLE_CLASS_NAME_KEY, getMapClassName() );
		
		checkLegalObject( result, false );
		
		return( result );
	}
	
	/**
		Make this Object immutable.
	 */
		public void
	makeImmutable()
	{
		if ( ! mWasMadeImmutable )
		{
			mFields	= Collections.unmodifiableMap( mFields );
			mWasMadeImmutable	= true;
		}
	}
	
	
	/**
		Add a field to the object.  The Object can choose to reject addition
		of the field.
	 */
		public void
	putField(
		final String key,
		final Serializable     value )
	{
		checkLegalObject( value );
		
		final Serializable newValue	= putFieldHook( key, value );
		getFields().put( key, newValue );
	}
	
		protected Serializable
	putFieldHook(
		final String        key,
		final Serializable  value )
	{
		return( value );
	}
	
	/**
		Add fields one-by-one so we can check compliance with OpenTypes
	 */
		protected <T extends Serializable>void
	putAll( final Map<String,T> m )
	{
		if ( m != null )
		{
			for( final String key : m.keySet() )
			{
				putField( key, m.get( key ) );
			}
		}
	}
	
	
		private Map<String,Serializable>
	getFields()
	{
		return( mFields );
	}
	
		protected Serializable
	getField( String key )
	{
		return( getFields().get( key ) );
	}
	
		protected final Serializable
	getObject( final String key )
	{
		return( getFields().get( key ) );
	}
	
		protected final String
	getString( final String key )
	{
		return( (String)getObject( key ) );
	}
	
		protected final String[]
	getStringArray( final String key )
	{
		return( (String[])getObject( key ) );
	}
	
		protected final Boolean
	getBoolean( final String key )
	{
		return( (Boolean)getObject( key ) );
	}
	
		protected final Byte
	getByte( final String key )
	{
		return( (Byte)getObject( key ) );
	}
	
	
		protected final boolean
	getboolean( final String key )
	{
		final Boolean	b	= getBoolean( key );
		
		if ( b == null )
		{
			throw new IllegalArgumentException( key );
		}
		
		return( b.booleanValue() );
	}
	
		protected final Integer
	getInteger( final String key )
	{
		return( (Integer)getObject( key ) );
	}

        protected final Map<String,Serializable>
    getMap( final String key )
    {
        return (Map<String,Serializable>)getObject( key );
    }

		protected final File
	getFile( final String key )
	{
		final String	s	= getString( key );
		
		return( s == null ? null : new File( s ) );
	}
	
		public boolean
	equals( final Object o )
	{
		if ( o == this )
		{
			return( true );
		}
		else if ( ! (o instanceof MapCapableBase) )
		{
			return( false );
		}
		
		boolean	equals	= false;
		
		final MapCapableBase	rhs	= (MapCapableBase)o;
		equals	= MapUtil.mapsEqual( getFields(), rhs.getFields() );
		
		return( equals );
	}
	
		public String
	toString()
	{
		return( MapUtil.toString( getFields() ) );
	}
}








