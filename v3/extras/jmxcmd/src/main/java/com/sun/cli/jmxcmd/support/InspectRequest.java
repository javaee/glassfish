/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.support;

import java.io.Serializable;

/**
 */
public final class InspectRequest implements Serializable
{
		public
	InspectRequest(  )
	{
		this( true );
	}
	
		private
	InspectRequest( boolean	defaultValue )
	{
		final String	names	= defaultValue ? "*" : null;
		
		includeName			= defaultValue;
		//includeMBeanInfo	= false;
		includeSummary		= defaultValue;
		includeDescription     	= false;
		attrs				= names;
		operations			= names;
		constructors		= defaultValue;
		notifications		= names;
	}
	
	//public boolean	includeMBeanInfo;
	public boolean	includeName;
	public boolean	includeSummary;
	public boolean	includeDescription;
	public String	attrs;			// comma-separated list
	public String	operations;		// comma-separated list
	public boolean	constructors;
	public String	notifications;	// comma-separated list
}