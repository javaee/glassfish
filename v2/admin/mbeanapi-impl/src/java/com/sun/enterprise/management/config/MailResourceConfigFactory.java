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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/MailResourceConfigFactory.java,v 1.6 2006/03/17 03:34:15 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2006/03/17 03:34:15 $
 */


package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

	
import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.appserv.management.config.MailResourceConfigKeys;
import com.sun.appserv.management.config.ResourceConfigKeys;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.Description;

public final class MailResourceConfigFactory  extends ResourceFactoryImplBase
{
		public
	MailResourceConfigFactory(
		final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}

          
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet( 
		MailResourceConfigKeys.STORE_PROTOCOL_KEY,
		MailResourceConfigKeys.STORE_PROTOCOL_CLASS_KEY,
		MailResourceConfigKeys.TRANSPORT_PROTOCOL_KEY,
		MailResourceConfigKeys.TRANSPORT_PROTOCOL_CLASS_KEY,
		MailResourceConfigKeys.DEBUG_KEY,
        Description.DESCRIPTION_KEY,        
		ResourceConfigKeys.ENABLED_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	
	public static final String HOST_KEY                     = "Host";
	public static final String USER_KEY                     = "User";
	public static final String FROM_KEY                     = "From";
	
	      
        protected Map<String,String>
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "jndi-name" ) );
	}

                
        /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
		trace( "MailResourceConfigFactory.createOldChildConfig: creating using: " +
			stringify( translatedAttrs ) );
			
		final ObjectName	objectName	=
				getOldResourcesMBean().createMailResource( translatedAttrs );
		
		return( objectName );
	}


	/**
		Create a new &lt;mail-resource>
		
		@param jndiName			
		@param host
                @param user
                @param from
		@param optional
	 */
		public ObjectName
	create( String      jndiName,
		String          host,
		String          user,
		String          from,
		Map<String,String>   			optional)
	{
		final String[] requiredParams = new String[]
		{
		HOST_KEY,       host,
		USER_KEY,       user,
		FROM_KEY,       from,
		};
		    
		final Map<String,String> params	= initParams( jndiName, requiredParams, optional );

		final ObjectName	amxName = createNamedChild( jndiName, params );

		return( amxName );                                           
	}
	
		final protected void
	removeByName( String name )
	{
	    final Set<ResourceRefConfig> refs   =
	        findAllRefConfigs( XTypes.MAIL_RESOURCE_CONFIG, name );
	    
	    if ( refs.size() == 0 )
	    {
		    getOldResourcesMBean().removeMailResourceByJndiName( name );
	    }
	    else
	    {
    	    for( final ResourceRefConfig ref : refs )
    	    {
    	        final String target = ref.getContainer().getName();
		        getOldResourcesMBean().deleteMailResource( name, target );
    	    }
		}
	}
	
	
	
}

