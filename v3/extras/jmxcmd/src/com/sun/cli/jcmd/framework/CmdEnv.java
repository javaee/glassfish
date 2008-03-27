/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEnv.java,v 1.4 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:16 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Set;


/**
	Commands deriving from CmdBase have a command environment available to
	them.
 */
public interface CmdEnv
{
	/**
		Remove a value from the environement.
		
		@param key	the key for the value to remove
	 */
	public void		remove( String key );
	
	/**
		Add/replace a value in the environment.
		<p>
		If a value is persistable, it should be a String when placed into the
		environment.
		
		@param key				the key for the value to remove
		@param value			the value to associate with the key
		@param allowPersistance	if true, the value will be persisted upon exit
	 */
	public void		put( String key, Object value, boolean allowPersistance );
	
	/**
		Query whether the value has been marked as persistable.
		
		@return true if persistable, false otherwise
	 */
	public boolean	isPersistable( String key );
	
	/**
		Get the value associated with the key.
		
		@return the value, or null if not found.
	 */
	public Object	get( String key );
	
	/**
		Get all keys in the environment.
		
		@return a Set of all the keys
	 */
	public Set<String>		getKeys();
	
	
	/**
		Determine if the CmdEnv needs to be saved.
		
		@return true if a save should be done, false otherwise
	 */
	public boolean		needsSave();
	
	/**
		Save the CmdEnv.
	 */
	public void		save() throws Exception;
};
	