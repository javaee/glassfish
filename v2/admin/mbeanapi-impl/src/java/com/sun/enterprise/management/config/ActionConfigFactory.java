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

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.config.ManagementRuleConfig;
import com.sun.appserv.management.config.ActionConfig;

import com.sun.enterprise.management.support.oldconfig.OldManagementRules;


/**
 */
public final class ActionConfigFactory extends ConfigFactory
{
		public
	ActionConfigFactory( final ConfigFactoryCallback callbacks )
	{
		super( callbacks );
	}
	
	    private OldManagementRules
	getOldManagementRules()
	{
	    return getOldConfigProxies().getOldManagementRules( getConfigName() );
	}

		public ObjectName
	create( final String mbeanName )
	{
        if ( mbeanName == null )
        {
            throw new IllegalArgumentException();
        }
            
	    final ManagementRuleConfig  ruleConfig  =
	        (ManagementRuleConfig)getFactoryContainer();
        
        ActionConfig actionConfig = ruleConfig.getActionConfig();
        if ( actionConfig != null )
        {
            throw new IllegalStateException( "action already exists as " +
                        actionConfig.getName() + ", " + actionConfig.getActionMBeanName());
        }
        
	    final String ruleName   = ruleConfig.getName();
        getOldManagementRules().addActionToManagementRule( ruleName, mbeanName );
	    
	    // wait till AMX version makes its appearance
        // This is clumsy, but unfortunately the MBean name is not returned by addActionToManagementRule()
	    while ( (actionConfig = ruleConfig.getActionConfig()) == null )
	    {
	        sleepMillis( 10 );
	    }
	    
	    final ObjectName    amxObjectName   = Util.getObjectName( actionConfig );
		getCallbacks().sendConfigCreatedNotification( amxObjectName );
		
	    return amxObjectName;
	}

		protected void
	internalRemove(final ObjectName	objectName)
	{
	    throw new RuntimeException( "not supported" );
	}
}




