/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/DisallowedOptionDependency.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
package com.sun.cli.jcmd.util.cmd;


/**
	A dependency denoting a disallowed option (one that may not ocurr in conjunction
	with the option having this dependency).
 */
public final class DisallowedOptionDependency extends OptionDependency
{
	/**
		Create a new instance specifying that these OptionInfos are
		not allowed in conjunction with an option carrying this dependency.
	 */
		public
	DisallowedOptionDependency( OptionInfo[] refs )
	{
		super( refs );
	}
	
	/**
	 */
		public
	DisallowedOptionDependency( OptionInfo ref )
	{
		this( new OptionInfo[] { ref } );
	}
	
		public
	DisallowedOptionDependency( OptionInfo ref1, OptionInfo ref2 )
	{
		this( new OptionInfo[] { ref1, ref2 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5 } );
	}
	
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 ,
		OptionInfo		ref6 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5, ref6 } );
	}
	
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 ,
		OptionInfo		ref6,
		OptionInfo		ref7 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5, ref6, ref7} );
	}
}

