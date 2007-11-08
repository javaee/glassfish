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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/CustomMBeanConfigFactory.java,v 1.4 2006/03/09 20:30:37 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2006/03/09 20:30:37 $
 */


package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;
import java.util.Properties;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Container;

import com.sun.appserv.management.config.CustomMBeanConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.RefConfig;

import com.sun.appserv.management.helper.RefHelper;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.oldconfig.OldApplicationsConfigMBean;

import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;


public final class CustomMBeanConfigFactory  extends ConfigFactory
{
		public
	CustomMBeanConfigFactory(
		final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}
                
  /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
	    final Map<String,String> m   = 
	        JMXUtil.attributeListToStringMap( translatedAttrs );
	    
	    final String    DOMAIN  = null; // magic value 
		final String	name	= getOld().createMBean( DOMAIN, m );
		
		final ObjectName  objectName  = getOld().getMbeanByName( name );
		
	    debug( "created custom MBean with name: " + objectName );
		return( objectName );
	}

        private OldApplicationsConfigMBean
    getOld()
    {
        return getOldConfigProxies().getOldApplicationsConfigMBean();
    }

		public ObjectName
	create(
        final String    name,
        final String    implClassname,
        final String    objectName,
        final boolean   enabled,
        final Map<String,String> optional  )
	{
		final String[] requiredParams = new String[]
		{
		CustomMBeanConstants.NAME_KEY,             name,
		CustomMBeanConstants.IMPL_CLASS_NAME_KEY,  implClassname,
		CustomMBeanConstants.OBJECT_NAME_KEY,      objectName,
		CustomMBeanConstants.ENABLED_KEY,     "" + enabled,
		};
		final Map<String,String> params	= initParams( name, requiredParams, optional );

		final ObjectName	amxName = createNamedChild( name, params );
		return amxName;           
	}
	
	
	    private final String
	getRefContainerName( final String name )
	{
	    final QueryMgr  queryMgr    = getDomainRoot().getQueryMgr();
	    
	    // this is all bug-workaround code
	    final Set<DeployedItemRefConfig> refs   =
	        RefHelper.findAllRefConfigsWithName( queryMgr, XTypes.DEPLOYED_ITEM_REF_CONFIG, name );
	       
	    final RefConfig ref = GSetUtil.getSingleton( refs );
	    final Container refContainer   = ref.getContainer();
	    
	    return refContainer.getName();
	}
	
	
		final public void
	removeByName( final String name )
	{
	    // bug-workaround code
		getOld().deleteMBean( getRefContainerName( name ), name );
	}
}





