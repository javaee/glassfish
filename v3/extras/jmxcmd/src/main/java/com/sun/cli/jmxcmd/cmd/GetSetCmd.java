/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/GetSetCmd.java,v 1.2 2003/12/17 04:31:11 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/12/17 04:31:11 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jcmd.framework.CmdEnv;


/**
	Base class for GetCmd and SetCmd.
 */
public abstract class GetSetCmd extends JMXCmd
{
		protected
	GetSetCmd( final CmdEnv env )
	{
		// disallow instantiation
		super( env );
	}
	
		String
	getAttributes()
	{
		// guaranteed to be at least one
		return( getOperands()[ 0 ] );
	}
	
		protected String []
	getTargets()
	{
		final String []		operands	= getOperands();
		String []	targets	= null;
		
		if ( operands.length == 1 )
		{
			// one operand; that is the attribute list
			// so get attributes on current target
			targets	= getEnvTargets( );
		}
		else
		{
		
			// first operand is attributes, subsequent are the targets
			targets	= new String [ operands.length - 1 ];
			
			for( int i = 0; i < targets.length; ++i )
			{
				targets[ i ]	= operands[ i + 1 ];
			}
		}
		
		return( targets );
	}
}


















