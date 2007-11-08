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

import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.config.MessageSecurityConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.config.ProviderConfig;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.enterprise.management.support.oldconfig.OldSecurityServiceMBean;
import com.sun.enterprise.management.support.oldconfig.OldMessageSecurityConfigMBean;

/**
 */
public final class ProviderConfigFactory extends ConfigFactory 
	// implements ProviderConfigMgr
{
	private final OldSecurityServiceMBean       mOldSecurityServiceMBean;
	private final OldMessageSecurityConfigMBean	mOldMessageSecurityConfigMBean;
	
	public ProviderConfigFactory(final ConfigFactoryCallback	callbacks)
	{
		super( callbacks );
		
        // the type=message-security MBean just doesn't work; we must use
        // the type=security-service MBean instead.
		mOldSecurityServiceMBean	=
			getOldConfigProxies().getOldSecurityServiceMBean( getConfigName() );
		
		mOldMessageSecurityConfigMBean   = getOldMessageSecurityConfigMBean();
	}
	
		private final OldMessageSecurityConfigMBean
	getOldMessageSecurityConfigMBean()
	{
		final String name   = getFactoryContainer().getName();
		return getOldConfigProxies().getOldMessageSecurityConfigMBean( name );
	}


    /**
        The underlying API is function-oriented mess. We actually have to "create" a new
        message security (which might already exist) in order to create a provider-config.
        This is a special-case compared to all the other ones.
     */
		public ObjectName	
    create(
		final String    name,
		final String    providerType, 
		final String    providerClassname,
        Map<String,String>  optional )
	{
        final MessageSecurityConfig msc = (MessageSecurityConfig)getFactoryContainer();
        final SecurityServiceConfig ss  = (SecurityServiceConfig)(msc.getContainer());
        
        final MessageSecurityConfig newMSC  = 
            ss.createMessageSecurityConfig( msc.getAuthLayer(), name, providerType, providerClassname, optional );
        // 'newMSC' and 'msc' should be the same.
        
        final ProviderConfig providerConfig = newMSC.getProviderConfigMap().get( name );
        
		return Util.getObjectName( providerConfig );
	}


		protected final void
	removeByName(final String name)
	{
		getOldMessageSecurityConfigMBean().removeProviderConfigByProviderId( name );
	}
}

















