/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/JMXConnectorProvider.java,v 1.6 2004/06/04 01:06:57 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/06/04 01:06:57 $
 */
 
package com.sun.cli.jmxcmd.spi;

import javax.management.remote.JMXConnector;

/**
	Interface which allows pluggability of JMX Connectors into jmxcmd.
	
	The names HOST, PORT, etc are standard names which can be used as arguments
	to jmxcmd. They can subseqently be used as keys by the provider to look
	up their respect values from the Map.
	
 */
public interface JMXConnectorProvider
{
	// standard names for required or common parameters
	// parameters are case-insensitive
	public final static String	HOST			= "HOST";
	public final static String	PORT			= "PORT";
	public final static String	PROTOCOL		= "PROTOCOL";
	public final static String	USER			= "USER";
	public final static String	PASSWORD_FILE	= "PASSWORD_FILE";
	public final static String	TRUSTSTORE_FILE	= "TRUSTSTORE_FILE";
	public final static String	JNDI_NAME		= "JNDI_NAME";
	public final static String	URL				= "URL";
	
	/**
		eg PLAIN, CRAM-MD5, DIGEST-MD5
	 */
	public final static String	SASL			= "SASL_ALGORITHM";
	
	// these are not passable on the command line, for security reasons,
	// but are obtained from the pasword file or interactively
	public final static String	PASSWORD		= "PASSWORD";
	public final static String	TRUSTSTORE_PASSWORD	= "TRUSTSTORE_PASSWORD";
	
	// the "user name" in the password file used to look up the trust-store password
	public final static String	TRUSTSTORE_USER	= "truststore";
	
	/**
		Return TRUE if the protocol is supported.  The protcol may or may not be supplied; if not
		a decision can be made at the discretion of the implementor.
		
		Standard keys of HOST, PORT, PROTOCOL, USER, PASSWORD, PASSWORD_FILE
		will be present if they have been specified to jmxcmd.
		
		Optional keys will exist if --options was specified.  These have been
		pre-parsed and will be whatever was entered by the user.  The syntax of
		--options is as follows:
		<code>
		--options=name=value[,name=value]*
		</code>
		<p>
		Thus, each option will be placed into the map as a name/value pair.
		
		@param m	the Map containing options entered as argument to jmxcmd
		@return		true if supported, false if not
	 */
	public boolean		isSupported( java.util.Map<String,String> m );
	
	/**
		Connect to a server using the specified values found in the Map.
		
		@param m	the Map containing the connection information.
		@return		a JMXConnector
	 */
	public JMXConnector	connect( java.util.Map<String,String> m ) throws Exception;
	
}




