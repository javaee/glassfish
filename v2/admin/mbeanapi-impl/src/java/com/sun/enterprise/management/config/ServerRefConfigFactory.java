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
 */


package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Map;

import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ServerRefConfigCR;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.LBConfig;

import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.oldconfig.OldLbConfig;
import com.sun.enterprise.management.support.oldconfig.OldClusterMBean;
import com.sun.enterprise.management.support.oldconfig.OldCreateRemoveServerRef;


final class ServerRefConfigFactory extends ConfigFactory
{
    public ServerRefConfigFactory(final ConfigFactoryCallback callbacks)
    {
        super( callbacks );
	}
                
        protected Map<String,String> 
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "ref" ) );
	}

    private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		        ServerRefConfigCR.DISABLE_TIMEOUT_IN_MINUTES_KEY,
                ServerRefConfigCR.LB_ENABLED_KEY,
                ServerRefConfigCR.ENABLED_KEY );

        protected Set<String> getLegalOptionalCreateKeys() {
		return( LEGAL_OPTIONAL_KEYS );
	}
        
        private OldCreateRemoveServerRef
    getOldCreateRemoveServerRef()
    {
        final ServerRefConfigCR     fc  = (ServerRefConfigCR)getFactoryContainer();
        final String                name = Util.asAMX( fc ).getName();
        
        OldCreateRemoveServerRef    result  = null;
        if ( fc instanceof ClusterConfig )
        {
            result  = getOldConfigProxies().getOldClusterMBean( name );
        }
        else if ( fc instanceof LBConfig )
        {
            result  = getOldConfigProxies().getOldLbConfig( name );
        }
        
        return result;
    }
    
		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		return getOldCreateRemoveServerRef().createServerRef(translatedAttrs);
	}

		public ObjectName
	create(
		final String referencedServerName, 
		final Map<String,String> optional)
	{
		final String[] requiredParams = new String[] {};

		final Map<String,String> params =
		    initParams(referencedServerName,  requiredParams, optional);

		final ObjectName amxName = createNamedChild(referencedServerName, params );

		return( amxName );                
	}

		public ObjectName
	create(
		final String referencedServerName, 
		final String disableTimeoutInMinutes,
		final boolean lbEnabled, 
		final boolean enabled)
	{
		final Map<String,String> optionalParams = new java.util.HashMap<String,String>();
		optionalParams.put( ServerRefConfigCR.DISABLE_TIMEOUT_IN_MINUTES_KEY,disableTimeoutInMinutes); 
		optionalParams.put( ServerRefConfigCR.LB_ENABLED_KEY,Boolean.toString(lbEnabled)); 
		optionalParams.put( ServerRefConfigCR.ENABLED_KEY,Boolean.toString(enabled)); 

		return create(referencedServerName, optionalParams);
	}

	public void removeByName(final String referencedServerName)
	{
		getOldCreateRemoveServerRef().removeServerRefByRef(referencedServerName);
	}
}



