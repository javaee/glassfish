/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ParseResultStringifier.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
 

package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.stringifier.*;


/**
 */
public class ParseResultStringifier implements Stringifier
{
	final char	mArrayDelim;
	
		public
	ParseResultStringifier( char arrayDelim )
	{
		mArrayDelim	= arrayDelim;
	}
	
		public
	ParseResultStringifier(  )
	{
		mArrayDelim	= ',';
	}
	
	
		public String
	stringify( Object o )
	{
		final ParseResult	pr	= (ParseResult)o;
		
		
		return( pr.toString( mArrayDelim ) );
	}
}



