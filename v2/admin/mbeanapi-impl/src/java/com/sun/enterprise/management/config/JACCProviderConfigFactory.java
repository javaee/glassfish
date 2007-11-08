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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.enterprise.management.support.oldconfig.OldSecurityServiceMBean;

/**
 */
public final class JACCProviderConfigFactory extends ConfigFactory
{
	private static final	String	POLICY_PROVIDER_KEY = "PolicyProvider";

	private static final	String	POLICY_CONFIGURATION_FACTORY_PROVIDER_KEY	= 
		"PolicyConfigurationFactoryProvider";
	
	private final OldSecurityServiceMBean	mOldSecurityService;
	
	/**

	 */
		public
	JACCProviderConfigFactory(final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
		
		mOldSecurityService	=
		    getOldConfigProxies().getOldSecurityServiceMBean( getConfigName() );
	}

	/**
		Create a new &lt;jacc-provider&gt;
		
		@param name			name of the &lt;jacc-provider>
		@param policyProvider		a classname
		@param policyConfigurationFactoryProvider	a classname
		@param reservedForFutureUse	reserved for future use
	 */
		public ObjectName	
	create(
        String	name,
        String	policyProvider,
        String	policyConfigurationFactoryProvider,
        Map<String,String> 	reservedForFutureUse )
	{
		final String[] requiredParams = {	
			POLICY_PROVIDER_KEY,					policyProvider,	
			POLICY_CONFIGURATION_FACTORY_PROVIDER_KEY,	policyConfigurationFactoryProvider,
		};
		
		if ( name == null )
		{
		    throw new IllegalArgumentException( "" + null );
		}

		final Map<String,String> params = initParams( name, requiredParams, reservedForFutureUse );

		final ObjectName amxName = createNamedChild( name, params );

		return amxName;
	}

		public void		
	removeByName( final String name )
	{
		mOldSecurityService.removeJaccProviderByName( name );
	}

	/**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		trace( "createOldChildConfig: attrs: " + stringify( translatedAttrs ) );
		
		final ObjectName	objectName	=
				mOldSecurityService.createJaccProvider( translatedAttrs );
		
		return( objectName );
	}
}




