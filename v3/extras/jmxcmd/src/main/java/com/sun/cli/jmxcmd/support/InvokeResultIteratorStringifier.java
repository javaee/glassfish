/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/InvokeResultIteratorStringifier.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
 

package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.stringifier.*;

/**
 */
public class InvokeResultIteratorStringifier extends IteratorStringifierBase
{
		public 
	InvokeResultIteratorStringifier( String delim )
	{
		super(  delim, InvokeResultStringifier.DEFAULT );
	}
	
	/**
		Subclass may choose to override this.
	 */
		public void
	stringifyElement(
		Object			elem,
		String			delim,
		StringBuffer	buf)
	{
		final InvokeResult	r	= (InvokeResult)elem;
		
		final InvokeResult.ResultType	result	= r.getResultType();
		
		// don't output "not found" results; only those that
		// were invoked and succeeded or failed
		if ( result == InvokeResult.SUCCESS ||
			result == InvokeResult.FAILURE )
		{
			buf.append( mElementStringifier.stringify( elem ) );
			buf.append( delim );
		}
	}
}