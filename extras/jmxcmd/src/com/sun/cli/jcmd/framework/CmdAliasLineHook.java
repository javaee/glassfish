/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdAliasLineHook.java,v 1.1 2003/12/15 21:58:56 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/12/15 21:58:56 $
 */
package com.sun.cli.jcmd.framework;

final class CmdAliasLineHook implements CmdReader.LineHook
{
	private final CmdAliasMgr		mMgr;
	
		public
	CmdAliasLineHook( CmdAliasMgr	mgr )
	{
		assert( mgr != null );
		mMgr	= mgr;
	}
	
		public String
	processLine( final String lineIn )
	{
		String	lineOut	= lineIn;
		
		int	tokenEnd	= lineIn.length();
		
		for( int i = 0; i < tokenEnd; ++i )
		{
			final char	c	= lineIn.charAt( i );
			
			if ( c == ' ' || c == '\t' )
			{
				tokenEnd	= i;
				break;
			}
		}
		
		final String	firstToken	= lineIn.substring( 0, tokenEnd );
		
		final String	aliasValue	= mMgr.getAliasValue( firstToken );
		if ( aliasValue != null )
		{
			lineOut	= aliasValue + lineIn.substring( firstToken.length(), lineIn.length() );
		}
		
		return( lineOut );
	}
}
