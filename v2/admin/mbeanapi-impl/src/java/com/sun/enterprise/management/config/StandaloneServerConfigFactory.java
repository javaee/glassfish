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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/StandaloneServerConfigFactory.java,v 1.6 2006/03/09 20:30:41 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2006/03/09 20:30:41 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Properties;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.enterprise.management.support.oldconfig.OldServersMBean;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.config.ServerConfigKeys;


public class StandaloneServerConfigFactory extends ConfigFactory
{
	private final OldServersMBean	mOldServersMBean;
	
		public
	StandaloneServerConfigFactory( final ConfigFactoryCallback	callbacks )
	{
		super( callbacks );
		
		mOldServersMBean	= callbacks.getOldConfigProxies().getOldServersMBean();
	}
        
    private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet( 
		ServerConfigKeys.HTTP_LISTENER_1_PORT_KEY,
		ServerConfigKeys.HTTP_LISTENER_2_PORT_KEY,
		ServerConfigKeys.ORB_LISTENER_1_PORT_KEY,
		ServerConfigKeys.ADMIN_LISTENER_PORT_KEY,
		ServerConfigKeys.SSL_PORT_KEY,
		ServerConfigKeys.SSL_MUTUALAUTH_PORT_KEY,
		ServerConfigKeys.JMX_SYSTEM_CONNECTOR_PORT_KEY,
		ServerConfigKeys.JMS_PROVIDER_PORT_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	
		public ObjectName
	create(
		final String name,
		final String nodeAgentName,
		final String configName,
		final Map<String,String> optional)
	{
	    debug( "create:\n" + (optional == null ? "null" : MapUtil.toString( optional )) );
	    
		final Map<String,String> all = initParams(name, null, optional );
		final Properties props = new Properties();
		final AttributeList attrList = new AttributeList();
		translateParams(all, attrList, props);

		//create the new server with the new config that was copied
		final ObjectName oldObjectName =
			mOldServersMBean.createServerInstance(nodeAgentName, name, configName, null, props);

		return syncNewAMXMBeanWithOld(oldObjectName);
	}
        
		protected void
	removeByName(final String name)
	{
		mOldServersMBean.deleteServerInstance(name);
	}
}




