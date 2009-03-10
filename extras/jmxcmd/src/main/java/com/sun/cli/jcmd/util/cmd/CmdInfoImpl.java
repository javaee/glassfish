/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.util.cmd;

import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

/**
 */
public class CmdInfoImpl implements CmdInfo
{
	private final String		mName;
	private final OptionsInfo	mOptionsInfo;
	private final OperandsInfo	mOperandsInfo;
	
	private final static String	SPACE	= " ";
	
	
	public String		getName()				{ return( mName ); }
	public OptionsInfo	getOptionsInfo()		{ return( mOptionsInfo ); }
	public OperandsInfo	getOperandsInfo()		{ return( mOperandsInfo ); }
	
		public
	CmdInfoImpl( String name )
	{
		this( name, OptionsInfoImpl.NONE, OperandsInfoImpl.NONE );
	}
	
		public
	CmdInfoImpl( String name, OperandsInfo operandsInfo )
	{
		this( name, OptionsInfoImpl.NONE, operandsInfo );
	}
	
		public
	CmdInfoImpl( String name, OptionsInfo optionsInfo, OperandsInfo operandsInfo )
	{
		mName					= name;
		mOptionsInfo			= optionsInfo == null ? OptionsInfoImpl.NONE : optionsInfo;
		mOperandsInfo			= operandsInfo == null ? OperandsInfoImpl.NONE : operandsInfo;
	}
	
		public String
	toString()
	{
		return( getSyntax() );
	}
	
		public String
	getSyntax()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( getName() );
		
		if ( mOptionsInfo != null && mOptionsInfo.getOptionInfos().size() != 0 )
		{
			buf.append( SPACE );
			buf.append( mOptionsInfo.toString() );
		}
		
		if ( mOperandsInfo != null )
		{
			final String	s	= mOperandsInfo.toString();
			if ( s.length() != 0 )
			{
				buf.append( SPACE );
				buf.append(  mOperandsInfo.toString() );
			}
		}
		
		return( buf.toString() );
	}
}





