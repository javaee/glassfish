/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/InvokeResultStringifier.java,v 1.3 2004/04/26 07:29:39 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/04/26 07:29:39 $
 */
 

package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.stringifier.*;

/**
 */
public class InvokeResultStringifier implements Stringifier
{
	public final static InvokeResultStringifier	DEFAULT = new InvokeResultStringifier();
	
		public String
	stringify( Object o )
	{
		final InvokeResult	result	= (InvokeResult)o;
		
		String	str	= SupportUtil.getObjectNameDisplay( result.mObjectName ) + "\n";
		
		if ( result.noSuchMethod() )
		{
			str	= str + "<operation not found>";
		}
		else if ( result.getThrowable() != null )
		{
			str	= str + "Exception: " +
				result.getThrowable().getClass().getName() + " = " + 
				result.getThrowable().getMessage();
		}
		else
		{
			if ( result.getResult() == null )
			{
				str	= str + "<null>";
			}
			else
			{
				str	= str + new SmartStringifier( "\n", false ).stringify( result.mResult );
			}
		}
		
		return( str );
	}
}