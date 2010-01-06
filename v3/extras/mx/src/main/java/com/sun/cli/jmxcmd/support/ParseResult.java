/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ParseResult.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
 

 
package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
 */
final class ParseResult
{
	final static int	LITERAL_STRING	= 0;
	final static int	OTHER			= 1;
	final static int	ARRAY			= 2;
	
	int				mType;
	String			mTypeCast;
	String			mName;
	Object			mData;
	

	ParseResult( int type, Object data)
	{
		mType		= type;
		mTypeCast	= null;
		mName		= null;
		mData		= data;
	}
	
	ParseResult( int type, Object data, String typecast)
	{
		mType		= type;
		mTypeCast	= typecast;
		mName		= null;
		mData		= data;
	}
	
	ParseResult( int type, Object data, String typecast, String name)
	{
		mType		= type;
		mTypeCast	= typecast;
		mName		= name;
		mData		= data;
	}
	
		public int
	getType()
	{
		return( mType );
	}
	
		public void
	setType( int type )
	{
		mType	= type;
		if ( ! (mData instanceof String) )
		{
			throw new IllegalArgumentException(
				"can't set non-String to type LITERAL_STRING: " + mData.getClass().getName());
		}
	}
	
		public Object
	getData()
	{
		return( mData );
	}
	
		public void
	setData( Object data )
	{
		mData	= data;
	}
	
		public void
	setTypeCast( String typeCast )
	{
		mTypeCast	= typeCast;
	}
	
		public String
	getTypeCast()
	{
		return( mTypeCast );
	}
	
	
		public void
	setName( String name )
	{
		mName	= name;
	}
	
		public String
	getName()
	{
		return( mName );
	}
	
		public String
	toString()
	{
		return( this.toString( ',' ) );
	}
	
		boolean
	equalString( String s1, String s2 )
	{
		if ( s1 == s2 )
			return( true );
		
		// they can't both be null now
		
		if ( s1 != null )
		{
			return( s1.equals( s2 ) );
		}
		
		// s2 != null
		return( s2.equals( s1 ) );
	}
	
		public static boolean
	checkEqualArrays( final ParseResult [] lhs, final ParseResult [] rhs )
	{
		if ( lhs == rhs )
			return( true );
			
		boolean	equal	= lhs.length == rhs.length;
		
		if ( equal )
		{
			for( int i = 0; i < lhs.length; ++i )
			{
				equal	= lhs[ i ].mData.equals( rhs[ i ].mData );
				if ( ! equal )
					break;
			}
		}
		
		return( equal );
	}
	
		public boolean
	equals( final Object o )
	{
		if ( o == this )
			return( true );
		if ( ! (o instanceof ParseResult) )
			return( false );
		
		
		final ParseResult	rhs	= (ParseResult)o;
		
		boolean	dataEqual	= false;
		
		if ( mData instanceof ParseResult [] )
		{
			dataEqual	= checkEqualArrays( (ParseResult [])mData, (ParseResult [])rhs.mData);
		}
		else
		{
			dataEqual	= mData.equals( rhs.mData );
		}
		
		final boolean equal	=
			mType == rhs.mType &&
			dataEqual &&
			equalString( mName, rhs.mName ) &&
			equalString( mTypeCast, rhs.mTypeCast );
		
			
		return( equal );
	}
	
		public String
	toString( final char delim )
	{
		final int 		type	= getType();
		final Object	data	= getData();
		
		String	result	= "";
		
		if ( type == ParseResult.LITERAL_STRING )
		{
			result	= (String)data;
		}
		else if ( type == ParseResult.OTHER )
		{
			result	= (String)data;
		}
		else if ( type == ParseResult.ARRAY )
		{
			final ParseResult []	contents	= (ParseResult [])data;
			
			result	= "{" + ArrayStringifier.stringify( contents, "" + delim ) + "}";
		}
		else
		{
			assert( false );
		}
		
		final String typeCast	= getTypeCast();
		if ( typeCast != null )
		{
			result	= "(" + typeCast + ")" + result;
		}
		
		final String	name	= getName();
		if ( name != null )
		{
			result	= name + "=" + result;
		}
		
		return( result );
	}
}

