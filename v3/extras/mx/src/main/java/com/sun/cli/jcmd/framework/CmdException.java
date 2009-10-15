/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdException.java,v 1.3 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:42 $
 */
package com.sun.cli.jcmd.framework;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

public class CmdException extends Exception
{
    static final long serialVersionUID = 8106277925004114576L;
    
	final String	mSubCmdName;
	final Integer	mErrorCode;
	
	public static final int	GENERAL_ERROR_CODE				= 1;
	public static final int	ILLEGAL_USAGE_ERROR				= 2;
	public static final int	INVALID_COMMAND_ERROR			= 3;
	public static final int	WRONG_NUMBER_OF_OPERANDS_ERROR	= 4;
	public static final int	IO_EXCEPTION					= 5;
	
		public
	CmdException( String subCmdName, String msg )
	{
		this( subCmdName, msg, GENERAL_ERROR_CODE );
	}
	
		public
	CmdException( String subCmdName, int errorCode )
	{
		this( subCmdName, "<no message>", errorCode );
	}
	
		public
	CmdException( String subCmdName, String msg, int errorCode )
	{
		super( msg );
		mSubCmdName	= subCmdName;
		mErrorCode	= new Integer( errorCode );
	}
	
		public
	CmdException( String subCmdName, String msg, int errorCode, Throwable cause)
	{
		super( msg, cause );
		mSubCmdName	= subCmdName;
		mErrorCode	= new Integer( errorCode );
	}
	
		String
	getSubCmdName()
	{
		return( mSubCmdName );
	}
	
		protected static String
	quote( Object o )
	{
		return( "\"" + SmartStringifier.toString( o ) + "\"" );
	}
	
		int
	getErrorCode()
	{
		return( mErrorCode.intValue() );
	}
	
		public boolean
	isUsageError()
	{
		final int	e	= getErrorCode();
		
		return( 
				e == CmdException.ILLEGAL_USAGE_ERROR ||
				e == INVALID_COMMAND_ERROR ||
				e == WRONG_NUMBER_OF_OPERANDS_ERROR  );
	}
	
		public String
	getErrorCodeName()
	{
		String	name	= "GENERAL_ERROR";
		
		switch( getErrorCode() )
		{
			case ILLEGAL_USAGE_ERROR:	name		= "ILLEGAL_USAGE";	break;
			case INVALID_COMMAND_ERROR:	name		= "INVALID_COMMAND";break;
			case WRONG_NUMBER_OF_OPERANDS_ERROR:	name	= "WRONG_NUMBER_OF_OPERANDS";break;
			case IO_EXCEPTION:			name = "IO_EXCEPTION";break;
		}
		
		return( name );
	}
}
