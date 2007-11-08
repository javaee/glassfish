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

import javax.management.AttributeList;
import javax.management.ObjectName;
	
import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.config.JNDIResourceConfig;
import com.sun.appserv.management.config.ResourceRefConfig;

import com.sun.appserv.management.helper.RefHelper;

import com.sun.appserv.management.util.misc.MapUtil;


/**
	MBean managing all instances of JNDIResource.
 */

public final class JNDIResourceConfigFactory  extends ResourceFactoryImplBase
{
		public
	JNDIResourceConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}

	private static final String  JNDI_LOOKUP_NAME_KEY    = "JNDILookupName";
	private static final String	FACTORY_CLASS_KEY		= "FactoryClass";
	private static final String	RES_TYPE_KEY		    = "ResType";

                
        /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		
		trace( "JNDIResourceConfigFactory.createOldChildConfig: creating using: " +
			stringify( translatedAttrs ) );
			
		final ObjectName	objectName	=
				getOldResourcesMBean().createExternalJndiResource( translatedAttrs );
		
		return( objectName );
	}
                
		protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "jndi-name" ) );
	}
                
	/**
        Creates a new &lt;external-jndi-resource&gt;.
 
        @param jndiName
        @param jndiLookupName
        @param resType
        @param factoryClass
        @param optional
	 */
		public ObjectName
	create( String    jndiName,
		String    jndiLookupName, 
		String    resType,
		String    factoryClass,
		Map<String,String> optional)
	{
		final String[] requiredParams = new String[]
		{
			JNDI_LOOKUP_NAME_KEY,       jndiLookupName,
		    RES_TYPE_KEY,               resType,
		    FACTORY_CLASS_KEY,          factoryClass,
		};
		        
		final Map<String,String> params	= initParams( jndiName, requiredParams, optional );

		final ObjectName	amxName = createNamedChild( jndiName, params );

		return( amxName );
	}
        
	
		protected void
	removeByName( final String name )
	{
	    final Set<ResourceRefConfig> refs   =
	        findAllRefConfigs( XTypes.JNDI_RESOURCE_CONFIG, name );
	    
	    if ( refs.size() == 0 )
	    {
		    getOldResourcesMBean().removeExternalJndiResourceByJndiName( name );
	    }
	    else
	    {
    	    for( final ResourceRefConfig ref : refs )
    	    {
    	        final String target = ref.getContainer().getName();
		        getOldResourcesMBean().deleteExternalJndiResource( name, target );
    	    }
		}
    }
}

