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

import java.util.Set;
import java.util.Map;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;


import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfigKeys;
import com.sun.appserv.management.config.ResourceRefConfig;

/**
	
 */
public final class PersistenceManagerFactoryResourceConfigFactory  extends ResourceFactoryImplBase
{
		public
	PersistenceManagerFactoryResourceConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}

	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		    PersistenceManagerFactoryResourceConfigKeys.FACTORY_CLASS_KEY,
		    PersistenceManagerFactoryResourceConfigKeys.JDBC_RESOURCE_JNDI_NAME_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	   
	    /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		final ObjectName	objectName	=
				getOldResourcesMBean().createPersistenceManagerFactoryResource( translatedAttrs );
		
		return( objectName );
	}
	            
		protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "jndi-name" ) );
	}
	            
	            
	/**
		Create a new &lt;persistence-resource%gt;
		
		@param jndiName				
		@param optional
	 */
		public ObjectName
	create( final String jndiName, final Map<String,String> optional)
	{
		final String[] requiredParams = new String[]{};
		final Map<String,String> params	= initParams( jndiName, requiredParams, optional );
		final ObjectName	amxName = createNamedChild( jndiName, params );
		return( amxName );
	}

		protected void
	removeByName( final String name )
	{
	    final Set<ResourceRefConfig> refs   =
	        findAllRefConfigs( XTypes.PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG, name );
	    
	    if ( refs.size() == 0 )
	    {
		    getOldResourcesMBean().removePersistenceManagerFactoryResourceByJndiName( name );
	    }
	    else
	    {
    	    for( final ResourceRefConfig ref : refs )
    	    {
    	        final String target = ref.getContainer().getName();
		        getOldResourcesMBean().deletePersistenceManagerFactoryResource( name, target );
    	    }
		}
    }
	
}

