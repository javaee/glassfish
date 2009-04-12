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


package com.sun.appserv.management.config;

import com.sun.appserv.management.base.XTypes;



/**
	 Configuration for an &lt;http-listener&gt; element.
*/
@AMXCreateInfo(paramNames={"id", "address", "port", "default-virtual-server", "server-name", "optional"})
public interface HTTPListenerConfig
	extends PropertiesAccess, NamedConfigElement, SSLConfigContainer, Enabled, DefaultValues
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.HTTP_LISTENER_CONFIG;
    
    @ResolveTo(Integer.class)
	public String	getAcceptorThreads();
	public void		setAcceptorThreads( String value );

	public String	getAddress();
	public void		setAddress( String value );

	public String	getDefaultVirtualServer();
	public void		setDefaultVirtualServer( String value );

	/**


		The port can be either a number or a system property ${...}, thus its
		type is String.
	 */
    @ResolveTo(Integer.class)
	public String	getPort();
	public void		setPort( String value );

	/**


		The port can be either a number or a system property ${...}, thus its
		type is String.
	 */
    @ResolveTo(Integer.class)
	public String	getRedirectPort();
	public void		setRedirectPort( String value );

    @ResolveTo(Boolean.class)
	public String	getSecurityEnabled();
	public void		setSecurityEnabled( String value );

	public String	getServerName();
	public void		setServerName( String value );

    @ResolveTo(Boolean.class)
	public String	getXpoweredBy();
	public void		setXpoweredBy( String value );

	/**
		See {@link HTTPListenerConfigFamilyValues}.
	*/
	public String	getFamily();
	/**
		@param value Valid values are as defined in {@link HTTPListenerConfigFamilyValues}.
	*/
	public void	setFamily( final String value );

    @ResolveTo(Integer.class)
	public String	getExternalPort();
	public void	setExternalPort( final String value );

    @ResolveTo(Boolean.class)
	public String	getBlockingEnabled();
	public void	setBlockingEnabled( final String value );
}
