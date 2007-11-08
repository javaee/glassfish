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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/ModuleLogLevelsConfigFactory.java,v 1.5 2006/03/09 20:30:40 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2006/03/09 20:30:40 $
 */


package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.enterprise.management.support.oldconfig.OldLogServiceMBean;	

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.config.ModuleLogLevelsConfigKeys;

public final class ModuleLogLevelsConfigFactory  extends ConfigFactory
{
	private final OldLogServiceMBean	mOldLogServiceMBean;
	
		public
	ModuleLogLevelsConfigFactory(
		final ConfigFactoryCallback	callbacks,
		final OldLogServiceMBean	old )
	{
		super( callbacks );
		
		mOldLogServiceMBean	= old;
	}
	
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet( 
		ModuleLogLevelsConfigKeys.ROOT_KEY,
		ModuleLogLevelsConfigKeys.SERVER_KEY,
		ModuleLogLevelsConfigKeys.EJB_CONTAINER_KEY,
		ModuleLogLevelsConfigKeys.CMP_CONTAINER_KEY,
		ModuleLogLevelsConfigKeys.MDB_CONTAINER_KEY,
		ModuleLogLevelsConfigKeys.WEB_CONTAINER_KEY,
		ModuleLogLevelsConfigKeys.CLASSLOADER_KEY,
		ModuleLogLevelsConfigKeys.CONFIGURATION_KEY,
		ModuleLogLevelsConfigKeys.NAMING_KEY,
		ModuleLogLevelsConfigKeys.SECURITY_KEY,
		ModuleLogLevelsConfigKeys.JTS_KEY,
		ModuleLogLevelsConfigKeys.JTA_KEY,
		ModuleLogLevelsConfigKeys.ADMIN_KEY,
		ModuleLogLevelsConfigKeys.DEPLOYMENT_KEY,
		ModuleLogLevelsConfigKeys.VERIFIER_KEY,
		ModuleLogLevelsConfigKeys.JAXR_KEY,
		ModuleLogLevelsConfigKeys.JAXRPC_KEY,
		ModuleLogLevelsConfigKeys.SAAJ_KEY,
		ModuleLogLevelsConfigKeys.CORBA_KEY,
		ModuleLogLevelsConfigKeys.JAVAMAIL_KEY,
		ModuleLogLevelsConfigKeys.JMS_KEY,
		ModuleLogLevelsConfigKeys.CONNECTOR_KEY,
		ModuleLogLevelsConfigKeys.JDO_KEY,
		ModuleLogLevelsConfigKeys.CMP_KEY,
		ModuleLogLevelsConfigKeys.UTIL_KEY,
		ModuleLogLevelsConfigKeys.RESOURCE_ADAPTER_KEY,
		ModuleLogLevelsConfigKeys.SYNCHRONIZATION_KEY,
		ModuleLogLevelsConfigKeys.NODE_AGENT_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
		public ObjectName
	create( Map<String,String> optional )
	{
		final Map<String,String>		params			= initParams( optional );

		final ObjectName	amxName	= createChild( params );
		
		return( amxName );
	}

		protected ObjectName
	createOldChildConfig( String oldType, AttributeList attrs )
	{
		final ObjectName old = mOldLogServiceMBean.createModuleLogLevels( attrs );
		return old;
	}

		protected void
	internalRemove( final ObjectName objectName )
	{
		mOldLogServiceMBean.removeModuleLogLevels();
	}

}

