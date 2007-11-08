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
import com.sun.appserv.management.util.misc.MapUtil;
import javax.management.AttributeList;

import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.config.ClusterRefConfigCR;
import com.sun.enterprise.management.support.oldconfig.OldClusterMBean;
import com.sun.enterprise.management.support.oldconfig.OldLbConfig;

final class ClusterRefConfigFactory extends ConfigFactory {
	
    private final OldLbConfig mOldLbConfigMBean;
    
    private final Set<String>	LEGAL_OPTIONAL_KEYS = 
        GSetUtil.newUnmodifiableStringSet(
            ClusterRefConfigCR.LB_POLICY_KEY,
            ClusterRefConfigCR.LB_POLICY_MODULE_KEY );

    protected Map<String,String> getParamNameOverrides() {
        return(MapUtil.newMap(CONFIG_NAME_KEY, "ref"));
    }

    protected Set<String> getLegalOptionalCreateKeys() {
        return(LEGAL_OPTIONAL_KEYS);
    }

    public ClusterRefConfigFactory(final ConfigFactoryCallback callbacks) {
        super(callbacks);
        final String containerName = getFactoryContainer().getName();
        mOldLbConfigMBean = getOldConfigProxies().getOldLbConfig(containerName);
    }
        
    protected ObjectName createOldChildConfig(final AttributeList translatedAttrs) {
        return mOldLbConfigMBean.createClusterRef(translatedAttrs);
    }

    public ObjectName create(final String referencedClusterName, 
        final Map<String,String> optional) {

        final String[] requiredParams = new String[] {};
        final Map<String,String> params = initParams(referencedClusterName, requiredParams, optional);
        final ObjectName amxName = createNamedChild(referencedClusterName, params);
        return(amxName);                
    }

    public ObjectName create(final String referencedClusterName, 
        final String lbPolicy, final String lbPolicyModule) {

        final Map<String,String> optionalParams = new java.util.HashMap<String,String>();
        if (lbPolicy != null)
            optionalParams.put(ClusterRefConfigCR.LB_POLICY_KEY,lbPolicy); 
        if (lbPolicyModule != null)
            optionalParams.put(ClusterRefConfigCR.LB_POLICY_MODULE_KEY, lbPolicyModule); 

        return create(referencedClusterName, optionalParams);
    }

    public void removeByName(final String referencedClusterName)	{
        mOldLbConfigMBean.removeClusterRefByRef(referencedClusterName);
    }
}