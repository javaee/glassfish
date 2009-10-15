/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OptionDependency.java,v 1.3 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:18 $
 */
package com.sun.cli.jcmd.util.cmd;

import org.glassfish.admin.amx.util.ArrayUtil;


/**
	Base class for denoting option dependencies.
 */
public abstract class OptionDependency
{
	final OptionInfo[]	mRefs;
	
	/**
		Create a new dependency of the specified type.
		
		@param refs 	the relevant options
	 */
		public
	OptionDependency( final OptionInfo[] refs )
	{
		mRefs	= refs;
	}
	
	/**
		Create a new dependency of the specified type with a single requirement.
		
		@param ref 	the relevant option
	 */
		public
	OptionDependency( OptionInfo ref )
	{
		this( new OptionInfo[] { ref } );
	}
	
	
		public OptionInfo[]
	getRefs()
	{
		return( mRefs );
	}
	
		public boolean
	equals( Object rhs )
	{
		if ( this == rhs )
			return( true );
			
		boolean	equals	= false;
		
		if ( rhs != null && rhs.getClass() == this.getClass() )
		{
			final OptionDependency	other = (OptionDependency)rhs;
			
			equals	= mRefs != null && ArrayUtil.arraysEqual( mRefs, other.mRefs );
		}
		return( equals );
	}
}


