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

import java.util.Map;

import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;

import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.Delegate;
import com.sun.appserv.management.config.LBConfig;
import com.sun.appserv.management.base.Util;


/**
	Configuration for the &lt;lb-config&gt; element.
*/
public final class LBConfigImpl extends AMXConfigImplBase 
        implements ConfigFactoryCallback {
    
    public LBConfigImpl(final Delegate delegate) {
        super(delegate);
    }
        
    public Map<String,ObjectName>	getClusterRefConfigObjectNameMap() {
        return(getContaineeObjectNameMap(XTypes.CLUSTER_REF_CONFIG));
    }

    public Map<String,ObjectName>	getServerRefConfigObjectNameMap() {
        return(getContaineeObjectNameMap(XTypes.SERVER_REF_CONFIG));
    }

/*
    private ClusterRefConfigFactory getClusterRefConfigFactory() {
        return new ClusterRefConfigFactory(this);
    }

    public ObjectName createClusterRefConfig(String referencedClusterName, 
        String lbPolicy, String lbPolicyModule) {

        return getClusterRefConfigFactory().create(referencedClusterName, 
        lbPolicy, lbPolicyModule);
    }

    public void removeClusterRefConfig( final String name) {
        final Map<String,ObjectName> items = getClusterRefConfigObjectNameMap();
        getClusterRefConfigFactory().remove(Util.getObjectName(items, name));
    }

    private ServerRefConfigFactory getServerRefConfigFactory() {
        return new ServerRefConfigFactory(this);
    }

    public ObjectName createServerRefConfig(final String referencedServerName, 
        final Map<String,String> optional) {

        return getServerRefConfigFactory().create(referencedServerName, optional);
    }

    public ObjectName createServerRefConfig(final String referencedServerName, 
        final String disableTimeoutInMinutes, final boolean lbEnabled, 
        final boolean enabled) {

        return getServerRefConfigFactory().create(referencedServerName, 
        disableTimeoutInMinutes, lbEnabled, enabled);
    }

    public void removeServerRefConfig( final String name) {
        final Map<String,ObjectName> items = getServerRefConfigObjectNameMap();
        getServerRefConfigFactory().remove(Util.getObjectName(items, name));
    }
*/
}