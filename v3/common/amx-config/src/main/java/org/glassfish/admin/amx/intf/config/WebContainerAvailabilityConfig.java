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
package org.glassfish.admin.amx.intf.config;



/**
	 Configuration for the &lt;web-container-availability> element.
 */
public interface WebContainerAvailabilityConfig extends ConfigElement, PropertiesAccess
{
    public static final String AMX_TYPE = "web-container-availability";
    
	public static final String		MEMORY			= "memory";
	public static final String		FILE			= "file";
	public static final String		HA				= "ha";
    
    
	
	public static final String WEB_CONTAINER_AVAILABILITY_ENABLED_KEY	= "AvailabilityEnabled";

	/**
		See {@link PersistenceTypeValues}.
	 */
	public static final String PERSISTENCE_TYPE_KEY					= "PersistenceType";

	/**
		See {@link SessionSaveFrequencyValues}.
	 */
	public static final String PERSISTENCE_FREQUENCY_KEY				= "PersistenceFrequency";

	/**
		See {@link SessionSaveScopeValues}.
	 */
	public static final String PERSISTENCE_SCOPE_KEY					= "PersistenceScope";

	public static final String PERSISTENCE_STORE_HEALTH_CHECK_ENABLED_KEY = "PersistenceStoreHealthCheckEnabled";

	public static final String SSO_FAILOVER_ENABLED_KEY				= "SSOFailoverEnabled";

	public static final String HTTP_SESSION_STORE_POOL_NAME_KEY		= "HTTPSessionStorePoolName";
    
    
	
	public static final String		WEB_METHOD	= "web-method";
	public static final String		TIME_BASED	= "time-based";
	public static final String		ON_DEMAND	= "on-demand";
    
    
	public static final String		SESSION				= "session";
	public static final String		MODIFIED_SESSION	= "modified-session";
	public static final String		MODIFIED_ATTRIBUTE	= "modified-attribute";
    
    
    
	public String	getAvailabilityEnabled();
	public void	setAvailabilityEnabled( final String value );

	public String	getHTTPSessionStorePoolName();
	public void	setHTTPSessionStorePoolName( final String value );

    @ResolveTo(Integer.class)
	public String	getPersistenceFrequency();
	/**
		See {@link SessionSaveFrequencyValues}.
	 */
	public void	setPersistenceFrequency( final String value );


	/**
		See {@link SessionSaveScopeValues}.
	 */
	public String	getPersistenceScope();
	
	/**
		See {@link SessionSaveScopeValues}.
	 */
	public void	setPersistenceScope( final String value );

    @ResolveTo(Boolean.class)
	public String	getPersistenceStoreHealthCheckEnabled();
	public void	setPersistenceStoreHealthCheckEnabled( final String value );

	public String	getPersistenceType();
	/**
		See {@link PersistenceTypeValues}.
	 */
	public void	setPersistenceType( final String value );

    @ResolveTo(Boolean.class)
	public String	getSSOFailoverEnabled();
	public void	setSSOFailoverEnabled( final String value );



}
