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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/LoadBalancerConfigFactory.java,v 1.5 2006/03/09 20:30:39 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2006/03/09 20:30:39 $
 */
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collections;

import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.config.LoadBalancerConfigKeys;

final class LoadBalancerConfigFactory extends ConfigFactory {

    public LoadBalancerConfigFactory(final ConfigFactoryCallback callbacks) {
        super(callbacks);
    }

    private final Set<String> LEGAL_OPTIONAL_KEYS = 
        GSetUtil.newUnmodifiableStringSet(
            LoadBalancerConfigKeys.NAME,
            LoadBalancerConfigKeys.LB_CONFIG_NAME,
            LoadBalancerConfigKeys.AUTO_APPLY_ENABLED );

    protected Set<String> getLegalOptionalCreateKeys()  {
        return LEGAL_OPTIONAL_KEYS;
    }

    public ObjectName create(String name, String lbConfigName, 
        boolean autoApplyEnabled, Map<String,String> optional) {
        final String[] required =
            new String[] {"lb-config-name", lbConfigName, "auto-apply-enabled", "" + autoApplyEnabled };
        final Map<String,String> params = initParams(name, required, optional);
        final ObjectName amxName = createNamedChild(name, params);
        return amxName;
    }

    protected ObjectName createOldChildConfig(final AttributeList translatedAttrs) {
        return getOldConfigProxies().getOldLoadBalancers().createLoadBalancer(translatedAttrs);
    }
	
    public void removeByName(String name) {
        getOldConfigProxies().getOldLoadBalancers().removeLoadBalancerByName(name);
    }    
}





