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
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ProviderConfig;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.enterprise.management.support.oldconfig.OldSecurityServiceMBean;
import com.sun.enterprise.management.support.oldconfig.OldConfigsMBean;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;


import com.sun.appserv.management.config.MessageSecurityConfigKeys;

/**
 */
public final class MessageSecurityConfigFactory extends ConfigFactory 
{
	private final OldSecurityServiceMBean	mOldSecurityServiceMBean;
	private final OldConfigsMBean           mOldConfigs;
	
		public
	MessageSecurityConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
		
		mOldSecurityServiceMBean	=
			getOldConfigProxies().getOldSecurityServiceMBean( getConfigName() );
        
        mOldConfigs = getOldConfigProxies().getOldConfigsMBean();
	}
    
		public ObjectName	
    create(
        final String  authLayer,
        final String  providerID,
        final String  providerType, 
	    final String  providerClassname,
        Map<String,String>  optional )
	{
        final String requestAuthSource  = null;
        final String requestAuthRecipient  = null;
        final String responseAuthSource  = null;
        final String responseAuthRecipient  = null;
        final boolean   isDefaultProvider   = false;
        final java.util.Properties props  = null;
        final String targetName = getConfigName();
        
        final ObjectName provider = mOldConfigs.createMessageSecurityProvider(
            authLayer,
            providerID,
            providerType,
            providerClassname,
            requestAuthSource,
            requestAuthRecipient,
            responseAuthSource,
            responseAuthRecipient,
            isDefaultProvider,
            props,
            targetName
            );
        // the resulting MBean is the *provider*, NOT the type=message-security mbean
        getLogger().info( "OBJECTNAME: " + JMXUtil.toString( provider ) );
        
        final ObjectName providerObjectName = finish( provider, null );
        final ProviderConfig providerConfig = (ProviderConfig)
            Util.getExtra(getFactoryContainer()).getProxyFactory().getProxy( providerObjectName );
        
		final ObjectName amxName = Util.getExtra(providerConfig.getContainer()).getObjectName();
        
		return amxName;
	}


		protected final void		
	removeByName( final String authLayer )
	{
		mOldSecurityServiceMBean.removeMessageSecurityConfigByAuthLayer( authLayer );
	}
}






