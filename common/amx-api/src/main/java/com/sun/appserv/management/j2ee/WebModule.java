/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/j2ee/WebModule.java,v 1.4 2007/05/05 05:30:51 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2007/05/05 05:30:51 $
 */
 
package com.sun.appserv.management.j2ee;

import java.util.Map;

import com.sun.appserv.management.base.Container;

/**
	A J2EE WebModule. Extends the J2EE management model with
	additional product-specific Attributes and operations.
	<br>
	The monitoring peer as returned from getMonitoringPeer() is
	{@link com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor}
 */
public interface WebModule extends J2EEModule, Container
{
	public final static String	J2EE_TYPE	= J2EETypes.WEB_MODULE;
	
	/**
		Note that the Attribute name is case-sensitive
		"servlets" as defined by JSR 77.
	 */
	public String[]	getservlets();
	
	/**
	 */
	public Map<String,Servlet>	getServletMap();
	
	
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
	public String	getDefaultWebXML();

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
		Startup time for this context (elapsed milliseconds).
	*/
	public long	getStartupTime();

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
