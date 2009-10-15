/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OperandsInfoImpl.java,v 1.5 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
	Information about the operands a command supports
 */
public final class OperandsInfoImpl implements OperandsInfo
{
	final String	mDescription;
	final int		mMinOperands;
	final int		mMaxOperands;
	
	public final static int	NO_MAX	= OperandsInfo.NO_MAX;
	
	public static final OperandsInfoImpl	NONE	= new OperandsInfoImpl( null, 0, 0);
	
	
		public 
	OperandsInfoImpl()
	{
		this( null, 0, NO_MAX  );
	}
	
		public 
	OperandsInfoImpl( String description )
	{
		this( description, 0, NO_MAX );
	}
	
		public 
	OperandsInfoImpl( String description, int minOperands)
	{
		this( description, minOperands, NO_MAX );
	}
	
		private static String
	normalizeDescription( String d, int minOperands )
	{
		String	s	= d == null ? "" : d;
		
		if ( s.length() != 0 && s.indexOf( "<" ) < 0)
		{
			if (s.indexOf( "[" ) < 0 )
			{
				// operands should have <> around them, ensure this is the case
				String[]	values	= d.split( " " );
				
				for( int i = 0; i < values.length; ++i )
				{
					values[ i ]	= StringUtil.quote( values[ i ], '<' );
				}
				
				s	= ArrayStringifier.stringify( values, " " );
			}
			
			// if operands are optional, ensure [] surrounds them
			if ( minOperands == 0  && ! s.startsWith( "[" ) )
			{
				s	= StringUtil.quote( s, '[' );
			}
		}
		
		return( s );
	}

		public
	OperandsInfoImpl( String description, int minOperands, int maxOperands )
	{
		if ( minOperands < 0 || maxOperands < minOperands )
		{
			new Exception().printStackTrace();
			throw new IllegalArgumentException( "Illegal min/max operands: " +
				minOperands + ", " + maxOperands );
		}
		
		mMinOperands	= minOperands;
		mMaxOperands	= maxOperands;
		mDescription	= normalizeDescription( description, minOperands);
	}
	
	
		public int
	getMinOperands()
	{
		return( mMinOperands );
	}
	
		public int
	getMaxOperands()
	{
		return( mMaxOperands );
	}

		public String
	toString()
	{
		return( mDescription );
	}
}





