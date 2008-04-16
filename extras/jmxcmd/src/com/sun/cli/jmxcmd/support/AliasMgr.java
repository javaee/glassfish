/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/AliasMgr.java,v 1.2 2004/04/25 07:14:10 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/04/25 07:14:10 $
 */
 

package com.sun.cli.jmxcmd.support;

public interface AliasMgr
{
	/**
		Create a persistent alias name for the String 
		
		@param aliasName  the name of the alias
		@param value	 the ObjectName value of the alias
	 */
		public void
	createAlias( String aliasName, String value ) throws Exception;
	
	/**
		Return the value to which the alias resolves
		
		@param aliasName	the name of the alias
		@return the value of the the alias or NULL if not found
	 */
		public String
	getAliasValue( String aliasName ) ;
	
	/**
		Delete a persistent alias name, 
		
		@param aliasName  the name of the alias
	 */
		public void
	deleteAlias( String aliasName ) throws Exception;
	
	/**
		Return a list of all alias names (as an operation)
		
		@param showValues	whether to show the values or not
		@return		an array of all alias names
	 */
		public String []
	listAliases( boolean showValues ) throws Exception;
	
	/**
		Return a list of all alias names (as an Attribute)
		
		@return an array of all alias names
	 */
		public String []
	getAliases() throws Exception;
}