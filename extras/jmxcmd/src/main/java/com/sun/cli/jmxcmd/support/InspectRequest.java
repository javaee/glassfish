/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/InspectRequest.java,v 1.2 2004/01/26 21:20:43 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 21:20:43 $
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
	
		public
	InspectRequest( boolean	defaultValue )
	{
		final String	names	= defaultValue ? "*" : null;
		
		includeName			= defaultValue;
		includeMBeanInfo	= false;
		includeSummary		= defaultValue;
		includeDescription	= defaultValue;
		attrs				= names;
		operations			= names;
		constructors		= defaultValue;
		notifications		= names;
	}
	
	public boolean	includeMBeanInfo;
	public boolean	includeName;
	public boolean	includeSummary;
	public boolean	includeDescription;
	public String	attrs;			// comma-separated list
	public String	operations;		// comma-separated list
	public boolean	constructors;
	public String	notifications;	// comma-separated list
}