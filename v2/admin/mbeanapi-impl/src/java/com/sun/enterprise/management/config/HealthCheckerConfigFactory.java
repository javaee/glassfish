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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/config/HealthCheckerConfigFactory.java,v 1.8 2007/04/25 23:49:33 pa100654 Exp $
 * $Revision: 1.8 $
 * $Date: 2007/04/25 23:49:33 $
 */
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collections;

import static com.sun.appserv.management.base.XTypes.SERVER_REF_CONFIG;
import static com.sun.appserv.management.base.XTypes.CLUSTER_REF_CONFIG;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.jmx.JMXUtil;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.GSetUtil;
import static com.sun.appserv.management.config.HealthCheckerConfigKeys.*;

final class HealthCheckerConfigFactory extends ConfigFactory {

    public HealthCheckerConfigFactory(final ConfigFactoryCallback callbacks) {
        super( callbacks );
    }

    private final Set<String>    LEGAL_OPTIONAL_KEYS    =
        GSetUtil.newUnmodifiableStringSet(
            URL_KEY,
            INTERVAL_IN_SECONDS_KEY,
            TIMEOUT_IN_SECONDS_KEY);

    protected Set<String> getLegalOptionalCreateKeys()  {
        return(LEGAL_OPTIONAL_KEYS);
    }

        private String
    getProps() {
        final ObjectName objectName = Util.getExtra( getFactoryContainer() ).getObjectName();
        final String lbConfigName = objectName.getKeyProperty( XTypes.LB_CONFIG); //"lb-config" );
        final String clusterName  = objectName.getKeyProperty( XTypes.CLUSTER_CONFIG );

        String props = null;
        if ( lbConfigName != null ) {
            props = Util.makeProp( "lb-config", lbConfigName );
        }
        else if ( clusterName != null ) {
            props = Util.makeProp( "cluster", clusterName );
        }
        else {
            throw new IllegalArgumentException( JMXUtil.toString(objectName) );
        }
        return props;
    }

    protected ObjectName createOldChildConfig(final AttributeList translatedAttrs) {
        final String containerName = getFactoryContainer().getName();
        if (getFactoryContainer().getJ2EEType().equals(CLUSTER_REF_CONFIG)) {
            return getOldConfigProxies().getOldClusterRefMBean(
                containerName, getProps()).createHealthChecker(translatedAttrs);
        } else if (getFactoryContainer().getJ2EEType().equals(SERVER_REF_CONFIG)) {
            return getOldConfigProxies().getOldServerRefMBean(
                containerName, getProps() ).createHealthChecker(translatedAttrs);
        }
        return null;
    }

    public ObjectName create(String url, String intervalInSeconds, String timeoutInSeconds)
    {
        Map<String,String> optional = new HashMap<String,String>();
        optional.put(URL_KEY, url);
        optional.put(INTERVAL_IN_SECONDS_KEY, intervalInSeconds);
        optional.put(TIMEOUT_IN_SECONDS_KEY, timeoutInSeconds);

        final Map<String,String> params = initParams(optional);
        final ObjectName amxName = createChild(params);
        return amxName;
    }

    public void remove(String name) {
        final String containerName = getFactoryContainer().getName();
        if (getFactoryContainer().getJ2EEType().equals(CLUSTER_REF_CONFIG)) {
            getOldConfigProxies().getOldClusterRefMBean(containerName, getProps()).removeHealthChecker();
        } else if (getFactoryContainer().getJ2EEType().equals(SERVER_REF_CONFIG)) {
            getOldConfigProxies().getOldServerRefMBean(containerName, getProps() ).removeHealthChecker();
        }
    }
}
