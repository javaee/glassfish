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

import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;

import com.sun.enterprise.management.support.oldconfig.OldApplicationsConfigMBean;
	

public final class LifecycleModuleConfigFactory  extends ConfigFactory
{
		public
	LifecycleModuleConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}
			  
	private static final String				  CLASSNAME_KEY			  = "class-name";
	private static final String				  CLASSPATH_KEY			  = "classpath";
	private static final String				  ENABLED_KEY				 = "enabled";
	private static final String				  LOAD_ORDER_KEY			 = "load-order";
	private static final String				  IS_FAILURE_FATAL_KEY	 = "is-failure-fatal";
	private static final String				  DESCRIPTION_KEY			= "description";


		private OldApplicationsConfigMBean
	getOldApplicationsConfigMBean()
	{
		 return getOldConfigProxies().getOldApplicationsConfigMBean();
	}

	/**
	The caller is responsible for dealing with any Properties.
	*/
	protected ObjectName createOldChildConfig( 
			  final AttributeList translatedAttrs )
	{

		 trace( "LifecycleModuleConfigFactory.createOldChildConfig: creating using: " +
			 stringify( translatedAttrs ) );

		 final ObjectName objectName = 
			  getOldApplicationsConfigMBean().createLifecycleModule(translatedAttrs);
		 
		 return( objectName );
	}

	/**
		Create a new lifecycle module with all attributes specified.

		@return the ObjectName of the MBean representing the new module
	*/
		public ObjectName
	create(
		final String name,
		final String description,
		final String classname,
		final String classpath, 
		final String loadOrder,
		final boolean	isFailureFatal,
		final boolean enabled,
		final Map<String,String>		reserved )
	{
        final String[] requiredParams =
        {	
            DESCRIPTION_KEY,		(description == null ? "" : description),
            CLASSNAME_KEY,			classname,
            CLASSPATH_KEY,			(classpath == null ? "" : classpath),
            LOAD_ORDER_KEY,			loadOrder,
            IS_FAILURE_FATAL_KEY,	"" + isFailureFatal,
            ENABLED_KEY,	        "" + enabled,
        };

        final Map<String,String> params =
            initParams( name, requiredParams, reserved );

        final ObjectName amxName =  createNamedChild( name, params );
        return( amxName );
	}


	/**
		Removes an existing lifecycle module.

		@param objectName the name of the lifecycle module to be removed.
	 */
	public void internalRemove( final ObjectName	objectName )
	{
		final String	name	= Util.getName( objectName );

		getOldApplicationsConfigMBean().removeLifecycleModuleByName( name );
	}
}



