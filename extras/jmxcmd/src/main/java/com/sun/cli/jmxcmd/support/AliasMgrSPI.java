/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/AliasMgrSPI.java,v 1.1 2003/11/21 21:23:47 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:47 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Set;

public interface AliasMgrSPI
{
	/**
		Create an alias.  Preflighting has been done.  The alias should be replaced
		if it already exists.
		
		@param aliasName	name of the alias
		@param value		value of the alias
	 */
	public void		create( String aliasName, String value ) throws Exception;
	
	/**
		Lookup an alias by name.
		
		@return	alias value, or null if not found
	 */
	public String	get( String aliasName );
	
	/**
		Delete an alias by name.  Does nothing if alias does not exist.
	 */
	public void		delete( String aliasName ) throws Exception;
	
	/**
		Return a Set of all valid alias names.
	 */
	public Set		getNames();
};


