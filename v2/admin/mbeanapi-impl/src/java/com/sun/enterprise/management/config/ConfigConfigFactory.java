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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ConfigConfigFactory.java,v 1.8 2006/03/17 03:34:15 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2006/03/17 03:34:15 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Properties;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;


import com.sun.appserv.management.base.XTypes;
import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.Delegate;

import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ConfigConfigKeys;

import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.oldconfig.OldConfigsMBean;

/**
 */
public final class ConfigConfigFactory extends ConfigFactory
{
    private String  mSrcConfigName;
    private String  mNewConfigName;
    
		public
	ConfigConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
		
		mSrcConfigName  = null;
		mNewConfigName  = null;
	}
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		ConfigConfigKeys.DYNAMIC_RECONFIGURATION_ENABLED_KEY,
		ConfigConfigKeys.SRC_CONFIG_NAME_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	
		protected ObjectName
	createOldChildConfig(
	    final AttributeList translatedAttrs,
	    final Properties    props )
	{
		trace( "ConfigConfigFactory.createOldChildConfig: creating using: " +
		    stringify( translatedAttrs ) );

        final OldConfigsMBean  old = getOldConfigProxies().getOldConfigsMBean();
        
		final ObjectName objectName = old.copyConfiguration( mSrcConfigName, mNewConfigName, props);

		return( objectName );
	}

		public ObjectName
	create(
	    final String name,
	    final Map<String,String> optional)
	{
		final String[] requiredParams = new String[] {};
		final Map<String,String> params = initParams(name, requiredParams, optional);
		
		mNewConfigName  = name;
        mSrcConfigName   = (String)optional.get( ConfigConfigKeys.SRC_CONFIG_NAME_KEY );
        if ( mSrcConfigName == null )
        {
            mSrcConfigName   = ConfigConfigKeys.DEFAULT_SRC_CONFIG_NAME;
        }
        
        
		final ObjectName amxName = createNamedChild(name, params);
		
        
		// set any optional params
		if ( amxName != null )
		{
		    final ConfigConfig  config  = getCallbacks().getProxyFactory().getProxy( amxName, ConfigConfig.class);
		    
    		Object value;
    		
    		value   = optional.get( ConfigConfigKeys.DYNAMIC_RECONFIGURATION_ENABLED_KEY );
    		if ( value != null )
    		{
    		    config.setDynamicReconfigurationEnabled( Boolean.parseBoolean( "" + value ) );
    		}
		}
		
		return( amxName );
	}


		protected void
	removeByName(String name)
	{
		getOldConfigProxies().getOldConfigsMBean().deleteConfiguration(name);
	}
}




