/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/RequiredOptionDependency.java,v 1.3 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:19 $
 */
package com.sun.cli.jcmd.util.cmd;


/**
	A dependency denoting required option(s).
 */
public final class RequiredOptionDependency extends OptionDependency
{
	/**
		Create a new instance specifying that these OptionInfos are
		required in conjunction with an option carrying this dependency.
	 */
		public
	RequiredOptionDependency( final OptionInfo[] refs )
	{
		super( refs );
	}
	
	/**
	 */
		public
	RequiredOptionDependency( OptionInfo ref )
	{
		this( new OptionInfo[] { ref } );
	}
	
		public
	RequiredOptionDependency( OptionInfo ref1, OptionInfo ref2 )
	{
		this( new OptionInfo[] { ref1, ref2 } );
	}
	
		public
	RequiredOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3 } );
	}
	
		public
	RequiredOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3,
		OptionInfo		ref4  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4 } );
	}
}


