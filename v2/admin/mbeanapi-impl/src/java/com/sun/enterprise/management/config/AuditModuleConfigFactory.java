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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/AuditModuleConfigFactory.java,v 1.6 2006/03/09 20:30:36 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2006/03/09 20:30:36 $
 */
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.config.PropertiesAccess;

import com.sun.enterprise.management.support.oldconfig.OldSecurityServiceMBean;

/**
 */
public final class AuditModuleConfigFactory extends ConfigFactory
{
	private static final String AUDIT_ON_PROPERTY_KEY	=
		PropertiesAccess.PROPERTY_PREFIX + "auditOn";
		
	private static final	String	CLASSNAME_KEY		= "Classname";

	private final OldSecurityServiceMBean	mOldSecurityServiceMBean;
	

		public
	AuditModuleConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
		
		mOldSecurityServiceMBean =
		    getOldConfigProxies().getOldSecurityServiceMBean( getConfigName() );
	}
	
		public ObjectName	
	create( String name, String className, boolean auditOn, 
		Map<String,String> reservedForFutureUse )
	{
		final String[] requiredParams = {	
			CLASSNAME_KEY,			className,	
			AUDIT_ON_PROPERTY_KEY,	Boolean.toString(auditOn),
		};

		final Map<String,String> params = initParams( name, requiredParams, reservedForFutureUse );

		final ObjectName amxName = createNamedChild( name, params );

		return amxName;
	}

	
		protected void		
	internalRemove( final ObjectName objectName )
	{
		final String	name	= Util.getName( objectName );
		
		mOldSecurityServiceMBean.removeAuditModuleByName(name);
	}

		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		return mOldSecurityServiceMBean.createAuditModule( translatedAttrs );
	}
}




