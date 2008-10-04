/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/WebModule.java,v 1.4 2004/10/14 19:07:08 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:08 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import java.util.Set;

/**
 */
public interface WebModule extends J2EEModule
{
	public final static String	J2EE_TYPE	= J2EETypes.WEB_MODULE;
	
	/**
		Note that the Attribute name is case-sensitive
		"servlets" as defined by JSR 77.
	 */
	public String[]	getservlets();
	
	/**
	 */
	public Set	getServletSet();
	
	
	/**
		Allow symlinking to outside the webapp root directory,
		if the webapp is an exploded directory
	*/
	public boolean	getAllowLinking();
	/**
		Allow symlinking to outside the webapp root directory,
		if the webapp is an exploded directory
	*/
	public void	setAllowLinking( final boolean value );

	/**
		Maximum cache size in KB
	*/
	public int	getCacheMaxSize();
	/**
		Maximum cache size in KB
	*/
	public void	setCacheMaxSize( final int value );

	/**
		Time interval in ms between cache refeshes
	*/
	public int	getCacheTTL();
	/**
		Time interval in ms between cache refeshes
	*/
	public void	setCacheTTL( final int value );

	/**
		Should we cache static resources for this webapp
	*/
	public boolean	getCachingAllowed();
	/**
		Should we cache static resources for this webapp
	*/
	public void	setCachingAllowed( final boolean value );

	/**
		Should case sensitivity checks be performed
	*/
	public boolean	getCaseSensitive();
	/**
		Should case sensitivity checks be performed
	*/
	public void	setCaseSensitive( final boolean value );

	/**
		Object names of all children
	public ObjectName[]	getChildren();
	*/

	/**
		The compiler classpath to use
	*/
	public String	getCompilerClasspath();
	/**
		The compiler classpath to use
	*/
	public void	setCompilerClasspath( final String value );

	/**
		Should we attempt to use cookies for session id communication?
	*/
	public boolean	getCookies();
	/**
		Should we attempt to use cookies for session id communication?
	*/
	public void	setCookies( final boolean value );

	/**
		Should we allow the ServletContext.getContext() method
		to access the context of other web applications in this server?
	*/
	public boolean	getCrossContext();
	/**
		Should we allow the ServletContext.getContext() method
		to access the context of other web applications in this server?
	*/
	public void	setCrossContext( final boolean value );

	/**
		The debugging detail level for this component
	*/
	public int	getDebug();
	/**
		The debugging detail level for this component
	*/
	public void	setDebug( final int value );

	/**
		Location of the default web.xml resource or file
	*/
	public String	getDefaultWebXml();

	/**
		The document root for this web application
	*/
	public String	getDocBase();

	/**
		An array of URL addresses defined in this Web Module to invoke web services endpoints implementations
	*/
	public String[]	getEndpointAddresses();

	/**
		Name of the engine domain, if different from the context domain
	*/
	public String	getEngineName();
	/**
		Name of the engine domain, if different from the context domain
	*/
	public void	setEngineName( final String value );

	/**
		True if the web module implements web services endpoints
	*/
	public boolean	getHasWebServices();

	/**
		The frequency of the manager checks (expiration and passivation)
	*/
	public int	getManagerChecksFrequency();
	/**
		The frequency of the manager checks (expiration and passivation)
	*/
	public void	setManagerChecksFrequency( final int value );

	/**
		Type of the modeled resource. Can be set only once
	*/
	public String	getModelerType();

	/**
		The DefaultContext override flag for this web application
	*/
	public boolean	getOverride();
	/**
		The DefaultContext override flag for this web application
	*/
	public void	setOverride( final boolean value );

	/**
		The context path for this Context
	*/
	public String	getPath();
	/**
		The context path for this Context
	*/
	public void	setPath( final String value );

	/**
		The reloadable flag for this web application
	*/
	public boolean	getReloadable();
	/**
		The reloadable flag for this web application
	*/
	public void	setReloadable( final boolean value );

	/**
		 Names of all the defined resource references for this application.
	*/
	public String[]	getResourceNames();

	/**
		Startup time for this context
	*/
	public long	getStartupTime();

	/**
		Current state of this component
	*/
	public int	getState();

	/**
		Flag to set to cause the system.out and system.err to be redirected to the logger when executing a servlet
	*/
	public boolean	getSwallowOutput();
	/**
		Flag to set to cause the system.out and system.err to be redirected to the logger when executing a servlet
	*/
	public void	setSwallowOutput( final boolean value );

	/**
		Time spend scanning jars for TLDs for this context
	*/
	public long	getTLDScanTime();
	/**
		Time spend scanning jars for TLDs for this context
	*/
	public void	setTLDScanTime( final long value );

	/**
		Create a JNDI naming context for this application?
	*/
	public boolean	getUseNaming();
	/**
		Create a JNDI naming context for this application?
	*/
	public void	setUseNaming( final boolean value );

	/**
		The welcome files for this context
	*/
	public String[]	getWelcomeFiles();

	/**
		The pathname to the work directory for this context
	*/
	public String	getWorkDir();


// -------------------- Operations --------------------

	/**
	*/
	public void	reload();
}
