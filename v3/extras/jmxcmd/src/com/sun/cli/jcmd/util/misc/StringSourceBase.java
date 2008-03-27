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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/StringSourceBase.java,v 1.2 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:24 $
 */
 
package com.sun.cli.jcmd.util.misc;

/**
	Default base class for StringSource implementationsn.
 */
public class StringSourceBase implements StringSource
{
	private final StringSource	mNext;
	
	private final static class EmptyStringSource implements StringSource
	{
		public final static EmptyStringSource INSTANCE	= new EmptyStringSource();
		public EmptyStringSource()	{}
		public String	getString( String id )	{ return( id ); }
		public String	getString( String id, String defaultValue )	{ return( defaultValue ); }
	}
	
		public
	StringSourceBase()
	{
		this( EmptyStringSource.INSTANCE );
	}
	
		public
	StringSourceBase( StringSource next )
	{
		mNext	= next == null ? EmptyStringSource.INSTANCE : next;
	}
	
		public String
	getString( String id )
	{
		return( getString( id, id ) );
	}
	
		public String
	getString( String id, String defaultValue )
	{
		return( mNext.getString( id, defaultValue ) );
	}
};



